package pl.varlab.payment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.varlab.payment.account.PaymentAccountGuard;
import pl.varlab.payment.guard.ComplianceGuard;
import pl.varlab.payment.guard.FraudDetectionGuard;
import pl.varlab.payment.transaction.PaymentTransactionEventGuard;
import pl.varlab.payment.transaction.PaymentTransactionEventService;
import pl.varlab.payment.transaction.TransactionBlocker;
import pl.varlab.payment.transaction.handler.*;

@Configuration
public class RootContextConfig {

    @Bean
    public TransactionHandler transactionHandler(PaymentTransactionEventGuard paymentTransactionEventGuard,
                                                 PaymentAccountGuard paymentAccountGuard,
                                                 PaymentTransactionEventService transactionEventService,
                                                 FraudDetectionGuard fraudDetectionGuard,
                                                 ComplianceGuard complianceGuard,
                                                 TransactionBlocker transactionBlocker) {
        var validationHandler = new InitialValidationTransactionHandler(paymentTransactionEventGuard, paymentAccountGuard);
        var withdrawHandler = new WithdrawTransactionHandler(transactionEventService, transactionBlocker);
        var verificationHandler = new GuardTransactionHandler(fraudDetectionGuard, complianceGuard, transactionBlocker);
        var depositHandler = new DepositTransactionHandler(transactionEventService, transactionBlocker);

        validationHandler.setHandler(withdrawHandler);
        withdrawHandler.setHandler(verificationHandler);
        verificationHandler.setHandler(depositHandler);

        return validationHandler;
    }

}
