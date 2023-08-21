package top.zproto.jmanipulator.utils.converter;

import top.zproto.jmanipulator.core.*;
import top.zproto.jmanipulator.core.inner.ClassInfo;
import top.zproto.jmanipulator.core.inner.ClassInfoChecker;
import top.zproto.jmanipulator.core.inner.SubclassEnhanceExecutor;
import top.zproto.jmanipulator.core.inner.SuperClassTemplateAnalyser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 自定义模板且批量增强执行
 */
public class CustomSuperclassEnhanceExecutor {

    private CustomTemplate customTemplate;


    public List<CustomEnhanceGenerationHolder> execute(CustomTemplate template, List<? extends SuperClassEnhancer<?>> enhancers) {
        Objects.requireNonNull(template);
        Objects.requireNonNull(enhancers);
        this.customTemplate = template;
        byte[] byteCode = doConvert(template);
        return enhance(enhancers, byteCode);
    }

    private byte[] doConvert(CustomTemplate template) {
        if (template instanceof CustomTemplateWithNoParam) {
            return TemplateAdapter.convert(template.customTemplateClass, template.customTemplateMethod);
        } else {
            return TemplateAdapter.convert(template.customTemplateClass, template.customTemplateMethod, template.param);
        }
    }

    private List<CustomEnhanceGenerationHolder> enhance(List<? extends SuperClassEnhancer<?>> enhancers, byte[] templateData) {
        List<CustomEnhanceGenerationHolder> res = new ArrayList<>();
        enhancers.forEach(e -> {
            InnerSuperclassEnhance innerSuperclassEnhance = new InnerSuperclassEnhance(e);
            res.add(innerSuperclassEnhance.enhance(templateData));
        });
        return res;
    }

    @SuppressWarnings("rawtypes")
    private class InnerSuperclassEnhance extends SuperClassEnhancer {
        public InnerSuperclassEnhance(Class<?> superClass, TargetMethodFilter filter) {
            super(superClass, filter);
        }

        public InnerSuperclassEnhance(SuperClassEnhancer<?> enhancer) {
            super(enhancer.getSuperClass(), enhancer.getFilter());
        }

        public CustomEnhanceGenerationHolder enhance(byte[] template) {
            ClassInfo classInfo, superClassInfo;
            try {
                superClassInfo = SuperClassTemplateAnalyser.doAnalyser(superClass);
                classInfo = SuperClassTemplateAnalyser.doAnalyser(template);
            } catch (IOException e) {
                throw new RuntimeException("ASM class reading error", e);
            }
            // 准备工作
            ClassInfoChecker.checkClass(superClassInfo);// 检查父类
            List<ClassInfo.Method> targetMethod = getProperTargetMethod(superClassInfo); // 获取所有需要增强的方法
            SubclassEnhanceExecutor enhanceExecutor = new SubclassEnhanceExecutor(superClass, classInfo.getFields(),
                    targetMethod, template, customTemplate.customTemplateClass.getName());
            byte[] bytes = enhanceExecutor.doGenerate();
            return new CustomEnhanceGenerationHolder(
                    bytes,
                    enhanceExecutor.getSelfName(),
                    superClass,
                    superClassInfo,
                    classInfo
            );
        }
    }
}
