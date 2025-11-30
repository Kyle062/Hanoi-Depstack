package Model;
import java.awt.Color;

public class Debt {
    private String name;
    private double currentBalance;
    private double interestRate;
    private double minimumPayment;

    public Debt(String name, double totalAmount, double interestRate, double minimumPayment) {
        this.name = name;
        this.currentBalance = totalAmount;
        this.interestRate = interestRate;
        this.minimumPayment = minimumPayment;
    }

    public void makePayment(double amount) {
        this.currentBalance = Math.max(0, this.currentBalance - amount);
    }

    public boolean isPaidOff() { return this.currentBalance <= 0.01; }
    public String getName() { return name; }
    public double getCurrentBalance() { return currentBalance; }
    public double getInterestRate() { return interestRate; }

    @Override
    public String toString() { return String.format("%s: $%.2f", name, currentBalance); }
}