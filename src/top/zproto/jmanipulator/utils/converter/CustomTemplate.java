package top.zproto.jmanipulator.utils.converter;

/**
 * 指定用户自定义的模板类和模板方法
 * 模板类要确定可以通过ClassLoader获取字节码文件流
 * 模板方法只能有一个参数或者没有参数，如果有唯一参数必须要是MethodEntryPoint
 * 模板方法的返回值必须是Object类型，不支持void / primitive
 */
public abstract class CustomTemplate {
    protected Class<?> customTemplateClass;
    protected String customTemplateMethod;
    protected Class<?> param;

    public static CustomTemplate getTemplate(Class<?> customTemplateClass, String methodName) {
        return new CustomTemplateWithNoParam(customTemplateClass, methodName);
    }

    public static CustomTemplate getTemplate(Class<?> customTemplateClass, String methodName, Class<?> arg) {
        return new CustomTemplateWithParam(customTemplateClass, methodName, arg);
    }

    public Class<?> getCustomTemplateClass() {
        return customTemplateClass;
    }

    public void setCustomTemplateClass(Class<?> customTemplateClass) {
        this.customTemplateClass = customTemplateClass;
    }

    public String getCustomTemplateMethod() {
        return customTemplateMethod;
    }

    public void setCustomTemplateMethod(String customTemplateMethod) {
        this.customTemplateMethod = customTemplateMethod;
    }

    public Class<?> getParam() {
        return param;
    }

    public void setParam(Class<?> param) {
        this.param = param;
    }
}
