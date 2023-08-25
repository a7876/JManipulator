package top.zproto.jmanipulator.test.defaultTest;

import top.zproto.jmanipulator.test.customTest.Marker;

public class Business {
    private String returnValue = "returnValue";

    public String working() {
        System.out.println("working ......");
        return returnValue;
    }

    @Marker
    public Business() {
    }
}
