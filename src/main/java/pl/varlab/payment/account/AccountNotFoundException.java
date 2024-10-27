package pl.varlab.payment.account;

public class AccountNotFoundException extends Exception {

    public AccountNotFoundException(String accountId) {
        super(STR."Account with id '\{accountId}' not found");
    }
}
