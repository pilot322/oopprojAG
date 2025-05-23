package models.users;

public class Company extends Customer {

    public Company(int id, String legalName, String userName, String password, String vatNumber) {
        super(id, legalName, userName, password, vatNumber);
    }

    @Override
    public String marshal() {
        return "type:Company," + super.marshal();
    }

    @Override
    public void unmarshal(String data) {
        super.unmarshal(data);
    }

}
