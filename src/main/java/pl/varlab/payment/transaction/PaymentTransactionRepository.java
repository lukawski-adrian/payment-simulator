package pl.varlab.payment.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {

    boolean existsByTransactionIdAndTransactionType(UUID transactionId, PaymentTransactionType transactionType);

}
