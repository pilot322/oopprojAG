package test;
import managers.*;

import models.bills.Bill;
import org.junit.Before;
import org.junit.Test;
import system.BankSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random; // For generating distinct RFs if needed for testing

import static org.junit.Assert.*;

public class BillManagerTest {

    private BankSystem bankSystem;
    private BillManager billManager;
    private UserManager userManager;
    // AccountManager might be needed if BillManager uses it, but based on code, not directly.

    private int businessId1, businessId2;
    private int customerIdIndiv1, customerIdComp1;
    private int nonExistentUserId = 999;
    private String generatedRFSuffix = ""; // To make RFs unique in tests if needed

    // Helper for unique RFs in tests if BillManager's random RF isn't easily predictable
    private String generateTestRF(String prefix) {
        if (generatedRFSuffix.isEmpty()) {
            generatedRFSuffix = String.valueOf(System.currentTimeMillis() % 10000);
        }
        return prefix + "_" + generatedRFSuffix + "_" + (new Random().nextInt(1000));
    }


    @Before
    public void setUp() {
        bankSystem = new BankSystem();
        billManager = bankSystem.getBillManager();
        userManager = bankSystem.getUserManager();

        // Setup users
        userManager.register("Company", "compOneBill", "pass", "Business One Inc.", "111222333");
        businessId1 = userManager.login("compOneBill", "pass").getId();

        userManager.register("Company", "compTwoBill", "pass", "Business Two Co.", "111222332");
        businessId2 = userManager.login("compTwoBill", "pass").getId();

        userManager.register("Individual", "custIndivOne", "pass", "Customer Indiv One", "444555661");
        customerIdIndiv1 = userManager.login("custIndivOne", "pass").getId();

        userManager.register("Company", "custCompOne", "pass", "Customer Comp One", "777888993");
        customerIdComp1 = userManager.login("custCompOne", "pass").getId();
        generatedRFSuffix = ""; // Reset for each test
    }

    // --- issueBill Tests ---

    @Test
    public void testIssueBill_Success_NewRF() throws Exception {
        LocalDateTime expireTime = LocalDateTime.now().plusDays(30);
        billManager.issueBill(businessId1, customerIdIndiv1, 100.0, expireTime, null); // null for oldRF -> new RF

        List<Bill> customerBills = billManager.getActiveBillsForCustomer(customerIdIndiv1);
        assertEquals(1, customerBills.size());
        Bill issuedBill = customerBills.get(0);

        assertEquals(businessId1, issuedBill.getBusinessId());
        assertEquals(customerIdIndiv1, issuedBill.getCustomerId());
        assertEquals(100.0, issuedBill.getAmount(), 0.001);
        assertNotNull(issuedBill.getRF()); // RF should be generated
        assertFalse(issuedBill.getRF().isEmpty());
        assertTrue(issuedBill.isActive());
        // assertFalse(issuedBill.isPaid());
        // Expire time check might be tricky if exact LocalDateTime.now() in SUT is used.
        // Checking if it's close to what was passed or simply that it's after now.
        assertTrue(issuedBill.getExpireTime().isAfter(LocalDateTime.now()));
        assertNotNull(issuedBill.getTimePublished());
    }

