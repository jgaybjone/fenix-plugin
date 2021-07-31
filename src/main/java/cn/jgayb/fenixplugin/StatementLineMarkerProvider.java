package cn.jgayb.fenixplugin;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by jg.wang on 2021/6/21.
 * Description:
 */
public class StatementLineMarkerProvider implements LineMarkerProvider {
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!isTheElement(element)) return null;

        Optional<PsiMethod> processResult = apply((XmlTag) element);
        return processResult.map(psiMethod -> new LineMarkerInfo<>(
                (XmlTag) element,
                element.getTextRange(),
                IconLoader.getIcon("/images/logo.png", Objects.requireNonNull(ReflectionUtil.getGrandCallerClass())),

                from -> "Data access object found - " + Objects.requireNonNull(psiMethod.getContainingClass()).getQualifiedName(),
                (e, from) -> ((Navigatable) psiMethod).navigate(true),
                GutterIconRenderer.Alignment.CENTER,
                () -> "Fenix"
        )).orElse(null);
    }

    private boolean isTheElement(PsiElement element) {
        return element instanceof XmlTag
                && MapperUtils.isElementWithinFenixFile(element);
    }

    private Optional<PsiMethod> apply(@NotNull XmlTag from) {
        if (from instanceof XmlTagImpl) {
            final String name = from.getName();
            if (!"fenix".equals(name)) {
                return Optional.empty();
            }
        }
        DomElement domElement = DomUtil.getDomElement(from);
        if (!(domElement instanceof IdDomElement)) {
            return Optional.empty();
        }
        return JavaUtils.findMethod(from.getProject(), (IdDomElement) domElement);
    }
}
