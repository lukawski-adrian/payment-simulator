package pl.varlab.payment;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pl.varlab.payment.transaction.CustomAsyncExceptionHandler;

import java.util.concurrent.Executor;


@Configuration
@EnableAsync
@Profile("!test")
public class AsyncConfig implements AsyncConfigurer {

    public static final String TRANSACTION_GUARDS_THREAD_POOL_TASK_EXECUTOR = "transactionGuardsThreadPoolTaskExecutor";

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    @Bean(name = TRANSACTION_GUARDS_THREAD_POOL_TASK_EXECUTOR)
    public Executor transactionGuardThreadPoolTaskExecutor() {
        var threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("transaction-guards-");
        threadPoolTaskExecutor.setCorePoolSize(50);
        threadPoolTaskExecutor.setMaxPoolSize(500);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    @Override
    public Executor getAsyncExecutor() {
        var threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

}
