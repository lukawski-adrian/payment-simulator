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

    public void emitBlocked(TransactionException transactionException) {
        var tr = transactionException.getTransactionRequest();
        var sender = paymentAccountRepository.findByName(tr.senderId()).orElse(null);
        var recipient = paymentAccountRepository.findByName(tr.senderId()).orElse(null);

        emitTransactionEvent(tr, BLOCKED, sender, recipient);
    }

    private void emitTransactionEvent(TransactionRequest tr, PaymentTransactionType type) throws PaymentAccountNotFoundException {
        var sender = getPaymentAccount(tr.senderId());
        var recipient = getPaymentAccount(tr.recipientId());

        emitTransactionEvent(tr, type, sender, recipient);
    }

    private void emitTransactionEvent(TransactionRequest tr, PaymentTransactionType type, PaymentAccount sender, PaymentAccount recipient) {
        var transactionEvent = new PaymentTransaction()
                .setTransactionType(type)
                .setTransactionId(tr.transactionId())
                .setSender(sender)
                .setRecipient(recipient)
                .setCreatedOn(clock.now());

        paymentTransactionRepository.save(transactionEvent);
    }

    public PaymentAccount getPaymentAccount(String externalAccountId) throws PaymentAccountNotFoundException {
        return paymentAccountRepository.findByName(externalAccountId)
                .orElseThrow(() -> new PaymentAccountNotFoundException(externalAccountId));
    }
}
