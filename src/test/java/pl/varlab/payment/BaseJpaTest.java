package pl.varlab.payment;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Example;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.varlab.payment.account.PaymentAccountBalance;
import pl.varlab.payment.transfer.MoneyTransfer;
import pl.varlab.payment.transfer.MoneyTransferRepository;
import pl.varlab.payment.transfer.TransferType;

import java.math.BigDecimal;
import java.util.UUID;

@Testcontainers
public class BaseJpaTest extends BaseSpringContextTest {

    @Container
    @ServiceConnection(type = JdbcConnectionDetails.class)
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:17.0")
            .withDatabaseName("foo")
            .withUsername("foo")
            .withPassword("secret");


    @Autowired
    protected MoneyTransferRepository moneyTransferRepository;


    protected boolean isAnyTransferExists(UUID transactionId) {
        return isTransferExists(transactionId, null);
    }

    protected boolean isTransferExists(UUID transactionId, TransferType transferType) {
        var moneyTransferExample = Example.of(new MoneyTransfer()
                .setTransactionId(transactionId)
                .setTransferType(transferType));
        return moneyTransferRepository.exists(moneyTransferExample);
    }

    protected BigDecimal getAccountBalance(String accountNumber) {
        return moneyTransferRepository.getAllAccountsBalance().stream()
                .filter(ab -> accountNumber.equals(ab.accountNumber()))
                .map(PaymentAccountBalance::balance)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
}
