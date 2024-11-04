package pl.varlab.payment.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.varlab.payment.transfer.MoneyTransfer;
import pl.varlab.payment.transfer.MoneyTransferGuard;
import pl.varlab.payment.transfer.MoneyTransferRepository;
import pl.varlab.payment.guard.FraudDetectedException;

import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static pl.varlab.payment.transaction.TransactionTestCommons.getTransactionRequest;
import static pl.varlab.payment.transaction.TransferType.DEPOSIT;
import static pl.varlab.payment.transaction.TransferType.WITHDRAW;

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

        when(moneyTransferRepository.findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW))
                .thenReturn(Optional.of(paymentEvent));

        paymentTransactionEventGuard.assertConsistentWithdraw(tr);

        verify(paymentEvent).getAmount();
        verify(moneyTransferRepository).findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW);
        verifyNoMoreInteractions(moneyTransferRepository, paymentEvent);
    }

    @Test
    public void shouldAssertConsistentWithdrawTransaction_whenConsistentWithdrawNotExists() throws FraudDetectedException {
        var tr = getTransactionRequest();

        when(moneyTransferRepository.findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW))
                .thenReturn(Optional.empty());

        paymentTransactionEventGuard.assertConsistentWithdraw(tr);

        verify(moneyTransferRepository).findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW);
        verifyNoMoreInteractions(moneyTransferRepository);
    }

    @Test
    public void shouldAssertConsistentDepositTransaction_whenConsistentDepositExists() throws FraudDetectedException {
        var tr = getTransactionRequest();
        var paymentEvent = mock(MoneyTransfer.class);

        when(paymentEvent.getAmount()).thenReturn(tr.amount());

        when(moneyTransferRepository.findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT))
                .thenReturn(Optional.of(paymentEvent));

        paymentTransactionEventGuard.assertConsistentDeposit(tr);

        verify(paymentEvent).getAmount();
        verify(moneyTransferRepository).findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT);
        verifyNoMoreInteractions(moneyTransferRepository, paymentEvent);
    }

    @Test
    public void shouldAssertConsistentDepositTransaction_whenConsistentDepositNotExists() throws FraudDetectedException {
        var tr = getTransactionRequest();

        when(moneyTransferRepository.findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT))
                .thenReturn(Optional.empty());

        paymentTransactionEventGuard.assertConsistentDeposit(tr);

        verify(moneyTransferRepository).findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT);
        verifyNoMoreInteractions(moneyTransferRepository);
    }

    @Test
    public void shouldThrowFraudException_whenInconsistentWithdrawTransactionExists() {
        var tr = getTransactionRequest();
        var paymentEvent = mock(MoneyTransfer.class);

        var fraudExceptionMessage = STR."Inconsistent WITHDRAW transaction found for \{tr.transactionId()}";

        when(paymentEvent.getAmount()).thenReturn(tr.amount().subtract(ONE).negate());

        when(moneyTransferRepository.findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW))
                .thenReturn(Optional.of(paymentEvent));

        try {
            paymentTransactionEventGuard.assertConsistentWithdraw(tr);
            fail();
        } catch (FraudDetectedException e) {
            assertEquals(fraudExceptionMessage, e.getMessage());
        }

        verify(paymentEvent).getAmount();
        verify(moneyTransferRepository).findByTransactionIdAndTransactionType(tr.transactionId(), WITHDRAW);
        verifyNoMoreInteractions(moneyTransferRepository, paymentEvent);
    }

    @Test
    public void shouldThrowFraudException_whenInconsistentDepositTransactionExists() {
        var tr = getTransactionRequest();
        var paymentEvent = mock(MoneyTransfer.class);

        var fraudExceptionMessage = STR."Inconsistent DEPOSIT transaction found for \{tr.transactionId()}";

        when(paymentEvent.getAmount()).thenReturn(tr.amount().add(ONE));

        when(moneyTransferRepository.findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT))
                .thenReturn(Optional.of(paymentEvent));

        try {
            paymentTransactionEventGuard.assertConsistentDeposit(tr);
            fail();
        } catch (FraudDetectedException e) {
            assertEquals(fraudExceptionMessage, e.getMessage());
        }

        verify(paymentEvent).getAmount();
        verify(moneyTransferRepository).findByTransactionIdAndTransactionType(tr.transactionId(), DEPOSIT);
        verifyNoMoreInteractions(moneyTransferRepository, paymentEvent);
    }


}
