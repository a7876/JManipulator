package top.zproto.jmanipulator.utils.converter;

import top.zproto.jmanipulator.core.GenerationHolder;
import top.zproto.jmanipulator.core.inner.ClassInfo;
import top.zproto.jmanipulator.utils.ClassLoaderWrapper;

import java.lang.reflect.Field;
import java.util.Map;

public class CustomEnhanceGenerationHolder extends GenerationHolder {
    public CustomEnhanceGenerationHolder(byte[] byteCode, String targetClassName, Class<?> superClass, ClassInfo superClassInfo, ClassInfo templateInfo) {
        super(byteCode, targetClassName, superClass, null, superClassInfo, templateInfo);
    }

    private Class<?> generatedClass;
    private Object customTemplate;

    public Class<?> getGeneratedClass(ClassLoader classLoader) {
        generatedClass = ClassLoaderWrapper.loadClass(classLoader, targetClassName, byteCode);
        return generatedClass;
    }

    /**
     * @param o         生成的增强类的一个对象
     * @param fieldName 模板类中原来字段名
     * @param value     字段值
     */
    public void populateField(Object o, String fieldName, Object value) {
        Class<?> targetClass = o.getClass();
        if (targetClass != generatedClass) {
            throw new RuntimeException("not last time generated class");
        }
        doPopulateField(o, fieldName, value);
    }

    private void doPopulateField(Object o, String fieldName, Object value) {
        getFieldNameMapper();
        try {
            Field field = generatedClass.getDeclaredField(fieldNameMapper.get(fieldName));
            field.setAccessible(true);
            field.set(o, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void populateFields(Object o) {
        Class<?> targetClass = o.getClass();
        if (targetClass != generatedClass) {
            throw new RuntimeException("not last time generated class");
        }
        if (customTemplate == null)
            throw new RuntimeException("customTemplate uninitialized");
        Field[] declaredFields = customTemplate.getClass().getDeclaredFields();
        for (Field f : declaredFields) {
            f.setAccessible(true);
            try {
                doPopulateField(o, f.getName(), f.get(customTemplate));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("unexpected exception",e);
            }
        }
    }

    private Map<String, String> fieldNameMapper;

    private void getFieldNameMapper() {
        if (fieldNameMapper == null)
            fieldNameMapper = fieldNameConvert(templateInfo.getFields());
    }

    public void setCustomTemplate(Object customTemplate) {
        this.customTemplate = customTemplate;
    }
}
