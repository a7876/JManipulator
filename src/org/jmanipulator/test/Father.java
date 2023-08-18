package org.jmanipulator.test;

public class Father implements Sayable {
    public Father(int count) {
        System.out.println("ds");
    }

    @Override
    public void say() {
        actualSay();
    }

    public String rs() {
        return "hello!";
    }

    public int ri() {
        return 132245;
    }

    private void actualSay() {
        System.out.println("father speaking");
    }
}
