package pl.varlab.payment.transaction;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasLength;

public record TransactionRequest(UUID transactionId, String senderId, String recipientId, BigDecimal amount) {

    public TransactionRequest {
        if (transactionId == null)
            throw new IllegalArgumentException("TransactionId cannot be empty");

        if (!hasLength(senderId))
            throw new IllegalArgumentException("SenderId cannot be empty");

        if (!hasLength(recipientId))
            throw new IllegalArgumentException("RecipientId cannot be empty");

        if(senderId.equals(recipientId))
            throw new IllegalArgumentException("RecipientId and SenderId cannot be the same");

        if (amount == null)
            throw new IllegalArgumentException("Amount cannot be empty");

        if (BigDecimal.ZERO.compareTo(amount) >= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");
    }

}
