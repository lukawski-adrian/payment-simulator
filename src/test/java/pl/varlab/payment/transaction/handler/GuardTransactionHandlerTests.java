package pl.varlab.payment.transaction.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.common.ValidationException;
import pl.varlab.payment.guard.ComplianceGuard;
import pl.varlab.payment.guard.FraudDetectedException;
import pl.varlab.payment.guard.FraudDetectionGuard;
import pl.varlab.payment.guard.NonCompliantTransactionException;
import pl.varlab.payment.transaction.TransactionBlocker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;

public class GuardTransactionHandlerTests {

    private static final String UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE = "Unexpected handler exception";
    private static final String ERR_MSG = "err msg";
    private final ComplianceGuard complianceGuard = mock(ComplianceGuard.class);
    private final FraudDetectionGuard fraudDetectionGuard = mock(FraudDetectionGuard.class);
    private final TransactionBlocker transactionBlocker = mock(TransactionBlocker.class);
    private final TransactionHandler nextHandler = mock(TransactionHandler.class);
    private GuardTransactionHandler guardTransactionHandler;

    @BeforeEach
    void setUp() {
        reset(fraudDetectionGuard, complianceGuard, transactionBlocker, nextHandler);
        guardTransactionHandler = new GuardTransactionHandler(fraudDetectionGuard, complianceGuard, transactionBlocker);
        guardTransactionHandler.setHandler(nextHandler);
    }

    @Test
    public void shouldPassTransactionToNextHandler() {
        var transactionRequest = getTransactionRequest();

        when(complianceGuard.assertCompliant(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));
        when(fraudDetectionGuard.assertNotFraud(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));

        guardTransactionHandler.handle(transactionRequest);

        verify(complianceGuard).assertCompliant(transactionRequest);
        verify(fraudDetectionGuard).assertNotFraud(transactionRequest);
        verify(nextHandler).handle(transactionRequest);
        verifyNoMoreInteractions(fraudDetectionGuard, complianceGuard, nextHandler);
        verifyNoInteractions(transactionBlocker);
    }

    @Test
    public void shouldBlockTransactionAndThrowValidationException_whenFraudDetected() {
        var transactionRequest = getTransactionRequest();

        var fraudException = new FraudDetectedException(transactionRequest, ERR_MSG);
        when(complianceGuard.assertCompliant(transactionRequest)).thenReturn(CompletableFuture.failedFuture(fraudException));
        when(fraudDetectionGuard.assertNotFraud(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));

        try {
            guardTransactionHandler.handle(transactionRequest);
            fail();
        } catch (ValidationException e) {
            assertEquals(ERR_MSG, e.getMessage());
        }

        verify(transactionBlocker).blockTransaction(fraudException);
        verify(complianceGuard).assertCompliant(transactionRequest);
        verify(fraudDetectionGuard).assertNotFraud(transactionRequest);
        verifyNoMoreInteractions(fraudDetectionGuard, complianceGuard, transactionBlocker);
        verifyNoInteractions(nextHandler);
    }

    @Test
    public void shouldBlockTransactionAndThrowValidationException_whenTransactionIsNonCompliant() {
        var transactionRequest = getTransactionRequest();

        var nonCompliantException = new NonCompliantTransactionException(transactionRequest, ERR_MSG);
        when(complianceGuard.assertCompliant(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));
        when(fraudDetectionGuard.assertNotFraud(transactionRequest)).thenReturn(CompletableFuture.failedFuture(nonCompliantException));

        try {
            guardTransactionHandler.handle(transactionRequest);
        } catch (ValidationException e) {
            assertEquals(ERR_MSG, e.getMessage());
        }

        verify(transactionBlocker).blockTransaction(nonCompliantException);
        verify(complianceGuard).assertCompliant(transactionRequest);
        verify(fraudDetectionGuard).assertNotFraud(transactionRequest);
        verifyNoMoreInteractions(fraudDetectionGuard, complianceGuard, transactionBlocker);
        verifyNoInteractions(nextHandler);
    }

    @Test
    public void shouldThrowException_whenUnexpectedExceptionOccurred() {
        var transactionRequest = getTransactionRequest();

        var unexpectedException = new IllegalArgumentException(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE);

        doThrow(unexpectedException)
                .when(fraudDetectionGuard).assertNotFraud(transactionRequest);

        try {
            guardTransactionHandler.handle(transactionRequest);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE, e.getMessage());
        }

        verifyNoInteractions(transactionBlocker, nextHandler);
    }

    @Test
    public void shouldThrowException_whenUnexpectedExecutionExceptionOccurred() {
        var transactionRequest = getTransactionRequest();

        var unexpectedException = new IllegalArgumentException(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE);
        var executionException = new ExecutionException(unexpectedException);

        when(fraudDetectionGuard.assertNotFraud(transactionRequest))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(complianceGuard.assertCompliant(transactionRequest))
                .thenReturn(CompletableFuture.failedFuture(executionException));

        try {
            guardTransactionHandler.handle(transactionRequest);
            fail();
        } catch (RuntimeException e) {
            assertEquals("Unexpected execution error during transaction verification", e.getMessage());
            assertEquals(executionException, e.getCause());
        }

        verifyNoInteractions(transactionBlocker, nextHandler);
    }
}
