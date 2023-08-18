package org.jmanipulator.core;

import org.jmanipulator.core.inner.ClassInfo;
import org.jmanipulator.utils.ClassLoaderWrapper;
import org.jmanipulator.utils.ClassNameAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class GenerationHolder {
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

    public Map<String, String> fieldNameConvert(List<ClassInfo.Field> fieldName){
        HashMap<String, String> res = new HashMap<>();
        for (ClassInfo.Field field : fieldName) {
            String name = field.getFieldName();
            res.put(name, ClassNameAdapter.getSyntheticFieldName(name));
        }
        return res;
    }

    public SourceMeta convert(ClassInfo info) {
        SourceMeta sourceMeta = new SourceMeta();
        List<ClassInfo.Field> fields = info.getFields();
        List<SourceField> newFields = new ArrayList<>();
        fields.forEach(i -> {
            SourceField sourceField = new SourceField();
            sourceField.name = i.getFieldName();
            sourceField.desc = i.getDesc();
            newFields.add(sourceField);
        });
        sourceMeta.fields = newFields;
        return sourceMeta;
    }

    static class SourceMeta {
        private List<SourceField> fields = new ArrayList<>();

        public void add(SourceField sourceField) {
            fields.add(sourceField);
        }

        public void forEach(Consumer<SourceField> consumer) {
            fields.forEach(consumer);
        }
    }

    static class SourceField {
        private String name;
        private String desc;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    public byte[] getByteCode() {
        return byteCode;
    }

    public Class<?> getTargetClass() {
        if (targetClass == null)
            throw new IllegalStateException("class not load!");
        return targetClass;
    }

    public Class<?> getTargetClass(ClassLoader loader) {
        targetClass = new ClassLoaderWrapper(loader).loadClass(targetClassName, byteCode);
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

    public ClassInfo getSuperClassInfo() {
        return superClassInfo;
    }

    public ClassInfo getTemplateInfo() {
        return templateInfo;
    }
}
