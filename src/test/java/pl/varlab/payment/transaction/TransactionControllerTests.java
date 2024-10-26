package pl.varlab.payment.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.varlab.payment.common.ErrorResponse;

import java.math.BigDecimal;
import java.net.URI;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@SpringBootTest
public class TransactionControllerTests {

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
        var paymentRequest = getTransactionRequest();
        var paymentRequestJsonBody = MAPPER.writeValueAsString(paymentRequest);

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequestJsonBody))
                .andExpect(status().isAccepted())
                .andExpect(content().string(EMPTY));

        verify(transactionService).processTransaction(paymentRequest);
        verifyNoMoreInteractions(transactionService);
    }

    @Test
    void shouldReturnInternalServerErrorStatusWhenErrorOccurred() throws Exception {
        var paymentRequest = getTransactionRequest();
        var paymentRequestJsonBody = MAPPER.writeValueAsString(paymentRequest);

        doThrow(RuntimeException.class).when(transactionService).processTransaction(paymentRequest);

        this.mockMvc.perform(post(TRANSACTIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequestJsonBody))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(getInternalServerErrorJsonResponse()));

        verify(transactionService).processTransaction(paymentRequest);
        verifyNoMoreInteractions(transactionService);
    }

    private static TransactionRequest getTransactionRequest() {
        return new TransactionRequest("tx1", "acc1", "acc2", BigDecimal.valueOf(10.33d));
    }

    private String getInternalServerErrorJsonResponse() throws JsonProcessingException {
        var errorMessage = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), INTERNAL_SERVER_ERROR);
        return MAPPER.writeValueAsString(errorMessage);
    }
}
