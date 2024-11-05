package pl.varlab.payment.account;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentAccountGuard {

    private final PaymentAccountRepository paymentAccountRepository;

    public void assertAccountExists(String accountNumber) throws PaymentAccountNotFoundException {
        if (paymentAccountRepository.existsByAccountNumber(accountNumber))
            return;

        throw new PaymentAccountNotFoundException(accountNumber);
    }
}
