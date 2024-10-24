package pl.varlab.payment;

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

import java.net.URI;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@SpringBootTest
public class PaymentControllerTests {

    private static final String INTERNAL_SERVER_ERROR = "Internal server error";
    private static final String EMPTY = "";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final URI PAYMENTS_ENDPOINT = URI.create("/payments");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        reset(paymentService);
    }

    @Test
    void shouldReturnAcceptedStatusWhenReceivedPaymentRequest() throws Exception {
        var paymentRequest = getPaymentRequest();
        var paymentRequestJsonBody = MAPPER.writeValueAsString(paymentRequest);

        this.mockMvc.perform(post(PAYMENTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequestJsonBody))
                .andExpect(status().isAccepted())
                .andExpect(content().string(EMPTY));

        verify(paymentService).processPayment(paymentRequest);
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    void shouldReturnInternalServerErrorStatusWhenErrorOccurred() throws Exception {
        var paymentRequest = getPaymentRequest();
        var paymentRequestJsonBody = MAPPER.writeValueAsString(paymentRequest);

        doThrow(RuntimeException.class).when(paymentService).processPayment(paymentRequest);

        this.mockMvc.perform(post(PAYMENTS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequestJsonBody))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(getInternalServerErrorJsonResponse()));

        verify(paymentService).processPayment(paymentRequest);
        verifyNoMoreInteractions(paymentService);
    }

    private static PaymentRequest getPaymentRequest() {
        return new PaymentRequest("tx1", "acc1", "acc2", 10.33);
    }

    private String getInternalServerErrorJsonResponse() throws JsonProcessingException {
        var errorMessage = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), INTERNAL_SERVER_ERROR);
        return MAPPER.writeValueAsString(errorMessage);
    }
}
