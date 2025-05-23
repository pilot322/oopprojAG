package models.transactions;

import managers.AccountStatementManager;
import models.accounts.BankAccount;
import system.BankSystem;

public class Deposit extends Transaction {
    public Deposit(int transactorId, String accountIBAN, String description, double amount, BankSystem systemRef) {
        super(transactorId, accountIBAN, description, amount, systemRef);
    }

    @Override
    public void execute() throws Exception{
        if (!isValid()) {
            throw new IllegalArgumentException("Arguments invalid.");
        }

        // 1. ayksanw to balance toy bank account sto opoio antistoixei to iban kata to
        // // amount

        BankAccount b = systemRef.getAccountManager().findAccountByIBAN(accountIBAN);
        b.addToBalance(amount);

        executed = true;

        // 2. dhmioyrgeitai ena account statement
        // xreiazesai to account statement manager
        AccountStatementManager accStmtManager = systemRef.getAccountStatementManager();

        accStmtManager.addStatement(accountIBAN, transactorId, description, amount, b.getBalance(), "deposit",
                null);
    }
}