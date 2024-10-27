package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.account.AccountNotFoundException;
import pl.varlab.payment.account.AccountService;
import pl.varlab.payment.transaction.TransactionRequest;

@Slf4j
@AllArgsConstructor
public class DepositTransactionHandler extends BaseTransactionHandler {

    private final AccountService accountService;

    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            // TODO: consider again check if blocked
            accountService.deposit(transactionRequest);
            super.handle(transactionRequest);
        } catch (AccountNotFoundException e) {
            // TODO: consider more sophisticated error handling, verify sender and recipient ids at the beginning?
            // TODO: or recover? refund?
            log.warn("Recipient accountId not found: {}", transactionRequest);
        }
    }
}
