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
import pl.varlab.payment.guards.ComplianceGuard;
import pl.varlab.payment.guards.FraudDetectionException;
import pl.varlab.payment.guards.FraudDetectionGuard;

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
        log.info("Processing payment request: {}", transactionRequest);

        try {
            accountService.withdrawal(transactionRequest);
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

        waitForResults(fraudDetectionResult, complianceResult);

        handleFraudDetectionResult(transactionRequest, fraudDetectionResult);
        handleComplianceVerificationResult(transactionRequest, complianceResult);
    }

    private static void waitForResults(CompletableFuture<Void> fraudDetectionResult, CompletableFuture<Void> complianceResult) {
        try {
            // TODO: wrap and retry in case of exception, move to other queue after retry
            CompletableFuture.allOf(fraudDetectionResult, complianceResult).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Error occurred while processing payment", e);
            throw new RuntimeException(e);
        }
    }

    private static void handleFraudDetectionResult(TransactionRequest transactionRequest, CompletableFuture<Void> fraudDetectionResult) {
        try {
            fraudDetectionResult.get();
        } catch (InterruptedException e) {
            log.error("Fraud detection thread has been interrupted {}", transactionRequest);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof FraudDetectionException) {
                // TODO: handle fraud detection in special manner
                log.error("Fraud has been detected! {}", transactionRequest);
            } else {
                log.error("Error occurred while processing fraud detection result", e);
                throw new RuntimeException(e.getCause());
            }
        }
    }

    private static void handleComplianceVerificationResult(TransactionRequest transactionRequest, CompletableFuture<Void> complianceVerficationResult) {
        try {
            complianceVerficationResult.get();
        } catch (InterruptedException e) {
            log.error("Compliance verification thread has been interrupted {}", transactionRequest);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof FraudDetectionException) {
                // TODO: handle fraud detection in special manner
                log.error("Compliance verification failed! {}", transactionRequest);
            } else {
                log.error("Error occurred while processing compliance verification result", e);
                throw new RuntimeException(e.getCause());
            }
        }
    }
}
