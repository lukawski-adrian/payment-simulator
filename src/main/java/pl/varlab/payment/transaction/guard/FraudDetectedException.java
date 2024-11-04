package pl.varlab.payment.transaction.guard;

import pl.varlab.payment.transaction.PaymentTransactionException;
import pl.varlab.payment.transaction.TransactionRequest;

public class FraudDetectedException extends PaymentTransactionException {
    public FraudDetectedException(TransactionRequest transactionRequest, String message) {
        super(transactionRequest, message);
    }
}
