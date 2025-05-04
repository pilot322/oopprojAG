package frontend;

import java.util.*;

import models.accounts.PersonalAccount;
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
        System.out.println("PRESS 1 FOR LOGIN");
        System.out.println("PRESS 2 TO REGISTER");
        choice = input.nextInt();
        if (choice == 1) {
            attemptToLogin();

        } else if (choice == 2) {
            attemptToRegister();
            attemptToLogin();
        } else {
            System.out.println("Invalid choice, attempting to log in");
            attemptToLogin();
        }

        mainloop();

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

    static void attemptToCreateNewBankAccount() {
        String countryCode = getStringWithDefensive("Enter country code: ", Arrays.asList("GR", "EN", "AL"));
        // TODO: enter secondary owners by id or username
        // system.getAccountManager().createPersonalAccount(-1, countryCode);
    }

    static void mainloop() {
        if (currentUser instanceof Individual) {
            System.out.println("Pick an account or create one. ");
            System.out.println("0. New personal account");

            // gia kathe account poy exei, tha valw mia epilogh
            ArrayList<PersonalAccount> accounts = system.getAccountManager()
                    .findAccountsByIndividualId(currentUser.getId());

            // dedomena: accounts
            // stoxos: na ektypwsw
            // 1. (IBAN prwtoy account)
            // 2. (...)
            for (int i = 0; i < accounts.size(); i++) {
                System.out.printf("Account no%d: %s", i + 1, accounts.get(i).getIBAN());
            }
            int choice = input.nextInt();
            if (choice == 0) {
                attemptToCreateNewBankAccount();
            }
        }
        while (true) {
            System.out.println("");
            int choice = input.nextInt();
        }
    }

}
