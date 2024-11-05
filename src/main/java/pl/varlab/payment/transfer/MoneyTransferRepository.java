package pl.varlab.payment.transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.varlab.payment.account.PaymentAccountBalance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MoneyTransferRepository extends JpaRepository<MoneyTransfer, Integer> {

    @Query("""
                SELECT SUM(t.amount) FROM MoneyTransfer t
                    JOIN t.account a
                    WHERE a.accountNumber = :accountName
                    GROUP BY a.id
            """)
    Optional<BigDecimal> getAvailableFunds(@Param("accountName") String accountName);

    @Query("""
                SELECT new pl.varlab.payment.account.PaymentAccountBalance(a.accountNumber, SUM(t.amount)) FROM MoneyTransfer t
                    JOIN t.account a
                    GROUP BY a.id, a.accountNumber
            """)
    List<PaymentAccountBalance> getAllAccountsBalance();

    boolean existsByTransactionIdAndTransferTypeAndAmount(UUID transactionId, TransferType transferType, BigDecimal amount);

    Optional<MoneyTransfer> findByTransactionIdAndTransferType(UUID transactionId, TransferType transferType);

}
