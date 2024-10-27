package pl.varlab.payment.validation;

import pl.varlab.payment.transaction.TransactionException;

import java.util.UUID;

public class ValidationException extends TransactionException {
    public ValidationException(UUID transactionId, String message) {
        super(transactionId, message);
    }
}
