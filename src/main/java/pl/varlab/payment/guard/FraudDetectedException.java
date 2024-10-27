package pl.varlab.payment.guard;

import pl.varlab.payment.transaction.TransactionException;

import java.util.UUID;

public class FraudDetectedException extends TransactionException {
    public FraudDetectedException(UUID transactionId, String message) {
        super(transactionId, message);
    }
}
