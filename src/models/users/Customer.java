package models.users;

public abstract class Customer extends User {
    protected String VAT;

    public Customer(int id, String legalName, String userName, String password, String VAT) {
        super(id, legalName, userName, password);
        this.VAT = VAT;
    }

    public String getVAT() {
        return VAT;
    }

    @Override
    public String marshal() {
        return String.format("id:%d,legalName:%s,userName:%s,password:%s,vatNumber:%s",
                id, legalName, userName, password, VAT);
    }

    @Override
    public void unmarshal(String data) {
        String[] parts = data.split(",");
        this.id = Integer.parseInt(parts[1].split(":")[1]);
        this.legalName = parts[2].split(":")[1];
        this.userName = parts[3].split(":")[1];
        this.password = parts[4].split(":")[1];
        this.VAT = parts[5].split(":")[1];
    }
}
