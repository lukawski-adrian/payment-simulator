package pl.varlab.payment.transaction;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentFallbackService {

    // TODO: dead-letter queue
    public void reportTransactionProcessFailure(TransactionRequest transactionRequest, Exception cause) {
        log.error("Unexpected error when try to report transaction {}", transactionRequest);
        log.error("Error:", cause);
    }
}
