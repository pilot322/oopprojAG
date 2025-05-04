package models.accounts;

import java.util.ArrayList;

public class PersonalAccount extends BankAccount {
    private ArrayList<Integer> secondaryOwnerIds;

    public PersonalAccount(String IBAN, int ownerId, double interestRate,
            ArrayList<Integer> secondaryOwnerIds) {
        super(IBAN, ownerId, interestRate);
        this.secondaryOwnerIds = new ArrayList<>(secondaryOwnerIds);
    }

    public ArrayList<Integer> getSecondaryOwnerIds() {
        return new ArrayList<>(secondaryOwnerIds); // giati new?
        // epeidh an epistrepseis thn idia thn lista, aftos poy kalese thn synarthsh
        // mporei na thn allaksei
        // me afton ton tropo, ftiaxneis ena antigrafo
    }
}
