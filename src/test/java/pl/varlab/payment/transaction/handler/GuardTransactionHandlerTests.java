package pl.varlab.payment.transaction.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.common.ConflictDataException;
import pl.varlab.payment.transaction.PaymentTransactionBlocker;
import pl.varlab.payment.transaction.PaymentTransactionService;
import pl.varlab.payment.transaction.guard.ComplianceGuard;
import pl.varlab.payment.transaction.guard.FraudDetectedException;
import pl.varlab.payment.transaction.guard.FraudDetectionGuard;
import pl.varlab.payment.transaction.guard.NonCompliantTransactionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static pl.varlab.payment.transaction.PaymentTransactionTestCommons.getTransactionRequest;

public class GuardTransactionHandlerTests {

    private static final String UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE = "Unexpected handler exception";
    private static final String ERR_MSG = "err msg";
    private final ComplianceGuard complianceGuard = mock(ComplianceGuard.class);
    private final FraudDetectionGuard fraudDetectionGuard = mock(FraudDetectionGuard.class);
    private final PaymentTransactionBlocker transactionBlocker = mock(PaymentTransactionBlocker.class);
    private final TransactionHandler nextHandler = mock(TransactionHandler.class);
    private final PaymentTransactionService paymentTransactionService = mock(PaymentTransactionService.class);
    private GuardTransactionHandler guardTransactionHandler;

    @BeforeEach
    void setUp() {
        reset(fraudDetectionGuard, complianceGuard, transactionBlocker, nextHandler);
        var transactionGuards = List.of(complianceGuard, fraudDetectionGuard);
        guardTransactionHandler = new GuardTransactionHandler(transactionGuards, transactionBlocker, paymentTransactionService);
        guardTransactionHandler.setHandler(nextHandler);
    }

    @Test
    public void shouldPassTransactionToNextHandler() {
        var transactionRequest = getTransactionRequest();

        when(complianceGuard.assertTransaction(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));
        when(fraudDetectionGuard.assertTransaction(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));

        guardTransactionHandler.handle(transactionRequest);

        verify(complianceGuard).assertTransaction(transactionRequest);
        verify(fraudDetectionGuard).assertTransaction(transactionRequest);
        verify(nextHandler).handle(transactionRequest);
        verifyNoMoreInteractions(fraudDetectionGuard, complianceGuard, nextHandler);
        verifyNoInteractions(transactionBlocker);
    }

    @Test
    public void shouldBlockTransactionAndThrowValidationException_whenFraudDetected() {
        var transactionRequest = getTransactionRequest();

        var fraudException = new FraudDetectedException(transactionRequest, ERR_MSG);
        when(complianceGuard.assertTransaction(transactionRequest)).thenReturn(CompletableFuture.failedFuture(fraudException));
        when(fraudDetectionGuard.assertTransaction(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));

        assertThrows(ConflictDataException.class, () -> guardTransactionHandler.handle(transactionRequest), ERR_MSG);

        verify(transactionBlocker).blockTransaction(fraudException);
        verify(complianceGuard).assertTransaction(transactionRequest);
        verify(fraudDetectionGuard).assertTransaction(transactionRequest);
        verifyNoMoreInteractions(fraudDetectionGuard, complianceGuard, transactionBlocker);
        verifyNoInteractions(nextHandler);
    }

    @Test
    public void shouldBlockTransactionAndThrowValidationException_whenTransactionIsNonCompliant() {
        var transactionRequest = getTransactionRequest();

        var nonCompliantException = new NonCompliantTransactionException(transactionRequest, ERR_MSG);
        when(complianceGuard.assertTransaction(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));
        when(fraudDetectionGuard.assertTransaction(transactionRequest)).thenReturn(CompletableFuture.failedFuture(nonCompliantException));

        assertThrows(ConflictDataException.class, () -> guardTransactionHandler.handle(transactionRequest), ERR_MSG);

        verify(transactionBlocker).blockTransaction(nonCompliantException);
        verify(complianceGuard).assertTransaction(transactionRequest);
        verify(fraudDetectionGuard).assertTransaction(transactionRequest);
        verifyNoMoreInteractions(fraudDetectionGuard, complianceGuard, transactionBlocker);
        verifyNoInteractions(nextHandler);
    }

    @Test
    public void shouldThrowException_whenUnexpectedExceptionOccurred() {
        var transactionRequest = getTransactionRequest();

        var unexpectedException = new IllegalArgumentException(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE);

        doThrow(unexpectedException)
                .when(fraudDetectionGuard).assertTransaction(transactionRequest);

        assertThrows(IllegalArgumentException.class,
                () -> guardTransactionHandler.handle(transactionRequest),
                UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE);

        verifyNoInteractions(transactionBlocker, nextHandler);
    }

    @Test
    public void shouldThrowException_whenUnexpectedExecutionExceptionOccurred() {
        var transactionRequest = getTransactionRequest();

        var unexpectedException = new IllegalArgumentException(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE);
        var executionException = new ExecutionException(unexpectedException);

        when(fraudDetectionGuard.assertTransaction(transactionRequest))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(complianceGuard.assertTransaction(transactionRequest))
                .thenReturn(CompletableFuture.failedFuture(executionException));


        assertThrows(RuntimeException.class,
                () -> guardTransactionHandler.handle(transactionRequest),
                UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE);

        verifyNoInteractions(transactionBlocker, nextHandler);
    }
}
