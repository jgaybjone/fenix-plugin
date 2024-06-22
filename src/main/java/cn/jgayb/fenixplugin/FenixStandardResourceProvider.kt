package cn.jgayb.fenixplugin

import com.intellij.javaee.ResourceRegistrar
import com.intellij.javaee.StandardResourceProvider

class FenixStandardResourceProvider : StandardResourceProvider {
    override fun registerResources(registrar: ResourceRegistrar) {
        registrar.addStdResource("https://blinkfox.github.io/fenix.dtd", "/dtd/fenix.dtd", this.javaClass)
    }
}