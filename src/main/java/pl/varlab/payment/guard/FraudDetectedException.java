package pl.varlab.payment.guard;

import pl.varlab.payment.transaction.TransactionException;

public class FraudDetectedException extends TransactionException {
    public FraudDetectedException(String transactionId, String message) {
        super(transactionId, message);
    }
}
