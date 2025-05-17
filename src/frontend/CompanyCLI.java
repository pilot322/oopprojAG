package frontend;

public class CompanyCLI {
    static void companyMainLoop() {
        System.out.printf("You picked the account with the IBAN:%s", CLI.currentBankAccountIBAN);
        System.out.printf("\n 0.Overview\n1.Bills\n2.Logout");
        while (true) {
            int choice = CLI.input.nextInt();
            if (choice < 0 || choice > 3) {
                System.out.println("Choice invalid!");
                continue;
            } else if (choice == 0) {
                // showCompanyAccountOverview();
            } else if (choice == 1) {
                // billMenu();
            } else if (choice == 2) {
                System.out.println("Logging Out...");
                break;
            }
        }
    }
}
