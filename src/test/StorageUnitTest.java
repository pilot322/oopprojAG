package test;
import managers.*;

import models.users.Admin;
import models.users.Company;
import models.users.Individual;
import models.users.User;
import org.junit.Before; // JUnit 4
import org.junit.Test;  // JUnit 4
import system.BankSystem;

import static org.junit.Assert.*; // JUnit 4

public class StorageUnitTest {

    private BankSystem bankSystem;
    private UserManager userManager;
    int individualId1, individualId2, adminId, companyId;

    @Before // Changed from @BeforeEach
    public void setUp() { // Method must be public for JUnit 4 @Before
        bankSystem = new BankSystem();
        userManager = bankSystem.getUserManager();

        userManager.register("Individual", "userOne", "pass", "User One", "111111111");
        individualId1 = userManager.login("userOne", "pass").getId();

        userManager.register("Individual", "userTwo", "pass", "User Two", "222222222");
        individualId2 = userManager.login("userTwo", "pass").getId();

        userManager.register("Admin", "admin", "adminPass", "Admin User", null);
        adminId = userManager.login("admin", "adminPass").getId();

        userManager.register("Company", "compOne", "compPass", "Company One", "333333333");
        companyId = userManager.login("compOne", "compPass").getId();

    // @DisplayName("Register Individual: Successful case") // Removed
    }

    // --- Registration Tests ---

    @Test
    public void testAttemptToMakeAdminString(){
        String adminString = userManager.findUserById(adminId).marshal();
        assertTrue(adminString.equals("type:Admin,legalName:Admin User,username:admin,password:adminPass"));
    }
}
