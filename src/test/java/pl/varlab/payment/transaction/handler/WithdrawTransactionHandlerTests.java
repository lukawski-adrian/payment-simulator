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

public class WithdrawTransactionHandlerTests {

    private static final String UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE = "Unexpected handler exception";
    private static final String FRAUD_ERROR_MESSAGE = "fraud error message";
    private final PaymentTransactionEventService transactionEventService = mock(PaymentTransactionEventService.class);
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

        try {
            withdrawTransactionHandler.handle(transactionRequest);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE, e.getMessage());
        }

        verify(transactionEventService).withdraw(transactionRequest);
        verifyNoMoreInteractions(transactionEventService);
        verifyNoInteractions(nextHandler, transactionBlocker);
    }

    @Test
    public void shouldNotDepositFundsAndBlockTransaction_whenFraudDetectedFound() throws FraudDetectedException {
        var transactionRequest = getTransactionRequest();

        var fraudException = new FraudDetectedException(transactionRequest, FRAUD_ERROR_MESSAGE);
        doThrow(fraudException).when(transactionEventService).withdraw(transactionRequest);

        try {
            withdrawTransactionHandler.handle(transactionRequest);
            fail();
        } catch (ValidationException e) {
            assertEquals(FRAUD_ERROR_MESSAGE, e.getMessage());
        }

        verify(transactionEventService).withdraw(transactionRequest);
        verify(transactionBlocker).blockTransaction(fraudException);
        verifyNoMoreInteractions(transactionEventService, transactionBlocker);
        verifyNoInteractions(nextHandler);
    }
}
