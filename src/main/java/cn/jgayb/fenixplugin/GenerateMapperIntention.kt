package cn.jgayb.fenixplugin

import com.google.common.collect.Collections2
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import java.io.File
import java.util.*

class GenerateMapperIntention : IntentionAction {
    protected var chooser: IntentionChooser = GenerateMapperChooser.INSTANCE

    override fun getText(): String {
        return "[Fenixs] FenixsGenerator mapper of xml"
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        return text
    }

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return chooser.isAvailable(project, editor, file)
    }

    override fun startInWriteAction(): Boolean {
        return true
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val element = file.findElementAt(editor.caretModel.offset)
        val clazz = PsiTreeUtil.getParentOfType(
            element,
            PsiClass::class.java
        )
        val directories: Collection<PsiDirectory?> = MapperUtils.findMapperDirectories(project)
        if (CollectionUtils.isEmpty(directories)) {
            handleChooseNewFolder(project, editor, clazz)
        } else {
            handleMultipleDirectories(project, editor, clazz, directories)
        }
    }

    private fun handleMultipleDirectories(
        project: Project,
        editor: Editor,
        clazz: PsiClass?,
        directories: Collection<PsiDirectory?>
    ) {
        val pathMap = getPathMap(directories)
        val keys = pathMap.keys.toList()
        val popupListener: ListSelectionListener = object : ListSelectionListener {
            override fun selected(index: Int) {
                processGenerate(editor, clazz, pathMap[keys[index]])
            }

            override fun isWriteAction(): Boolean {
                return true
            }
        }
        val uiComponentFacade = UiComponentFacade.getInstance(project)
        uiComponentFacade.showListPopupWithSingleClickable(
            "Choose folder",
            popupListener,
            "Choose another",
            getChooseFolderListener(editor, clazz),
            getPathTextForShown(project, keys, pathMap)
        )
    }

    private fun getChooseFolderListener(editor: Editor, clazz: PsiClass?): ClickableListener {
        val project = clazz!!.project
        return object : ClickableListener {
            override fun clicked() {
                handleChooseNewFolder(project, editor, clazz)
            }

            override fun isWriteAction(): Boolean {
                return false
            }
        }
    }

    private fun handleChooseNewFolder(project: Project, editor: Editor, clazz: PsiClass?) {
        val uiComponentFacade = UiComponentFacade.getInstance(project)
        val baseDir = project.guessProjectDir()
        val vf = uiComponentFacade.showSingleFolderSelectionDialog("Select target folder", baseDir, baseDir)
        if (null != vf) {
            processGenerate(editor, clazz, PsiManager.getInstance(project).findDirectory(vf))
        }
    }

    private fun getPathTextForShown(
        project: Project,
        paths: List<String?>,
        pathMap: Map<String?, PsiDirectory?>
    ): Array<String?> {
        paths.sortedBy {
            it
        }
        val projectBasePath = project.basePath!!
        val result: Collection<String?> = Lists.newArrayList(
            Collections2.transform(
                paths
            ) { input ->
                val relativePath = FileUtil.getRelativePath(
                    Objects.requireNonNull(projectBasePath),
                    input!!, File.separatorChar
                )
                val module = ModuleUtil.findModuleForPsiElement(
                    pathMap[input]!!
                )
                if (null == module) relativePath else "[" + module.name + "] " + relativePath
            })
        return result.toTypedArray<String?>()
    }

    private fun getPathMap(directories: Collection<PsiDirectory?>): Map<String?, PsiDirectory?> {
        val result: MutableMap<String?, PsiDirectory?> = Maps.newHashMap()
        for (directory in directories) {
            val presentableUrl = directory!!.virtualFile.presentableUrl
            result[presentableUrl] = directory
        }
        return result
    }

    private fun processGenerate(editor: Editor, clazz: PsiClass?, directory: PsiDirectory?) {
        if (null == directory) {
            return
        }
        if (!directory.isWritable) {
            HintManager.getInstance().showErrorHint(editor, "Target directory is not writable")
            return
        }
        try {
            val properties = Properties()
            properties.setProperty("NAMESPACE", clazz!!.qualifiedName)
            val psiFile = MapperUtils.createMapperFromFileTemplate(
                editor.project!!,
                FenixsFileTemplateDescriptorFactory.FENIX_MAPPER_XML_TEMPLATE,
                Objects.requireNonNull(clazz.name)!!, directory, properties
            )
            EditorService.getInstance(clazz.project).scrollTo(psiFile, 0)
        } catch (e: Exception) {
            HintManager.getInstance().showErrorHint(editor, "Failed: " + e.cause)
        }
    }
}