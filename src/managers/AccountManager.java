package managers;

import java.util.ArrayList;

import models.accounts.BankAccount;
import models.accounts.BusinessAccount;
import models.accounts.PersonalAccount;
import system.BankSystem;

public class AccountManager extends Manager {

    public AccountManager(BankSystem systemRef) {
        super(systemRef);
    }

    private ArrayList<BankAccount> bankAccountList = new ArrayList<>();

    private String generateIBAN(String countryCode, String typeOfAccountCode) {
        String IBAN = countryCode + typeOfAccountCode;

        while (true) {
            for (int i = 0; i < 15; i++) {
                String random_pshfio = Integer.toString((int) (Math.random() * 10));
                IBAN += random_pshfio;
            }

            // TODO: Elegxos etsi wste an yparxei to idio iban sto systhma, na kanoyme
            // generate kiallo IBAN,
            // mexri na pesoyme se kapoio poy den yparxeis
            BankAccount ba = findAccountByIBAN(IBAN);
            if (ba == null) {
                return IBAN;

            } else {
                IBAN = countryCode + typeOfAccountCode;
            }
        }
    }

    public void createPersonalAccount(int ownerId, String countryCode, double interestRate,
            ArrayList<Integer> secondaryOwnerIds) throws Exception {
        // throw new Exception("TODO");

        // otan kalw ayth thn synarthsh:
        // stoxos: na dhmioyrgeitai ena kainoyrio personal account kai na mpainei sthn
        // lista
        // IBAN: COUNTRY CODE + 100/200 (100 gia individual) + TYXAIA 15 PSHFIA
        String IBAN = generateIBAN(countryCode, "100");
        PersonalAccount ba = new PersonalAccount(IBAN, ownerId, interestRate, secondaryOwnerIds);

        bankAccountList.add(ba);

    }

    public void createBusinessAccount(int ownerId, String countryCode, double interestRate) {
        // TODO: vres maintenance fee pws krokyptei
        double maintenanceFee = 10;
        String IBAN = generateIBAN(countryCode, "200");
        BusinessAccount ba = new BusinessAccount(IBAN, ownerId, interestRate, maintenanceFee);
        bankAccountList.add(ba);
    }

    // ftiakse synarthsh gia thn dhmioyrgia business bank account
    public BankAccount findAccountByIBAN(String IBAN) {
        for (BankAccount account : bankAccountList) {
            if (account.getIBAN().equals(IBAN)) {
                return account;
            }
        }
        return null;
    }

    public BusinessAccount findAccountByBusinessId(int businessId) {
        // dedomena: bankAccountList, businessId
        // stoxos: na epistrepsoyme to bank account to opoio exei san owner to user be
        // id == businessId
        // estw businessId = 3

        // estw oti to bankAccountList einai keno

        // return null;

        // estw oti bankAccountList == { (ownerId = X), (ownerId =Y)}

        // grammikh anazhthsh

        // for (int i = 0; i < bankAccountList.size(); i++) {
        // if (bankAccountList.get(i).getOwnerId() == businessId) {
        // return (BusinessAccount) bankAccountList.get(i);
        // }
        // }

        for (BankAccount b : bankAccountList) {
            if (isOwnerOfBankAccount(b, businessId)) {
                return (BusinessAccount) b;
            }
        }

        return null;
    }

    boolean isOwnerOfBankAccount(BankAccount b, int ownerId) {
        if (b.getOwnerId() == ownerId) {
            return true;
        }

        String ownerType = systemRef.getUserManager().getUserType(ownerId);

        if (b instanceof PersonalAccount && ownerType.equals("Individual")) {
            PersonalAccount p = (PersonalAccount) b;
            // dedomena: b (PersonalAccount), ownerId
            // stoxos: na vrw an to ownerId yparxei sto secondaryOwners toy p
            ArrayList<Integer> secondaryOwners = p.getSecondaryOwnerIds();

            // secondaryOwners = {3, 6, 7}
            // ownerId = 7

            for (int i = 0; i < secondaryOwners.size(); i++) {
                if (ownerId == secondaryOwners.get(i)) {
                    return true;
                }
            }
        }

        return false;
    }

    public ArrayList<PersonalAccount> findAccountsByIndividualId(int individualId) {
        ArrayList<PersonalAccount> personalAccounts = new ArrayList<>();

        for (BankAccount b : bankAccountList) {
            if (isOwnerOfBankAccount(b, individualId)) {
                personalAccounts.add((PersonalAccount) b);
            }
        }

        return personalAccounts;
    }
}
