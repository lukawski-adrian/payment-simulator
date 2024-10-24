package pl.varlab.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentService {

    void processPayment(PaymentRequest paymentRequest) {
        log.info("Processing payment request: {}", paymentRequest);
    }

}
