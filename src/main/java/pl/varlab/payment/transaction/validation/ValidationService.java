package pl.varlab.payment.transaction.validation;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.account.PaymentAccountGuard;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.guard.NonCompliantTransactionException;
import pl.varlab.payment.transfer.MoneyTransferGuard;
import pl.varlab.payment.transaction.PaymentTransactionGuard;
import pl.varlab.payment.transaction.PaymentTransactionService;
import pl.varlab.payment.transaction.TransactionRequest;

@AllArgsConstructor
@Service
public class ValidationService {
    private final PaymentTransactionGuard paymentTransactionGuard;
    private final PaymentTransactionService paymentTransactionService;
    private final MoneyTransferGuard paymentTransactionEventGuard;
    private final PaymentAccountGuard paymentAccountGuard;

    // TODO: tests
    @Transactional
    public void validate(TransactionRequest transactionRequest) throws NonCompliantTransactionException, PaymentAccountNotFoundException, InsufficientFundsException {
        // TODO: verify if account is blocked
        paymentTransactionGuard.assertProcessableTransaction(transactionRequest);
        paymentAccountGuard.assertAccountExists(transactionRequest.senderId());
        paymentAccountGuard.assertAccountExists(transactionRequest.recipientId());
        paymentTransactionEventGuard.assertAvailableFunds(transactionRequest);
        paymentTransactionService.emitValidated(transactionRequest);
    }
}
