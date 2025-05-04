package models.users;

public abstract class User {
    private String id;
    private String userName;
    private String password;
    private String legalName;

    public User(String id, String legalName, String userName, String password) {
        this.id = id;
        this.legalName = legalName;
        this.userName = userName;
        this.password = password;

    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

}
