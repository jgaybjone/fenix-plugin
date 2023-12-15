package cn.jgayb.fenixplugin;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.util.IconLoader;

/**
 * @author yanglin
 */
@SuppressWarnings("TestInspectionTool")
public class FenixsFileTemplateDescriptorFactory implements FileTemplateGroupDescriptorFactory {

    public static final String FENIX_MAPPER_XML_TEMPLATE = "Fenixs Mapper.xml";

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("Fenixs", IconLoader.getIcon("/images/pluginIcon.svg", FenixsFileTemplateDescriptorFactory.class));
        group.addTemplate(new FileTemplateDescriptor(FENIX_MAPPER_XML_TEMPLATE, IconLoader.getIcon("/images/pluginIcon.svg", FenixsFileTemplateDescriptorFactory.class)));
        return group;
    }

}
