package cn.jgayb.fenixplugin

import com.google.common.collect.Collections2
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import com.intellij.util.CommonProcessors
import com.intellij.util.Processor
import org.apache.commons.lang3.StringUtils
import org.jetbrains.kotlin.psi.*
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors

class FenixItemLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>?>
    ) {
        val processor = CommonProcessors.CollectProcessor<IdDomElement?>()
        if (element is PsiMethodImpl) {
            Arrays.stream(element.annotations)
                .filter { psiAnnotation: PsiAnnotation -> psiAnnotation.qualifiedName == "com.blinkfox.fenix.jpa.QueryFenix" }
                .findFirst().ifPresent { psiAnnotation: PsiAnnotation ->
                    val psiAnnotationMemberValue =
                        psiAnnotation.findAttributeValue("value")
                    if (psiAnnotationMemberValue is PsiLiteralExpressionImpl) {
                        val valueObj: Any? = psiAnnotationMemberValue.value
                        if (valueObj != null && valueObj.toString().isNotEmpty()) {
                            val value: String = psiAnnotationMemberValue.value.toString()
                            val split =
                                value.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            if (split.size < 2) {
                                return@ifPresent
                            }
                            val nameSpace = split[0]
                            val fenixId = split[1]
                            println("value: $value")
                            this.process(element.getProject(), processor, "$nameSpace.$fenixId")
                        } else {
                            fenixEmpty(element, processor)
                        }
                    } else {
                        fenixEmpty(element, processor)
                    }
                }
        }
        if (element is KtNamedFunction) {
            this.ktFenix(element, processor)
        }
        val results = processor.results
        if (CollectionUtils.isNotEmpty(results)) {
            val icon = getIcon("/images/pluginIcon.svg", FenixDescription::class.java)
            val builder: NavigationGutterIconBuilder<PsiElement> = NavigationGutterIconBuilder
                .create(icon)
                .setAlignment(GutterIconRenderer.Alignment.CENTER)
                .setTargets(
                    Collections2.transform(results) { obj: IdDomElement? -> obj?.xmlTag }
                )
                .setTooltipTitle("Navigation to Target in Fenix Mapper Xml")
            result.add(builder.createLineMarkerInfo(Objects.requireNonNull((element as PsiNameIdentifierOwner).nameIdentifier)!!))
        }
    }

    private fun fenixEmpty(psiMethod: PsiMethodImpl, processor: CommonProcessors.CollectProcessor<IdDomElement?>) {
        val className = Objects.requireNonNull(psiMethod.containingClass)?.qualifiedName
        println("class name: $className")
        val fenixId = psiMethod.name
        this.process(psiMethod.project, processor, "$className.$fenixId")
    }

    private fun process(project: Project, processor: Processor<IdDomElement?>, id: String) {
        for (fenixs in MapperUtils.findMappers(project)) {
            for (idDomElement in fenixs.getDaoElements()) {
                if (MapperUtils.getIdSignature(idDomElement!!) == id) {
                    processor.process(idDomElement)
                }
            }
        }
    }

    private fun ktFenix(ktFun: KtNamedFunction, processor: CommonProcessors.CollectProcessor<IdDomElement?>) {
        val namespace: String?
        val parent = ktFun.parent.parent
        if (parent is KtClass) {
            if (!parent.isInterface()) {
                return
            }
            val classFq = parent.fqName
            namespace = classFq?.asString()
        } else {
            namespace = null
        }
        if (StringUtils.isEmpty(namespace)) {
            return
        }
        val importFenixAnn: Boolean =
            Arrays.stream(ktFun.containingKtFile.children).anyMatch(
                Predicate { child: PsiElement ->
                    if (child is KtImportList) {
                        return@Predicate child.imports.any { anImport: KtImportDirective ->
                            anImport.importedFqName != null
                                    && ("com.blinkfox.fenix.jpa.QueryFenix" == anImport.importedFqName?.asString()
                                    || "com.blinkfox.fenix.jpa.*" == anImport.importedFqName?.asString())
                            return@any true
                        }
                    }
                    false
                })
        if (!importFenixAnn) {
            return
        }

        val annotationEntries = ktFun.annotationEntries
        if (CollectionUtils.isNotEmpty(annotationEntries)) {
            annotationEntries.stream()
                .filter { ann: KtAnnotationEntry? ->
                    ann!!.text.startsWith("@QueryFenix")
                }
                .findFirst()
                .ifPresent { ann: KtAnnotationEntry? ->
                    println("Annotation: " + ann!!.text)
                    val valueArgumentList = ann.valueArgumentList
                    if (valueArgumentList != null) {
                        val arguments =
                            valueArgumentList.arguments
                        println(
                            "Annotation args: " + arguments.stream()
                                .map { obj: KtValueArgument -> obj.text }
                                .filter { obj: String? ->
                                    Objects.nonNull(
                                        obj
                                    )
                                }.collect(Collectors.joining(";"))
                        )
                        val fenixArgOpt =
                            valueArgumentList.arguments.stream().filter { a: KtValueArgument ->
                                a.getArgumentName() == null || a.getArgumentName()!!.asName.asString() == "value"
                            }.findFirst()
                        if (fenixArgOpt.isPresent) {
                            val argument = fenixArgOpt.get()
                            val text1 = argument.text.replace("\"", "")
                            val argumentName = argument.getArgumentName()
                            val fenixM = if (argumentName != null) {
                                text1.replace(argumentName.asName.asString(), "").trim { it <= ' ' }
                            } else {
                                text1.trim { it <= ' ' }
                            }
                            this.process(ktFun.project, processor, "$namespace.$fenixM")
                        } else {
                            val fenixM = ktFun.name!!
                            this.process(ktFun.project, processor, "$namespace.$fenixM")
                        }
                    } else {
                        val fenixM = ktFun.name!!
                        this.process(ktFun.project, processor, "$namespace.$fenixM")
                    }
                }
        }
    }
}