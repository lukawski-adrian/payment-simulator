package pl.varlab.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.varlab.payment.common.ConflictDataException;
import pl.varlab.payment.transaction.TransactionRequest;

import java.math.BigDecimal;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pl.varlab.payment.transfer.TransferType.DEPOSIT;
import static pl.varlab.payment.transfer.TransferType.WITHDRAW;


public class PaymentServiceTests extends BaseJpaTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    public void shouldProcessTransaction_thenTransferFunds() {
        var senderAccountNumber = "ACC1";
        var recipientAccountNumber = "ACC2";
        var amount = BigDecimal.valueOf(20.33d);

        var transactionId = UUID.randomUUID();
        var transactionRequest = new TransactionRequest(transactionId, senderAccountNumber, recipientAccountNumber, amount);

        var senderAccountBalanceBefore = getAccountBalance(senderAccountNumber);
        var recipientAccountBalanceBefore = getAccountBalance(recipientAccountNumber);

        assertThat(isAnyTransferExists(transactionId)).isFalse();
        assertThat(senderAccountBalanceBefore).isGreaterThan(amount);
        assertThat(recipientAccountBalanceBefore).isGreaterThan(ZERO);

        paymentService.processTransaction(transactionRequest);

        var senderAccountBalanceAfter = getAccountBalance(senderAccountNumber);
        var recipientAccountBalanceAfter = getAccountBalance(recipientAccountNumber);

        assertThat(isTransferExists(transactionId, WITHDRAW)).isTrue();
        assertThat(isTransferExists(transactionId, DEPOSIT)).isTrue();
        assertThat(senderAccountBalanceAfter).isEqualTo(senderAccountBalanceBefore.subtract(amount));
        assertThat(recipientAccountBalanceAfter).isEqualTo(recipientAccountBalanceBefore.add(amount));
    }

    @Test
    public void shouldBlockTransaction_whenFraudExceptionOccurred() {
        var senderAccountNumber = "ACC2";
        var recipientAccountNumber = "ACC1";
        var amount = BigDecimal.valueOf(11);

        var transactionId = UUID.randomUUID();
        var transactionRequest = new TransactionRequest(transactionId, senderAccountNumber, recipientAccountNumber, amount);

        var senderAccountBalanceBefore = getAccountBalance(senderAccountNumber);
        var recipientAccountBalanceBefore = getAccountBalance(recipientAccountNumber);

        assertThat(isAnyTransferExists(transactionId)).isFalse();
        assertThat(senderAccountBalanceBefore).isGreaterThan(amount);
        assertThat(recipientAccountBalanceBefore).isGreaterThan(ZERO);

        assertThrows(ConflictDataException.class, () -> paymentService.processTransaction(transactionRequest), "Fraud detected (divisor 11)");

        var senderAccountBalanceAfter = getAccountBalance(senderAccountNumber);
        var recipientAccountBalanceAfter = getAccountBalance(recipientAccountNumber);

        assertThat(isTransferExists(transactionId, WITHDRAW)).isTrue();
        assertThat(isTransferExists(transactionId, DEPOSIT)).isFalse();
        assertThat(senderAccountBalanceAfter).isEqualTo(senderAccountBalanceBefore.subtract(amount));
        assertThat(recipientAccountBalanceAfter).isEqualTo(recipientAccountBalanceBefore);
    }


}
