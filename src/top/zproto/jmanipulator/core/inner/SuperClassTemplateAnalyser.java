package top.zproto.jmanipulator.core.inner;

import jdk.internal.org.objectweb.asm.*;

import java.io.IOException;

public class SuperClassTemplateAnalyser {
    public static ClassInfo doAnalyser(Class<?> klass) throws IOException {
        ClassReader classReader = new ClassReader(klass.getName());
        InnerRecorder innerRecorder = new InnerRecorder(Opcodes.ASM5, null);
        classReader.accept(innerRecorder, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return innerRecorder.getInfo();
    }

    public static ClassInfo doAnalyser(byte[] classByteCode) throws IOException {
        ClassReader classReader = new ClassReader(classByteCode);
        InnerRecorder innerRecorder = new InnerRecorder(Opcodes.ASM5, null);
        classReader.accept(innerRecorder, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return innerRecorder.getInfo();
    }

    static class InnerRecorder extends ClassVisitor implements Opcodes {
        ClassInfo info = new ClassInfo();

        public InnerRecorder(int api, ClassVisitor cv) {
            super(api, cv);
        }

        ClassInfo getInfo() {
            return info;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            info.setClassName(name);
            info.setSuperClass(superName);
            info.setClassAccess(access);
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            ClassInfo.Method method = new ClassInfo.Method();
            method.setMethodName(name);
            method.setDesc(desc);
            method.setSignature(signature);
            method.setExceptions(exceptions);
            method.setMethodAccess(access);
            info.addMethod(method);
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            ClassInfo.Field field = new ClassInfo.Field();
            field.setFieldName(name);
            field.setDesc(desc);
            info.addField(field);
            field.setFieldAccess(access);
            return super.visitField(access, name, desc, signature, value);
        }
    }
}
