package top.zproto.jmanipulator.core;

public interface Enhancer<P extends EnhanceTemplate<?>> {
    GenerationHolder enhance(P point);
}
