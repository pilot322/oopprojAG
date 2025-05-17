package interfaces;

public interface StoreManager {
    public void load(Storable s, String filePath);
    public void save(Storable s, String filePath, boolean append);
}
