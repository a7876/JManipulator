package top.zproto.jmanipulator.core.inner.helper;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import top.zproto.jmanipulator.core.MethodEntryPoint;
import top.zproto.jmanipulator.core.inner.ClassInfo;

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
            checkCast(returnType); // 检查转化是否真确，即使不正确也是抛出ClassCastException而不是VerifyError
            reverseHandleReturnValue(returnType); // 转化为合理的返回值，主要是primitive类型
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
            // 这里需要考虑加入在后面不是直接诶return的情况下，如果处理这个返回值的问题,主要是primitive类型
            handleReturnValue(methodType.getReturnType());
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
     * 处理调用原方法的返回值，如果是void存入null，其他就存入对应的包装类，因为后面如果不是直接返回的情况下一定直接就是astore应为模板方法是返回Object
     */
    private void handleReturnValue(Type returnType) {
        switch (returnType.getSort()) {
            case Type.VOID:
                super.visitInsn(ACONST_NULL);
                break;
            case Type.INT:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                break;
            case Type.BYTE:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                break;
            case Type.CHAR:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                break;
            case Type.SHORT:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                break;
            case Type.BOOLEAN:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                break;
            case Type.LONG:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                break;
            case Type.FLOAT:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                break;
            case Type.DOUBLE:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                break;
        }
    }

    /**
     * 反向处理一次，本质上是拆包的过程
     */
    private void reverseHandleReturnValue(Type returnType) {
        switch (returnType.getSort()) {
            case Type.INT:
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                break;
            case Type.BYTE:
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
                break;
            case Type.CHAR:
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
                break;
            case Type.SHORT:
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
                break;
            case Type.BOOLEAN:
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                break;
            case Type.LONG:
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                break;
            case Type.FLOAT:
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
                break;
            case Type.DOUBLE:
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
                break;
        }
    }

    /**
     * 借助上面统一的装箱操作，这里使用checkCast指令最终类型是否符合
     */
    private void checkCast(Type returnType) {
        switch (returnType.getSort()) {
            case Type.INT:
                super.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                break;
            case Type.BYTE:
                super.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                break;
            case Type.CHAR:
                super.visitTypeInsn(CHECKCAST, "java/lang/Character");
                break;
            case Type.SHORT:
                super.visitTypeInsn(CHECKCAST, "java/lang/Short");
                break;
            case Type.BOOLEAN:
                super.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                break;
            case Type.LONG:
                super.visitTypeInsn(CHECKCAST, "java/lang/Long");
                break;
            case Type.FLOAT:
                super.visitTypeInsn(CHECKCAST, "java/lang/Float");
                break;
            case Type.DOUBLE:
                super.visitTypeInsn(CHECKCAST, "java/lang/Double");
                break;
            case Type.VOID:
                break;
            default:
                System.out.println(returnType.getSort());
                super.visitTypeInsn(CHECKCAST, returnType.getInternalName());
                break;
        }
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
