package pl.varlab.payment.transaction.handler;

import lombok.Setter;
import pl.varlab.payment.transaction.TransactionRequest;


@Setter
sealed class BaseTransactionHandler implements TransactionHandler permits InitialValidationTransactionHandler, WithdrawTransactionHandler,
GuardTransactionHandler, DepositTransactionHandler{

    private TransactionHandler handler;

    @Override
    public void handle(TransactionRequest transactionRequest) {
        if (handler != null)
            handler.handle(transactionRequest);
    }
}
