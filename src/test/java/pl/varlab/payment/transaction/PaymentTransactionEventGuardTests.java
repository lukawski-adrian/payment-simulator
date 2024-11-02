package pl.varlab.payment.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.guard.FraudDetectedException;

import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;
import static pl.varlab.payment.transaction.TransactionType.DEPOSIT;
import static pl.varlab.payment.transaction.TransactionType.WITHDRAW;

public class PaymentTransactionEventGuardTests {

    private final PaymentTransactionEventRepository paymentTransactionEventRepository = mock(PaymentTransactionEventRepository.class);
    private PaymentTransactionEventGuard paymentTransactionEventGuard;

    @BeforeEach
    void setUp() {
        reset(paymentTransactionEventRepository);
        this.paymentTransactionEventGuard = new PaymentTransactionEventGuard(paymentTransactionEventRepository);
    }

    @Test
    public void shouldAssertConsistentWithdrawTransaction_whenConsistentWithdrawExists() throws FraudDetectedException {
        var tr = getTransactionRequest();
        var paymentEvent = mock(PaymentTransactionEvent.class);

        when(paymentEvent.getAmount()).thenReturn(tr.amount().negate());

        when(paymentTransactionEventRepository.findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW))
                .thenReturn(Optional.of(paymentEvent));

        paymentTransactionEventGuard.assertConsistentWithdraw(tr);

        verify(paymentEvent).getAmount();
        verify(paymentTransactionEventRepository).findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW);
        verifyNoMoreInteractions(paymentTransactionEventRepository, paymentEvent);
    }

    @Test
    public void shouldAssertConsistentWithdrawTransaction_whenConsistentWithdrawNotExists() throws FraudDetectedException {
        var tr = getTransactionRequest();

        when(paymentTransactionEventRepository.findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW))
                .thenReturn(Optional.empty());

        paymentTransactionEventGuard.assertConsistentWithdraw(tr);

        verify(paymentTransactionEventRepository).findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW);
        verifyNoMoreInteractions(paymentTransactionEventRepository);
    }

    @Test
    public void shouldAssertConsistentDepositTransaction_whenConsistentDepositExists() throws FraudDetectedException {
        var tr = getTransactionRequest();
        var paymentEvent = mock(PaymentTransactionEvent.class);

        when(paymentEvent.getAmount()).thenReturn(tr.amount());

        when(paymentTransactionEventRepository.findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT))
                .thenReturn(Optional.of(paymentEvent));

        paymentTransactionEventGuard.assertConsistentDeposit(tr);

        verify(paymentEvent).getAmount();
        verify(paymentTransactionEventRepository).findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT);
        verifyNoMoreInteractions(paymentTransactionEventRepository, paymentEvent);
    }

    @Test
    public void shouldAssertConsistentDepositTransaction_whenConsistentDepositNotExists() throws FraudDetectedException {
        var tr = getTransactionRequest();

        when(paymentTransactionEventRepository.findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT))
                .thenReturn(Optional.empty());

        paymentTransactionEventGuard.assertConsistentDeposit(tr);

        verify(paymentTransactionEventRepository).findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT);
        verifyNoMoreInteractions(paymentTransactionEventRepository);
    }

    @Test
    public void shouldThrowFraudException_whenInconsistentWithdrawTransactionExists() {
        var tr = getTransactionRequest();
        var paymentEvent = mock(PaymentTransactionEvent.class);

        var fraudExceptionMessage = STR."Inconsistent WITHDRAW transaction found for \{tr.transactionId()}";

        when(paymentEvent.getAmount()).thenReturn(tr.amount().subtract(ONE).negate());

        when(paymentTransactionEventRepository.findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW))
                .thenReturn(Optional.of(paymentEvent));

        try {
            paymentTransactionEventGuard.assertConsistentWithdraw(tr);
            fail();
        } catch (FraudDetectedException e) {
            assertEquals(fraudExceptionMessage, e.getMessage());
        }

        verify(paymentEvent).getAmount();
        verify(paymentTransactionEventRepository).findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW);
        verifyNoMoreInteractions(paymentTransactionEventRepository, paymentEvent);
    }

    @Test
    public void shouldThrowFraudException_whenInconsistentDepositTransactionExists() {
        var tr = getTransactionRequest();
        var paymentEvent = mock(PaymentTransactionEvent.class);

        var fraudExceptionMessage = STR."Inconsistent DEPOSIT transaction found for \{tr.transactionId()}";

        when(paymentEvent.getAmount()).thenReturn(tr.amount().add(ONE));

        when(paymentTransactionEventRepository.findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT))
                .thenReturn(Optional.of(paymentEvent));

        try {
            paymentTransactionEventGuard.assertConsistentDeposit(tr);
            fail();
        } catch (FraudDetectedException e) {
            assertEquals(fraudExceptionMessage, e.getMessage());
        }

        verify(paymentEvent).getAmount();
        verify(paymentTransactionEventRepository).findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT);
        verifyNoMoreInteractions(paymentTransactionEventRepository, paymentEvent);
    }


}
