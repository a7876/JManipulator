package top.zproto.jmanipulator.core.inner;

import jdk.internal.org.objectweb.asm.Opcodes;
import top.zproto.jmanipulator.exception.IllegalAccess4ClassCheckException;
import top.zproto.jmanipulator.exception.IllegalAccess4MethodCheckException;

import java.util.function.Consumer;

public class ClassInfoChecker implements Opcodes {
    private final static Consumer<ClassInfo> NO_FINAL_MUST_PUBLIC_NO_ABSTRACT = info -> {
        int classAccess = info.getClassAccess();
        if ((classAccess & ACC_FINAL) != 0)
            throw new IllegalAccess4ClassCheckException(info.getClassName() + " is final class");
        if ((classAccess & ACC_ABSTRACT) != 0)
            throw new IllegalAccess4ClassCheckException(info.getClassName() + " is abstract class");
        if ((classAccess & ACC_PUBLIC) == 0)
            throw new IllegalAccess4ClassCheckException(info.getClassName() + " is not public");
    };

    private final static Consumer<ClassInfo.Field> ALL_ACCEPT = field -> {
    };

    private final static Consumer<ClassInfo.Method> NO_FINAL_NO_PRIVATE_NO_STATIC = method -> {
        int methodAccess = method.getMethodAccess();
        if ((methodAccess & ACC_FINAL) != 0)
            throw new IllegalAccess4MethodCheckException(method.getMethodName() + " is final method");
        if ((methodAccess & ACC_PRIVATE) != 0)
            throw new IllegalAccess4MethodCheckException(method.getMethodName() + " is private method");
        if ((methodAccess & ACC_STATIC) != 0)
            throw new IllegalAccess4MethodCheckException(method.getMethodName() + " is static method");
    };

    public static void checkFields(Consumer<ClassInfo.Field> consumer, ClassInfo.Field field) {
        consumer.accept(field);
    }

    public static void checkClass(Consumer<ClassInfo> consumer, ClassInfo info) {
        consumer.accept(info);
    }

    public static void checkMethod(Consumer<ClassInfo.Method> consumer, ClassInfo.Method method) {
        consumer.accept(method);
    }

    public static void checkFields(ClassInfo.Field field) {
        ALL_ACCEPT.accept(field);
    }

    public static void checkClass(ClassInfo info) {
        NO_FINAL_MUST_PUBLIC_NO_ABSTRACT.accept(info);
    }

    public static void checkMethod(ClassInfo.Method method) {
        NO_FINAL_NO_PRIVATE_NO_STATIC.accept(method);
    }

}
