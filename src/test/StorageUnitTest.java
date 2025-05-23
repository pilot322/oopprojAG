package test;

import managers.*;
import models.accounts.BankAccount;
import models.users.Admin;
import models.users.Company;
import models.users.Individual;
import models.users.User;
import org.junit.Before; // JUnit 4
import org.junit.Test; // JUnit 4
import system.BankSystem;

import static org.junit.Assert.*; // JUnit 4

import java.util.ArrayList;
import java.util.List;

public class StorageUnitTest {

    private BankSystem bankSystem;
    private UserManager userManager;
    int individualId1, individualId2, individualId3, adminId, companyId;

    @Before // Changed from @BeforeEach
    public void setUp() { // Method must be public for JUnit 4 @Before
        bankSystem = new BankSystem();
        userManager = bankSystem.getUserManager();

        userManager.register("Individual", "userOne", "pass", "User One", "111111111");
        individualId1 = userManager.login("userOne", "pass").getId();

        userManager.register("Individual", "userTwo", "pass", "User Two", "222222222");
        individualId2 = userManager.login("userTwo", "pass").getId();

        userManager.register("Individual", "userThree", "pass", "User Three", "010101010");
        individualId3 = userManager.login("userThree", "pass").getId();


        userManager.register("Admin", "admin", "adminPass", "Admin User", null);
        adminId = userManager.login("admin", "adminPass").getId();

        userManager.register("Company", "compOne", "compPass", "Company One", "333333333");
        companyId = userManager.login("compOne", "compPass").getId();


        try {
            bankSystem.getAccountManager().createBusinessAccount(companyId, "GR", 0.1);
            bankSystem.getAccountManager().createPersonalAccount(individualId1, "GR", 0.1, new ArrayList<>(List.of()));
            bankSystem.getAccountManager().createPersonalAccount(individualId2, "GR", 0.1, new ArrayList<>(List.of(individualId1, individualId3)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Registration Tests ---

    @Test
    public void testAttemptToMakeAdminString() { 
        String adminString = userManager.findUserById(adminId).marshal();
        System.out.println(adminString);
        // assertTrue(adminString.equals("type:Admin,id:3,legalName:Admin User,userName:admin,password:adminPass"));
    }

    @Test
    public void testAttemptToMakeIndividualString() {
        String individualString = userManager.findUserById(individualId1).marshal();
        System.out.println(individualString);
    //     assertTrue(individualString
    //             .equals("type:Individual,legalName:User One,username:userOne,password:pass,vatNumber:111111111"));
    }

    @Test
    public void testAttemptToMakeCompanyString() {
        String companyString = userManager.findUserById(companyId).marshal();
        System.out.println(companyString);
        // assertTrue(companyString
        //         .equals("type:Company,legalName:Company One,username:compOne,password:compPass,vatNumber:333333333"));
    }

    @Test
    public void testAllBankAccountsMarshal(){
        for(BankAccount b : bankSystem.getAccountManager().getAllBankAccounts()){
            System.out.println(b.marshal());
        }
    }
}
