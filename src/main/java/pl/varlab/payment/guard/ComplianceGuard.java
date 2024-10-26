package pl.varlab.payment.guard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.varlab.payment.AsyncConfig;
import pl.varlab.payment.transaction.TransactionRequest;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ComplianceGuard {

    @Async(AsyncConfig.TRANSACTION_GUARDS_THREAD_POOL_TASK_EXECUTOR)
    public CompletableFuture<Void> assertCompliant(TransactionRequest transactionRequest) {
        try {
            // TODO: compliance blocked user case
            log.info("Compliance verification: {}", transactionRequest);
            Thread.sleep(500);
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
