package pl.varlab.payment.guard;


import pl.varlab.payment.transaction.TransactionException;
import pl.varlab.payment.transaction.TransactionRequest;

public class NonCompliantTransactionException extends TransactionException {
    public NonCompliantTransactionException(TransactionRequest transactionRequest, String message) {
        super(transactionRequest, message);
    }
}
