package Gui;

import Model.AppController;
import Model.Debt;
import Model.DebtManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hanoi DebtStack Dashboard - Complete redesign to match reference image
 */
public class UserDashboard extends JFrame {

    private final static AppController controller = new AppController();
    private DebtManager manager;

    // UI components
    private JLayeredPane layeredPane;
    private JPanel mainLayer;
    private JLabel backgroundLabel;
    private JPanel towerPanel;
    private JPanel cardPanel;
    private JTextArea logsArea;
    private JScrollPane logsScrollPane;
    private List<Debt> auxiliaryDebts = new ArrayList<>();
    private List<Debt> paidOffDebts = new ArrayList<>();

    // Right panel components
    private JTextField nameField, amountField, intField, minField, payField;

    public UserDashboard(AppController controller2) {
        manager = controller.getManager();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        initUI();
        refreshAll();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initUI() {
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(getSize());
        add(layeredPane, BorderLayout.CENTER);
        setupBackground();

        // Create main layer for all UI components
        mainLayer = new JPanel(null);
        mainLayer.setOpaque(false);
        mainLayer.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(mainLayer, JLayeredPane.PALETTE_LAYER);

        createSidebar();

        createTopBar();

        // Create right control panel
        createRightControlPanel();

        // Create center area with tower and card
        createCenterArea();

        // Create logs panel
        createLogsPanel();
    }

    private void setupBackground() {
        try {
            BufferedImage bg = ImageIO.read(new File("Images/DashboardMainBackground.png"));
            Image scaled = bg.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
            backgroundLabel = new JLabel(new ImageIcon(scaled));
        } catch (IOException e) {
            backgroundLabel = new JLabel();
            backgroundLabel.setOpaque(true);
            backgroundLabel.setBackground(new Color(240, 240, 245));
        }
        backgroundLabel.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);
    }

