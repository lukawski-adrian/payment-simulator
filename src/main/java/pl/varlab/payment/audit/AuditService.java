package pl.varlab.payment.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.varlab.payment.transaction.TransactionRequest;
import pl.varlab.payment.transaction.TransactionType;

import static pl.varlab.payment.transaction.TransactionType.DEPOSIT;
import static pl.varlab.payment.transaction.TransactionType.WITHDRAW;

@Service
@Slf4j
public class AuditService {
    public void logWithdraw(TransactionRequest t) {
        log.info("{}|{}|{}|{}|{}",
                t.transactionId(),
                WITHDRAW,
                t.senderId(),
                t.recipientId(),
                t.amount().negate());
    }

    public void logDeposit(TransactionRequest t) {
        log.info("{}|{}|{}|{}|{}",
                t.transactionId(),
                DEPOSIT,
                t.senderId(),
                t.recipientId(),
                t.amount());
    }
}
