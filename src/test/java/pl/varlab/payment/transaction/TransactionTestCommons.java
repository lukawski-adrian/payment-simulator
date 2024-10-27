package pl.varlab.payment.transaction;

import java.math.BigDecimal;
import java.util.UUID;

public final class TransactionTestCommons {

    public static final UUID TRANSACTION_ID = UUID.fromString("db69dd8e-3aff-4031-b699-0452bb75095f");

    public static TransactionRequest getTransactionRequest() {
        return new TransactionRequest(TRANSACTION_ID, "acc1", "acc2", BigDecimal.valueOf(10.33d));
    }

    private TransactionTestCommons() {
        // prevent initialization
    }
}
