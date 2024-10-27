package pl.varlab.payment.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PaymentTransactionEventRepository extends JpaRepository<PaymentTransactionEvent, Integer> {

    @Query("""    
                SELECT new pl.varlab.payment.transaction.AccountFunds(a.id, SUM(t.amount)) FROM PaymentTransactionEvent t
                JOIN PaymentAccount a
                WHERE a.name = :accountName GROUP BY a.id HAVING SUM(t.amount) >= :amount
            """)
    boolean hasAvailableFunds(@Param("accountName") String accountName, @Param("amount") BigDecimal amount);

}
