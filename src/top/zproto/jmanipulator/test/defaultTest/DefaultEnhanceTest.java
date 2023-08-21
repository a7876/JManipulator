package top.zproto.jmanipulator.test.defaultTest;

import top.zproto.jmanipulator.core.*;

public class DefaultEnhanceTest {
    public static void main(String[] args) throws NoSuchMethodException {
        Template template = new Template();
        TargetMethodFilter filter = new TargetMethodFilter();
        filter.add(new TargetMethod(Business.class.getDeclaredMethod("working")));
        SuperClassEnhancer<Business> enhancer = new SuperClassEnhancer<>(Business.class, filter);
        SuperClassGenerationHolder<Business> enhanced = enhancer.enhance(template);
        enhanced.getTargetClass(ClassLoader.getSystemClassLoader());
        Business instance = enhanced.getPopulatedInstance(GenerationHolder.EMPTY_CLASSES);
        System.out.println(instance.working());
    }
}
