package Model;

import java.io.Serializable;
import java.util.*;

public class DebtManager implements Serializable {

    // ===========================
    // Constructor and Initialization Methods - Methods for initializing the
    // application and setting up default data
    // ===========================
    // AppController() - Constructor that initializes the controller, loads users
    // from storage, and creates test users if they don't exist

    // ===========================
    // Authentication Methods - Methods for user login, registration, and logout
    // operations
    // ===========================
    // login(String username, String password) - Authenticates user, loads debts if
    // successful
    // register(String full, String email, String username, String pass, String
    // userType) - Registers new user with specified type
    // register(String full, String email, String username, String pass) - Registers
    // new user as debtor (default)
    // logout() - Logs out user, saves debts, resets debt manager

    // ===========================
    // User Management Methods - Methods for accessing and checking current user
    // information
    // ===========================
    // getCurrentUser() - Returns currently logged-in user object
    // getCurrentUserType() - Returns user type (ADVISOR/DEBTOR) of current user
    // getCurrentUsername() - Returns username of currently logged-in user
    // isAdvisor() - Checks if current user is financial advisor
    // isDebtor() - Checks if current user is debtor

    // ===========================
    // Debt Management Methods - Methods for managing and persisting user debt data
    // ===========================
    // getManager() - Returns DebtManager instance for debt operations
    // saveUserDebts() - Saves current user's debts to storage
    // loadUserDebts(String username) - Loads user's debts from storage (private)
    // saveAllData() - Saves all application data including users and debts

    // ===========================
    // Debt Stack Operations - Methods for managing the debt stack (LIFO structure)
    // ===========================
    // pushDebt(Debt debt) - Adds a new debt to the top of the stack, applies
    // strategy if not LIFO
    // popDebt() - Removes and returns the top debt from the stack
    // moveToPaidOff(Debt d) - Transfers a debt from active stack to paid-off
    // history list
    // peekTOS() - Returns the top debt without removing it from stack

    // ===========================
    // Data Retrieval Methods - Methods for getting debt data for display
    // ===========================
    // getStackForVisualization() - Returns all active debts as ArrayList for
    // visualization
    // getPaidOffForVisualization() - Returns all paid-off debts as ArrayList for
    // display
    // getMaxDebtAmount() - Returns the highest debt amount for scaling
    // visualizations

    // ===========================
    // Strategy Management - Methods for controlling debt repayment strategies
    // ===========================
    // setStrategy(Strategy strategy) - Changes the current debt repayment strategy
    // getStrategy() - Returns the currently active debt repayment strategy
    // applyStrategy() - Private method that sorts debts based on active strategy
    // (except LIFO)
    // reorderForLIFO() - Ensures stack maintains LIFO order for LIFO strategy

    // ===========================
    // Debug and Utility Methods - Helper methods for testing and debugging
    // ===========================
    // getStackOrderDebug() - Returns formatted string showing current stack order
    // for debugging

    private static final long serialVersionUID = 1L;
    private Stack<Debt> debtStack = new Stack<>();
    // New list to track history for the "Paid Off" pillar
    private ArrayList<Debt> paidOffDebts = new ArrayList<>();

    public enum Strategy {
        AVALANCHE, SNOWBALL, LIFO // ADDED LIFO strategy
    }

    private Strategy currentStrategy = Strategy.LIFO; // CHANGED: Default to LIFO

    public void pushDebt(Debt debt) {
        debtStack.push(debt);
        // CHANGED: Only apply strategy if NOT LIFO
        if (currentStrategy != Strategy.LIFO) {
            applyStrategy();
        }
    }

    public Debt popDebt() {
        if (debtStack.isEmpty())
            return null;
        Debt d = debtStack.pop();
        return d;
    }

    // Method to move a debt to the paid off list
    public void moveToPaidOff(Debt d) {
        paidOffDebts.add(d);
    }

    public Debt peekTOS() {
        return debtStack.isEmpty() ? null : debtStack.peek();
    }

    public ArrayList<Debt> getStackForVisualization() {
        // IMPORTANT: Return the stack as-is for LIFO visualization
        // For LIFO, the last element in the ArrayList is the TOS (most recently added)
        return new ArrayList<>(debtStack);
    }

    public ArrayList<Debt> getPaidOffForVisualization() {
        return new ArrayList<>(paidOffDebts);
    }

    public void setStrategy(Strategy strategy) {
        this.currentStrategy = strategy;
        applyStrategy();
    }

    private void applyStrategy() {
        // Only apply sorting if NOT LIFO strategy
        if (currentStrategy == Strategy.LIFO) {
            // For LIFO, do NOT sort - keep stack in insertion order
            // The Stack class already maintains LIFO order
            return;
        }

        List<Debt> list = new ArrayList<>(debtStack);
        Comparator<Debt> comparator;
        if (currentStrategy == Strategy.SNOWBALL) {
            // Snowball: smallest balance first (lowest to highest)
            comparator = Comparator.comparingDouble(Debt::getCurrentBalance);
        } else { // AVALANCHE
            // Avalanche: highest interest first
            comparator = Comparator.comparingDouble(Debt::getInterestRate).reversed();
        }
        Collections.sort(list, comparator);
        debtStack.clear();

        // IMPORTANT: When adding back to stack, push in reverse order
        // so the first element in the sorted list becomes TOS
        for (int i = list.size() - 1; i >= 0; i--) {
            debtStack.push(list.get(i));
        }
    }

    public double getMaxDebtAmount() {
        double maxStack = debtStack.stream().mapToDouble(Debt::getCurrentBalance).max().orElse(1.0);
        double maxPaid = paidOffDebts.stream().mapToDouble(Debt::getCurrentBalance).max().orElse(1.0);
        return Math.max(maxStack, maxPaid);
    }

    public Strategy getStrategy() {
        return currentStrategy;
    }

    // Add method to manually reorder stack for true LIFO (if needed)
    public void reorderForLIFO() {
        // If strategy is LIFO, ensure the stack reflects insertion order
        // with newest (last pushed) at the top
        if (currentStrategy == Strategy.LIFO) {
            // The Stack class already maintains LIFO order
            // No need to reorder
        }
    }

    // Debug method to check stack order
    public String getStackOrderDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append("Stack Order (Strategy: ").append(currentStrategy).append("):\n");
        if (debtStack.isEmpty()) {
            sb.append("  Empty\n");
        } else {
            // Show from bottom (oldest) to top (newest)
            List<Debt> list = new ArrayList<>(debtStack);
            for (int i = 0; i < list.size(); i++) {
                Debt d = list.get(i);
                String position;
                if (i == list.size() - 1) {
                    position = "TOS (Top/Newest)";
                } else {
                    position = "Position " + (list.size() - i - 1) + " from top";
                }
                sb.append("  ").append(position).append(": ")
                        .append(d.getName()).append(" ($").append(d.getCurrentBalance()).append(")\n");
            }
        }
        return sb.toString();
    }
}