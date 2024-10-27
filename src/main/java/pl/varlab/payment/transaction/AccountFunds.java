package pl.varlab.payment.transaction;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
public class AccountFunds {
    private Integer accountId;
    private BigDecimal amount;
}
