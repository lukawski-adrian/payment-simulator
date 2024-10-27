package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.guard.ComplianceGuard;
import pl.varlab.payment.guard.FraudDetectionGuard;
import pl.varlab.payment.transaction.TransactionBlocker;
import pl.varlab.payment.transaction.TransactionException;
import pl.varlab.payment.transaction.TransactionRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@AllArgsConstructor
public class GuardTransactionHandler extends BaseTransactionHandler {

    private final FraudDetectionGuard fraudDetectionGuard;
    private final ComplianceGuard complianceGuard;
    private final TransactionBlocker transactionBlocker;


    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            verifyTransaction(transactionRequest);
            super.handle(transactionRequest);
        } catch (ExecutionException e) {
            var cause = e.getCause();
            if (cause instanceof TransactionException) {
                transactionBlocker.blockTransaction((TransactionException) cause);
                return;
            }
            throw new RuntimeException("Unexpected execution error during transaction verification", cause);
        }
    }

    private void verifyTransaction(TransactionRequest transactionRequest) throws ExecutionException {
        // TODO: enable virtual threads
        // TODO: consider common transaction guard interface
        var fraudDetectionResult = this.fraudDetectionGuard.assertNotFraud(transactionRequest);
        var complianceResult = this.complianceGuard.assertCompliant(transactionRequest);

        try {
            CompletableFuture.allOf(fraudDetectionResult, complianceResult).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            log.error("Transaction guard interrupted or timeout exceeded", e);
            throw new RuntimeException(e);
        }
    }
}
