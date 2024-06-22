package cn.jgayb.fenixplugin

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.intellij.openapi.util.IconLoader.getIcon

class FenixsFileTemplateDescriptorFactory : FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor(
            "Fenixs", getIcon(
                "/images/pluginIcon.svg",
                FenixsFileTemplateDescriptorFactory::class.java
            )
        )
        group.addTemplate(
            FileTemplateDescriptor(
                FENIX_MAPPER_XML_TEMPLATE, getIcon(
                    "/images/pluginIcon.svg",
                    FenixsFileTemplateDescriptorFactory::class.java
                )
            )
        )
        return group
    }

    companion object {
        const val FENIX_MAPPER_XML_TEMPLATE: String = "Fenixs Mapper.xml"
    }
}