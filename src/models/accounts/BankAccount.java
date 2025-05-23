package models.accounts;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import interfaces.Storable;

public abstract class BankAccount implements Storable {
    protected String IBAN;
    protected int ownerId;
    protected double balance;
    protected double interestRate;
    protected LocalDateTime date;

    public BankAccount(String IBAN, int ownerId, double interestRate) {
        this.IBAN = IBAN;
        this.ownerId = ownerId;
        this.interestRate = interestRate;
        this.balance = 0.0;
        this.date = LocalDateTime.now();
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getIBAN() {
        return IBAN;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public double getBalance() {
        return balance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public boolean addToBalance(double amount) {
        if (amount < 0)
            return false;

        this.balance += amount;
        return true;
    }

    public boolean removeFromBalance(double amount) {
        if (amount < 0 || this.balance - amount < 0)
            return false;
        this.balance -= amount;
        return true;
    }

    public String marshal() {
        return String.format("iban:%s,primaryOwner:%d,dateCreated:%s,rate:%.4f,balance:%.2f",
                IBAN, ownerId, date.toString(), interestRate, balance);
    }

    public void unmarshal(String data) {
        String[] parts = data.split(",");
        this.IBAN = parts[1].split(":")[1];
        this.ownerId = Integer.parseInt(parts[2].split(":")[1]);
        this.date = LocalDateTime.parse(parts[3].split(":")[1]);
        this.interestRate = Double.parseDouble(parts[4].split(":")[1]);
        this.balance = Double.parseDouble(parts[5].split(":")[1]);
    }
}
