package pl.varlab.payment.transaction.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.account.AccountNotFoundException;
import pl.varlab.payment.account.AccountService;
import pl.varlab.payment.account.InsufficientFundsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;

public class WithdrawTransactionHandlerTests {

    private static final String UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE = "Unexpected handler exception";
    private final AccountService accountService = mock(AccountService.class);
    private final TransactionHandler nextHandler = mock(TransactionHandler.class);
    private WithdrawTransactionHandler withdrawTransactionHandler;

    @BeforeEach
    void setUp() {
        reset(accountService, nextHandler);
        withdrawTransactionHandler = new WithdrawTransactionHandler(accountService);
        withdrawTransactionHandler.setHandler(nextHandler);
    }

    @Test
    public void shouldWithdrawFunds() throws InsufficientFundsException, AccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        withdrawTransactionHandler.handle(transactionRequest);

        verify(accountService).withdraw(transactionRequest);
        verify(nextHandler).handle(transactionRequest);
        verifyNoMoreInteractions(accountService, nextHandler);
    }

    @Test
    public void shouldNotWithdrawFunds_whenInsufficientFunds() throws InsufficientFundsException, AccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        doThrow(InsufficientFundsException.class).when(accountService).withdraw(transactionRequest);

        withdrawTransactionHandler.handle(transactionRequest);

        verify(accountService).withdraw(transactionRequest);
        verifyNoMoreInteractions(accountService);
        verifyNoInteractions(nextHandler);
    }

    @Test
    public void shouldNotWithdrawFunds_whenSenderAccountNotFound() throws InsufficientFundsException, AccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        doThrow(AccountNotFoundException.class).when(accountService).withdraw(transactionRequest);

        withdrawTransactionHandler.handle(transactionRequest);

        verify(accountService).withdraw(transactionRequest);
        verifyNoMoreInteractions(accountService);
        verifyNoInteractions(nextHandler);
    }

    @Test
    public void shouldNotWithdrawFundsAndThrowException_whenUnexpectedExceptionOccurred() throws InsufficientFundsException, AccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        doThrow(new IllegalArgumentException(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE)).when(accountService).withdraw(transactionRequest);

        try {
            withdrawTransactionHandler.handle(transactionRequest);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE, e.getMessage());
        }

        verify(accountService).withdraw(transactionRequest);
        verifyNoMoreInteractions(accountService);
        verifyNoInteractions(nextHandler);
    }
}
