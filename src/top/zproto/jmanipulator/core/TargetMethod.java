package top.zproto.jmanipulator.core;

import jdk.internal.org.objectweb.asm.Type;
import top.zproto.jmanipulator.utils.ClassNameAdapter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class TargetMethod {
    private final String name;
    private final String[] params;

    public TargetMethod(String name, Class<?>[] params) {
        if (params == null)
            params = new Class[0];
        this.name = name;
        this.params = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            String desc = ClassNameAdapter.getDesc(params[i]);
            this.params[i] = desc;
        }
    }

    public TargetMethod(String name, String desc) {
        this.name = name;
        Type methodType = Type.getMethodType(desc);
        Type[] argumentTypes = methodType.getArgumentTypes();
        this.params = new String[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            this.params[i] = argumentTypes[i].getDescriptor();
        }
    }

    public TargetMethod(Method method) {
        this(method.getName(), method.getParameterTypes());
    }

    public String getName() {
        return name;
    }

    public String[] getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "TargetMethod{" +
                "name='" + name + '\'' +
                ", params=" + Arrays.toString(params) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetMethod that = (TargetMethod) o;
        return Objects.equals(name, that.name) && Arrays.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }
}
