package pl.varlab.payment.transfer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;
import pl.varlab.payment.account.PaymentAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@NoArgsConstructor
@Setter
@Getter
@Accessors(chain = true)
@Table(name = "money_transfers")
@Entity
public class MoneyTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "transaction_id", nullable = false, length = 64)
    @JdbcType(VarcharJdbcType.class)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false, length = 64)
    private TransferType transferType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private PaymentAccount account;

}

