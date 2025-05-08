package models.transactions;

import managers.AccountStatementManager;
import java.util.List;

import javax.management.RuntimeErrorException;

import models.accounts.BankAccount;
import models.accounts.BusinessAccount;
import models.bills.Bill;
import system.BankSystem;

public class Payment extends TwoWay {
    private final String RF;
    private final Bill bill;

    public Payment(int transactorId, String senderIBAN,
            String senderDescription,
            String RF, BankSystem systemRef) {

        super(transactorId,
                senderIBAN,
                senderDescription,
                systemRef.getBillManager().getActiveBillByRf(RF).getAmount(),
                systemRef.getAccountManager()
                        .findAccountByBusinessId(systemRef.getBillManager().getActiveBillByRf(RF).getBusinessId())
                        .getIBAN(),
                "Bill sent to Business with ID " + systemRef.getBillManager().getActiveBillByRf(RF).getBusinessId(),
                systemRef);
        this.RF = RF;
        this.bill = systemRef.getBillManager().getActiveBillByRf(RF);

        if (bill == null || bill.isPaid()) {
            throw new RuntimeException("Bill doesn't exist or is paid!");
        }
        // me vash to RF tha prepei na vreis to amount, to reiverIBAN kai to
        // receiverDescription
    }

    @Override
    public boolean execute() {
        if (!isValid()) {
            return false;
        }

        BankAccount senderAccount = systemRef.getAccountManager().findAccountByIBAN(accountIBAN);
        BankAccount receiverAccount = systemRef.getAccountManager().findAccountByIBAN(receiverIBAN);
        // kanonika DEN xreiazetai o parakatw elegxos, afoy to receiverIBAN to phra apo ena Bill, kai ta bills kratane mono business bank account iban
        // if (!(receiverAccount instanceof BusinessAccount)) {
        //     return false;
        // }

        receiverAccount.addToBalance(amount);
        senderAccount.removeFromBalance(amount);
        bill.markAsPaid();

        AccountStatementManager accStmtManager = systemRef.getAccountStatementManager();
        accStmtManager.addStatement(accountIBAN, transactorId, description, amount, senderAccount.getBalance(),
                "payment", receiverIBAN);
        accStmtManager.addStatement(receiverIBAN, transactorId, receiverDescription, amount,
                receiverAccount.getBalance(), "payment", accountIBAN);
        executed = true;
        return true;

    }

    public String getRF() {
        return RF;
    }
}