package pl.varlab.payment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.varlab.payment.guard.ComplianceGuard;
import pl.varlab.payment.guard.FraudDetectionGuard;
import pl.varlab.payment.transfer.MoneyTransferService;
import pl.varlab.payment.transaction.PaymentTransactionService;
import pl.varlab.payment.transaction.TransactionBlocker;
import pl.varlab.payment.transaction.handler.*;
import pl.varlab.payment.transaction.validation.ValidationService;

@Configuration
public class RootContextConfig {

    @Bean
    public TransactionHandler transactionHandler(MoneyTransferService transactionEventService,
                                                 PaymentTransactionService paymentTransactionService,
                                                 ValidationService validationService,
                                                 FraudDetectionGuard fraudDetectionGuard,
                                                 ComplianceGuard complianceGuard,
                                                 TransactionBlocker transactionBlocker) {
        var validationHandler = new InitialValidationTransactionHandler(validationService);
        var withdrawHandler = new WithdrawTransactionHandler(transactionEventService, transactionBlocker);
        var verificationHandler = new GuardTransactionHandler(fraudDetectionGuard, complianceGuard, transactionBlocker, paymentTransactionService);
        var depositHandler = new DepositTransactionHandler(transactionEventService, transactionBlocker);

        validationHandler.setHandler(withdrawHandler);
        withdrawHandler.setHandler(verificationHandler);
        verificationHandler.setHandler(depositHandler);

        return validationHandler;
    }

}
