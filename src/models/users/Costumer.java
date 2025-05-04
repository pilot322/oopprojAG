package models.users;

public class Costumer extends User {
    String VAT;

    public Costumer(int id, String userName, String password, String VAT) {
        super(id, userName, password);
        this.VAT = VAT;
    }

    public String getVAT() {
        return VAT;
    }
}
