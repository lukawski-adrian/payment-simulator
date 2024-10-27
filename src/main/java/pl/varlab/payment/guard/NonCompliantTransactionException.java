package pl.varlab.payment.guard;


import pl.varlab.payment.transaction.TransactionException;

import java.util.UUID;

public class NonCompliantTransactionException extends TransactionException {
    public NonCompliantTransactionException(UUID transactionId, String message) {
        super(transactionId, message);
    }
}
