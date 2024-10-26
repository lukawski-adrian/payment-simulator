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
import pl.varlab.payment.account.AccountNotFoundException;
import pl.varlab.payment.account.AccountService;
import pl.varlab.payment.account.InsufficientFundsException;
import pl.varlab.payment.guard.FraudDetectionGuard;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;
import static pl.varlab.payment.AsyncConfig.TRANSACTION_GUARDS_THREAD_POOL_TASK_EXECUTOR;
import static pl.varlab.payment.AsyncConfig.TRANSACTION_PROCESSORS_THREAD_POOL_TASK_EXECUTOR;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;

@SpringBootTest
@SpringJUnitConfig
@ActiveProfiles(profiles = "non-async")
public class TransactionServiceResilienceTests {

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
    private AccountService accountService;

    @MockBean
    private FraudDetectionGuard fraudDetectionGuard;

    @MockBean
    private TransactionFallbackService fallbackService;

    @Autowired
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        reset(accountService, fraudDetectionGuard, fallbackService);
    }

    @Test
    public void shouldRetryTransactionProcess3Times_thenRedirectToFallbackService() throws InsufficientFundsException, AccountNotFoundException {
        var transactionRequest = getTransactionRequest();

        when(fraudDetectionGuard.assertNotFraud(transactionRequest)).thenReturn(CompletableFuture.failedFuture(new RuntimeException()));

        transactionService.processTransaction(transactionRequest);

        verify(accountService, times(3)).withdraw(transactionRequest);
        verify(fraudDetectionGuard, times(3)).assertNotFraud(transactionRequest);
        verify(fallbackService).reportTransactionProcessFailure(transactionRequest);

        verifyNoMoreInteractions(accountService, fraudDetectionGuard, fallbackService);
    }

}
