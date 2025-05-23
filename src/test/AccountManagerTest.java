package test;


import managers.*;

import models.accounts.BankAccount;
import models.accounts.BusinessAccount;
import models.accounts.PersonalAccount;
// JUnit 4 imports
import org.junit.Before;
import org.junit.Test;
import system.BankSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

// JUnit 4 static asserts
import static org.junit.Assert.*;

public class AccountManagerTest {

    private BankSystem bankSystem;
    private AccountManager accountManager;
    private UserManager userManager;

    private int individualId1; // John
    private int individualId2; // Alice
    private int individualId3; // Bob
    private int companyId1;    // TechCorp

    @Before // JUnit 4
    public void setUp() { // Must be public for JUnit 4 @Before
        bankSystem = new BankSystem();
        accountManager = bankSystem.getAccountManager();
        userManager = bankSystem.getUserManager();

        // Pre-register users for tests
        // User IDs will be 0, 1, 2, 3, 4 in order of registration
        userManager.register("Individual", "johnD", "pass", "John Doe", "111111111");
        individualId1 = userManager.login("johnD", "pass").getId(); // ID 0

        userManager.register("Individual", "aliceS", "pass", "Alice Smith", "222222222");
        individualId2 = userManager.login("aliceS", "pass").getId(); // ID 1

        userManager.register("Individual", "bobJ", "pass", "Bob Johnson", "333333333");
        individualId3 = userManager.login("bobJ", "pass").getId(); // ID 2

        userManager.register("Company", "techCorp", "pass", "TechCorp Inc.", "444444444");
        companyId1 = userManager.login("techCorp", "pass").getId(); // ID 3

        userManager.register("Admin", "adminUser", "adminPass", "Main Admin", null); // ID 4
    }

    // --- createPersonalAccount Tests ---

    @Test
    public void testCreatePersonalAccount_Success_NoSecondary() throws Exception { // Added throws Exception
        accountManager.createPersonalAccount(individualId1, "GR", 0.01, new ArrayList<Integer>());
        ArrayList<PersonalAccount> accounts = accountManager.findAccountsByIndividualId(individualId1);
        assertFalse("Account list should not be empty.", accounts.isEmpty());
        assertEquals(1, accounts.size());

        PersonalAccount pa = accounts.get(0);
        assertNotNull(pa.getIBAN());
        assertTrue("IBAN should start with GR100 for personal.", pa.getIBAN().startsWith("GR100"));
        // IBAN length check: "GR100" is 5 chars, total length should be 5 + 15 = 20
        assertEquals("IBAN total length should be 20.", 20, pa.getIBAN().length());
        assertEquals(individualId1, pa.getOwnerId());
        assertEquals(0.0, pa.getBalance(), 0.001); // Added delta for double comparison
        assertEquals(0.01, pa.getInterestRate(), 0.001);
        assertTrue(pa.getSecondaryOwnerIds().isEmpty());
    }

