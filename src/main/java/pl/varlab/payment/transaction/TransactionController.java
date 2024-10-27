package pl.varlab.payment.transaction;

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
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void newTransaction(@RequestBody TransactionRequest transactionRequest) {
        var newTransactionRequest = transactionRequest.newTransactionId();
        transactionService.processTransaction(newTransactionRequest);
    }

}
