package models.accounts;

import java.time.LocalDateTime;

public class BusinessAccount extends BankAccount {
    private double maintenanceFee;

    public BusinessAccount(String IBAN, int ownerId, double interestRate, double maintenanceFee) {
        super(IBAN, ownerId, interestRate);
        this.maintenanceFee = maintenanceFee;
    }

    public double getMaintenanceFee() {
        return maintenanceFee;
    }

    @Override
    public String marshal() {
        return "type:BusinessAccount," + super.marshal();
    }

    @Override
    public void unmarshal(String data) {
        super.unmarshal(data);
        String[] parts = data.split(",");
        this.maintenanceFee = Double.parseDouble(parts[6].split(":")[1]);
    }

}
