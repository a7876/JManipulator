package top.zproto.jmanipulator.utils.converter;

/**
 * 没有参数的自定义模板版本
 */
public class CustomTemplateWithNoParam extends CustomTemplate {
    public CustomTemplateWithNoParam(Class<?> templateClass, String templateName) {
        customTemplateClass = templateClass;
        customTemplateMethod = templateName;
    }
}
