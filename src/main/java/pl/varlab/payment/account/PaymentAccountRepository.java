package pl.varlab.payment.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Integer> {

    Optional<PaymentAccount> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);
}
