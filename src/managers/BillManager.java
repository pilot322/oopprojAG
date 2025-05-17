package managers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.RowFilter;

import models.bills.Bill;
import system.BankSystem;

public class BillManager extends Manager {
    private List<Bill> bills = new ArrayList<>();

    public BillManager(BankSystem system) {
        super(system);
    }

    public void addBill(Bill bill) {
        bills.add(bill);
    }

    public Bill getBillById(int id) {
        for (Bill b : bills) {
            if (b.getId() == id) {
                return b;
            }
        }
        return null;
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

    public boolean payBill(int billId) {
        Bill b = getBillById(billId);
        if (b != null && b.isActive() && !b.isPaid()) {
            b.markAsPaid();
            return true;
        }
        return false;
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