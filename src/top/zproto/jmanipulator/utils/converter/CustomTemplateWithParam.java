package top.zproto.jmanipulator.utils.converter;

public class CustomTemplateWithParam extends CustomTemplate{
    public CustomTemplateWithParam(Class<?> templateClass, String methodName, Class<?> arg) {
        customTemplateClass = templateClass;
        customTemplateMethod = methodName;
        param = arg;
    }
}
