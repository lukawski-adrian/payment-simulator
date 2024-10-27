package pl.varlab.payment.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import pl.varlab.payment.transaction.handler.TransactionHandler;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;
import static pl.varlab.payment.AsyncConfig.TRANSACTION_GUARDS_THREAD_POOL_TASK_EXECUTOR;
import static pl.varlab.payment.AsyncConfig.TRANSACTION_PROCESSORS_THREAD_POOL_TASK_EXECUTOR;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;

@SpringBootTest
@SpringJUnitConfig
@ActiveProfiles(profiles = "non-async")
public class TransactionServiceResilienceTests {

    private static final int RETRY_MAX_ATTEMPTS = 3;

    @TestConfiguration
    public static class TestAsyncConfig implements AsyncConfigurer {

        @Bean(name = TRANSACTION_PROCESSORS_THREAD_POOL_TASK_EXECUTOR)
        public Executor transactionProcessorThreadPoolTaskExecutor() {
            return new SimpleAsyncTaskExecutor();
        }

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
    private TransactionFallbackService fallbackService;

    @Autowired
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        reset(transactionHandler, fallbackService);
    }

    @Test
    public void shouldRetryTransactionProcessMaxAttemptTimes_thenRedirectToFallbackService() {
        var transactionRequest = getTransactionRequest();

        doThrow(RuntimeException.class).when(transactionHandler).handle(transactionRequest);

        transactionService.processTransaction(transactionRequest);

        verify(transactionHandler, times(RETRY_MAX_ATTEMPTS)).handle(transactionRequest);
        verify(fallbackService).reportTransactionProcessFailure(transactionRequest);

        verifyNoMoreInteractions(transactionHandler, fallbackService);
    }

    @Test
    public void shouldProcessTransactionAfterFirstRetry() {
        var transactionRequest = getTransactionRequest();

        doThrow(RuntimeException.class).doNothing().when(transactionHandler).handle(transactionRequest);

        transactionService.processTransaction(transactionRequest);

        verify(transactionHandler, times(RETRY_MAX_ATTEMPTS - 1)).handle(transactionRequest);

        verifyNoMoreInteractions(transactionHandler);
        verifyNoInteractions(fallbackService);
    }

}
