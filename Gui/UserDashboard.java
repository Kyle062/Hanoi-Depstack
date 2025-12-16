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
    private ArrayList<Debt> auxiliaryDebts = new ArrayList<>();
    private ArrayList<Debt> paidOffDebts = new ArrayList<>();

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
        SwingUtilities.invokeLater(() -> refreshConsultationRequests());
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
        sidebar.setLayout(new GridLayout(11, 1, 0, 20)); // Changed from 9 to 11 for new buttons
        sidebar.setOpaque(false);
        sidebar.setBounds(20, 150, 100, 650);

        sidebar.add(createIconButton("Consultation", "Request Consultation", e -> showConsultationDialog()));
        sidebar.add(createIconButton("My Requests", "View My Requests", e -> showMyRequests()));
        sidebar.add(createIconButton("PEEK", "View Top", e -> onPeekClicked()));
        
        // New UPDATE button
        sidebar.add(createIconButton("UPDATE", "Update Debt Details", e -> onUpdateClicked()));
        
        sidebar.add(createIconButton("PAY", "Settle", e -> onSettleClicked()));
        
        // New TRAVERSAL button
        sidebar.add(createIconButton("SEARCH", "Search & Filter Debts", e -> onTraversalClicked()));
        
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

        // Strategy buttons section - VIEW ONLY, doesn't change LIFO order
        JLabel strategyLabel = new JLabel("View Strategies:");
        strategyLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        strategyLabel.setBounds(20, 440, 100, 20);
        panel.add(strategyLabel);

        // Avalanche button - View only
        JButton avalancheBtn = new JButton("Avalanche View");
        avalancheBtn.setBounds(20, 465, 150, 30);
        avalancheBtn.setBackground(new Color(52, 152, 219)); // Blue color
        avalancheBtn.setForeground(Color.WHITE);
        avalancheBtn.setFocusPainted(false);
        avalancheBtn.setBorderPainted(false);
        avalancheBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        avalancheBtn.setToolTipText("Show debts sorted by highest interest rate (View Only)");
        avalancheBtn.addActionListener(e -> showAvalancheView());
        panel.add(avalancheBtn);

        // Snowball button - View only
        JButton snowballBtn = new JButton("Snowball View");
        snowballBtn.setBounds(180, 465, 150, 30);
        snowballBtn.setBackground(new Color(46, 204, 113)); // Green color
        snowballBtn.setForeground(Color.WHITE);
        snowballBtn.setFocusPainted(false);
        snowballBtn.setBorderPainted(false);
        snowballBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        snowballBtn.setToolTipText("Show debts sorted by smallest balance (View Only)");
        snowballBtn.addActionListener(e -> showSnowballView());
        panel.add(snowballBtn);

        return panel;
    }

    private void showAvalancheView() {
        ArrayList<Debt> activeDebts = getDebtsForVisualization();
        logStrategyView("Avalanche");
        if (activeDebts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No active debts to display!",
                    "Avalanche Strategy View",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create a copy of debts for sorting (doesn't affect actual stack)
        ArrayList<Debt> sortedDebts = new ArrayList<>(activeDebts);

        // Sort by interest rate (highest first)
        Collections.sort(sortedDebts, new Comparator<Debt>() {
            @Override
            public int compare(Debt d1, Debt d2) {
                return Double.compare(d2.getInterestRate(), d1.getInterestRate()); // Descending
            }
        });

        // Build display message
        StringBuilder message = new StringBuilder();
        message.append("AVALANCHE STRATEGY VIEW\n");
        message.append("=======================\n");
        message.append("Debts sorted by HIGHEST interest rate first\n");
        message.append("(This is a VIEW ONLY - Stack remains in LIFO order)\n\n");

        // Current LIFO order
        message.append("Current LIFO Stack Order (Newest to Oldest):\n");
        for (int i = 0; i < activeDebts.size(); i++) {
            Debt debt = activeDebts.get(i);
            String position = (i == 0) ? "TOS (Newest)" : "Position #" + i;
            message.append(position).append(": ").append(debt.getName())
                    .append(" (Int: ").append(debt.getInterestRate()).append("%)\n");
        }

        message.append("\n----------------------------------------\n\n");

        // Avalanche sorted view
        message.append("Avalanche Strategy Order (Highest Interest First):\n\n");
        for (int i = 0; i < sortedDebts.size(); i++) {
            Debt debt = sortedDebts.get(i);
            message.append((i + 1)).append(". ").append(debt.getName()).append("\n");
            message.append("   Interest Rate: ").append(debt.getInterestRate()).append("%\n");
            message.append("   Balance: $").append(String.format("%.2f", debt.getCurrentBalance())).append("\n");
            message.append("   Min Payment: $").append(String.format("%.2f", debt.getMinimumPayment())).append("\n");

            // Show LIFO position
            int lifoPosition = activeDebts.indexOf(debt);
            String lifoStatus;
            if (lifoPosition == 0) {
                lifoStatus = "TOS (Top of Stack)";
            } else {
                lifoStatus = "Position #" + lifoPosition + " in LIFO stack";
            }
            message.append("   LIFO Status: ").append(lifoStatus).append("\n\n");
        }

        // Show recommended TOS for avalanche
        Debt highestInterest = sortedDebts.get(0);
        message.append("RECOMMENDED by Avalanche:\n");
        message.append("Focus on: ").append(highestInterest.getName()).append("\n");
        message.append("Highest interest rate: ").append(highestInterest.getInterestRate()).append("%\n");
        message.append("Current LIFO TOS: ").append(activeDebts.get(0).getName()).append("\n");

        JOptionPane.showMessageDialog(this,
                message.toString(),
                "Avalanche Strategy View (Highest Interest First)",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSnowballView() {
        ArrayList<Debt> activeDebts = getDebtsForVisualization();
        logStrategyView("Snowball");
        if (activeDebts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No active debts to display!",
                    "Snowball Strategy View",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create a copy of debts for sorting (doesn't affect actual stack)
        ArrayList<Debt> sortedDebts = new ArrayList<>(activeDebts);

        // Sort by balance (smallest first)
        Collections.sort(sortedDebts, new Comparator<Debt>() {
            @Override
            public int compare(Debt d1, Debt d2) {
                return Double.compare(d1.getCurrentBalance(), d2.getCurrentBalance()); // Ascending
            }
        });

        // Build display message
        StringBuilder message = new StringBuilder();
        message.append("SNOWBALL STRATEGY VIEW\n");
        message.append("======================\n");
        message.append("Debts sorted by SMALLEST balance first\n");
        message.append("(This is a VIEW ONLY - Stack remains in LIFO order)\n\n");

        // Current LIFO order
        message.append("Current LIFO Stack Order (Newest to Oldest):\n");
        for (int i = 0; i < activeDebts.size(); i++) {
            Debt debt = activeDebts.get(i);
            String position = (i == 0) ? "TOS (Newest)" : "Position #" + i;
            message.append(position).append(": ").append(debt.getName())
                    .append(" (Bal: $").append(String.format("%.2f", debt.getCurrentBalance())).append(")\n");
        }

        message.append("\n----------------------------------------\n\n");

        // Snowball sorted view
        message.append("Snowball Strategy Order (Smallest Balance First):\n\n");
        for (int i = 0; i < sortedDebts.size(); i++) {
            Debt debt = sortedDebts.get(i);
            message.append((i + 1)).append(". ").append(debt.getName()).append("\n");
            message.append("   Balance: $").append(String.format("%.2f", debt.getCurrentBalance())).append("\n");
            message.append("   Interest Rate: ").append(debt.getInterestRate()).append("%\n");
            message.append("   Min Payment: $").append(String.format("%.2f", debt.getMinimumPayment())).append("\n");

            // Show LIFO position
            int lifoPosition = activeDebts.indexOf(debt);
            String lifoStatus;
            if (lifoPosition == 0) {
                lifoStatus = "TOS (Top of Stack)";
            } else {
                lifoStatus = "Position #" + lifoPosition + " in LIFO stack";
            }
            message.append("   LIFO Status: ").append(lifoStatus).append("\n\n");
        }

        // Show recommended TOS for snowball
        Debt smallestBalance = sortedDebts.get(0);
        message.append("RECOMMENDED by Snowball:\n");
        message.append("Focus on: ").append(smallestBalance.getName()).append("\n");
        message.append("Smallest balance: $").append(String.format("%.2f", smallestBalance.getCurrentBalance()))
                .append("\n");
        message.append("Current LIFO TOS: ").append(activeDebts.get(0).getName()).append("\n");

        JOptionPane.showMessageDialog(this,
                message.toString(),
                "Snowball Strategy View (Smallest Balance First)",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void logStrategyView(String strategyName) {
        log("VIEWED: " + strategyName + " strategy (View only - LIFO unchanged)");
        addEventToCalendar("Viewed " + strategyName + " debt strategy");
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
            // Get debts in LIFO order
            ArrayList<Debt> activeDebts = getDebtsForVisualization();
            if (activeDebts != null && !activeDebts.isEmpty()) {
                // The first element (index 0) is the TOS (newest debt)
                Debt topDebt = activeDebts.get(0); // FIXED: Changed from last element to first element

                cardTitleLabel.setText("Debt Details - TOS (Newest)");
                posLabel.setText("Position: TOS - Newest of " + activeDebts.size() + " debts");
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
        ArrayList<User> advisors = DataManager.getFinancialAdvisors();
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
        ArrayList<ConsultationRequest> requests = DataManager.loadClientRequests(controller.getCurrentUsername());

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

        // Add a refresh button to the header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("My Consultation Requests (" + requests.size() + ")");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            // Close and reopen the dialog to refresh
            requestDialog.dispose();
            SwingUtilities.invokeLater(() -> showMyRequests());
        });

        headerPanel.add(title, BorderLayout.CENTER);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        mainPanel.add(headerPanel);
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

    private void refreshConsultationRequests() {
        ArrayList<ConsultationRequest> requests = DataManager.loadClientRequests(controller.getCurrentUsername());
        log("Refreshed consultation requests. Total: " + requests.size());
        addEventToCalendar("Consultation requests refreshed at " + new SimpleDateFormat("hh:mm a").format(new Date()));
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

            // IMPORTANT: Ensure the debt is added as TOS (newest)
            manager.pushDebt(newDebt);

            // Log with emphasis on it being newest
            log("PUSH: Added " + name + " ($" + String.format("%.2f", amt) + ") as NEW TOS (Newest debt)");

            if (!dueDate.isEmpty()) {
                addEventToCalendar("Due for " + name + " on " + dueDate + " - Payment: $" + String.format("%.2f", min));
            } else {
                addEventToCalendar("Added NEW debt: " + name + " ($" + String.format("%.2f", amt) + ") as TOS");
            }

            // Clear fields
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
                    // Add to beginning of ArrayList to maintain LIFO order (newest first)
                    paidOffDebts.add(0, paidOffDebt);
                    log("COMPLETED: " + top.getName() + " is now paid off!");

                    if (manager.peekTOS() == null && !auxiliaryDebts.isEmpty()) {
                        // Remove from beginning of ArrayList (LIFO)
                        Debt newActive = auxiliaryDebts.remove(0);
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
            ArrayList<Debt> activeDebts = getDebtsForVisualization();
            JOptionPane.showMessageDialog(this,
                    "Top of Stack Details:\n" +
                            "Name: " + d.getName() + "\n" +
                            "Position: TOS (Newest of " + activeDebts.size() + ")\n" +
                            "Current Balance: $" + String.format("%.2f", d.getCurrentBalance()) + "\n" +
                            "Interest Rate: " + d.getInterestRate() + "%\n" +
                            "Minimum Payment: $" + String.format("%.2f", d.getMinimumPayment()) + "\n" +
                            "Original Amount: $" + String.format("%.2f", d.getOriginalAmount()) + "\n" +
                            "Stack Order: LIFO (Last-In-First-Out)\n" +
                            "Description: This is the most recently added debt",
                    "Top of Stack Details (NEWEST)",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No active debts!");
        }
    }

    private void onUpdateClicked() {
        // First, ask for debt name to identify which debt to update
        String debtName = JOptionPane.showInputDialog(this,
                "Enter the name of the debt you want to update:",
                "Update Debt",
                JOptionPane.QUESTION_MESSAGE);
        
        if (debtName == null || debtName.trim().isEmpty()) {
            return; // User cancelled or entered empty
        }
        
        debtName = debtName.trim();
        
        // Search for the debt in all stacks
        Debt foundDebt = null;
        String debtLocation = "";
        
        // Search in active stack
        ArrayList<Debt> activeDebts = getDebtsForVisualization();
        for (Debt debt : activeDebts) {
            if (debt.getName().equalsIgnoreCase(debtName)) {
                foundDebt = debt;
                debtLocation = "Active Stack";
                break;
            }
        }
        
        // Search in auxiliary stack
        if (foundDebt == null) {
            for (Debt debt : auxiliaryDebts) {
                if (debt.getName().equalsIgnoreCase(debtName)) {
                    foundDebt = debt;
                    debtLocation = "Auxiliary Stack";
                    break;
                }
            }
        }
        
        // Search in paid-off stack
        if (foundDebt == null) {
            for (Debt debt : paidOffDebts) {
                if (debt.getName().equalsIgnoreCase(debtName)) {
                    foundDebt = debt;
                    debtLocation = "Paid-Off Stack";
                    break;
                }
            }
        }
        
        if (foundDebt == null) {
            JOptionPane.showMessageDialog(this,
                    "Debt with name '" + debtName + "' not found in any stack.",
                    "Debt Not Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show current details
        JOptionPane.showMessageDialog(this,
                "Found Debt Details:\n" +
                "Name: " + foundDebt.getName() + "\n" +
                "Location: " + debtLocation + "\n" +
                "Current Balance: $" + String.format("%.2f", foundDebt.getCurrentBalance()) + "\n" +
                "Interest Rate: " + foundDebt.getInterestRate() + "%\n" +
                "Minimum Payment: $" + String.format("%.2f", foundDebt.getMinimumPayment()) + "\n" +
                "Original Amount: $" + String.format("%.2f", foundDebt.getOriginalAmount()),
                "Current Debt Details",
                JOptionPane.INFORMATION_MESSAGE);
        
        // Create update dialog
        JPanel updatePanel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JLabel newBalanceLabel = new JLabel("New Balance ($):");
        JTextField newBalanceField = new JTextField(String.format("%.2f", foundDebt.getCurrentBalance()));
        
        JLabel newInterestLabel = new JLabel("New Interest Rate (%):");
        JTextField newInterestField = new JTextField(String.format("%.1f", foundDebt.getInterestRate()));
        
        JLabel newMinPaymentLabel = new JLabel("New Minimum Payment ($):");
        JTextField newMinPaymentField = new JTextField(String.format("%.2f", foundDebt.getMinimumPayment()));
        
        JLabel newNameLabel = new JLabel("New Name (optional):");
        JTextField newNameField = new JTextField(foundDebt.getName());
        
        updatePanel.add(newBalanceLabel);
        updatePanel.add(newBalanceField);
        updatePanel.add(newInterestLabel);
        updatePanel.add(newInterestField);
        updatePanel.add(newMinPaymentLabel);
        updatePanel.add(newMinPaymentField);
        updatePanel.add(newNameLabel);
        updatePanel.add(newNameField);
        
        int result = JOptionPane.showConfirmDialog(this,
                updatePanel,
                "Update Debt Details",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                // Get new values
                double newBalance = Double.parseDouble(newBalanceField.getText().trim());
                double newInterest = Double.parseDouble(newInterestField.getText().trim());
                double newMinPayment = Double.parseDouble(newMinPaymentField.getText().trim());
                String newName = newNameField.getText().trim();
                
                // Validate
                if (newBalance < 0 || newInterest < 0 || newMinPayment < 0) {
                    throw new NumberFormatException("Negative values not allowed");
                }
                
                // Update debt (Note: Debt class needs setters for this to work)
                // Since Debt class doesn't have setters, we need to create a new debt
                // and replace the old one in the appropriate stack
                
                // Create updated debt
                Debt updatedDebt = new Debt(
                    newName.isEmpty() ? foundDebt.getName() : newName,
                    newBalance,
                    newInterest,
                    newMinPayment
                );
                
                // Replace in appropriate stack
                if (debtLocation.equals("Active Stack")) {
                    // Find and replace in active stack
                    Stack<Debt> tempStack = new Stack<>();
                    boolean replaced = false;
                    
                    // Pop until we find the debt
                    while (!manager.getStackForVisualization().isEmpty()) {
                        Debt current = manager.popDebt();
                        if (current.getName().equals(foundDebt.getName())) {
                            // Found it, push the updated version
                            tempStack.push(updatedDebt);
                            replaced = true;
                        } else {
                            tempStack.push(current);
                        }
                    }
                    
                    // Push everything back
                    while (!tempStack.isEmpty()) {
                        manager.pushDebt(tempStack.pop());
                    }
                    
                    if (!replaced) {
                        // Try to find in the visualization list
                        for (int i = 0; i < activeDebts.size(); i++) {
                            if (activeDebts.get(i).getName().equals(foundDebt.getName())) {
                                activeDebts.set(i, updatedDebt);
                                replaced = true;
                                break;
                            }
                        }
                    }
                } else if (debtLocation.equals("Auxiliary Stack")) {
                    // Replace in auxiliary
                    for (int i = 0; i < auxiliaryDebts.size(); i++) {
                        if (auxiliaryDebts.get(i).getName().equals(foundDebt.getName())) {
                            auxiliaryDebts.set(i, updatedDebt);
                            break;
                        }
                    }
                } else if (debtLocation.equals("Paid-Off Stack")) {
                    // Replace in paid-off
                    for (int i = 0; i < paidOffDebts.size(); i++) {
                        if (paidOffDebts.get(i).getName().equals(foundDebt.getName())) {
                            paidOffDebts.set(i, updatedDebt);
                            break;
                        }
                    }
                }
                
                log("UPDATED: Debt '" + foundDebt.getName() + "' updated in " + debtLocation);
                addEventToCalendar("Updated debt: " + foundDebt.getName() + " in " + debtLocation);
                
                JOptionPane.showMessageDialog(this,
                        "Debt updated successfully!\n" +
                        "Location: " + debtLocation + "\n" +
                        "New Balance: $" + String.format("%.2f", newBalance) + "\n" +
                        "New Interest Rate: " + newInterest + "%\n" +
                        "New Minimum Payment: $" + String.format("%.2f", newMinPayment),
                        "Update Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                
                refreshAll();
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers for all fields.\n" +
                        "All values must be non-negative.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error updating debt: " + e.getMessage(),
                        "Update Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onTraversalClicked() {
        // Show options for search or filter
        String[] options = {"Search Debt by Name", "Filter by Amount Range", "Filter by Interest Rate", "Show All Debts"};
        int choice = JOptionPane.showOptionDialog(this,
                "Choose traversal method:",
                "Debt Search & Filter",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        
        if (choice == -1) return; // User cancelled
        
        switch (choice) {
            case 0: // Search by name
                searchDebtByName();
                break;
            case 1: // Filter by amount range
                filterByAmountRange();
                break;
            case 2: // Filter by interest rate
                filterByInterestRate();
                break;
            case 3: // Show all debts
                showAllDebts();
                break;
        }
    }
    
    private void searchDebtByName() {
        String searchTerm = JOptionPane.showInputDialog(this,
                "Enter debt name to search (partial names allowed):",
                "Search Debt",
                JOptionPane.QUESTION_MESSAGE);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return;
        }
        
        searchTerm = searchTerm.trim().toLowerCase();
        ArrayList<Debt> allDebts = getAllDebts();
        ArrayList<Debt> foundDebts = new ArrayList<>();
        
        for (Debt debt : allDebts) {
            if (debt.getName().toLowerCase().contains(searchTerm)) {
                foundDebts.add(debt);
            }
        }
        
        if (foundDebts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No debts found matching: " + searchTerm,
                    "Search Results",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Display results
        StringBuilder result = new StringBuilder();
        result.append("Search Results for '").append(searchTerm).append("':\n");
        result.append("================================\n\n");
        result.append("Found ").append(foundDebts.size()).append(" debt(s):\n\n");
        
        for (int i = 0; i < foundDebts.size(); i++) {
            Debt debt = foundDebts.get(i);
            result.append(i + 1).append(". ").append(debt.getName()).append("\n");
            result.append("   Balance: $").append(String.format("%.2f", debt.getCurrentBalance())).append("\n");
            result.append("   Interest: ").append(debt.getInterestRate()).append("%\n");
            result.append("   Min Payment: $").append(String.format("%.2f", debt.getMinimumPayment())).append("\n");
            result.append("   Location: ").append(getDebtLocation(debt)).append("\n\n");
        }
        
        JTextArea textArea = new JTextArea(result.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this,
                scrollPane,
                "Search Results",
                JOptionPane.INFORMATION_MESSAGE);
        
        log("SEARCH: Searched for debt name containing '" + searchTerm + "' - Found " + foundDebts.size() + " result(s)");
    }
    
    private void filterByAmountRange() {
        JPanel filterPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        JLabel minLabel = new JLabel("Minimum Amount ($):");
        JTextField minField = new JTextField("0.00");
        
        JLabel maxLabel = new JLabel("Maximum Amount ($):");
        JTextField maxField = new JTextField("1000000.00");
        
        filterPanel.add(minLabel);
        filterPanel.add(minField);
        filterPanel.add(maxLabel);
        filterPanel.add(maxField);
        
        int result = JOptionPane.showConfirmDialog(this,
                filterPanel,
                "Filter by Amount Range",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                double minAmount = Double.parseDouble(minField.getText().trim());
                double maxAmount = Double.parseDouble(maxField.getText().trim());
                
                if (minAmount < 0 || maxAmount < 0 || minAmount > maxAmount) {
                    throw new NumberFormatException("Invalid range");
                }
                
                ArrayList<Debt> allDebts = getAllDebts();
                ArrayList<Debt> filteredDebts = new ArrayList<>();
                
                for (Debt debt : allDebts) {
                    double balance = debt.getCurrentBalance();
                    if (balance >= minAmount && balance <= maxAmount) {
                        filteredDebts.add(debt);
                    }
                }
                
                displayFilterResults(filteredDebts, 
                    "Amount Range: $" + String.format("%.2f", minAmount) + 
                    " to $" + String.format("%.2f", maxAmount));
                
                log("FILTER: Filtered debts by amount range $" + 
                    String.format("%.2f", minAmount) + " to $" + 
                    String.format("%.2f", maxAmount) + " - Found " + 
                    filteredDebts.size() + " result(s)");
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers for amount range.\n" +
                        "Minimum must be less than or equal to maximum.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void filterByInterestRate() {
        JPanel filterPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        JLabel minLabel = new JLabel("Minimum Interest Rate (%):");
        JTextField minField = new JTextField("0.0");
        
        JLabel maxLabel = new JLabel("Maximum Interest Rate (%):");
        JTextField maxField = new JTextField("100.0");
        
        filterPanel.add(minLabel);
        filterPanel.add(minField);
        filterPanel.add(maxLabel);
        filterPanel.add(maxField);
        
        int result = JOptionPane.showConfirmDialog(this,
                filterPanel,
                "Filter by Interest Rate",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                double minRate = Double.parseDouble(minField.getText().trim());
                double maxRate = Double.parseDouble(maxField.getText().trim());
                
                if (minRate < 0 || maxRate < 0 || minRate > maxRate) {
                    throw new NumberFormatException("Invalid range");
                }
                
                ArrayList<Debt> allDebts = getAllDebts();
                ArrayList<Debt> filteredDebts = new ArrayList<>();
                
                for (Debt debt : allDebts) {
                    double rate = debt.getInterestRate();
                    if (rate >= minRate && rate <= maxRate) {
                        filteredDebts.add(debt);
                    }
                }
                
                displayFilterResults(filteredDebts,
                    "Interest Rate Range: " + String.format("%.1f", minRate) + 
                    "% to " + String.format("%.1f", maxRate) + "%");
                
                log("FILTER: Filtered debts by interest rate " + 
                    String.format("%.1f", minRate) + "% to " + 
                    String.format("%.1f", maxRate) + "% - Found " + 
                    filteredDebts.size() + " result(s)");
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers for interest rate range.\n" +
                        "Minimum must be less than or equal to maximum.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showAllDebts() {
        ArrayList<Debt> allDebts = getAllDebts();
        
        if (allDebts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No debts found in any stack.",
                    "All Debts",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Group by location
        ArrayList<Debt> activeDebts = getDebtsForVisualization();
        ArrayList<Debt> auxiliaryDebtsList = auxiliaryDebts;
        ArrayList<Debt> paidOffDebtsList = paidOffDebts;
        
        StringBuilder result = new StringBuilder();
        result.append("ALL DEBTS - COMPLETE OVERVIEW\n");
        result.append("=============================\n\n");
        
        result.append("TOTAL DEBTS: ").append(allDebts.size()).append("\n\n");
        
        // Active Debts
        result.append("ACTIVE DEBTS (").append(activeDebts.size()).append("):\n");
        result.append("-----------------\n");
        if (activeDebts.isEmpty()) {
            result.append("  No active debts\n\n");
        } else {
            for (int i = 0; i < activeDebts.size(); i++) {
                Debt debt = activeDebts.get(i);
                result.append("  ").append(i + 1).append(". ").append(debt.getName()).append("\n");
                result.append("     Balance: $").append(String.format("%.2f", debt.getCurrentBalance())).append("\n");
                result.append("     Interest: ").append(debt.getInterestRate()).append("%\n");
                result.append("     Position: ").append(i == 0 ? "TOS (Newest)" : "#" + i + " from top").append("\n\n");
            }
        }
        
        // Auxiliary Debts
        result.append("AUXILIARY DEBTS (").append(auxiliaryDebtsList.size()).append("):\n");
        result.append("--------------------\n");
        if (auxiliaryDebtsList.isEmpty()) {
            result.append("  No auxiliary debts\n\n");
        } else {
            for (int i = 0; i < auxiliaryDebtsList.size(); i++) {
                Debt debt = auxiliaryDebtsList.get(i);
                result.append("  ").append(i + 1).append(". ").append(debt.getName()).append("\n");
                result.append("     Balance: $").append(String.format("%.2f", debt.getCurrentBalance())).append("\n");
                result.append("     Interest: ").append(debt.getInterestRate()).append("%\n\n");
            }
        }
        
        // Paid-Off Debts
        result.append("PAID-OFF DEBTS (").append(paidOffDebtsList.size()).append("):\n");
        result.append("-------------------\n");
        if (paidOffDebtsList.isEmpty()) {
            result.append("  No paid-off debts\n");
        } else {
            for (int i = 0; i < paidOffDebtsList.size(); i++) {
                Debt debt = paidOffDebtsList.get(i);
                result.append("  ").append(i + 1).append(". ").append(debt.getName()).append("\n");
                result.append("     Original: $").append(String.format("%.2f", debt.getOriginalAmount())).append("\n");
                result.append("     Status: PAID OFF\n\n");
            }
        }
        
        // Summary
        double totalActiveBalance = activeDebts.stream().mapToDouble(Debt::getCurrentBalance).sum();
        double totalAuxiliaryBalance = auxiliaryDebtsList.stream().mapToDouble(Debt::getCurrentBalance).sum();
        double totalOriginalPaid = paidOffDebtsList.stream().mapToDouble(Debt::getOriginalAmount).sum();
        
        result.append("\nSUMMARY:\n");
        result.append("--------\n");
        result.append("Total Active Balance: $").append(String.format("%.2f", totalActiveBalance)).append("\n");
        result.append("Total Auxiliary Balance: $").append(String.format("%.2f", totalAuxiliaryBalance)).append("\n");
        result.append("Total Paid Off Amount: $").append(String.format("%.2f", totalOriginalPaid)).append("\n");
        result.append("Grand Total (All Debts): $").append(String.format("%.2f", totalActiveBalance + totalAuxiliaryBalance + totalOriginalPaid)).append("\n");
        
        JTextArea textArea = new JTextArea(result.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 500));
        
        JOptionPane.showMessageDialog(this,
                scrollPane,
                "All Debts Overview",
                JOptionPane.INFORMATION_MESSAGE);
        
        log("TRAVERSAL: Viewed all debts - Total: " + allDebts.size());
    }
    
    private void displayFilterResults(ArrayList<Debt> filteredDebts, String filterCriteria) {
        if (filteredDebts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No debts found matching the filter criteria:\n" + filterCriteria,
                    "Filter Results",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        StringBuilder result = new StringBuilder();
        result.append("FILTER RESULTS\n");
        result.append("==============\n");
        result.append("Filter: ").append(filterCriteria).append("\n");
        result.append("Found ").append(filteredDebts.size()).append(" debt(s):\n\n");
        
        // Group by location for better organization
        Map<String, ArrayList<Debt>> groupedDebts = new LinkedHashMap<>();
        groupedDebts.put("Active Stack", new ArrayList<>());
        groupedDebts.put("Auxiliary Stack", new ArrayList<>());
        groupedDebts.put("Paid-Off Stack", new ArrayList<>());
        
        for (Debt debt : filteredDebts) {
            String location = getDebtLocation(debt);
            groupedDebts.get(location).add(debt);
        }
        
        for (Map.Entry<String, ArrayList<Debt>> entry : groupedDebts.entrySet()) {
            String location = entry.getKey();
            ArrayList<Debt> debts = entry.getValue();
            
            if (!debts.isEmpty()) {
                result.append(location).append(" (").append(debts.size()).append("):\n");
                result.append("-".repeat(location.length() + 4)).append("\n");
                
                for (int i = 0; i < debts.size(); i++) {
                    Debt debt = debts.get(i);
                    result.append("  ").append(i + 1).append(". ").append(debt.getName()).append("\n");
                    result.append("     Balance: $").append(String.format("%.2f", debt.getCurrentBalance())).append("\n");
                    result.append("     Interest: ").append(debt.getInterestRate()).append("%\n");
                    result.append("     Min Payment: $").append(String.format("%.2f", debt.getMinimumPayment())).append("\n");
                    
                    if (location.equals("Active Stack")) {
                        // Find position in active stack
                        ArrayList<Debt> activeDebts = getDebtsForVisualization();
                        int position = activeDebts.indexOf(debt);
                        if (position == 0) {
                            result.append("     Position: TOS (Top of Stack)\n");
                        } else if (position > 0) {
                            result.append("     Position: #").append(position).append(" from top\n");
                        }
                    }
                    result.append("\n");
                }
                result.append("\n");
            }
        }
        
        JTextArea textArea = new JTextArea(result.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(550, 450));
        
        JOptionPane.showMessageDialog(this,
                scrollPane,
                "Filter Results: " + filterCriteria,
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private ArrayList<Debt> getAllDebts() {
        ArrayList<Debt> allDebts = new ArrayList<>();
        allDebts.addAll(getDebtsForVisualization());
        allDebts.addAll(auxiliaryDebts);
        allDebts.addAll(paidOffDebts);
        return allDebts;
    }
    
    private String getDebtLocation(Debt debt) {
        ArrayList<Debt> activeDebts = getDebtsForVisualization();
        if (activeDebts.contains(debt)) {
            return "Active Stack";
        } else if (auxiliaryDebts.contains(debt)) {
            return "Auxiliary Stack";
        } else if (paidOffDebts.contains(debt)) {
            return "Paid-Off Stack";
        }
        return "Unknown";
    }

    private void onAuxiliary() {
        Debt top = manager.peekTOS();
        if (top != null) {
            // Ask for password verification
            JPasswordField passwordField = new JPasswordField(20);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Enter your password to move debt to auxiliary:"), BorderLayout.NORTH);
            panel.add(passwordField, BorderLayout.CENTER);
            
            int option = JOptionPane.showConfirmDialog(this,
                    panel,
                    "Password Verification",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            
            if (option == JOptionPane.OK_OPTION) {
                String enteredPassword = new String(passwordField.getPassword());
                User currentUser = controller.getCurrentUser();
                
                if (currentUser != null && enteredPassword.equals(currentUser.getPassword())) {
                    // Password correct, proceed with move
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Move " + top.getName() + " to auxiliary?\nBalance: $"
                                    + String.format("%.2f", top.getCurrentBalance()) + "\nPosition: TOS",
                            "Confirm Move to Auxiliary", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        Debt movedDebt = manager.popDebt();
                        // Add to beginning of ArrayList to maintain LIFO order (newest first)
                        auxiliaryDebts.add(0, movedDebt);
                        log("MOVED: " + movedDebt.getName() + " from TOS to auxiliary (Password verified)");

                        Debt newTOS = manager.peekTOS();
                        tosLabel.setText("Current TOS: " + (newTOS != null ? newTOS.getName() + " (Position: TOS)" : "None"));
                        refreshAll();
                        
                        JOptionPane.showMessageDialog(this,
                                "Successfully moved to auxiliary:\n" +
                                movedDebt.getName() + "\n" +
                                "Balance: $" + String.format("%.2f", movedDebt.getCurrentBalance()) + "\n" +
                                "New TOS: " + (newTOS != null ? newTOS.getName() : "None"),
                                "Moved to Auxiliary",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Incorrect password! Operation cancelled.",
                            "Password Error",
                            JOptionPane.ERROR_MESSAGE);
                    log("FAILED: Attempt to move debt to auxiliary - Incorrect password");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No active debt to move!\n" +
                    "Client debt stack is empty.",
                    "No TOS Available",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onSettleClicked() {
        makePayment();
    }

    private void onDeleteClicked() {
        Debt top = manager.peekTOS();
        if (top != null) {
            // Always require confirmation for delete
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to DELETE " + top.getName() + "?\n" +
                            "Balance: $" + String.format("%.2f", top.getCurrentBalance()) + "\n" +
                            "Position: TOS\n\n" +
                            "This action cannot be undone!",
                    "CONFIRM DELETE",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Double confirmation for important operations
                int finalConfirm = JOptionPane.showConfirmDialog(this,
                        "FINAL WARNING: This will permanently delete '" + top.getName() + "'\n" +
                                "Are you absolutely sure?",
                        "FINAL CONFIRMATION",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE);
                
                if (finalConfirm == JOptionPane.YES_OPTION) {
                    manager.popDebt();
                    log("DELETED: " + top.getName() + " from TOS (Confirmed twice)");
                    addEventToCalendar("Deleted debt: " + top.getName());

                    Debt newTOS = manager.peekTOS();
                    tosLabel.setText("Current TOS: " + (newTOS != null ? newTOS.getName() + " (Position: TOS)" : "None"));
                    refreshAll();
                    
                    JOptionPane.showMessageDialog(this,
                            "Debt successfully deleted:\n" +
                            top.getName() + "\n" +
                            "New TOS: " + (newTOS != null ? newTOS.getName() : "None"),
                            "Delete Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    log("CANCELLED: Delete operation cancelled at final confirmation");
                }
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
        ArrayList<Debt> activeDebtsList = getDebtsForVisualization();
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

    // Helper method to get debts in proper LIFO order for visualization
    private ArrayList<Debt> getDebtsForVisualization() {
        ArrayList<Debt> debts = (ArrayList<Debt>) manager.getStackForVisualization();
        if (debts.isEmpty()) {
            return new ArrayList<>();
        }

        // For LIFO visualization, we want newest (TOS) at index 0
        // The DebtManager returns oldest at index 0, newest at last index
        ArrayList<Debt> result = new ArrayList<>();

        // Reverse the order for visualization
        for (int i = debts.size() - 1; i >= 0; i--) {
            result.add(debts.get(i));
        }

        return result;
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
            ArrayList<Debt> debts = getDebtsForVisualization();
            if (debts.isEmpty()) {
                // Show empty message
                g2.setColor(Color.LIGHT_GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
                String msg = "Active Stack Empty";
                g2.drawString(msg, centerX - 50, baseY - 100);
                return;
            }

            int brickH = 40;
            int gap = 5;
            int maxVisible = 6; // Maximum number of debts to display

            // Calculate how many debts we can actually display
            int displayCount = Math.min(debts.size(), maxVisible);

            // Draw from top (TOS/newest) to bottom (oldest)
            for (int i = 0; i < displayCount; i++) {
                // Get debt from the list (index 0 is newest/TOS)
                Debt d = debts.get(i);

                // Calculate y position - TOS (i=0) at the top visually
                int yPos = baseY - (displayCount * (brickH + gap)) + (i * (brickH + gap));

                Color c;
                if (i == 0) // TOS - NEWEST DEBT (added most recently)
                    c = new Color(220, 53, 69); // Red
                else if (i == 1) // Second newest
                    c = new Color(253, 126, 20); // Orange
                else // Older debts
                    c = new Color(255, 193, 7); // Yellow

                g2.setColor(c);

                // Make TOS (newest) WIDER, older debts NARROWER
                int width = 250 - (i * 20); // i=0 (TOS/Newest): 250, i=1: 230, i=2: 210, etc.
                width = Math.max(width, 150); // Minimum width

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 15, 15);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));

                // Display debt name
                String name = d.getName();
                if (name.length() > 20)
                    name = name.substring(0, 17) + "...";
                g2.drawString(name, centerX - width / 2 + 10, yPos + 15);

                // Display balance
                String balance = "$" + (int) d.getCurrentBalance();
                g2.drawString(balance, centerX + width / 2 - 40, yPos + 15);

                if (i == 0) { // TOS indicator for NEWEST debt
                    g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                    g2.drawString("TOS (Newest)", centerX - width / 2 + 10, yPos + 28);

                    // Show "Added most recently"
                    g2.drawString("Added: Latest", centerX + width / 2 - 50, yPos + 28);
                } else {
                    // Show position and age indicator
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
                    String position = "Position #" + (i + 1);
                    g2.drawString(position, centerX - width / 2 + 10, yPos + 28);

                    // Show how old relative to TOS
                    String ageText = (i) + " below TOS";
                    g2.drawString(ageText, centerX + width / 2 - 40, yPos + 28);
                }
            }

            // If there are more debts than we can display, show a count
            if (debts.size() > maxVisible) {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 10));
                String moreText = "+ " + (debts.size() - maxVisible) + " older debts";
                g2.drawString(moreText, centerX - 40, baseY - 5);
            }

            // Draw stack direction indicator
            if (!debts.isEmpty()) {
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.drawString("↑ Newest (TOS)", centerX - 40, baseY - (displayCount * (brickH + gap)) - 10);
                g2.drawString("↓ Older", centerX - 25, baseY + 15);

                // Add a visual stack indicator
                g2.setColor(new Color(100, 100, 100, 100));
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int stackTopY = baseY - (displayCount * (brickH + gap)) - 20;
                int stackBottomY = baseY + 10;
                g2.drawLine(centerX, stackTopY, centerX, stackBottomY);
                g2.drawString("Stack", centerX - 15, stackTopY - 5);
            }
        }

        private void drawAuxiliaryStack(Graphics2D g2, int centerX, int baseY) {
            if (auxiliaryDebts.isEmpty()) {
                // Show empty message
                g2.setColor(Color.LIGHT_GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
                String msg = "Auxiliary Stack Empty";
                g2.drawString(msg, centerX - 60, baseY - 100);
                return;
            }

            int brickH = 35;
            int gap = 5;

            // Draw the auxiliary stack (also in LIFO order)
            for (int i = 0; i < Math.min(auxiliaryDebts.size(), 6); i++) {
                Debt d = auxiliaryDebts.get(i); // Index 0 is newest in auxiliary
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                Color c = new Color(150, 150, 200); // Blue-gray for auxiliary
                g2.setColor(c);
                int width = 180 + (i * 15); // Narrower at top (newer)

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));

                String name = d.getName();
                if (name.length() > 18)
                    name = name.substring(0, 15) + "...";
                g2.drawString(name, centerX - width / 2 + 5, yPos + 15);

                String balance = "$" + (int) d.getCurrentBalance();
                g2.drawString(balance, centerX + width / 2 - 30, yPos + 15);

                // Show "AUX" label for auxiliary debts
                g2.setFont(new Font("SansSerif", Font.BOLD, 8));
                if (i == 0) {
                    g2.drawString("AUX (Newest)", centerX - width / 2 + 5, yPos + 28);
                } else {
                    g2.drawString("AUX", centerX - width / 2 + 5, yPos + 28);
                }
            }

            // Show count if more debts than displayed
            if (auxiliaryDebts.size() > 6) {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 10));
                String moreText = "+ " + (auxiliaryDebts.size() - 6) + " more";
                g2.drawString(moreText, centerX - 30, baseY - 10);
            }
        }

        private void drawPaidOffStack(Graphics2D g2, int centerX, int baseY) {
            if (paidOffDebts.isEmpty()) {
                // Show empty message
                g2.setColor(Color.LIGHT_GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
                String msg = "Paid-off Stack Empty";
                g2.drawString(msg, centerX - 60, baseY - 100);
                return;
            }

            int brickH = 30;
            int gap = 5;

            // Draw the paid-off stack (also in LIFO order)
            for (int i = 0; i < Math.min(paidOffDebts.size(), 8); i++) {
                Debt d = paidOffDebts.get(i); // Index 0 is newest in paid-off
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                Color c = new Color(100, 200, 100); // Green for paid-off
                g2.setColor(c);
                int width = 160 + (i * 10); // Narrower at top (newer)

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 8));

                String name = d.getName();
                if (name.length() > 20)
                    name = name.substring(0, 17) + "...";
                g2.drawString(name, centerX - width / 2 + 5, yPos + 15);

                g2.setFont(new Font("SansSerif", Font.BOLD, 7));
                if (i == 0) {
                    g2.drawString("PAID (Latest)", centerX - width / 2 + 5, yPos + 25);
                } else {
                    g2.drawString("PAID", centerX - width / 2 + 5, yPos + 25);
                }

                String balance = "$" + (int) d.getCurrentBalance();
                g2.drawString(balance, centerX + width / 2 - 25, yPos + 15);
            }

            // Show count if more debts than displayed
            if (paidOffDebts.size() > 8) {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 10));
                String moreText = "+ " + (paidOffDebts.size() - 8) + " more";
                g2.drawString(moreText, centerX - 25, baseY - 10);
            }
        }
    }

}