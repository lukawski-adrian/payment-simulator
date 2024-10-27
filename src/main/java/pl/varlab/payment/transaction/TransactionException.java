package pl.varlab.payment.transaction;

import lombok.Getter;

import static com.google.common.base.Preconditions.checkNotNull;


@Getter
public class TransactionException extends Exception {
    private final TransactionRequest transactionRequest;

    public TransactionException(TransactionRequest transactionRequest, String message) {
        super(message);
        this.transactionRequest = checkNotNull(transactionRequest, "transaction request cannot be null");
    }

}
