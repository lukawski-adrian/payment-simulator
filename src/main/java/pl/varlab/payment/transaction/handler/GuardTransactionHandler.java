package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.common.ConflictDataException;
import pl.varlab.payment.transaction.PaymentTransactionBlocker;
import pl.varlab.payment.transaction.PaymentTransactionException;
import pl.varlab.payment.transaction.PaymentTransactionService;
import pl.varlab.payment.transaction.TransactionRequest;
import pl.varlab.payment.transaction.guard.FraudDetectedException;
import pl.varlab.payment.transaction.guard.TransactionGuard;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@AllArgsConstructor
public final class GuardTransactionHandler extends BaseTransactionHandler {

    private final List<TransactionGuard> transactionGuards;
    private final PaymentTransactionBlocker transactionBlocker;
    private final PaymentTransactionService paymentTransactionService;


    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            verifyTransaction(transactionRequest);
            paymentTransactionService.emitVerified(transactionRequest);
            super.handle(transactionRequest);
        } catch (ExecutionException e) {
            var cause = e.getCause();
            if (cause instanceof PaymentTransactionException) {
                transactionBlocker.blockTransaction((PaymentTransactionException) cause);
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
        var verificationResults = transactionGuards.stream()
                .map(g -> g.assertTransaction(transactionRequest))
                .toArray(CompletableFuture[]::new);

        try {
            CompletableFuture.allOf(verificationResults).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            log.error("Transaction guard interrupted or timeout exceeded", e);
            throw new RuntimeException(e);
        }
    }
}
