package pl.varlab.payment.transaction.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.common.ConflictDataException;
import pl.varlab.payment.transaction.guard.FraudDetectedException;
import pl.varlab.payment.transaction.PaymentTransactionBlocker;
import pl.varlab.payment.transfer.MoneyTransferService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static pl.varlab.payment.transaction.PaymentTransactionTestCommons.getTransactionRequest;

public class DepositTransactionHandlerTests {

    private static final String UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE = "Unexpected handler exception";
    private static final String FRAUD_ERROR_MESSAGE = "fraud error message";
    private final MoneyTransferService transactionEventService = mock(MoneyTransferService.class);
    private final PaymentTransactionBlocker transactionBlocker = mock(PaymentTransactionBlocker.class);
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

        assertThrows(IllegalArgumentException.class, () -> depositTransactionHandler.handle(transactionRequest), UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE);

        verify(transactionEventService).deposit(transactionRequest);
        verifyNoMoreInteractions(transactionEventService);
        verifyNoInteractions(nextHandler, transactionBlocker);
    }

    @Test
    public void shouldNotDepositFundsAndBlockTransaction_whenFraudDetectedFound() throws FraudDetectedException {
        var transactionRequest = getTransactionRequest();

        var fraudException = new FraudDetectedException(transactionRequest, FRAUD_ERROR_MESSAGE);
        doThrow(fraudException).when(transactionEventService).deposit(transactionRequest);

        assertThrows(ConflictDataException.class, () -> depositTransactionHandler.handle(transactionRequest), FRAUD_ERROR_MESSAGE);

        verify(transactionEventService).deposit(transactionRequest);
        verify(transactionBlocker).blockTransaction(fraudException);
        verifyNoMoreInteractions(transactionEventService, transactionBlocker);
        verifyNoInteractions(nextHandler);
    }
}
