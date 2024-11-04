package pl.varlab.payment.transfer;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.guard.FraudDetectedException;
import pl.varlab.payment.transaction.TransactionRequest;
import pl.varlab.payment.transaction.TransferType;

import static java.math.BigDecimal.ZERO;
import static pl.varlab.payment.transaction.TransferType.DEPOSIT;
import static pl.varlab.payment.transaction.TransferType.WITHDRAW;

@Component
@AllArgsConstructor
public class MoneyTransferGuard {
    private final MoneyTransferRepository moneyTransferRepository;

    public void assertAvailableFunds(TransactionRequest tr) throws InsufficientFundsException {
        var senderId = tr.senderId();
        var amount = tr.amount();

        if (moneyTransferRepository.getAvailableFunds(senderId)
                .filter(funds -> ZERO.compareTo(funds.subtract(amount)) <= 0)
                .isPresent())
            return;

        throw new InsufficientFundsException();
    }

    public void assertCorrespondingWithdraw(TransactionRequest tr) throws FraudDetectedException {
        var transactionId = tr.transactionId();
        var amount = tr.amount();

        if (moneyTransferRepository.existsByTransactionIdAndTransactionTypeAndAmount(transactionId, WITHDRAW, amount.negate()))
            return;

        throw new FraudDetectedException(tr, STR."No corresponding WITHDRAW transaction found for \{transactionId}");
    }

    public void assertConsistentWithdraw(TransactionRequest tr) throws FraudDetectedException {
        assertConsistentTransaction(tr, WITHDRAW);
    }

    public void assertConsistentDeposit(TransactionRequest tr) throws FraudDetectedException {
        assertConsistentTransaction(tr, DEPOSIT);
    }

    private void assertConsistentTransaction(TransactionRequest tr, TransferType transferType) throws FraudDetectedException {
        var transactionId = tr.transactionId();
        var amount = WITHDRAW == transferType ? tr.amount().negate() : tr.amount();

        var inconsistentTransactionExists = moneyTransferRepository.findByTransactionIdAndTransactionType(transactionId, transferType)
                .filter(e -> e.getAmount().compareTo(amount) != 0)
                .isPresent();

        if (inconsistentTransactionExists)
            throw new FraudDetectedException(tr, STR."Inconsistent \{transferType} transaction found for \{transactionId}");
    }


}
