package pl.varlab.payment.account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccountEntity, Integer> {
}
