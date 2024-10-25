package pl.varlab.payment.guards;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.varlab.payment.transaction.TransactionRequest;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class FraudDetectionGuard {

    @Async
    public CompletableFuture<Void> assertNotFraud(TransactionRequest transactionRequest) {
        try {
            log.info("Fraud detection verification: {}", transactionRequest);
            // TODO: what in case of runtime exception?
            Thread.sleep(500);
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
