package top.zproto.jmanipulator.core.inner;

import jdk.internal.org.objectweb.asm.*;
import top.zproto.jmanipulator.core.SuperClassEnhanceTemplate;
import top.zproto.jmanipulator.core.inner.helper.ConstructorAdapter;
import top.zproto.jmanipulator.core.inner.helper.SubclassMethodEnhancerExecutor;
import top.zproto.jmanipulator.core.inner.helper.SuperClassConstructorAdapter;
import top.zproto.jmanipulator.utils.ClassNameAdapter;
import top.zproto.jmanipulator.utils.Constants;

import java.io.IOException;
import java.util.*;

/**
 * 实际执行子类生成的类
 * 1. 目标方法的增强转化
 * 2. 增强类构造器的转化
 * 待检验
 * 1. 关于额外信息的处理问题（如注解等）
 */
public class SubclassEnhanceExecutor implements Constants {
    private final Class<?> superClass;
    private final Set<ClassInfo.Method> targetMethod;
    private final Map<String, String> fieldMapper;
    private SuperClassEnhanceTemplate template;

    public SubclassEnhanceExecutor(Class<?> superClass, List<ClassInfo.Field> fieldToAdd,
                                   List<ClassInfo.Method> methodToEnhance, SuperClassEnhanceTemplate template) {
        this.superClass = superClass;
        this.targetMethod = new HashSet<>(methodToEnhance);
        this.fieldMapper = convert(fieldToAdd);
        this.template = template;

        superClassName = ClassNameAdapter.getInternalName(superClass.getName());
        selfName = ClassNameAdapter.getSyntheticClassName(superClassName);
        templateName = ClassNameAdapter.getInternalName(template.getClass().getName());
    }

    private byte[] templateByteCode;

    public SubclassEnhanceExecutor(Class<?> superClass, List<ClassInfo.Field> fieldToAdd,
                                   List<ClassInfo.Method> methodToEnhance, byte[] template, String templateName) {
        this.superClass = superClass;
        this.targetMethod = new HashSet<>(methodToEnhance);
        this.fieldMapper = convert(fieldToAdd);
        this.templateByteCode = template;

        superClassName = ClassNameAdapter.getInternalName(superClass.getName());
        selfName = ClassNameAdapter.getSyntheticClassName(superClassName);
        this.templateName = ClassNameAdapter.getInternalName(templateName);
    }

    public byte[] doGenerate() {
        ClassReader superClassReader, templateReader;
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        try {
            superClassReader = new ClassReader(superClass.getName()); // 读取被增强父类
            if (templateByteCode == null) {
                templateReader = new ClassReader(template.getClass().getName()); // 读取模板类
            } else {
                templateReader = new ClassReader(templateByteCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("ASM reading class error", e);
        }
        InnerHelper innerHelper = new InnerHelper(ASM5, classWriter); // 构造最后一层classVisitor
        doTemplateAccept(templateReader, innerHelper); // 模板执行visit
        superClassReader.accept(innerHelper, ClassReader.SKIP_DEBUG); // 父类执行visit
        innerHelper.trueVisitEnd(); // 结束
        return classWriter.toByteArray();
    }

    private void doTemplateAccept(final ClassReader reader, final ClassVisitor next) {
        HashSet<String> alreadyAdd = new HashSet<>(); // 用来限制只添加一次字段
        targetMethod.forEach(method -> {
            InnerHelperForTemplate helper = new InnerHelperForTemplate(ASM5, next, method, alreadyAdd);
            reader.accept(helper, ClassReader.SKIP_DEBUG);
        });
    }

    /**
     * 插入字段名字转化
     */
    private Map<String, String> convert(List<ClassInfo.Field> fields) {
        HashMap<String, String> mapper = new HashMap<>();
        fields.forEach(f -> {
            String syntheticFieldName = ClassNameAdapter.getSyntheticFieldName(f.getFieldName());
            mapper.put(f.getFieldName(), syntheticFieldName);
        });
        return mapper;
    }

    // 生成类所必要的信息
    private final String superClassName;
    private final String selfName;
    private final String templateName;

    public String getSelfName() {
        // 执行完doGenerate才能执行
        return selfName;
    }

    /**
     * 内部协助类
     * 用于中间处理class，向新添加的类中添加模板中的全局属性
     * 拦截父类的需要被增强的方法
     */
    private class InnerHelper extends ClassVisitor {

        private boolean visitClass;

        public InnerHelper(int api, ClassVisitor cv) {
            super(api, cv);
        }

        /**
         * 控制生成子类
         */
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if (!visitClass) {
                super.visit(VERSION, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, selfName, null, superClassName, null);
                visitClass = true;
            }
        }

        @Override
        public void visitEnd() {
            // 需要手动调用super的visitEnd
        }

        void trueVisitEnd() {
            super.visitEnd();
        }

        private final Set<ClassInfo.Method> alreadyEnhanced = new HashSet<>();

        /**
         * 处理方法的生成
         */
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ((access & ACC_FINAL) != 0 || (access & ACC_STATIC) != 0)
                return null;
            ClassInfo.Method method = new ClassInfo.Method();
            method.setMethodName(name);
            method.setDesc(desc);
            method.setMethodAccess(access);
            if (targetMethod.contains(method)) {
                if (alreadyEnhanced.contains(method)) // 父类不能再次覆盖
                    return null;
                alreadyEnhanced.add(method);
                MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                return new SubclassMethodEnhancerExecutor(Opcodes.ASM5, methodVisitor, method,
                        superClassName, selfName, templateName, Type.getMethodType(desc), fieldMapper);
            } else if (name.equals("<init>")) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
//                // 固定的构造器格式
//                SuperClassConstructorAdapter.adaptSuperConstructor(methodVisitor, superClassName, name, desc);
                return new ConstructorAdapter(ASM_API, methodVisitor, superClassName, name, desc);
            } else {
//                return super.visitMethod(access, name, desc, signature, exceptions);
                return null;
            }
        }
    }

    /**
     * 内部协助类
     * 用于引入模板中的方法
     */
    private class InnerHelperForTemplate extends ClassVisitor {
        private final ClassInfo.Method targetMethod;

        public InnerHelperForTemplate(int api, ClassVisitor cv, ClassInfo.Method target, Set<String> alreadyAdd) {
            super(api, cv);
            targetMethod = target;
            this.alreadyAdd = alreadyAdd;
        }

        private final Set<String> alreadyAdd;

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            String mappedName = fieldMapper.get(name);
            if (!alreadyAdd.contains(name)) {
                alreadyAdd.add(name);
                return super.visitField(access, mappedName, desc, signature, value);
            }
            return null;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            // 不允许写入Annotation
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            // 使用模板方法进行操作
            if (templateMethod(name, desc)) {
                return super.visitMethod(targetMethod.getMethodAccess(), targetMethod.getMethodName(), targetMethod.getDesc(),
                        targetMethod.getSignature(), targetMethod.getExceptions());
            }
            return null;
        }

        /**
         * 找出模板方法
         */
        private boolean templateMethod(String name, String desc) {
            return name.equals(SuperClassEnhanceTemplate.TEMPLATE)
                    && desc.equals(SuperClassEnhanceTemplate.DESC);
        }
    }
}
