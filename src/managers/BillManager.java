package managers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.bills.Bill;
import system.BankSystem;

public class BillManager extends Manager {
    private List<Bill> bills = new ArrayList<>();

    public BillManager(BankSystem system) {
        super(system);
    }

    public void issueBill(int businessId, int customerId, double amount, LocalDateTime expireTime, String oldRF) throws Exception{
        throw new RuntimeException("TODO!");
    }

    public List<Bill> getBillsByRF(String RF) {
        List<Bill> result = new ArrayList<>();
        for (Bill b : bills) {
            if (b.getRF().equals(RF)) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Bill> getActiveBillsForCustomer(int customerId) {
        List<Bill> result = new ArrayList<>();
        for (Bill b : bills) {
            if (b.getCustomerId() == customerId && b.isActive() && !b.isPaid()) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Bill> getActiveBillsForBusiness(int businessId) {
        List<Bill> result = new ArrayList<>();
        for (Bill b : bills) {
            if (b.getBusinessId() == businessId && b.isActive() && !b.isPaid()) {
                result.add(b);
            }
        }
        return result;
    }

    public void markBillAsPaid(String RF) {
        throw new RuntimeException("TODO!");
    }

    public ArrayList<Bill> getBillsForBusinessCustomerPair(int customerId, int businessId){
        throw new RuntimeException("TODO!");
    }

    public void deactivateBillsWithRF(String RF) {
        for (Bill b : bills) {
            if (b.getRF().equals(RF)) {
                b.setActive(false);
            }
        }
    }

    public Bill getActiveBillByRf(String RF) {
        for (Bill b : bills) {
            if (b.getRF().equals(RF) && b.isActive() && !b.isPaid()) {
                return b;
            }
        }
        return null;
    }

}