package pl.varlab.payment.transfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.transaction.guard.FraudDetectedException;

import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static pl.varlab.payment.transaction.PaymentTransactionTestCommons.getTransactionRequest;
import static pl.varlab.payment.transfer.TransferType.DEPOSIT;
import static pl.varlab.payment.transfer.TransferType.WITHDRAW;

public class MoneyTransferGuardTests {

    private final MoneyTransferRepository moneyTransferRepository = mock(MoneyTransferRepository.class);
    private MoneyTransferGuard paymentTransactionEventGuard;

    @BeforeEach
    void setUp() {
        reset(moneyTransferRepository);
        this.paymentTransactionEventGuard = new MoneyTransferGuard(moneyTransferRepository);
    }

    @Test
    public void shouldAssertConsistentWithdrawTransaction_whenConsistentWithdrawExists() throws FraudDetectedException {
        var tr = getTransactionRequest();
        var paymentEvent = mock(MoneyTransfer.class);

        when(paymentEvent.getAmount()).thenReturn(tr.amount().negate());

        when(moneyTransferRepository.findByTransactionIdAndTransferType(tr.transactionId(), WITHDRAW))
                .thenReturn(Optional.of(paymentEvent));

        paymentTransactionEventGuard.assertConsistentWithdraw(tr);

        verify(paymentEvent).getAmount();
        verify(moneyTransferRepository).findByTransactionIdAndTransferType(tr.transactionId(), WITHDRAW);
        verifyNoMoreInteractions(moneyTransferRepository, paymentEvent);
    }

    @Test
    public void shouldAssertConsistentWithdrawTransaction_whenConsistentWithdrawNotExists() throws FraudDetectedException {
        var tr = getTransactionRequest();

        when(moneyTransferRepository.findByTransactionIdAndTransferType(tr.transactionId(), WITHDRAW))
                .thenReturn(Optional.empty());

        paymentTransactionEventGuard.assertConsistentWithdraw(tr);

        verify(moneyTransferRepository).findByTransactionIdAndTransferType(tr.transactionId(), WITHDRAW);
        verifyNoMoreInteractions(moneyTransferRepository);
    }

    @Test
    public void shouldAssertConsistentDepositTransaction_whenConsistentDepositExists() throws FraudDetectedException {
        var tr = getTransactionRequest();
        var paymentEvent = mock(MoneyTransfer.class);

        when(paymentEvent.getAmount()).thenReturn(tr.amount());

        when(moneyTransferRepository.findByTransactionIdAndTransferType(tr.transactionId(), DEPOSIT))
                .thenReturn(Optional.of(paymentEvent));

        paymentTransactionEventGuard.assertConsistentDeposit(tr);

        verify(paymentEvent).getAmount();
        verify(moneyTransferRepository).findByTransactionIdAndTransferType(tr.transactionId(), DEPOSIT);
        verifyNoMoreInteractions(moneyTransferRepository, paymentEvent);
    }

    @Test
    public void shouldAssertConsistentDepositTransaction_whenConsistentDepositNotExists() throws FraudDetectedException {
        var tr = getTransactionRequest();

        when(moneyTransferRepository.findByTransactionIdAndTransferType(tr.transactionId(), DEPOSIT))
                .thenReturn(Optional.empty());

        paymentTransactionEventGuard.assertConsistentDeposit(tr);

        verify(moneyTransferRepository).findByTransactionIdAndTransferType(tr.transactionId(), DEPOSIT);
        verifyNoMoreInteractions(moneyTransferRepository);
    }

    @Test
    public void shouldThrowFraudException_whenInconsistentWithdrawTransactionExists() {
        var tr = getTransactionRequest();
        var paymentEvent = mock(MoneyTransfer.class);

        var fraudExceptionMessage = STR."Inconsistent WITHDRAW transaction found for \{tr.transactionId()}";

        when(paymentEvent.getAmount()).thenReturn(tr.amount().subtract(ONE).negate());

        when(moneyTransferRepository.findByTransactionIdAndTransferType(tr.transactionId(), WITHDRAW))
                .thenReturn(Optional.of(paymentEvent));

        assertThrows(FraudDetectedException.class, () -> paymentTransactionEventGuard.assertConsistentWithdraw(tr), fraudExceptionMessage);

        verify(paymentEvent).getAmount();
        verify(moneyTransferRepository).findByTransactionIdAndTransferType(tr.transactionId(), WITHDRAW);
        verifyNoMoreInteractions(moneyTransferRepository, paymentEvent);
    }

    @Test
    public void shouldThrowFraudException_whenInconsistentDepositTransactionExists() {
        var tr = getTransactionRequest();
        var paymentEvent = mock(MoneyTransfer.class);

        var fraudExceptionMessage = STR."Inconsistent DEPOSIT transaction found for \{tr.transactionId()}";

        when(paymentEvent.getAmount()).thenReturn(tr.amount().add(ONE));

        when(moneyTransferRepository.findByTransactionIdAndTransferType(tr.transactionId(), DEPOSIT))
                .thenReturn(Optional.of(paymentEvent));

        assertThrows(FraudDetectedException.class, () -> paymentTransactionEventGuard.assertConsistentDeposit(tr), fraudExceptionMessage);

        verify(paymentEvent).getAmount();
        verify(moneyTransferRepository).findByTransactionIdAndTransferType(tr.transactionId(), DEPOSIT);
        verifyNoMoreInteractions(moneyTransferRepository, paymentEvent);
    }


}
