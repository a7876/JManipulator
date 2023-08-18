package org.jmanipulator.test;

import org.jmanipulator.core.SuperClassGenerationHolder;
import org.jmanipulator.utils.ClassLoaderWrapper;
import org.jmanipulator.core.SuperClassEnhancer;
import org.jmanipulator.core.TargetMethod;
import org.jmanipulator.core.TargetMethodFilter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

public class test {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        TestClass testClass = new TestClass();
        TargetMethodFilter targetMethodFilter = new TargetMethodFilter();
        targetMethodFilter.add(new TargetMethod(Father.class.getMethod("say")));
        targetMethodFilter.add(new TargetMethod(Father.class.getMethod("rs")));
        targetMethodFilter.add(new TargetMethod(Father.class.getMethod("ri")));
        SuperClassEnhancer<Father> superClassEnhancer = new SuperClassEnhancer<>(Father.class, targetMethodFilter);
        SuperClassGenerationHolder<Father> holder = superClassEnhancer.enhance(testClass);
        holder.getTargetClass(ClassLoader.getSystemClassLoader());
        Father father = holder.getPopulatedInstance(new Class[]{int.class}, 1);
        father.say();
        System.out.println(father.rs());
        System.out.println(father.ri());
        Files.newOutputStream(Paths.get("NewTest.class")).write(holder.getByteCode());
    }
}
