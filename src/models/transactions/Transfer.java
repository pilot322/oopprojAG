package models.transactions;

import managers.AccountStatementManager;
import models.accounts.BankAccount;
import system.BankSystem;

public class Transfer extends TwoWay {
        public Transfer(int transactorId, String senderIBAN,
                        String senderDescription, double amount,
                        String receiverIBAN, String receiverDescription, BankSystem system) {
                super(transactorId, senderIBAN, senderDescription, amount,
                                receiverIBAN, receiverDescription, system);
        }

        @Override
        public boolean execute() {
                // AccountStatement senderStatement = new AccountStatement(
                // accountIBAN,
                // timestamp,
                // transactorId,
                // "TRANSFER_OUT",
                // description + " -> " + receiverDescription,
                // -amount,
                // -amount);

                // AccountStatement receiverStatement = new AccountStatement(
                // receiverIBAN,
                // timestamp,
                // transactorId,
                // "TRANSFER_IN",
                // receiverDescription + " <- " + description,
                // amount,
                // amount);

                // statementManager.addStatement(senderStatement);
                // statementManager.addStatement(receiverStatement);
                if(!isValid()){
                        return false;
                } 

                BankAccount sender = systemRef.getAccountManager().findAccountByIBAN(accountIBAN);
                BankAccount receiver = systemRef.getAccountManager().findAccountByIBAN(receiverIBAN);

                sender.removeFromBalance(amount);
                receiver.addToBalance(amount);

                executed = true;

                AccountStatementManager accStmtManager = systemRef.getAccountStatementManager();

                accStmtManager.addStatement(accountIBAN, transactorId,
                                description + " -> " + receiverDescription, amount, sender.getBalance(), "transfer_out",
                                receiverIBAN);

                accStmtManager.addStatement(receiverIBAN, transactorId,
                                receiverDescription + " <- " + description, amount, receiver.getBalance(),
                                "transfer_in", accountIBAN);

                return true;
        }
}