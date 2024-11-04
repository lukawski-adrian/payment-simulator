package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.common.ConflictDataException;
import pl.varlab.payment.guard.ComplianceGuard;
import pl.varlab.payment.guard.FraudDetectedException;
import pl.varlab.payment.guard.FraudDetectionGuard;
import pl.varlab.payment.transaction.PaymentTransactionService;
import pl.varlab.payment.transaction.TransactionBlocker;
import pl.varlab.payment.transaction.TransactionException;
import pl.varlab.payment.transaction.TransactionRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@AllArgsConstructor
public final class GuardTransactionHandler extends BaseTransactionHandler {

    private final FraudDetectionGuard fraudDetectionGuard;
    private final ComplianceGuard complianceGuard;
    private final TransactionBlocker transactionBlocker;
    private final PaymentTransactionService paymentTransactionService;


    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            verifyTransaction(transactionRequest);
            paymentTransactionService.emitVerified(transactionRequest);
            super.handle(transactionRequest);
        } catch (ExecutionException e) {
            var cause = e.getCause();
            if (cause instanceof TransactionException) {
                transactionBlocker.blockTransaction((TransactionException) cause);
                throw new ConflictDataException(cause.getMessage());
            }
            throw new RuntimeException("Unexpected execution error during transaction verification", cause);
        } catch (PaymentAccountNotFoundException e) {
            log.error("Payment account not found during transaction verification {}", transactionRequest);
            transactionBlocker.blockTransaction(new FraudDetectedException(transactionRequest, e.getMessage()));
            throw new ConflictDataException(e.getMessage());
        }
    }

    private void verifyTransaction(TransactionRequest transactionRequest) throws ExecutionException {
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
