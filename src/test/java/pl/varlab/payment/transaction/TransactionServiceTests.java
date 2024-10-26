package pl.varlab.payment.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.account.AccountNotFoundException;
import pl.varlab.payment.account.AccountService;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.guard.ComplianceGuard;
import pl.varlab.payment.guard.FraudDetectedException;
import pl.varlab.payment.guard.FraudDetectionGuard;
import pl.varlab.payment.guard.NonCompliantTransactionException;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static pl.varlab.payment.transaction.TransactionTestCommons.TRANSACTION_ID;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;

// TODO: distinguish between unit and integration tests
public class TransactionServiceTests {

    private final ComplianceGuard complianceGuard = mock(ComplianceGuard.class);
    private final FraudDetectionGuard fraudDetectionGuard = mock(FraudDetectionGuard.class);
    private final AccountService accountService = mock(AccountService.class);
    private final TransactionBlocker transactionBlocker = mock(TransactionBlocker.class);
    private final TransactionFallbackService fallbackService = mock(TransactionFallbackService.class);

    @BeforeEach
    void setUp() {
        reset(complianceGuard, fraudDetectionGuard, accountService, transactionBlocker, fallbackService);
    }

    @Test
    public void shouldProcessTransaction_thenTransferMoney() throws InsufficientFundsException, AccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        when(fraudDetectionGuard.assertNotFraud(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));
        when(complianceGuard.assertCompliant(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));

        var transactionService = new TransactionService(fraudDetectionGuard, complianceGuard, accountService, transactionBlocker, fallbackService);

        transactionService.processTransaction(transactionRequest);

        verify(accountService).withdraw(transactionRequest);
        verify(accountService).deposit(transactionRequest);
        verify(complianceGuard).assertCompliant(transactionRequest);
        verify(fraudDetectionGuard).assertNotFraud(transactionRequest);

        verifyNoMoreInteractions(accountService, complianceGuard, fraudDetectionGuard);
        verifyNoInteractions(fallbackService, transactionBlocker);
    }


    @Test
    public void shouldNotProcessTransaction_whenInsufficientFunds() throws InsufficientFundsException, AccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        doThrow(InsufficientFundsException.class).when(accountService).withdraw(transactionRequest);

        var transactionService = new TransactionService(fraudDetectionGuard, complianceGuard, accountService, transactionBlocker, fallbackService);

        transactionService.processTransaction(transactionRequest);

        verify(accountService).withdraw(transactionRequest);
        verifyNoMoreInteractions(accountService);
        verifyNoInteractions(complianceGuard, fraudDetectionGuard, transactionBlocker, fallbackService);
    }

    @Test
    public void shouldNotProcessTransaction_whenSenderAccountNotFound() throws InsufficientFundsException, AccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        doThrow(AccountNotFoundException.class).when(accountService).withdraw(transactionRequest);

        var transactionService = new TransactionService(fraudDetectionGuard, complianceGuard, accountService, transactionBlocker, fallbackService);

        transactionService.processTransaction(transactionRequest);

        verify(accountService).withdraw(transactionRequest);
        verifyNoMoreInteractions(accountService);
        verifyNoInteractions(complianceGuard, fraudDetectionGuard, transactionBlocker, fallbackService);
    }

    @Test
    public void shouldStopProcessTransaction_whenRecipientAccountNotFound() throws InsufficientFundsException, AccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        doThrow(AccountNotFoundException.class).when(accountService).deposit(transactionRequest);

        when(complianceGuard.assertCompliant(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));
        when(fraudDetectionGuard.assertNotFraud(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));

        var transactionService = new TransactionService(fraudDetectionGuard, complianceGuard, accountService, transactionBlocker, fallbackService);

        transactionService.processTransaction(transactionRequest);

        verify(accountService).withdraw(transactionRequest);
        verify(accountService).deposit(transactionRequest);

        verify(complianceGuard).assertCompliant(transactionRequest);
        verify(fraudDetectionGuard).assertNotFraud(transactionRequest);

        verifyNoMoreInteractions(accountService, complianceGuard, fraudDetectionGuard, transactionBlocker);
        verifyNoInteractions(fallbackService);
    }

    @Test
    public void shouldStopProcessAndBlockTransaction_whenFraudDetected() throws InsufficientFundsException, AccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        var fraudException = new FraudDetectedException(TRANSACTION_ID, "err msg");
        when(complianceGuard.assertCompliant(transactionRequest)).thenReturn(CompletableFuture.failedFuture(fraudException));
        when(fraudDetectionGuard.assertNotFraud(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));

        var transactionService = new TransactionService(fraudDetectionGuard, complianceGuard, accountService, transactionBlocker, fallbackService);

        transactionService.processTransaction(transactionRequest);

        verify(accountService).withdraw(transactionRequest);
        verify(transactionBlocker).blockTransaction(fraudException);
        verify(fraudDetectionGuard).assertNotFraud(transactionRequest);
        verify(complianceGuard).assertCompliant(transactionRequest);

        verifyNoMoreInteractions(accountService, complianceGuard, fraudDetectionGuard, transactionBlocker);
        verifyNoInteractions(fallbackService);
    }

    @Test
    public void shouldStopProcessAndBlockTransaction_whenTransactionIsNonCompliant() throws InsufficientFundsException, AccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        var nonCompliantException = new NonCompliantTransactionException(TRANSACTION_ID, "err msg");
        when(complianceGuard.assertCompliant(transactionRequest)).thenReturn(CompletableFuture.completedFuture(null));
        when(fraudDetectionGuard.assertNotFraud(transactionRequest)).thenReturn(CompletableFuture.failedFuture(nonCompliantException));

        var transactionService = new TransactionService(fraudDetectionGuard, complianceGuard, accountService, transactionBlocker, fallbackService);

        transactionService.processTransaction(transactionRequest);

        verify(accountService).withdraw(transactionRequest);
        verify(transactionBlocker).blockTransaction(nonCompliantException);
        verify(fraudDetectionGuard).assertNotFraud(transactionRequest);
        verify(complianceGuard).assertCompliant(transactionRequest);

        verifyNoMoreInteractions(accountService, complianceGuard, fraudDetectionGuard, transactionBlocker);
        verifyNoInteractions(fallbackService);
    }


}
