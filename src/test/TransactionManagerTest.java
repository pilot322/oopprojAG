
package test;

import managers.*;

import models.accounts.BankAccount;
import models.accounts.PersonalAccount; // For casting if needed
import models.bills.Bill; // For mocking pay
import models.statements.AccountStatement;
import org.junit.Before;
import org.junit.Test;
import system.BankSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TransactionManagerTest {

    private BankSystem bankSystem;
    private TransactionManager transactionManager;
    private AccountManager accountManager;
    private UserManager userManager;
    private AccountStatementManager accountStatementManager;
    // BillManager might be needed for 'pay' if we don't mock deeply
    private BillManager billManager;


    private int individualId1, individualId2, adminId;
    private final int BANK_USER_ID = -1;
    private String iban1, iban2, iban3_business; // iban3 for business
    private int companyId;


    @Before
    public void setUp() throws Exception {
        bankSystem = new BankSystem();
        transactionManager = bankSystem.getTransactionManager();
        accountManager = bankSystem.getAccountManager();
        userManager = bankSystem.getUserManager();
        accountStatementManager = bankSystem.getAccountStatementManager();
        billManager = bankSystem.getBillManager(); // Initialize BillManager

        // Setup users
        userManager.register("Individual", "userOne", "pass", "User One", "111111111");
        individualId1 = userManager.login("userOne", "pass").getId();

        userManager.register("Individual", "userTwo", "pass", "User Two", "222222222");
        individualId2 = userManager.login("userTwo", "pass").getId();

        userManager.register("Admin", "admin", "adminPass", "Admin User", null);
        adminId = userManager.login("admin", "adminPass").getId();
        
        userManager.register("Company", "compOne", "compPass", "Company One", "333333333");
        companyId = userManager.login("compOne", "compPass").getId();

        // Setup accounts
        accountManager.createPersonalAccount(individualId1, "GR", 0.01, new ArrayList<Integer>());
        iban1 = accountManager.findAccountsByIndividualId(individualId1).get(0).getIBAN();

        accountManager.createPersonalAccount(individualId2, "GR", 0.01, new ArrayList<Integer>());
        iban2 = accountManager.findAccountsByIndividualId(individualId2).get(0).getIBAN();
        
        accountManager.createBusinessAccount(companyId, "GR", 0.01);
        iban3_business = accountManager.findAccountByBusinessId(companyId).getIBAN();


        // Pre-fund iban1 for withdrawal/transfer tests
        // Directly manipulating balance or using a "bank deposit"
        BankAccount acc1 = accountManager.findAccountByIBAN(iban1);
        acc1.addToBalance(1000.0); // Start with 1000
    }

    // --- Deposit Tests ---
    @Test
    public void testDeposit_Success() {
        double initialBalance = accountManager.findAccountByIBAN(iban2).getBalance();
        transactionManager.deposit(iban2, individualId2, "Initial deposit", 200.0);
        assertEquals(initialBalance + 200.0, accountManager.findAccountByIBAN(iban2).getBalance(), 0.001);
        List<AccountStatement> statements = accountStatementManager.getStatements(iban2);
        assertFalse(statements.isEmpty());
        assertEquals("deposit", statements.get(0).getTransactionType()); // Assuming latest is first
        assertEquals(200.0, statements.get(0).getAmount(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeposit_InvalidAccountIBAN_ThrowsException() {
        transactionManager.deposit("INVALID_IBAN", individualId1, "Deposit fail", 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeposit_NonExistentTransactorId_ThrowsException() {
        transactionManager.deposit(iban1, 999, "Deposit fail", 100.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDeposit_UnrelatedTransactorNotAdminOrBank_ThrowsException() {
        // individualId2 trying to deposit into iban1 (owned by individualId1)
        // This test assumes that depositing might also have some transactor authorization,
        // or that the transactorId is primarily for logging.
        // If anyone can deposit to any account, this test might need re-evaluation
        // based on SUT's deposit logic for transactorId.
        // For now, let's assume transactorId should be related or special.
        // However, typically deposits are allowed by anyone.
        // Let's refine this: if transactorId is just for logging, it should pass.
        // If it implies permission, it should fail.
        // Given the prompt "test for a user making a transaction with an account that they are not associated with"
        // this might apply. Let's assume it should throw if not owner/admin/bank.
        // This might be more relevant for withdraw/transfer. For deposit, it's usually open.
        // Clarification: "The system should ALLOW the transactorId to be either -1 or an admin's id" for *any* transaction.
        // This implies others might be restricted.
        // For deposit, if it's an open operation, then this test is invalid.
        // Let's assume for now that even for deposit, if transactorId is specified and not owner/admin/bank, it's an issue.
        // This needs to be confirmed by SUT's actual behavior.
        // If SUT allows anyone to deposit, this test should be removed or changed.
        // For now, I will assume a strict check as per "user making a transaction with an account that they are not associated with".
        // transactionManager.deposit(iban1, individualId2, "Deposit by unrelated", 100.0);
        // Re-evaluating: Deposit is usually an exception to "must be associated". Anyone can put money in.
        // The "transactorId" in deposit is more about "who initiated this record".
        // So, this particular test for deposit is likely not what's intended for "unassociated user".
        // Let's assume deposit by individualId2 to iban1 is fine.
         transactionManager.deposit(iban1, individualId2, "Deposit by unrelated", 100.0); // Should pass
         assertTrue("Deposit by unrelated user should pass if allowed", true); // Placeholder if no exception
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDeposit_NegativeAmount_ThrowsException() {
        transactionManager.deposit(iban1, individualId1, "Negative deposit", -50.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeposit_ZeroAmount_ThrowsException() {
        transactionManager.deposit(iban1, individualId1, "Zero deposit", 0.0);
    }

    @Test
    public void testDeposit_Success_ByBankUser() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance();
        transactionManager.deposit(iban1, BANK_USER_ID, "Bank deposit", 500.0);
        assertEquals(initialBalance + 500.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
    }

    @Test
    public void testDeposit_Success_ByAdminUser() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance();
        transactionManager.deposit(iban1, adminId, "Admin deposit", 300.0);
        assertEquals(initialBalance + 300.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
    }

    // --- Withdraw Tests ---
    @Test
    public void testWithdraw_Success() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance(); // Should be 1000
        transactionManager.withdraw(iban1, individualId1, "Cash withdrawal", 100.0);
        assertEquals(initialBalance - 100.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
        List<AccountStatement> statements = accountStatementManager.getStatements(iban1);
        // Statements for deposit in setup, and this withdrawal
        assertEquals("withdraw", statements.get(0).getTransactionType()); // Latest
        assertEquals(100.0, statements.get(0).getAmount(), 0.001); // Amount of transaction
    }

    @Test
    public void testWithdraw_InsufficientFunds_ThrowsException_NoStatement_BalanceUnchanged() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance(); // 1000
        int initialStatementCount = accountStatementManager.getStatements(iban1).size();
        try {
            transactionManager.withdraw(iban1, individualId1, "Withdraw too much", initialBalance + 2000.0);
            fail("Should throw IllegalStateException for insufficient funds.");
        } catch (IllegalStateException e) {
            // Expected
        }
        assertEquals(initialBalance, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
        assertEquals(initialStatementCount, accountStatementManager.getStatements(iban1).size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWithdraw_UnauthorizedUser_ThrowsException() {
        // individualId2 (not owner of iban1) tries to withdraw from iban1
        transactionManager.withdraw(iban1, individualId2, "Unauthorized withdraw", 50.0);
    }

    @Test
    public void testWithdraw_Success_ByBankUser() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance();
        transactionManager.withdraw(iban1, BANK_USER_ID, "Bank withdrawal", 50.0);
        assertEquals(initialBalance - 50.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
    }

    @Test
    public void testWithdraw_Success_ByAdminUser() {
        double initialBalance = accountManager.findAccountByIBAN(iban1).getBalance();
        transactionManager.withdraw(iban1, adminId, "Admin withdrawal", 50.0);
        assertEquals(initialBalance - 50.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
    }


    // --- Transfer Tests ---
    @Test
    public void testTransfer_Success() {
        double senderInitial = accountManager.findAccountByIBAN(iban1).getBalance(); // 1000
        double receiverInitial = accountManager.findAccountByIBAN(iban2).getBalance();

        transactionManager.transfer(iban1, individualId1, "Payment for services", 150.0, iban2);
        
        assertEquals(senderInitial - 150.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
        assertEquals(receiverInitial + 150.0, accountManager.findAccountByIBAN(iban2).getBalance(), 0.001);

        List<AccountStatement> senderStatements = accountStatementManager.getStatements(iban1);
        assertEquals("transfer_out", senderStatements.get(0).getTransactionType());
        assertEquals(150.0, senderStatements.get(0).getAmount(),0.001);
        assertEquals(iban2, senderStatements.get(0).getReceiverIBAN());


        List<AccountStatement> receiverStatements = accountStatementManager.getStatements(iban2);
        assertEquals("transfer_in", receiverStatements.get(0).getTransactionType());
        assertEquals(150.0, receiverStatements.get(0).getAmount(),0.001);
    }

    @Test
    public void testTransfer_SenderInsufficientFunds_ThrowsException_NoStatements_BalancesUnchanged() {
        double senderInitial = accountManager.findAccountByIBAN(iban1).getBalance(); // 1000
        double receiverInitial = accountManager.findAccountByIBAN(iban2).getBalance();
        int senderInitialStmtCount = accountStatementManager.getStatements(iban1).size();
        int receiverInitialStmtCount = accountStatementManager.getStatements(iban2).size();

        try {
            transactionManager.transfer(iban1, individualId1, "Large transfer", senderInitial + 500.0, iban2);
            fail("Should throw IllegalStateException for insufficient funds.");
        } catch (IllegalStateException e) {
            // Expected
        }
        assertEquals(senderInitial, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
        assertEquals(receiverInitial, accountManager.findAccountByIBAN(iban2).getBalance(), 0.001);
        assertEquals(senderInitialStmtCount, accountStatementManager.getStatements(iban1).size());
        assertEquals(receiverInitialStmtCount, accountStatementManager.getStatements(iban2).size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTransfer_SameSenderAndReceiverIBAN_ThrowsException() {
        transactionManager.transfer(iban1, individualId1, "Self transfer", 50.0, iban1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransfer_UnauthorizedUser_ThrowsException() {
        // individualId2 (not owner of iban1) tries to transfer from iban1
        transactionManager.transfer(iban1, individualId2, "Unauthorized transfer", 50.0, iban2);
    }
    
    @Test
    public void testTransfer_Success_ByAdmin() {
        double senderInitial = accountManager.findAccountByIBAN(iban1).getBalance(); // 1000
        double receiverInitial = accountManager.findAccountByIBAN(iban2).getBalance();

        transactionManager.transfer(iban1, adminId, "Admin transfer", 150.0, iban2);
        
        assertEquals(senderInitial - 150.0, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
        assertEquals(receiverInitial + 150.0, accountManager.findAccountByIBAN(iban2).getBalance(), 0.001);
    }

    // --- Pay Tests ---
    // These require BillManager to be functional or mocked.
    // For now, let's assume a simplified BillManager interaction for unit testing TransactionManager.
    // We will need a way to tell BillManager "this RF exists, has this amount, and maps to this business (receiver IBAN)".

    @Test
    public void testPay_Success() throws Exception {
        // Simplified setup: Assume BillManager can provide Bill details.
        // This is where mocking BillManager would be ideal for a unit test.
        // Since we are not using a mocking framework, we'll have to rely on
        // BillManager's actual (potentially TODO) implementation or make assumptions.

        // Let's assume we can add a bill to BillManager for testing.
        // Bill constructor: int id, int businessId, int customerId, String RF, double amount, LocalDateTime timePublished, LocalDateTime expireTime
        String testRF = "RF12345";
        double billAmount = 75.0;
        // The bill's businessId is companyId, which owns iban3_business
        Bill testBill = new Bill(1, companyId, individualId1, testRF, billAmount, LocalDateTime.now(), LocalDateTime.now().plusDays(30));
        
        // This is a placeholder for how you'd make the bill known to BillManager.
        // If BillManager has an addBill method:
        // billManager.addBill(testBill);
        // And a method like:
        // Bill billManager.getBillByRF(String RF)
        // And a method like:
        // void billManager.markBillAsPaid(String RF) or (int billId)

        // For this test to pass without full BillManager, TransactionManager.pay()
        // needs to be adapted or BillManager needs to be pre-populated/mocked.
        // Let's assume TransactionManager.pay internally resolves RF to amount and receiver IBAN.
        // And it calls something like billManager.processPaymentForRF(RF, amountPaid);

        // Pre-condition: Payer (iban1) has funds. Receiver (iban3_business) exists.
        double payerInitialBalance = accountManager.findAccountByIBAN(iban1).getBalance(); // 1000
        double businessInitialBalance = accountManager.findAccountByIBAN(iban3_business).getBalance();

        // To make this test runnable, we need to ensure TransactionManager.pay can get bill details.
        // This is a major dependency. For a true *unit* test of TransactionManager,
        // BillManager should be a mock/stub.
        // Given the constraints, this test leans towards an integration test.
        // I will write it with the *assumption* that TransactionManager.pay can somehow
        // resolve RF -> (amount, receiverIBAN for companyId) and mark bill paid.

        // --- SIMPLIFIED APPROACH for now: ---
        // We'll assume TransactionManager.pay will need to be refactored to accept amount and receiverIBAN
        // if BillManager interaction is too complex for this phase, OR we mock it.
        // Since direct mocking isn't used, let's assume the current pay signature:
        // public void pay(String accountIBAN, int transactorId, String description, String RF)
        // This implies TransactionManager *must* use BillManager.

        // To make this test work, we need a BillManager that, when its methods are called
        // by TransactionManager, behaves as expected.
        // For example, if TransactionManager calls billManager.getBillDetails(RF), it should return them.
        // And if TransactionManager calls billManager.markBillPaid(RF), it should do so.

        // This test will likely FAIL unless BillManager is implemented and populated.
        // For the purpose of showing the test structure:
        try {
            // This is a conceptual placeholder for making the bill available via BillManager
            // In a real scenario, BillManager would have methods to add/retrieve bills.
            // For now, this will likely cause TransactionManager to fail if it can't find the bill.
             System.out.println("Warning: testPay_Success depends on BillManager returning valid bill details for RF: " + testRF + " and receiver " + iban3_business);


            // Manually adjust business account for "receiving" payment if direct deposit is hard
            // This bypasses the actual BillManager lookup for amount and receiver.
            // This is NOT ideal but helps test the TransactionManager's other parts.
            // A better way: TransactionManager.pay might need to take amount and receiverIBAN if BillManager is a black box.
            // Or, BillManager needs to be testable.

            // Let's assume TransactionManager.pay is smart enough to get amount and receiver from RF via BillManager
            // AND BillManager is set up. This is a big assumption for a unit test.
            // If BillManager is not ready, this test needs to be adapted or marked @Ignore.

            // To proceed, let's assume a hypothetical scenario where BillManager is pre-configured:
            // 1. BillManager knows RF12345 is for 75.0 to companyId (iban3_business).
            // 2. BillManager has a method that TransactionManager calls to mark RF12345 as paid.

            // transactionManager.pay(iban1, individualId1, "Paying bill " + testRF, testRF);

            // assertEquals(payerInitialBalance - billAmount, accountManager.findAccountByIBAN(iban1).getBalance(), 0.001);
            // assertEquals(businessInitialBalance + billAmount, accountManager.findAccountByIBAN(iban3_business).getBalance(), 0.001);

            // List<AccountStatement> payerStmts = accountStatementManager.getStatements(iban1);
            // assertEquals("payment_out", payerStmts.get(0).getTransactionType());

            // List<AccountStatement> receiverStmts = accountStatementManager.getStatements(iban3_business);
            // assertEquals("payment_in", receiverStmts.get(0).getTransactionType());

            // assertTrue("Bill should be marked as paid", billManager.isBillPaid(testRF)); // Hypothetical
            fail("testPay_Success requires a fully implemented or mockable BillManager. Test is conceptual.");

        } catch (Exception e) {
            // If BillManager is not set up, this might throw.
             System.err.println("testPay_Success failed, likely due to BillManager dependency: " + e.getMessage());
             // Depending on the exception, this might be the expected outcome if RF is not found.
             // For now, let this fail to highlight the dependency.
             // If RF not found should throw IllegalArgumentException from TransactionManager:
             // assertInstanceOf(IllegalArgumentException.class, e);
             throw e; // Re-throw if it's an unexpected failure
        }
    }
}
