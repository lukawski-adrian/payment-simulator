package pl.varlab.payment.account;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.varlab.payment.common.BaseController;
import pl.varlab.payment.transaction.PaymentTransactionEventRepository;

import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
@AllArgsConstructor
public class PaymentAccountController extends BaseController {

    private final PaymentTransactionEventRepository transactionEventRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PaymentAccountBalance> getAllAccountsBalance() {
        return transactionEventRepository.getAllAccountsBalance();
    }
}
