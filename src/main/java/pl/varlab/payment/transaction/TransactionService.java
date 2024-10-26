package pl.varlab.payment.transaction;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.varlab.payment.AsyncConfig;
import pl.varlab.payment.account.AccountNotFoundException;
import pl.varlab.payment.account.AccountService;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.guard.ComplianceGuard;
import pl.varlab.payment.guard.FraudDetectionGuard;
import pl.varlab.payment.guard.TransactionException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@AllArgsConstructor
public class TransactionService {

    private final FraudDetectionGuard fraudDetectionGuard;
    private final ComplianceGuard complianceGuard;
    private final AccountService accountService;
    private final TransactionBlocker transactionBlocker;
    private final TransactionFallbackService fallbackService;

    // TODO: tests for @Retry, @Async
    @Async(AsyncConfig.TRANSACTION_PROCESSORS_THREAD_POOL_TASK_EXECUTOR)
    @Retry(name = "transaction-service", fallbackMethod = "fallback")
    public void processTransaction(TransactionRequest transactionRequest) {
        // TODO: validate input request
        log.info("Processing transaction request: {}", transactionRequest);

        try {
            accountService.withdraw(transactionRequest);
        } catch (InsufficientFundsException e) {
            log.warn("Insufficient funds on sender account for payment request: {}", transactionRequest);
            return;
        } catch (AccountNotFoundException e) {
            log.warn("Sender accountId not found: {}", transactionRequest);
            return;
        }

        try {
            verifyTransaction(transactionRequest);
        } catch (ExecutionException e) {
            var cause = e.getCause();

            if (cause instanceof TransactionException) {
                transactionBlocker.blockTransaction((TransactionException) cause);
                return;
            }

            throw new RuntimeException("Unexpected execution error during transaction verification", cause);
        }

        try {
            accountService.deposit(transactionRequest);
        } catch (AccountNotFoundException e) {
            // TODO: consider more sophisticated error handling, verify sender and recipient ids at the beginning?
            // TODO: or recover? refund?
            log.warn("Recipient accountId not found: {}", transactionRequest);
        }
    }

    private void verifyTransaction(TransactionRequest transactionRequest) throws ExecutionException {
        // TODO: consider multiple concurrent transactions
        // TODO: enable virtual threads
        // TODO: fees
        // TODO: consider common transaction guard interface
        var fraudDetectionResult = this.fraudDetectionGuard.assertNotFraud(transactionRequest);
        var complianceResult = this.complianceGuard.assertCompliant(transactionRequest);

        try {
            // TODO: wrap and retry in case of exception, move to other queue after retry (specific exception type?)
            CompletableFuture.allOf(fraudDetectionResult, complianceResult).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            log.error("Transaction guard interrupted or timeout exceeded", e);
            throw new RuntimeException(e);
        }
    }

    private void fallback(TransactionRequest transactionRequest, Exception exception) {
        log.error("Unexpected error when try to process transaction {}", transactionRequest);
        log.error("Error:", exception);

        log.info("Redirecting to fallback service {}", transactionRequest);
        fallbackService.reportTransactionProcessFailure(transactionRequest);
    }

}
