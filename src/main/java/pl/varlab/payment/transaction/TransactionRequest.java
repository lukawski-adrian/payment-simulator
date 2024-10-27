package pl.varlab.payment.transaction;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRequest(UUID transactionId, String senderId, String recipientId, BigDecimal amount) {

    public TransactionRequest newTransactionId() {
        return new TransactionRequest(UUID.randomUUID(), senderId, recipientId, amount);
    }

}
