package cn.jgayb.fenixplugin

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.navigation.activateFileWithPsiElement
import com.intellij.formatting.FormatTextRanges
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.codeStyle.CodeFormatterFacade

@Service(Service.Level.PROJECT)
class EditorService(project: Project) {
    private val fileEditorManager: FileEditorManager = FileEditorManager.getInstance(project)

    private var codeFormatterFacade: CodeFormatterFacade? = null

    fun format(file: PsiFile, element: PsiElement) {
        this.codeFormatterFacade =
            CodeFormatterFacade(CodeStyle.getSettings(element.project), element.language)
        codeFormatterFacade!!.processText(file, FormatTextRanges(element.textRange, true), true)
    }

    fun scrollTo(element: PsiElement, offset: Int) {
        activateFileWithPsiElement(element, true)
        val editor = fileEditorManager.selectedTextEditor
        if (null != editor) {
            editor.caretModel.moveToOffset(offset)
            editor.scrollingModel.scrollToCaret(ScrollType.RELATIVE)
        }
    }

    companion object {
        fun getInstance(project: Project): EditorService {
            return project.getService(EditorService::class.java)
        }
    }
}