package managers;

import system.BankSystem;

import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import interfaces.*;

public abstract class Manager implements StoreManager{
    BankSystem systemRef;
    
    Manager(BankSystem systemRef){
        this.systemRef = systemRef;
    }
    public void load(Storable s, String filePath){}
    public void save(Storable s, String filePath, boolean append){}

    public void writeListToFile(String filePath, List<Storable> list){
        List<String> lines = new ArrayList<>();
        for(Storable s:list){
            lines.add(s.marshal());
        }
        
        try{
            Path p = Path.of(filePath);
            Files.write(p, lines);
        } catch(Exception e){
            System.out.println("Unsuccessful write to " + filePath);
            e.printStackTrace();
        }

    }
}
