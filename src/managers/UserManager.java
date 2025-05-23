package managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.users.Admin;
import models.users.Company;
import models.users.Individual;
import models.users.User;
import interfaces.Storable;
import system.BankSystem;

public class UserManager extends Manager {
    public UserManager(BankSystem systemRef) {
        super(systemRef);
    }

    private final Map<Integer, User> usersMap = new HashMap<>();

    private int nextId = 0; // ayto aplws krataei to posa users esxoyn dhmioyrghthei

    public User login(String username, String password) {
        for (User user : usersMap.values()) {
            if (user.getUserName().equalsIgnoreCase(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    // check for already used username
    private boolean isUsernameTaken(String username) {

        for (User user : usersMap.values()) {
            if (user.getUserName().equalsIgnoreCase(username)) {
                return true;
            }
        }

        return false;

    }

    private int generateUserId() {
        // return 0;
        // throw new RuntimeException("Mynhma skasimatos");
        return nextId++;
    }

    public void register(String type, String username, String password, String legalName, String vat) {
        // throw new RuntimeException("Mynhma skasimatos");

        if (isUsernameTaken(username)) {
            throw new RuntimeException("Username is taken");
        }
        if (type.equals("Admin") && vat != null) {
            throw new IllegalArgumentException("Admins should not have a VAT.");
        }
        if ((type.equals("Individual") || type.equals("Company"))) {
            if (vat == null || vat.length() != 9 || !isDigit(vat)) {
                throw new IllegalArgumentException("VAT must be exactly 9 digits.");
            }
        }

        switch (type) {
            case "Admin":
                Admin newUser = new Admin(generateUserId(), legalName, username, password);
                usersMap.put(newUser.getId(), newUser);
                break;
            case "Individual":
                Individual individual = new Individual(generateUserId(), legalName, username, password, vat);
                usersMap.put(individual.getId(), individual);
                break;

            case "Company":
                Company company = new Company(generateUserId(), legalName, username, password, vat);
                usersMap.put(company.getId(), company);
                break;
            default:
                throw new RuntimeException("Account type not supported.");
        }

    }

    private boolean isDigit(String vat) {
        try {
            Integer.parseInt(vat);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // epistrefei null an den yparxei to user
    public User findUserById(int userId) {
        User user = usersMap.get(userId);
        return user;
    }

    // epistrefei "admin", "individual" h "company"
    public String getUserType(int userId) {
        User user = findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException();
        }
        if (user instanceof Admin) {
            return "Admin";
        } else if (user instanceof Individual) {
            return "Individual";
        } else {
            return "Company";
        }
    }

    public void saveAll(){
        List<Storable> usersList = new ArrayList<>(usersMap.values());

        writeListToFile("data/users/users.csv", usersList);
    }
}
