package org.example;

import org.example.BankAccount;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;


public class ATMGUI extends JFrame {

    private final BankAccount account;
    private final JTextArea displayScreen;

    private int pinAttempts = 0;
    private static final int MAX_PIN_ATTEMPTS = 3;

    // UI Colors and Fonts
    private final Color primaryColor = new Color(45, 52, 54);
    private final Color secondaryColor = new Color(99, 110, 114);
    private final Color accentColor = new Color(0, 206, 209);
    private final Color textColor = Color.WHITE;
    private final Font buttonFont = new Font("Arial", Font.BOLD, 16);
    private final Font displayFont = new Font("Monospaced", Font.BOLD, 16);

    public ATMGUI() {
        account = new BankAccount(1000.00, "1015");


        setTitle("ATM Machine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(false);


        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(primaryColor);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(mainPanel);


        displayScreen = new JTextArea(12, 40);
        displayScreen.setFont(displayFont);
        displayScreen.setEditable(false);
        displayScreen.setBackground(new Color(25, 35, 45));
        displayScreen.setForeground(accentColor);
        displayScreen.setMargin(new Insets(10, 10, 10, 10));
        displayScreen.setText("Welcome to the ATM!\n\nPlease select an option.");
        JScrollPane scrollPane = new JScrollPane(displayScreen);
        scrollPane.setBorder(BorderFactory.createLineBorder(accentColor, 2));
        mainPanel.add(scrollPane, BorderLayout.NORTH);


        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        buttonPanel.setBackground(primaryColor);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);


        JButton checkBalanceButton = createStyledButton("Check Balance");
        JButton depositButton = createStyledButton("Deposit");
        JButton withdrawButton = createStyledButton("Withdraw");
        JButton historyButton = createStyledButton("Transaction History");
        JButton changePinButton = createStyledButton("Change PIN");
        JButton exitButton = createStyledButton("Exit");

        buttonPanel.add(checkBalanceButton);
        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(changePinButton);
        buttonPanel.add(exitButton);


        checkBalanceButton.addActionListener(e -> performActionWithPinCheck(this::checkBalance));
        depositButton.addActionListener(e -> performActionWithPinCheck(this::depositMoney));
        withdrawButton.addActionListener(e -> performActionWithPinCheck(this::withdrawMoney));
        historyButton.addActionListener(e -> performActionWithPinCheck(this::showHistory));
        changePinButton.addActionListener(e -> performActionWithPinCheck(this::changePin));
        exitButton.addActionListener(e -> System.exit(0));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(buttonFont);
        button.setForeground(textColor);
        button.setBackground(secondaryColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 1),
                new EmptyBorder(20, 30, 20, 30)
        ));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(accentColor);
                button.setForeground(primaryColor);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(secondaryColor);
                button.setForeground(textColor);
            }
        });
        return button;
    }

    private void performActionWithPinCheck(Runnable action) {
        if (promptForPin()) {
            action.run();
        }
    }

    private boolean promptForPin() {
        JPasswordField pinField = new JPasswordField(10);
        int option = JOptionPane.showConfirmDialog(
                this, pinField, "Enter PIN",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String enteredPin = new String(pinField.getPassword());
            if (account.validatePin(enteredPin)) {
                displayScreen.setText("PIN accepted.");
                pinAttempts = 0;
                return true;
            } else {
                pinAttempts++;
                if (pinAttempts >= MAX_PIN_ATTEMPTS) {
                    displayScreen.setText("Too many incorrect attempts.\nAccount locked. Exiting.");
                    JOptionPane.showMessageDialog(this, "Account locked.", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    displayScreen.setText("Incorrect PIN. Attempts remaining: " + (MAX_PIN_ATTEMPTS - pinAttempts));
                }
                return false;
            }
        }
        return false;
    }

    private void checkBalance() {
        displayScreen.setText(String.format("Your current balance is: $%.2f", account.getBalance()));
    }

    private void depositMoney() {
        String amountStr = JOptionPane.showInputDialog(this, "Enter amount to deposit:", "Deposit", JOptionPane.PLAIN_MESSAGE);
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                String result = account.deposit(amount);
                displayScreen.setText(result + String.format("\nNew balance: $%.2f", account.getBalance()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void withdrawMoney() {
        String amountStr = JOptionPane.showInputDialog(this, "Enter amount to withdraw:", "Withdraw", JOptionPane.PLAIN_MESSAGE);
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                String result = account.withdraw(amount);
                displayScreen.setText(result + String.format("\nNew balance: $%.2f", account.getBalance()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showHistory() {
        List<String> history = account.getTransactionHistory();
        if (history.isEmpty()) {
            displayScreen.setText("No transaction history found.");
            return;
        }
        StringBuilder historyText = new StringBuilder("--- Transaction History ---\n");
        for (int i = history.size() - 1; i >= 0; i--) {
            historyText.append(history.get(i)).append("\n");
        }
        displayScreen.setText(historyText.toString());
    }

    private void changePin() {
        JPasswordField oldPinField = new JPasswordField(10);
        int oldPinOption = JOptionPane.showConfirmDialog(this, oldPinField, "Enter OLD PIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (oldPinOption == JOptionPane.OK_OPTION) {
            String oldPin = new String(oldPinField.getPassword());
            if (account.validatePin(oldPin)) {
                JPasswordField newPinField = new JPasswordField(10);
                int newPinOption = JOptionPane.showConfirmDialog(this, newPinField, "Enter NEW PIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (newPinOption == JOptionPane.OK_OPTION) {
                    String newPin = new String(newPinField.getPassword());
                    if (newPin.matches("\\d{4,}")) {
                        account.changePin(oldPin, newPin);
                        displayScreen.setText("PIN changed successfully.");
                    } else {
                        displayScreen.setText("Invalid PIN format. Must be at least 4 digits.");
                    }
                }
            } else {
                displayScreen.setText("Incorrect old PIN.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ATMGUI atm = new ATMGUI();
            atm.setVisible(true);
        });
    }
}
