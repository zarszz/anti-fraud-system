package antifraud.controller.exception;

public class UnProcessableEntityException extends RuntimeException {
    public UnProcessableEntityException(String message) {
        super(message);
    }
}
