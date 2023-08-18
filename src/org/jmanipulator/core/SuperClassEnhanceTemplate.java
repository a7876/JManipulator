package org.jmanipulator.core;

/**
 * 方法增强模板，使用拓展父类的形式增强方法
 * 这是一个用于分析的模板，告知框架应该如何增强目标方法，配合SuperClassEnhancer实现增强
 * 同时新生成的类的对象也将会引用所有此类的一个对象中的变量
 * 使用这个类的时候必须直接拓展自这个类，不能间接拓展
 * 不要使用嵌套类（需要可以直接获取到模板类的字段，不能使用外部字段）
 */
public abstract class SuperClassEnhanceTemplate implements EnhanceTemplate<MethodEntryPoint> {
    public static final String DESC = "(Lorg/jmanipulator/core/MethodEntryPoint;)Ljava/lang/Object;";
    @Override
    public abstract Object template(MethodEntryPoint point);
}
