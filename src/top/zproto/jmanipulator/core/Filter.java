package top.zproto.jmanipulator.core;

public interface Filter<T> {
    boolean doFilter(T type);
    void add(T t);
}
