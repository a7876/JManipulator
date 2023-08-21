package top.zproto.jmanipulator.utils.quickTemplate;

import top.zproto.jmanipulator.core.MethodEntryPoint;
import top.zproto.jmanipulator.core.SuperClassEnhanceTemplate;

import java.util.function.Consumer;

/**
 * 快速增强模板
 * 传入一个Consumer消费被增强方法的返回值，最终直接返回原返回值
 * （只消费，不修改）
 */
public class AfterEnhanceTemplate_Consumer implements SuperClassEnhanceTemplate {
    private Consumer<Object> consumer;
    private AfterEnhanceTemplate_Consumer(){}
    public static SuperClassEnhanceTemplate getTemplate(Consumer<Object> consumer){
        AfterEnhanceTemplate_Consumer template = new AfterEnhanceTemplate_Consumer();
        template.consumer = consumer;
        return template;
    }
    @Override
    public Object template(MethodEntryPoint point) {
        Object res = point.defaultBehaviour();
        consumer.accept(res);
        return res;
    }
}
