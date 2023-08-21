package top.zproto.jmanipulator.core;

import top.zproto.jmanipulator.core.inner.ClassInfo;
import top.zproto.jmanipulator.utils.ClassLoaderWrapper;
import top.zproto.jmanipulator.utils.ClassNameAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GenerationHolder {
    public static final Class<?>[] EMPTY_CLASSES = new Class[0];
    protected byte[] byteCode;
    protected Class<?> targetClass;
    protected String targetClassName;
    protected Class<?> superClass;
    protected EnhanceTemplate<?> template;
    protected ClassInfo superClassInfo;
    protected ClassInfo templateInfo;

    public GenerationHolder(byte[] byteCode, String targetClassName,
                            Class<?> superClass, EnhanceTemplate<?> template, ClassInfo superClassInfo,
                            ClassInfo templateInfo) {
        this.byteCode = byteCode;
        this.targetClassName = ClassNameAdapter.getReverseInternalName(targetClassName);
        this.superClass = superClass;
        this.template = template;
        this.superClassInfo = superClassInfo;
        this.templateInfo = templateInfo;
    }

    public Map<String, String> fieldNameConvert(List<ClassInfo.Field> fieldName) {
        HashMap<String, String> res = new HashMap<>();
        for (ClassInfo.Field field : fieldName) {
            String name = field.getFieldName();
            res.put(name, ClassNameAdapter.getSyntheticFieldName(name));
        }
        return res;
    }

    public byte[] getByteCode() {
        return byteCode;
    }

    public Class<?> getTargetClass() {
        if (targetClass == null)
            throw new IllegalStateException("class not load!, please invoke getTargetClass with parameter version in first time");
        return targetClass;
    }

    public Class<?> getTargetClass(ClassLoader loader) {
        targetClass = ClassLoaderWrapper.loadClass(loader, targetClassName, byteCode);
        return targetClass;
    }

    public String getTargetClassName() {
        return targetClassName;
    }

    public Class<?> getSuperClass() {
        return superClass;
    }

    public EnhanceTemplate<?> getTemplate() {
        return template;
    }
}
