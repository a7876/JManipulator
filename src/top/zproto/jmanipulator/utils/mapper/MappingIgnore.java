package top.zproto.jmanipulator.utils.mapper;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记注解，被标志的字段/getter/setter都会被忽视
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface MappingIgnore {
    boolean getIgnore() default true;

    boolean setIgnore() default true;
}
