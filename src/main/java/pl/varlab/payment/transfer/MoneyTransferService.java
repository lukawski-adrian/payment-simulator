package pl.varlab.payment.transfer;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.varlab.payment.Clock;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.account.PaymentAccount;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.account.PaymentAccountRepository;
import pl.varlab.payment.transaction.guard.FraudDetectedException;
import pl.varlab.payment.transaction.PaymentTransactionService;
import pl.varlab.payment.transaction.TransactionRequest;

import static pl.varlab.payment.transfer.TransferType.DEPOSIT;
import static pl.varlab.payment.transfer.TransferType.WITHDRAW;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
// TODO: tests
public class MoneyTransferService {
    private final MoneyTransferGuard paymentTransactionEventGuard;
    private final MoneyTransferRepository moneyTransferRepository;
    private final PaymentTransactionService paymentTransactionService;
    private final PaymentAccountRepository paymentAccountRepository;
    private final Clock clock;

    public void withdraw(TransactionRequest transactionRequest) throws FraudDetectedException {
        try {
            paymentTransactionEventGuard.assertAvailableFunds(transactionRequest);
            paymentTransactionEventGuard.assertConsistentWithdraw(transactionRequest);
            var newWithdrawEvent = getWithdrawTransactionEvent(transactionRequest);
            publishIfNeeded(newWithdrawEvent);
            paymentTransactionService.emitStarted(transactionRequest);
        } catch (InsufficientFundsException | PaymentAccountNotFoundException e) {
            log.warn("Suspicious transaction data during withdraw: {}", transactionRequest);
            throw new FraudDetectedException(transactionRequest, e.getMessage());
        }
    }

    public void deposit(TransactionRequest transactionRequest) throws FraudDetectedException {
        try {
            paymentTransactionEventGuard.assertCorrespondingWithdraw(transactionRequest);
            paymentTransactionEventGuard.assertConsistentDeposit(transactionRequest);
            var newDepositEvent = getDepositTransactionEvent(transactionRequest);
            publishIfNeeded(newDepositEvent);
            paymentTransactionService.emitSettled(transactionRequest);
        } catch (PaymentAccountNotFoundException e) {
            log.warn("Suspicious transaction data during deposit: {}", transactionRequest);
            throw new FraudDetectedException(transactionRequest, e.getMessage());
        }
    }

    private void publishIfNeeded(MoneyTransfer newEvent) {
        var transactionId = newEvent.getTransactionId();
        var transactionType = newEvent.getTransferType();
        var amount = newEvent.getAmount();

        if (moneyTransferRepository.existsByTransactionIdAndTransferTypeAndAmount(transactionId, transactionType, amount)) {
            log.info("Event already exists skip publishing [{}, {}]", transactionId, transactionType);
        } else {
            log.info("Publishing money transfer [{}, {}]", transactionId, transactionType);
            moneyTransferRepository.save(newEvent);
        }
    }


    private MoneyTransfer getWithdrawTransactionEvent(TransactionRequest tr) throws PaymentAccountNotFoundException {
        var sender = getPaymentAccount(tr.senderAccountNumber());

        return new MoneyTransfer()
                .setTransferType(WITHDRAW)
                .setTransactionId(tr.transactionId())
                .setAmount(tr.amount().negate())
                .setAccount(sender)
                .setCreatedOn(clock.now());
    }

    private MoneyTransfer getDepositTransactionEvent(TransactionRequest tr) throws PaymentAccountNotFoundException {
        var recipient = getPaymentAccount(tr.recipientAccountNumber());

        return new MoneyTransfer()
                .setTransferType(DEPOSIT)
                .setTransactionId(tr.transactionId())
                .setAccount(recipient)
                .setAmount(tr.amount())
                .setCreatedOn(clock.now());
    }

    public PaymentAccount getPaymentAccount(String accountNumber) throws PaymentAccountNotFoundException {
        return paymentAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new PaymentAccountNotFoundException(accountNumber));
    }
}
