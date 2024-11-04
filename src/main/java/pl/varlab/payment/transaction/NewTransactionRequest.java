package pl.varlab.payment.transaction;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasLength;

public record NewTransactionRequest(String senderAccountNumber, String recipientAccountNumber, BigDecimal amount) {

    private static final int MAX_AMOUNT_SCALE = 2;

    public NewTransactionRequest {
        if (!hasLength(senderAccountNumber))
            throw new IllegalArgumentException("SenderAccountNumber cannot be empty");

        if (!hasLength(recipientAccountNumber))
            throw new IllegalArgumentException("RecipientAccountNumber cannot be empty");

        if (senderAccountNumber.equals(recipientAccountNumber))
            throw new IllegalArgumentException("RecipientAccountNumber and SenderAccountNumber cannot be the same");

        if (amount == null)
            throw new IllegalArgumentException("Amount cannot be empty");

        if (BigDecimal.ZERO.compareTo(amount) >= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");

        if (amount.scale() > MAX_AMOUNT_SCALE)
            throw new IllegalArgumentException("Max scale is two digits after comma");
    }

    public TransactionRequest newTransactionId() {
        return new TransactionRequest(UUID.randomUUID(), senderAccountNumber, recipientAccountNumber, amount);
    }

}
