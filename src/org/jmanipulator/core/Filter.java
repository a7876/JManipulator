package org.jmanipulator.core;

public interface Filter<T> {
    boolean doFilter(T type);
    void add(T t);
}
