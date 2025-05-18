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

        // estw epitrepontai mono ta GR, AL kai EN
        if(!"GR".equals(countryCode) && !"AL".equals(countryCode) && !"EN".equals(countryCode)){
            throw new IllegalArgumentException("Illegal country code.");
        }

        if (systemRef.getUserManager().findUserById(ownerId) == null) {
            throw new IllegalArgumentException("Owner with ID " + ownerId + " does not exist.");
        }
        String userType = systemRef.getUserManager().getUserType(ownerId);
        if (!userType.equals("Individual")) {
            throw new IllegalArgumentException("Only users of type 'Individual' can own a personal account.");
        }
        if (countryCode == null || countryCode.length() != 2) {
            throw new IllegalArgumentException("Country code must exist and be exactly 2 characters.");
        }
        if (interestRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative.");
        }
        if (secondaryOwnerIds == null) {
            secondaryOwnerIds = new ArrayList<>(); // Dhmiourgoyme nea lista an h lista einai null
        }
        for (int id : secondaryOwnerIds) {
            if (systemRef.getUserManager().findUserById(id) == null) {
                throw new IllegalArgumentException("Secondary owner with ID " + id + " does not exist.");
            }
            if (!systemRef.getUserManager().getUserType(id).equals("Individual")) {
                throw new IllegalArgumentException("Secondary owners must be of type 'Individual'.");
            }
        }
        if (secondaryOwnerIds.contains(ownerId)) {
            throw new IllegalArgumentException("The primary owner cannot also be a secondary owner.");
        }
        String IBAN = generateIBAN(countryCode, "100");
        PersonalAccount ba = new PersonalAccount(IBAN, ownerId, interestRate, secondaryOwnerIds);
        System.out.println("debug: " + ba.getIBAN());
        if (!ba.getIBAN().startsWith(countryCode + "100")) {
            throw new IllegalArgumentException("IBAN should start with " + countryCode + "100 for personal.");
        }
        if (ba.getIBAN().length() != 20) {
            throw new IllegalArgumentException("IBAN total length should be 20.");
        }
        bankAccountList.add(ba);

    }

    public void createBusinessAccount(int ownerId, String countryCode, double interestRate) throws Exception {
        // TODO: vres maintenance fee pws krokyptei
        if (systemRef.getUserManager().findUserById(ownerId) == null) {
            throw new IllegalArgumentException("Owner with ID " + ownerId + " does not exist.");
        }
        String userType = systemRef.getUserManager().getUserType(ownerId);
        if (!userType.equals("Company")) {
            throw new IllegalArgumentException("Only users of type 'Company' can own a business account.");
        }
        if (countryCode == null || countryCode.length() != 2) {
            throw new IllegalArgumentException("Country code must exist and be exactly 2 characters.");
        }
        if (interestRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative.");
        }

        // dokimh gia na paroyme yparxwn bank account. an yparxei, tha petaksoyme exception (afoy hdh yparxei, apagoreyetai na dhmioyrghthei kiallo)
        
        try {
            findAccountByBusinessId(ownerId); // "kokkino" - den yparxei
            // an den skasei h parapanw, exw provlhma
            throw new IllegalStateException(); // "mple" - yparxei bank account
        } catch(IllegalStateException e){
            // an skasei, tote den yparxei thema (dld )
            throw new IllegalStateException("Yparxei to bank account");
        } catch(Exception e){
            // ...
        }

        double maintenanceFee = interestRate * 1000;
        String IBAN = generateIBAN(countryCode, "200");
        BusinessAccount ba = new BusinessAccount(IBAN, ownerId, interestRate, maintenanceFee);
        if (!ba.getIBAN().startsWith(countryCode + "200")) {
            throw new IllegalArgumentException("IBAN should start with " + countryCode + "200 for business.");
        }
        if (ba.getIBAN().length() != 20) {
            throw new IllegalArgumentException("Iban length should be 20.");
        }
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

    public BusinessAccount findAccountByBusinessId(int businessId) throws Exception {
        for (BankAccount b : bankAccountList) {
            if (isOwnerOfBankAccount(b, businessId)) {
                return (BusinessAccount) b;
            }
        }
        throw new IllegalArgumentException("Business account not found");
    }

    public boolean isOwnerOfBankAccount(BankAccount b, int ownerId) {
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
        if (personalAccounts.isEmpty()) {
            throw new IllegalArgumentException("Individual account not found.");
        }

        return personalAccounts;
    }
}
