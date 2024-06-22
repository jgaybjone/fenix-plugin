package cn.jgayb.fenixplugin

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PropertyUtil
import com.intellij.psi.util.PsiTreeUtil
import java.util.*

class JavaUtils private constructor() {
    init {
        throw UnsupportedOperationException()
    }

    companion object {
        fun isModelClazz(clazz: PsiClass?): Boolean {
            return null != clazz && !clazz.isAnnotationType && !clazz.isInterface && !clazz.isEnum && clazz.isValid
        }

        fun findSettablePsiField(
            clazz: PsiClass,
            propertyName: String?
        ): Optional<PsiField> {
            val field = PropertyUtil.findPropertyField(clazz, propertyName, false)
            return if (field != null) Optional.of(field) else Optional.empty()
        }

        fun findSettablePsiFields(clazz: PsiClass): Array<PsiField> {
            val fields = clazz.allFields
            val settableFields: MutableList<PsiField> = ArrayList(fields.size)

            for (f in fields) {
                val modifiers = f.modifierList

                if (modifiers != null && (modifiers.hasModifierProperty(PsiModifier.STATIC) ||
                            modifiers.hasModifierProperty(PsiModifier.FINAL))
                ) {
                    continue
                }

                settableFields.add(f)
            }

            return settableFields.toTypedArray<PsiField>()
        }

        fun isElementWithinInterface(element: PsiElement?): Boolean {
            if (element is PsiClass && element.isInterface) {
                return true
            }
            val type = PsiTreeUtil.getParentOfType(
                element,
                PsiClass::class.java
            )
            return Optional.ofNullable(type).isPresent && type!!.isInterface
        }

        fun findClazz(project: Project, clazzName: String): Optional<PsiClass> {
            return Optional.ofNullable(
                JavaPsiFacade.getInstance(project).findClass(clazzName, GlobalSearchScope.allScope(project))
            )
        }

        fun findMethod(project: Project, clazzName: String?, methodName: String?): Optional<PsiMethod> {
            if (clazzName.isNullOrEmpty() && methodName.isNullOrEmpty()) {
                return Optional.empty()
            }
            val clazz = findClazz(
                project,
                clazzName!!
            )
            if (clazz.isPresent) {
                val methods = clazz.get().findMethodsByName(methodName, true)
                return if (methods.isEmpty()) Optional.empty() else Optional.of(
                    methods[0]
                )
            }
            return Optional.empty()
        }

        fun findMethod(project: Project, element: IdDomElement): Optional<PsiMethod> {
            return findMethod(project, MapperUtils.getNamespace(element), MapperUtils.getId(element))
        }

        fun hasImportClazz(file: PsiJavaFile, clazzName: String): Boolean {
            val importList = file.importList ?: return false
            val statements = importList.importStatements
            for (tmp in statements) {
                if (null != tmp && tmp.qualifiedName == clazzName) {
                    return true
                }
            }
            return false
        }
    }
}