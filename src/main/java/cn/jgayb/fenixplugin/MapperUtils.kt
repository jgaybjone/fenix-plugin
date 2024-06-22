package cn.jgayb.fenixplugin

import com.google.common.collect.Collections2
import com.google.common.collect.Lists
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.xml.XmlElement
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomUtil
import org.jetbrains.annotations.NonNls
import java.util.*

class MapperUtils private constructor() {
    init {
        throw UnsupportedOperationException()
    }

    companion object {
        @JvmStatic
        fun findParentIdDomElement(element: PsiElement?): Optional<IdDomElement> {
            val domElement = DomUtil.getDomElement(element) ?: return Optional.empty()
            if (domElement is IdDomElement) {
                return Optional.of(domElement)
            }
            return Optional.ofNullable(DomUtil.getParentOfType(domElement, IdDomElement::class.java, true))
        }

        @Throws(Exception::class)
        fun createMapperFromFileTemplate(
            project: Project,
            fileTemplateName: String,
            fileName: String,
            directory: PsiDirectory,
            pops: Properties?
        ): PsiElement {
            val fileTemplate = FileTemplateManager.getInstance(project).getJ2eeTemplate(fileTemplateName)
            return FileTemplateUtil.createFromTemplate(fileTemplate, fileName, pops, directory)
        }

        fun findMapperDirectories(project: Project): Collection<PsiDirectory> {
            return Collections2.transform(
                findMappers(project)
            ) { input -> input!!.xmlElement!!.containingFile.containingDirectory }
        }

        fun isElementWithinFenixFile(element: PsiElement): Boolean {
            val psiFile = element.containingFile
            return element is XmlElement && DomUtils.isFenixsFile(psiFile)
        }

        @NonNls
        fun findMappers(project: Project): Collection<Fenixs> {
            return DomUtils.findDomElements(project, Fenixs::class.java)
        }

        @NonNls
        fun findMappers(project: Project, namespace: String): Collection<Fenixs?> {
            val result: MutableList<Fenixs?> = Lists.newArrayList()
            for (fenixs in findMappers(project)) {
                if (getNamespace(fenixs) == namespace) {
                    result.add(fenixs)
                }
            }
            return result
        }

        fun findMappers(project: Project, clazz: PsiClass): Collection<Fenixs?> {
            return if (JavaUtils.isElementWithinInterface(clazz)) findMappers(
                project,
                clazz.qualifiedName!!
            ) else emptyList<Fenixs>()
        }

        fun findMappers(project: Project, method: PsiMethod): Collection<Fenixs?> {
            val clazz = method.containingClass
            return if (null == clazz) emptyList() else findMappers(project, clazz)
        }

        @NonNls
        fun findFirstMapper(project: Project, namespace: String): Optional<Fenixs> {
            val fenixs = findMappers(project, namespace)
            return if (CollectionUtils.isEmpty(fenixs)) Optional.empty() else Optional.of(
                fenixs.iterator().next()!!
            )
        }

        @NonNls
        fun findFirstMapper(project: Project, clazz: PsiClass): Optional<Fenixs> {
            val qualifiedName = clazz.qualifiedName
            return if (null != qualifiedName) findFirstMapper(project, qualifiedName) else Optional.empty()
        }

        @NonNls
        fun findFirstMapper(project: Project, method: PsiMethod): Optional<Fenixs> {
            val containingClass = method.containingClass
            return if (null != containingClass) findFirstMapper(project, containingClass) else Optional.empty()
        }

        @NonNls
        fun getMapper(element: DomElement): Fenixs {
            val optional = Optional.ofNullable(
                DomUtil.getParentOfType(
                    element,
                    Fenixs::class.java, true
                )
            )
            if (optional.isPresent) {
                return optional.get()
            } else {
                throw IllegalArgumentException("Unknown element")
            }
        }

        @NonNls
        fun getNamespace(fenixs: Fenixs): String {
            val ns = fenixs.getNamespace().stringValue
            return ns ?: ""
        }

        @NonNls
        fun getNamespace(element: DomElement): String {
            return getNamespace(getMapper(element))
        }

        @NonNls
        fun isMapperWithSameNamespace(fenixs: Fenixs?, target: Fenixs?): Boolean {
            return null != fenixs && null != target && getNamespace(fenixs) == getNamespace(
                target
            )
        }

        @NonNls
        fun <T : IdDomElement?> getId(domElement: T): String? {
            return domElement?.id?.rawText
        }

        @NonNls
        fun <T : IdDomElement> getIdSignature(domElement: T): String {
            return getNamespace(domElement) + "." + getId(domElement)
        }

        @NonNls
        fun <T : IdDomElement> getIdSignature(domElement: T, fenixs: Fenixs): String {
            val contextFenixs = getMapper(domElement)
            var id = getId(domElement)
            if (id == null) {
                id = ""
            }
            val idsignature = getIdSignature(domElement)
            return if (isMapperWithSameNamespace(contextFenixs, fenixs)) id else idsignature
        }
    }
}