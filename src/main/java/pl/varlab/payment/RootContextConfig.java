package pl.varlab.payment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.varlab.payment.account.PaymentAccountService;
import pl.varlab.payment.guard.ComplianceGuard;
import pl.varlab.payment.guard.FraudDetectionGuard;
import pl.varlab.payment.transaction.TransactionBlocker;
import pl.varlab.payment.transaction.handler.*;
import pl.varlab.payment.validation.ValidationService;

@Configuration
public class RootContextConfig {

    @Bean
    public TransactionHandler transactionHandler(ValidationService validationService,
                                                 PaymentAccountService accountService,
                                                 FraudDetectionGuard fraudDetectionGuard,
                                                 ComplianceGuard complianceGuard,
                                                 TransactionBlocker transactionBlocker) {

        var validationHandler = new InitialValidationTransactionHandler(validationService);
        var withdrawHandler = new WithdrawTransactionHandler(accountService);
        var verificationHandler = new GuardTransactionHandler(fraudDetectionGuard, complianceGuard, transactionBlocker);
        var depositHandler = new DepositTransactionHandler(accountService);

        validationHandler.setHandler(withdrawHandler);
        withdrawHandler.setHandler(verificationHandler);
        verificationHandler.setHandler(depositHandler);

        return validationHandler;
    }

}
