package pl.varlab.payment.account;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import pl.varlab.payment.transaction.PaymentTransactionEventRepository;
import pl.varlab.payment.transaction.TransactionRequest;

@Component
@AllArgsConstructor
public class PaymentAccountGuard {

    private final PaymentTransactionEventRepository paymentTransactionEventRepository;

    public void assertAvailableFunds(TransactionRequest tr) throws InsufficientFundsException {
        var senderId = tr.senderId();
        var amount = tr.amount();

        if (paymentTransactionEventRepository.hasAvailableFunds(senderId, amount))
            return;

        throw new InsufficientFundsException();
    }

}