    @Test
    public void testIssueBill_Success_WithOldRF_PreviousActiveBillExists() throws Exception {
        String commonRF = generateTestRF("COMMONRF"); // Ensures it's a predictable RF for the test
        LocalDateTime firstExpireTime = LocalDateTime.now().plusDays(10);
        // Issue first bill
        billManager.issueBill(businessId1, customerIdIndiv1, 50.0, firstExpireTime, commonRF);

        Bill firstBill = billManager.getActiveBillByRf(commonRF);
        assertNotNull(firstBill);
        assertEquals(50.0, firstBill.getAmount(), 0.001);
        assertTrue(firstBill.isActive());

        // Issue second bill with the same RF (oldRF = commonRF)
        LocalDateTime secondExpireTime = LocalDateTime.now().plusDays(20);
        billManager.issueBill(businessId1, customerIdIndiv1, 70.0, secondExpireTime, commonRF);

        // Verify first bill is now inactive
        // To get the first bill reliably after it might be deactivated, we need a better way
        // than getActiveBillByRF. Let's assume we can get it by ID if issueBill returns it,
        // or get all bills for the customer.
        List<Bill> allBillsForCustomer = billManager.getBillsForBusinessCustomerPair(customerIdIndiv1, businessId1);
        Bill originalFirstBill = null;
        Bill newSecondBill = null;

        for (Bill b : allBillsForCustomer) {
            if (b.getRF().equals(commonRF)) {
                if (b.getExpireTime().isEqual(firstExpireTime) || b.getAmount() == 50.0) { // Heuristic to find original
                    originalFirstBill = b;
                }
                if (b.getExpireTime().isEqual(secondExpireTime)) {
                    newSecondBill = b;
                }
            }
        }
        assertNotNull("Original first bill should still exist", originalFirstBill);
        assertFalse("Original first bill should be deactivated", originalFirstBill.isActive());

        // Verify new bill
        assertNotNull("New second bill should exist", newSecondBill);
        assertTrue(newSecondBill.isActive());
        // assertFalse(newSecondBill.isPaid());
        assertEquals(commonRF, newSecondBill.getRF());
        assertEquals(50.0 + 70.0, newSecondBill.getAmount(), 0.001); // Amount should be combined
    }
    
