package pl.varlab.payment.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionEventRepository extends JpaRepository<PaymentTransactionEvent, Integer> {

    @Query("""
                SELECT SUM(t.amount) FROM PaymentTransactionEvent t
                    JOIN t.account a
                    WHERE a.name = :accountName
                    GROUP BY a.id
            """)
    Optional<BigDecimal> getAvailableFunds(@Param("accountName") String accountName);

    boolean existsByTransactionIdAndTransactionTypeAndAmount(UUID transactionId, TransactionType transactionType, BigDecimal amount);
}
