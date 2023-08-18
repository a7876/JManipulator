package org.jmanipulator.core.inner.helper;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import org.jmanipulator.core.MethodEntryPoint;
import org.jmanipulator.core.inner.ClassInfo;

import java.util.Map;

/**
 * 协助类，用于负责转化模板方法中的占位调用和成员变量调用映射
 */
public class SubclassMethodEnhancerExecutor extends MethodVisitor implements Opcodes {
    private final ClassInfo.Method currentMethod;
    private final String superClassName;
    private final Map<String, String> fieldMapper;
    private final String selfName;
    private final String templateName;
    private final Type methodType;
    private final int firstArgSize;

    /**
     * <p>
     * 待优化点
     * 1. 处理多余的aload_1 (在EntryPoint调用处)
     */

    /**
     * 被增强函数的实际转化适配类
     */
    public SubclassMethodEnhancerExecutor(int api, MethodVisitor mv, ClassInfo.Method currentMethod,
                                          String superClassName,
                                          String selfName,
                                          String templateName,
                                          Type methodType,
                                          Map<String, String> fieldMapper) {
        super(api, mv);
        this.currentMethod = currentMethod;
        this.superClassName = superClassName;
        this.fieldMapper = fieldMapper;
        this.selfName = selfName;
        this.templateName = templateName;
        this.methodType = methodType;
        this.firstArgSize = setFirstArgSize();
    }


    @Override
    public void visitCode() {
        // 确保aload_1不出现异常
        super.visitCode();
        if (firstArgSize == -1) { // make sure aload_1 is available
            super.visitInsn(ACONST_NULL);
            super.visitVarInsn(ASTORE, 1);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == ARETURN) {
            Type returnType = methodType.getReturnType();
            super.visitInsn(returnType.getOpcode(IRETURN));
        } else {
            super.visitInsn(opcode);
        }
    }

    /**
     * 调用被增强的原方法的方法
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (isPlaceHolder(owner, name, desc)) {
            popDuplicatedVar();
            loadParams();
            super.visitMethodInsn(INVOKESPECIAL, superClassName, currentMethod.getMethodName(), currentMethod.getDesc(), false);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    /**
     * 找出占位调用
     */
    private boolean isPlaceHolder(String owner, String name, String desc) {
        return owner.equals(MethodEntryPoint.SELF_NAME) &&
                name.equals(MethodEntryPoint.DEFAULT_BEHAVIOUR) && desc.equals(MethodEntryPoint.DESC);
    }


    /**
     * 替换模板中全局变量的名字
     */
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        String newName = fieldMapper.get(name);
        if (newName != null && owner.equals(templateName)) {
            super.visitFieldInsn(opcode, selfName, newName, desc);
        } else {
            super.visitFieldInsn(opcode, owner, name, desc);
        }
    }

    /**
     * 加载所有必须的参数以调用被增强的方法
     */
    private void loadParams() {
        Type[] argumentTypes = methodType.getArgumentTypes();
        super.visitVarInsn(ALOAD, 0); // load this
        int current = 1;
        for (Type type : argumentTypes) {
            super.visitVarInsn(type.getOpcode(ILOAD), current);
            current += type.getSize();
        }
    }

    /**
     * 确定第一个局部变量的信息
     */
    private int setFirstArgSize() {
        Type[] argumentTypes = methodType.getArgumentTypes();
        if (argumentTypes.length == 0)
            return -1;
        return argumentTypes[0].getSize();
    }

    /**
     * 弹出多余的栈上操作数
     */
    private void popDuplicatedVar() {
        if (firstArgSize == 2) {
            super.visitInsn(POP2);
        } else {
            super.visitInsn(POP);
        }
    }
}
