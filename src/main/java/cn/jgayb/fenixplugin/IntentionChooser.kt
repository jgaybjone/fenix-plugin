package cn.jgayb.fenixplugin

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

interface IntentionChooser {
    fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean
}