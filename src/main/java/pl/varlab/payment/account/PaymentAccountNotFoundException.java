package pl.varlab.payment.account;

public class PaymentAccountNotFoundException extends Exception {

    public PaymentAccountNotFoundException(String accountId) {
        super(STR."Account with id '\{accountId}' not found");
    }
}
