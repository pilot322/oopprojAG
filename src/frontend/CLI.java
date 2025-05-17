package frontend;

import java.util.*;

import models.accounts.BankAccount;
import models.accounts.BusinessAccount;
import models.accounts.PersonalAccount;
import models.users.Admin;
import models.users.Company;
import models.users.Individual;
import models.users.User;
import system.BankSystem;

public class CLI {
    public static Scanner input = new Scanner(System.in);
    public static BankSystem system = new BankSystem();
    static User currentUser = null;
    static String currentBankAccountIBAN = null;

    public static void main(String[] args) {
        // sthn arxh, register h login
        int choice;
        system.getUserManager().register("Individual", "abcd", "1234", "aaa", "123456789");
        system.getUserManager().register("Individual", "dcba", "1234", "bbb", "987654321");
        while (true) {
            System.out.println("PRESS 1 FOR LOGIN");
            System.out.println("PRESS 2 TO REGISTER");
            System.out.println("PRESS 3 TO EXIT");
            choice = input.nextInt();
            if (choice == 1) {
                attemptToLogin();

            } else if (choice == 2) {
                attemptToRegister();
                attemptToLogin();
            } else if (choice == 3) {
                System.out.println("Goodbye.");
                break;
            } else {
                System.out.println("Invalid choice, attempting to log in");
                attemptToLogin();
            }

            mainloop();
        }

        // afoy kanoyme login, tha mas deixnei to kyrio menu

        // kai tha prepei na ftiaksoyme thn roh kathe epilogh

    }

    static void attemptToLogin() {
        while (true) {
            System.out.println("Enter username:");
            String username = input.next();
            System.out.println("Enter password:");
            String password = input.next();

            User loginUser = system.getUserManager().login(username, password);

            if (loginUser != null) {
                currentUser = loginUser;
                break;
            } else {
                System.out.println("Wrong credentials, try again.");
            }
        }
    }

    static void attemptToRegister() {

        while (true) {
            String type = getStringWithDefensive("Enter type of account:",
                    Arrays.asList("Individual", "Company", "Admin"));

            System.out.println("Create username:");
            String username = input.next();
            System.out.println("Create password:");
            String password = input.next();
            System.out.println("Enter legal name:");
            String legalName = input.next();
            String VAT = null;
            if (!type.equals("Admin")) {
                System.out.println("Enter VAT");
                VAT = input.next();
            }

            try {
                system.getUserManager().register(type, username, password, legalName, VAT);
                System.out.println("Register successful!");
                return;
            } catch (Exception e) {
                System.out.println("Error in registration, please try again.");
                System.out.printf("Reason: %s\n", e.getMessage());
            }

        }

    }

    static String getStringWithDefensive(String prompt, List<String> values) {
        System.out.print(prompt);

        while (true) {
            String temp = input.next();

            for (String val : values) {
                if (temp.equals(val)) {
                    return temp;
                }
            }
            System.out.println("Value must be one of the following: ");
            for (String val : values) {
                System.out.println(val);
            }
            System.out.print("> ");
        }
    }

