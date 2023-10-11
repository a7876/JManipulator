package top.zproto.jmanipulator.utils.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解用于告知是否需要复制父类中的属性
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SuperInclude {
    // 声明需要排除的父类
    Class<?>[] exclude() default {};
}
