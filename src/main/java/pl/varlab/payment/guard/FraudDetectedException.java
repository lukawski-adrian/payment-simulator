package pl.varlab.payment.guard;

public class FraudDetectedException extends TransactionException {
    public FraudDetectedException(String transactionId, String message) {
        super(transactionId, message);
    }
}
