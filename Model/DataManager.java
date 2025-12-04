package Model;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private static final String USERS_FILE = "users.dat";
    private static final String DEBTS_FILE = "debts_";
    
    // Save users to file
    public static void saveUsers(Map<String, User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
            System.out.println("Users saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Load users from file
    @SuppressWarnings("unchecked")
    public static Map<String, User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            System.out.println("Users file not found. Creating new user database.");
            return new HashMap<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    // Save debts for a specific user
    public static void saveUserDebts(String username, List<Debt> debts, List<Debt> paidOffDebts) {
        String filename = DEBTS_FILE + username + ".dat";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            DebtData debtData = new DebtData(debts, paidOffDebts);
            oos.writeObject(debtData);
            System.out.println("Debts saved for user: " + username);
        } catch (IOException e) {
            System.err.println("Error saving debts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Load debts for a specific user
    public static DebtData loadUserDebts(String username) {
        String filename = DEBTS_FILE + username + ".dat";
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("No saved debts found for user: " + username);
            return new DebtData();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (DebtData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading debts: " + e.getMessage());
            e.printStackTrace();
            return new DebtData();
        }
    }
    
    // Inner class to hold both current and paid off debts
    public static class DebtData implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<Debt> currentDebts;
        private List<Debt> paidOffDebts;
        
        public DebtData() {
            this.currentDebts = new java.util.ArrayList<>();
            this.paidOffDebts = new java.util.ArrayList<>();
        }
        
        public DebtData(List<Debt> currentDebts, List<Debt> paidOffDebts) {
            this.currentDebts = currentDebts;
            this.paidOffDebts = paidOffDebts;
        }
        
        public List<Debt> getCurrentDebts() { return currentDebts; }
        public List<Debt> getPaidOffDebts() { return paidOffDebts; }
    }
}