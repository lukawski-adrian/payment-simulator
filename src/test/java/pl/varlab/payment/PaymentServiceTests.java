package pl.varlab.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.transaction.PaymentFallbackService;
import pl.varlab.payment.transaction.handler.TransactionHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;


public class PaymentServiceTests {

    private final TransactionHandler transactionHandler = mock(TransactionHandler.class);
    private final PaymentFallbackService fallbackService = mock(PaymentFallbackService.class);

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
