package pl.varlab.payment.common;

public class PaymentFlowException extends RuntimeException {
    protected PaymentFlowException(String message) {
        super(message);
    }
}
