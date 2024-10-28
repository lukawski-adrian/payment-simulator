package pl.varlab.payment.common;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.*;

@Slf4j
public class BaseController {

    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error";

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse internalServerError(RuntimeException e) {
        log.error("Unexpected error", e);
        return new ErrorResponse(INTERNAL_SERVER_ERROR.name(), INTERNAL_SERVER_ERROR_MESSAGE);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    public ErrorResponse unprocessableEntity(HttpMessageNotReadableException e) {
        var cause = getRootCause(e);
        return new ErrorResponse(UNPROCESSABLE_ENTITY.name(), cause.getMessage());
    }

    @ExceptionHandler(BulkheadFullException.class)
    @ResponseStatus(TOO_MANY_REQUESTS)
    public ErrorResponse tooManyRequests(BulkheadFullException e) {
        return new ErrorResponse(TOO_MANY_REQUESTS.name(), e.getMessage());
    }

    private static Throwable getRootCause(Throwable throwable) {
        while (throwable.getCause() != null)
            throwable = throwable.getCause();

        return throwable;
    }
}
