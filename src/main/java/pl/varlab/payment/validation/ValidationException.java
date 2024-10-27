package pl.varlab.payment.validation;

import pl.varlab.payment.transaction.TransactionException;

public class ValidationException extends TransactionException {
    public ValidationException(String transactionId, String message) {
        super(transactionId, message);
    }
}
