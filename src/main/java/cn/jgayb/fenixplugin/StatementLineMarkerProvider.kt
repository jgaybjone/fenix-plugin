package cn.jgayb.fenixplugin

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomUtil
import java.awt.event.MouseEvent
import java.util.*

class StatementLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (!isTheElement(element)) return null

        val processResult = apply(element as XmlTag)
        return processResult.map { psiMethod: PsiMethod ->
            LineMarkerInfo(
                element,
                element.getTextRange(),
                getIcon("/images/pluginIcon.svg", FenixsFileTemplateDescriptorFactory::class.java),
                { from: XmlTag? ->
                    "Data access object found - " + Objects.requireNonNull(
                        psiMethod.containingClass
                    )!!.qualifiedName
                },
                { e: MouseEvent?, from: XmlTag? ->
                    (psiMethod as Navigatable).navigate(
                        true
                    )
                },
                GutterIconRenderer.Alignment.CENTER,
                { "Fenix" }
            )
        }.orElse(null)
    }

    private fun isTheElement(element: PsiElement): Boolean {
        return (element is XmlTag
                && MapperUtils.isElementWithinFenixFile(element))
    }

    private fun apply(from: XmlTag): Optional<PsiMethod> {
        if (from is XmlTagImpl) {
            val name = from.getName()
            if ("fenix" != name) {
                return Optional.empty()
            }
        }
        val domElement = DomUtil.getDomElement(from) as? IdDomElement ?: return Optional.empty()
        return JavaUtils.findMethod(from.project, domElement)
    }
}