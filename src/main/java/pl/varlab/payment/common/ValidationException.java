package pl.varlab.payment.common;

public class ValidationException extends PaymentFlowException {
    public ValidationException(String message) {
        super(message);
    }
}
