package pl.varlab.payment.transaction;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.varlab.payment.PaymentService;
import pl.varlab.payment.common.BaseController;


@RestController
@RequestMapping("/v1/transactions")
@AllArgsConstructor
public class PaymentTransactionController extends BaseController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Bulkhead(name = "transaction-controller")
    public TransactionResponse newTransaction(@RequestBody NewTransactionRequest newTransactionRequest) {
        var newTransaction = newTransactionRequest.newTransactionId();
        paymentService.processTransaction(newTransaction);
        return new TransactionResponse(newTransaction.transactionId());
    }


    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Bulkhead(name = "transaction-controller")
    public void reprocessTransaction(@RequestBody TransactionRequest transactionRequest) {
        paymentService.processTransaction(transactionRequest);
    }

}
