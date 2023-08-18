package org.jmanipulator.utils;

import jdk.internal.org.objectweb.asm.Type;

import java.util.concurrent.atomic.AtomicInteger;

public interface ClassNameAdapter {
    String DOT = ".";
    String INTERNAL_SEPARATOR = "/";
    AtomicInteger count = new AtomicInteger();

    static String getInternalName(String name) {
        return name.replace(DOT, INTERNAL_SEPARATOR);
    }

    static String getReverseInternalName(String internalName) {
        return internalName.replace(INTERNAL_SEPARATOR, DOT);
    }

    static String getDesc(Class<?> type) {
        return Type.getType(type).getDescriptor();
    }

    static String getSyntheticFieldName(String baseName) {
        return Constants.SYNTHETIC_FIELD + baseName;
    }

    static String getSyntheticClassName(String baseName) {
        return baseName + Constants.SYNTHETIC_CLASS + count.getAndIncrement();
    }
}
