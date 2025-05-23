package managers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import interfaces.Storable;
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
        if (systemRef.getAccountManager().findAccountByIBAN(accountIBAN) == null) {
            throw new IllegalArgumentException("Account IBAN does not exist: " + accountIBAN);
        }
        if (systemRef.getUserManager().findUserById(transactorId) == null) {
            throw new IllegalArgumentException("Transactor with ID" + transactorId + "does not exist.");
        }
        if (amount == 0.0) {
            throw new IllegalArgumentException("Amount cannot be zero.");
        }

        if (!type.equalsIgnoreCase("deposit") &&
                !type.equalsIgnoreCase("withdraw") &&
                !type.equalsIgnoreCase("transfer_out")) {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }
        if (!"withdraw".equalsIgnoreCase(type) && amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative for non-withdraw transactions.");
        }

        if (type.equalsIgnoreCase("transfer_out")) {
            if (receiverIBAN == null || receiverIBAN.isEmpty()) {
                throw new IllegalArgumentException("Receiver IBAN must be provided for transfer_out type.");
            }
            if (systemRef.getAccountManager().findAccountByIBAN(receiverIBAN) == null) {
                throw new IllegalArgumentException("Receiver IBAN does not exist: " + receiverIBAN);
            }
        }
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
            List<AccountStatement> original = statements.get(accountIBAN);
            List<AccountStatement> sorted = new ArrayList<>(original);
            sorted.sort((a, b) -> b.getTransactionTime().compareTo(a.getTransactionTime()));
            return sorted;
        }
        return new ArrayList<>();
    }
    public void saveAll(){
        for(String IBAN : statements.keySet()){
            List<Storable> list = new ArrayList<>(statements.get(IBAN));
            writeListToFile("data/statements/"+IBAN+".csv", list);
        }
    }
}
