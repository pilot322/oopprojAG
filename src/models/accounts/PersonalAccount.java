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

    @Override
    public String marshal() {
        String result = "type:PersonalAccount," + super.marshal();
        if (!secondaryOwnerIds.isEmpty()) {
            for (int id : secondaryOwnerIds) {
                result += ",coOwner";
                result += ":" + id;
            }
        }
        return result;
    }

    @Override
    public void unmarshal(String data) {
        super.unmarshal(data);
        String[] parts = data.split(",");
        this.secondaryOwnerIds = new ArrayList<>();
        if (parts.length > 6 && parts[6].startsWith("coOwner")) {
            String[] coOwners = parts[6].split(":");
            for (int i = 1; i < coOwners.length; i++) {
                secondaryOwnerIds.add(Integer.parseInt(coOwners[i]));
            }
        }
    }

}
