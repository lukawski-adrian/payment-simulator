package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.common.ConflictDataException;
import pl.varlab.payment.common.NotFoundException;
import pl.varlab.payment.common.ValidationException;
import pl.varlab.payment.transaction.guard.NonCompliantTransactionException;
import pl.varlab.payment.transaction.TransactionRequest;
import pl.varlab.payment.transaction.validation.ValidationService;

@Slf4j
@AllArgsConstructor
public final class InitialValidationTransactionHandler extends BaseTransactionHandler {

    private final ValidationService validationService;

    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            validationService.validate(transactionRequest);
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
