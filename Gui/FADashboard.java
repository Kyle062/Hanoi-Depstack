package Gui;

import Model.AppController;
import Model.Debt;
import Model.DebtManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FADashboard extends JFrame {

    // --- Original State/Model variables kept for structure ---
    private final static AppController controller = new AppController();
    private DebtManager manager;

    // UI components
    private JLayeredPane layeredPane;
    private JPanel mainLayer;
    private JLabel backgroundLabel;

    // Panels
    private JPanel towerContainer;
    private JPanel reportsPanel;
    private JPanel reportCreationPanel;
    private JPanel appointmentSchedulePanel;
    private JPanel logsPanel;

    // Controls
    private JTextField appointmentsField, solvedField, failedField, addedField;
    private JTextArea logsArea;
    private JScrollPane logsScrollPane;

    // Data lists
    private List<Debt> auxiliaryDebts = new ArrayList<>();
    private List<Debt> paidOffDebts = new ArrayList<>();

    // Colors
    private final Color ORANGE_ACCENT = new Color(241, 122, 80);
    private final Color BLUE_ACCENT = new Color(133, 196, 230);
    private final Color TOWER_PILLAR_COLOR = Color.BLACK;
    private final Color LIGHT_GREEN = new Color(145, 201, 74);
    private final Color YELLOW = new Color(255, 215, 0);
    private final Color ORANGE_DEBT = new Color(240, 150, 60);
    private final Color RED_DEBT = new Color(255, 100, 50);
    private final Color RED_REPORT = new Color(255, 100, 50);
    private final Color DARK_ORANGE_REPORT = new Color(220, 100, 50);

    // Sidebar button states
    private SidebarButton currentActiveButton = null;
    private final Color SIDEBAR_INACTIVE_COLOR = Color.WHITE;
    private final Color SIDEBAR_ACTIVE_COLOR = new Color(241, 122, 80); // ORANGE_ACCENT
    private final Color SIDEBAR_HOVER_COLOR = new Color(255, 140, 100);

    // Component positions - YOU HAVE FULL CONTROL OVER THESE VALUES
    private int sidebarX = 10;
    private int sidebarY = 100;
    private int sidebarWidth = 80;
    private int sidebarHeight = 600;

    private int towerX = 100;
    private int towerY = 30;
    private int towerWidth = 600;
    private int towerHeight = 400;

    private int reportCreationX = 1450;
    private int reportCreationY = 30;
    private int reportCreationWidth = 450;
    private int reportCreationHeight = 500;

    private int reportsX = 90;
    private int reportsY = 450;
    private int reportsWidth = 600;
    private int reportsHeight = 100;

    private int appointmentX = 1450;
    private int appointmentY = 550;
    private int appointmentWidth = 450;
    private int appointmentHeight = 450;

    private int logsX = 100;
    private int logsY = 800;
    private int logsWidth = 600;
    private int logsHeight = 200;

    public FADashboard(AppController controller2) {
        manager = controller.getManager();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("Hanoi Debt Tower Dashboard");
        setLayout(null); // Set layout to null for manual positioning

        initUI();
        setVisible(true);
    }

    private void initUI() {
        layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.setLayout(null);
        add(layeredPane);

        // Add resize listener - KEPT BUT SIMPLIFIED
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Only update background and main layer sizes
                updateBaseLayout(getWidth(), getHeight());
            }
        });

        setupBackground();

        // Main layer
        mainLayer = new JPanel(null);
        mainLayer.setOpaque(false);
        mainLayer.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(mainLayer, JLayeredPane.PALETTE_LAYER);

        // Create all components with your manual positions
        createSidebar();
        createTopRow();
        createBottomRow();

        // Update base layout initially
        updateBaseLayout(getWidth(), getHeight());
    }

    private void updateBaseLayout(int screenWidth, int screenHeight) {
        // Update layered pane bounds
        layeredPane.setBounds(0, 0, screenWidth, screenHeight);

        // Update background
        backgroundLabel.setBounds(0, 0, screenWidth, screenHeight);

        // Update main layer
        mainLayer.setBounds(0, 0, screenWidth, screenHeight);

        // Repaint
        repaint();
    }

    private void setupBackground() {
        try {
            BufferedImage bg = ImageIO.read(new File("Images/DashboardMainBackground.png"));

            backgroundLabel = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    if (bg != null) {
                        g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        g.setColor(new Color(60, 40, 30));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };

        } catch (IOException e) {
            backgroundLabel = new JLabel();
            backgroundLabel.setOpaque(true);
            backgroundLabel.setBackground(new Color(60, 40, 30));
        }

        backgroundLabel.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);
    }

    private JPanel sidebar;
    private TowerVisualizationPanel towerVis;

    private void createSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setBounds(sidebarX, sidebarY, sidebarWidth, sidebarHeight);

        // Title/Header (HANOI)
        JLabel hanoi = new JLabel("HANOI");
        hanoi.setForeground(Color.WHITE);
        hanoi.setFont(new Font("SansSerif", Font.BOLD, 14));
        hanoi.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 0));
        hanoi.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(hanoi);

        sidebar.add(Box.createVerticalStrut(40));

        // DASHBOARD button - Active by default
        SidebarButton dashboardBtn = createSidebarButton("DASH", "Dashboard", () -> {
            showDashboard();
            log("Dashboard view activated");
        });
        sidebar.add(dashboardBtn);
        sidebar.add(Box.createVerticalStrut(2));

        // DASHBOARD sub-label
        JLabel dashboardSubLabel = createSidebarSubLabel("BOARD");
        sidebar.add(dashboardSubLabel);
        sidebar.add(Box.createVerticalStrut(40));

        // CLIENT button
        SidebarButton clientBtn = createSidebarButton("CLIENT", "Client Management", () -> {
            showClientManagement();
            log("Client Management view activated");
        });
        sidebar.add(clientBtn);
        sidebar.add(Box.createVerticalStrut(40));

        // HISTORY button
        SidebarButton historyBtn = createSidebarButton("HISTORY", "Transaction History", () -> {
            showHistory();
            log("Transaction History view activated");
        });
        sidebar.add(historyBtn);
        sidebar.add(Box.createVerticalStrut(40));

        // PROFILE button
        SidebarButton profileBtn = createSidebarButton("PROFILE", "User Profile", () -> {
            showProfile();
            log("User Profile view activated");
        });
        sidebar.add(profileBtn);

        // Set dashboard as active by default
        setActiveSidebarButton(dashboardBtn);

        mainLayer.add(sidebar);
    }

    // SidebarButton inner class
    private class SidebarButton extends JButton {
        private boolean isActive = false;

        public SidebarButton(String text) {
            super(text);
            setupButton();
        }

        private void setupButton() {
            setForeground(SIDEBAR_INACTIVE_COLOR);
            setFont(new Font("SansSerif", Font.BOLD, 18));
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setMaximumSize(new Dimension(80, 50));
            setAlignmentX(Component.CENTER_ALIGNMENT);

            // Add hover effect
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isActive) {
                        setForeground(SIDEBAR_HOVER_COLOR);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isActive) {
                        setForeground(SIDEBAR_INACTIVE_COLOR);
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            if (active) {
                setForeground(SIDEBAR_ACTIVE_COLOR);
                setFont(new Font("SansSerif", Font.BOLD, 20)); // Slightly larger when active
            } else {
                setForeground(SIDEBAR_INACTIVE_COLOR);
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        }

        public boolean isActive() {
            return isActive;
        }
    }

    private SidebarButton createSidebarButton(String text, String tooltip, Runnable action) {
        SidebarButton button = new SidebarButton(text);
        button.setToolTipText(tooltip);

        button.addActionListener(e -> {
            // Execute the action
            action.run();

            // Update active state
            setActiveSidebarButton(button);
        });

        return button;
    }

    private JLabel createSidebarSubLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 10));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setMaximumSize(new Dimension(80, 20));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void setActiveSidebarButton(SidebarButton button) {
        // Deactivate current button
        if (currentActiveButton != null) {
            currentActiveButton.setActive(false);
        }

        // Activate new button
        button.setActive(true);
        currentActiveButton = button;
    }

    // Sidebar button functionalities
    private void showDashboard() {
        // This is already the dashboard view
        // You could add specific dashboard refresh logic here
        log("Refreshing dashboard data...");

        // Example: Refresh tower visualization
        if (towerVis != null) {
            towerVis.repaint();
        }

        // Example: Update logs
        log("Dashboard refreshed at: " + new java.util.Date());
    }

    private void showClientManagement() {
        // Create and show client management dialog/modal
        log("Opening Client Management...");

        JDialog clientDialog = new JDialog(this, "Client Management", true);
        clientDialog.setSize(600, 400);
        clientDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Client management UI components
        JLabel title = new JLabel("Client Management", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(ORANGE_ACCENT);

        JTextArea clientInfo = new JTextArea();
        clientInfo.setText("Client Management Features:\n\n" +
                "1. View Client List\n" +
                "2. Add New Client\n" +
                "3. Edit Client Information\n" +
                "4. View Client Debt History\n" +
                "5. Schedule Appointments\n" +
                "6. Generate Client Reports");
        clientInfo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        clientInfo.setEditable(false);
        clientInfo.setOpaque(false);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> clientDialog.dispose());

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(clientInfo), BorderLayout.CENTER);
        panel.add(closeBtn, BorderLayout.SOUTH);

        clientDialog.add(panel);
        clientDialog.setVisible(true);
    }

    private void showHistory() {
        // Create and show history dialog
        log("Opening Transaction History...");

        JDialog historyDialog = new JDialog(this, "Transaction History", true);
        historyDialog.setSize(700, 500);
        historyDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // History UI components
        JLabel title = new JLabel("Transaction History", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(ORANGE_ACCENT);

        // Create a table for transaction history
        String[] columns = { "Date", "Client", "Amount", "Type", "Status" };
        Object[][] data = {
                { "2024-01-15", "John Doe", "$1,250.00", "Payment", "Completed" },
                { "2024-01-14", "Jane Smith", "$2,500.00", "Debt Added", "Pending" },
                { "2024-01-13", "Bob Johnson", "$750.00", "Payment", "Completed" },
                { "2024-01-12", "Alice Brown", "$3,200.00", "Debt Added", "Pending" },
                { "2024-01-11", "Charlie Wilson", "$1,800.00", "Payment", "Completed" }
        };

        JTable historyTable = new JTable(data, columns);
        historyTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        historyTable.setRowHeight(25);

        JScrollPane tableScroll = new JScrollPane(historyTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportBtn = new JButton("Export to CSV");
        JButton refreshBtn = new JButton("Refresh");
        JButton closeBtn = new JButton("Close");

        exportBtn.addActionListener(e -> log("Exporting history to CSV..."));
        refreshBtn.addActionListener(e -> log("Refreshing history data..."));
        closeBtn.addActionListener(e -> historyDialog.dispose());

        buttonPanel.add(exportBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(closeBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        historyDialog.add(panel);
        historyDialog.setVisible(true);
    }

    private void showProfile() {
        // Create and show profile dialog
        log("Opening User Profile...");

        JDialog profileDialog = new JDialog(this, "User Profile", true);
        profileDialog.setSize(500, 400);
        profileDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Profile UI components
        JLabel title = new JLabel("User Profile", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(ORANGE_ACCENT);

        // Profile information
        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        infoPanel.add(new JLabel("Username:"));
        infoPanel.add(new JLabel("financial_advisor"));

        infoPanel.add(new JLabel("Name:"));
        infoPanel.add(new JLabel("John Smith"));

        infoPanel.add(new JLabel("Email:"));
        infoPanel.add(new JLabel("john.smith@company.com"));

        infoPanel.add(new JLabel("Role:"));
        infoPanel.add(new JLabel("Financial Advisor"));

        infoPanel.add(new JLabel("Total Clients:"));
        infoPanel.add(new JLabel("23,113"));

        infoPanel.add(new JLabel("Solved Cases:"));
        infoPanel.add(new JLabel("11,241"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editBtn = new JButton("Edit Profile");
        JButton logoutBtn = new JButton("Logout");
        JButton closeBtn = new JButton("Close");

        editBtn.addActionListener(e -> {
            log("Editing profile...");
            // Open edit profile dialog
        });

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(profileDialog,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                log("User logged out");
                profileDialog.dispose();
                // Here you would typically close the application or show login screen
            }
        });

        closeBtn.addActionListener(e -> profileDialog.dispose());

        buttonPanel.add(editBtn);
        buttonPanel.add(logoutBtn);
        buttonPanel.add(closeBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        profileDialog.add(panel);
        profileDialog.setVisible(true);
    }

    private void createTopRow() {
        // Tower Panel
        towerContainer = new RoundedPanel(20, Color.WHITE);
        towerContainer.setLayout(null);
        towerContainer.setBounds(towerX, towerY, towerWidth, towerHeight);

        towerVis = new TowerVisualizationPanel();
        towerVis.setBounds(0, 0, towerWidth, towerHeight);
        towerContainer.add(towerVis);
        mainLayer.add(towerContainer);

        // Create Report Panel
        reportCreationPanel = createReportCreationPanel();
        reportCreationPanel.setLayout(null);
        reportCreationPanel.setBounds(reportCreationX, reportCreationY, reportCreationWidth, reportCreationHeight);
        mainLayer.add(reportCreationPanel);
    }

    private void createBottomRow() {
        // Reports Panel
        reportsPanel = createReportsPanel();
        reportsPanel.setBounds(reportsX, reportsY, reportsWidth, reportsHeight);
        mainLayer.add(reportsPanel);

        // Appointment Schedule Panel
        appointmentSchedulePanel = createAppointmentSchedulePanel();
        appointmentSchedulePanel.setBounds(appointmentX, appointmentY, appointmentWidth, appointmentHeight);
        mainLayer.add(appointmentSchedulePanel);

        // Logs Panel
        logsPanel = createLogsPanel();
        logsPanel.setBounds(logsX, logsY, logsWidth, logsHeight);
        mainLayer.add(logsPanel);
    }

    private JPanel createReportCreationPanel() {
        RoundedPanel panel = new RoundedPanel(20, Color.WHITE);
        panel.setLayout(null);
        panel.setBounds(0, 0, reportCreationWidth, reportCreationHeight);

        JLabel title = new JLabel("Create Report");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBounds(20, 20, 200, 30);
        panel.add(title);

        int y = 70;
        int gap = 55;
        int fieldH = 30;
        int width = reportCreationWidth - 40; // 20px padding on both sides

        // Total Client Appointments
        addLabel(panel, "Total Client Appointments:", 20, y - 20);
        appointmentsField = addTextField(panel, "", 20, y, width, fieldH);

        // Total Solved
        y += gap;
        addLabel(panel, "Total Solved:", 20, y - 20);
        solvedField = addTextField(panel, "", 20, y, width, fieldH);

        // Total Failed
        y += gap;
        addLabel(panel, "Total Failed:", 20, y - 20);
        failedField = addTextField(panel, "", 20, y, width, fieldH);

        // Total Added
        y += gap;
        addLabel(panel, "Total Added:", 20, y - 20);
        addedField = addTextField(panel, "", 20, y, width, fieldH);

        // PUSH Button
        JButton pushBtn = createOrangeButton("PUSH");
        pushBtn.setBounds(20, reportCreationHeight - 60, width, 40);
        pushBtn.addActionListener(e -> {
            log("PUSH button pressed.");
            log("Creating report with data:");
            log("  Appointments: " + appointmentsField.getText());
            log("  Solved: " + solvedField.getText());
            log("  Failed: " + failedField.getText());
            log("  Added: " + addedField.getText());

            // Show confirmation
            JOptionPane.showMessageDialog(this,
                    "Report created successfully!\nData has been saved.",
                    "Report Created",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(pushBtn);

        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel container = new JPanel(null);
        container.setOpaque(false);
        container.setBounds(0, 0, reportsWidth, reportsHeight);

        JLabel reportsTitle = new JLabel("REPORTS");
        reportsTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        reportsTitle.setForeground(Color.WHITE);
        reportsTitle.setBounds(0, 0, 100, 20);
        container.add(reportsTitle);

        JPanel reportBoxes = new JPanel(new GridLayout(1, 3, 15, 0));
        reportBoxes.setOpaque(false);
        reportBoxes.add(createReportBox("TOTAL CLIENTS", "23,113", DARK_ORANGE_REPORT));
        reportBoxes.add(createReportBox("TOTAL APPOINTMENT", "21", BLUE_ACCENT));
        reportBoxes.add(createReportBox("SOLVED", "11241 users", RED_REPORT));
        reportBoxes.setBounds(0, 25, reportsWidth, 75);
        container.add(reportBoxes);

        return container;
    }

    private JPanel createReportBox(String title, String value, Color bgColor) {
        RoundedPanel panel = new RoundedPanel(10, bgColor);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(190, 75));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(valueLabel);

        return panel;
    }

    private JPanel createAppointmentSchedulePanel() {
        RoundedPanel panel = new RoundedPanel(10, Color.WHITE);
        panel.setLayout(null);
        panel.setBounds(0, 0, appointmentWidth, appointmentHeight);

        JLabel title = new JLabel("Appointment Schedule");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBounds(20, 20, 300, 30);
        panel.add(title);

        JTextArea scheduleText = new JTextArea();
        scheduleText.setText(
                "Maria is set for a consultation within this\n" +
                        "day, just clicked the zoom link below.\n\n" +
                        "John is set for consultation for being\n" +
                        "inactive.");

        scheduleText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        scheduleText.setEditable(false);
        scheduleText.setLineWrap(true);
        scheduleText.setWrapStyleWord(true);
        scheduleText.setOpaque(false);
        scheduleText.setBounds(20, 60, appointmentWidth - 40, 100);
        panel.add(scheduleText);

        int lineY = 170;
        for (int i = 0; i < 4; i++) {
            JLabel line = new JLabel();
            line.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY.brighter()));
            line.setBounds(20, lineY + (i * 20), appointmentWidth - 40, 1);
            panel.add(line);
        }

        return panel;
    }

    private JPanel createLogsPanel() {
        JPanel logContainer = new JPanel(new BorderLayout());
        logContainer.setBackground(Color.BLACK);
        logContainer.setBounds(0, 0, logsWidth, logsHeight);

        JLabel logsTitle = new JLabel("OPERATION LOGS");
        logsTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        logsTitle.setForeground(Color.WHITE);
        logsTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        logContainer.add(logsTitle, BorderLayout.NORTH);

        logsArea = new JTextArea();
        logsArea.setEditable(false);
        logsArea.setBackground(Color.BLACK);
        logsArea.setForeground(Color.WHITE);
        logsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logsArea.setText(" > System Initialized.\n" +
                " > Launched 3 debts to Active Debt Stack.\n" +
                " > Dashboard loaded successfully.");

        logsScrollPane = new JScrollPane(logsArea);
        logsScrollPane.setBorder(null);
        logsScrollPane.setBackground(Color.BLACK);
        logsScrollPane.getViewport().setBackground(Color.BLACK);

        logContainer.add(logsScrollPane, BorderLayout.CENTER);

        return logContainer;
    }

    private void addLabel(JPanel p, String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(Color.BLACK);
        l.setBounds(x, y, 300, 20);
        p.add(l);
    }

    private JTextField addTextField(JPanel p, String ph, int x, int y, int w, int h) {
        JTextField tf = new JTextField();
        tf.setBounds(x, y, w, h);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setToolTipText(ph);
        p.add(tf);
        return tf;
    }

    private JButton createOrangeButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ORANGE_ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        return btn;
    }

    private void log(String s) {
        if (logsArea != null) {
            logsArea.append(" > " + s + "\n");
            logsArea.setCaretPosition(logsArea.getDocument().getLength());
        }
    }

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
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            g2.dispose();
            super.paintComponent(g);
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

            // Title
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 24));
            String title = "THE HANOI DEBT TOWER";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, (w - fm.stringWidth(title)) / 2, 40);

            // Base Line
            int baseY = h - 60;
            g2.setColor(new Color(150, 80, 150));
            g2.fillRoundRect(50, baseY, w - 100, 15, 10, 10);

            // Columns
            String[] labels = { "APPOINTMENT", "AUXILIARY", "SOLVED" };
            int colW = w / 3;

            g2.setColor(TOWER_PILLAR_COLOR);
            for (int i = 0; i < 3; i++) {
                int cx = colW * i + colW / 2;
                g2.fillRoundRect(cx - 5, 80, 10, baseY - 80, 10, 10);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                String lbl = labels[i];
                g2.drawString(lbl, cx - fm.stringWidth(lbl) / 2, baseY + 30);
            }

            // Draw Debts
            int brickH = 30;
            int gap = 5;
            int cx = colW / 2;

            // Disk 4 (Bottom)
            drawDisk(g2, cx, baseY - (4 * (brickH + gap)), 180, brickH, RED_DEBT, "Debt Due");

            // Disk 3
            drawDisk(g2, cx, baseY - (3 * (brickH + gap)), 160, brickH, ORANGE_DEBT, "Media Complaince Lead");

            // Disk 2
            drawDisk(g2, cx, baseY - (2 * (brickH + gap)), 140, brickH, YELLOW, "Agile Execution");

            // Disk 1 (Top)
            drawDisk(g2, cx, baseY - (1 * (brickH + gap)), 120, brickH, LIGHT_GREEN, "Appointment");
        }

        private void drawDisk(Graphics2D g2, int centerX, int y, int width, int height, Color c, String text) {
            g2.setColor(c);
            g2.fillRoundRect(centerX - width / 2, y, width, height, 15, 15);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, centerX - fm.stringWidth(text) / 2, y + height / 2 + 3);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FADashboard(controller));
    }
}