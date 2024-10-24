package pl.varlab.payment;

public record PaymentRequest(String transactionId, String senderId, String recipientId, Double amount) {
}
