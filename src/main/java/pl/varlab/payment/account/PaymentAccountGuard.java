package pl.varlab.payment.account;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentAccountGuard {

    private final PaymentAccountRepository paymentAccountRepository;

    public void assertAccountExists(String accountName) throws PaymentAccountNotFoundException {
        if (paymentAccountRepository.existsByName(accountName))
            return;

        throw new PaymentAccountNotFoundException(accountName);
    }
}
