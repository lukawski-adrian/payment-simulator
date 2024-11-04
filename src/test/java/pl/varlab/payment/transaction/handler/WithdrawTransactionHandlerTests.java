package pl.varlab.payment.transaction.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.common.ConflictDataException;
import pl.varlab.payment.guard.FraudDetectedException;
import pl.varlab.payment.transaction.TransactionBlocker;
import pl.varlab.payment.transfer.MoneyTransferService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;

public class WithdrawTransactionHandlerTests {

    private static final String UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE = "Unexpected handler exception";
    private static final String FRAUD_ERROR_MESSAGE = "fraud error message";
    private final MoneyTransferService transactionEventService = mock(MoneyTransferService.class);
    private final TransactionBlocker transactionBlocker = mock(TransactionBlocker.class);
    private final TransactionHandler nextHandler = mock(TransactionHandler.class);
    private WithdrawTransactionHandler withdrawTransactionHandler;

    @BeforeEach
    void setUp() {
        reset(transactionEventService, nextHandler, transactionBlocker);
        withdrawTransactionHandler = new WithdrawTransactionHandler(transactionEventService, transactionBlocker);
        withdrawTransactionHandler.setHandler(nextHandler);
    }

    @Test
    public void shouldWithdrawFunds() throws FraudDetectedException {
        var transactionRequest = getTransactionRequest();

        withdrawTransactionHandler.handle(transactionRequest);

        verify(transactionEventService).withdraw(transactionRequest);
        verify(nextHandler).handle(transactionRequest);
        verifyNoMoreInteractions(transactionEventService, nextHandler);
        verifyNoInteractions(transactionBlocker);
    }

    @Test
    public void shouldNotWithdrawFundsAndThrowException_whenUnexpectedExceptionOccurred() throws FraudDetectedException {
        var transactionRequest = getTransactionRequest();

        doThrow(new IllegalArgumentException(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE)).when(transactionEventService).withdraw(transactionRequest);

        assertThrows(IllegalArgumentException.class, () -> withdrawTransactionHandler.handle(transactionRequest), UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE);

        verify(transactionEventService).withdraw(transactionRequest);
        verifyNoMoreInteractions(transactionEventService);
        verifyNoInteractions(nextHandler, transactionBlocker);
    }

    @Test
    public void shouldNotDepositFundsAndBlockTransaction_whenFraudDetectedFound() throws FraudDetectedException {
        var transactionRequest = getTransactionRequest();

        var fraudException = new FraudDetectedException(transactionRequest, FRAUD_ERROR_MESSAGE);
        doThrow(fraudException).when(transactionEventService).withdraw(transactionRequest);

        assertThrows(ConflictDataException.class, () -> withdrawTransactionHandler.handle(transactionRequest), FRAUD_ERROR_MESSAGE);

        verify(transactionEventService).withdraw(transactionRequest);
        verify(transactionBlocker).blockTransaction(fraudException);
        verifyNoMoreInteractions(transactionEventService, transactionBlocker);
        verifyNoInteractions(nextHandler);
    }
}
