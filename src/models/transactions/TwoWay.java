package models.transactions;

import models.accounts.BankAccount;
import system.BankSystem;

public abstract class TwoWay extends Transaction {

    protected String receiverIBAN;
    protected String receiverDescription;

    public TwoWay(int transactorId, String senderIBAN, String senderDescription, double amount, String receiverIBAN,
            String receiverDescription, BankSystem system) {
        super(transactorId, senderIBAN, senderDescription, amount, system);
        this.receiverIBAN = receiverIBAN;
        this.receiverDescription = receiverDescription;
    }

    public String getReceiverIBAN() {
        return receiverIBAN;
    }

    public String getReceiverDescription() {
        return receiverDescription;
    }

    protected boolean isValid(){
        if(!super.isValid()){
            return false;
        }

        BankAccount receiver = systemRef.getAccountManager().findAccountByIBAN(receiverIBAN);

        // Elegxos an receiver, sender kai transactor einai egkyroi
        if (receiver == null || accountIBAN == null || accountIBAN.equals(receiverIBAN)) {
                return false;
        }
        if (systemRef.getUserManager().findUserById(transactorId) == null) {
                return false;
        } 

     

        return true;
    }

}