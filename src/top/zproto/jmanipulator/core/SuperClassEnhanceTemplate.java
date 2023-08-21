package top.zproto.jmanipulator.core;

/**
 * 方法增强模板，使用实现接口的形式定义增强模板
 * 这是一个用于分析的模板，告知框架应该如何增强目标方法，配合<code>SuperClassEnhancer</code>实现增强
 * 同时新生成的类的对象将会新增此类对应模板对象中的所有变量
 * 使用这个类的时候必须直接实现这个接口，不能间接拓展
 * 不要声明实现类为嵌套类（需要可以直接获取到模板类的字段，不能使用外部类字段）
 * 模板中的模板方法和成员字段上的注解将会进入被增强的生成类中<br>
 * <strong>注意虽然返回值是<code>Object</code>，但是实际在template中返回值应该是原方法的准确类型,特别注意原始类型的返回值一定要一致,
 * 如果不一致会抛出<code>ClassCastException</code>。如果原方法返回值是primitive类型，但是返回了null值就会抛出<code>NullPointerException</code></strong><br>
 * template方法只是一个模板，本框架从来不直接实例化和执行该类的实现类
 */
public interface SuperClassEnhanceTemplate extends EnhanceTemplate<MethodEntryPoint> {
    String DESC = "(Ltop/zproto/jmanipulator/core/MethodEntryPoint;)Ljava/lang/Object;";

    @Override
    Object template(MethodEntryPoint point);
}
