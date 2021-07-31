package cn.jgayb.fenixplugin;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author yanglin
 */
public interface IdDomElement extends DomElement {

    @Required
    @NameValue
    @Attribute("id")
    GenericAttributeValue<String> getId();

    @NotNull
    @Attribute("resultType")
    @Convert(AliasConverter.class)
    GenericAttributeValue<PsiClass> getResultType();

    void setValue(String content);
}
