package Model;

import java.util.HashMap;
import java.util.Map;

public class AppController {
    private Map<String, User> users = new HashMap<>();
    private DebtManager debtManager = new DebtManager();

    public AppController() {
        // Pre-register admin
        users.put("admin", new User("Admin User", "admin@test.com", "admin", "password"));
        // Dummy Data
        debtManager.pushDebt(new Debt("Student Loan", 12400, 6.8, 150));
        debtManager.pushDebt(new Debt("Car Loan", 8000, 5.5, 200));
        debtManager.pushDebt(new Debt("Credit Card", 5000, 15.5, 100));
    }

    public boolean login(String user, String pass) {
        return users.containsKey(user) && users.get(user).getPassword().equals(pass);
    }

    public boolean register(String full, String email, String user, String pass) {
        if (users.containsKey(user))
            return false;
        users.put(user, new User(full, email, user, pass));
        return true;
    }

    public DebtManager getManager() {
        return debtManager;
    }
}
