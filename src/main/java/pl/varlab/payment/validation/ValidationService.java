package pl.varlab.payment.validation;

import org.springframework.stereotype.Service;
import pl.varlab.payment.transaction.TransactionRequest;

@Service
public class ValidationService {
    public void validateInputRequest(TransactionRequest transactionRequest) throws ValidationException {
        // TODO: impl:, accounts exists, positive amount, available funds, not found transaction, generated uuid transaction
    }
}
