package pl.varlab.payment.transaction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import pl.varlab.payment.account.PaymentAccount;

import java.math.BigDecimal;
import java.sql.Timestamp;


@NoArgsConstructor
@Setter
@Getter
@Accessors(chain = true)
@Table(name = "payment_transaction_events")
@Entity
public class PaymentTransactionEvent {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "transaction_id", nullable = false, length = 64)
    private String transactionId;

    @Column(name = "transaction_type", nullable = false, length = 64)
    private TransactionType transactionType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_on", nullable = false)
    private Timestamp createdOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private PaymentAccount account;

}

