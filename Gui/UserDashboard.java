package Gui;

import Model.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserDashboard extends JFrame {
    private AppController controller;
    private DebtManager manager;
    private java.util.Stack<Debt> auxiliaryDebts = new java.util.Stack<>();
    private java.util.Stack<Debt> paidOffDebts = new java.util.Stack<>();

    private JLayeredPane layeredPane;
    private JPanel mainLayer;
    private JLabel backgroundLabel;
    private JPanel towerContainer;
    private JPanel cardPanel;
    private JPanel addDebtPanel;
    private JPanel paymentPanel;
    private JPanel calendarPanel;
    private JTextArea logsArea;
    private JScrollPane logsScrollPane;
    private JTextField nameField, amountField, intField, minField, payField, dueDateField;
    private JTextArea calendarEventsArea;
    private JLabel cardTitleLabel, posLabel, balValLabel, intValLabel, ogAmtLabel, minPayLabel;
    private JProgressBar progressBar;
    private JLabel tosLabel;
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

        sidebar.add(createIconButton("Consultation", "Request Consultation", e -> showConsultationDialog()));
        sidebar.add(createIconButton("My Requests", "View My Requests", e -> showMyRequests()));
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
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.saveAllData();
            controller.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new Login(controller).setVisible(true));
        }
    }

    private void createTopRow() {
        towerContainer = new RoundedPanel(25, Color.WHITE);
        towerContainer.setLayout(null);
        towerContainer.setBounds(150, 30, 1300, 530);

        TowerVisualizationPanel towerVis = new TowerVisualizationPanel();
        towerVis.setBounds(50, 60, 1200, 400);
        towerContainer.add(towerVis);
        mainLayer.add(towerContainer);

        addDebtPanel = createAddDebtPanel();
        addDebtPanel.setBounds(1500, 30, 350, 530);
        mainLayer.add(addDebtPanel);
    }

    private void createBottomRow() {
        int startY = 580;
        int height = 280;

        cardPanel = createCardPanel();
        cardPanel.setBounds(150, startY, 700, height);
        mainLayer.add(cardPanel);

        paymentPanel = createPaymentPanel();
        paymentPanel.setBounds(870, startY, 500, height);
        mainLayer.add(paymentPanel);

        calendarPanel = createEventCalendarPanel();
        calendarPanel.setBounds(1400, startY, 500, height);
        mainLayer.add(calendarPanel);
    }

    private void createLogsPanel() {
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

        int y = 70, gap = 65, fieldH = 35, width = 310;

        addLabel(panel, "Debt Name", 20, y - 20);
        nameField = addTextField(panel, "Credit Card, Loan...", 20, y, width, fieldH);

        y += gap;
        addLabel(panel, "Total Amount ($)", 20, y - 20);
        amountField = addTextField(panel, "5000.00", 20, y, width, fieldH);

        y += gap;
        addLabel(panel, "Interest Rate (%)", 20, y - 20);
        intField = addTextField(panel, "15.5", 20, y, width, fieldH);

        y += gap;
        addLabel(panel, "Minimum Payment ($)", 20, y - 20);
        minField = addTextField(panel, "100.00", 20, y, width, fieldH);

        y += gap;
        addLabel(panel, "Due Date (e.g., Dec 31)", 20, y - 20);
        dueDateField = addTextField(panel, "Dec 31", 20, y, width, fieldH);

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

        JButton minBtn = new JButton("Min Payment");
        styleQuickButton(minBtn);
        minBtn.setBounds(20, 170, 180, 30);
        minBtn.addActionListener(e -> {
            Debt top = manager.peekTOS();
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
            Debt top = manager.peekTOS();
            if (top != null)
                payField.setText(String.format("%.2f", top.getCurrentBalance()));
            else
                JOptionPane.showMessageDialog(this, "No active debt to pay!");
        });
        panel.add(fullBtn);

        JButton settleBtn = createOrangeButton("SETTLE PAYMENT");
        settleBtn.setBounds(20, 220, 390, 40);
        settleBtn.addActionListener(e -> makePayment());
        panel.add(settleBtn);

        return panel;
    }

    private JPanel createCardPanel() {
        RoundedPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(null);

        cardTitleLabel = new JLabel("Debt Details - TOS");
        cardTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        cardTitleLabel.setBounds(20, 20, 200, 25);
        panel.add(cardTitleLabel);

        posLabel = new JLabel("Position: TOS (Top of Stack)");
        posLabel.setForeground(Color.GRAY);
        posLabel.setBounds(20, 45, 300, 20);
        panel.add(posLabel);

        JLabel balLabel = new JLabel("Current Balance");
        balLabel.setForeground(Color.GRAY);
        balLabel.setBounds(20, 90, 150, 20);
        panel.add(balLabel);

        balValLabel = new JLabel("$0.00");
        balValLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        balValLabel.setBounds(20, 115, 200, 35);
        panel.add(balValLabel);

        JLabel intLabel = new JLabel("% Interest Rate");
        intLabel.setForeground(Color.GRAY);
        intLabel.setBounds(250, 90, 150, 20);
        panel.add(intLabel);

        intValLabel = new JLabel("0.0%");
        intValLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        intValLabel.setForeground(new Color(234, 88, 12));
        intValLabel.setBounds(250, 115, 150, 35);
        panel.add(intValLabel);

        progressBar = new JProgressBar();
        progressBar.setValue(0);
        progressBar.setBounds(20, 170, 360, 8);
        progressBar.setForeground(new Color(234, 88, 12));
        progressBar.setBackground(new Color(240, 240, 240));
        progressBar.setBorderPainted(false);
        panel.add(progressBar);

        ogAmtLabel = new JLabel("Original Amount: $0.00");
        ogAmtLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        ogAmtLabel.setBounds(20, 190, 200, 20);
        panel.add(ogAmtLabel);

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
            java.util.List<Debt> activeDebts = manager.getStackForVisualization();
            if (activeDebts != null && !activeDebts.isEmpty()) {
                Debt topDebt = activeDebts.get(0);
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
            e.printStackTrace();
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

    // Consultation dialog
    private void showConsultationDialog() {
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 10));

        JLabel reasonLabel = new JLabel("Reason for Consultation:");
        JTextField reasonField = new JTextField(20);

        JLabel advisorLabel = new JLabel("Choose Financial Advisor:");
        ArrayList<User> advisors = DataManager.getFinancialAdvisors(); // Changed to ArrayList
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

        JLabel feeLabel = new JLabel("Consultation Fee ($):");
        JTextField feeField = new JTextField("50.00");

        formPanel.add(reasonLabel);
        formPanel.add(reasonField);
        formPanel.add(advisorLabel);
        formPanel.add(advisorComboBox);
        formPanel.add(platformLabel);
        formPanel.add(platformComboBox);
        formPanel.add(feeLabel);
        formPanel.add(feeField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Request Consultation",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String reason = reasonField.getText().trim();
            String advisorSelection = (String) advisorComboBox.getSelectedItem();
            String platform = (String) platformComboBox.getSelectedItem();
            String feeText = feeField.getText().trim();

            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a reason for consultation.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (advisors.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No financial advisors are currently available.",
                        "No Advisors", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double fee;
            try {
                fee = Double.parseDouble(feeText);
                if (fee <= 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid consultation fee.",
                        "Fee Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String advisorUsername = advisorSelection.substring(
                    advisorSelection.lastIndexOf('(') + 1,
                    advisorSelection.lastIndexOf(')'));
            String advisorName = advisorSelection.substring(0, advisorSelection.lastIndexOf('(')).trim();

            ConsultationRequest request = new ConsultationRequest(
                    controller.getCurrentUsername(),
                    controller.getCurrentUser().getFullName(),
                    reason,
                    advisorUsername,
                    advisorName,
                    platform);

            DataManager.addConsultationRequest(request);

            log("Consultation request submitted to " + advisorName);
            addEventToCalendar("Consultation requested with " + advisorName + " - $" + fee);

            JOptionPane.showMessageDialog(this,
                    "Consultation request submitted successfully!\n" +
                            "Your request has been sent to " + advisorName + ".\n" +
                            "You will be notified when they respond.\n" +
                            "Consultation Fee: $" + String.format("%.2f", fee),
                    "Request Submitted",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showMyRequests() {
        ArrayList<ConsultationRequest> requests = DataManager.loadClientRequests(controller.getCurrentUsername()); // Changed
                                                                                                                   // to
                                                                                                                   // ArrayList

        if (requests.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "You have no pending consultation requests.",
                    "My Requests",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog requestDialog = new JDialog(this, "My Consultation Requests", true);
        requestDialog.setSize(600, 400);
        requestDialog.setLocationRelativeTo(this);
        requestDialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("My Consultation Requests (" + requests.size() + ")");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(10));

        for (ConsultationRequest request : requests) {
            mainPanel.add(createRequestPanel(request));
            mainPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        requestDialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> requestDialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeBtn);
        requestDialog.add(buttonPanel, BorderLayout.SOUTH);

        requestDialog.setVisible(true);
    }

    private JPanel createRequestPanel(ConsultationRequest request) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 2, 2));

        Color statusColor;
        switch (request.getStatus()) {
            case "PENDING":
                statusColor = Color.ORANGE;
                break;
            case "SCHEDULED":
                statusColor = Color.GREEN.darker();
                break;
            case "REJECTED":
                statusColor = Color.RED;
                break;
            case "COMPLETED":
                statusColor = Color.BLUE;
                break;
            default:
                statusColor = Color.GRAY;
        }

        JLabel statusLabel = new JLabel("Status: " + request.getStatus());
        statusLabel.setForeground(statusColor);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel advisorLabel = new JLabel("Advisor: " + request.getAdvisorName());
        JLabel reasonLabel = new JLabel("Reason: " + request.getReason());
        JLabel platformLabel = new JLabel("Platform: " + request.getPlatform());
        JLabel dateLabel = new JLabel("Requested: " +
                new SimpleDateFormat("MMM dd, yyyy").format(request.getRequestDate()));

        infoPanel.add(statusLabel);
        infoPanel.add(advisorLabel);
        infoPanel.add(reasonLabel);
        infoPanel.add(platformLabel);
        infoPanel.add(dateLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        if ("REJECTED".equals(request.getStatus())) {
            JLabel rejectedLabel = new JLabel("Request was rejected");
            rejectedLabel.setForeground(Color.RED);
            rejectedLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
            panel.add(rejectedLabel, BorderLayout.SOUTH);
        }

        return panel;
    }

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

            Debt newDebt = new Debt(name, amt, ir, min);
            manager.pushDebt(newDebt);

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

            double amt = Double.parseDouble(paymentText);
            Debt top = manager.peekTOS();

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
                            "Confirm Payment", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        amt = top.getCurrentBalance();
                    } else {
                        return;
                    }
                }

                top.makePayment(amt);
                log("PAID: $" + String.format("%.2f", amt) + " to TOS: " + top.getName());

                if (top.isPaidOff()) {
                    Debt paidOffDebt = manager.popDebt();
                    paidOffDebts.push(paidOffDebt);
                    log("COMPLETED: " + top.getName() + " is now paid off!");

                    if (manager.peekTOS() == null && !auxiliaryDebts.isEmpty()) {
                        Debt newActive = auxiliaryDebts.pop();
                        manager.pushDebt(newActive);
                        log("MOVED: " + newActive.getName() + " from auxiliary to active as TOS");
                    }
                }

                payField.setText("0.00");
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(this, "No active debt to pay!");
            }
        } catch (Exception e) {
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
        Debt d = manager.peekTOS();
        return d != null ? d.getName() + " (Position: TOS)" : "None";
    }

    private void onPeekClicked() {
        Debt d = manager.peekTOS();
        if (d != null) {
            java.util.List<Debt> activeDebts = manager.getStackForVisualization();
            JOptionPane.showMessageDialog(this,
                    "Top of Stack Details:\n" +
                            "Name: " + d.getName() + "\n" +
                            "Position: TOS (1 of " + activeDebts.size() + ")\n" +
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
        Debt top = manager.peekTOS();
        if (top != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Move " + top.getName() + " to auxiliary?\nBalance: $"
                            + String.format("%.2f", top.getCurrentBalance()) + "\nPosition: TOS",
                    "Confirm Move to Auxiliary", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Debt movedDebt = manager.popDebt();
                auxiliaryDebts.push(movedDebt);
                log("MOVED: " + movedDebt.getName() + " from TOS to auxiliary");

                Debt newTOS = manager.peekTOS();
                tosLabel.setText("Current TOS: " + (newTOS != null ? newTOS.getName() + " (Position: TOS)" : "None"));
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
        Debt top = manager.peekTOS();
        if (top != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete " + top.getName() + "?\n" +
                            "Balance: $" + String.format("%.2f", top.getCurrentBalance()) + "\nPosition: TOS",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                manager.popDebt();
                log("DELETED: " + top.getName() + " from TOS");

                Debt newTOS = manager.peekTOS();
                tosLabel.setText("Current TOS: " + (newTOS != null ? newTOS.getName() + " (Position: TOS)" : "None"));
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
        java.util.List<Debt> activeDebtsList = manager.getStackForVisualization();
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

    // Custom Panels
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

            drawActiveStack(g2, colW / 2, baseY);
            drawAuxiliaryStack(g2, colW + colW / 2, baseY);
            drawPaidOffStack(g2, colW * 2 + colW / 2, baseY);
        }

        private void drawActiveStack(Graphics2D g2, int centerX, int baseY) {
            java.util.List<Debt> debts = manager.getStackForVisualization();
            if (debts == null || debts.isEmpty())
                return;

            int brickH = 40;
            int gap = 5;

            for (int i = 0; i < Math.min(debts.size(), 6); i++) {
                Debt d = debts.get(i);
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                Color c;
                if (i == 0)
                    c = new Color(220, 53, 69);
                else if (i == 1)
                    c = new Color(253, 126, 20);
                else
                    c = new Color(255, 193, 7);

                g2.setColor(c);
                int width = 250 + ((debts.size() - i - 1) * 20);
                if (i == 0)
                    width += 50;

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 15, 15);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));

                String info = d.getName() + " $" + (int) d.getCurrentBalance();
                if (info.length() > 30)
                    info = info.substring(0, 27) + "...";

                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(info, centerX - fm.stringWidth(info) / 2, yPos + 25);

                if (i == 0) {
                    g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                    g2.drawString("TOS", centerX - width / 2 + 10, yPos + 38);
                }
            }
        }

        private void drawAuxiliaryStack(Graphics2D g2, int centerX, int baseY) {
            if (auxiliaryDebts.isEmpty())
                return;

            int brickH = 35;
            int gap = 5;
            Debt[] debtsArray = auxiliaryDebts.toArray(new Debt[0]);

            for (int i = 0; i < Math.min(debtsArray.length, 6); i++) {
                Debt d = debtsArray[i];
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                Color c = new Color(150, 150, 200);
                g2.setColor(c);
                int width = 200 + ((debtsArray.length - i - 1) * 15);

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));

                String info = d.getName() + " $" + (int) d.getCurrentBalance();
                if (info.length() > 25)
                    info = info.substring(0, 22) + "...";
                g2.drawString(info, centerX - width / 2 + 10, yPos + 22);
            }
        }

        private void drawPaidOffStack(Graphics2D g2, int centerX, int baseY) {
            if (paidOffDebts.isEmpty())
                return;

            int brickH = 30;
            int gap = 5;
            Debt[] debtsArray = paidOffDebts.toArray(new Debt[0]);

            for (int i = 0; i < Math.min(debtsArray.length, 8); i++) {
                Debt d = debtsArray[i];
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                Color c = new Color(100, 200, 100);
                g2.setColor(c);
                int width = 180 + ((debtsArray.length - i - 1) * 10);

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 8));

                String info = d.getName();
                if (info.length() > 30)
                    info = info.substring(0, 27) + "...";
                g2.drawString(info, centerX - width / 2 + 5, yPos + 18);

                g2.setFont(new Font("SansSerif", Font.BOLD, 7));
                g2.drawString("PAID", centerX - width / 2 + 5, yPos + 28);
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