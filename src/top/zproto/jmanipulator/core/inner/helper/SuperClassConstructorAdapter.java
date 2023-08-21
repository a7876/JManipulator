package top.zproto.jmanipulator.core.inner.helper;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;

/**
 * 继承增强类型中，使用本类完成构造器的转换
 */
public class SuperClassConstructorAdapter implements Opcodes {

    public static void adaptSuperConstructor(MethodVisitor mv, String superClassName,
                                             String name, String desc) {
        mv.visitCode();
        loadParams(mv, desc);
        mv.visitMethodInsn(INVOKESPECIAL, superClassName, name, desc, false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void loadParams(MethodVisitor mv, String desc) {
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        mv.visitVarInsn(ALOAD, 0); // load this
        int current = 1;
        for (Type type : argumentTypes) {
            mv.visitVarInsn(type.getOpcode(ILOAD), current);
            current += type.getSize();
        }
    }
}