    private void createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(8, 1, 10, 15));
        sidebar.setOpaque(false);
        sidebar.setBounds(20, 150, 80, 500);

        // Add buttons
        sidebar.add(createIconButton("ADD", "Add New Debt", e -> onAddClicked()));
        sidebar.add(createIconButton("PEEK", "View Top Debt", e -> onPeekClicked()));
        sidebar.add(createIconButton("PAY", "Make Payment", e -> onSettleClicked()));
        sidebar.add(createIconButton("DEL", "Delete Debt", e -> onDeleteClicked()));
        sidebar.add(createIconButton("HIST", "View History", e -> onHistoryClicked()));
        sidebar.add(createIconButton("PRO", "Profile", e -> onProfileClicked()));

        mainLayer.add(sidebar);
    }

    private JButton createIconButton(String text, String tooltip, ActionListener action) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw rounded background
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Draw border
                g2.setColor(new Color(200, 200, 220));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);

                // Draw text
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(text, x, y);

                g2.dispose();
            }
        };

        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(70, 70));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(action);

        return button;
    }

    private void createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBounds(125, 30, 1300, 50);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        JTextField searchField = new JTextField("Search here...", 1300);
        searchField.setPreferredSize(new Dimension(1300, 50));
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(searchField);
        topBar.add(searchPanel, BorderLayout.WEST);

        mainLayer.add(topBar);
    }

    private void createRightControlPanel() {
        JPanel rightPanel = new JPanel(null);
        rightPanel.setOpaque(false);
        int panelWidth = 450;
        rightPanel.setBounds(getWidth() - panelWidth - 30, 0, panelWidth, 2000);

        // Add New Debt Section
        JPanel addDebtPanel = createAddDebtPanel();
        addDebtPanel.setBounds(0, 35, panelWidth, 550);
        rightPanel.add(addDebtPanel);

        // Make Payment Section
        JPanel paymentPanel = createPaymentPanel();
        paymentPanel.setBounds(0, 620, panelWidth, 300);
        rightPanel.add(paymentPanel);

        mainLayer.add(rightPanel);
    }

    private JPanel createAddDebtPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(255, 255, 255, 230));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 200), 2),
                new EmptyBorder(15, 15, 15, 15)));

        // Title
        JLabel title = new JLabel("Add New Debt");
        title.setFont(new Font("Sans", Font.BOLD, 25));
        title.setBounds(20, 40, 200, 25); // ADJUST: Title position and size
        panel.add(title);

        // Starting position for first label
        int yLabel = 100; // Y position for first label
        int xLabel = 20; // X position for all labels
        int labelWidth = 300; // Width for all labels
        int labelHeight = 28; // Height for all labels
        int labelFontSize = 16; // Font size for all labels

        // Field positions (below labels)
        int xField = 20; // X position for all fields (same as labels)
        int fieldWidth = 400; // Width for all fields
        int fieldHeight = 35; // Height for all fields
        int verticalSpacing = 80; // Vertical spacing between each label+field pair

        // Debt Name - Label
        JLabel nameLabel = new JLabel("Debt Name");
        nameLabel.setBounds(xLabel, yLabel - 20, labelWidth, labelHeight); // ADJUST: Label position
        nameLabel.setFont(new Font("Arial", Font.BOLD, labelFontSize)); // ADJUST: Label font
        panel.add(nameLabel);

        // Debt Name - Field
        nameField = new JTextField("Credit Card, Loan, etc.");
        nameField.setBounds(xField, yLabel + 15, fieldWidth, fieldHeight); // ADJUST: Field position (below label)
        nameField.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(nameField);

        // Total Amount - Label
        yLabel += verticalSpacing; // Move down for next label
        JLabel amountLabel = new JLabel("Total Amount ($)");
        amountLabel.setBounds(xLabel, yLabel - 20, labelWidth, labelHeight); // ADJUST: Consistent label position
        amountLabel.setFont(new Font("Arial", Font.BOLD, labelFontSize)); // ADJUST: Consistent label font
        panel.add(amountLabel);

        // Total Amount - Field
        amountField = new JTextField("5000.00");
        amountField.setBounds(xField, yLabel + 15, fieldWidth, fieldHeight); // ADJUST: Consistent field position
        amountField.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(amountField);

        // Interest Rate - Label
        yLabel += verticalSpacing; // Move down for next label
        JLabel intLabel = new JLabel("Interest Rate (%)");
        intLabel.setBounds(xLabel, yLabel - 20, labelWidth, labelHeight); // ADJUST: Consistent label position
        intLabel.setFont(new Font("Arial", Font.BOLD, labelFontSize)); // ADJUST: Consistent label font
        panel.add(intLabel);

        // Interest Rate - Field
        intField = new JTextField("15.50");
        intField.setBounds(xField, yLabel + 15, fieldWidth, fieldHeight); // ADJUST: Consistent field position
        intField.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(intField);

        // Minimum Payment - Label
        yLabel += verticalSpacing; // Move down for next label
        JLabel minLabel = new JLabel("Min Payment ($)");
        minLabel.setBounds(xLabel, yLabel - 20, labelWidth, labelHeight); // ADJUST: Consistent label position
        minLabel.setFont(new Font("Arial", Font.BOLD, labelFontSize)); // ADJUST: Consistent label font
        panel.add(minLabel);

        // Minimum Payment - Field
        minField = new JTextField("100.00");
        minField.setBounds(xField, yLabel + 15, fieldWidth, fieldHeight); // ADJUST: Consistent field position
        minField.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(minField);

        // Strategy Label - Same Y position as minField label
        yLabel += verticalSpacing; // Move down for strategy section
        JLabel strategyLabel = new JLabel("Strategy");
        strategyLabel.setBounds(xLabel, yLabel - 20, labelWidth, labelHeight); // ADJUST: Same position as other labels
        strategyLabel.setFont(new Font("Arial", Font.BOLD, labelFontSize)); // ADJUST: Same font as other labels
        panel.add(strategyLabel);

        // Strategy Radio Buttons - Same Y position as minField
        int radioY = yLabel + 15; // Same calculation as field positions

        // Create button group for radio buttons
        ButtonGroup strategyGroup = new ButtonGroup();

        // Avalanche Radio Button
        JRadioButton avalancheRadio = new JRadioButton("Avalanche", true); // Default selected
        avalancheRadio.setBounds(xField, radioY, 120, 25); // ADJUST: Radio button position and size
        avalancheRadio.setFont(new Font("Arial", Font.PLAIN, 14)); // ADJUST: Radio button font size
        avalancheRadio.setOpaque(false);
        strategyGroup.add(avalancheRadio);
        panel.add(avalancheRadio);

        // Snowball Radio Button
        JRadioButton snowballRadio = new JRadioButton("Snowball");
        snowballRadio.setBounds(xField + 130, radioY, 120, 25); // ADJUST: Position next to avalanche
        snowballRadio.setFont(new Font("Arial", Font.PLAIN, 14)); // ADJUST: Same font size
        snowballRadio.setOpaque(false);
        strategyGroup.add(snowballRadio);
        panel.add(snowballRadio);

        // Push Button - Position below strategy radio buttons
        int buttonY = radioY + 40; // Below radio buttons with some spacing
        JButton pushButton = new JButton("PUSH TO STACK");
        pushButton.setBounds(xField, buttonY, fieldWidth, 45); // ADJUST: Button uses same width as fields
        pushButton.setFont(new Font("Arial", Font.BOLD, 16)); // ADJUST: Button font size
        pushButton.setBackground(new Color(234, 88, 12));
        pushButton.setForeground(Color.WHITE);
        pushButton.setFocusPainted(false);
        pushButton.setOpaque(true); // ADD THIS: Makes background fully visible
        pushButton.setBorderPainted(false); // ADD THIS: Removes default border
        pushButton.addActionListener(e -> pushNewDebt());
        panel.add(pushButton);

        // ADJUST: Set panel size to fit all components
        panel.setPreferredSize(new Dimension(fieldWidth + 40, buttonY + 60));

        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(255, 255, 255, 230));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 200), 2),
                new EmptyBorder(15, 15, 15, 15)));

        // Title
        JLabel title = new JLabel("Make Payment");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBounds(10, 10, 200, 25);
        panel.add(title);

        // Current TOS
        JLabel tosLabel = new JLabel("Current TOS: " + getTopName());
        tosLabel.setBounds(10, 45, 300, 25);
        panel.add(tosLabel);

        // Payment Amount
        JLabel payLabel = new JLabel("Payment Amount ($):");
        payLabel.setBounds(10, 85, 120, 25);
        panel.add(payLabel);
        payField = new JTextField("0.00");
        payField.setBounds(140, 85, 170, 30);
        panel.add(payField);

        // Min/Full buttons
        JPanel minFullPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        minFullPanel.setOpaque(false);
        minFullPanel.setBounds(10, 125, 300, 30);

        JButton minButton = new JButton("Min ()");
        minButton.setFont(new Font("Arial", Font.PLAIN, 12));
        minButton.setPreferredSize(new Dimension(100, 25));
        minButton.addActionListener(e -> payField.setText("150.00"));

        JButton fullButton = new JButton("Full (.00)");
        fullButton.setFont(new Font("Arial", Font.PLAIN, 12));
        fullButton.setPreferredSize(new Dimension(120, 25));
        fullButton.addActionListener(e -> payField.setText("5000.00"));

        minFullPanel.add(minButton);
        minFullPanel.add(fullButton);
        panel.add(minFullPanel);

        // Settle Payment Button
        JButton settleButton = new JButton("SETTLE PAYMENT");
        settleButton.setBounds(10, 165, 300, 35);
        settleButton.setFont(new Font("Arial", Font.BOLD, 14));
        settleButton.setBackground(new Color(80, 160, 100));
        settleButton.setForeground(Color.WHITE);
        settleButton.setFocusPainted(false);
        settleButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        settleButton.addActionListener(e -> makePayment());
        panel.add(settleButton);

        return panel;
    }

    private void createCenterArea() {
        // Declare centerWidth first
        int centerWidth = getWidth() - 540;

        JPanel centerPanel = new JPanel(null);
        centerPanel.setOpaque(false);

        // Tower Panel
        towerPanel = new TowerVisualizationPanel();
        int towerHeight = 450; // Tower height
        towerPanel.setBounds(0, 0, centerWidth, towerHeight);
        centerPanel.add(towerPanel);

        // Card Panel
        cardPanel = createCardPanel();
        int cardHeight = 200; // Card height
        int cardY = towerHeight + 20; // 20px spacing between tower and card
        cardPanel.setBounds(10, cardY, 6000, cardHeight);
        centerPanel.add(cardPanel);

        // Calculate total height needed
        int totalHeight = cardY + cardHeight + 1000; // Add 20px bottom padding

        // Set centerPanel bounds with calculated height
        centerPanel.setBounds(120, 100, centerWidth + 3000, totalHeight);

        mainLayer.add(centerPanel);
    }

    private class TowerVisualizationPanel extends JPanel {
        private final Color pillarColor = new Color(80, 80, 80);
        private final Color baseColor = new Color(150, 150, 150);
        private final Color activeBrickColor = new Color(220, 80, 80);
        private final Color auxiliaryBrickColor = new Color(80, 140, 220);
        private final Color paidBrickColor = new Color(80, 180, 100);

        public TowerVisualizationPanel() {
            setOpaque(false);

        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // ADD THIS: Draw white background panel for the tower
            g2.setColor(new Color(255, 255, 255, 240)); // Semi-transparent white
            g2.fillRect(10, 10, width - 80, height - 0);

            // Draw title - adjust Y position since we added padding
            g2.setFont(new Font("Arial", Font.BOLD, 28));
            g2.setColor(new Color(50, 50, 80));
            String title = "THE HANOI DEBT TOWER";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, (width - fm.stringWidth(title)) / 2, 50); // Changed from 40 to 50

            // Draw columns
            int columnWidth = width / 3;
            int pillarHeight = 250;
            int baseY = height - 70; // Changed from -50 to -70 to account for padding

            // Draw base line
            g2.setColor(baseColor);
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(50, baseY, width - 50, baseY);

            // Draw pillars and debts for each column
            String[] columnNames = { "ACTIVE DEBT", "AUXILIARY", "PAID-OFF" };
            Color[] columnColors = { activeBrickColor, auxiliaryBrickColor, paidBrickColor };
            List<Debt>[] debtLists = new List[] { manager.getStackForVisualization(), auxiliaryDebts, paidOffDebts
            };

            for (int i = 0; i < 3; i++) {
                int pillarX = columnWidth * i + columnWidth / 2;

                // Draw pillar
                g2.setColor(pillarColor);
                g2.fillRect(pillarX - 10, baseY - pillarHeight, 20, pillarHeight);

                // Draw column label
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.setColor(Color.BLACK);
                int labelWidth = fm.stringWidth(columnNames[i]);
                g2.drawString(columnNames[i], pillarX - labelWidth / 2, baseY - pillarHeight - 25);

                // Draw debts on this pillar
                drawDebtsOnPillar(g2, debtLists[i], pillarX, baseY, columnColors[i]);
            }

            g2.dispose();
        }

        private void drawDebtsOnPillar(Graphics2D g2, List<Debt> debts, int centerX, int baseY, Color color) {
            if (debts == null || debts.isEmpty()) {
                g2.setFont(new Font("Arial", Font.ITALIC, 12));
                g2.setColor(Color.GRAY);
                g2.drawString("Empty", centerX - 20, baseY - 100);
                return;
            }

            // Draw up to 5 debts
            int maxDebts = Math.min(debts.size(), 3);
            int brickHeight = 30;
            int brickSpacing = 5;
            int currentY = baseY - brickSpacing;

            for (int i = 0; i < maxDebts; i++) {
                Debt debt = debts.get(i);

                // Calculate brick width based on balance (relative to max balance)
                double maxBalance = 10000; // Example max for scaling
                double balanceRatio = Math.min(debt.getCurrentBalance() / maxBalance, 1.0);
                int brickWidth = 80 + (int) (120 * balanceRatio);

                // Draw brick
                int brickX = centerX - brickWidth / 2;
                int brickY = currentY - brickHeight;

                GradientPaint gp = new GradientPaint(
                        brickX, brickY, color.brighter(),
                        brickX, brickY + brickHeight, color.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(brickX, brickY, brickWidth, brickHeight, 8, 8);

                // Brick border
                g2.setColor(color.darker().darker());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(brickX, brickY, brickWidth, brickHeight, 8, 8);

                // Debt info
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                g2.setColor(Color.WHITE);

                // Name (truncated if needed)
                String name = debt.getName();
                FontMetrics fm = g2.getFontMetrics();
                if (fm.stringWidth(name) > brickWidth - 10) {
                    name = name.substring(0, Math.min(6, name.length())) + "...";
                }
                g2.drawString(name, brickX + 5, brickY + brickHeight / 2 - 3);

                // Balance
                String balance = String.format("$%.0f", debt.getCurrentBalance());
                int balanceWidth = fm.stringWidth(balance);
                g2.drawString(balance, brickX + brickWidth - balanceWidth - 5, brickY + brickHeight / 2 + 10);

                currentY = brickY - brickSpacing;
            }

            // Show count if more debts
            if (debts.size() > 5) {
                g2.setFont(new Font("Arial", Font.PLAIN, 10));
                g2.setColor(Color.DARK_GRAY);
                String extra = "+" + (debts.size() - 5) + " more";
                g2.drawString(extra, centerX - 15, currentY - 5);
            }
        }
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel(null);
        panel.setPreferredSize(new Dimension(3000, 3000));
        panel.setOpaque(false);

        JPanel card = new JPanel(null);
        card.setBackground(new Color(255, 255, 255, 240));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 220), 2),
                new EmptyBorder(20, 20, 20, 20)));
        card.setBounds(0, 0, 1300, 300);

        // Title with close button
        JLabel title = new JLabel("Credit Card Details");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBounds(10, 10, 300, 30);
        card.add(title);

        JLabel position = new JLabel("Position: 1");
        position.setFont(new Font("Arial", Font.PLAIN, 14));
        position.setBounds(10, 45, 100, 25);
        card.add(position);

        // Balance section
        JLabel balanceLabel = new JLabel("Current Balance");
        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        balanceLabel.setBounds(10, 80, 120, 25);
        card.add(balanceLabel);

        JLabel balanceValue = new JLabel(",000.00");
        balanceValue.setFont(new Font("Arial", Font.BOLD, 24));
        balanceValue.setForeground(new Color(0, 100, 200));
        balanceValue.setBounds(10, 105, 150, 30);
        card.add(balanceValue);

        // Interest rate section
        JLabel interestLabel = new JLabel("Interest Rate");
        interestLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        interestLabel.setBounds(200, 80, 100, 25);
        card.add(interestLabel);

        JLabel interestValue = new JLabel("18.5%");
        interestValue.setFont(new Font("Arial", Font.BOLD, 24));
        interestValue.setForeground(new Color(220, 50, 50));
        interestValue.setBounds(200, 105, 100, 30);
        card.add(interestValue);

        // Progress section
        JLabel progressLabel = new JLabel("Progress");
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        progressLabel.setBounds(10, 150, 100, 25);
        card.add(progressLabel);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setBounds(10, 175, 250, 20);
        progressBar.setStringPainted(true);
        progressBar.setString("0.0%");
        progressBar.setForeground(new Color(80, 160, 100));
        card.add(progressBar);

        // Original amount
        JLabel originalLabel = new JLabel("Original Amount: ,000");
        originalLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        originalLabel.setForeground(Color.DARK_GRAY);
        originalLabel.setBounds(300, 150, 180, 25);
        card.add(originalLabel);

        // Minimum payment
        JLabel minPaymentLabel = new JLabel("Min. Payment: ");
        minPaymentLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        minPaymentLabel.setForeground(Color.DARK_GRAY);
        minPaymentLabel.setBounds(300, 175, 180, 25);
        card.add(minPaymentLabel);

        panel.add(card);
        return panel;
    }

    private void createLogsPanel() {
        logsArea = new JTextArea();
        logsArea.setEditable(false);
        logsArea.setRows(8);
        logsArea.setBackground(new Color(30, 30, 40, 220));
        logsArea.setForeground(Color.WHITE);
        logsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logsArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        logsScrollPane = new JScrollPane(logsArea);
        logsScrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100)));
        logsScrollPane.setBounds(130, 800, getWidth() - 620, 200);
        logsScrollPane.getVerticalScrollBar().setUnitIncrement(18);

        mainLayer.add(logsScrollPane);
    }

    // Action methods
    private void pushNewDebt() {
        String name = nameField.getText().trim();
        String amountStr = amountField.getText().trim();
        String intStr = intField.getText().trim();
        String minStr = minField.getText().trim();

        if (name.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter debt name and amount.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            double interest = intStr.isEmpty() ? 0.0 : Double.parseDouble(intStr);
            double minPayment = minStr.isEmpty() ? 0.0 : Double.parseDouble(minStr);

            Debt newDebt = new Debt(name, amount, interest, minPayment);
            controller.getManager().pushDebt(newDebt);

            log("PUSH: Added new debt - " + name + " ($" + String.format("%.2f", amount) + ")");
            refreshAll();

            // Clear fields
            nameField.setText("");
            amountField.setText("");
            intField.setText("");
            minField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makePayment() {
        String paymentStr = payField.getText().trim();

        if (paymentStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter payment amount.",
                    "Missing Payment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double payment = Double.parseDouble(paymentStr);
            Debt topDebt = controller.getManager().peekTOS();

            if (topDebt == null) {
                JOptionPane.showMessageDialog(this, "No debts to pay.",
                        "Empty Stack", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            topDebt.makePayment(payment);
            log("PAYMENT: Applied $" + String.format("%.2f", payment) +
                    " to " + topDebt.getName() +
                    " (Remaining: $" + String.format("%.2f", topDebt.getCurrentBalance()) + ")");

            if (topDebt.isPaidOff()) {
                Debt paidDebt = controller.getManager().popDebt();
                paidOffDebts.add(paidDebt);
                log("PAID OFF: " + paidDebt.getName() + " moved to Paid-Off column");
            }

            payField.setText("");
            refreshAll();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid payment amount.",
                    "Invalid Payment", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Original action handlers (updated)
    private void onAddClicked() {
        JTextField name = new JTextField();
        JTextField amt = new JTextField();
        JTextField ir = new JTextField();
        JTextField mp = new JTextField();
        Object[] message = {
                "Debt Name:", name,
                "Total Amount:", amt,
                "Interest Rate (%):", ir,
                "Minimum Payment:", mp
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Debt",
                JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String nm = name.getText().trim();
                double a = Double.parseDouble(amt.getText().trim());
                double i = ir.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(ir.getText().trim());
                double m = mp.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(mp.getText().trim());
                Debt d = new Debt(nm, a, i, m);
                controller.getManager().pushDebt(d);
                log("PUSH: Added " + nm + " ($" + String.format("%.2f", a) + ")");
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onPeekClicked() {
        Debt top = controller.getManager().peekTOS();
        if (top == null) {
            JOptionPane.showMessageDialog(this, "Stack is empty.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Move to auxiliary
        Debt moved = controller.getManager().popDebt();
        auxiliaryDebts.add(moved);
        log("PEEK -> AUX: " + moved.getName() + " moved to Auxiliary");
        refreshAll();
    }

    private void onSettleClicked() {
        Debt top = controller.getManager().peekTOS();
        if (top == null) {
            JOptionPane.showMessageDialog(this, "No debt on top of stack.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String s = JOptionPane.showInputDialog(this,
                "Enter payment amount for " + top.getName(), "Settle Payment", JOptionPane.PLAIN_MESSAGE);
        if (s == null || s.trim().isEmpty())
            return;

        try {
            double amt = Double.parseDouble(s.trim());
            top.makePayment(amt);
            log("SETTLE: $" + String.format("%.2f", amt) + " applied to " + top.getName());

            if (top.isPaidOff()) {
                Debt removed = controller.getManager().popDebt();
                paidOffDebts.add(removed);
                log("PAID-OFF: " + removed.getName() + " moved to Paid-Off");
            }
            refreshAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteClicked() {
        Debt top = controller.getManager().peekTOS();
        if (top == null) {
            JOptionPane.showMessageDialog(this, "Stack empty.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete top debt: " + top.getName() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Debt removed = controller.getManager().popDebt();
            paidOffDebts.add(removed);
            log("DELETE: " + removed.getName() + " removed (moved to Paid-Off)");
            refreshAll();
        }
    }

    private void onHistoryClicked() {
        // Toggle logs visibility
        boolean visible = logsScrollPane.isVisible();
        logsScrollPane.setVisible(!visible);
        mainLayer.revalidate();
        mainLayer.repaint();
    }

    private void onProfileClicked() {
        JOptionPane.showMessageDialog(this,
                "Profile management feature coming soon!\n\n" +
                        "Future features:\n" +
                        "- User profile customization\n" +
                        "- Financial goals setting\n" +
                        "- Progress tracking\n" +
                        "- Strategy preferences",
                "Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    // Utility methods
    private void log(String message) {
        String timestamp = java.time.LocalTime.now().withNano(0).toString();
        logsArea.append("[" + timestamp + "] " + message + "\n");
        logsArea.setCaretPosition(logsArea.getDocument().getLength());
    }

    private String getTopName() {
        Debt top = controller.getManager().peekTOS();
        return top == null ? "None" : top.getName();
    }

    private void refreshAll() {
        // Update tower visualization
        if (towerPanel != null) {
            towerPanel.repaint();
        }

        // Update card panel
        updateCardPanel();

        // Update payment panel TOS label
        mainLayer.revalidate();
        mainLayer.repaint();
    }

    private void updateCardPanel() {
        // This would update the card with current TOS information
        // For now, we'll just repaint
        if (cardPanel != null) {
            cardPanel.repaint();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new UserDashboard(controller));
    }
}
