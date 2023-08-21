package top.zproto.jmanipulator.test.customTest;

import com.sun.org.slf4j.internal.LoggerFactory;
import top.zproto.jmanipulator.core.*;
import top.zproto.jmanipulator.utils.converter.CustomEnhanceGenerationHolder;
import top.zproto.jmanipulator.utils.converter.CustomSuperclassEnhanceExecutor;
import top.zproto.jmanipulator.utils.converter.CustomTemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class Test {
    public static void main(String[] args) throws NoSuchMethodException {
        CustomTemplate template = CustomTemplate.getTemplate(Template.class, "template", MethodEntryPoint.class);
        CustomSuperclassEnhanceExecutor executor = new CustomSuperclassEnhanceExecutor();

        TargetMethodFilter targetMethodFilter = new TargetMethodFilter();
        targetMethodFilter.add(new TargetMethod(BusinessImpl.class.getDeclaredMethod("working")));
        SuperClassEnhancer<BusinessImpl> enhancerE = new SuperClassEnhancer<>(BusinessImpl.class, targetMethodFilter);
        List<SuperClassEnhancer<BusinessImpl>> superClassEnhancers = Arrays.asList(enhancerE);
        List<CustomEnhanceGenerationHolder> execute = executor.execute(template, superClassEnhancers);
        execute.forEach(e -> {
            Class<?> kclass = e.getClass(ClassLoader.getSystemClassLoader());
            try {
                Constructor<?> constructor = kclass.getConstructor();
                BusinessImpl o = (BusinessImpl) constructor.newInstance();
                e.populateFields(o, "logger", LoggerFactory.getLogger(Test.class));
                o.working();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }

            Field[] declaredFields = kclass.getDeclaredFields();
            for (Field f : declaredFields) {
                if (f.isAnnotationPresent(Marker.class)) {
                    System.out.println(f);
                }
            }
        });
    }
}
