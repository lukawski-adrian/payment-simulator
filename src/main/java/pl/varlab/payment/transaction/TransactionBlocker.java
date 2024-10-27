package pl.varlab.payment.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
// TODO: provide mock implementation and tests
public class TransactionBlocker {

    public void blockTransaction(TransactionException te) {
        log.info("Block suspicious transaction {}", te.getTransactionId());
    }
}
