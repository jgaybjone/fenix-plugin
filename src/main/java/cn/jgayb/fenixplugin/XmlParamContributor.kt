package cn.jgayb.fenixplugin

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext

class XmlParamContributor : CompletionContributor() {
    /**
     * 指定代码补全的位置，xmlAttributeValue里面的xmlAttribute的name叫test的
     * XmlPatterns.psiElement()
     * .inside(XmlPatterns.xmlAttributeValue().inside(XmlPatterns.xmlAttribute().withName("test"))
     * 事实上这样也是足够的： XmlPatterns.psiElement().XmlPatterns.xmlAttribute().withName("test1")
     */
    init {
        extend(
            CompletionType.BASIC,  //指定代码补全的位置，xmlAttributeValue里面的xmlAttribute的name叫test的
            XmlPatterns.psiElement()
                .inside(XmlPatterns.xmlTag()).inside(XmlPatterns.xmlText()),
            provider
        )
    }

    private val provider: CompletionProvider<CompletionParameters>
        get() = object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(
                parameters: CompletionParameters, context: ProcessingContext,
                result: CompletionResultSet
            ) {
                val position: PsiElement = parameters.position
                addElementForPsiParameter(
                    position.project, result,
                    MapperUtils.findParentIdDomElement(position).orElse(null)
                )
            }
        }

    companion object {
        /**
         * @param project project
         * @param result  提示结果集合
         * @param element xml 元素
         */
        @JvmStatic
        fun addElementForPsiParameter(
            project: Project, result: CompletionResultSet,
            element: IdDomElement?
        ) {
            if (null == element) {
                return
            }
            val psiClass: PsiClass = element.resultType.value ?: return
            addLookupElements(result, psiClass.allFields)
        }

        private fun addLookupElements(result: CompletionResultSet, fields: Array<PsiField>) {
            for (field: PsiField in fields) {
                val builder: LookupElementBuilder = LookupElementBuilder.create(field.name)
                    .withIcon(PlatformIcons.FIELD_ICON)
                //变成一个有优先级的对象，其实不加也行
                result.addElement(builder)
            }
        }
    }
}