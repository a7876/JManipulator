package top.zproto.jmanipulator.utils.mapper;

import jdk.internal.org.objectweb.asm.*;
import top.zproto.jmanipulator.utils.ClassLoaderWrapper;
import top.zproto.jmanipulator.utils.ClassNameAdapter;
import top.zproto.jmanipulator.utils.Constants;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 对象属性复制工具类<br>
 * 实现是基于动态生成一个专门转化类执行赋值操作，避免每次赋值的反射开支
 * source是属性值的来源，target是目标
 * ps:对于source而言，只有具有public getter实例方法的属性可以作为来源，而对于target而言，只有具有public setter实例方法的属性可以被赋值
 * 可以使用<code>MappingIgnore</code>注解来声明不接受被复制的值
 * 属性的复制只针对当前类，不针对父类
 */
public final class FieldMapper implements Opcodes {
    // thread safe

    private static final Map<MapperInfo, MappingImpl> caches = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> concurrentController = new ConcurrentHashMap<>();
    private static final Object PLACE_HOLDER = new Object();

    /**
     * @param source 源对象
     * @param target 目标对象
     * @return 即为target目标对象
     */
    public static Object map(Object source, Object target) {
        MapperInfo mapperInfo = new MapperInfo(source.getClass(), target.getClass());
        MappingImpl mapping = caches.get(mapperInfo);
        if (mapping == null) {
            try {
                concurrentCheck(source.getClass());
                mapping = caches.get(mapperInfo);
                if (mapping == null)
                    first(source, target, mapperInfo);
                else
                    mapping.mapping(source, target);
            } finally {
                concurrentRelease(source.getClass());
            }
        } else {
            mapping.mapping(source, target);
        }
        return target;
    }

    private static void first(Object source, Object target, final MapperInfo mapperInfo) {
        mapperInfo.checkModifier();
        List<Method> sourceMethods = extractFieldFromSource(mapperInfo.source);
        List<Method> targetMethods = extractFieldFromTarget(mapperInfo.target);
        List<CoupleMethod> coupleMethods = matchCoupleMethod(sourceMethods, targetMethods);
        MappingImpl mapper = (MappingImpl) getMapper(mapperInfo, coupleMethods);
        caches.put(mapperInfo, mapper);
        mapper.mapping(source, target);
    }

    /**
     * 处理并发
     */
    private static void concurrentCheck(Class<?> source) {
        while (concurrentController.putIfAbsent(source, PLACE_HOLDER) != null) ;
    }

    private static void concurrentRelease(Class<?> source) {
        concurrentController.remove(source);
    }

