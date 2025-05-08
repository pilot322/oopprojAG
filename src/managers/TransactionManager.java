package managers;

import system.BankSystem;
import models.transactions.Deposit;
import models.transactions.Payment;
import models.transactions.Transfer;
import models.transactions.Withdraw;

public class TransactionManager extends Manager {

    public TransactionManager(BankSystem system) {
        super(system);
    }

    /**
     * Εκτέλεση ανάληψης
     * 
     * @return true αν η ανάληψη ολοκληρώθηκε επιτυχώς
     */
    public boolean withdraw(String accountIBAN, int transactorId, String description, double amount) {
        if (amount <= 0) {
            return false;
        }
        Withdraw w = new Withdraw(transactorId, accountIBAN, description, amount, systemRef);
        return w.execute();

        // throw new RuntimeException("TODO");
    }

    /**
     * Εκτέλεση κατάθεσης
     * 
     * @return true αν η κατάθεση ολοκληρώθηκε επιτυχώς
     */
    public boolean deposit(String accountIBAN, int transactorId,
            String description, double amount) {
        if (amount <= 0) {
            return false;
        }
        Deposit d = new Deposit(transactorId, accountIBAN, description, amount, systemRef);
        return d.execute();
        // throw new RuntimeException("TODO");
    }

    /**
     * Εκτέλεση μεταφοράς
     * 
     * @return true αν η μεταφορά ολοκληρώθηκε επιτυχώς
     */
    public boolean transfer(String senderIBAN, int transactorId,
            String description, double amount,
            String receiverIBAN) {
        if (amount <= 0) {
            return false;
        }
        if (senderIBAN.equals(receiverIBAN)) {
            return false;
        }
        Transfer t = new Transfer(transactorId, senderIBAN, description, amount, receiverIBAN,
                "Transfer to " + receiverIBAN, systemRef);
        return t.execute();

        // throw new RuntimeException("TODO");
    }

    /**
     * Εκτέλεση πληρωμής λογαριασμού
     * 
     * @return true αν η πληρωμή ολοκληρώθηκε επιτυχώς
     */
    public boolean pay(String accountIBAN, int transactorId,
            String description, String RF) {
        if (RF == null) {
            return false;
        }
        Payment p = new Payment(transactorId, accountIBAN, description, RF, systemRef);
        return p.execute();
        // throw new RuntimeException("TODO");
    }
}