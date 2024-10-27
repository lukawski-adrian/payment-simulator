package pl.varlab.payment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.varlab.payment.guard.ComplianceGuard;
import pl.varlab.payment.guard.FraudDetectionGuard;
import pl.varlab.payment.transaction.PaymentTransactionEventService;
import pl.varlab.payment.transaction.TransactionBlocker;
import pl.varlab.payment.transaction.handler.*;
import pl.varlab.payment.validation.ValidationService;

@Configuration
public class RootContextConfig {

    @Bean
    public TransactionHandler transactionHandler(ValidationService validationService,
                                                 PaymentTransactionEventService transactionEventService,
                                                 FraudDetectionGuard fraudDetectionGuard,
                                                 ComplianceGuard complianceGuard,
                                                 TransactionBlocker transactionBlocker) {

        var validationHandler = new InitialValidationTransactionHandler(validationService);
        var withdrawHandler = new WithdrawTransactionHandler(transactionEventService);
        var verificationHandler = new GuardTransactionHandler(fraudDetectionGuard, complianceGuard, transactionBlocker);
        var depositHandler = new DepositTransactionHandler(transactionEventService);

        validationHandler.setHandler(withdrawHandler);
        withdrawHandler.setHandler(verificationHandler);
        verificationHandler.setHandler(depositHandler);

        return validationHandler;
    }

}
