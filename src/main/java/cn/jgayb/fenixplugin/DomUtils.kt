package cn.jgayb.fenixplugin

import com.google.common.collect.Collections2
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomService
import org.jetbrains.annotations.NonNls

class DomUtils private constructor() {
    init {
        throw UnsupportedOperationException()
    }

    companion object {
        @NonNls
        fun <T : DomElement> findDomElements(project: Project, clazz: Class<T>): Collection<T> {
            val scope = GlobalSearchScope.allScope(project)
            val elements = DomService.getInstance().getFileElements(clazz, project, scope)
            return Collections2.transform(
                elements
            ) { input -> input!!.rootElement }
        }

        @JvmStatic
        fun isFenixsFile(file: PsiFile?): Boolean {
            if (!isXmlFile(file!!)) {
                return false
            }
            val rootTag = (file as XmlFile?)!!.rootTag
            val result = null != rootTag && rootTag.name == "fenixs"
            if (result) {
            }
            return result
        }

        fun isMybatisConfigurationFile(file: PsiFile): Boolean {
            if (!isXmlFile(file)) {
                return false
            }
            val rootTag = (file as XmlFile).rootTag
            return null != rootTag && rootTag.name == "configuration"
        }

        fun isBeansFile(file: PsiFile): Boolean {
            if (!isXmlFile(file)) {
                return false
            }
            val rootTag = (file as XmlFile).rootTag
            return null != rootTag && rootTag.name == "beans"
        }

        fun isXmlFile(file: PsiFile): Boolean {
            return file is XmlFile
        }

    }
}