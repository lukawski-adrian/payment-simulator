package pl.varlab.payment.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor
@Setter
@Getter
@Accessors(chain = true)
@Entity(name = "payment_accounts")
public class PaymentAccountEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique = true, nullable = false, length = 64)
    private String name;

}