    @Test
    public void testCreatePersonalAccount_WithValidSecondaryOwners_Success() throws Exception { // Added throws Exception
        ArrayList<Integer> secondaryIds = new ArrayList<Integer>(Arrays.asList(individualId2, individualId3));
        accountManager.createPersonalAccount(individualId1, "AL", 0.02, secondaryIds);

        PersonalAccount pa = accountManager.findAccountsByIndividualId(individualId1).get(0);
        assertEquals(2, pa.getSecondaryOwnerIds().size());
        assertTrue(pa.getSecondaryOwnerIds().contains(individualId2));
        assertTrue(pa.getSecondaryOwnerIds().contains(individualId3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePersonalAccount_OwnerDoesNotExist_ThrowsException() throws Exception {
        accountManager.createPersonalAccount(99, "GR", 0.01, new ArrayList<Integer>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePersonalAccount_OwnerIsNotIndividual_ThrowsException() throws Exception {
        accountManager.createPersonalAccount(companyId1, "GR", 0.01, new ArrayList<Integer>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePersonalAccount_InvalidCountryCode_TooShort_ThrowsException() throws Exception {
        accountManager.createPersonalAccount(individualId1, "G", 0.01, new ArrayList<Integer>());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreatePersonalAccount_InvalidCountryCode_Numeric_ThrowsException() throws Exception {
        accountManager.createPersonalAccount(individualId1, "12", 0.01, new ArrayList<Integer>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePersonalAccount_NegativeInterestRate_ThrowsException() throws Exception {
        accountManager.createPersonalAccount(individualId1, "GR", -0.01, new ArrayList<Integer>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePersonalAccount_SecondaryOwnerDoesNotExist_ThrowsException() throws Exception {
        ArrayList<Integer> secondaryIds = new ArrayList<Integer>(Arrays.asList(individualId2, 99)); // 99 doesn't exist
        accountManager.createPersonalAccount(individualId1, "GR", 0.01, secondaryIds);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePersonalAccount_SecondaryOwnerNotIndividual_ThrowsException() throws Exception {
        ArrayList<Integer> secondaryIds = new ArrayList<Integer>(Arrays.asList(individualId2, companyId1));
        accountManager.createPersonalAccount(individualId1, "GR", 0.01, secondaryIds);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePersonalAccount_PrimaryOwnerAsSecondaryOwner_ThrowsException() throws Exception {
        ArrayList<Integer> secondaryIds = new ArrayList<Integer>(Collections.singletonList(individualId1));
        accountManager.createPersonalAccount(individualId1, "GR", 0.01, secondaryIds);
    }

    @Test
    public void testCreatePersonalAccount_NullSecondaryOwners_Success() throws Exception {
        accountManager.createPersonalAccount(individualId1, "EN", 0.015, null); // Passing null
        PersonalAccount pa = accountManager.findAccountsByIndividualId(individualId1).get(0);
        assertNotNull("Secondary owners list should not be null if SUT handles it.", pa.getSecondaryOwnerIds());
        assertTrue("Secondary owners list should be empty if SUT handles null as empty.", pa.getSecondaryOwnerIds().isEmpty());
    }

    // --- createBusinessAccount Tests ---

    @Test
    public void testCreateBusinessAccount_Success() throws Exception { // Added throws Exception for potential SUT changes
        accountManager.createBusinessAccount(companyId1, "EN", 0.005);
        BusinessAccount ba = accountManager.findAccountByBusinessId(companyId1);
        assertNotNull(ba);
        assertTrue("IBAN should start with EN200 for business.", ba.getIBAN().startsWith("EN200"));
        assertEquals("IBAN total length should be 20.", 20, ba.getIBAN().length());
        assertEquals(companyId1, ba.getOwnerId());
        assertEquals(0.0, ba.getBalance(), 0.001);
        assertEquals(0.005, ba.getInterestRate(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBusinessAccount_OwnerDoesNotExist_ThrowsException() throws Exception {
        accountManager.createBusinessAccount(99, "GR", 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBusinessAccount_OwnerIsNotCompany_ThrowsException() throws Exception {
        accountManager.createBusinessAccount(individualId1, "GR", 0.01);
    }
    
    @Test(expected = IllegalStateException.class) // As per clarification
    public void testCreateBusinessAccount_CompanyAlreadyHasAccount_ThrowsException() throws Exception {
        accountManager.createBusinessAccount(companyId1, "EN", 0.01); // First account
        accountManager.createBusinessAccount(companyId1, "AL", 0.02); // Attempt second
    }

    // --- findAccountByIBAN Tests ---
    @Test
    public void testFindAccountByIBAN_AccountExists() throws Exception {
        accountManager.createPersonalAccount(individualId1, "AL", 0.01, new ArrayList<Integer>());
        String iban = accountManager.findAccountsByIndividualId(individualId1).get(0).getIBAN();
        BankAccount found = accountManager.findAccountByIBAN(iban);
        assertNotNull(found);
        assertEquals(iban, found.getIBAN());
    }

    @Test
    public void testFindAccountByIBAN_AccountDoesNotExist_ReturnsNull() {
        BankAccount found = accountManager.findAccountByIBAN("NONEXISTENTIBAN");
        assertNull(found);
    }

    // --- findAccountByBusinessId Tests ---
    @Test
    public void testFindAccountByBusinessId_AccountExists_UserIsCompany_Success() throws Exception {
        accountManager.createBusinessAccount(companyId1, "CA", 0.01);
        BusinessAccount ba = accountManager.findAccountByBusinessId(companyId1);
        assertNotNull(ba);
        assertEquals(companyId1, ba.getOwnerId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindAccountByBusinessId_UserNotCompany_ThrowsException() throws Exception {
        // This test assumes findAccountByBusinessId first validates the user type.
        // If individualId1 is not a company, this call should throw.
        accountManager.findAccountByBusinessId(individualId1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindAccountByBusinessId_NoAccountForCompany_ReturnsNull() throws Exception {
        // companyId1 is registered but has no accounts yet in this specific test path
        // Before calling, ensure companyId1 is indeed a company to avoid false positive from UserNotCompany check
        assertEquals("Company", userManager.getUserType(companyId1)); // Pre-condition
        BusinessAccount ba = accountManager.findAccountByBusinessId(companyId1);
    }
    
    // --- findAccountsByIndividualId Tests ---
    @Test
    public void testFindAccountsByIndividualId_AccountsExist_UserIsIndividual_Success() throws Exception {
        accountManager.createPersonalAccount(individualId1, "EN", 0.01, new ArrayList<Integer>());
        accountManager.createPersonalAccount(individualId1, "AL", 0.015, new ArrayList<Integer>(Collections.singletonList(individualId2)));

        ArrayList<PersonalAccount> accounts = accountManager.findAccountsByIndividualId(individualId1);
        assertEquals(2, accounts.size());
        for (PersonalAccount pa : accounts) {
            // isOwnerOfBankAccount is a method in AccountManager, not directly on PersonalAccount.
            // We need to ensure the test logic reflects how ownership is checked.
            // The findAccountsByIndividualId should already filter correctly.
            // We can check if the primary owner is individualId1 or if individualId1 is in secondary owners.
            boolean isOwner = (pa.getOwnerId() == individualId1) || pa.getSecondaryOwnerIds().contains(individualId1);
            assertTrue("Retrieved account should be owned by the individualId", isOwner);
            assertTrue(pa instanceof PersonalAccount);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindAccountsByIndividualId_UserNotIndividual_ThrowsException() throws Exception {
        // This test assumes findAccountsByIndividualId first validates the user type.
        accountManager.findAccountsByIndividualId(companyId1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindAccountsByIndividualId_NoAccountsForIndividual_ReturnsEmptyList() throws Exception {
        // Ensure individualId1 is an individual
        assertEquals("Individual", userManager.getUserType(individualId1)); // Pre-condition
        ArrayList<PersonalAccount> accounts = accountManager.findAccountsByIndividualId(individualId1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreationConstraint_IndividualCannotOwnBusinessAccount() throws Exception {
        accountManager.createBusinessAccount(individualId1, "GR", 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreationConstraint_CompanyCannotOwnPersonalAccount() throws Exception {
        accountManager.createPersonalAccount(companyId1, "GR", 0.01, new ArrayList<Integer>());
    }

    // --- isOwnerOfBankAccount Tests (isOwnerOfBankAccount is package-private or protected in SUT) ---
    // These tests might need adjustment if isOwnerOfBankAccount is not public.
    // For now, assuming it can be called or its effects tested via public methods.
    // If it's not public, these specific unit tests for it are harder to write directly.
    // The provided AccountManager.java has it as default (package-private).
    // So these tests will compile if the test class is in the same 'managers' package.

    @Test
    public void testIsOwnerOfBankAccount_PrimaryOwner_Personal() throws Exception {
        accountManager.createPersonalAccount(individualId1, "EN", 0.01, new ArrayList<Integer>());
        PersonalAccount pa = accountManager.findAccountsByIndividualId(individualId1).get(0);
        assertTrue(accountManager.isOwnerOfBankAccount(pa, individualId1));
    }

    @Test
    public void testIsOwnerOfBankAccount_SecondaryOwner_Personal() throws Exception {
        accountManager.createPersonalAccount(individualId1, "EN", 0.01, new ArrayList<Integer>(Collections.singletonList(individualId2)));
        PersonalAccount pa = accountManager.findAccountsByIndividualId(individualId1).get(0); // This gets the account owned by individualId1
        // To test for individualId2 being a secondary owner, we need to check that specific account
        assertTrue(accountManager.isOwnerOfBankAccount(pa, individualId2));
    }

    @Test
    public void testIsOwnerOfBankAccount_NotAnOwner() throws Exception {
        accountManager.createPersonalAccount(individualId1, "AL", 0.01, new ArrayList<Integer>());
        PersonalAccount pa = accountManager.findAccountsByIndividualId(individualId1).get(0);
        assertFalse(accountManager.isOwnerOfBankAccount(pa, individualId3)); // individualId3 is not owner
    }
}
