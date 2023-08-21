package top.zproto.jmanipulator.test.defaultTest;

import top.zproto.jmanipulator.core.MethodEntryPoint;
import top.zproto.jmanipulator.core.SuperClassEnhanceTemplate;

public class Template implements SuperClassEnhanceTemplate {
    private final String templateString = "templateString";
    private final int templateInt = 123;

    @Override
    public Object template(MethodEntryPoint point) {
        System.out.println("templateString " + templateString);
        Object res = point.defaultBehaviour();
        System.out.println("templateInt " + templateInt);
        return res;
    }
}
