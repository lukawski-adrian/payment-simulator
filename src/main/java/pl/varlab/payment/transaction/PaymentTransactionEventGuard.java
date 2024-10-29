package pl.varlab.payment.transaction;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.guard.FraudDetectedException;
import pl.varlab.payment.guard.NonCompliantTransactionException;

import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static pl.varlab.payment.transaction.TransactionType.*;

@Component
@AllArgsConstructor
public class PaymentTransactionEventGuard {
    private static final Set<TransactionType> BLOCKED_TRANSACTION_TYPES = Set.of(REPORT, BLOCK);
    private final PaymentTransactionEventRepository paymentTransactionEventRepository;

    public void assertAvailableFunds(TransactionRequest tr) throws InsufficientFundsException {
        var senderId = tr.senderId();
        var amount = tr.amount();

        if (paymentTransactionEventRepository.getAvailableFunds(senderId)
                .filter(funds -> ZERO.compareTo(funds.subtract(amount)) <= 0)
                .isPresent())
            return;

        throw new InsufficientFundsException();
    }

    public void assertCorrespondingWithdraw(TransactionRequest tr) throws FraudDetectedException {
        var transactionId = tr.transactionId();
        var amount = tr.amount();

        if (paymentTransactionEventRepository.existsByTransactionIdAndTransactionTypeAndAmount(transactionId, WITHDRAW, amount.negate()))
            return;

        throw new FraudDetectedException(tr, STR."No corresponding WITHDRAW transaction found for \{transactionId}");
    }

    public void assertConsistentWithdraw(TransactionRequest tr) throws FraudDetectedException {
        var transactionId = tr.transactionId();
        var amount = tr.amount();

        var duplicatedWithdrawExists = paymentTransactionEventRepository.findByTransactionIdAndTransactionType(transactionId, WITHDRAW)
                .filter(e -> e.getAmount().compareTo(amount.negate()) != 0)
                .isPresent();

        if (duplicatedWithdrawExists)
            throw new FraudDetectedException(tr, STR."Inconsistent WITHDRAW transaction found for \{transactionId}");
    }

    public void assertProcessableTransaction(TransactionRequest tr) throws NonCompliantTransactionException {
        if (paymentTransactionEventRepository.existsByTransactionIdAndTransactionTypeIn(tr.transactionId(), BLOCKED_TRANSACTION_TYPES))
            throw new NonCompliantTransactionException(tr, "Transaction already blocked");
    }
}
