package pl.varlab.payment.transaction;

import lombok.Getter;

import static lombok.Lombok.checkNotNull;

@Getter
public class TransactionException extends Exception {
    private final String transactionId;

    public TransactionException(String transactionId, String message) {
        super(message);
        this.transactionId = checkNotNull(transactionId, "transactionId cannot be null");
    }

}
