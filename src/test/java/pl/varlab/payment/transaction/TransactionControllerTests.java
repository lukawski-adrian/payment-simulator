package pl.varlab.payment.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.varlab.payment.common.ErrorResponse;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;


public class TransactionControllerTests extends BaseSpringContextTest {

    private static final String INTERNAL_SERVER_ERROR = "Internal server error";
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
    void shouldReturnAcceptedStatusWhenReceivedPaymentRequest() throws Exception {
        var transactionRequest = getTransactionRequest();
        var transactionRequestJsonBody = MAPPER.writeValueAsString(transactionRequest);
        var transactionRequestCaptor = ArgumentCaptor.forClass(TransactionRequest.class);

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJsonBody))
                .andExpect(status().isAccepted())
                .andExpect(content().string(EMPTY));

        verify(transactionService).processTransaction(transactionRequestCaptor.capture());
        verifyNoMoreInteractions(transactionService);

        assertEqualsRequests(transactionRequestCaptor, transactionRequest);
    }

    @Test
    void shouldReturnInternalServerErrorStatusWhenErrorOccurred() throws Exception {
        var transactionRequest = getTransactionRequest();
        var transactionRequestJsonBody = MAPPER.writeValueAsString(transactionRequest);
        var transactionRequestCaptor = ArgumentCaptor.forClass(TransactionRequest.class);

        doThrow(RuntimeException.class).when(transactionService).processTransaction(any(TransactionRequest.class));

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJsonBody))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(getInternalServerErrorJsonResponse()));

        verify(transactionService).processTransaction(transactionRequestCaptor.capture());
        verifyNoMoreInteractions(transactionService);

        assertEqualsRequests(transactionRequestCaptor, transactionRequest);
    }

    private String getInternalServerErrorJsonResponse() throws JsonProcessingException {
        var errorMessage = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), INTERNAL_SERVER_ERROR);
        return MAPPER.writeValueAsString(errorMessage);
    }

    private static void assertEqualsRequests(ArgumentCaptor<TransactionRequest> transactionRequestCaptor, TransactionRequest transactionRequest) {
        var capturedRequest = getOnlyElement(transactionRequestCaptor.getAllValues());

        assertNotEquals(transactionRequest.transactionId(), capturedRequest.transactionId());
        assertEquals(transactionRequest.amount(), capturedRequest.amount());
        assertEquals(transactionRequest.senderId(), capturedRequest.senderId());
        assertEquals(transactionRequest.recipientId(), capturedRequest.recipientId());
    }
}
