package antifraud.controller.exception;

public class RoleAttachedException extends RuntimeException {
    public RoleAttachedException(String message) {
        super(message);
    }
}
