package top.zproto.jmanipulator.utils.converter;

import jdk.internal.org.objectweb.asm.*;
import top.zproto.jmanipulator.core.EnhanceTemplate;
import top.zproto.jmanipulator.core.MethodEntryPoint;
import top.zproto.jmanipulator.core.SuperClassEnhanceTemplate;
import top.zproto.jmanipulator.utils.ClassNameAdapter;
import top.zproto.jmanipulator.utils.Constants;

import java.io.IOException;

class TemplateAdapter {
    public static byte[] convert(Class<?> source, String methodName, Class<?>... arg) {
        int length = arg.length;
        if (length == 1) {
            Class<?> klass = arg[0];
            if (klass != MethodEntryPoint.class)
                throw new RuntimeException("the parameter type is not MethodEntryPoint");
            check(source, methodName, klass);
            return doConvert(source, methodName, true);
        } else if (length == 0) {
            check(source, methodName, null);
            return doConvert(source, methodName, false);
        } else {
            throw new IllegalArgumentException("number of parameter must be 1 or 0");
        }
    }

    private static byte[] doConvert(Class<?> source, String methodName, boolean hasParam) {
        ClassReader cr = getClassReader(source);
        String className = getClassName(ClassNameAdapter.getInternalName(source.getName()));
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        InnerClassConverter converter = new InnerClassConverter(Constants.ASM_API, cw, methodName, hasParam, className);
        cr.accept(converter, ClassReader.SKIP_DEBUG);
        return cw.toByteArray();
    }

    private static ClassReader getClassReader(Class<?> source) {
        try {
            return new ClassReader(source.getName());
        } catch (IOException e) {
            throw new RuntimeException("ASM class reading error", e);
        }
    }

    private static String getClassName(String className) {
        return ClassNameAdapter.getSyntheticClassName(className);
    }

    private static void check(Class<?> source, String methodName, Class<?> arg) {
        Class<?> returnType;
        try {
            if (arg != null) {
                returnType = source.getDeclaredMethod(methodName, arg).getReturnType();
            } else {
                returnType = source.getDeclaredMethod(methodName).getReturnType();
            }
            if (returnType.isPrimitive() || returnType == void.class) {
                throw new IllegalArgumentException("method return type not expected");
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(" method named " + methodName + " with specified parameter not existed ", e);
        }
    }

    private static class InnerClassConverter extends ClassVisitor {
        private final String methodName;
        private final boolean hasParameter;
        private final String newName;

        public InnerClassConverter(int api, ClassVisitor cv, String methodName, boolean hasParameter, String newName) {
            super(api, cv);
            this.methodName = methodName;
            this.hasParameter = hasParameter;
            this.newName = newName;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            int newLength = interfaces.length + 1;
            String[] newInterfaces = new String[newLength];
            System.arraycopy(interfaces, 0, newInterfaces, 0, newLength - 1);
            newInterfaces[newLength - 1] = ClassNameAdapter.getInternalName(SuperClassEnhanceTemplate.class.getName());
            super.visit(version, access, newName, signature, superName, newInterfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (isTarget(name, desc)) // 转化为目标方法
                return super.visitMethod(access, EnhanceTemplate.TEMPLATE, SuperClassEnhanceTemplate.DESC, signature, exceptions);
            else {
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }

        private boolean isTarget(String name, String desc) {
            if (!name.equals(methodName))
                return false;
            Type[] argumentTypes = Type.getMethodType(desc).getArgumentTypes();
            if (argumentTypes.length == 0 && !hasParameter)
                return true;
            if (argumentTypes.length == 1 && hasParameter) {
                Type argumentType = argumentTypes[0];
                return argumentType.getSort() == Type.OBJECT
                        && argumentType.getInternalName().equals(MethodEntryPoint.SELF_NAME);
            }
            return false;
        }
    }
}
