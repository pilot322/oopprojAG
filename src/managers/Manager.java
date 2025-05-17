package managers;

import system.BankSystem;
import interfaces.*;

public abstract class Manager implements StoreManager{
    BankSystem systemRef;
    
    Manager(BankSystem systemRef){
        this.systemRef = systemRef;
    }
    public void load(Storable s, String filePath){}
    public void save(Storable s, String filePath, boolean append){}
}
