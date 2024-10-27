package pl.varlab.payment.guard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.varlab.payment.AsyncConfig;
import pl.varlab.payment.transaction.TransactionRequest;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class FraudDetectionGuard {

    private static final BigDecimal FRAUD_DIVISOR = BigDecimal.valueOf(11);

    @Async(AsyncConfig.TRANSACTION_GUARDS_THREAD_POOL_TASK_EXECUTOR)
    public CompletableFuture<Void> assertNotFraud(TransactionRequest transactionRequest) {
        try {
            log.info("Fraud detection verification: {}", transactionRequest);
            Thread.sleep(500);
            if (isFraudDetected(transactionRequest.amount())) {
                var fraudException = new FraudDetectedException(transactionRequest, STR."Fraud detected (divisor \{FRAUD_DIVISOR})");
                return CompletableFuture.failedFuture(fraudException);
            } else {
                return CompletableFuture.completedFuture(null);
            }
        } catch (InterruptedException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private boolean isFraudDetected(BigDecimal amount) {
        return amount.remainder(FRAUD_DIVISOR)
                .compareTo(BigDecimal.ZERO) == 0;
    }
}
