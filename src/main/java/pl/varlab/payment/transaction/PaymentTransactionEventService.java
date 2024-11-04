package pl.varlab.payment.transaction;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.varlab.payment.Clock;
import pl.varlab.payment.account.*;
import pl.varlab.payment.guard.FraudDetectedException;

import static pl.varlab.payment.transaction.TransactionType.DEPOSIT;
import static pl.varlab.payment.transaction.TransactionType.WITHDRAW;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
// TODO: tests
public class PaymentTransactionEventService {
    private final PaymentTransactionEventGuard paymentTransactionEventGuard;
    private final PaymentTransactionEventRepository paymentTransactionEventRepository;
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

    private void publishIfNeeded(PaymentTransactionEvent newEvent) {
        var transactionId = newEvent.getTransactionId();
        var transactionType = newEvent.getTransactionType();
        var amount = newEvent.getAmount();

        if (paymentTransactionEventRepository.existsByTransactionIdAndTransactionTypeAndAmount(transactionId, transactionType, amount)) {
            log.info("Event already exists skip publishing [{}, {}]", transactionId, transactionType);
        } else {
            log.info("Publishing transaction event [{}, {}]", transactionId, transactionType);
            paymentTransactionEventRepository.save(newEvent);
        }
    }


    private PaymentTransactionEvent getWithdrawTransactionEvent(TransactionRequest tr) throws PaymentAccountNotFoundException {
        var sender = getPaymentAccount(tr.senderId());

        return new PaymentTransactionEvent()
                .setTransactionType(WITHDRAW)
                .setTransactionId(tr.transactionId())
                .setAmount(tr.amount().negate())
                .setAccount(sender)
                .setCreatedOn(clock.now());
    }

    private PaymentTransactionEvent getDepositTransactionEvent(TransactionRequest tr) throws PaymentAccountNotFoundException {
        var recipient = getPaymentAccount(tr.recipientId());

        return new PaymentTransactionEvent()
                .setTransactionType(DEPOSIT)
                .setTransactionId(tr.transactionId())
                .setAccount(recipient)
                .setAmount(tr.amount())
                .setCreatedOn(clock.now());
    }

    public PaymentAccount getPaymentAccount(String externalAccountId) throws PaymentAccountNotFoundException {
        return paymentAccountRepository.findByName(externalAccountId)
                .orElseThrow(() -> new PaymentAccountNotFoundException(externalAccountId));
    }
}
