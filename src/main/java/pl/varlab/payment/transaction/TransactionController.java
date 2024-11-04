package pl.varlab.payment.transaction;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.varlab.payment.common.BaseController;


@RestController
@RequestMapping("/v1/transactions")
@AllArgsConstructor
public class TransactionController extends BaseController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Bulkhead(name = "transaction-controller")
    public void newTransaction(@RequestBody NewTransactionRequest newTransactionRequest) {
        var newTransaction = newTransactionRequest.newTransactionId();
        transactionService.processTransaction(newTransaction);
    }


    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Bulkhead(name = "transaction-controller")
    public void reprocessTransaction(@RequestBody TransactionRequest transactionRequest) {
        transactionService.processTransaction(transactionRequest);
    }

}
