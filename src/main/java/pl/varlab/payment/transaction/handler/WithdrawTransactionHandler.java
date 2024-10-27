package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.transaction.PaymentTransactionEventService;
import pl.varlab.payment.transaction.TransactionRequest;

@Slf4j
@AllArgsConstructor
public class WithdrawTransactionHandler extends BaseTransactionHandler {

    private final PaymentTransactionEventService transactionEventService;

    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            transactionEventService.withdraw(transactionRequest);
            super.handle(transactionRequest);
        } catch (InsufficientFundsException e) {
            log.warn("Insufficient funds on sender account for payment request: {}", transactionRequest);
        } catch (PaymentAccountNotFoundException e) {
            log.warn("Sender accountId not found: {}", transactionRequest);
        }
    }
}
