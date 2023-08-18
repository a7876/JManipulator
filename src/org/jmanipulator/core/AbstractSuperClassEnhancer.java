package org.jmanipulator.core;

public abstract class AbstractSuperClassEnhancer<T> implements Enhancer<TargetMethod, SuperClassEnhanceTemplate> {
    protected final Class<T> superClass;
    protected final TargetMethodFilter filter;

    public AbstractSuperClassEnhancer(Class<T> superClass, TargetMethodFilter filter) {
        this.superClass = superClass;
        this.filter = filter;
    }

    @Override
    public boolean match(TargetMethod method) {
        return filter.doFilter(method);
    }

    public Class<?> getSuperClass() {
        return superClass;
    }

}
