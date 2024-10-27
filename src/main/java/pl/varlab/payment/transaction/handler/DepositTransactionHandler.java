package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.account.PaymentAccountService;
import pl.varlab.payment.transaction.TransactionRequest;

@Slf4j
@AllArgsConstructor
public class DepositTransactionHandler extends BaseTransactionHandler {

    private final PaymentAccountService accountService;

    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            // TODO: consider again check if blocked
            accountService.deposit(transactionRequest);
            super.handle(transactionRequest);
        } catch (PaymentAccountNotFoundException e) {
            // TODO: consider more sophisticated error handling, verify sender and recipient ids at the beginning?
            // TODO: or recover? refund?
            log.warn("Recipient accountId not found: {}", transactionRequest);
        }
    }
}
