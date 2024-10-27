package pl.varlab.payment.validation;

import pl.varlab.payment.transaction.TransactionException;
import pl.varlab.payment.transaction.TransactionRequest;

public class ValidationException extends TransactionException {
    public ValidationException(TransactionRequest transactionRequest, String message) {
        super(transactionRequest, message);
    }
}
