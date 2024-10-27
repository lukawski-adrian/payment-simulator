package pl.varlab.payment.transaction.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.account.PaymentAccountNotFoundException;
import pl.varlab.payment.account.PaymentAccountService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;

public class DepositTransactionHandlerTests {

    private static final String UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE = "Unexpected handler exception";
    private final PaymentAccountService accountService = mock(PaymentAccountService.class);
    private final TransactionHandler nextHandler = mock(TransactionHandler.class);
    private DepositTransactionHandler depositTransactionHandler = new DepositTransactionHandler(accountService);

    @BeforeEach
    void setUp() {
        reset(accountService, nextHandler);
        depositTransactionHandler = new DepositTransactionHandler(accountService);
        depositTransactionHandler.setHandler(nextHandler);
    }

    @Test
    public void shouldDepositFunds() throws PaymentAccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        depositTransactionHandler.handle(transactionRequest);

        verify(accountService).deposit(transactionRequest);
        verify(nextHandler).handle(transactionRequest);
        verifyNoMoreInteractions(accountService, nextHandler);
    }

    @Test
    public void shouldNotDepositFunds_whenSenderAccountNotFound() throws PaymentAccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        doThrow(PaymentAccountNotFoundException.class).when(accountService).deposit(transactionRequest);

        depositTransactionHandler.handle(transactionRequest);

        verify(accountService).deposit(transactionRequest);
        verifyNoMoreInteractions(accountService);
        verifyNoInteractions(nextHandler);
    }

    @Test
    public void shouldNotDepositFundsAndThrowException_whenUnexpectedExceptionOccurred() throws PaymentAccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        doThrow(new IllegalArgumentException(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE)).when(accountService).deposit(transactionRequest);

        try {
            depositTransactionHandler.handle(transactionRequest);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(UNEXPECTED_HANDLER_EXCEPTION_ERROR_MESSAGE, e.getMessage());
        }

        verify(accountService).deposit(transactionRequest);
        verifyNoMoreInteractions(accountService);
        verifyNoInteractions(nextHandler);
    }
}
