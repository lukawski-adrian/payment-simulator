package pl.varlab.payment.transaction;

import lombok.Getter;

import static java.util.Objects.requireNonNull;


@Getter
public class PaymentTransactionException extends Exception {
    private final TransactionRequest transactionRequest;

    public PaymentTransactionException(TransactionRequest transactionRequest, String message) {
        super(message);
        this.transactionRequest = requireNonNull(transactionRequest, "transaction request cannot be null");
    }

}
