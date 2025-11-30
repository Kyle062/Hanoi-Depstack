package Model;

import java.util.*;

public class DebtManager {
    private Stack<Debt> debtStack = new Stack<>();

    public enum Strategy {
        AVALANCHE, SNOWBALL
    }

    private Strategy currentStrategy = Strategy.AVALANCHE;

    public void pushDebt(Debt debt) {
        debtStack.push(debt);
        applyStrategy();
    }

    public Debt popDebt() {
        return debtStack.isEmpty() ? null : debtStack.pop();
    }

    public Debt peekTOS() {
        return debtStack.isEmpty() ? null : debtStack.peek();
    }

    public List<Debt> getStackForVisualization() {
        return new ArrayList<>(debtStack);
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
        return debtStack.stream().mapToDouble(Debt::getCurrentBalance).max().orElse(1.0);
    }

    public Strategy getCurrentStrategy() {
        return currentStrategy;
    }
}