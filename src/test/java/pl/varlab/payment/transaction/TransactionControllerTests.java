package pl.varlab.payment.transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;


public class TransactionControllerTests extends BaseTransactionControllerTest {

    @Test
    void shouldReturnAcceptedStatus_whenReceivedTransactionRequest() throws Exception {
        var userRequest = getTransactionRequest();
        var transactionRequestJsonBody = MAPPER.writeValueAsString(userRequest);

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJsonBody))
                .andExpect(status().isAccepted())
                .andExpect(content().string(EMPTY));

        verify(transactionService).processTransaction(userRequest);
        verifyNoMoreInteractions(transactionService);
    }

    @CsvSource(value = {
            "{\"transactionId\":\"d8dfcb88-f9b5-4f9d-ba11-7905e9dce4ba\", \"recipientId\":\"acc2\",\"amount\":10.33}                    |SenderId cannot be empty",
            "{\"transactionId\":\"d8dfcb88-f9b5-4f9d-ba11-7905e9dce4ba\", \"senderId\":\"acc1\",\"amount\":10.33}                       |RecipientId cannot be empty",
            "{\"transactionId\":\"d8dfcb88-f9b5-4f9d-ba11-7905e9dce4ba\", \"senderId\":\"acc1\",\"recipientId\":\"acc2\"}               |Amount cannot be empty",
            "{\"transactionId\":\"d8dfcb88-f9b5-4f9d-ba11-7905e9dce4ba\", \"senderId\":\"acc1\",\"recipientId\":\"acc2\",\"amount\":0}  |Amount must be greater than zero",
            "{\"transactionId\":\"d8dfcb88-f9b5-4f9d-ba11-7905e9dce4ba\", \"senderId\":\"acc1\",\"recipientId\":\"acc2\",\"amount\":-1} |Amount must be greater than zero",
            "{\"senderId\":\"acc1\",\"recipientId\":\"acc2\",\"amount\":10.33}                                                          |TransactionId cannot be empty",
    }, delimiter = '|')
    @ParameterizedTest
    void shouldReturnUnprocessableEntityStatus_whenReceivedMalformedTransactionRequest(String malformedRequestBody, String errorMessage) throws Exception {
        var expectedResponseBody = getUnprocessableEntityErrorJsonResponse(errorMessage);

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedRequestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(expectedResponseBody));

        verifyNoInteractions(transactionService);
    }


    @Test
    void shouldReturnInternalServerErrorStatus_whenErrorOccurred() throws Exception {
        var userRequest = getTransactionRequest();
        var transactionRequestJsonBody = MAPPER.writeValueAsString(userRequest);

        doThrow(RuntimeException.class).when(transactionService).processTransaction(any(TransactionRequest.class));

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJsonBody))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(getInternalServerErrorJsonResponse()));

        verify(transactionService).processTransaction(userRequest);
        verifyNoMoreInteractions(transactionService);
    }

}
