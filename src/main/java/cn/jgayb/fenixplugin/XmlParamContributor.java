package cn.jgayb.fenixplugin;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yanglin
 * @update itar
 * @function xml中的代码补全
 */
public class XmlParamContributor extends CompletionContributor {

    /**
     * 指定代码补全的位置，xmlAttributeValue里面的xmlAttribute的name叫test的
     * XmlPatterns.psiElement()
     * .inside(XmlPatterns.xmlAttributeValue().inside(XmlPatterns.xmlAttribute().withName("test"))
     * 事实上这样也是足够的： XmlPatterns.psiElement().XmlPatterns.xmlAttribute().withName("test1")
     */
    public XmlParamContributor() {
        extend(CompletionType.BASIC,
                //指定代码补全的位置，xmlAttributeValue里面的xmlAttribute的name叫test的
                XmlPatterns.psiElement()
                        .inside(XmlPatterns.xmlTag()).inside(XmlPatterns.xmlText()),
                getProvider());
    }

    @NotNull
    private CompletionProvider<CompletionParameters> getProvider() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                PsiElement position = parameters.getPosition();
                addElementForPsiParameter(position.getProject(), result,
                        MapperUtils.findParentIdDomElement(position).orElse(null));
            }
        };
    }

    /**
     * @param project project
     * @param result  提示结果集合
     * @param element xml 元素
     */
    public static void addElementForPsiParameter(@NotNull Project project, @NotNull CompletionResultSet result,
                                                 @Nullable IdDomElement element) {
        if (null == element) {
            return;
        }
        final PsiClass psiClass = element.getResultType().getValue();
        if (psiClass == null) {
            return;
        }
        addLookupElements(result, psiClass.getAllFields());
    }

    private static void addLookupElements(@NotNull CompletionResultSet result, PsiField[] fields) {
        for (PsiField field : fields) {
            LookupElementBuilder builder = LookupElementBuilder.create(field.getName())
                    .withIcon(PlatformIcons.FIELD_ICON);
            //变成一个有优先级的对象，其实不加也行
            result.addElement(builder);
        }
    }
}
