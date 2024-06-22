package cn.jgayb.fenixplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBList

class UiComponentFacade private constructor(private val project: Project) {
    private val fileEditorManager: FileEditorManager = FileEditorManager.getInstance(project)

    fun showSingleFolderSelectionDialog(
        title: String,
        toSelect: VirtualFile?,
        vararg roots: VirtualFile?
    ): VirtualFile? {
        val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        descriptor.title = title
        descriptor.setRoots(*roots)
        return FileChooser.chooseFile(descriptor, project, toSelect)
    }

    fun showListPopupWithSingleClickable(
        popupTitle: String,
        popupListener: ListSelectionListener,
        clickableTitle: String,
        clickableListener: ClickableListener?,
        objs: Array<String?>
    ): JBPopup {
        val builder = createListPopupBuilder(popupTitle, popupListener, objs)
        val checkBox = JBCheckBox(clickableTitle)
        builder.setSouthComponent(checkBox)
        val popup = builder.createPopup()
        if (null != clickableListener) {
            val runnable = Runnable { clickableListener.clicked() }
            checkBox.addActionListener {
                popup.dispose()
                setActionForExecutableListener(runnable, clickableListener)
            }
        }
        setPositionForShown(popup)
        return popup
    }

    fun showListPopup(
        title: String,
        listener: ListSelectionListener?,
        objs: Array<Any?>
    ): JBPopup {
        val builder = createListPopupBuilder(title, listener, objs)
        val popup = builder.createPopup()
        setPositionForShown(popup)
        return popup
    }

    private fun setPositionForShown(popup: JBPopup) {
        val editor = fileEditorManager.selectedTextEditor
        if (null != editor) {
            popup.showInBestPositionFor(editor)
        } else {
            popup.showCenteredInCurrentWindow(project)
        }
    }

    private fun setActionForExecutableListener(runnable: Runnable, listener: ExecutableListener) {
        val application = ApplicationManager.getApplication()
        if (listener.isWriteAction()) {
            application.runWriteAction(runnable)
        } else {
            application.runReadAction(runnable)
        }
    }

    fun createListPopupBuilder(
        title: String,
        listener: ListSelectionListener?,
        vararg objs: Any
    ): PopupChooserBuilder<*> {
        val list: JBList<*> = JBList(*objs)
        val builder: PopupChooserBuilder<*> = PopupChooserBuilder(list)
        builder.setTitle(title)
        if (null != listener) {
            val runnable = Runnable { listener.selected(list.selectedIndex) }
            builder.setItemChosenCallback(Runnable { setActionForExecutableListener(runnable, listener) })
//            builder.setItemChoosenCallback { setActionForExecutableListener(runnable, listener) }
        }
        return builder
    }

    companion object {
        fun getInstance(project: Project): UiComponentFacade {
            return UiComponentFacade(project)
        }
    }
}