package pl.varlab.payment.account;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor
@Setter
@Getter
@Accessors(chain = true)
@Table(name = "payment_accounts")
@Entity
public class PaymentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account_number", unique = true, nullable = false, length = 64)
    private String accountNumber;

}

