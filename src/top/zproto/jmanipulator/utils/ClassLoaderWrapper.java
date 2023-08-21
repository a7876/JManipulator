package top.zproto.jmanipulator.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 实现通过byte[]加载类
 */
public class ClassLoaderWrapper {
    private final ClassLoader classLoader;
    private final static Method innerMethod;

    static {
        try {
            innerMethod = ClassLoader.class
                    .getDeclaredMethod(
                            "defineClass",
                            String.class,
                            byte[].class,
                            int.class,
                            int.class
                    );
            innerMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("unexpected error", e);
        }
    }

    public ClassLoaderWrapper(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Class<?> loadClass(String className, byte[] classData) {
        try {
            return (Class<?>) innerMethod.
                    invoke(classLoader, className, classData, 0, classData.length);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("class loading error", e);
        }
    }

    public static Class<?> loadClass(ClassLoader classLoader, String className, byte[] classData) {
        try {
            return (Class<?>) innerMethod.
                    invoke(classLoader, className, classData, 0, classData.length);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("class loading error", e);
        }
    }
}
