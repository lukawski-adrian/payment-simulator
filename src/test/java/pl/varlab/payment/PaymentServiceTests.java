package pl.varlab.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.varlab.payment.transaction.PaymentFallbackService;
import pl.varlab.payment.transaction.handler.TransactionHandler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

@Testcontainers
public class PaymentServiceTests {

    private final TransactionHandler transactionHandler = mock(TransactionHandler.class);
    private final PaymentFallbackService fallbackService = mock(PaymentFallbackService.class);

    // will be shared between test methods
    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = new PostgreSQLContainer()
            .withDatabaseName("foo")
            .withUsername("foo")
            .withPassword("secret");

    @BeforeEach
    void setUp() {
        reset(transactionHandler, fallbackService);
    }

    @Test
    public void shouldProcessTransaction_thenTransferFunds() {
        assertTrue(POSTGRESQL_CONTAINER.isRunning());
        // TODO: Implement integration test for transaction chain of responsibility
    }

    @Test
    public void shouldReportTransaction_whenUnexpectedExceptionOccurred() {
        // TODO: Implement integration test for transaction chain of responsibility
    }

}
