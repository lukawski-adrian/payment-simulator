package pl.varlab.payment.common;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
