package top.zproto.jmanipulator.exception;

public class IllegalAccess4FieldCheckException extends IllegalAccessCheckException{
    public IllegalAccess4FieldCheckException() {
        super();
    }

    public IllegalAccess4FieldCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalAccess4FieldCheckException(String message) {
        super(message);
    }
}
