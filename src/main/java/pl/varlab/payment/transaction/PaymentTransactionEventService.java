package pl.varlab.payment.transaction;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.varlab.payment.Clock;
import pl.varlab.payment.account.*;

import java.math.BigDecimal;

import static pl.varlab.payment.transaction.TransactionType.*;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
// TODO: tests
public class PaymentTransactionEventService {
    private final PaymentAccountGuard paymentAccountGuard;
    private final PaymentAccountRepository paymentAccountRepository;
    private final PaymentTransactionEventRepository paymentTransactionEventRepository;
    private final Clock clock;

    public void blockTransaction(TransactionException transactionException) throws PaymentAccountNotFoundException {
        var newBlockEvent = getBlockTransactionEvent(transactionException);
        publishIfNeeded(newBlockEvent);
    }

    public void reportTransaction(TransactionRequest transactionRequest, Exception cause) throws PaymentAccountNotFoundException {
        var newReportEvent = getReportTransactionEvent(transactionRequest, cause);
        publishIfNeeded(newReportEvent);
    }

    public void withdraw(TransactionRequest transactionRequest) throws InsufficientFundsException, PaymentAccountNotFoundException {
        paymentAccountGuard.assertAvailableFunds(transactionRequest);
        var newWithdrawEvent = getWithdrawTransactionEvent(transactionRequest);
        publishIfNeeded(newWithdrawEvent);
    }

    public void deposit(TransactionRequest transactionRequest) throws PaymentAccountNotFoundException {
        var newDepositEvent = getDepositTransactionEvent(transactionRequest);
        publishIfNeeded(newDepositEvent);
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

    private PaymentAccount getPaymentAccount(String accountName) throws PaymentAccountNotFoundException {
        return paymentAccountRepository.findByName(accountName)
                .orElseThrow(() -> new PaymentAccountNotFoundException(accountName));
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

    private PaymentTransactionEvent getBlockTransactionEvent(TransactionException transactionException) throws PaymentAccountNotFoundException {
        var tr = transactionException.getTransactionRequest();
        var sender = getPaymentAccount(tr.senderId());

        return new PaymentTransactionEvent()
                .setTransactionType(BLOCK)
                .setTransactionId(tr.transactionId())
                .setAmount(BigDecimal.ZERO)
                .setAccount(sender)
                .setComment(transactionException.getMessage())
                .setCreatedOn(clock.now());
    }

    private PaymentTransactionEvent getReportTransactionEvent(TransactionRequest tr, Exception cause) throws PaymentAccountNotFoundException {
        var sender = getPaymentAccount(tr.senderId());

        return new PaymentTransactionEvent()
                .setTransactionType(REPORT)
                .setTransactionId(tr.transactionId())
                .setAmount(BigDecimal.ZERO)
                .setAccount(sender)
                .setComment(cause.getMessage())
                .setCreatedOn(clock.now());
    }
}
