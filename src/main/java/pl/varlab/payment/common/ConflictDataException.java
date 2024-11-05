package pl.varlab.payment.common;

public class ConflictDataException extends PaymentFlowException {
    public ConflictDataException(String message) {
        super(message);
    }
}