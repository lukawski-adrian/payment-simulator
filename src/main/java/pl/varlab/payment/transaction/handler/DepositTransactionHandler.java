package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.guard.FraudDetectedException;
import pl.varlab.payment.transaction.PaymentTransactionEventService;
import pl.varlab.payment.transaction.TransactionBlocker;
import pl.varlab.payment.transaction.TransactionRequest;

@Slf4j
@AllArgsConstructor
public final class DepositTransactionHandler extends BaseTransactionHandler {

    private final PaymentTransactionEventService transactionEventService;
    private final TransactionBlocker transactionBlocker;

    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            // TODO: consider again check if blocked
            transactionEventService.deposit(transactionRequest);
            super.handle(transactionRequest);
        } catch (PaymentAccountNotFoundException e) {
            // TODO: consider more sophisticated error handling, verify sender and recipient ids at the beginning?
            // TODO: or recover? refund?
            log.warn("Recipient accountId not found: {}", transactionRequest);
        } catch (FraudDetectedException e) {
            log.warn("Fraud detected during deposit: {}", transactionRequest);
            this.transactionBlocker.blockTransaction(e);
        }
    }
}
