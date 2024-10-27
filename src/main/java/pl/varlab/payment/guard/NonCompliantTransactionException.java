package pl.varlab.payment.guard;


import pl.varlab.payment.transaction.TransactionException;

public class NonCompliantTransactionException extends TransactionException {
    public NonCompliantTransactionException(String transactionId, String message) {
        super(transactionId, message);
    }
}
