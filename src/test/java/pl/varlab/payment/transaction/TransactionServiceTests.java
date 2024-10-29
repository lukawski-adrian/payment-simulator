package pl.varlab.payment.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.transaction.handler.TransactionHandler;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;


public class TransactionServiceTests {

    private final TransactionHandler transactionHandler = mock(TransactionHandler.class);
    private final TransactionFallbackService fallbackService = mock(TransactionFallbackService.class);

    @BeforeEach
    void setUp() {
        reset(transactionHandler, fallbackService);
    }

    @Test
    public void shouldProcessTransaction_thenTransferFunds() {
        // TODO: Implement integration test for transaction chain of responsibility
    }

    @Test
    public void shouldReportTransaction_whenUnexpectedExceptionOccurred() {
        // TODO: Implement integration test for transaction chain of responsibility
    }

}
