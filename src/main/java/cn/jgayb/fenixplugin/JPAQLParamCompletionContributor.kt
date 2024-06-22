package cn.jgayb.fenixplugin

import cn.jgayb.fenixplugin.DomUtils.Companion.isFenixsFile
import cn.jgayb.fenixplugin.MapperUtils.Companion.findParentIdDomElement
import cn.jgayb.fenixplugin.XmlParamContributor.Companion.addElementForPsiParameter
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class JPAQLParamCompletionContributor : CompletionContributor() {
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        //sql补全一般是普通类型的，不是smart
        if (parameters.completionType != CompletionType.BASIC) {
            return
        }
        val position = parameters.position

        //这种方式拿到的就是xmlFile，如果用 position.getContainingFile(); 拿到的上层文件，不能拿到顶层的
        //sql是inject在xml里面的
        val topLevelFile = InjectedLanguageManager.getInstance(position.project).getTopLevelFile(position)
        if (isFenixsFile(topLevelFile)) {
            if (shouldAddElement(position.containingFile, parameters.offset)) {
                process(topLevelFile, result, position)
            }
        }
    }

    private fun process(xmlFile: PsiFile, result: CompletionResultSet, position: PsiElement) {
        //总而言之是为了拿到documentWindows
        val virtualFile = position.containingFile.virtualFile
        val documentWindow = (virtualFile as VirtualFileWindow).documentWindow
        val offset = documentWindow.injectedToHost(position.textOffset)
        val idDomElement = findParentIdDomElement(xmlFile.findElementAt(offset))
        if (idDomElement.isPresent) {
            addElementForPsiParameter(position.project, result, idDomElement.get())
            result.stopHere()
        }
    }

    /**
     * @param file   fenix file
     * @param offset 偏移量
     * @return boolean
     */
    private fun shouldAddElement(file: PsiFile, offset: Int): Boolean {
        return true
    }
}
