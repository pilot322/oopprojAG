package managers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.statements.AccountStatement;
import system.BankSystem;

public class AccountStatementManager extends Manager {
    private HashMap<String, List<AccountStatement>> statements;

    public AccountStatementManager(BankSystem system) {
        super(system);
        this.statements = new HashMap<>();
    }

    // Προσθήκη νέου statement
    public void addStatement(String accountIBAN, int transactorId,
            String description, double amount,
            double balanceAfter, String type,
            String receiverIBAN) {
        List<AccountStatement> accountStatements = statements.getOrDefault(accountIBAN, new ArrayList<>());
        int nextId = accountStatements.size();
        LocalDateTime transactionTime = LocalDateTime.now();

        AccountStatement statement = new AccountStatement(nextId, accountIBAN, transactionTime,
                transactorId, description, type, amount, balanceAfter, receiverIBAN);
        statements.putIfAbsent(accountIBAN, new ArrayList<>());
        statements.get(accountIBAN).add(statement);
        // throw new RuntimeException("TODO");
    }

    // Λήψη όλων των statements για IBAN
    public List<AccountStatement> getStatements(String accountIBAN) {
        if (statements.containsKey(accountIBAN)) {
            return statements.get(accountIBAN);
        }
        return new ArrayList<>();
    }

}