package pl.varlab.payment.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.stubbing.Answer;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;


@Slf4j
public class TransactionControllerResilienceTests extends BaseTransactionControllerTest {

    private static final int MAX_CONCURRENT_CALLS = 2;
    private static final int HTTP_OK_STATUS = 200;
    private static final int HTTP_TOO_MANY_REQUESTS_STATUS = 429;


    @ParameterizedTest
    @CsvSource({"2,0", "3,1", "2,0"})
    public void shouldProcessMaxConcurrentRequestsAndReturnTooManyRequestsForRest(int numberOfRequests, int expectedBlockedRequests) throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {

        doAnswer(transactionProcessingDelay()).when(transactionService).processTransaction(any(TransactionRequest.class));

        var userRequest = getTransactionRequest();
        var transactionRequestJsonBody = MAPPER.writeValueAsString(userRequest);

        var responseStatuses = executeConcurrentRequests(numberOfRequests, transactionRequestJsonBody);

        assertResponseStatuses(expectedBlockedRequests, responseStatuses);

        verify(transactionService, times(MAX_CONCURRENT_CALLS)).processTransaction(userRequest);
        verifyNoMoreInteractions(transactionService);
    }

    private static void assertResponseStatuses(int expectedNumberOfBlockedRequests, List<CompletableFuture<Integer>> responseStatuses) throws InterruptedException, ExecutionException, TimeoutException {
        int countProcessed = 0;
        int countBlocked = 0;
        int countOthers = 0;
        for (var httpResponseStatus : responseStatuses)
            switch (httpResponseStatus.get(5, TimeUnit.SECONDS)) {
                case HTTP_OK_STATUS -> countProcessed++;
                case HTTP_TOO_MANY_REQUESTS_STATUS -> countBlocked++;
                default -> countOthers++;
            }

        assertEquals(MAX_CONCURRENT_CALLS, countProcessed);
        assertEquals(expectedNumberOfBlockedRequests, countBlocked);
        assertEquals(0, countOthers);
    }

    private ArrayList<CompletableFuture<Integer>> executeConcurrentRequests(int concurrentRequests, String userRequest) {
        var requestResults = new ArrayList<CompletableFuture<Integer>>(concurrentRequests);
        var executor = Executors.newFixedThreadPool(concurrentRequests);
        for (int i = 0; i < concurrentRequests; i++)
            requestResults.add(CompletableFuture.supplyAsync(transactionRequestSupplier(userRequest), executor));
        return requestResults;
    }

    private static Answer<Void> transactionProcessingDelay() {
        return _ -> {
            try {
                Thread.sleep(500);
                return null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Supplier<Integer> transactionRequestSupplier(String userRequest) {
        return () -> {
            try {
                return this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(userRequest)).andReturn().getResponse().getStatus();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
