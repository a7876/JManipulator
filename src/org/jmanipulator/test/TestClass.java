package org.jmanipulator.test;

import org.jmanipulator.core.MethodEntryPoint;
import org.jmanipulator.core.SuperClassEnhanceTemplate;

public class TestClass extends SuperClassEnhanceTemplate {
    int a = 1;

    @Override
    public Object template(MethodEntryPoint point) {
        System.out.println("enhance speaking " + a);
        return point.defaultBehaviour();
    }
}
