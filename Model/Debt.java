package Model;

import java.io.Serializable;

public class Debt implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private double currentBalance;
    private double originalAmount; // Added field
    private double interestRate;
    private double minimumPayment;

    public Debt(String name, double totalAmount, double interestRate, double minimumPayment) {
        this.name = name;
        this.originalAmount = totalAmount; // Store original amount
        this.currentBalance = totalAmount;
        this.interestRate = interestRate;
        this.minimumPayment = minimumPayment;
    }

    public void makePayment(double amount) {
        this.currentBalance = Math.max(0, this.currentBalance - amount);
    }

    public boolean isPaidOff() {
        return this.currentBalance <= 0.01;
    }

    public String getName() {
        return name;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getMinimumPayment() {
        return minimumPayment;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public double getOriginalBalance() {
        return originalAmount;
    }

    @Override
    public String toString() {
        return String.format("%s: $%.2f (Original: $%.2f)", name, currentBalance, originalAmount);
    }
}