    static void attemptToCreateNewPersonalBankAccount() {
        // vhma 1: country code
        String countryCode = getStringWithDefensive("Enter country code: ", Arrays.asList("GR", "EN", "AL"));

        System.out.println("Give secondary owner ids: (-1 to stop)");

        // ftiaxnw thn lista
        ArrayList<Integer> secondaryOwnerIds = new ArrayList<>();

        // edw thn gemizw
        while (true) {
            int inp = input.nextInt();
            if (inp == -1) {
                break;
            } else if (inp < 0) {
                System.out.println("Invalid id, insert positive or -1 to exit.");
                continue;
            }
            secondaryOwnerIds.add(inp);
        }

        // thn dinw sthn createPersonalAccount
        try {
            system.getAccountManager().createPersonalAccount(currentUser.getId(), countryCode, 0.1, secondaryOwnerIds);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void mainloop() {
        if (currentUser instanceof Individual) {
            choosePersonalAccount();
            IndividualCLI.individualMainLoop();
        } else if (currentUser instanceof Company) {
            chooseCompanyAccount();
            CompanyCLI.companyMainLoop();
        } else if (currentUser instanceof Admin){
            adminMainLoop();
        }
    }

    // prepei na anathesei sto currentIBAN to iban toy account poy theloyme
    static void choosePersonalAccount() {
        int choice;
        while (true) {
            System.out.println("Pick an account or create one. ");
            System.out.println("0. New personal account");

            // ektypwsh olwn twn bankaccount
            ArrayList<PersonalAccount> accounts = new ArrayList<>();
            
            try {
                accounts = system.getAccountManager()
                        .findAccountsByIndividualId(currentUser.getId());
            } catch(Exception e){
                // ..
            }

            for (int i = 0; i < accounts.size(); i++) {
                System.out.printf("Account no%d: %s\n", i + 1, accounts.get(i).getIBAN());
            }

            choice = input.nextInt();

            if (choice < 0 || choice > accounts.size()) {
                System.out.println("Invalid, try again.");
                continue;
            }

            if (choice == 0) {
                attemptToCreateNewPersonalBankAccount();
                continue;
            }

            BankAccount b = accounts.get(choice - 1);
            currentBankAccountIBAN = b.getIBAN();

            break;
        }

        // 0: new personal
    }

    static void chooseCompanyAccount() {
        // 1. an yparxei business account sto onoma toy:
        // tha epileksei afto aytomata kai tha proxwrhsei
        try {
            BusinessAccount ba = system.getAccountManager().findAccountByBusinessId(currentUser.getId());
            // an den skasei h apo panw grammh, shmainei oti to ba yparxei
            currentBankAccountIBAN = ba.getIBAN();
            return;
        } catch (Exception e) {
            // den kanw tpt
        }
        // 2. an den yparxei: dhmioyrgia kainoyrioy
        // an den yparxei to ba, mpainoyme edw

        // attemptToCreateNewBusinessBankAccount();
        try {
            String countryCode = getStringWithDefensive("Enter country code: ", Arrays.asList("GR", "EN", "AL"));
            system.getAccountManager().createBusinessAccount(currentUser.getId(), countryCode, 0.1);
            BusinessAccount ba = system.getAccountManager().findAccountByBusinessId(currentUser.getId());
            // an den skasei h apo panw grammh, shmainei oti to ba yparxei
            currentBankAccountIBAN = ba.getIBAN();
            return;
        } catch (Exception e) {
            // ...
            e.printStackTrace();
        }

        // int choice;
        // while (true) {
        // System.out.println("Pick an account or create one. ");
        // System.out.println("0. New Company account");
        // ArrayList<BusinessAccount> accounts =
        // system.getAccountManager().findAccountByBusinessId(currentUser.getId());
        // for (int i = 0; i < accounts.size(); i++) {
        // System.out.printf("Account no%d: %s\n", i + 1, accounts.get(i).getIBAN());
        // }

        // choice = input.nextInt();

        // if (choice < 0 || choice > accounts.size()) {
        // System.out.println("Invalid, try again.");
        // continue;
        // }

        // if (choice == 0) {
        // attemptToCreateNewPersonalBankAccount();
        // continue;
        // }

        // BankAccount b = accounts.get(choice - 1);
        // currentBankAccountIBAN = b.getIBAN();

        // break;
        // }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////// VOITHIAAAAAAAAAAAAAAAAA
    /////////////////////////////////////////////////////////////////////////////////////////////// DEN
    /////////////////////////////////////////////////////////////////////////////////////////////// KATALAVAINW
    static void withdrawMenu() {
        // Get the current account
        BankAccount account = system.getAccountManager().findAccountByIBAN(currentBankAccountIBAN);

        // Verify account exists
        if (account == null) {
            System.out.println("Error: Account not found!");
            return;
        }

        // Get withdrawal amount from user
        System.out.println("Enter amount to withdraw:");
        double amount;
        try {
            amount = input.nextDouble();

            // Validate amount is positive
            if (amount <= 0) {
                System.out.println("Amount must be positive!");
                return;
            }

            // Verify sufficient balance
            if (amount > account.getBalance()) {
                System.out.println("Insufficient balance!");
                return;
            }

            // Execute withdrawal through TransactionManager
            boolean success = system.getTransactionManager().withdraw(
                    currentBankAccountIBAN,
                    currentUser.getId(),
                    "ATM Withdrawal",
                    amount);

            if (success) {
                System.out.printf("Successfully withdrew %.2f €\n", amount);
                System.out.printf("New balance: %.2f €\n", account.getBalance());
            } else {
                System.out.println("Withdrawal failed!");
            }

        } catch (InputMismatchException e) {
            System.out.println("Invalid amount entered!");
            input.next(); // Clear the invalid input
        } catch (Exception e) {
            System.out.println("Error during withdrawal: " + e.getMessage());
        }
    }

    static void depositMenu() {
        // Get the current account
        BankAccount account = system.getAccountManager().findAccountByIBAN(currentBankAccountIBAN);

        // Verify account exists
        if (account == null) {
            System.out.println("Error: Account not found!");
            return;
        }

        try {
            // Get deposit amount from user
            System.out.println("Enter amount to deposit:");
            double amount = input.nextDouble();

            // Validate amount is positive
            if (amount <= 0) {
                System.out.println("Amount must be positive!");
                return;
            }

            // Get deposit description
            System.out.println("Enter deposit description:");
            String description = input.nextLine();
            while(description.equals("")){
                description = input.nextLine();
            }

            // Execute deposit through TransactionManager
            boolean success = system.getTransactionManager().deposit(
                    currentBankAccountIBAN,
                    currentUser.getId(),
                    description,
                    amount);

            if (success) {
                System.out.printf("Successfully deposited %.2f €\n", amount);
                System.out.printf("New balance: %.2f €\n", account.getBalance());
            } else {
                System.out.println("Deposit failed!");
            }

        } catch (InputMismatchException e) {
            System.out.println("Invalid amount entered! Please enter numbers only.");
            input.next(); // Clear the invalid input
        } catch (Exception e) {
            System.out.println("Error during deposit: " + e.getMessage());
        }
    }

    static void transferMenu() {
        // Get the sender's account
        BankAccount senderAccount = system.getAccountManager().findAccountByIBAN(currentBankAccountIBAN);

        // Verify sender account exists
        if (senderAccount == null) {
            System.out.println("Error: Your account not found!");
            return;
        }

        try {
            // Get transfer amount
            System.out.println("Enter amount to transfer:");
            double amount = input.nextDouble();

            // Validate amount
            if (amount <= 0) {
                System.out.println("Amount must be positive!");
                return;
            }

            // Check sufficient balance (including potential fees)
            if (amount > senderAccount.getBalance()) {
                System.out.println("Insufficient balance!");
                return;
            }

            // Get recipient IBAN
            System.out.println("Enter recipient IBAN:");
            String receiverIBAN = input.next();

            // Validate IBAN format (basic check)
            if (receiverIBAN == null || receiverIBAN.length() != 20) {
                System.out.println("Invalid IBAN format!");
                return;
            }

            // Prevent self-transfer
            if (receiverIBAN.equals(currentBankAccountIBAN)) {
                System.out.println("Cannot transfer to yourself!");
                return;
            }

            // Get transfer description
            System.out.println("Enter transfer description:");
            String description = input.next();

            // Execute transfer
            boolean success = system.getTransactionManager().transfer(
                    currentBankAccountIBAN,
                    currentUser.getId(),
                    description,
                    amount,
                    receiverIBAN);

            // Show result
            if (success) {
                System.out.printf("Successfully transferred %.2f € to %s\n", amount, receiverIBAN);
                System.out.printf("New balance: %.2f €\n", senderAccount.getBalance());
            } else {
                System.out.println("Transfer failed! Recipient account may not exist.");
            }

        } catch (InputMismatchException e) {
            System.out.println("Invalid input! Please enter numbers for amount.");
            input.next(); // Clear invalid input
        } catch (Exception e) {
            System.out.println("Error during transfer: " + e.getMessage());
        }
    }

    static void showBankAccountTransactionMenu() {
        int choice;
        System.out.printf("\n--Transactions\n1.Withdraw\n2.Deposit\n3.Transfer\n4.Pay Bill");
        choice = input.nextInt();
        while (true) {
            if (choice < 1 || choice > 4) {
                System.out.println("Choice invalid!");
                continue;
            }
            if (choice == 1) {
                System.out.println("--Withdraw");
                withdrawMenu();
            }
            if (choice == 2) {
                System.out.println("--Deposit");
                depositMenu();
            }
            if (choice == 3) {
                System.out.println("--Transfer");
                transferMenu();
            }
            if (choice == 4) {
                System.out.println("--Pay bill");
            }
            break;
        }
    }

    static void ShowStandingOrderMenu() {
        System.out.printf("\n--Standing Orders\n1.Create New Standing Order\n2.Show Standing Orders");
        int choice;
        choice = input.nextInt();
        while (true) {
            if (choice < 1 || choice > 2) {
                System.out.println("Choice invalid!");
                break;
            } else if (choice == 1) {
                System.out.println("--Create new Standing Order");
            } else if (choice == 2) {
                System.out.println("--Show Standing Orders");
            }
        }
    }

    static void billMenu() {
        System.out.printf("\n--Bills\n1.Load Issued Bills\n2.Show Paid Bills");
        int choice;
        choice = input.nextInt();
        while (true) {
            if (choice < 1 || choice > 2) {
                System.out.println("Choice invalid!");
                continue;
            }
            if (choice == 1) {
                System.out.println("--Load Issued Bills");
            }
            if (choice == 2) {
                System.out.println("--Show Paid Bills");
            }
            break;
        }
    }

    static void showCustomersMenu() {
        System.out.printf("--Customers\n1.Show Customers\n2.Show Customer Details");
        int choice = input.nextInt();
        while (true) {
            if (choice < 1 || choice > 2) {
                System.out.println("Choice invalid!");
                continue;
            }
            if (choice == 1) {
                System.out.println("--Show Customers");
            }
            if (choice == 2) {
                System.out.println("--Show Customer Details");
            }
            break;
        }
    }

    static void showBankAccountMenu() {
        System.out.printf(
                "\n--Bank Accounts\n1.Show Bank Accounts\n2.Show Bank Account Info\n3.Show Bank Account Statements");
        int choice = input.nextInt();
        while (true) {
            if (choice < 1 || choice > 3) {
                System.out.println("Choice invalid!");
                continue;
            }
            if (choice == 1) {
                System.out.println("--Show Bank Accounts");
            }
            if (choice == 2) {
                System.out.println("--Show Bank Account Info");
            }
            if (choice == 3) {
                System.out.println("--Show Bank account Statements");
            }
            break;
        }
    }

    static void companyBillMenu() {
        System.out.printf("--Company Bills\n1.Show Issued Bills\n2.Show Paid Bills\n3.Load Company Bills");
        int choice = input.nextInt();
        while (true) {
            if (choice < 1 || choice > 3) {
                System.out.println("Choice invalid!");
                continue;
            }
            if (choice == 1) {
                System.out.println("--Show Issued Bills");
            }
            if (choice == 2) {
                System.out.println("--Show Paid Bills");
            }
            if (choice == 3) {
                System.out.println("--Load Company Bills");
            }
            break;
        }
    }

    static void showPersonalAccountOverview() {
        BankAccount account = system.getAccountManager().findAccountByIBAN(currentBankAccountIBAN);
        User owner = system.getUserManager().findUserById(account.getOwnerId());
        String ownerName;
        if (owner != null) {
            ownerName = owner.getLegalName();
        } else {
            ownerName = "Unknown";
        }

        // Display the account information
        System.out.printf("\n--- Account Overview ---");
        System.out.printf("\nIBAN: %s", currentBankAccountIBAN);
        System.out.printf("\nBalance: %.2f", account.getBalance());
        System.out.printf("\nAccount Holder: %s", ownerName);
        if (account instanceof PersonalAccount) {
            PersonalAccount personalAccount = (PersonalAccount) account;
            if (!personalAccount.getSecondaryOwnerIds().isEmpty()) {
                System.out.printf("\nSecondary Owners:");
                List<Integer> secondaryIds = personalAccount.getSecondaryOwnerIds();
                for (int i = 0; i < secondaryIds.size(); i++) {
                    int ownerId = secondaryIds.get(i);
                    User secondaryOwner = system.getUserManager().findUserById(ownerId);
                    if (secondaryOwner != null) {
                        System.out.printf("\n- %s", secondaryOwner.getLegalName());
                    }
                }
            }
        }
    }

    static void showCompanyAccountOverview() {
        BankAccount account = system.getAccountManager().findAccountByIBAN(currentBankAccountIBAN);
        if (!(account instanceof BusinessAccount)) {
            System.out.println("Error: This is not a business account!");
            return;
        }
        BusinessAccount businessAccount = (BusinessAccount) account;
        User company = system.getUserManager().findUserById(businessAccount.getOwnerId());
        String companyName;
        if (company != null) {
            companyName = company.getLegalName();
        } else {
            companyName = "Unknown Company";
        }
        String vat;
        if (company != null) {
            vat = ((Company) company).getVAT();
        } else {
            vat = "Unknown VAT";
        }
        System.out.printf("\n--- Company Account Overview ---");
        System.out.printf("\nIBAN: %s", currentBankAccountIBAN);
        System.out.printf("\nBalance: %.2f €", businessAccount.getBalance());
        System.out.printf("\nCompany: %s", companyName);
        System.out.printf("\nVAT: %s", vat);
        System.out.printf("\nMonthly Maintenance Fee: %.2f €", businessAccount.getMaintenanceFee());
        System.out.printf("\nInterest Rate: %.2f%%", businessAccount.getInterestRate() * 100);
    }

    



    static void adminMainLoop() {
        System.out.printf(
                "\n 1.Customers\n2.Bank Accounts\n3.Company Bills\n4.Standing Orders\n5.PayCustomer's bill\n6.Simulate Time Passing\n7.Logout");
        int choice = input.nextInt();
        while (true) {
            if (choice < 1 || choice > 7) {
                System.out.println("Choice invalid!");
                continue;
            } else if (choice == 1) {
                showCustomersMenu();
            } else if (choice == 2) {
                showBankAccountMenu();
            } else if (choice == 3) {
                companyBillMenu();
            } else if (choice == 4) {
                System.out.println("--Standing Orders");
            } else if (choice == 5) {
                System.out.println("--Pay Customer's Bill");
            } else if (choice == 6) {
                System.out.println("--Simulate Time Passing");
            }
            if (choice == 7) {
                System.out.println("--Loging Out...");
            }
            break;
        }
    }

    static void mainloop_old() {

    }
}