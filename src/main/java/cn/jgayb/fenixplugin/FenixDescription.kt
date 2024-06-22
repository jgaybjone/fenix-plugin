package cn.jgayb.fenixplugin

import com.intellij.openapi.module.Module
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomFileDescription

class FenixDescription : DomFileDescription<Fenixs>(Fenixs::class.java, "fenixs") {
    override fun isMyFile(file: XmlFile, module: Module?): Boolean {
        return DomUtils.isFenixsFile(file)
    }
}