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
    public void withdraw(String accountIBAN, int transactorId, String description, double amount) throws Exception {
        Withdraw w = new Withdraw(transactorId, accountIBAN, description, amount, systemRef);
        w.execute();

        // throw new RuntimeException("TODO");
    }

    /**
     * Εκτέλεση κατάθεσης
     * 
     * @return true αν η κατάθεση ολοκληρώθηκε επιτυχώς
     */
    public void deposit(String accountIBAN, int transactorId,
            String description, double amount) throws Exception {

        Deposit d = new Deposit(transactorId, accountIBAN, description, amount, systemRef);
        d.execute();
    }

    /**
     * Εκτέλεση μεταφοράς
     * 
     * @return true αν η μεταφορά ολοκληρώθηκε επιτυχώς
     */
    public void transfer(String senderIBAN, int transactorId,
            String description, double amount,
            String receiverIBAN) throws Exception {
            Transfer t = new Transfer(transactorId, senderIBAN, description, amount, receiverIBAN,
                    "Transfer to " + receiverIBAN, systemRef);
            t.execute();
    }

    /**
     * Εκτέλεση πληρωμής λογαριασμού
     * 
     * @return true αν η πληρωμή ολοκληρώθηκε επιτυχώς
     */
    public void pay(String accountIBAN, int transactorId,
            String description, String RF) throws Exception {
            Payment p = new Payment(transactorId, accountIBAN, description, RF, systemRef);
            p.execute();
            throw new RuntimeException("TODO!");
    }
}