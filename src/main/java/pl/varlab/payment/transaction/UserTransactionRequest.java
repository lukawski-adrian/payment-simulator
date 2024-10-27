package pl.varlab.payment.transaction;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasLength;

public record UserTransactionRequest(String senderId, String recipientId, BigDecimal amount) {

    public UserTransactionRequest {
        if (!hasLength(senderId))
            throw new IllegalArgumentException("SenderId cannot be empty");

        if (!hasLength(recipientId))
            throw new IllegalArgumentException("RecipientId cannot be empty");

        if (amount == null)
            throw new IllegalArgumentException("Amount cannot be empty");
    }

    public TransactionRequest toTransactionRequest() {
        return new TransactionRequest(UUID.randomUUID(), senderId, recipientId, amount);
    }

}
