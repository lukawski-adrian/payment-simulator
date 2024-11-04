package pl.varlab.payment.transaction;

import java.math.BigDecimal;
import java.util.UUID;

public final class TransactionTestCommons {

    public static final UUID TRANSACTION_ID = UUID.fromString("db69dd8e-3aff-4031-b699-0452bb75095f");
    public static final String SENDER_ID = "acc1";
    public static final String RECIPIENT_ID = "acc2";
    public static final BigDecimal AMOUNT = BigDecimal.valueOf(10.33d);

    public static TransactionRequest getTransactionRequest() {
        return new TransactionRequest(TRANSACTION_ID, SENDER_ID, RECIPIENT_ID, AMOUNT);
    }

    public static NewTransactionRequest getNewTransactionRequest() {
        return new NewTransactionRequest(SENDER_ID, RECIPIENT_ID, AMOUNT);
    }

    private TransactionTestCommons() {
        // prevent initialization
    }
}
