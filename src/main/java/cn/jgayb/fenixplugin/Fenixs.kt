package cn.jgayb.fenixplugin

import com.intellij.psi.PsiClass
import com.intellij.util.xml.*

interface Fenixs : DomElement {

    @SubTagList("fenix")
    fun getDaoElements(): List<IdDomElement?>

    @Required
    @NameValue
    @Attribute("namespace")
    fun getNamespace(): GenericAttributeValue<String?>

}

interface IdDomElement : DomElement {
    @get:Attribute("id")
    @get:NameValue
    @get:Required
    val id: GenericAttributeValue<String?>?

    @get:Convert(AliasConverter::class)
    @get:Attribute("resultType")
    val resultType: GenericAttributeValue<PsiClass?>

    fun setValue(content: String?)
}
