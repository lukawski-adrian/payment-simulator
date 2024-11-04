package pl.varlab.payment.transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.varlab.payment.transaction.TransactionTestCommons.getNewTransactionRequest;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;


public class TransactionControllerTests extends BaseTransactionControllerTest {

    @Test
    void shouldReturnCreatedStatus_whenReceivedNewTransactionRequest() throws Exception {
        var userRequest = getNewTransactionRequest();
        var transactionRequestJsonBody = MAPPER.writeValueAsString(userRequest);
        var requestCaptor = ArgumentCaptor.forClass(TransactionRequest.class);

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJsonBody))
                .andExpect(status().isCreated())
                .andExpect(content().string(EMPTY));

        verify(transactionService).processTransaction(requestCaptor.capture());
        verifyNoMoreInteractions(transactionService);

        assertTransactionRequest(requestCaptor, userRequest);
    }

    @Test
    void shouldReturnNoContentStatus_whenReceivedTransactionRequest() throws Exception {
        var userRequest = getTransactionRequest();
        var transactionRequestJsonBody = MAPPER.writeValueAsString(userRequest);

        this.mockMvc.perform(put(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJsonBody))
                .andExpect(status().isNoContent())
                .andExpect(content().string(EMPTY));

        verify(transactionService).processTransaction(userRequest);
        verifyNoMoreInteractions(transactionService);
    }

    @CsvSource(value = {
            "{ \"recipientId\":\"acc2\",\"amount\":10.33}                        |SenderId cannot be empty",
            "{ \"senderId\":\"acc1\", \"recipientId\":\"acc1\",\"amount\":10.33} |RecipientId and SenderId cannot be the same",
            "{ \"senderId\":\"acc1\",\"amount\":10.33}                           |RecipientId cannot be empty",
            "{ \"senderId\":\"acc1\",\"recipientId\":\"acc2\"}                   |Amount cannot be empty",
            "{ \"senderId\":\"acc1\",\"recipientId\":\"acc2\",\"amount\":0}      |Amount must be greater than zero",
            "{ \"senderId\":\"acc1\",\"recipientId\":\"acc2\",\"amount\":-1}     |Amount must be greater than zero"
    }, delimiter = '|')
    @ParameterizedTest
    void shouldReturnBadRequestStatus_whenReceivedMalformedNewTransactionRequest(String malformedRequestBody, String errorMessage) throws Exception {
        var expectedResponseBody = getBadRequestErrorJsonResponse(errorMessage);

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponseBody));

        verifyNoInteractions(transactionService);
    }


    @CsvSource(value = {
            "{\"transactionId\":\"d8dfcb88-f9b5-4f9d-ba11-7905e9dce4ba\", \"recipientId\":\"acc2\",\"amount\":10.33}                        |SenderId cannot be empty",
            "{\"transactionId\":\"d8dfcb88-f9b5-4f9d-ba11-7905e9dce4ba\", \"senderId\":\"acc1\", \"recipientId\":\"acc1\",\"amount\":10.33} |RecipientId and SenderId cannot be the same",
            "{\"transactionId\":\"d8dfcb88-f9b5-4f9d-ba11-7905e9dce4ba\", \"senderId\":\"acc1\",\"amount\":10.33}                           |RecipientId cannot be empty",
            "{\"transactionId\":\"d8dfcb88-f9b5-4f9d-ba11-7905e9dce4ba\", \"senderId\":\"acc1\",\"recipientId\":\"acc2\"}                   |Amount cannot be empty",
            "{\"transactionId\":\"d8dfcb88-f9b5-4f9d-ba11-7905e9dce4ba\", \"senderId\":\"acc1\",\"recipientId\":\"acc2\",\"amount\":0}      |Amount must be greater than zero",
            "{\"transactionId\":\"d8dfcb88-f9b5-4f9d-ba11-7905e9dce4ba\", \"senderId\":\"acc1\",\"recipientId\":\"acc2\",\"amount\":-1}     |Amount must be greater than zero",
            "{\"senderId\":\"acc1\",\"recipientId\":\"acc2\",\"amount\":10.33}                                                              |TransactionId cannot be empty",
    }, delimiter = '|')
    @ParameterizedTest
    void shouldReturnBadRequestStatus_whenReceivedMalformedTransactionRequest(String malformedRequestBody, String errorMessage) throws Exception {
        var expectedResponseBody = getBadRequestErrorJsonResponse(errorMessage);

        this.mockMvc.perform(put(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponseBody));

        verifyNoInteractions(transactionService);
    }


    @Test
    void shouldReturnInternalServerErrorStatus_whenErrorOccurredOnNewTransactionRequest() throws Exception {
        var userRequest = getNewTransactionRequest();
        var transactionRequestJsonBody = MAPPER.writeValueAsString(userRequest);
        var requestCaptor = ArgumentCaptor.forClass(TransactionRequest.class);

        doThrow(RuntimeException.class).when(transactionService).processTransaction(any(TransactionRequest.class));

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJsonBody))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(getInternalServerErrorJsonResponse()));

        verify(transactionService).processTransaction(requestCaptor.capture());
        verifyNoMoreInteractions(transactionService);

        assertTransactionRequest(requestCaptor, userRequest);
    }

    @Test
    void shouldReturnInternalServerErrorStatus_whenErrorOccurredOnTransactionRequest() throws Exception {
        var userRequest = getTransactionRequest();
        var transactionRequestJsonBody = MAPPER.writeValueAsString(userRequest);

        doThrow(RuntimeException.class).when(transactionService).processTransaction(userRequest);

        this.mockMvc.perform(put(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJsonBody))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(getInternalServerErrorJsonResponse()));

        verify(transactionService).processTransaction(userRequest);
        verifyNoMoreInteractions(transactionService);
    }

    private static void assertTransactionRequest(ArgumentCaptor<TransactionRequest> requestCaptor, NewTransactionRequest userRequest) {
        List<TransactionRequest> requests = requestCaptor.getAllValues();
        assertEquals(1, requests.size());
        var request = requests.getFirst();
        assertEquals(userRequest.senderId(), request.senderId());
        assertEquals(userRequest.recipientId(), request.recipientId());
        assertEquals(userRequest.amount(), request.amount());
    }

}
