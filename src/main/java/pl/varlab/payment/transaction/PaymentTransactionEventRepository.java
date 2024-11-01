package pl.varlab.payment.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.varlab.payment.account.PaymentAccountBalance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PaymentTransactionEventRepository extends JpaRepository<PaymentTransactionEvent, Integer> {

    @Query("""
                SELECT SUM(t.amount) FROM PaymentTransactionEvent t
                    JOIN t.account a
                    WHERE a.name = :accountName
                    GROUP BY a.id
            """)
    Optional<BigDecimal> getAvailableFunds(@Param("accountName") String accountName);

    @Query("""
                SELECT new pl.varlab.payment.account.PaymentAccountBalance(a.name, SUM(t.amount)) FROM PaymentTransactionEvent t
                    JOIN t.account a
                    GROUP BY a.id, a.name
            """)
    List<PaymentAccountBalance> getAllAccountsBalance();

    boolean existsByTransactionIdAndTransactionTypeAndAmount(UUID transactionId, TransactionType transactionType, BigDecimal amount);

    Optional<PaymentTransactionEvent> findByTransactionIdAndTransactionType(UUID transactionId, TransactionType transactionType);

    boolean existsByTransactionIdAndTransactionTypeIn(UUID transactionId, Set<TransactionType> transactionTypes);
}