    @Test
    public void testIssueBill_Success_WithOldRF_NoPreviousActiveBill_UsesOldRFAsNew() throws Exception {
        String specificRF = generateTestRF("SPECIFIC_RF_NO_ACTIVE");
        LocalDateTime expireTime = LocalDateTime.now().plusDays(15);

        // Issue bill with an 'oldRF' that doesn't have a currently active counterpart
        billManager.issueBill(businessId1, customerIdIndiv1, 120.0, expireTime, specificRF);

        Bill issuedBill = billManager.getActiveBillByRf(specificRF);
        assertNotNull("Bill should be issued with the specified RF", issuedBill);
        assertEquals(businessId1, issuedBill.getBusinessId());
        assertEquals(customerIdIndiv1, issuedBill.getCustomerId());
        assertEquals(120.0, issuedBill.getAmount(), 0.001); // Amount is just the new amount
        assertTrue(issuedBill.isActive());
        // assertFalse(issuedBill.isPaid());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testIssueBill_InvalidBusinessId_ThrowsException() throws Exception {
        billManager.issueBill(nonExistentUserId, customerIdIndiv1, 100.0, LocalDateTime.now().plusDays(30), null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIssueBill_BusinessIdNotCompany_ThrowsException() throws Exception {
        // customerIdIndiv1 is an Individual, not a Company
        billManager.issueBill(customerIdIndiv1, customerIdComp1, 100.0, LocalDateTime.now().plusDays(30), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testIssueBill_InvalidCustomerId_ThrowsException() throws Exception {
        billManager.issueBill(businessId1, nonExistentUserId, 100.0, LocalDateTime.now().plusDays(30), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIssueBill_NegativeAmount_ThrowsException() throws Exception {
        billManager.issueBill(businessId1, customerIdIndiv1, -100.0, LocalDateTime.now().plusDays(30), null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIssueBill_ZeroAmount_ThrowsException() throws Exception {
        billManager.issueBill(businessId1, customerIdIndiv1, 0.0, LocalDateTime.now().plusDays(30), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testIssueBill_ExpireTimeInPast_ThrowsException() throws Exception {
        billManager.issueBill(businessId1, customerIdIndiv1, 100.0, LocalDateTime.now().minusDays(1), null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIssueBill_ExpireTimeEqualToPublishTime_ThrowsException() throws Exception {
        // Publish time is effectively LocalDateTime.now() inside issueBill.
        // This test is tricky without controlling 'now'. If expireTime is exactly now, it might pass/fail based on nanoseconds.
        // A more robust way is to ensure expireTime is strictly after publishTime.
        // For this, we'd ideally pass publishTime or ensure SUT uses a fixed 'now' for testing.
        // Assuming a slight delay, LocalDateTime.now() might be considered "in past or equal".
        // Let's test with now(), assuming it should be strictly after.
        billManager.issueBill(businessId1, customerIdIndiv1, 100.0, LocalDateTime.now(), null);
    }


    // --- markBillAsPaid Tests ---
    @Test
    public void testMarkBillAsPaid_Success() throws Exception {
        String rf = generateTestRF("PAY_ME");
        billManager.issueBill(businessId1, customerIdIndiv1, 50.0, LocalDateTime.now().plusDays(5), rf);
        Bill billBeforePay = billManager.getActiveBillByRf(rf);
        assertNotNull(billBeforePay);
        assertTrue(billBeforePay.isActive());
        // assertFalse(billBeforePay.isPaid());

        billManager.markBillAsPaid(rf);

        Bill billAfterPay = billManager.getBillsByRF(rf).get(0); // Get by RF to find it even if inactive
        assertNotNull(billAfterPay);
        assertFalse(billAfterPay.isActive()); // Should be inactive after payment
        // assertTrue(billAfterPay.isPaid());   // Should be paid
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMarkBillAsPaid_BillNotFound_ThrowsException() {
        billManager.markBillAsPaid("NON_EXISTENT_RF");
    }

    @Test(expected = IllegalStateException.class)
    public void testMarkBillAsPaid_BillAlreadyPaid_ThrowsException() throws Exception {
        String rf = generateTestRF("ALREADY_PAID");
        billManager.issueBill(businessId1, customerIdIndiv1, 50.0, LocalDateTime.now().plusDays(5), rf);
        billManager.markBillAsPaid(rf); // First payment
        billManager.markBillAsPaid(rf); // Attempt second payment
    }

    @Test(expected = IllegalStateException.class)
    public void testMarkBillAsPaid_BillNotActive_ThrowsException() throws Exception {
        String rf = generateTestRF("NOT_ACTIVE_PAY");
        billManager.issueBill(businessId1, customerIdIndiv1, 50.0, LocalDateTime.now().plusDays(5), rf);
        Bill bill = billManager.getActiveBillByRf(rf);
        assertNotNull(bill);
        bill.setActive(false); // Manually deactivate for test (or use deactivateBillsWithRF)
        
        billManager.markBillAsPaid(rf);
    }

    // --- getBillsByRF Tests ---
    @Test
    public void testGetBillsByRF_Found() throws Exception {
        String rf = generateTestRF("RF_GET_MULTI");
        billManager.issueBill(businessId1, customerIdIndiv1, 10.0, LocalDateTime.now().plusDays(1), rf);
        billManager.issueBill(businessId2, customerIdComp1, 20.0, LocalDateTime.now().plusDays(2), rf); // Same RF, different business

        List<Bill> bills = billManager.getBillsByRF(rf);
        assertEquals(2, bills.size());
    }

    @Test
    public void testGetBillsByRF_NotFound_ReturnsEmptyList() {
        List<Bill> bills = billManager.getBillsByRF("RF_NOT_FOUND");
        assertTrue(bills.isEmpty());
    }

    // --- getActiveBillsForCustomer Tests ---
    @Test
    public void testGetActiveBillsForCustomer_Success() throws Exception {
        LocalDateTime future = LocalDateTime.now().plusDays(5);
        billManager.issueBill(businessId1, customerIdIndiv1, 10.0, future, null);
        billManager.issueBill(businessId1, customerIdIndiv1, 20.0, future, null);
        billManager.issueBill(businessId2, customerIdIndiv1, 30.0, LocalDateTime.now().minusDays(1), null); // Expired
        billManager.issueBill(businessId1, customerIdIndiv1, 40.0, future, generateTestRF("PAID_CUST"));
        billManager.markBillAsPaid(generateTestRF("PAID_CUST"));


        List<Bill> activeBills = billManager.getActiveBillsForCustomer(customerIdIndiv1);
        assertEquals(2, activeBills.size()); // Only the first two non-expired, unpaid bills
        for(Bill b : activeBills) {
            assertTrue(b.isActive());
            // assertFalse(b.isPaid());
            assertTrue(b.getExpireTime().isAfter(LocalDateTime.now()));
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetActiveBillsForCustomer_InvalidCustomerId_ThrowsException() {
        billManager.getActiveBillsForCustomer(nonExistentUserId);
    }
    
    @Test
    public void testGetActiveBillsForCustomer_NoActiveBills_ReturnsEmptyList() throws Exception {
        // customerIdIndiv1 exists but has no active bills initially or after specific setup
        billManager.issueBill(businessId1, customerIdIndiv1, 10.0, LocalDateTime.now().minusDays(1), null); // Expired
        List<Bill> activeBills = billManager.getActiveBillsForCustomer(customerIdIndiv1);
        assertTrue(activeBills.isEmpty());
    }


    // --- getActiveBillsForBusiness Tests ---
    @Test
    public void testGetActiveBillsForBusiness_Success() throws Exception {
        LocalDateTime future = LocalDateTime.now().plusDays(5);
        billManager.issueBill(businessId1, customerIdIndiv1, 10.0, future, null);
        billManager.issueBill(businessId1, customerIdComp1, 20.0, future, null);
        billManager.issueBill(businessId1, customerIdIndiv1, 30.0, LocalDateTime.now().minusDays(1), null); // Expired by businessId1
        billManager.issueBill(businessId2, customerIdIndiv1, 40.0, future, null); // Different business

        List<Bill> activeBills = billManager.getActiveBillsForBusiness(businessId1);
        assertEquals(2, activeBills.size());
         for(Bill b : activeBills) {
            assertTrue(b.isActive());
            // assertFalse(b.isPaid());
            assertTrue(b.getExpireTime().isAfter(LocalDateTime.now()));
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetActiveBillsForBusiness_InvalidBusinessId_ThrowsException() {
        billManager.getActiveBillsForBusiness(nonExistentUserId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetActiveBillsForBusiness_BusinessIdNotCompany_ThrowsException() {
        billManager.getActiveBillsForBusiness(customerIdIndiv1); // customerIdIndiv1 is an Individual
    }


    // --- getBillsForBusinessCustomerPair Tests ---
    // Assuming the method signature in BillManager.java will be changed to return ArrayList<Bill>
    @Test
    public void testGetBillsForBusinessCustomerPair_Success() throws Exception {
        String rf1 = generateTestRF("PAIR_1"); String rf2 = generateTestRF("PAIR_2"); String rf3 = generateTestRF("PAIR_3");
        billManager.issueBill(businessId1, customerIdIndiv1, 10.0, LocalDateTime.now().plusDays(1), rf1);
        billManager.issueBill(businessId1, customerIdIndiv1, 20.0, LocalDateTime.now().plusDays(2), rf2);
        billManager.markBillAsPaid(rf1); // Mark one as paid
        billManager.issueBill(businessId2, customerIdIndiv1, 30.0, LocalDateTime.now().plusDays(3), rf3); // Different business

        // Assuming getBillsForBusinessCustomerPair return type is List<Bill>
        List<Bill> pairBills = billManager.getBillsForBusinessCustomerPair(customerIdIndiv1, businessId1);
        assertEquals("Should find 2 bills for the pair", 2, pairBills.size());
        
        boolean foundRf1 = false;
        boolean foundRf2 = false;
        for(Bill b : pairBills) {
            assertEquals(businessId1, b.getBusinessId());
            assertEquals(customerIdIndiv1, b.getCustomerId());
            if(b.getRF().equals(rf1)) foundRf1 = true;
            if(b.getRF().equals(rf2)) foundRf2 = true;
        }
        assertTrue(foundRf1 && foundRf2);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetBillsForBusinessCustomerPair_InvalidCustomerId_ThrowsException() {
        billManager.getBillsForBusinessCustomerPair(nonExistentUserId, businessId1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBillsForBusinessCustomerPair_InvalidBusinessId_ThrowsException() {
        billManager.getBillsForBusinessCustomerPair(customerIdIndiv1, nonExistentUserId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetBillsForBusinessCustomerPair_BusinessIdNotCompany_ThrowsException() {
        billManager.getBillsForBusinessCustomerPair(customerIdIndiv1, customerIdIndiv1); // businessId is an Individual
    }


    // --- deactivateBillsWithRF Tests ---
    @Test
    public void testDeactivateBillsWithRF_DeactivatesAllMatchingRF() throws Exception {
        String rf = generateTestRF("DEACTIVATE_ME");
        billManager.issueBill(businessId1, customerIdIndiv1, 10.0, LocalDateTime.now().plusDays(1), rf);
        billManager.issueBill(businessId2, customerIdComp1, 20.0, LocalDateTime.now().plusDays(2), rf); // Same RF, different bill

        billManager.deactivateBillsWithRF(rf);

        List<Bill> billsAfterDeactivation = billManager.getBillsByRF(rf);
        assertEquals(2, billsAfterDeactivation.size());
        for (Bill b : billsAfterDeactivation) {
            assertFalse(b.isActive());
        }
        // Check that getActiveBillByRf now returns null
        assertNull(billManager.getActiveBillByRf(rf));
    }
    
    @Test
    public void testDeactivateBillsWithRF_NoMatchingRF_NoChange() throws Exception {
        String rfActive = generateTestRF("STILL_ACTIVE");
        billManager.issueBill(businessId1, customerIdIndiv1, 10.0, LocalDateTime.now().plusDays(1), rfActive);
        int initialCount = billManager.getBillsByRF(rfActive).size();
        
        billManager.deactivateBillsWithRF("NON_EXISTENT_RF_FOR_DEACT");
        
        Bill bill = billManager.getActiveBillByRf(rfActive);
        assertNotNull(bill);
        assertTrue(bill.isActive());
        assertEquals(initialCount, billManager.getBillsByRF(rfActive).size());
    }


    // --- getActiveBillByRf Tests ---
    @Test
    public void testGetActiveBillByRf_Found() throws Exception {
        String rf = generateTestRF("GET_ACTIVE");
        billManager.issueBill(businessId1, customerIdIndiv1, 10.0, LocalDateTime.now().plusDays(1), rf);
        Bill bill = billManager.getActiveBillByRf(rf);
        assertNotNull(bill);
        assertEquals(rf, bill.getRF());
        assertTrue(bill.isActive());
        // assertFalse(bill.isPaid());
    }

    @Test
    public void testGetActiveBillByRf_NotFound_ReturnsNull() {
        Bill bill = billManager.getActiveBillByRf("RF_NO_ACTIVE_BILL");
        assertNull(bill);
    }

    @Test
    public void testGetActiveBillByRf_BillExistsButNotActive_ReturnsNull() throws Exception {
        String rf = generateTestRF("INACTIVE_BILL_RF");
        billManager.issueBill(businessId1, customerIdIndiv1, 10.0, LocalDateTime.now().plusDays(1), rf);
        billManager.deactivateBillsWithRF(rf); // Deactivate it
        Bill bill = billManager.getActiveBillByRf(rf);
        assertNull(bill);
    }

    @Test
    public void testGetActiveBillByRf_BillExistsButPaid_ReturnsNull() throws Exception {
        String rf = generateTestRF("PAID_BILL_RF");
        billManager.issueBill(businessId1, customerIdIndiv1, 10.0, LocalDateTime.now().plusDays(1), rf);
        billManager.markBillAsPaid(rf); // Pay it
        Bill bill = billManager.getActiveBillByRf(rf);
        assertNull(bill);
    }
    
    @Test
    public void testGetActiveBillByRf_BillExistsButExpired_ReturnsNull() throws Exception {
        // This assumes getActiveBillByRf also checks expiry based on previous clarifications
        String rf = generateTestRF("EXPIRED_BILL_RF");
        billManager.issueBill(businessId1, customerIdIndiv1, 10.0, LocalDateTime.now().minusDays(1), rf); // Expired
        
        // If getActiveBillByRf ALSO checks expireTime (as it should per clarification for other active getters)
        Bill bill = billManager.getActiveBillByRf(rf);
        assertNull("Expired bill should not be returned by getActiveBillByRf if it checks expiry", bill);
        
        // If getActiveBillByRf ONLY checks b.isActive() && !b.isPaid(), then it would be found
        // and this test would need adjustment or SUT for getActiveBillByRf needs to align.
        // Based on clarification "The system should not consider a bill inactive if the expire time is after the current moment"
        // this check should be there.
    }
}
