package pl.varlab.payment.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import pl.varlab.payment.BaseSpringContextTest;
import pl.varlab.payment.PaymentService;
import pl.varlab.payment.common.PaymentFlowException;
import pl.varlab.payment.transaction.handler.TransactionHandler;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static pl.varlab.payment.AsyncConfig.TRANSACTION_GUARDS_THREAD_POOL_TASK_EXECUTOR;
import static pl.varlab.payment.transaction.PaymentTransactionTestCommons.getTransactionRequest;

@SpringJUnitConfig
public class PaymentServiceResilienceTests extends BaseSpringContextTest {

    private static final int RETRY_MAX_ATTEMPTS = 3;

    @TestConfiguration
    public static class TestAsyncConfig implements AsyncConfigurer {

        @Bean(name = TRANSACTION_GUARDS_THREAD_POOL_TASK_EXECUTOR)
        public Executor transactionGuardThreadPoolTaskExecutor() {
            return new SimpleAsyncTaskExecutor();
        }

        @Override
        public Executor getAsyncExecutor() {
            return new SimpleAsyncTaskExecutor();
        }

    }

    @MockBean
    private TransactionHandler transactionHandler;

    @MockBean
    private PaymentFallbackService fallbackService;

    @Autowired
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        reset(transactionHandler, fallbackService);
    }

    @Test
    public void shouldRetryTransactionProcessMaxAttemptTimes_thenRedirectToFallbackService() {
        var transactionRequest = getTransactionRequest();

        var exception = new RuntimeException();
        doThrow(exception).when(transactionHandler).handle(transactionRequest);

        paymentService.processTransaction(transactionRequest);

        verify(transactionHandler, times(RETRY_MAX_ATTEMPTS)).handle(transactionRequest);
        verify(fallbackService).reportTransactionProcessFailure(transactionRequest, exception);

        verifyNoMoreInteractions(transactionHandler, fallbackService);
    }

    @Test
    public void shouldProcessTransactionAfterFirstRetry() {
        var transactionRequest = getTransactionRequest();

        doThrow(RuntimeException.class).doNothing().when(transactionHandler).handle(transactionRequest);

        paymentService.processTransaction(transactionRequest);

        verify(transactionHandler, times(RETRY_MAX_ATTEMPTS - 1)).handle(transactionRequest);

        verifyNoMoreInteractions(transactionHandler);
        verifyNoInteractions(fallbackService);
    }

    @Test
    public void shouldNotProcessTransactionAndThrowPaymentFlowException() {
        var transactionRequest = getTransactionRequest();

        doThrow(PaymentFlowException.class).doNothing().when(transactionHandler).handle(any(TransactionRequest.class));

        assertThrows(PaymentFlowException.class, () -> paymentService.processTransaction(transactionRequest));

        verify(transactionHandler).handle(transactionRequest);

        verifyNoMoreInteractions(transactionHandler);
        verifyNoInteractions(fallbackService);
    }
}
