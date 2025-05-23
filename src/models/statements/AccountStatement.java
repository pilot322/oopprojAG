package models.statements;

import java.time.LocalDateTime;

import interfaces.Storable;

public class AccountStatement implements Storable {
    private int id;
    private String accountIBAN;
    private LocalDateTime transactionTime;
    private int transactorId;
    private String description;
    private String transactionType;
    private double amount;
    private double balanceAfterTransaction;
    private String receiverIBAN;

    public AccountStatement(int id, String accountIBAN, LocalDateTime transactionTime,
            int transactorId, String description, String transactionType,
            double amount, double balanceAfterTransaction,
            String receiverIBAN) {
        this.id = id;
        this.accountIBAN = accountIBAN;
        this.transactionTime = transactionTime;
        this.transactorId = transactorId;
        this.description = description;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.receiverIBAN = receiverIBAN;
    }

    public int getId() {
        return id;
    }

    public String getAccountIBAN() {
        return accountIBAN;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public int getTransactorId() {
        return transactorId;
    }

    public String getDescription() {
        return description;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public String getReceiverIBAN() {
        return receiverIBAN;
    }

    public String toString() {
        return String.format(
                "AccountStatement[id=%d, account=%s, time=%s, type=%s, amount=%.2f, balance=%.2f]",
                id, accountIBAN, transactionTime, transactionType, amount, balanceAfterTransaction);
    }

    public String marshal() {
        String recieverStr;
        if (receiverIBAN == null) {
            recieverStr = "null";
        } else {
            recieverStr = receiverIBAN;
        }

        String temp =  String.format(
                "id:%d,accountIBAN:%s,transactionTime:%s,transactorId:%d,description:%s,transactionType:%s,amount:%.2f,balanceAfterTransaction:%.2f",
                id,
                accountIBAN,
                transactionTime.toString(),
                transactorId,
                description,
                transactionType,
                amount,
                balanceAfterTransaction
                );

        if (receiverIBAN != null){
            temp += ",receiverIBAN:"+receiverIBAN;
        }
        return temp;
    }

    public void unmarshal(String data) {
        String[] parts = data.split(",");
        int id = Integer.parseInt(parts[0].split(":")[1]);
        String accountIBAN = parts[1].split(":")[1];
        LocalDateTime transactionTime = LocalDateTime.parse(parts[2].split(":")[1]);
        int transactorId = Integer.parseInt(parts[3].split(":")[1]);
        String description = parts[4].split(":")[1];
        String transactionType = parts[5].split(":")[1];
        double amount = Double.parseDouble(parts[6].split(":")[1]);
        double balanceAfterTransaction = Double.parseDouble(parts[7].split(":")[1]);
        String receiverIBAN = parts[8].split(":")[1];

        this.id = id;
        this.accountIBAN = accountIBAN;
        this.transactionTime = transactionTime;
        this.transactorId = transactorId;
        this.description = description;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.receiverIBAN = receiverIBAN;
    }
}