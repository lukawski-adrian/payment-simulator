package pl.varlab.payment.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionFallbackService {

    // TODO: think about mock implementation
    public void reportTransactionProcessFailure(TransactionRequest transactionRequest) {
        log.info("Transaction request received: {}", transactionRequest);
    }
}
