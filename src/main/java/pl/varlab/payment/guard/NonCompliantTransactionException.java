package pl.varlab.payment.guard;


import pl.varlab.payment.transaction.PaymentTransactionException;
import pl.varlab.payment.transaction.TransactionRequest;

public class NonCompliantTransactionException extends PaymentTransactionException {
    public NonCompliantTransactionException(TransactionRequest transactionRequest, String message) {
        super(transactionRequest, message);
    }
}
