package top.zproto.jmanipulator.core.inner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ClassInfo {
    private String className;
    private String superClass;
    private int classAccess;
    private final List<Field> fields = new ArrayList<>();
    private final List<Method> methods = new ArrayList<>();

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void addField(Field field) {
        this.fields.add(field);
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void addMethod(Method method) {
        this.methods.add(method);
    }

    public int getClassAccess() {
        return classAccess;
    }

    public void setClassAccess(int classAccess) {
        this.classAccess = classAccess;
    }

    public static class Field {
        private String fieldName;
        private String desc;
        private int fieldAccess;

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public int getFieldAccess() {
            return fieldAccess;
        }

        public void setFieldAccess(int fieldAccess) {
            this.fieldAccess = fieldAccess;
        }

        @Override
        public String toString() {
            return "Field{" +
                    "fieldName='" + fieldName + '\'' +
                    ", desc='" + desc + '\'' +
                    ", fieldAccess=" + fieldAccess +
                    '}';
        }
    }

    public static class Method {
        private String methodName;
        private String desc;
        private int methodAccess;
        private String signature;
        private String[] exceptions;

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String[] getExceptions() {
            return exceptions;
        }

        public void setExceptions(String[] exceptions) {
            this.exceptions = exceptions;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public int getMethodAccess() {
            return methodAccess;
        }

        public void setMethodAccess(int methodAccess) {
            this.methodAccess = methodAccess;
        }

        @Override
        public String toString() {
            return "Method{" +
                    "methodName='" + methodName + '\'' +
                    ", desc='" + desc + '\'' +
                    ", methodAccess=" + methodAccess +
                    ", signature='" + signature + '\'' +
                    ", exceptions=" + Arrays.toString(exceptions) +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Method method = (Method) o;
            return Objects.equals(methodName, method.methodName) && Objects.equals(desc, method.desc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(methodName, desc);
        }
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "className='" + className + '\'' +
                ", superClass='" + superClass + '\'' +
                ", classAccess=" + classAccess +
                ", fields=" + fields +
                ", methods=" + methods +
                '}';
    }
}
