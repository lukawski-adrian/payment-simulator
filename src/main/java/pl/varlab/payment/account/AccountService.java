package pl.varlab.payment.account;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import pl.varlab.payment.transaction.TransactionRequest;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

// TODO: consider Outbox pattern
@Service
public class AccountService {

    private final ConcurrentHashMap<String, BigDecimal> accounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private ReentrantLock getLockForKey(String accountId) throws AccountNotFoundException {
        if (locks.containsKey(accountId))
            return locks.get(accountId);

        throw new AccountNotFoundException(accountId);
    }

    public void withdrawal(TransactionRequest transactionRequest) throws InsufficientFundsException, AccountNotFoundException {
        var amount = transactionRequest.amount();
        var senderId = transactionRequest.senderId();

        var lock = getLockForKey(senderId);
        lock.lock();
        try {
            internalWithdrawal(senderId, amount);
        } finally {
            lock.unlock();
        }
    }

    public void deposit(TransactionRequest transactionRequest) throws AccountNotFoundException {
        var amount = transactionRequest.amount();
        var recipientId = transactionRequest.recipientId();

        var lock = getLockForKey(recipientId);
        lock.lock();
        try {
            internalDeposit(recipientId, amount);
        } finally {
            lock.unlock();
        }
    }

    private void internalWithdrawal(String senderId, BigDecimal amount) throws InsufficientFundsException {
        var accountBalance = accounts.get(senderId);
        // TODO: verify second math context function
        var updateAccountBalance = accountBalance.subtract(amount);
        if (BigDecimal.ZERO.compareTo(updateAccountBalance) > 0)
            throw new InsufficientFundsException();

        accounts.put(senderId, updateAccountBalance);
    }

    private void internalDeposit(String recipientId, BigDecimal amount) {
        var accountBalance = accounts.get(recipientId);
        // TODO: verify second math context function
        var updateAccountBalance = accountBalance.add(amount);
        accounts.put(recipientId, updateAccountBalance);
    }

    private void createAccount(String accountId) {
        locks.put(accountId, new ReentrantLock());
        accounts.put(accountId, BigDecimal.valueOf(100));
    }

    @PostConstruct
    public void init() {
        createAccount("acc1");
        createAccount("acc2");
    }
}
