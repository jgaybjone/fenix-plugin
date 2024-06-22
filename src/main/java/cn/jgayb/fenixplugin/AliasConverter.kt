package cn.jgayb.fenixplugin

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.xml.*
import org.jetbrains.annotations.NonNls
import java.util.*

class AliasConverter : ResolvingConverter<PsiClass>(), CustomReferenceConverter<PsiClass> {
    private val delegate = PsiClassConverter()

    override fun getVariants(context: ConvertContext?): Collection<PsiClass> {
        return emptyList()
    }

    override fun fromString(@NonNls s: String?, context: ConvertContext): PsiClass? {
        if (s.isNullOrBlank()) {
            return null
        }
        return DomJavaUtil.findClass(
            Objects.requireNonNull(s).trim { it <= ' ' }, context.file, context.module,
            GlobalSearchScope.allScope(context.project)
        )
    }

    override fun toString(psiClass: PsiClass?, context: ConvertContext?): String? {
        return delegate.toString(psiClass, context)
    }

    override fun createReferences(
        value: GenericDomValue<PsiClass?>?,
        element: PsiElement?,
        context: ConvertContext?
    ): Array<PsiReference> {
        return delegate.createReferences(value, element, context)
    }
}