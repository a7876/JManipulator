package top.zproto.jmanipulator.utils.quickTemplate;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import top.zproto.jmanipulator.core.MethodEntryPoint;
import top.zproto.jmanipulator.core.SuperClassEnhanceTemplate;

/**
 * 快速增强模板
 * 传入两个Runnable参数（可以为null，null的不执行），一个在执行原方法前执行，一个在执行原方法后执行
 * 注意：是在同一个线程执行，并不会开启新的线程执行Runnable
 */
public class AroundEnhanceTemplate_Simple implements SuperClassEnhanceTemplate {
    private Runnable pre, post;

    private AroundEnhanceTemplate_Simple() {
    }

    public static SuperClassEnhanceTemplate getTemplate(@NotNull Runnable pre, @Nullable Runnable post) {
        AroundEnhanceTemplate_Simple template = new AroundEnhanceTemplate_Simple();
        template.pre = pre;
        template.post = post;
        return template;
    }

    @Override
    public Object template(MethodEntryPoint point) {
        if (pre != null)
            pre.run();
        Object res = point.defaultBehaviour();
        if (post != null)
            post.run();
        return res;
    }
}
