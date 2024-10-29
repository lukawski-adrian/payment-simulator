package pl.varlab.payment.transaction;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class TransactionFallbackService {

    private final PaymentTransactionEventService transactionEventService;

    // TODO: CircuitBreaker
    public void reportTransactionProcessFailure(TransactionRequest transactionRequest, Exception cause) {
        try {
            transactionEventService.reportTransaction(transactionRequest, cause);
        } catch (Exception e) {
            log.error("Unexpected error when try to report transaction {}", transactionRequest);
            log.error("Error:", e);
        }
    }
}
