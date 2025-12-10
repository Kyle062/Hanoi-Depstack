package Gui;

import Model.AppController;
import Model.Debt;
import Model.DebtManager;

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

    private final static AppController controller = new AppController();
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

    // Credit Card Labels - Need to update these dynamically
    private JLabel cardTitleLabel;
    private JLabel posLabel;
    private JLabel balValLabel;
    private JLabel intValLabel;
    private JLabel ogAmtLabel;
    private JLabel minPayLabel;
    private JProgressBar progressBar;
    private JLabel tosLabel; // Current TOS label in payment panel

    // Data lists for visualization
    private List<Debt> auxiliaryDebts = new ArrayList<>();
    private List<Debt> paidOffDebts = new ArrayList<>();

    // Date formatter for event calendar
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d");

    public UserDashboard(AppController controller2) {
        manager = controller.getManager();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Make it full screen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("Hanoi Debt Tower Dashboard");
        setLayout(new BorderLayout());

        initUI();
        refreshAll();
        setVisible(true);
    }

    private void initUI() {
        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // We need to wait for the frame to be visible to get correct sizes
        // But for now we use a default size to avoid errors
        setSize(1400, 900);

        setupBackground();

        // Main layer holds all the controls
        mainLayer = new JPanel(null);
        mainLayer.setOpaque(false);
        mainLayer.setBounds(0, 0, 1920, 1080); // Large bounds to fit everything
        layeredPane.add(mainLayer, JLayeredPane.PALETTE_LAYER);

        createSidebar();
        createTopRow();
        createBottomRow();
        createLogsPanel();

        // Add a component listener to resize background when window changes
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
            // Load your image
            BufferedImage bg = ImageIO.read(new File("Images/DashboardMainBackground.png"));
            // We just use the image as an icon, we will scale it in paint if needed
            // but for simplicity, let's just set it plain
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
        // Spacing between buttons
        sidebar.setLayout(new GridLayout(8, 1, 0, 25));
        sidebar.setOpaque(false);
        // Position on the far left
        sidebar.setBounds(20, 150, 100, 600);

        // Add buttons using the helper method
        sidebar.add(createIconButton("Consultation \nAppointment", "Consultation \nAppointment",
                e -> {
                    try {
                        new UserConsultation().setVisible(true);
                    } catch (Exception ex) {
                        // If UserConsultation doesn't exist yet, show a message
                        JOptionPane.showMessageDialog(this,
                                "Consultation feature is not available yet.",
                                "Information",
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    dispose();
                }));
        sidebar.add(createIconButton("PEEK", "View Top", e -> onPeekClicked()));
        sidebar.add(createIconButton("PAY", "Settle", e -> onSettleClicked()));
        sidebar.add(createIconButton("HISTORY", "History", e -> onHistoryClicked()));
        sidebar.add(createIconButton("DELETE", "Delete", e -> onDeleteClicked()));
        sidebar.add(createIconButton("PROFILE", "Profile", e -> onProfileClicked()));
        sidebar.add(createIconButton("AUXILIARY", "Move TOS to Auxiliary", e -> onAuxiliary()));
        mainLayer.add(sidebar);
    }

    private void createTopRow() {
        // 1. Tower Panel (Top Center/Left)
        towerContainer = new RoundedPanel(25, Color.WHITE);
        towerContainer.setLayout(null);
        // Position: x=110 (right of sidebar), y=30
        towerContainer.setBounds(150, 30, 1300, 530);

        TowerVisualizationPanel towerVis = new TowerVisualizationPanel();
        towerVis.setBounds(50, 60, 1200, 400);
        towerContainer.add(towerVis);
        mainLayer.add(towerContainer);

        // 2. Add Debt Panel (Top Right)
        addDebtPanel = createAddDebtPanel();
        // Position: right of tower
        addDebtPanel.setBounds(1500, 30, 350, 530);
        mainLayer.add(addDebtPanel);
    }

    private void createBottomRow() {
        int startY = 580; // Y position for the bottom row
        int height = 280; // Height of bottom panels

        // 1. Credit Card Panel (Bottom Left)
        cardPanel = createCardPanel();
        cardPanel.setBounds(150, startY, 700, height);
        mainLayer.add(cardPanel);

        // 2. Make Payment Panel (Bottom Middle)
        paymentPanel = createPaymentPanel();
        paymentPanel.setBounds(870, startY, 500, height);
        mainLayer.add(paymentPanel);

        // 3. Event Calendar (Bottom Right) - NEW
        calendarPanel = createEventCalendarPanel();
        calendarPanel.setBounds(1400, startY, 500, height);
        mainLayer.add(calendarPanel);
    }

    // --- Panel Creators ---

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
        cardTitleLabel = new JLabel("Credit Card");
        cardTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        cardTitleLabel.setBounds(20, 20, 200, 25);
        panel.add(cardTitleLabel);

        // Position label
        posLabel = new JLabel("Position: 1");
        posLabel.setForeground(Color.GRAY);
        posLabel.setBounds(20, 45, 100, 20);
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
        intValLabel.setForeground(new Color(234, 88, 12)); // Orange text
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

    private void updateCreditCardPanel() {
        try {
            List<Debt> activeDebts = manager.getStackForVisualization();
            if (activeDebts != null && !activeDebts.isEmpty()) {
                // The first element in the list is the TOS (top of stack)
                Debt topDebt = activeDebts.get(0);

                // Update card title
                cardTitleLabel.setText(topDebt.getName());

                // Update position (always 1 for TOS)
                posLabel.setText("Position: 1 (TOS)");

                // Update current balance
                balValLabel.setText(String.format("$%,.2f", topDebt.getCurrentBalance()));

                // Update interest rate - FIXED: Handle any type of interest rate
                try {
                    // Get interest rate as a number
                    Object rateObj = topDebt.getInterestRate();
                    if (rateObj instanceof Number) {
                        double interestRate = ((Number) rateObj).doubleValue();
                        intValLabel.setText(String.format("%.1f%%", interestRate));
                    } else {
                        // If it's not a number, just convert to string
                        intValLabel.setText(rateObj.toString() + "%");
                    }
                } catch (Exception e) {
                    // If there's any error, just show it as-is
                    intValLabel.setText("Error");
                }

                // Update original amount
                ogAmtLabel.setText(String.format("Original Amount: $%,.2f", topDebt.getOriginalAmount()));

                // Update minimum payment
                minPayLabel.setText(String.format("Min Payment: $%,.2f", topDebt.getMinimumPayment()));

                // Update progress bar (percentage paid)
                double progress = ((topDebt.getOriginalAmount() - topDebt.getCurrentBalance())
                        / topDebt.getOriginalAmount()) * 100;
                progressBar.setValue((int) Math.min(100, Math.max(0, progress)));
            } else {
                // No active debts - show defaults
                cardTitleLabel.setText("No Active Debt");
                posLabel.setText("Position: N/A");
                balValLabel.setText("$0.00");
                intValLabel.setText("0.0%");
                ogAmtLabel.setText("Original Amount: $0.00");
                minPayLabel.setText("Min Payment: $0.00");
                progressBar.setValue(0);
            }
        } catch (Exception e) {
            // Ignore any errors in updating the credit card panel
            // The application should continue working even if this panel fails
        }
    }

    private JPanel createEventCalendarPanel() {
        RoundedPanel panel = new RoundedPanel(25, Color.WHITE);
        panel.setLayout(null);

        JLabel title = new JLabel("Event Calendar");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBounds(20, 20, 200, 30);
        panel.add(title);

        // Use a text area for the list of events to keep it simple
        calendarEventsArea = new JTextArea();
        calendarEventsArea.setText("");
        calendarEventsArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        calendarEventsArea.setEditable(false);
        calendarEventsArea.setLineWrap(true);
        calendarEventsArea.setWrapStyleWord(true);
        calendarEventsArea.setOpaque(false); // See through to panel

        JScrollPane scrollPane = new JScrollPane(calendarEventsArea);
        scrollPane.setBounds(20, 60, 310, 200);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane);

        return panel;
    }

    private void addEventToCalendar(String event) {
        // Get current date
        Date now = new Date();
        String dateStr = dateFormat.format(now);

        // Add the event with timestamp
        calendarEventsArea.append(dateStr + ": " + event + "\n");

        // Scroll to bottom
        calendarEventsArea.setCaretPosition(calendarEventsArea.getDocument().getLength());
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

    // --- Helper Methods for Styling ---

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
        btn.setBackground(new Color(234, 88, 12)); // The exact orange from the image
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

    private JButton createIconButton(String text, String tooltip, ActionListener action) {
        // White square button with text
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("SansSerif", Font.BOLD, 9));
        // Make it look like a rounded icon box
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }

    // --- Custom Panels ---

    /**
     * A simple helper class to draw a white panel with rounded corners
     */
    class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false); // Important for round corners
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

    /**
     * The Tower Visualization
     */
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

            // Title
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 24));
            String title = "THE HANOI DEBT TOWER";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, (w - fm.stringWidth(title)) / 2, 40);

            // Draw Base Line (purple gradient in image, we'll do solid purple)
            int baseY = h - 50;
            g2.setColor(new Color(100, 40, 100)); // Dark purple
            g2.fillRoundRect(50, baseY, w - 100, 15, 10, 10);

            // Columns
            String[] labels = { "ACTIVE DEBT", "AUXILIARY", "PAID-OFF" };
            int colW = w / 3;

            // Draw Pillars (vertical lines)
            g2.setColor(new Color(120, 40, 120)); // Purple pillar color
            for (int i = 0; i < 3; i++) {
                int cx = colW * i + colW / 2;
                // Draw the vertical stick
                g2.fillRoundRect(cx - 5, 80, 10, baseY - 80, 10, 10);

                // Draw Label
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                String lbl = labels[i];
                g2.drawString(lbl, cx - fm.stringWidth(lbl) / 2 + 30, baseY + 40);
                g2.setColor(new Color(120, 40, 120)); // Reset for next pillar
            }

            // Draw Debts
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

            // Draw stack properly: TOS (first element) at the top, newer items on top
            // In a stack, index 0 is the TOS, so we draw them in order from top to bottom
            
            int totalDebts = Math.min(debts.size(), 6); // Limit to 6 for visualization
            
            for (int i = 0; i < totalDebts; i++) {
                Debt d = debts.get(i);
                
                // Calculate Y position: TOS at the top (lowest Y), older debts below
                int yPos = baseY - gap - brickH - (i * (brickH + gap));
                
                // Color logic based on position in stack
                Color c;
                if (i == 0)
                    c = new Color(50, 200, 50); // Green (Top/Active - TOS)
                else if (i == 1)
                    c = new Color(255, 200, 50); // Yellow (Second in stack)
                else
                    c = new Color(220, 100, 50); // Orange/Red (Lower in stack)

                // If paid off column - use gray
                if (centerX > getWidth() / 2 + 100)
                    c = new Color(100, 100, 100);

                g2.setColor(c);
                
                // Make debts wider as they go down (pyramid style)
                // Bottom debts (higher index) should be wider
                int width = 250 + ((totalDebts - i - 1) * 20);

                // Draw the debt "brick"
                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 15, 15);

                // Add debt info text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                String info = d.getName() + " $" + (int) d.getCurrentBalance();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(info, centerX - fm.stringWidth(info) / 2, yPos + 25);
            }
        }
    }

    // --- Action Methods ---

    private void pushNewDebt() {
        try {
            String name = nameField.getText().trim();
            String amountText = amountField.getText().trim();
            String intText = intField.getText().trim();
            String minText = minField.getText().trim();
            String dueDate = dueDateField.getText().trim();

            // Simple validation
            if (name.isEmpty() || amountText.isEmpty() || intText.isEmpty() || minText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields");
                return;
            }

            // Parse values with simple error handling
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

            // Validate minimum values
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

            // Create and add the debt
            Debt newDebt = new Debt(name, amt, ir, min);
            controller.getManager().pushDebt(newDebt);

            // Debug: Print stack order
            printStackOrder();

            log("PUSH: Added " + name + " ($" + String.format("%.2f", amt) + ")");

            // Add to event calendar if due date is provided
            if (!dueDate.isEmpty()) {
                addEventToCalendar("Due for " + name + " on " + dueDate + " - Payment: $" + String.format("%.2f", min));
            } else {
                addEventToCalendar("Added new debt: " + name + " ($" + String.format("%.2f", amt) + ")");
            }

            // Clear fields
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

                // Check if payment exceeds balance
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
                log("PAID: $" + String.format("%.2f", amt) + " to " + top.getName());

                // Check if debt is paid off
                if (top.isPaidOff()) {
                    // Move to paid-off debts
                    Debt paidOffDebt = controller.getManager().popDebt();
                    paidOffDebts.add(0, paidOffDebt); // Add to beginning (top of paid-off stack)
                    log("COMPLETED: " + top.getName() + " is now paid off!");

                    // Add to event calendar
                    addEventToCalendar(top.getName() + " has been successfully paid off!");

                    // Check if there's a new TOS and move it from auxiliary if needed
                    if (controller.getManager().peekTOS() == null && !auxiliaryDebts.isEmpty()) {
                        // Move the top debt from auxiliary back to active (LIFO)
                        Debt newActive = auxiliaryDebts.remove(0);
                        controller.getManager().pushDebt(newActive);
                        log("MOVED: " + newActive.getName() + " from auxiliary to active (LIFO)");
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
        // Update TOS label in payment panel
        tosLabel.setText("Current TOS: " + getTopName());

        // Update credit card panel
        updateCreditCardPanel();

        // Repaint everything
        towerContainer.repaint();
        mainLayer.revalidate();
        mainLayer.repaint();
    }

    private String getTopName() {
        Debt d = controller.getManager().peekTOS();
        return d != null ? d.getName() : "None";
    }

    // Helper method to debug stack order
    private void printStackOrder() {
        System.out.println("=== Current Stack Order (TOS First) ===");
        List<Debt> debts = manager.getStackForVisualization();
        if (debts != null) {
            for (int i = 0; i < debts.size(); i++) {
                System.out.println((i + 1) + ". " + debts.get(i).getName() + 
                    " - Balance: $" + String.format("%.2f", debts.get(i).getCurrentBalance()));
            }
        } else {
            System.out.println("No active debts");
        }
        System.out.println("=========================");
    }

    // Placeholder actions for sidebar buttons
    private void onAddClicked() {
        JOptionPane.showMessageDialog(this, "Use panel on right");
    }

    private void onPeekClicked() {
        Debt d = controller.getManager().peekTOS();
        if (d != null) {
            JOptionPane.showMessageDialog(this,
                    "Top of Stack Details:\n" +
                            "Name: " + d.getName() + "\n" +
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
                            + String.format("%.2f", top.getCurrentBalance()),
                    "Confirm Move to Auxiliary",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Move TOS to auxiliary - pop from main stack
                Debt movedDebt = controller.getManager().popDebt();
                
                // Add to beginning of auxiliary list (top of auxiliary stack)
                auxiliaryDebts.add(0, movedDebt);
                
                log("MOVED: " + movedDebt.getName() + " from active to auxiliary (LIFO)");
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
                            "Balance: $" + String.format("%.2f", top.getCurrentBalance()),
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                controller.getManager().popDebt();
                log("DELETED: " + top.getName() + " (Balance: $" + String.format("%.2f", top.getCurrentBalance())
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
        // Show user profile information
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
                        "Active Debts: " + activeDebts + "\n" +
                        "Total Active Balance: $" + String.format("%.2f", totalBalance) + "\n" +
                        "Auxiliary Debts: " + auxiliaryDebts.size() + "\n" +
                        "Paid-off Debts: " + paidOffDebts.size() + "\n" +
                        "Stack Order (LIFO): Newest debt is TOS",
                "Profile Summary",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserDashboard(controller));
    }
}