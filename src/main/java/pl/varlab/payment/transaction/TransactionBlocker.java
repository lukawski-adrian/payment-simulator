package pl.varlab.payment.transaction;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
// TODO: provide mock implementation and tests
public class TransactionBlocker {

    private final PaymentTransactionService paymentTransactionService;

    public void blockTransaction(TransactionException te) {
        log.warn("Block suspicious transaction: {}", te.getTransactionRequest());
        log.warn("Transaction exception: {}", te.getMessage());
        paymentTransactionService.emitBlocked(te);
    }
}
