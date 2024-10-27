package pl.varlab.payment.account;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.varlab.payment.Clock;
import pl.varlab.payment.transaction.PaymentTransactionEvent;
import pl.varlab.payment.transaction.PaymentTransactionEventRepository;
import pl.varlab.payment.transaction.TransactionRequest;

import static pl.varlab.payment.transaction.TransactionType.DEPOSIT;
import static pl.varlab.payment.transaction.TransactionType.WITHDRAW;

// TODO: tests
@Service
@AllArgsConstructor
@Transactional
public class PaymentAccountService {

    private final PaymentAccountGuard paymentAccountGuard;
    private final PaymentAccountRepository paymentAccountRepository;
    private final PaymentTransactionEventRepository paymentTransactionEventRepository;
    private final Clock clock;

    public void withdraw(TransactionRequest transactionRequest) throws InsufficientFundsException, PaymentAccountNotFoundException {
        // TODO: idempotence
        paymentAccountGuard.assertAvailableFunds(transactionRequest);
        var newTransactionEvent = getWithdrawTransactionEvent(transactionRequest);
        paymentTransactionEventRepository.save(newTransactionEvent);
    }

    public void deposit(TransactionRequest transactionRequest) throws PaymentAccountNotFoundException {
        // TODO: idempotence
        var newTransactionEvent = getDepositTransactionEvent(transactionRequest);
        paymentTransactionEventRepository.save(newTransactionEvent);
    }

    private PaymentTransactionEvent getWithdrawTransactionEvent(TransactionRequest tr) throws PaymentAccountNotFoundException {
        var sender = paymentAccountRepository.findByName(tr.senderId())
                .orElseThrow(() -> new PaymentAccountNotFoundException(tr.senderId()));

        return new PaymentTransactionEvent()
                .setTransactionType(WITHDRAW)
                .setTransactionId(tr.transactionId())
                .setAmount(tr.amount().negate())
                .setAccount(sender)
                .setCreatedOn(clock.now());
    }

    private PaymentTransactionEvent getDepositTransactionEvent(TransactionRequest tr) throws PaymentAccountNotFoundException {
        var recipient = paymentAccountRepository.findByName(tr.recipientId())
                .orElseThrow(() -> new PaymentAccountNotFoundException(tr.recipientId()));

        return new PaymentTransactionEvent()
                .setTransactionType(DEPOSIT)
                .setTransactionId(tr.transactionId())
                .setAccount(recipient)
                .setAmount(tr.amount())
                .setCreatedOn(clock.now());
    }


}