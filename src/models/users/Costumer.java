package models.users;

public abstract class Costumer extends User {
    String VAT;

    public Costumer(int id, String legalName, String userName, String password, String VAT) {
        super(id, legalName, userName, password);
        this.VAT = VAT;
    }

    public String getVAT() {
        return VAT;
    }

    public String marshal(){
        return null;
    }
    public void unmarshal(String data){

    } 
}
