package top.zproto.jmanipulator.core;

/**
 * 增强模板
 * @param <T> 指定增强点的类型
 */
public interface EnhanceTemplate<T extends TargetPoint> {
    String TEMPLATE = "template";
    Object template(T point);
}
