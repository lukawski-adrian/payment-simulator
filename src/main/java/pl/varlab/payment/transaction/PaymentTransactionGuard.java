package pl.varlab.payment.transaction;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import pl.varlab.payment.transaction.guard.NonCompliantTransactionException;

import static pl.varlab.payment.transaction.PaymentTransactionType.BLOCKED;

@Component
@AllArgsConstructor
// TODO: tests
public class PaymentTransactionGuard {

    private final PaymentTransactionRepository paymentTransactionRepository;

    public void assertProcessableTransaction(TransactionRequest tr) throws NonCompliantTransactionException {
        if (paymentTransactionRepository.existsByTransactionIdAndTransactionType(tr.transactionId(), BLOCKED))
            throw new NonCompliantTransactionException(tr, "Transaction already blocked");
    }
}
