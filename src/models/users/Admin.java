package models.users;

public class Admin extends User {

    public Admin(int id, String legalName, String userName, String password) {
        super(id, legalName, userName, password);
    }

    public String marshal(){
        return null;
    }
    public void unmarshal(String data){

    } 

}