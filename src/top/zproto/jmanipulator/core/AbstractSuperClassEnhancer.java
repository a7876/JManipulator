package top.zproto.jmanipulator.core;

public abstract class AbstractSuperClassEnhancer<T> implements Enhancer<SuperClassEnhanceTemplate> {
    protected final Class<T> superClass;
    protected final TargetMethodFilter filter;

    public AbstractSuperClassEnhancer(Class<T> superClass, TargetMethodFilter filter) {
        this.superClass = superClass;
        this.filter = filter;
    }

    public Class<?> getSuperClass() {
        return superClass;
    }

    public TargetMethodFilter getFilter() {
        return filter;
    }
}
