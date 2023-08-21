package top.zproto.jmanipulator.utils.quickTemplate;


import top.zproto.jmanipulator.core.MethodEntryPoint;
import top.zproto.jmanipulator.core.SuperClassEnhanceTemplate;

import java.util.function.Function;

/**
 * 快速增强模板
 * 传入一个Function消费被增强方法的返回值，最终会返回Function的返回值
 * （消费且修改）
 */
public class AfterEnhanceTemplate_Accepter implements SuperClassEnhanceTemplate {
    private Function<Object, Object> function;

    private AfterEnhanceTemplate_Accepter() {
    }

    public static SuperClassEnhanceTemplate getTemplate(Function<Object, Object> function) {
        AfterEnhanceTemplate_Accepter template = new AfterEnhanceTemplate_Accepter();
        template.function = function;
        return template;
    }

    @Override
    public Object template(MethodEntryPoint point) {
        return function.apply(point.defaultBehaviour());
    }
}