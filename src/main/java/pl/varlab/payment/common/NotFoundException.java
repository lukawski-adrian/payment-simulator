package pl.varlab.payment.common;

public class NotFoundException extends PaymentFlowException {
    public NotFoundException(String message) {
        super(message);
    }
}