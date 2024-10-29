package pl.varlab.payment.transaction.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.common.ValidationException;
import pl.varlab.payment.guard.FraudDetectedException;
import pl.varlab.payment.transaction.PaymentTransactionEventService;
import pl.varlab.payment.transaction.TransactionBlocker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;

public class DepositTransactionHandlerTests {

    private static final String UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE = "Unexpected handler exception";
    private static final String FRAUD_ERROR_MESSAGE = "fraud error message";
    private final PaymentTransactionEventService transactionEventService = mock(PaymentTransactionEventService.class);
    private final TransactionBlocker transactionBlocker = mock(TransactionBlocker.class);
    private final TransactionHandler nextHandler = mock(TransactionHandler.class);
    private DepositTransactionHandler depositTransactionHandler;

    @BeforeEach
    void setUp() {
        reset(transactionEventService, nextHandler, transactionBlocker);
        depositTransactionHandler = new DepositTransactionHandler(transactionEventService, transactionBlocker);
        depositTransactionHandler.setHandler(nextHandler);
    }

    @Test
    public void shouldDepositFunds() throws FraudDetectedException {
        var transactionRequest = getTransactionRequest();

        depositTransactionHandler.handle(transactionRequest);

        verify(transactionEventService).deposit(transactionRequest);
        verify(nextHandler).handle(transactionRequest);
        verifyNoMoreInteractions(transactionEventService, nextHandler);
        verifyNoInteractions(transactionBlocker);
    }

    @Test
    public void shouldNotDepositFundsAndThrowException_whenUnexpectedExceptionOccurred() throws FraudDetectedException {
        var transactionRequest = getTransactionRequest();

        doThrow(new IllegalArgumentException(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE)).when(transactionEventService).deposit(transactionRequest);

        try {
            depositTransactionHandler.handle(transactionRequest);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE, e.getMessage());
        }

        verify(transactionEventService).deposit(transactionRequest);
        verifyNoMoreInteractions(transactionEventService);
        verifyNoInteractions(nextHandler, transactionBlocker);
    }

    @Test
    public void shouldNotDepositFundsAndBlockTransaction_whenFraudDetectedFound() throws FraudDetectedException {
        var transactionRequest = getTransactionRequest();

        var fraudException = new FraudDetectedException(transactionRequest, FRAUD_ERROR_MESSAGE);
        doThrow(fraudException).when(transactionEventService).deposit(transactionRequest);

        try {
            depositTransactionHandler.handle(transactionRequest);
            fail();
        } catch (ValidationException e) {
            assertEquals(FRAUD_ERROR_MESSAGE, e.getMessage());
        }

        verify(transactionEventService).deposit(transactionRequest);
        verify(transactionBlocker).blockTransaction(fraudException);
        verifyNoMoreInteractions(transactionEventService, transactionBlocker);
        verifyNoInteractions(nextHandler);
    }
}
