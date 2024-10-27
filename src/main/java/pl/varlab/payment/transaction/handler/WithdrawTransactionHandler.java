package pl.varlab.payment.transaction.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.varlab.payment.account.AccountNotFoundException;
import pl.varlab.payment.account.AccountService;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.transaction.TransactionRequest;

@Slf4j
@AllArgsConstructor
public class WithdrawTransactionHandler extends BaseTransactionHandler {

    private final AccountService accountService;

    @Override
    public void handle(TransactionRequest transactionRequest) {
        try {
            accountService.withdraw(transactionRequest);
            super.handle(transactionRequest);
        } catch (InsufficientFundsException e) {
            log.warn("Insufficient funds on sender account for payment request: {}", transactionRequest);
        } catch (AccountNotFoundException e) {
            log.warn("Sender accountId not found: {}", transactionRequest);
        }
    }
}
