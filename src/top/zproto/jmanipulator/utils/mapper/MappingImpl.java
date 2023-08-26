package top.zproto.jmanipulator.utils.mapper;

import jdk.internal.org.objectweb.asm.Type;

/**
 * 转化器父类，永远不会直接实例化此类
 */
class MappingImpl {
    final static String METHOD_NAME = "mapping";
    final static String METHOD_DESC;

    static {
        try {
            METHOD_DESC = Type.getMethodDescriptor(MappingImpl.class.getDeclaredMethod("mapping", Object.class, Object.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("unexpected exception",e);
        }
    }

    public void mapping(Object source, Object target) {
    }
}
