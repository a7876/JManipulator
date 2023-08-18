package org.jmanipulator.core;

public class MethodEntryPoint implements TargetPoint {

    public static final String SELF_NAME = "org/jmanipulator/core/MethodEntryPoint";
    public static final String DEFAULT_BEHAVIOUR = "defaultBehaviour";
    public static final String DESC = "()Ljava/lang/Object;";

    @Override
    public Object defaultBehaviour() {
        // empty placeholder
        return null;
    }
}
