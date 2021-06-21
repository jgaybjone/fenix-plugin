package cn.jgayb.fenixplugin;

import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yanglin
 */
public class FenixDescription extends DomFileDescription<Fenixs> {

    public FenixDescription() {
        super(Fenixs.class, "fenixs");
    }

    @Override
    public boolean isMyFile(@NotNull XmlFile file, @Nullable Module module) {
        return DomUtils.isFenixsFile(file);
    }

}
