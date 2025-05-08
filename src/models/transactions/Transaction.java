package models.transactions;

import java.time.LocalDateTime;

import models.accounts.BankAccount;
import system.BankSystem;

public abstract class Transaction {
    protected final int transactorId;
    protected final String accountIBAN;
    protected final String description;
    protected  double amount;
    protected final LocalDateTime timestamp;
    protected boolean executed;
    protected BankSystem systemRef;

    public Transaction(int transactorId, String accountIBAN, String description, double amount, BankSystem system) {
        this.transactorId = transactorId;
        this.accountIBAN = accountIBAN;
        this.description = description;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.executed = false;
        this.systemRef = system;
    }

    public abstract boolean execute();

    protected boolean isValid(){
        if(executed){
            return false;
        }

        if (amount <= 0) {
            return false;
        }
        
        BankAccount b = systemRef.getAccountManager().findAccountByIBAN(accountIBAN);
        if (b == null) {
            return false;
        }
        if (systemRef.getUserManager().findUserById(transactorId) == null) {
            return false;
        }
        if (b.getBalance() < amount && ! (this instanceof Deposit) ) {
            return false;
        }

        return true;
    }

    public int getTransactorId() {
        return transactorId;
    }

    public String getAccountIBAN() {
        return accountIBAN;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isExecuted() {
        return executed;
    }
}