package cn.jgayb.fenixplugin;

import com.google.common.collect.Collections2;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by jg.wang on 2021/6/16.
 * Description:
 */
public class FenixItemLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (element instanceof PsiMethodImpl) {
            final PsiMethodImpl psiMethod = (PsiMethodImpl) element;
            Arrays.stream(psiMethod.getAnnotations()).filter(psiAnnotation -> Objects.equals(psiAnnotation.getQualifiedName(), "com.blinkfox.fenix.jpa.QueryFenix")).findFirst().ifPresent(psiAnnotation -> {
                final PsiAnnotationMemberValue psiAnnotationMemberValue = psiAnnotation.findAttributeValue("value");
                CommonProcessors.CollectProcessor<IdDomElement> processor = new CommonProcessors.CollectProcessor<IdDomElement>();
                if (psiAnnotationMemberValue instanceof PsiLiteralExpressionImpl) {
                    final PsiLiteralExpressionImpl psiLiteralExpression = (PsiLiteralExpressionImpl) psiAnnotationMemberValue;
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
                        this.process(psiMethod, processor, nameSpace + "." + fenixId);
                    } else {
                        fenixEmpty(psiMethod, processor);
                    }

                } else {
                    fenixEmpty(psiMethod, processor);
                }
                Collection<IdDomElement> results = processor.getResults();
                if (CollectionUtils.isNotEmpty(results)) {
                    NavigationGutterIconBuilder<PsiElement> builder =
                            NavigationGutterIconBuilder.create(IconLoader.getIcon("/images/logo.png", Objects.requireNonNull(ReflectionUtil.getGrandCallerClass())))
                                    .setAlignment(GutterIconRenderer.Alignment.CENTER)
                                    .setTargets(Collections2.transform(results, DomElement::getXmlTag))
                                    .setTooltipTitle("Navigation to Target in Fenix Mapper Xml");
                    result.add(builder.createLineMarkerInfo(Objects.requireNonNull(((PsiNameIdentifierOwner) element).getNameIdentifier())));
                }
            });
        }
    }

    private void fenixEmpty(PsiMethodImpl psiMethod, CommonProcessors.CollectProcessor<IdDomElement> processor) {
        final String className = Objects.requireNonNull(psiMethod.getContainingClass()).getQualifiedName();
        System.out.println("class name: " + className);
        final String fenixId = psiMethod.getName();
        this.process(psiMethod, processor, className + "." + fenixId);
    }

    private void process(@NotNull PsiMethod psiMethod, @NotNull Processor<IdDomElement> processor, String id) {
        for (Fenixs fenixs : MapperUtils.findMappers(psiMethod.getProject())) {
            for (IdDomElement idDomElement : fenixs.getDaoElements()) {
                if (MapperUtils.getIdSignature(idDomElement).equals(id)) {
                    processor.process(idDomElement);
                }
            }
        }
    }
}
