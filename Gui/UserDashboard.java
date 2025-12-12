package Gui;

import Model.AppController;
import Model.ConsultationRequest;
import Model.DataManager;
import Model.Debt;
import Model.DebtManager;
import Model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserDashboard extends JFrame {

    private AppController controller;
    private DebtManager manager;

    // UI components
    private JLayeredPane layeredPane;
    private JPanel mainLayer;
    private JLabel backgroundLabel;

    // Panels
    private JPanel towerContainer;
    private JPanel cardPanel;
    private JPanel addDebtPanel;
    private JPanel paymentPanel;
    private JPanel calendarPanel;

    // Controls
    private JTextArea logsArea;
    private JScrollPane logsScrollPane;
    private JTextField nameField, amountField, intField, minField, payField, dueDateField;

    // Event calendar text area
    private JTextArea calendarEventsArea;

    // Credit Card Labels
    private JLabel cardTitleLabel;
    private JLabel posLabel;
    private JLabel balValLabel;
    private JLabel intValLabel;
    private JLabel ogAmtLabel;
    private JLabel minPayLabel;
    private JProgressBar progressBar;
    private JLabel tosLabel;

    // Data lists for visualization
    private List<Debt> auxiliaryDebts = new ArrayList<>();
    private List<Debt> paidOffDebts = new ArrayList<>();

    // Date formatter for event calendar
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d");

    public UserDashboard(AppController controller) {
        this.controller = controller;
        this.manager = controller.getManager();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("Hanoi Debt Tower Dashboard - Client");
        setLayout(new BorderLayout());

        initUI();
        refreshAll();
        setVisible(true);

        // Add window listener to save data on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.saveAllData();
            }
        });
    }

    private void initUI() {
        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        setSize(1400, 900);

        setupBackground();

        mainLayer = new JPanel(null);
        mainLayer.setOpaque(false);
        mainLayer.setBounds(0, 0, 1920, 1080);
        layeredPane.add(mainLayer, JLayeredPane.PALETTE_LAYER);

        createSidebar();
        createTopRow();
        createBottomRow();
        createLogsPanel();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                backgroundLabel.setBounds(0, 0, getWidth(), getHeight());
                mainLayer.setBounds(0, 0, getWidth(), getHeight());
            }
        });
    }

    private void setupBackground() {
        try {
            BufferedImage bg = ImageIO.read(new File("Images/DashboardMainBackground.png"));
            Image scaled = bg.getScaledInstance(1920, 1080, Image.SCALE_SMOOTH);
            backgroundLabel = new JLabel(new ImageIcon(scaled));
        } catch (IOException e) {
            backgroundLabel = new JLabel();
            backgroundLabel.setOpaque(true);
            backgroundLabel.setBackground(new Color(240, 240, 245));
        }
        backgroundLabel.setBounds(0, 0, 1920, 1080);
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);
    }

    private void createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(9, 1, 0, 20));
        sidebar.setOpaque(false);
        sidebar.setBounds(20, 150, 100, 650);

        sidebar.add(createIconButton("Consultation \nAppointment", "Consultation \nAppointment",
                e -> showConsultationDialog(mainLayer)));
        sidebar.add(createIconButton("PEEK", "View Top", e -> onPeekClicked()));
        sidebar.add(createIconButton("PAY", "Settle", e -> onSettleClicked()));
        sidebar.add(createIconButton("HISTORY", "History", e -> onHistoryClicked()));
        sidebar.add(createIconButton("DELETE", "Delete", e -> onDeleteClicked()));
        sidebar.add(createIconButton("PROFILE", "Profile", e -> onProfileClicked()));
        sidebar.add(createIconButton("AUXILIARY", "Move TOS to Auxiliary", e -> onAuxiliary()));
        sidebar.add(createIconButton("LOGOUT", "Logout from system", e -> logout()));

        mainLayer.add(sidebar);
    }

    private JButton createIconButton(String text, String tooltip, ActionListener action) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("SansSerif", Font.BOLD, 9));
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.saveAllData();
            controller.logout();
            dispose();

            // Return to login screen
            SwingUtilities.invokeLater(() -> {
                new Login(controller).setVisible(true);
            });
        }
    }

    private void createTopRow() {
        // Tower Panel (Top Center/Left)
        towerContainer = new RoundedPanel(25, Color.WHITE);
        towerContainer.setLayout(null);
        towerContainer.setBounds(150, 30, 1300, 530);

        TowerVisualizationPanel towerVis = new TowerVisualizationPanel();
        towerVis.setBounds(50, 60, 1200, 400);
        towerContainer.add(towerVis);
        mainLayer.add(towerContainer);

        // Add Debt Panel (Top Right)
        addDebtPanel = createAddDebtPanel();
        addDebtPanel.setBounds(1500, 30, 350, 530);
        mainLayer.add(addDebtPanel);
    }

    private void createBottomRow() {
        int startY = 580;
        int height = 280;

        // Credit Card Panel (Bottom Left) - Shows TOS details
        cardPanel = createCardPanel();
        cardPanel.setBounds(150, startY, 700, height);
        mainLayer.add(cardPanel);

        // Make Payment Panel (Bottom Middle)
        paymentPanel = createPaymentPanel();
        paymentPanel.setBounds(870, startY, 500, height);
        mainLayer.add(paymentPanel);

        // Event Calendar (Bottom Right)
        calendarPanel = createEventCalendarPanel();
        calendarPanel.setBounds(1400, startY, 500, height);
        mainLayer.add(calendarPanel);
    }

    private void createLogsPanel() {
        // Logs go below the bottom row
        JPanel logContainer = new JPanel(new BorderLayout());
        logContainer.setOpaque(false);
        logContainer.setBounds(0, 870, 2000, 150);

        logsArea = new JTextArea();
        logsArea.setEditable(false);
        logsArea.setBackground(new Color(30, 30, 40));
        logsArea.setForeground(Color.WHITE);
        logsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JLabel logTitle = new JLabel(" Operation Logs");
        logTitle.setForeground(Color.WHITE);
        logTitle.setOpaque(true);
        logTitle.setBackground(new Color(50, 50, 60));

        logsScrollPane = new JScrollPane(logsArea);
        logsScrollPane.setBorder(null);

        logContainer.add(logTitle, BorderLayout.NORTH);
        logContainer.add(logsScrollPane, BorderLayout.CENTER);

        mainLayer.add(logContainer);
    }

    private JPanel createAddDebtPanel() {
        RoundedPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(null);

        JLabel title = new JLabel("Add New Debt");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBounds(20, 20, 200, 30);
        panel.add(title);

        int y = 70;
        int gap = 65;
        int fieldH = 35;
        int width = 310;

        // Name
        addLabel(panel, "Debt Name", 20, y - 20);
        nameField = addTextField(panel, "Credit Card, Loan...", 20, y, width, fieldH);

        // Amount
        y += gap;
        addLabel(panel, "Total Amount ($)", 20, y - 20);
        amountField = addTextField(panel, "5000.00", 20, y, width, fieldH);

        // Rate
        y += gap;
        addLabel(panel, "Interest Rate (%)", 20, y - 20);
        intField = addTextField(panel, "15.5", 20, y, width, fieldH);

        // Min Payment
        y += gap;
        addLabel(panel, "Minimum Payment ($)", 20, y - 20);
        minField = addTextField(panel, "100.00", 20, y, width, fieldH);

        // Due Date
        y += gap;
        addLabel(panel, "Due Date (e.g., Dec 31)", 20, y - 20);
        dueDateField = addTextField(panel, "Dec 31", 20, y, width, fieldH);

        // Button
        JButton pushBtn = createOrangeButton("PUSH TO STACK");
        pushBtn.setBounds(20, 390, width, 40);
        pushBtn.addActionListener(e -> pushNewDebt());
        panel.add(pushBtn);

        return panel;
    }

    private JPanel createPaymentPanel() {
        RoundedPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(null);

        JLabel title = new JLabel("Make Payment");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBounds(20, 20, 200, 30);
        panel.add(title);

        tosLabel = new JLabel("Current TOS: " + getTopName());
        tosLabel.setForeground(Color.ORANGE.darker());
        tosLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        tosLabel.setBounds(20, 60, 300, 20);
        panel.add(tosLabel);

        addLabel(panel, "Payment Amount ($)", 20, 90);
        payField = addTextField(panel, "0.00", 20, 115, 390, 40);
        payField.setFont(new Font("Arial", Font.BOLD, 18));

        // Quick buttons
        JButton minBtn = new JButton("Min Payment");
        styleQuickButton(minBtn);
        minBtn.setBounds(20, 170, 180, 30);
        minBtn.addActionListener(e -> {
            Debt top = controller.getManager().peekTOS();
            if (top != null)
                payField.setText(String.format("%.2f", top.getMinimumPayment()));
            else
                JOptionPane.showMessageDialog(this, "No active debt to pay!");
        });
        panel.add(minBtn);

        JButton fullBtn = new JButton("Full Balance");
        styleQuickButton(fullBtn);
        fullBtn.setBounds(220, 170, 190, 30);
        fullBtn.addActionListener(e -> {
            Debt top = controller.getManager().peekTOS();
            if (top != null)
                payField.setText(String.format("%.2f", top.getCurrentBalance()));
            else
                JOptionPane.showMessageDialog(this, "No active debt to pay!");
        });
        panel.add(fullBtn);

        // Settle Button
        JButton settleBtn = createOrangeButton("SETTLE PAYMENT");
        settleBtn.setBounds(20, 220, 390, 40);
        settleBtn.addActionListener(e -> makePayment());
        panel.add(settleBtn);

        return panel;
    }

    private JPanel createCardPanel() {
        RoundedPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(null);

        // Title
        cardTitleLabel = new JLabel("Debt Details - TOS");
        cardTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        cardTitleLabel.setBounds(20, 20, 200, 25);
        panel.add(cardTitleLabel);

        // Position label - shows stack position
        posLabel = new JLabel("Position: TOS (Top of Stack)");
        posLabel.setForeground(Color.GRAY);
        posLabel.setBounds(20, 45, 300, 20);
        panel.add(posLabel);

        // Balance Big
        JLabel balLabel = new JLabel("Current Balance");
        balLabel.setForeground(Color.GRAY);
        balLabel.setBounds(20, 90, 150, 20);
        panel.add(balLabel);

        balValLabel = new JLabel("$0.00");
        balValLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        balValLabel.setBounds(20, 115, 200, 35);
        panel.add(balValLabel);

        // Interest
        JLabel intLabel = new JLabel("% Interest Rate");
        intLabel.setForeground(Color.GRAY);
        intLabel.setBounds(250, 90, 150, 20);
        panel.add(intLabel);

        intValLabel = new JLabel("0.0%");
        intValLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        intValLabel.setForeground(new Color(234, 88, 12));
        intValLabel.setBounds(250, 115, 150, 35);
        panel.add(intValLabel);

        // Progress Bar
        progressBar = new JProgressBar();
        progressBar.setValue(0);
        progressBar.setBounds(20, 170, 360, 8);
        progressBar.setForeground(new Color(234, 88, 12));
        progressBar.setBackground(new Color(240, 240, 240));
        progressBar.setBorderPainted(false);
        panel.add(progressBar);

        // Original amount label
        ogAmtLabel = new JLabel("Original Amount: $0.00");
        ogAmtLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        ogAmtLabel.setBounds(20, 190, 200, 20);
        panel.add(ogAmtLabel);

        // Minimum payment label
        minPayLabel = new JLabel("Min Payment: $0.00");
        minPayLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        minPayLabel.setBounds(250, 190, 150, 20);
        panel.add(minPayLabel);

        return panel;
    }

    private JPanel createEventCalendarPanel() {
        RoundedPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(null);

        JLabel title = new JLabel("Event Calendar");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBounds(20, 20, 200, 30);
        panel.add(title);

        calendarEventsArea = new JTextArea();
        calendarEventsArea.setText("");
        calendarEventsArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        calendarEventsArea.setEditable(false);
        calendarEventsArea.setLineWrap(true);
        calendarEventsArea.setWrapStyleWord(true);
        calendarEventsArea.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(calendarEventsArea);
        scrollPane.setBounds(20, 60, 310, 200);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane);

        return panel;
    }

    private void updateCreditCardPanel() {
        try {
            List<Debt> activeDebts = manager.getStackForVisualization();
            if (activeDebts != null && !activeDebts.isEmpty()) {
                Debt topDebt = activeDebts.get(activeDebts.size() - 1); // Get TOS (LIFO - last element)

                cardTitleLabel.setText("Debt Details - TOS");
                posLabel.setText("Position: TOS (Stack Size: " + activeDebts.size() + ")");
                balValLabel.setText(String.format("$%,.2f", topDebt.getCurrentBalance()));

                try {
                    Object rateObj = topDebt.getInterestRate();
                    if (rateObj instanceof Number) {
                        double interestRate = ((Number) rateObj).doubleValue();
                        intValLabel.setText(String.format("%.1f%%", interestRate));
                    } else {
                        intValLabel.setText(rateObj.toString() + "%");
                    }
                } catch (Exception e) {
                    intValLabel.setText("Error");
                }

                ogAmtLabel.setText(String.format("Original Amount: $%,.2f", topDebt.getOriginalAmount()));
                minPayLabel.setText(String.format("Min Payment: $%,.2f", topDebt.getMinimumPayment()));

                double progress = ((topDebt.getOriginalAmount() - topDebt.getCurrentBalance())
                        / topDebt.getOriginalAmount()) * 100;
                progressBar.setValue((int) Math.min(100, Math.max(0, progress)));
            } else {
                cardTitleLabel.setText("No Active Debt");
                posLabel.setText("Position: N/A (Stack Empty)");
                balValLabel.setText("$0.00");
                intValLabel.setText("0.0%");
                ogAmtLabel.setText("Original Amount: $0.00");
                minPayLabel.setText("Min Payment: $0.00");
                progressBar.setValue(0);
            }
        } catch (Exception e) {
            // Ignore errors
        }
    }

    private void addEventToCalendar(String event) {
        Date now = new Date();
        String dateStr = dateFormat.format(now);
        calendarEventsArea.append(dateStr + ": " + event + "\n");
        calendarEventsArea.setCaretPosition(calendarEventsArea.getDocument().getLength());
    }

    private void addLabel(JPanel p, String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(Color.DARK_GRAY);
        l.setBounds(x, y, 200, 20);
        p.add(l);
    }

    private JTextField addTextField(JPanel p, String ph, int x, int y, int w, int h) {
        JTextField tf = new JTextField(ph);
        tf.setBounds(x, y, w, h);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        p.add(tf);
        return tf;
    }

    private JButton createOrangeButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(234, 88, 12));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        return btn;
    }

    private void styleQuickButton(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    // --- Action Methods ---

    private void pushNewDebt() {
        try {
            String name = nameField.getText().trim();
            String amountText = amountField.getText().trim();
            String intText = intField.getText().trim();
            String minText = minField.getText().trim();
            String dueDate = dueDateField.getText().trim();

            if (name.isEmpty() || amountText.isEmpty() || intText.isEmpty() || minText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields");
                return;
            }

            double amt, ir, min;
            try {
                amt = Double.parseDouble(amountText);
                ir = Double.parseDouble(intText);
                min = Double.parseDouble(minText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers for amount, interest, and minimum payment");
                return;
            }

            if (amt <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than 0");
                return;
            }

            if (ir < 0) {
                JOptionPane.showMessageDialog(this, "Interest rate cannot be negative");
                return;
            }

            if (min <= 0) {
                JOptionPane.showMessageDialog(this, "Minimum payment must be greater than 0");
                return;
            }

            Debt newDebt = new Debt(name, amt, ir, min);
            controller.getManager().pushDebt(newDebt);

            log("PUSH: Added " + name + " ($" + String.format("%.2f", amt) + ") as TOS");

            if (!dueDate.isEmpty()) {
                addEventToCalendar("Due for " + name + " on " + dueDate + " - Payment: $" + String.format("%.2f", min));
            } else {
                addEventToCalendar("Added new debt: " + name + " ($" + String.format("%.2f", amt) + ") as TOS");
            }

            nameField.setText("");
            amountField.setText("");
            intField.setText("");
            minField.setText("");
            dueDateField.setText("");

            refreshAll();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding debt. Please check your inputs.");
        }
    }

    private void makePayment() {
        try {
            String paymentText = payField.getText().trim();
            if (paymentText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a payment amount");
                return;
            }

            double amt;
            try {
                amt = Double.parseDouble(paymentText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for payment");
                return;
            }

            Debt top = controller.getManager().peekTOS();
            if (top != null) {
                if (amt <= 0) {
                    JOptionPane.showMessageDialog(this, "Payment amount must be greater than 0");
                    return;
                }

                if (amt > top.getCurrentBalance()) {
                    int option = JOptionPane.showConfirmDialog(this,
                            "Payment amount ($" + String.format("%.2f", amt) +
                                    ") exceeds current balance ($" + String.format("%.2f", top.getCurrentBalance()) +
                                    ").\nPay full balance instead?",
                            "Confirm Payment",
                            JOptionPane.YES_NO_OPTION);

                    if (option == JOptionPane.YES_OPTION) {
                        amt = top.getCurrentBalance();
                    } else {
                        return;
                    }
                }

                top.makePayment(amt);
                log("PAID: $" + String.format("%.2f", amt) + " to TOS: " + top.getName());

                if (top.isPaidOff()) {
                    Debt paidOffDebt = controller.getManager().popDebt();
                    paidOffDebts.add(0, paidOffDebt);
                    log("COMPLETED: " + top.getName() + " is now paid off and moved to paid-off!");
                    addEventToCalendar(top.getName() + " has been successfully paid off!");

                    if (controller.getManager().peekTOS() == null && !auxiliaryDebts.isEmpty()) {
                        Debt newActive = auxiliaryDebts.remove(0);
                        controller.getManager().pushDebt(newActive);
                        log("MOVED: " + newActive.getName() + " from auxiliary to active as TOS (LIFO)");
                    }
                }

                payField.setText("0.00");
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(this, "No active debt to pay!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error making payment. Please try again.");
        }
    }

    private void log(String s) {
        logsArea.append(" > " + s + "\n");
        logsArea.setCaretPosition(logsArea.getDocument().getLength());
    }

    private void refreshAll() {
        tosLabel.setText("Current TOS: " + getTopName());
        updateCreditCardPanel();
        towerContainer.repaint();
        mainLayer.revalidate();
        mainLayer.repaint();
    }

    private String getTopName() {
        Debt d = controller.getManager().peekTOS();
        return d != null ? d.getName() + " (Position: TOS)" : "None";
    }

    // Placeholder actions for sidebar buttons
    private void onPeekClicked() {
        Debt d = controller.getManager().peekTOS();
        if (d != null) {
            List<Debt> activeDebts = manager.getStackForVisualization();
            int position = activeDebts.indexOf(d) + 1;
            int total = activeDebts.size();

            JOptionPane.showMessageDialog(this,
                    "Top of Stack Details:\n" +
                            "Name: " + d.getName() + "\n" +
                            "Position: " + position + " of " + total + " (TOS)\n" +
                            "Current Balance: $" + String.format("%.2f", d.getCurrentBalance()) + "\n" +
                            "Interest Rate: " + d.getInterestRate() + "%\n" +
                            "Minimum Payment: $" + String.format("%.2f", d.getMinimumPayment()) + "\n" +
                            "Original Amount: $" + String.format("%.2f", d.getOriginalAmount()),
                    "Top of Stack Details",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No active debts!");
        }
    }

    private void onAuxiliary() {
        Debt top = controller.getManager().peekTOS();
        if (top != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Move " + top.getName() + " to auxiliary?\nBalance: $"
                            + String.format("%.2f", top.getCurrentBalance()) + "\nPosition: TOS",
                    "Confirm Move to Auxiliary",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Debt movedDebt = controller.getManager().popDebt();
                auxiliaryDebts.add(0, movedDebt);
                log("MOVED: " + movedDebt.getName() + " from TOS to auxiliary (LIFO)");
                refreshAll();
            }
        } else {
            JOptionPane.showMessageDialog(this, "No active debt to move!");
        }
    }

    private void onSettleClicked() {
        makePayment();
    }

    private void onDeleteClicked() {
        Debt top = controller.getManager().peekTOS();
        if (top != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete " + top.getName() + "?\n" +
                            "Balance: $" + String.format("%.2f", top.getCurrentBalance()) + "\nPosition: TOS",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                controller.getManager().popDebt();
                log("DELETED: " + top.getName() + " from TOS (Balance: $"
                        + String.format("%.2f", top.getCurrentBalance())
                        + ")");
                refreshAll();
            }
        } else {
            JOptionPane.showMessageDialog(this, "No active debt to delete!");
        }
    }

    private void onHistoryClicked() {
        boolean vis = logsScrollPane.isVisible();
        logsScrollPane.setVisible(!vis);
        if (!vis) {
            log("Log panel is now visible");
        }
    }

    private void onProfileClicked() {
        List<Debt> activeDebtsList = manager.getStackForVisualization();
        int activeDebts = activeDebtsList != null ? activeDebtsList.size() : 0;
        double totalBalance = 0;

        if (activeDebts > 0) {
            for (Debt debt : activeDebtsList) {
                totalBalance += debt.getCurrentBalance();
            }
        }

        JOptionPane.showMessageDialog(this,
                "User Profile:\n" +
                        "Username: " + controller.getCurrentUsername() + "\n" +
                        "Name: "
                        + (controller.getCurrentUser() != null ? controller.getCurrentUser().getFullName() : "N/A")
                        + "\n" +
                        "Email: "
                        + (controller.getCurrentUser() != null ? controller.getCurrentUser().getEmail() : "N/A") + "\n"
                        +
                        "Active Debts: " + activeDebts + " (Stack Size)\n" +
                        "Total Active Balance: $" + String.format("%.2f", totalBalance) + "\n" +
                        "Auxiliary Debts: " + auxiliaryDebts.size() + "\n" +
                        "Paid-off Debts: " + paidOffDebts.size() + "\n" +
                        "Stack Order (LIFO): Newest debt is TOS\n" +
                        "Current TOS: " + getTopName(),
                "Profile Summary",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Update the consultation dialog method in UserDashboard.java
    private void showConsultationDialog(Component parentComponent) {
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 10));

        JLabel reasonLabel = new JLabel("Reason for Consultation:");
        JTextField reasonField = new JTextField(20);

        JLabel advisorLabel = new JLabel("Choose Financial Advisor:");

        // Get all financial advisors
        List<User> advisors = DataManager.getFinancialAdvisors();
        String[] advisorOptions;

        if (advisors.isEmpty()) {
            advisorOptions = new String[] { "No advisors available" };
        } else {
            advisorOptions = new String[advisors.size()];
            for (int i = 0; i < advisors.size(); i++) {
                advisorOptions[i] = advisors.get(i).getFullName() + " (" + advisors.get(i).getUsername() + ")";
            }
        }

        JComboBox<String> advisorComboBox = new JComboBox<>(advisorOptions);
        advisorComboBox.setEnabled(!advisors.isEmpty());

        JLabel platformLabel = new JLabel("Platform to use:");
        String[] platforms = { "Zoom", "Google Meet", "Microsoft Teams", "Phone Call", "In Person" };
        JComboBox<String> platformComboBox = new JComboBox<>(platforms);

        JLabel dateLabel = new JLabel("Preferred Date (YYYY-MM-DD):");
        JTextField dateField = new JTextField(20);
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        formPanel.add(reasonLabel);
        formPanel.add(reasonField);

        formPanel.add(advisorLabel);
        formPanel.add(advisorComboBox);

        formPanel.add(platformLabel);
        formPanel.add(platformComboBox);

        formPanel.add(dateLabel);
        formPanel.add(dateField);

        int result = JOptionPane.showConfirmDialog(
                parentComponent,
                formPanel,
                "Consultation Appointment",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String reason = reasonField.getText().trim();
            String advisorSelection = (String) advisorComboBox.getSelectedItem();
            String platform = (String) platformComboBox.getSelectedItem();
            String date = dateField.getText().trim();

            // Validate inputs
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(parentComponent,
                        "Please enter a reason for consultation.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (advisors.isEmpty()) {
                JOptionPane.showMessageDialog(parentComponent,
                        "No financial advisors are currently available. Please try again later.",
                        "No Advisors", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate date format (simple check)
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(parentComponent,
                        "Please enter date in YYYY-MM-DD format.",
                        "Date Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Parse advisor information
            String advisorUsername = advisorSelection.substring(advisorSelection.lastIndexOf('(') + 1,
                    advisorSelection.lastIndexOf(')'));

            // Create consultation request
            ConsultationRequest request = new ConsultationRequest(
                    controller.getCurrentUsername(),
                    controller.getCurrentUser().getFullName(),
                    reason,
                    date + " " + platform);

            // Save the request
            DataManager.addConsultationRequest(request);

            System.out.println("--- Consultation Request Submitted ---");
            System.out.println("Client: " + controller.getCurrentUser().getFullName());
            System.out.println("Reason: " + reason);
            System.out.println("Advisor: " + advisorSelection);
            System.out.println("Platform: " + platform);
            System.out.println("Date: " + date);

            log("Consultation request submitted to " + advisorSelection);

            JOptionPane.showMessageDialog(parentComponent,
                    "Consultation request submitted successfully!\n" +
                            "Your request has been sent to the financial advisor.\n" +
                            "You will be contacted for confirmation.",
                    "Request Submitted",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println("Consultation request cancelled.");
        }
    }

    // --- Custom Panels ---

    class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
    }

    private class TowerVisualizationPanel extends JPanel {
        public TowerVisualizationPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 24));
            String title = "THE HANOI DEBT TOWER";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, (w - fm.stringWidth(title)) / 2, 40);

            int baseY = h - 50;
            g2.setColor(new Color(100, 40, 100));
            g2.fillRoundRect(50, baseY, w - 100, 15, 10, 10);

            String[] labels = { "ACTIVE DEBT", "AUXILIARY", "PAID-OFF" };
            int colW = w / 3;

            g2.setColor(new Color(120, 40, 120));
            for (int i = 0; i < 3; i++) {
                int cx = colW * i + colW / 2;
                g2.fillRoundRect(cx - 5, 80, 10, baseY - 80, 10, 10);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                String lbl = labels[i];
                g2.drawString(lbl, cx - fm.stringWidth(lbl) / 2 + 30, baseY + 40);
                g2.setColor(new Color(120, 40, 120));
            }

            // Draw stacks with proper LIFO visualization (TOS on top)
            drawStack(g2, manager.getStackForVisualization(), colW / 2, baseY);
            drawStack(g2, auxiliaryDebts, colW + colW / 2, baseY);
            drawStack(g2, paidOffDebts, colW * 2 + colW / 2, baseY);
        }

        private void drawStack(Graphics2D g2, List<Debt> debts, int centerX, int baseY) {
            if (debts == null || debts.isEmpty())
                return;

            int brickH = 40;
            int gap = 5;
            int currentY = baseY - gap - brickH;

            int totalDebts = Math.min(debts.size(), 6);

            for (int i = 0; i < totalDebts; i++) {
                // For LIFO: last element is TOS (on top), first element is bottom
                int debtIndex;
                if (centerX == getWidth() / 6) { // Active debts - reverse for proper TOS display
                    debtIndex = debts.size() - 1 - i;
                } else { // Auxiliary and paid-off - already in LIFO order
                    debtIndex = i;
                }

                Debt d = debts.get(debtIndex);
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                Color c;
                if (i == 0 && centerX == getWidth() / 6) // TOS for active debts
                    c = new Color(220, 53, 69); // Red
                else if (i == 1 && centerX == getWidth() / 6)
                    c = new Color(253, 126, 20); // Orange
                else if (centerX > getWidth() / 2 + 100) // Paid-off
                    c = new Color(100, 200, 100); // Green
                else // Auxiliary or other active debts
                    c = new Color(150, 150, 200); // Blue

                g2.setColor(c);

                int width = 250 + ((totalDebts - i - 1) * 20);
                if (i == 0 && centerX == getWidth() / 6) // Make TOS wider
                    width += 50;

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 15, 15);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                String info = d.getName() + " $" + (int) d.getCurrentBalance();

                // Truncate if too long
                if (info.length() > 30) {
                    info = info.substring(0, 27) + "...";
                }

                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(info, centerX - fm.stringWidth(info) / 2, yPos + 25);

                // Add TOS indicator for active debts
                if (i == 0 && centerX == getWidth() / 6) {
                    g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                    g2.drawString("TOS", centerX - width / 2 + 10, yPos + 38);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            new UserDashboard(controller).setVisible(true);
        });
    }
}