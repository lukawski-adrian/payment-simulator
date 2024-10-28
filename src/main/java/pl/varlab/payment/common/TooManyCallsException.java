package pl.varlab.payment.common;

public class TooManyCallsException extends RuntimeException {
    public TooManyCallsException(String message) {
        super(message);
    }
}
