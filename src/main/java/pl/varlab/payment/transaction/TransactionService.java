package pl.varlab.payment.transaction;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.varlab.payment.AsyncConfig;
import pl.varlab.payment.transaction.handler.TransactionHandler;

@Service
@Slf4j
@AllArgsConstructor
public class TransactionService {

    private final TransactionHandler transactionHandler;
    private final TransactionFallbackService fallbackService;

    // TODO: tests for @Retry, @Async
    @Async(AsyncConfig.TRANSACTION_PROCESSORS_THREAD_POOL_TASK_EXECUTOR)
    @Retry(name = "transaction-service", fallbackMethod = "fallback")
    public void processTransaction(TransactionRequest transactionRequest) {
        log.info("Processing transaction request: {}", transactionRequest);
        transactionHandler.handle(transactionRequest);
    }


    private void fallback(TransactionRequest transactionRequest, Exception exception) {
        log.error("Unexpected error when try to process transaction {}", transactionRequest);
        log.error("Error:", exception);

        log.info("Redirecting to fallback service {}", transactionRequest);
        fallbackService.reportTransactionProcessFailure(transactionRequest);
    }

}
