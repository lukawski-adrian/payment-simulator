package pl.varlab.payment.transaction;

import lombok.Getter;

import java.util.UUID;

import static lombok.Lombok.checkNotNull;

@Getter
public class TransactionException extends Exception {
    private final UUID transactionId;

    public TransactionException(UUID transactionId, String message) {
        super(message);
        this.transactionId = checkNotNull(transactionId, "transactionId cannot be null");
    }

}
