package org.jmanipulator.core;

import java.util.HashSet;
import java.util.Set;

public class TargetMethodFilter implements Filter<TargetMethod> {
    private final Set<TargetMethod> set = new HashSet<>();

    @Override
    public boolean doFilter(TargetMethod type) {
        return set.contains(type);
    }

    @Override
    public void add(TargetMethod targetMethod) {
        set.add(targetMethod);
    }
}
