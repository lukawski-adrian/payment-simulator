package pl.varlab.payment.transaction;

import java.math.BigDecimal;

public record TransactionRequest(String transactionId, String senderId, String recipientId, BigDecimal amount) {
}
