package cn.jgayb.fenixplugin;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * @author yanglin
 */
public final class MapperUtils {

    private MapperUtils() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public static Optional<IdDomElement> findParentIdDomElement(@Nullable PsiElement element) {
        DomElement domElement = DomUtil.getDomElement(element);
        if (null == domElement) {
            return Optional.empty();
        }
        if (domElement instanceof IdDomElement) {
            return Optional.of((IdDomElement) domElement);
        }
        return Optional.ofNullable(DomUtil.getParentOfType(domElement, IdDomElement.class, true));
    }

    public static PsiElement createMapperFromFileTemplate(@NotNull String fileTemplateName,
                                                          @NotNull String fileName,
                                                          @NotNull PsiDirectory directory,
                                                          @Nullable Properties pops) throws Exception {
        FileTemplate fileTemplate = FileTemplateManager.getInstance().getJ2eeTemplate(fileTemplateName);
        return FileTemplateUtil.createFromTemplate(fileTemplate, fileName, pops, directory);
    }

    @NotNull
    public static Collection<PsiDirectory> findMapperDirectories(@NotNull Project project) {
        return Collections2.transform(findMappers(project), new Function<Fenixs, PsiDirectory>() {
            @Override
            public PsiDirectory apply(Fenixs input) {
                return input.getXmlElement().getContainingFile().getContainingDirectory();
            }
        });
    }

    public static boolean isElementWithinFenixFile(@NotNull PsiElement element) {
        PsiFile psiFile = element.getContainingFile();
        return element instanceof XmlElement && DomUtils.isFenixsFile(psiFile);
    }

    @NotNull
    @NonNls
    public static Collection<Fenixs> findMappers(@NotNull Project project) {
        return DomUtils.findDomElements(project, Fenixs.class);
    }

    @NotNull
    @NonNls
    public static Collection<Fenixs> findMappers(@NotNull Project project, @NotNull String namespace) {
        List<Fenixs> result = Lists.newArrayList();
        for (Fenixs fenixs : findMappers(project)) {
            if (getNamespace(fenixs).equals(namespace)) {
                result.add(fenixs);
            }
        }
        return result;
    }

    @NotNull
    public static Collection<Fenixs> findMappers(@NotNull Project project, @NotNull PsiClass clazz) {
        return JavaUtils.isElementWithinInterface(clazz) ? findMappers(project, clazz.getQualifiedName()) : Collections.<Fenixs>emptyList();
    }

    @NotNull
    public static Collection<Fenixs> findMappers(@NotNull Project project, @NotNull PsiMethod method) {
        PsiClass clazz = method.getContainingClass();
        return null == clazz ? Collections.<Fenixs>emptyList() : findMappers(project, clazz);
    }

    @NotNull
    @NonNls
    public static Optional<Fenixs> findFirstMapper(@NotNull Project project, @NotNull String namespace) {
        Collection<Fenixs> fenixs = findMappers(project, namespace);
        return CollectionUtils.isEmpty(fenixs) ? Optional.empty() : Optional.of(fenixs.iterator().next());
    }

    @NotNull
    @NonNls
    public static Optional<Fenixs> findFirstMapper(@NotNull Project project, @NotNull PsiClass clazz) {
        String qualifiedName = clazz.getQualifiedName();
        return null != qualifiedName ? findFirstMapper(project, qualifiedName) : Optional.empty();
    }

    @NotNull
    @NonNls
    public static Optional<Fenixs> findFirstMapper(@NotNull Project project, @NotNull PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        return null != containingClass ? findFirstMapper(project, containingClass) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @NonNls
    public static Fenixs getMapper(@NotNull DomElement element) {
        Optional<Fenixs> optional = Optional.ofNullable(DomUtil.getParentOfType(element, Fenixs.class, true));
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new IllegalArgumentException("Unknown element");
        }
    }

    @NotNull
    @NonNls
    public static String getNamespace(@NotNull Fenixs fenixs) {
        String ns = fenixs.getNamespace().getStringValue();
        return null == ns ? "" : ns;
    }

    @NotNull
    @NonNls
    public static String getNamespace(@NotNull DomElement element) {
        return getNamespace(getMapper(element));
    }

    @NonNls
    public static boolean isMapperWithSameNamespace(@Nullable Fenixs fenixs, @Nullable Fenixs target) {
        return null != fenixs && null != target && getNamespace(fenixs).equals(getNamespace(target));
    }

    @Nullable
    @NonNls
    public static <T extends IdDomElement> String getId(@NotNull T domElement) {
        return domElement.getId().getRawText();
    }

    @NotNull
    @NonNls
    public static <T extends IdDomElement> String getIdSignature(@NotNull T domElement) {
        return getNamespace(domElement) + "." + getId(domElement);
    }

    @NotNull
    @NonNls
    public static <T extends IdDomElement> String getIdSignature(@NotNull T domElement, @NotNull Fenixs fenixs) {
        Fenixs contextFenixs = getMapper(domElement);
        String id = getId(domElement);
        if (id == null) {
            id = "";
        }
        String idsignature = getIdSignature(domElement);
        //getIdSignature(domElement)
        return isMapperWithSameNamespace(contextFenixs, fenixs) ? id : idsignature;
    }

//    public static void processConfiguredTypeAliases(@NotNull Project project, @NotNull Processor<TypeAlias> processor) {
//        for (Configuration conf : getMybatisConfigurations(project)) {
//            for (TypeAliases tas : conf.getTypeAliases()) {
//                for (TypeAlias ta : tas.getTypeAlias()) {
//                    String stringValue = ta.getAlias().getStringValue();
//                    if (null != stringValue && !processor.process(ta)) {
//                        return;
//                    }
//                }
//            }
//        }
//    }
//
//    private static Collection<Configuration> getMybatisConfigurations(Project project) {
//        return DomUtils.findDomElements(project, Configuration.class);
//    }
//
//    public static void processConfiguredPackage(@NotNull Project project,
//                                                @NotNull Processor<com.wuzhizhan.mybatis.dom.model.Package> processor) {
//        for (Configuration conf : getMybatisConfigurations(project)) {
//            for (TypeAliases tas : conf.getTypeAliases()) {
//                for (com.wuzhizhan.mybatis.dom.model.Package pkg : tas.getPackages()) {
//                    if (!processor.process(pkg)) {
//                        return;
//                    }
//                }
//            }
//        }
//    }
}
