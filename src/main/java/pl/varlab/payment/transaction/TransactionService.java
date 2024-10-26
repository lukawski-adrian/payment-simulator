package pl.varlab.payment.transaction;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.varlab.payment.account.AccountNotFoundException;
import pl.varlab.payment.account.AccountService;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.guard.ComplianceGuard;
import pl.varlab.payment.guard.FraudDetectionException;
import pl.varlab.payment.guard.FraudDetectionGuard;

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

    @Async
    // TODO: tests for retryable
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 4, backoff = @Backoff(delay = 1000))
    public void processTransaction(TransactionRequest transactionRequest) {
        log.info("Processing transaction request: {}", transactionRequest);

        try {
            accountService.withdraw(transactionRequest);
        } catch (InsufficientFundsException e) {
            // TODO: consider more sophisticated error handling
            log.info("Insufficient funds on sender account for payment request: {}", transactionRequest);
            return;
        } catch (AccountNotFoundException e) {
            log.info("Sender accountId not found: {}", transactionRequest);
            return;
        }

        verificationChecks(transactionRequest);

        try {
            accountService.deposit(transactionRequest);
        } catch (AccountNotFoundException e) {
            // TODO: consider more sophisticated error handling, verify sender and recipient ids at the beginning?
            log.info("Recipient accountId not found: {}", transactionRequest);
        }
    }

    private void verificationChecks(TransactionRequest transactionRequest) {
        // TODO: consider multiple concurrent transactions
        // TODO: enable virtual threads
        // TODO: fees
        var fraudDetectionResult = this.fraudDetectionGuard.assertNotFraud(transactionRequest);
        var complianceResult = this.complianceGuard.assertCompliant(transactionRequest);

        try {
            // TODO: wrap and retry in case of exception, move to other queue after retry
            CompletableFuture.allOf(fraudDetectionResult, complianceResult).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            // TODO: consider case when one of the guards throws exception then should all verifications should be retried?
            log.error("Error occurred while processing payment", e);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            switch (e.getCause()) {
                case FraudDetectionException fe -> log.info("Report fraud: {}", fe.getMessage());
                default -> log.error("Unexpected execution error", e);
            }
        }
    }

}
