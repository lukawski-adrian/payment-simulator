package pl.varlab.payment.transaction;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.common.ConflictDataException;

@Component
@Slf4j
@AllArgsConstructor
// TODO: provide mock implementation and tests
public class TransactionBlocker {

    private final PaymentTransactionEventService transactionEventService;

    public void blockTransaction(TransactionException te) {
        try {
            log.info("Block suspicious transaction {}", te.getTransactionRequest().transactionId());
            transactionEventService.blockTransaction(te);
        } catch (PaymentAccountNotFoundException e) {
            throw new ConflictDataException(e.getMessage());
        }
    }
}
