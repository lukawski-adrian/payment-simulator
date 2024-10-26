package pl.varlab.payment.transaction;

import java.math.BigDecimal;

final class TransactionTestCommons {

    static final String TRANSACTION_ID = "tx1";

    static TransactionRequest getTransactionRequest() {
        return new TransactionRequest(TRANSACTION_ID, "acc1", "acc2", BigDecimal.valueOf(10.33d));
    }

    private TransactionTestCommons() {
        // prevent initialization
    }
}
