package frontend;

import java.util.*;

import models.accounts.BankAccount;
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
        while (true) {
            System.out.println("PRESS 1 FOR LOGIN");
            System.out.println("PRESS 2 TO REGISTER");
            System.out.println("PRESS 3 TO EXIT");
            choice = input.nextInt();
            if (choice == 1) {
                attemptToLogin();

            } else if (choice == 2) {
                attemptToRegister();
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
                    Arrays.asList("Individual", "Business", "Admin"));

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

    // static String getChoiceWithDefensive(String prompt, List<String> choices) {
    // System.out.print(prompt + "\n> ");

    // choices.add("exit");

    // while (true) {
    // for (int i = 0; i < choices.size(); i++) {
    // System.out.printf("%d: %s\n", i, choices.get(i));
    // }
    // int temp = input.nextInt();

    // if (temp < choices.size() && temp >= 0) {
    // return choices.get(temp);
    // }
    // System.out.println("Value must be one of the following: ");

    // System.out.print("> ");
    // }
    // }

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
            individualMainLoop();
        } else if (currentUser instanceof Company) {
            companyMainLoop();
        } else {
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
            ArrayList<PersonalAccount> accounts = system.getAccountManager()
                    .findAccountsByIndividualId(currentUser.getId());

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
                // prepei me kapoion tropo na parw to iban toy kainoyrioy account
                continue;
            }

            BankAccount b = accounts.get(choice - 1);
            currentBankAccountIBAN = b.getIBAN();

            break;
        }

        // 0: new personal
    }

    static void withdrawMenu(){
        System.out.println("Enter amount to withdraw");
        double amount = input.nextDouble();

        system.getTransactionManager().withdraw(currentBankAccountIBAN, currentUser.getId(), "withdraw", amount);
    }

    static void showBankAccountTransactionMenu() {
        int choice;
        System.out.printf("\n--Transactions\n1.Withdraw\n2.Deposit\n3.Transfer\n4.Pay Bill");
        choice = input.nextInt();
        if (choice < 1 || choice > 4) {
            System.out.println("Choice invalid!");
            return;
        }
        if (choice == 1) {
            System.out.println("--Withdraw");
            withdrawMenu();
        }
        if (choice == 2) {
            System.out.println("--Deposit");
        }
        if (choice == 3) {
            System.out.println("--Transfer");
        }
        if (choice == 4) {
            System.out.println("--Pay bill");
        }
    }

    static void individualMainLoop() {
        System.out.printf("You picked the account with the IBAN:%s", currentBankAccountIBAN);

        while (true) {
            System.out.printf("\n 1.Overview\n2.Transactions\n3.Standing Orders\n4.logout");
            int choice = input.nextInt();

            if (choice < 1 || choice > 4) {
                System.out.println("Choise invalid!");
                continue;
            }
            if (choice == 1) {
                // TODO
                // showPersonalAccountOverview();
            } else if (choice == 2) {
                // TODO
                showBankAccountTransactionMenu();
            }
        }
        // if (choice < 1 || choice > 4) {
        // System.out.println("Choise invalid!");
        // break;
        // }
        // if (choice == 1) {
        // // TODO:
        // // toString();
        // }
        // if (choice == 2) {
        // System.out.printf("\n--Transactions\n1.Withdraw\n2.Deposit\n3.Transfer\n4.Pay
        // Bill");
        // choice = input.nextInt();
        // if (choice < 1 || choice > 4) {
        // System.out.println("Choice invalid!");
        // break;
        // }
        // if (choice == 1) {
        // System.out.println("--Withdraw");
        // }
        // if (choice == 2) {
        // System.out.println("--Deposit");
        // }
        // if (choice == 3) {
        // System.out.println("--Transfer");
        // }
        // if (choice == 4) {
        // System.out.println("--Pay bill");
        // }
        // }
        // if (choice == 3) {
        // System.out.printf("\n--Standing Orders\n1.___\n2.___");
        // choice = input.nextInt();
        // if (choice < 1 || choice > 2) {
        // System.out.println("Choise invalid!");
        // break;
        // }
        // if (choice == 1) {
        // System.out.println("--Create new Standing Order");
        // }
        // if (choice == 2) {
        // System.out.println("--Show Standing Orders");
        // }
        // }
        // if (choice == 4) {
        // System.out.println("--Loging Out...");
        // return;
        // }

    }

    static void companyMainLoop() {
    }

    static void adminMainLoop() {
    }

    static void mainloop_old() {
        // if (currentUser instanceof Individual) {
        // }
        // if (currentUser instanceof Company) {
        // System.out.println("Pick an account or create one. ");
        // System.out.println("0. New Business account");
        // ArrayList<PersonalAccount> accounts = system.getAccountManager()
        // .findAccountsByIndividualId(currentUser.getId());
        // for (int i = 0; i < accounts.size(); i++) {
        // System.out.printf("Account no%d: %s", i + 1, accounts.get(i).getIBAN());
        // }
        // int choice = input.nextInt();
        // {
        // System.out.println("Choice invalid!");
        // break;
        // }
        // if (choice == 0) {
        // // attemptToCreateNewBankAccount();
        // } else {
        // System.out.printf("You picked the account with the IBAN:%s",
        // accounts.get(choice - 1).getIBAN());
        // currentBankAccountIBAN = accounts.get(choice - 1).getIBAN();
        // System.out.printf("\n 1.Overview\n2.Bills\n3.Logout");
        // choice = input.nextInt();
        // if (choice < 1 || choice > 3) {
        // System.out.println("Choice invalid!");
        // break;
        // }
        // }
        // if (choice == 1) {
        // System.out.println("--Overview");
        // // toString();
        // }
        // if (choice == 2) {
        // System.out.printf("\n--Bills\n1.Load Issued Bills\n2.Show Paid Bills");
        // choice = input.nextInt();
        // if (choice < 1 || choice > 2) {
        // System.out.println("Choice invalid!");
        // break;
        // }
        // if (choice == 1) {
        // System.out.println("--Load Issued Bills");
        // }
        // if (choice == 2) {
        // System.out.println("--Show Paid Bills");
        // }

        // }
        // if (choice == 3) {
        // System.out.println("--Loging Out...");
        // // CLI();
        // }
        // }
        // if (currentUser instanceof Admin) {
        // System.out.println("Pick an account or create one. ");
        // System.out.println("0. New Admin account");
        // ArrayList<PersonalAccount> accounts = system.getAccountManager()
        // .findAccountsByIndividualId(currentUser.getId());
        // for (int i = 0; i < accounts.size(); i++) {
        // System.out.printf("Account no%d: %s", i + 1, accounts.get(i).getIBAN());
        // }
        // int choice = input.nextInt();
        // if (choice < 0 || choice > 3) {
        // System.out.println("Choise invalid!");
        // break;
        // }
        // if (choice == 0) {
        // // attemptToCreateNewBankAccount();
        // } else {
        // System.out.printf("You picked the account with the IBAN:%s",
        // accounts.get(choice - 1).getIBAN());
        // currentBankAccountIBAN = accounts.get(choice - 1).getIBAN();
        // System.out.printf(
        // "\n 1.Customers\n2.Bank Accounts\n3.Company Bills\n4.Standing Orders\n5.Pay
        // Customer's bill\n6.Simulate Time Passing\n7.Logout");
        // choice = input.nextInt();
        // }
        // if (choice < 1 || choice > 7) {
        // System.out.println("Choice invalid!");
        // break;
        // }
        // if (choice == 1) {
        // if (choice < 1 || choice > 2) {
        // System.out.println("Choice invalid!");
        // break;
        // }
        // System.out.printf("--Customers\n1.Show Customers\n2.Show Customer Details");
        // if (choice == 1) {
        // System.out.println("--Show Customers");
        // }
        // if (choice == 2) {
        // System.out.println("--Show Customer Details");
        // }
        // }
        // if (choice == 2) {
        // System.out.printf(
        // "\n--Bank Accounts\n1.Show Bank Accounts\n2.Show Bank Account Info\n3.Show
        // Bank Account Statements");
        // choice = input.nextInt();
        // if (choice < 1 || choice > 3) {
        // System.out.println("Choice invalid!");
        // break;
        // }
        // if (choice == 1) {
        // System.out.println("--Show Bank Accounts");
        // }
        // if (choice == 2) {
        // System.out.println("--Show Bank Account Info");
        // }
        // if (choice == 3) {
        // System.out.println("--Show Bank account Statements");
        // }
        // }
        // if (choice == 3) {
        // System.out.printf("--Company Bills\n1.Show Issued Bills\n2.Show Paid
        // Bills\n3.Load Company Bills");
        // choice = input.nextInt();
        // if (choice < 1 || choice > 3) {
        // System.out.println("Choice invalid!");
        // break;
        // }
        // if (choice == 1) {
        // System.out.println("--Show Issued Bills");
        // }
        // if (choice == 2) {
        // System.out.println("--Show Paid Bills");
        // }
        // if (choice == 3) {
        // System.out.println("--Load Company Bills");
        // }
        // }
        // if (choice == 4) {
        // System.out.println("--Standing Orders");
        // }
        // if (choice == 5) {
        // System.out.println("--Pay Customer's Bill");
        // }
        // if (choice == 6) {
        // System.out.println("--Simulate Time Passing");
        // }
        // if (choice == 7) {
        // System.out.println("--Loging Out...");
        // }
        // }
        // while (true) {
        // System.out.println("");
        // int choice = input.nextInt();
        // }
    }
}