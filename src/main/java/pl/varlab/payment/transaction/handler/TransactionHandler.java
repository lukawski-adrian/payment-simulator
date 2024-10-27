package pl.varlab.payment.transaction.handler;

import pl.varlab.payment.transaction.TransactionRequest;

public interface TransactionHandler {
    void handle(TransactionRequest transactionRequest);
}
