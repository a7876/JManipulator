package top.zproto.jmanipulator.utils.quickTemplate;

import com.sun.istack.internal.NotNull;
import top.zproto.jmanipulator.core.MethodEntryPoint;
import top.zproto.jmanipulator.core.SuperClassEnhanceTemplate;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 快速增强模板
 * 传入一个Predicate判断决定是否执行原方法，传入一个Supplier来产生替代值
 * 1. 如果Predicate返回true就返回原方法的返回值
 * 2. 如果Predicate返回false，而Supplier非null就返回其提供的返回值，如果Supplier为null，最终返回null
 */
public class AroundEnhanceTemplate_Predicate implements SuperClassEnhanceTemplate {
    private Predicate<Object> predicate;
    private Supplier<Object> supplier;

    private AroundEnhanceTemplate_Predicate() {
    }

    public static SuperClassEnhanceTemplate getTemplate(@NotNull Predicate<Object> predicate, Supplier<Object> supplier) {
        Objects.requireNonNull(predicate);
        AroundEnhanceTemplate_Predicate template = new AroundEnhanceTemplate_Predicate();
        template.predicate = predicate;
        template.supplier = supplier;
        return template;
    }

    @Override
    public Object template(MethodEntryPoint point) {
        Object res = point.defaultBehaviour();
        if (predicate.test(res)) {
            return true;
        }
        return supplier == null ? null : supplier.get();
    }
}
