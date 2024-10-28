package pl.varlab.payment.account;


public class InsufficientFundsException extends Exception {

    private static final String INSUFFICIENT_FUNDS = "Insufficient Funds";

    public InsufficientFundsException() {
        super(INSUFFICIENT_FUNDS);
    }
}
