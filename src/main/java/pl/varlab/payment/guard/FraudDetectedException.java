package pl.varlab.payment.guard;

import pl.varlab.payment.transaction.TransactionException;
import pl.varlab.payment.transaction.TransactionRequest;

public class FraudDetectedException extends TransactionException {
    public FraudDetectedException(TransactionRequest transactionRequest, String message) {
        super(transactionRequest, message);
    }
}
