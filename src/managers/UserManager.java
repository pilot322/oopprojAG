package managers;

import java.util.HashMap;
import java.util.Map;

import models.users.Admin;
import models.users.Company;
import models.users.Individual;
import models.users.User;
import system.BankSystem;

public class UserManager extends Manager {
    public UserManager(BankSystem systemRef) {
        super(systemRef);
    }

    private final Map<Integer, User> usersMap = new HashMap<>();

    private int nextId = 0; // ayto aplws krataei to posa users esxoyn dhmioyrghthei

    public User login(String username, String password) {
        for (User user : usersMap.values()) {
            if (user.getUserName().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    // check for already used username
    private boolean isUsernameTaken(String username) {

        for (User user : usersMap.values()) {
            if (user.getUserName().equals(username)) {
                return true;
            }
        }

        return false;

    }

    private String generateUserId() {
        throw new RuntimeException("Mynhma skasimatos");
    }

    public void register(String type, String username, String password, String legalName, String vat) {
        // throw new RuntimeException("Mynhma skasimatos");

        if (isUsernameTaken(username)) {
            throw new RuntimeException("Username is taken");
        }

        switch (type) {
            case "Admin":
                Admin newUser = new Admin("", legalName, username, password);
                usersMap.put(0, newUser);
                break;
            case "Individual":
                Individual individual = new Individual("", legalName, username, password, vat);
                usersMap.put(0, individual);
                break;

            case "Company":
                Company company = new Company("", legalName, username, password, vat);
                usersMap.put(0, company);
                break;
            default:
                throw new RuntimeException("Account type not supported.");
        }

    }

    private boolean isDigit(String vat) {
        try {
            int valid = Integer.parseInt(vat);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // TODO
    public User findUserById(String userId) {
        throw new RuntimeException("TODO");
    }

    // TODO
    public String getUserType(String userId) {
        throw new RuntimeException("TODO");
    }
}
