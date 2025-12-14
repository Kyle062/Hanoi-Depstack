package Model;

import java.io.Serializable;
import java.util.*;

public class DebtManager implements Serializable {
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