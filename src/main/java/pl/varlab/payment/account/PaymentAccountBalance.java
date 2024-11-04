package pl.varlab.payment.account;

import java.math.BigDecimal;

public record PaymentAccountBalance(String accountNumber, BigDecimal balance) {
}
