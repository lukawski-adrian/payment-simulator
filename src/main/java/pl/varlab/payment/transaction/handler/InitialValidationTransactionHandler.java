package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.account.PaymentAccountGuard;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.common.ConflictDataException;
import pl.varlab.payment.common.NotFoundException;
import pl.varlab.payment.common.ValidationException;
import pl.varlab.payment.guard.NonCompliantTransactionException;
import pl.varlab.payment.transaction.PaymentTransactionEventGuard;
import pl.varlab.payment.transaction.TransactionRequest;

@Slf4j
@AllArgsConstructor
public final class InitialValidationTransactionHandler extends BaseTransactionHandler {

    private final PaymentTransactionEventGuard paymentTransactionEventGuard;
    private final PaymentAccountGuard paymentAccountGuard;

    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            // TODO: verify if account is blocked
            paymentTransactionEventGuard.assertProcessableTransaction(transactionRequest);
            paymentAccountGuard.assertAccountExists(transactionRequest.senderId());
            paymentAccountGuard.assertAccountExists(transactionRequest.recipientId());
            paymentTransactionEventGuard.assertAvailableFunds(transactionRequest);
            super.handle(transactionRequest);
        } catch (InsufficientFundsException e) {
            throw new ValidationException(e.getMessage());
        } catch (NonCompliantTransactionException e) {
            throw new ConflictDataException(e.getMessage());
        } catch (PaymentAccountNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}
