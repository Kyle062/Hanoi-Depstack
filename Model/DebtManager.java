package Model;

import java.io.Serializable;
import java.util.*;

public class DebtManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private Stack<Debt> debtStack = new Stack<>();
    // New list to track history for the "Paid Off" pillar
    private List<Debt> paidOffDebts = new ArrayList<>();

    public enum Strategy {
        AVALANCHE, SNOWBALL
    }

    private Strategy currentStrategy = Strategy.AVALANCHE;

    public void pushDebt(Debt debt) {
        debtStack.push(debt);
        applyStrategy();
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

    public List<Debt> getStackForVisualization() {
        return new ArrayList<>(debtStack);
    }

    public List<Debt> getPaidOffForVisualization() {
        return new ArrayList<>(paidOffDebts);
    }

    public void setStrategy(Strategy strategy) {
        this.currentStrategy = strategy;
        applyStrategy();
    }

    private void applyStrategy() {
        List<Debt> list = new ArrayList<>(debtStack);
        Comparator<Debt> comparator;
        if (currentStrategy == Strategy.SNOWBALL) {
            comparator = Comparator.comparingDouble(Debt::getCurrentBalance).reversed();
        } else {
            comparator = Comparator.comparingDouble(Debt::getInterestRate);
        }
        Collections.sort(list, comparator);
        debtStack.clear();
        debtStack.addAll(list);
    }

    public double getMaxDebtAmount() {
        double maxStack = debtStack.stream().mapToDouble(Debt::getCurrentBalance).max().orElse(1.0);
        double maxPaid = paidOffDebts.stream().mapToDouble(Debt::getCurrentBalance).max().orElse(1.0);
        return Math.max(maxStack, maxPaid);
    }

    public Strategy getStrategy() {
        return currentStrategy; // FIXED: Return the actual strategy
    }
}