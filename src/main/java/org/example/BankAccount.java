package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BankAccount {
    private double balance;
    private String pin;
    private final List<String> transactionHistory;
    private final String historyFilePath;
    private static final int MAX_HISTORY_ITEMS = 10;


    public BankAccount(double initialBalance, String pin) {
        this.balance = Math.max(0, initialBalance);
        this.pin = pin;
        this.transactionHistory = new ArrayList<>();
        this.historyFilePath = "transaction_history.csv";

        // Check if this is a new account with a starting balance
        File historyFile = new File(historyFilePath);
        if (!historyFile.exists() && initialBalance > 0) {
            addTransaction("INITIAL DEPOSIT", initialBalance);
        }
    }

    public double getBalance() {
        return balance;
    }

    public boolean validatePin(String enteredPin) {
        return this.pin.equals(enteredPin);
    }

    public boolean changePin(String oldPin, String newPin) {
        if (validatePin(oldPin)) {
            this.pin = newPin;
            // Optionally log this event
            logTransactionToFile("PIN CHANGE", 0, this.balance);
            return true;
        }
        return false;
    }

    public String deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            addTransaction("DEPOSIT", amount);
            return "Successfully deposited $" + String.format("%.2f", amount);
        } else {
            return "Deposit amount must be positive.";
        }
    }

    public String withdraw(double amount) {
        if (amount <= 0) {
            return "Withdrawal amount must be positive.";
        } else if (amount > balance) {
            return "Insufficient funds.";
        } else {
            balance -= amount;
            addTransaction("WITHDRAWAL", amount);
            return "Successfully withdrew $" + String.format("%.2f", amount);
        }
    }

    private void addTransaction(String type, double amount) {
        // Add to in-memory list for quick display
        if (transactionHistory.size() >= MAX_HISTORY_ITEMS) {
            transactionHistory.remove(0);
        }
        String formattedTransaction = formatTransaction(type, amount);
        transactionHistory.add(formattedTransaction);

        // Log the transaction to the CSV file
        logTransactionToFile(type, amount, this.balance);
    }

    private String formatTransaction(String type, double amount) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sign = type.contains("DEPOSIT") ? "+" : "-";
        if (type.equals("PIN CHANGE")) sign = " ";
        return String.format("%s | %-17s | %s$%.2f", timestamp, type, sign, amount);
    }

    /**
     * Appends a transaction record to the CSV file.
     * @param type The type of transaction (e.g., DEPOSIT).
     * @param amount The amount involved.
     * @param newBalance The balance after the transaction.
     */
    private void logTransactionToFile(String type, double amount, double newBalance) {
        // 'try-with-resources' ensures the writer is closed automatically.
        try (FileWriter fw = new FileWriter(historyFilePath, true);
             PrintWriter pw = new PrintWriter(fw)) {

            File file = new File(historyFilePath);
            // Write a header if the file is new/empty
            if (file.length() == 0) {
                pw.println("Timestamp,Transaction Type,Amount,Balance After Transaction");
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pw.printf("%s,%s,%.2f,%.2f%n", timestamp, type, amount, newBalance);

        } catch (IOException e) {
            // In a real application, you might show an error to the user.
            // For this example, we'll just print to the console.
            System.out.println("Error writing to transaction log: " + e.getMessage());
        }
    }

    public List<String> getTransactionHistory() {
        // This returns the recent history stored in memory.
        // For a full history, you would read from the CSV file.
        return Collections.unmodifiableList(transactionHistory);
    }
}
