package org.jmanipulator.core;

import org.jmanipulator.core.inner.ClassInfo;
import org.jmanipulator.core.inner.FieldFiller;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuperClassGenerationHolder<T> extends GenerationHolder {

    private Map<String, String> fieldNameMapper;
    private Map<String, Field> fieldForTemplate;

    public SuperClassGenerationHolder(Class<T> superClass, byte[] byteCode, String targetClassNane, SuperClassEnhanceTemplate template,
                                      ClassInfo superClassInfo, ClassInfo templateClassInfo) {
        super(byteCode, targetClassNane, superClass, template, superClassInfo, templateClassInfo);
    }

    /**
     * 为一个生成的增强类的对象，填充模板中的字段
     */
    public T populateFieldFromTemplate(T object) {
        if (object.getClass() != targetClass) {
            throw new RuntimeException("not an instance of targetClass");
        }

        List<ClassInfo.Field> fields = templateInfo.getFields();
        Map<String, String> nameMapper = getFieldNameMapper();
        Map<String, Field> templateFields = getTemplateField();
        nameMapper.forEach((key, value) -> {
            try {
                Field targetField = targetClass.getDeclaredField(value);
                targetField.setAccessible(true);
                Field templateField = templateFields.get(key);
                targetField.set(object, templateField.get(template));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Unexpected Exception", e);
            }
        });
        return object;
    }

    /**
     * 获取模板类字段名和生成类字段名之间的映射关系
     */
    private Map<String, String> getFieldNameMapper() {
        if (fieldNameMapper == null)
            fieldNameMapper = fieldNameConvert(templateInfo.getFields());
        return fieldNameMapper;
    }

    /**
     * 获取模板对象中的字段信息
     */
    private Map<String, Field> getTemplateField() {
        if (fieldForTemplate == null) {
            Map<String, Field> res = new HashMap<>();
            Class<?> templateClass = template.getClass();
            templateInfo.getFields().forEach(field -> {
                try {
                    String fieldName = field.getFieldName();
                    Field f = templateClass.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    res.put(fieldName, f);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("Unexpected Exception", e);
                }
            });
            fieldForTemplate = res;
        }
        return fieldForTemplate;
    }


    /**
     * 获取一个生成类的已被填充字段的对象
     */
    @SuppressWarnings("unchecked")
    public T getPopulatedInstance(Class<?>[] classes, Object... args) {
        if (classes.length != args.length)
            throw new RuntimeException("mismatch length");
        Class<T> targetClass = (Class<T>) getTargetClass();
        try {
            T t = targetClass.getConstructor(classes).newInstance(args);
            return populateFieldFromTemplate(t);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("no such constructor", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("reflect exception", e);
        }
    }
}
