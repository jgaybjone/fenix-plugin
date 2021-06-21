package cn.jgayb.fenixplugin;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;

public class TestInspectionTool extends AbstractBaseJavaLocalInspectionTool {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        // 返回一个java元素的访问器，重写当访问变量的时候，需要做的操作
        return new JavaElementVisitor() {
            @Override
            public void visitField(PsiField field) {
                super.visitField(field);
                // 注册问题，也就是在变量上面显示异常红色下划线，并提示"this is an error"
                // 如果可以的话，这里也可以附带上快速修复问题的方法
                holder.registerProblem(field, "this is an error");
            }
        };
    }
}
