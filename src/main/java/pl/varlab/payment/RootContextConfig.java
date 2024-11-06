package pl.varlab.payment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.varlab.payment.transaction.PaymentTransactionBlocker;
import pl.varlab.payment.transaction.PaymentTransactionService;
import pl.varlab.payment.transaction.guard.TransactionGuard;
import pl.varlab.payment.transaction.handler.*;
import pl.varlab.payment.transaction.validation.ValidationService;
import pl.varlab.payment.transfer.MoneyTransferService;

import java.util.List;

@Configuration
public class RootContextConfig {

    @Bean
    public TransactionHandler transactionHandler(MoneyTransferService transactionEventService,
                                                 PaymentTransactionService paymentTransactionService,
                                                 ValidationService validationService,
                                                 PaymentTransactionBlocker transactionBlocker,
                                                 List<TransactionGuard> transactionHandlers) {

        var validationHandler = new InitialValidationTransactionHandler(validationService);
        var withdrawHandler = new WithdrawTransactionHandler(transactionEventService, transactionBlocker);
        var verificationHandler = new GuardTransactionHandler(transactionHandlers, transactionBlocker, paymentTransactionService);
        var depositHandler = new DepositTransactionHandler(transactionEventService, transactionBlocker);

        validationHandler.setHandler(withdrawHandler);
        withdrawHandler.setHandler(verificationHandler);
        verificationHandler.setHandler(depositHandler);

        return validationHandler;
    }

}
