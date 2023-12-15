package cn.jgayb.fenixplugin;

import com.google.common.collect.Collections2;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.xml.DomElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.psi.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jg.wang on 2021/6/16.
 * Description:
 */
public class FenixItemLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        CommonProcessors.CollectProcessor<IdDomElement> processor = new CommonProcessors.CollectProcessor<IdDomElement>();
        if (element instanceof PsiMethodImpl psiMethod) {
            Arrays.stream(psiMethod.getAnnotations()).filter(psiAnnotation -> Objects.equals(psiAnnotation.getQualifiedName(), "com.blinkfox.fenix.jpa.QueryFenix")).findFirst().ifPresent(psiAnnotation -> {
                final PsiAnnotationMemberValue psiAnnotationMemberValue = psiAnnotation.findAttributeValue("value");
                if (psiAnnotationMemberValue instanceof PsiLiteralExpressionImpl psiLiteralExpression) {
                    final Object valueObj = psiLiteralExpression.getValue();
                    if (valueObj != null && !valueObj.toString().isEmpty()) {
                        final String value = psiLiteralExpression.getValue().toString();
                        final String[] split = value.split("\\.");
                        if (split.length < 2) {
                            return;
                        }
                        final String nameSpace = split[0];
                        final String fenixId = split[1];
                        System.out.println("value: " + value);
                        this.process(psiMethod.getProject(), processor, nameSpace + "." + fenixId);
                    } else {
                        fenixEmpty(psiMethod, processor);
                    }

                } else {
                    fenixEmpty(psiMethod, processor);
                }

            });
        }
        if (element instanceof KtNamedFunction) {
            this.ktFenix((KtNamedFunction) element, processor);
        }
        Collection<IdDomElement> results = processor.getResults();
        if (CollectionUtils.isNotEmpty(results)) {
            var icon = IconLoader.getIcon("/images/pluginIcon.svg", FenixDescription.class);
            NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                    .create(icon)
                    .setAlignment(GutterIconRenderer.Alignment.CENTER)
                    .setTargets(Collections2.transform(results, DomElement::getXmlTag))
                    .setTooltipTitle("Navigation to Target in Fenix Mapper Xml");
            result.add(builder.createLineMarkerInfo(Objects.requireNonNull(((PsiNameIdentifierOwner) element).getNameIdentifier())));
        }
    }

    private void fenixEmpty(PsiMethodImpl psiMethod, CommonProcessors.CollectProcessor<IdDomElement> processor) {
        final String className = Objects.requireNonNull(psiMethod.getContainingClass()).getQualifiedName();
        System.out.println("class name: " + className);
        final String fenixId = psiMethod.getName();
        this.process(psiMethod.getProject(), processor, className + "." + fenixId);
    }

    private void process(@NotNull Project project, @NotNull Processor<IdDomElement> processor, String id) {
        for (Fenixs fenixs : MapperUtils.findMappers(project)) {
            for (IdDomElement idDomElement : fenixs.getDaoElements()) {
                if (MapperUtils.getIdSignature(idDomElement).equals(id)) {
                    processor.process(idDomElement);
                }
            }
        }
    }

    private void ktFenix(KtNamedFunction ktFun, CommonProcessors.CollectProcessor<IdDomElement> processor) {
        String namespace;
        final PsiElement parent = ktFun.getParent().getParent();
        if (parent instanceof KtClass) {
            final KtClass ktClass = (KtClass) parent;
            if (!ktClass.isInterface()) {
                return;
            }
            final FqName classFq = ktClass.getFqName();
            if (classFq != null) {
                namespace = classFq.asString();
            } else {
                namespace = null;
            }
        } else {
            namespace = null;
        }
        if (StringUtils.isEmpty(namespace)) {
            return;
        }
        boolean importFenixAnn = Arrays.stream(ktFun.getContainingKtFile().getChildren()).anyMatch(child -> {
            if (child instanceof KtImportList) {
                final KtImportList importList = (KtImportList) child;
                return importList.getImports().stream().anyMatch(anImport ->
                        anImport.getImportedFqName() != null
                                && ("com.blinkfox.fenix.jpa.QueryFenix".equals(anImport.getImportedFqName().asString())
                                || "com.blinkfox.fenix.jpa.*".equals(anImport.getImportedFqName().asString())));
            }
            return false;
        });
        if (!importFenixAnn) {
            return;
        }

        final List<KtAnnotationEntry> annotationEntries = ktFun.getAnnotationEntries();
        if (CollectionUtils.isNotEmpty(annotationEntries)) {
            annotationEntries.stream()
                    .filter(ann -> ann.getText().startsWith("@QueryFenix"))
                    .findFirst()
                    .ifPresent(ann -> {
                        System.out.println("Annotation: " + ann.getText());
                        final KtValueArgumentList valueArgumentList = ann.getValueArgumentList();
                        if (valueArgumentList != null) {
                            final List<KtValueArgument> arguments = valueArgumentList.getArguments();
                            System.out.println("Annotation args: " + arguments.stream().map(KtValueArgument::getText).filter(Objects::nonNull).collect(Collectors.joining(";")));
                            final Optional<KtValueArgument> fenixArgOpt = valueArgumentList.getArguments().stream().filter(a -> {
                                return a.getArgumentName() == null || a.getArgumentName().getAsName().asString().equals("value");
                            }).findFirst();
                            if (fenixArgOpt.isPresent()) {
                                final KtValueArgument argument = fenixArgOpt.get();
                                final String text1 = argument.getText().replace("\"", "");
                                final KtValueArgumentName argumentName = argument.getArgumentName();
                                String fenixM;
                                if (argumentName != null) {
                                    fenixM = text1.replace(argumentName.getAsName().asString(), "").trim();
                                } else {
                                    fenixM = text1.trim();
                                }
                                this.process(ktFun.getProject(), processor, namespace + "." + fenixM);
                            } else {
                                String fenixM = ktFun.getName();
                                this.process(ktFun.getProject(), processor, namespace + "." + fenixM);
                            }
                        } else {
                            String fenixM = ktFun.getName();
                            this.process(ktFun.getProject(), processor, namespace + "." + fenixM);
                        }
                    });
        }
    }

}
