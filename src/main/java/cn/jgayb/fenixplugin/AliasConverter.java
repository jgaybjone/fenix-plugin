package cn.jgayb.fenixplugin;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Created by jg.wang on 2021/7/30.
 * Description:
 */
public class AliasConverter extends ResolvingConverter<PsiClass> implements CustomReferenceConverter<PsiClass> {

    private final PsiClassConverter delegate = new PsiClassConverter();

    @Override
    public @NotNull Collection<? extends PsiClass> getVariants(ConvertContext context) {
        return Collections.emptyList();
    }

    @Override
    public @Nullable PsiClass fromString(@Nullable @NonNls String s, ConvertContext context) {
        return DomJavaUtil.findClass(Objects.requireNonNull(s).trim(), context.getFile(), context.getModule(),
                GlobalSearchScope.allScope(context.getProject()));
    }

    @Override
    public @Nullable String toString(@Nullable PsiClass psiClass, ConvertContext context) {
        return delegate.toString(psiClass, context);
    }

    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<PsiClass> value, PsiElement element, ConvertContext context) {
        return delegate.createReferences(value, element, context);
    }
}
