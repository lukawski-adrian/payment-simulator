package pl.varlab.payment.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import pl.varlab.payment.BaseSpringContextTest;
import pl.varlab.payment.PaymentService;
import pl.varlab.payment.common.ErrorResponse;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@SpringJUnitConfig
class BasePaymentTransactionControllerTest extends BaseSpringContextTest {

    static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error";
    static final URI TRANSACTIONS_ENDPOINT = URI.create("/v1/transactions");

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PaymentService paymentService;

    @BeforeEach
    void setUp() {
        reset(paymentService);
    }

    static String getInternalServerErrorJsonResponse() throws JsonProcessingException {
        var errorResponse = new ErrorResponse(INTERNAL_SERVER_ERROR.name(), INTERNAL_SERVER_ERROR_MESSAGE);
        return MAPPER.writeValueAsString(errorResponse);
    }

    static String getBadRequestErrorJsonResponse(String errorMessage) throws JsonProcessingException {
        var errorResponse = new ErrorResponse(BAD_REQUEST.name(), errorMessage);
        return MAPPER.writeValueAsString(errorResponse);
    }

    static void assertTransactionRequest(ArgumentCaptor<TransactionRequest> requestCaptor, NewTransactionRequest userRequest) {
        List<TransactionRequest> requests = requestCaptor.getAllValues();
        assertEquals(1, requests.size());
        var request = requests.getFirst();
        assertEquals(userRequest.senderAccountNumber(), request.senderAccountNumber());
        assertEquals(userRequest.recipientAccountNumber(), request.recipientAccountNumber());
        assertEquals(userRequest.amount(), request.amount());
    }
}
