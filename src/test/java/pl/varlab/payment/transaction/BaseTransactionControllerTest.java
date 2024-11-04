package pl.varlab.payment.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import pl.varlab.payment.BaseSpringContextTest;
import pl.varlab.payment.common.ErrorResponse;

import java.net.URI;

import static org.mockito.Mockito.reset;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@SpringJUnitConfig
class BaseTransactionControllerTest extends BaseSpringContextTest {

    static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error";
    static final URI TRANSACTIONS_ENDPOINT = URI.create("/v1/transactions");

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TransactionService transactionService;

    @BeforeEach
    void setUp() {
        reset(transactionService);
    }

    String getInternalServerErrorJsonResponse() throws JsonProcessingException {
        var errorResponse = new ErrorResponse(INTERNAL_SERVER_ERROR.name(), INTERNAL_SERVER_ERROR_MESSAGE);
        return MAPPER.writeValueAsString(errorResponse);
    }

    String getBadRequestErrorJsonResponse(String errorMessage) throws JsonProcessingException {
        var errorResponse = new ErrorResponse(BAD_REQUEST.name(), errorMessage);
        return MAPPER.writeValueAsString(errorResponse);
    }
}
