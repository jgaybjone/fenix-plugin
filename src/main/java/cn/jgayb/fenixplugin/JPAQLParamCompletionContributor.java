package cn.jgayb.fenixplugin;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.injected.editor.DocumentWindow;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlTokenImpl;
import com.intellij.psi.tree.xml.IXmlLeafElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Created by jg.wang on 2021/7/30.
 * Description:
 */
public class JPAQLParamCompletionContributor extends CompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        //sql补全一般是普通类型的，不是smart
        if (parameters.getCompletionType() != CompletionType.BASIC) {
            return;
        }
        PsiElement position = parameters.getPosition();

        //这种方式拿到的就是xmlFile，如果用 position.getContainingFile(); 拿到的上层文件，不能拿到顶层的
        //sql是inject在xml里面的
        PsiFile topLevelFile = InjectedLanguageManager.getInstance(position.getProject()).getTopLevelFile(position);
        if (DomUtils.isFenixsFile(topLevelFile)) {
            if (shouldAddElement(position.getContainingFile(), parameters.getOffset())) {
                process(topLevelFile, result, position);
            }
        }
    }

    private void process(PsiFile xmlFile, CompletionResultSet result, PsiElement position) {
        //总而言之是为了拿到documentWindows
        VirtualFile virtualFile = position.getContainingFile().getVirtualFile();
        DocumentWindow documentWindow = ((VirtualFileWindow) virtualFile).getDocumentWindow();
        int offset = documentWindow.injectedToHost(position.getTextOffset());
        Optional<IdDomElement> idDomElement = MapperUtils.findParentIdDomElement(xmlFile.findElementAt(offset));
        if (idDomElement.isPresent()) {
            XmlParamContributor.addElementForPsiParameter(position.getProject(), result, idDomElement.get());
            result.stopHere();
        }
    }

    /**
     * @param file   fenix file
     * @param offset 偏移量
     * @return boolean
     */
    private boolean shouldAddElement(PsiFile file, int offset) {
        return true;
    }


}
