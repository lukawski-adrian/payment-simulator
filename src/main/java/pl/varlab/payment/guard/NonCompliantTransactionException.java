package pl.varlab.payment.guard;


public class NonCompliantTransactionException extends TransactionException {
    public NonCompliantTransactionException(String transactionId, String message) {
        super(transactionId, message);
    }
}
