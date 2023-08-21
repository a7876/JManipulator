package top.zproto.jmanipulator.exception;

public class IllegalAccessCheckException extends RuntimeException{
    public IllegalAccessCheckException() {
        super();
    }

    public IllegalAccessCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalAccessCheckException(String message) {
        super(message);
    }
}
