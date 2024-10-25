package pl.varlab.payment.guards;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.varlab.payment.PaymentRequest;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ComplianceGuard {

    @Async
    public CompletableFuture<Void> assertCompliant(PaymentRequest paymentRequest) {
        try {
            // TODO: compliance blocked user case
            log.info("Compliance verification: {}", paymentRequest);
            Thread.sleep(500);
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
