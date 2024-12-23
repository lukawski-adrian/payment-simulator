package pl.varlab.payment.transaction;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.varlab.payment.Clock;
import pl.varlab.payment.account.PaymentAccount;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.account.PaymentAccountRepository;

import static pl.varlab.payment.transaction.PaymentTransactionType.*;

@AllArgsConstructor
@Service
@Transactional
// TODO: tests
public class PaymentTransactionService {

    private static final String FALLBACK_ACCOUNT = "FALLBACK_ACCOUNT";
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentAccountRepository paymentAccountRepository;
    private final Clock clock;

    public void emitValidated(TransactionRequest transactionRequest) throws PaymentAccountNotFoundException {
        emitTransactionEvent(transactionRequest, VALIDATED);
    }

    public void emitStarted(TransactionRequest transactionRequest) throws PaymentAccountNotFoundException {
        emitTransactionEvent(transactionRequest, STARTED);
    }

    public void emitVerified(TransactionRequest transactionRequest) throws PaymentAccountNotFoundException {
        emitTransactionEvent(transactionRequest, VERIFIED);
    }

    public void emitSettled(TransactionRequest transactionRequest) throws PaymentAccountNotFoundException {
        emitTransactionEvent(transactionRequest, SETTLED);
    }

    public void emitBlocked(PaymentTransactionException transactionException) {
        var tr = transactionException.getTransactionRequest();
        var sender = getPaymentAccountOrFallback(tr.senderAccountNumber());
        var recipient = getPaymentAccountOrFallback(tr.recipientAccountNumber());

        var transactionEvent = new PaymentTransaction()
                .setTransactionType(BLOCKED)
                .setTransactionId(tr.transactionId())
                .setSender(sender)
                .setRecipient(recipient)
                .setAmount(tr.amount())
                .setComment(transactionException.getMessage())
                .setCreatedOn(clock.now());

        paymentTransactionRepository.save(transactionEvent);
    }

    private void emitTransactionEvent(TransactionRequest tr, PaymentTransactionType type) throws PaymentAccountNotFoundException {
        var sender = getPaymentAccount(tr.senderAccountNumber());
        var recipient = getPaymentAccount(tr.recipientAccountNumber());

        var transactionEvent = new PaymentTransaction()
                .setTransactionType(type)
                .setTransactionId(tr.transactionId())
                .setSender(sender)
                .setRecipient(recipient)
                .setAmount(tr.amount())
                .setCreatedOn(clock.now());

        paymentTransactionRepository.save(transactionEvent);
    }

    public PaymentAccount getPaymentAccount(String accountNumber) throws PaymentAccountNotFoundException {
        return paymentAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new PaymentAccountNotFoundException(accountNumber));
    }

    public PaymentAccount getPaymentAccountOrFallback(String accountNumber) {
        return paymentAccountRepository.findByAccountNumber(accountNumber)
                .or(() -> paymentAccountRepository.findByAccountNumber(FALLBACK_ACCOUNT))
                .orElseThrow(() -> new RuntimeException(accountNumber));
    }
}
