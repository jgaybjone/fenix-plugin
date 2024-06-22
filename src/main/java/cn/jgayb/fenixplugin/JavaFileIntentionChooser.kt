package cn.jgayb.fenixplugin

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import java.util.*

abstract class JavaFileIntentionChooser : IntentionChooser {

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (file !is PsiJavaFile) return false
        val element = file.findElementAt(editor!!.caretModel.offset)
        return null != element && JavaUtils.isElementWithinInterface(element) && isAvailable(element)
    }

    abstract fun isAvailable(element: PsiElement): Boolean

    fun isPositionOfParameterDeclaration(element: PsiElement): Boolean {
        return element.parent is PsiParameter
    }

    fun isPositionOfMethodDeclaration(element: PsiElement): Boolean {
        return element.parent is PsiMethod
    }

    fun isPositionOfInterfaceDeclaration(element: PsiElement): Boolean {
        return element.parent is PsiClass
    }

    fun isTargetPresentInXml(element: PsiElement): Boolean {
        val javaService: JavaService = JavaService.getInstance(element.project)
        val processor: Optional<Any> = javaService.findWithFindFirstProcessor(element)
        return processor.isPresent
    }
}