package cn.jgayb.fenixplugin;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;

/**
 * Created by jg.wang on 2021/7/29.
 * Description:
 */
public class FenixStandardResourceProvider implements StandardResourceProvider {
    @Override
    public void registerResources(ResourceRegistrar registrar) {
        registrar.addStdResource("https://blinkfox.github.io/fenix.dtd", "/dtd/fenix.dtd", this.getClass());
    }
}
