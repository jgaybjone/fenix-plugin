package cn.jgayb.fenixplugin

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.util.CommonProcessors
import com.intellij.util.Processor
import com.intellij.util.xml.DomElement
import java.util.*

@Service(Service.Level.PROJECT)
class JavaService(private val project: Project) {
    private val javaPsiFacade: JavaPsiFacade = JavaPsiFacade.getInstance(project)

    fun getReferenceClazzOfPsiField(field: PsiElement): Optional<PsiClass> {
        if (field !is PsiField) {
            return Optional.empty()
        }
        val type = field.type
        return if (type is PsiClassReferenceType) Optional.ofNullable(type.resolve()) else Optional.empty()
    }

    fun findStatement(method: PsiMethod?): Optional<DomElement> {
        val processor = CommonProcessors.FindFirstProcessor<DomElement>()
        checkNotNull(method)
        process(method, processor)
        return if (processor.isFound) Optional.ofNullable(processor.foundValue) else Optional.empty()
    }


    fun process(target: PsiElement, processor: Processor<*>) {
        if (target is PsiMethod) {
            process(target, processor)
        } else if (target is PsiClass) {
            process(target, processor)
        }
    }

    fun <T> findWithFindFirstProcessor(target: PsiElement): Optional<T & Any> {
        val processor = CommonProcessors.FindFirstProcessor<T>()
        process(target, processor)
        return Optional.ofNullable(processor.foundValue)
    }


    fun process(psiMethod: PsiMethod, processor: Processor<IdDomElement?>) {
        val psiClass = psiMethod.containingClass ?: return
        val id = psiClass.qualifiedName + "." + psiMethod.name
        for (fenixs in MapperUtils.findMappers(psiMethod.project)) {
            for (idDomElement in fenixs.getDaoElements()) {
                if (MapperUtils.getIdSignature(idDomElement!!) == id) {
                    processor.process(idDomElement)
                }
            }
        }
    }

    fun process(clazz: PsiClass, processor: Processor<Fenixs?>) {
        val ns = clazz.qualifiedName
        for (mapper in MapperUtils.findMappers(clazz.project)) {
            if (MapperUtils.getNamespace(mapper).equals(ns)) {
                processor.process(mapper)
            }
        }
    }

    companion object {
        fun getInstance(project: Project): JavaService {
            return project.getService(JavaService::class.java)
        }
    }
}