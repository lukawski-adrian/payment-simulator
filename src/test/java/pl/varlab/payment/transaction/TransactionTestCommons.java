package pl.varlab.payment.transaction;

import java.math.BigDecimal;

public final class TransactionTestCommons {

    public static final String TRANSACTION_ID = "tx1";

    public static TransactionRequest getTransactionRequest() {
        return new TransactionRequest(TRANSACTION_ID, "acc1", "acc2", BigDecimal.valueOf(10.33d));
    }

    private TransactionTestCommons() {
        // prevent initialization
    }
}
