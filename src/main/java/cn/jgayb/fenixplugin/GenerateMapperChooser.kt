package cn.jgayb.fenixplugin

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class GenerateMapperChooser : JavaFileIntentionChooser() {
    override fun isAvailable(element: PsiElement): Boolean {
        if (isPositionOfInterfaceDeclaration(element)) {
            val clazz = PsiTreeUtil.getParentOfType(
                element,
                PsiClass::class.java
            )
            if (null != clazz) {
                return !isTargetPresentInXml(clazz)
            }
        }
        return false
    }

    companion object {
        val INSTANCE: JavaFileIntentionChooser = GenerateMapperChooser()
    }
}