package pl.varlab.payment.transaction;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.guard.FraudDetectedException;

import static java.math.BigDecimal.ZERO;
import static pl.varlab.payment.transaction.TransactionType.WITHDRAW;

@Component
@AllArgsConstructor
public class PaymentTransactionEventGuard {

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
}
