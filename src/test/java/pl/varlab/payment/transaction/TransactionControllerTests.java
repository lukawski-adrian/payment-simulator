package pl.varlab.payment.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.varlab.payment.common.ErrorResponse;

import java.net.URI;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.varlab.payment.transaction.TransactionTestCommons.getUserTransactionRequest;


public class TransactionControllerTests extends BaseSpringContextTest {

    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error";
    private static final String EMPTY = "";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final URI TRANSACTIONS_ENDPOINT = URI.create("/v1/transactions");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        reset(transactionService);
    }


    @Test
    void shouldReturnAcceptedStatus_whenReceivedTransactionRequest() throws Exception {
        var userRequest = getUserTransactionRequest();
        var transactionRequestJsonBody = MAPPER.writeValueAsString(userRequest);
        var transactionRequestCaptor = ArgumentCaptor.forClass(TransactionRequest.class);

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJsonBody))
                .andExpect(status().isAccepted())
                .andExpect(content().string(EMPTY));

        verify(transactionService).processTransaction(transactionRequestCaptor.capture());
        verifyNoMoreInteractions(transactionService);

        assertEqualsRequests(transactionRequestCaptor, userRequest);
    }


    @CsvSource(value = {
            "{\"recipientId\":\"acc2\",\"amount\":10.33}|SenderId cannot be empty",
            "{\"senderId\":\"acc1\",\"amount\":10.33}|RecipientId cannot be empty",
            "{\"senderId\":\"acc1\",\"recipientId\":\"acc2\"}|Amount cannot be empty",
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
        var userRequest = getUserTransactionRequest();
        var transactionRequestJsonBody = MAPPER.writeValueAsString(userRequest);
        var transactionRequestCaptor = ArgumentCaptor.forClass(TransactionRequest.class);

        doThrow(RuntimeException.class).when(transactionService).processTransaction(any(TransactionRequest.class));

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJsonBody))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(getInternalServerErrorJsonResponse()));

        verify(transactionService).processTransaction(transactionRequestCaptor.capture());
        verifyNoMoreInteractions(transactionService);

        assertEqualsRequests(transactionRequestCaptor, userRequest);
    }

    private String getInternalServerErrorJsonResponse() throws JsonProcessingException {
        var errorResponse = new ErrorResponse(INTERNAL_SERVER_ERROR.name(), INTERNAL_SERVER_ERROR_MESSAGE);
        return MAPPER.writeValueAsString(errorResponse);
    }

    private String getUnprocessableEntityErrorJsonResponse(String errorMessage) throws JsonProcessingException {
        var errorResponse = new ErrorResponse(UNPROCESSABLE_ENTITY.name(), errorMessage);
        return MAPPER.writeValueAsString(errorResponse);
    }

    private static void assertEqualsRequests(ArgumentCaptor<TransactionRequest> transactionRequestCaptor,
                                             UserTransactionRequest transactionRequest) {
        var capturedRequest = getOnlyElement(transactionRequestCaptor.getAllValues());

        assertEquals(transactionRequest.amount(), capturedRequest.amount());
        assertEquals(transactionRequest.senderId(), capturedRequest.senderId());
        assertEquals(transactionRequest.recipientId(), capturedRequest.recipientId());
    }
}
