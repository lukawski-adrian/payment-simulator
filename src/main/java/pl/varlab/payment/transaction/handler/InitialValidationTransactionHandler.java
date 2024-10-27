package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.transaction.TransactionRequest;
import pl.varlab.payment.validation.ValidationException;
import pl.varlab.payment.validation.ValidationService;

@Slf4j
@AllArgsConstructor
public class InitialValidationTransactionHandler extends BaseTransactionHandler {

    private final ValidationService validationService;

    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            // TODO: validate input request, account id, isblocked, 2decimal places,
            validationService.validateInputRequest(transactionRequest);
            super.handle(transactionRequest);
        } catch (ValidationException e) {
            log.error("Invalid transaction request", e);
        }
    }
}
