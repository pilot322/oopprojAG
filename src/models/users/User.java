package models.users;

import interfaces.Storable;

public abstract class User implements Storable{
    private int id;
    private String userName;
    private String password;
    private String legalName;

    public User(int id, String legalName, String userName, String password) {
        this.id = id;
        this.legalName = legalName;
        this.userName = userName;
        this.password = password;

    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getLegalName() {
        return legalName;
    }

}
