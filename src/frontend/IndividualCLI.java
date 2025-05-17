package frontend;

public class IndividualCLI {
    static void individualMainLoop() {
        System.out.printf("You picked the account with the IBAN:%s", CLI.currentBankAccountIBAN);

        while (true) {
            System.out.printf("\n 1.Overview\n2.Transactions\n3.Standing Orders\n4.logout");
            int choice = CLI.input.nextInt();

            if (choice < 1 || choice > 4) {
                System.out.println("Choice invalid!");
                continue;
            }
            if (choice == 1) {
                CLI.showPersonalAccountOverview();
            } else if (choice == 2) {
                CLI.showBankAccountTransactionMenu();
            } else if (choice == 3) {
                // ShowStandingOrderMenu();
            } else if (choice == 4) {
                System.out.println("Logging Out...");
                break;
            }
        }

    }
}
