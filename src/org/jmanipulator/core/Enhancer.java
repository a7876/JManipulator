package org.jmanipulator.core;

public interface Enhancer<T, P extends EnhanceTemplate<?>> {
    boolean match(T target);

    GenerationHolder enhance(P point);
}
