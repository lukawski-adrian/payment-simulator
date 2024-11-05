package pl.varlab.payment.transaction.guard;

import pl.varlab.payment.transaction.TransactionRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Common interface for all transaction verification guards. Read
 * <a href="https://github.com/lukawski-adrian/payment-simulator?tab=readme-ov-file#simplified-payment-flow-overview">more</a>.
 */
public interface TransactionGuard {
    CompletableFuture<Void> assertTransaction(TransactionRequest transactionRequest);
}
