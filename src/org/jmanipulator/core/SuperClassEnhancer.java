package org.jmanipulator.core;

import org.jmanipulator.core.inner.ClassInfo;
import org.jmanipulator.core.inner.ClassInfoChecker;
import org.jmanipulator.core.inner.SubclassEnhanceExecutor;
import org.jmanipulator.core.inner.SuperClassTemplateAnalyser;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用拓展父类，覆盖原方法的形式实现增强功能
 * 限制 ： 只能增强可见非final类的可见非final实例方法
 */
public class SuperClassEnhancer<T> extends AbstractSuperClassEnhancer<T> {

    public SuperClassEnhancer(Class<T> superClass, TargetMethodFilter filter) {
        super(superClass, filter);
    }

    @Override
    public SuperClassGenerationHolder<T> enhance(SuperClassEnhanceTemplate template) {
        ClassInfo classInfo, superClassInfo;
        try {
            superClassInfo = SuperClassTemplateAnalyser.doAnalyser(superClass);
            classInfo = SuperClassTemplateAnalyser.doAnalyser(template.getClass());
        } catch (IOException e) {
            throw new RuntimeException("ASM class reading error", e);
        }
        // 准备工作
        ClassInfoChecker.checkClass(superClassInfo);// 检查父类
        List<ClassInfo.Method> targetMethod = getProperTargetMethod(superClassInfo);
        SubclassEnhanceExecutor enhanceExecutor = new SubclassEnhanceExecutor(superClass, classInfo.getFields(), targetMethod, template);
        byte[] bytes = enhanceExecutor.doGenerate();
        return new SuperClassGenerationHolder<>(
                superClass,
                bytes,
                enhanceExecutor.getSelfName(),
                template,
                superClassInfo,
                classInfo
                );
    }

    /**
     * 找出需要增强目标方法，并且检查是否符合增强要求
     */
    private List<ClassInfo.Method> getProperTargetMethod(ClassInfo superInfo) {
        return superInfo.getMethods().stream().filter(method -> {
            TargetMethod targetMethod = new TargetMethod(method.getMethodName(), method.getDesc());
            if (filter.doFilter(targetMethod)) {
                ClassInfoChecker.checkMethod(method);
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }
}
