package top.zproto.jmanipulator.core.inner.helper;

import jdk.internal.org.objectweb.asm.*;

/**
 * 根据父类构造器适配被增强对象的构造器
 */
public class ConstructorAdapter extends MethodVisitor {
    private final String superClassName;
    private final String name;
    private final String desc;

    public ConstructorAdapter(int api, MethodVisitor mv, String superClassName, String name, String desc) {
        super(api, mv);
        this.superClassName = superClassName;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        SuperClassConstructorAdapter.adaptSuperConstructor(mv, superClassName, name, desc);
    }


    /*
    *
    * 以下方法全部重写，不产生任何实际作用
    *
    * */

    @Override
    public void visitInsn(int opcode) {
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
    }

    @Override
    public void visitLabel(Label label) {
    }

    @Override
    public void visitLdcInsn(Object cst) {
    }

    @Override
    public void visitIincInsn(int var, int increment) {
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return null;
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return null;
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
        return null;
    }
}
