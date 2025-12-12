package Model;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class AppController {
    private Map<String, User> users;
    private DebtManager debtManager = new DebtManager();
    private User currentUser;
    private String currentUsername;

    public AppController() {
        // Load existing users from file
        users = DataManager.loadUsers();

        // Pre-register a test financial advisor if not exists
        if (!users.containsKey("advisor1")) {
            users.put("advisor1", new User("John Financial", "john@hanoi.com", "advisor1", "password123", "ADVISOR"));
            DataManager.saveUsers(users);
            System.out.println("Test financial advisor created.");
        }

        // Pre-register another financial advisor
        if (!users.containsKey("advisor2")) {
            users.put("advisor2",
                    new User("Sarah Consultant", "sarah@hanoi.com", "advisor2", "password123", "ADVISOR"));
            DataManager.saveUsers(users);
            System.out.println("Second financial advisor created.");
        }

        // Pre-register a test client if not exists
        if (!users.containsKey("client")) {
            users.put("client", new User("Test Client", "client@test.com", "client", "password123", "DEBTOR"));
            DataManager.saveUsers(users);
            System.out.println("Test client created.");
        }

        System.out.println("Loaded " + users.size() + " users from storage.");
    }

    public boolean login(String username, String password) {
        if (users.containsKey(username) && users.get(username).getPassword().equals(password)) {
            currentUser = users.get(username);
            currentUsername = username;

            // Load user's debts from storage
            loadUserDebts(username);
            System.out.println("User '" + username + "' logged in successfully as " + currentUser.getUserType());
            return true;
        }
        System.out.println("Login failed for user: " + username);
        return false;
    }

    public boolean register(String full, String email, String username, String pass, String userType) {
        if (users.containsKey(username)) {
            System.out.println("Registration failed: Username '" + username + "' already exists.");
            return false;
        }

        users.put(username, new User(full, email, username, pass, userType));
        currentUser = users.get(username);
        currentUsername = username;

        // Save users to file
        DataManager.saveUsers(users);

        // Create new debt manager for new user
        debtManager = new DebtManager();

        System.out.println("User '" + username + "' registered successfully as " + userType);
        return true;
    }

    // Overloaded method for backward compatibility
    public boolean register(String full, String email, String username, String pass) {
        return register(full, email, username, pass, "DEBTOR");
    }

    // Logout method
    public void logout() {
        if (currentUsername != null) {
            // Save user's debts before logging out
            saveUserDebts();
            System.out.println("User '" + currentUsername + "' logged out.");
        }
        currentUser = null;
        currentUsername = null;
        debtManager = new DebtManager(); // Reset debt manager for next user
    }

    // Save current user's debts
    public void saveUserDebts() {
        if (currentUsername != null) {
            List<Debt> currentDebts = debtManager.getStackForVisualization();
            List<Debt> paidOffDebts = debtManager.getPaidOffForVisualization();
            DataManager.saveUserDebts(currentUsername, currentDebts, paidOffDebts);
            System.out.println("Debts saved for user: " + currentUsername);
        }
    }

    // Load user's debts
    private void loadUserDebts(String username) {
        DataManager.DebtData debtData = DataManager.loadUserDebts(username);

        // Clear current debts and load saved ones
        debtManager = new DebtManager();

        // Load current debts
        for (Debt debt : debtData.getCurrentDebts()) {
            debtManager.pushDebt(debt);
        }

        // Load paid off debts
        for (Debt debt : debtData.getPaidOffDebts()) {
            debtManager.moveToPaidOff(debt);
        }

        System.out.println("Loaded " + debtData.getCurrentDebts().size() +
                " current debts and " + debtData.getPaidOffDebts().size() +
                " paid off debts for user: " + username);
    }

    public DebtManager getManager() {
        return debtManager;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUserType() {
        return currentUser != null ? currentUser.getUserType() : null;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    // Check if current user is advisor
    public boolean isAdvisor() {
        return currentUser != null && "ADVISOR".equals(currentUser.getUserType());
    }

    // Check if current user is debtor
    public boolean isDebtor() {
        return currentUser != null && "DEBTOR".equals(currentUser.getUserType());
    }

    // Method to manually save data (can be called periodically)
    public void saveAllData() {
        DataManager.saveUsers(users);
        if (currentUsername != null) {
            saveUserDebts();
        }
    }
}