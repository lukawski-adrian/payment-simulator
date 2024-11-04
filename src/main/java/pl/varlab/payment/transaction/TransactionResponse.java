package pl.varlab.payment.transaction;

import java.util.UUID;

public record TransactionResponse(UUID transactionId) {

    public TransactionResponse {
        if (transactionId == null)
            throw new IllegalArgumentException("TransactionId cannot be empty");
    }


}
