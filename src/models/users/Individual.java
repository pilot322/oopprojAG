package models.users;

public class Individual extends Customer {

    public Individual(int id, String legalName, String userName, String password, String vatNumber) {
        super(id, legalName, userName, password, vatNumber);
    }

    @Override
    public String marshal() {
        return "type:Individual," + super.marshal();
    }

    @Override
    public void unmarshal(String data) {
        super.unmarshal(data);
    }

}