    private static List<Method> extractFieldFromSource(Class<?> source) {
        Set<String> fields = Arrays.stream(source.getDeclaredFields())
                .filter(field -> {
                    MappingIgnore annotation = field.getAnnotation(MappingIgnore.class);
                    if (annotation == null)
                        return true;
                    else
                        return !annotation.getIgnore();
                })
                .map(Field::getName).collect(Collectors.toSet());

        return Arrays.stream(source.getMethods()).filter(method -> {
            String name = getFieldNameInGetterMethodName(method.getName());
            MappingIgnore annotation = method.getAnnotation(MappingIgnore.class);
            if ((annotation != null && annotation.getIgnore()) || name == null)
                return false;
            int modifiers = method.getModifiers();
            return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)
                    && method.getParameterTypes().length == 0 && fields.contains(name);
        }).collect(Collectors.toList());
    }

    private static List<Method> extractFieldFromTarget(Class<?> target) {
        Set<String> fields = Arrays.stream(target.getDeclaredFields())
                .filter(field -> {
                    MappingIgnore annotation = field.getAnnotation(MappingIgnore.class);
                    if (annotation == null)
                        return true;
                    return !annotation.setIgnore();
                })
                .map(Field::getName).collect(Collectors.toSet());

        return Arrays.stream(target.getMethods()).filter(method -> {
            String name = getFieldNameInSetterMethodName(method.getName());
            MappingIgnore annotation = method.getAnnotation(MappingIgnore.class);
            if ((annotation != null && annotation.setIgnore()) || name == null)
                return false;
            int modifiers = method.getModifiers();
            return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)
                    && method.getParameterTypes().length == 1 && fields.contains(name);
        }).collect(Collectors.toList());
    }

    private static List<CoupleMethod> matchCoupleMethod(List<Method> source, List<Method> target) {
        Map<String, Method> tm = new HashMap<>();
        target.forEach(method -> tm.put(getFieldNameInSetterMethodName(method.getName()), method));

        List<CoupleMethod> res = new ArrayList<>();
        source.forEach(method -> {
            String name = getFieldNameInGetterMethodName(method.getName());
            Method tmp = tm.get(name);
            if (tmp != null) {
                CoupleMethod coupleMethod = checkType(method, tmp);
                if (coupleMethod != null)
                    res.add(coupleMethod);
            }
        });

        return res;
    }

    /**
     * 生成具体的Mapper
     */
    private static Object getMapper(MapperInfo info, List<CoupleMethod> coupleMethods) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        String superClassName = ClassNameAdapter.getInternalName(MappingImpl.class.getName());
        String uniqueName = ClassNameAdapter.getGlobalUniqueName("top/zproto/jmanipulator/utils/mapper/");
        classWriter.visit(Constants.VERSION, ACC_PUBLIC | ACC_SYNTHETIC,
                uniqueName, null, superClassName, null);
        // 生成基本的无参构造器
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", "()V", false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();

        // 重写map方法
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC, MappingImpl.METHOD_NAME,
                MappingImpl.METHOD_DESC, null, null);
        String sourceName = ClassNameAdapter.getInternalName(info.source.getName());
        String targetName = ClassNameAdapter.getInternalName(info.target.getName());
        methodVisitor.visitCode();
        for (CoupleMethod coupleMethod : coupleMethods) {
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitTypeInsn(CHECKCAST, targetName); // 转化类型
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitTypeInsn(CHECKCAST, sourceName); // 转化类型
            Method getter = coupleMethod.getter;
            Method setter = coupleMethod.setter;
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, sourceName,
                    getter.getName(), Type.getMethodDescriptor(getter), false);
            if (coupleMethod.box) {
                box(coupleMethod, methodVisitor);
            } else if (coupleMethod.unbox) {
                unbox(coupleMethod, methodVisitor);
            }
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, targetName,
                    setter.getName(), Type.getMethodDescriptor(setter), false);
        }
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(2, 3);
        methodVisitor.visitEnd();
        classWriter.visitEnd();

        // 实例化
        try {
            return ClassLoaderWrapper.loadClass(FieldMapper.class.getClassLoader(),
                    ClassNameAdapter.getReverseInternalName(uniqueName), classWriter.toByteArray()).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("unexpected exception", e);
        }
    }

    private static String getFieldNameInGetterMethodName(String name) {
        if (name.startsWith("get")) {
            return firstToLower(name.substring(3));
        } else if (name.startsWith("is")) {
            return firstToLower(name.substring(2));
        } else {
            return null;
        }
    }

    private static String getFieldNameInSetterMethodName(String name) {
        if (name.startsWith("set")) {
            return firstToLower(name.substring(3));
        } else {
            return null;
        }
    }

    private static String firstToLower(String str) {
        StringBuilder stringBuilder = new StringBuilder(str);
        stringBuilder.setCharAt(0, Character.toLowerCase(str.charAt(0)));
        return stringBuilder.toString();
    }

    private static void box(final CoupleMethod coupleMethod, MethodVisitor mv) {
        String desc = "(" +
                coupleMethod.primitiveType.getDescriptor() +
                ")" +
                coupleMethod.boxType.getDescriptor();
        mv.visitMethodInsn(INVOKESTATIC, coupleMethod.boxType.getInternalName(), "valueOf", desc, false);
    }

    private static void unbox(final CoupleMethod coupleMethod, MethodVisitor mv) {
        String name;
        switch (coupleMethod.primitiveType.getSort()) {
            case Type.BOOLEAN:
                name = "boolean";
                break;
            case Type.CHAR:
                name = "char";
                break;
            case Type.BYTE:
                name = "byte";
                break;
            case Type.SHORT:
                name = "short";
                break;
            case Type.INT:
                name = "int";
                break;
            case Type.FLOAT:
                name = "float";
                break;
            case Type.LONG:
                name = "long";
                break;
            case Type.DOUBLE:
                name = "double";
                break;
            default: // unreachable
                name = "";
        }
        name += "Value";
        String desc = "()" + coupleMethod.primitiveType.getDescriptor();
        // 防止空指针
        Label normal = new Label();
        Label directEnd = new Label();
        mv.visitVarInsn(ASTORE, 3);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitJumpInsn(IFNONNULL, normal);
        int sort = coupleMethod.primitiveType.getSort();
        switch (sort) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                mv.visitInsn(ICONST_0);
                break;
            case Type.FLOAT:
                mv.visitInsn(FCONST_0);
                break;
            case Type.LONG:
                mv.visitInsn(LCONST_0);
                break;
            case Type.DOUBLE:
                mv.visitInsn(DCONST_0);
                break;
        }
        mv.visitJumpInsn(GOTO, directEnd);
        mv.visitLabel(normal);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKEVIRTUAL, coupleMethod.boxType.getInternalName(), name, desc, false);
        mv.visitLabel(directEnd);
    }


    /**
     * 检查是否需要包装或者解包装
     */
    private static CoupleMethod checkType(Method source, Method target) {
        Type sourceReturnType = Type.getType(source.getReturnType());
        Type targetReturnType = Type.getType(target.getParameterTypes()[0]);
        if (sourceReturnType.equals(targetReturnType)) {
            return new CoupleMethod(source, target);
        } else {
            int ss = sourceReturnType.getSort();
            int ts = targetReturnType.getSort();
            Type boxType;
            CoupleMethod coupleMethod;
            if (ss <= 8 && ts == Type.OBJECT) {
                boxType = checkBoxing(sourceReturnType, targetReturnType);
                if (boxType != null) {
                    coupleMethod = new CoupleMethod(source, target);
                    coupleMethod.setBox(true);
                    coupleMethod.setBoxType(boxType);
                    coupleMethod.setPrimitiveType(sourceReturnType);
                    return coupleMethod;
                }
            } else if (ss == Type.OBJECT && ts <= 8) {
                boxType = checkBoxing(targetReturnType, sourceReturnType);
                if (boxType != null) {
                    coupleMethod = new CoupleMethod(source, target);
                    coupleMethod.setUnbox(true);
                    coupleMethod.setBoxType(boxType);
                    coupleMethod.setPrimitiveType(targetReturnType);
                    return coupleMethod;
                }
            } else {
                return null;
            }
            return null;
        }
    }

    /**
     * @param primitive 原子类型
     * @param Object    对象类型
     * @return 如果是null表示不相符，非null就是对应的包装类
     */
    private static Type checkBoxing(Type primitive, Type Object) {
        int sort = primitive.getSort();
        String internalName = Object.getInternalName();
        switch (sort) {
            case Type.BOOLEAN:
                if (!Type.getInternalName(Boolean.class).equals(internalName))
                    return null;
                break;
            case Type.CHAR:
                if (!Type.getInternalName(Character.class).equals(internalName))
                    return null;
                break;
            case Type.BYTE:
                if (!Type.getInternalName(Byte.class).equals(internalName))
                    return null;
                break;
            case Type.SHORT:
                if (!Type.getInternalName(Short.class).equals(internalName))
                    return null;
                break;
            case Type.INT:
                if (!Type.getInternalName(Integer.class).equals(internalName))
                    return null;
                break;
            case Type.FLOAT:
                if (!Type.getInternalName(Float.class).equals(internalName))
                    return null;
                break;
            case Type.LONG:
                if (!Type.getInternalName(Long.class).equals(internalName))
                    return null;
                break;
            case Type.DOUBLE:
                if (!Type.getInternalName(Double.class).equals(internalName))
                    return null;
                break;
            default:
                return null;
        }
        return Object;
    }

    private static class MapperInfo {
        Class<?> source;
        Class<?> target;

        public MapperInfo(Class<?> source, Class<?> target) {
            this.source = source;
            this.target = target;
        }

        public void checkModifier() {
            int modifiers = source.getModifiers();
            if (!Modifier.isPublic(modifiers))
                throw new IllegalStateException(source + " is not public");
            modifiers = target.getModifiers();
            if (!Modifier.isPublic(modifiers))
                throw new IllegalStateException(target + " is not public");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MapperInfo that = (MapperInfo) o;
            return Objects.equals(source, that.source) && Objects.equals(target, that.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target);
        }
    }

    private static class CoupleMethod {
        private final Method getter;
        private final Method setter;
        private boolean unbox;
        private boolean box;
        private Type boxType;
        private Type primitiveType;

        public CoupleMethod(Method getter, Method setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public boolean isUnbox() {
            return unbox;
        }

        public void setUnbox(boolean unbox) {
            this.unbox = unbox;
        }

        public boolean isBox() {
            return box;
        }

        public void setBox(boolean box) {
            this.box = box;
        }

        public Type getBoxType() {
            return boxType;
        }

        public void setBoxType(Type boxType) {
            this.boxType = boxType;
        }

        public Type getPrimitiveType() {
            return primitiveType;
        }

        public void setPrimitiveType(Type primitiveType) {
            this.primitiveType = primitiveType;
        }
    }

}
