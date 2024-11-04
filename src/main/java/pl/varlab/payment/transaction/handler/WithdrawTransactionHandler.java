package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.common.ConflictDataException;
import pl.varlab.payment.guard.FraudDetectedException;
import pl.varlab.payment.transfer.MoneyTransferService;
import pl.varlab.payment.transaction.TransactionBlocker;
import pl.varlab.payment.transaction.TransactionRequest;

@Slf4j
@AllArgsConstructor
public final class WithdrawTransactionHandler extends BaseTransactionHandler {

    private final MoneyTransferService transactionEventService;
    private final TransactionBlocker transactionBlocker;

    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            transactionEventService.withdraw(transactionRequest);
            super.handle(transactionRequest);
        } catch (FraudDetectedException e) {
            log.warn("Fraud detected during withdraw: {}", transactionRequest);
            this.transactionBlocker.blockTransaction(e);
            throw new ConflictDataException(e.getMessage());
        }
    }
}
