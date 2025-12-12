package Gui;

import Model.AppController;
import Model.ConsultationAppointment;
import Model.ConsultationRequest;
import Model.Debt;
import Model.DebtManager;
import Model.DataManager;
import Model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class FADashboard extends JFrame {

    private AppController controller;
    private DebtManager manager;

    // Consultation appointment data
    private List<ConsultationRequest> consultationRequests = new ArrayList<>();
    private List<ConsultationAppointment> scheduledAppointments = new ArrayList<>();

    // Debt data for advisor view - Now using Stack for proper LIFO
    private Stack<Debt> clientDebts = new Stack<>(); // Changed from List to Stack
    private List<Debt> auxiliaryDebts = new ArrayList<>();
    private List<Debt> paidOffDebts = new ArrayList<>();

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
    private JPanel auxiliaryPanel;

    // Controls
    private JTextField appointmentsField, solvedField, failedField, addedField;
    private JTextArea logsArea;
    private JScrollPane logsScrollPane;
    private JTextArea scheduleText;
    private JTextArea auxiliaryText;

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
    private final Color GREEN_ADD = new Color(0, 150, 0);
    private final Color AUXILIARY_COLOR = new Color(150, 150, 200);
    private final Color SOLVED_COLOR = new Color(100, 200, 100); // Green for solved reports

    // Sidebar button states
    private SidebarButton currentActiveButton = null;
    private final Color SIDEBAR_INACTIVE_COLOR = Color.WHITE;
    private final Color SIDEBAR_ACTIVE_COLOR = new Color(241, 122, 80);
    private final Color SIDEBAR_HOVER_COLOR = new Color(255, 140, 100);
    private final Color SIDEBAR_BG_ACTIVE = new Color(241, 122, 80, 50);

    private int sidebarX = 5;
    private int sidebarY = 250;
    private int sidebarWidth = 140;
    private int sidebarHeight = 400;

    private int towerX = 150;
    private int towerY = 30;
    private int towerWidth = 1250;
    private int towerHeight = 620;

    private int reportCreationX = 1450;
    private int reportCreationY = 30;
    private int reportCreationWidth = 450;
    private int reportCreationHeight = 500;

    private int reportsX = 150;
    private int reportsY = 670;
    private int reportsWidth = 1250;
    private int reportsHeight = 100;

    private int appointmentX = 1450;
    private int appointmentY = 550;
    private int appointmentWidth = 450;
    private int appointmentHeight = 450;

    private int logsX = 150;
    private int logsY = 800;
    private int logsWidth = 1250;
    private int logsHeight = 200;

    private int auxiliaryX = 150;
    private int auxiliaryY = 670;
    private int auxiliaryWidth = 300;
    private int auxiliaryHeight = 330;

    // Statistics
    private int totalAppointments = 0;
    private int totalSolved = 0;
    private int totalFailed = 0;
    private int totalAdded = 0;
    private int totalClients = 0;

    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    public FADashboard(AppController controller) {
        this.controller = controller;
        this.manager = controller.getManager();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("Hanoi Debt Tower Dashboard - Financial Advisor");
        setLayout(null);

        // Load data
        loadData();

        initUI();
        setVisible(true);
    }

    private void loadData() {
        // Load existing consultation data from file
        consultationRequests = DataManager.loadConsultationRequests();
        scheduledAppointments = DataManager.loadScheduledAppointments();

        // Calculate statistics
        updateStatistics();

        // Initialize with sample debt data for visualization
        initializeSampleDebtData();
    }

    private void initializeSampleDebtData() {
        // Clear existing data
        clientDebts.clear();
        auxiliaryDebts.clear();
        paidOffDebts.clear();

        // Add sample client debts for visualization (using Stack - LIFO)
        // Push to stack - last pushed will be TOS
        clientDebts.push(new Debt("Mike Johnson - Car Loan", 10000.00, 5.5, 250.00));
        clientDebts.push(new Debt("Sarah Smith - Student Loan", 15000.00, 6.8, 200.00));
        clientDebts.push(new Debt("John Doe - Credit Card", 5000.00, 18.5, 100.00));

        // Add some paid off debts
        paidOffDebts.add(new Debt("Emma Wilson - Medical Bill", 0.00, 0.0, 0.00));
        paidOffDebts.add(new Debt("Robert Brown - Personal Loan", 0.00, 0.0, 0.00));
    }

    private void updateStatistics() {
        totalAppointments = scheduledAppointments.size();
        totalSolved = (int) scheduledAppointments.stream()
                .filter(a -> a.getStatus().equals("COMPLETED"))
                .count();
        totalFailed = (int) scheduledAppointments.stream()
                .filter(a -> a.getStatus().equals("CANCELLED"))
                .count();
        totalAdded = totalAppointments;
        totalClients = (int) scheduledAppointments.stream()
                .map(ConsultationAppointment::getClientUsername)
                .distinct()
                .count();
    }

    private void initUI() {
        layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.setLayout(null);
        add(layeredPane);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateBaseLayout(getWidth(), getHeight());
            }
        });

        setupBackground();

        // Main layer
        mainLayer = new JPanel(null);
        mainLayer.setOpaque(false);
        mainLayer.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(mainLayer, JLayeredPane.PALETTE_LAYER);

        // Create all components
        createSidebar();
        createTopRow();
        createBottomRow();

        updateBaseLayout(getWidth(), getHeight());

        // Add window listener to save data on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.saveAllData();
                saveConsultationData();
            }
        });
    }

    private void saveConsultationData() {
        DataManager.saveConsultationRequests(consultationRequests);
        DataManager.saveScheduledAppointments(scheduledAppointments);
    }

    private void updateBaseLayout(int screenWidth, int screenHeight) {
        layeredPane.setBounds(0, 0, screenWidth, screenHeight);
        backgroundLabel.setBounds(0, 0, screenWidth, screenHeight);
        mainLayer.setBounds(0, 0, screenWidth, screenHeight);
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
        // Create a rounded panel for the sidebar background
        RoundedPanel sidebarContainer = new RoundedPanel(15, new Color(40, 40, 40, 200));
        sidebarContainer.setBounds(sidebarX, sidebarY, sidebarWidth, sidebarHeight);
        sidebarContainer.setLayout(new BorderLayout());

        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Title/Header (HANOI)
        JLabel hanoi = new JLabel("HANOI");
        hanoi.setForeground(Color.WHITE);
        hanoi.setFont(new Font("SansSerif", Font.BOLD, 16));
        hanoi.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        hanoi.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(hanoi);

        sidebar.add(Box.createVerticalStrut(20));

        // DASHBOARD button - Active by default
        SidebarButton dashboardBtn = createSidebarButton("Dashboard", "Dashboard", () -> {
            showDashboard();
            log("Dashboard view activated");
        });
        dashboardBtn.setFont(new Font("SansSerif", Font.BOLD, 10));
        sidebar.add(dashboardBtn);
        sidebar.add(Box.createVerticalStrut(20));

        // CLIENT button - Consultation appointments
        SidebarButton clientBtn = createSidebarButton("Client", "Client Consultation Appointment", () -> {
            showClientConsultationAppointment();
            log("Client Consultation Appointment Dialog activated");
        });
        sidebar.add(clientBtn);
        sidebar.add(Box.createVerticalStrut(20));

        // HISTORY button
        SidebarButton historyBtn = createSidebarButton("History", "Transaction History", () -> {
            showHistory();
            log("Transaction History view activated");
        });
        sidebar.add(historyBtn);
        sidebar.add(Box.createVerticalStrut(20));

        // PROFILE button
        SidebarButton profileBtn = createSidebarButton("Profile", "User Profile", () -> {
            showProfile();
            log("User Profile view activated");
        });
        sidebar.add(profileBtn);

        // AUXILIARY button - Move TOS to auxiliary
        sidebar.add(Box.createVerticalStrut(20));
        SidebarButton auxiliaryBtn = createSidebarButton("Auxiliary", "Move TOS to Auxiliary", () -> {
            moveToAuxiliary();
            log("Move to Auxiliary activated");
        });
        sidebar.add(auxiliaryBtn);

        // SOLVE REPORT button - Mark report as solved
        sidebar.add(Box.createVerticalStrut(20));
        SidebarButton solveReportBtn = createSidebarButton("Solve Report", "Mark report as solved", () -> {
            solveReport();
            log("Solve Report activated");
        });
        sidebar.add(solveReportBtn);

        // LOGOUT button
        sidebar.add(Box.createVerticalStrut(20));
        SidebarButton logoutBtn = createSidebarButton("Logout", "Logout from system", () -> {
            logout();
        });
        sidebar.add(logoutBtn);

        // Add vertical glue to push buttons to top
        sidebar.add(Box.createVerticalGlue());

        // Set dashboard as active by default
        setActiveSidebarButton(dashboardBtn);

        sidebarContainer.add(sidebar, BorderLayout.NORTH);
        mainLayer.add(sidebarContainer);
    }

    // SidebarButton inner class
    private class SidebarButton extends JPanel {
        private boolean isActive = false;
        private JLabel textLabel;
        private String buttonText;

        public SidebarButton(String text) {
            this.buttonText = text;
            setupButton();
        }

        private void setupButton() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(sidebarWidth - 20, 60));
            setPreferredSize(new Dimension(sidebarWidth - 20, 60));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            // Text label
            textLabel = new JLabel(buttonText);
            textLabel.setForeground(SIDEBAR_INACTIVE_COLOR);
            textLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
            textLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(textLabel, BorderLayout.CENTER);

            // Add hover and click effects
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isActive) {
                        textLabel.setForeground(SIDEBAR_HOVER_COLOR);
                        setBackground(new Color(255, 255, 255, 20));
                        setOpaque(true);
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isActive) {
                        textLabel.setForeground(SIDEBAR_INACTIVE_COLOR);
                        setOpaque(false);
                        repaint();
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            if (active) {
                textLabel.setForeground(SIDEBAR_ACTIVE_COLOR);
                textLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                setBackground(SIDEBAR_BG_ACTIVE);
                setOpaque(true);

                // Add active indicator
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, SIDEBAR_ACTIVE_COLOR),
                        BorderFactory.createEmptyBorder(10, 11, 10, 15)));
            } else {
                textLabel.setForeground(SIDEBAR_INACTIVE_COLOR);
                textLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
                setOpaque(false);
                setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            }
            repaint();
        }

        public boolean isActive() {
            return isActive;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isOpaque()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private SidebarButton createSidebarButton(String text, String tooltip, Runnable action) {
        SidebarButton button = new SidebarButton(text);
        button.setToolTipText(tooltip);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
                setActiveSidebarButton(button);
            }
        });

        return button;
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
        log("Refreshing dashboard data...");
        refreshDashboardData();
        if (towerVis != null) {
            towerVis.repaint();
        }
        log("Dashboard refreshed at: " + new java.util.Date());
    }

    private void refreshDashboardData() {
        // Update statistics
        updateStatistics();

        // Update report fields
        appointmentsField.setText(String.valueOf(totalAppointments));
        solvedField.setText(String.valueOf(totalSolved));
        failedField.setText(String.valueOf(totalFailed));
        addedField.setText(String.valueOf(totalAdded));

        // Update reports panel
        updateReportsPanel();

        // Update appointment schedule
        updateAppointmentSchedule();

        // Update auxiliary panel
        updateAuxiliaryPanel();
    }

    // AUXILIARY BUTTON FUNCTIONALITY - Same as UserDashboard
    private void moveToAuxiliary() {
        if (!clientDebts.isEmpty()) {
            Debt topDebt = clientDebts.peek(); // Get TOS (LIFO - Stack.peek())

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Move TOS to auxiliary?\n" +
                            "Client: " + topDebt.getName() + "\n" +
                            "Balance: $" + String.format("%.2f", topDebt.getCurrentBalance()) + "\n" +
                            "Position: TOS (Top of Stack)",
                    "Confirm Move to Auxiliary",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Debt movedDebt = clientDebts.pop(); // Remove from top of stack (LIFO)
                auxiliaryDebts.add(0, movedDebt); // Add to beginning for LIFO display

                log("MOVED: " + movedDebt.getName() + " from TOS to auxiliary (LIFO)");
                JOptionPane.showMessageDialog(this,
                        "Successfully moved to auxiliary:\n" +
                                movedDebt.getName() + "\n" +
                                "Balance: $" + String.format("%.2f", movedDebt.getCurrentBalance()),
                        "Moved to Auxiliary",
                        JOptionPane.INFORMATION_MESSAGE);

                refreshAllPanels();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No active debt to move!\n" +
                            "Client debt stack is empty.",
                    "No TOS Available",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // SOLVE REPORT BUTTON FUNCTIONALITY
    private void solveReport() {
        if (!clientDebts.isEmpty()) {
            Debt topReport = clientDebts.peek(); // Get TOS (LIFO)

            // Check if it's a report (contains " - " separator)
            if (topReport.getName().contains(" - ")) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Mark this report as SOLVED?\n" +
                                "Report: " + topReport.getName() + "\n" +
                                "Balance: $" + String.format("%.2f", topReport.getCurrentBalance()) + "\n" +
                                "Position: TOS (Top of Stack)",
                        "Confirm Solve Report",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    Debt solvedReport = clientDebts.pop(); // Remove from top of stack
                    solvedReport.makePayment(solvedReport.getCurrentBalance()); // Set balance to 0

                    // Add to paid-off at the beginning (LIFO order)
                    paidOffDebts.add(0, solvedReport);

                    log("SOLVED: Report marked as solved - " + solvedReport.getName());
                    JOptionPane.showMessageDialog(this,
                            "Report marked as SOLVED:\n" +
                                    solvedReport.getName() + "\n" +
                                    "Moved to Paid-Off section",
                            "Report Solved",
                            JOptionPane.INFORMATION_MESSAGE);

                    refreshAllPanels();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "The TOS is not a report.\n" +
                                "Only reports can be marked as solved.\n" +
                                "Current TOS: " + topReport.getName(),
                        "Not a Report",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No reports to solve!\n" +
                            "Client debt stack is empty.",
                    "No Reports Available",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showClientConsultationAppointment() {
        log("Opening Client Consultation Appointment Dialog...");

        JDialog appointmentDialog = new JDialog(this, "Client Consultation Appointments", true);
        appointmentDialog.setSize(600, 400);
        appointmentDialog.setLocationRelativeTo(this);
        appointmentDialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // Title
        JLabel title = new JLabel("Client Consultation Appointments");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(10));

        if (consultationRequests.isEmpty()) {
            JLabel noRequests = new JLabel("No pending consultation requests.");
            noRequests.setFont(new Font("SansSerif", Font.ITALIC, 12));
            noRequests.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(noRequests);
        } else {
            for (ConsultationRequest request : consultationRequests) {
                mainPanel.add(createRequestPanel(request, appointmentDialog));
                mainPanel.add(Box.createVerticalStrut(10));
            }
        }

        mainPanel.add(Box.createVerticalStrut(20));

        // Button to view scheduled appointments
        JButton viewScheduledBtn = new JButton("View Scheduled Appointments");
        viewScheduledBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewScheduledBtn.addActionListener(e -> {
            appointmentDialog.dispose();
            showScheduledAppointments();
        });
        mainPanel.add(viewScheduledBtn);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        appointmentDialog.add(scrollPane, BorderLayout.CENTER);
        appointmentDialog.setVisible(true);
    }

    private JPanel createRequestPanel(ConsultationRequest request, JDialog parentDialog) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Request Text
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        infoPanel.setOpaque(false);

        JLabel clientLabel = new JLabel(
                "Client: " + request.getClientName() + " (" + request.getClientUsername() + ")");
        clientLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel reasonLabel = new JLabel("Reason: " + request.getReason());
        reasonLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JLabel dateLabel = new JLabel("Preferred Date: " + request.getPreferredDate());
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        infoPanel.add(clientLabel);
        infoPanel.add(reasonLabel);
        infoPanel.add(dateLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        // Add Button (Green)
        JButton addBtn = new JButton("Schedule");
        addBtn.setBackground(GREEN_ADD);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.addActionListener(e -> scheduleAppointment(request, parentDialog));

        // Reject Button (Red)
        JButton rejectBtn = new JButton("Reject");
        rejectBtn.setBackground(Color.RED);
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.setFocusPainted(false);
        rejectBtn.setBorderPainted(false);
        rejectBtn.addActionListener(e -> rejectRequest(request, parentDialog));

        buttonGroup.add(addBtn);
        buttonGroup.add(rejectBtn);

        panel.add(buttonGroup, BorderLayout.EAST);

        return panel;
    }

    private void scheduleAppointment(ConsultationRequest request, JDialog parentDialog) {
        // Get all financial advisors
        Map<String, User> users = DataManager.loadUsers();
        List<User> advisors = new ArrayList<>();

        for (User user : users.values()) {
            if ("ADVISOR".equals(user.getUserType()) &&
                    !user.getUsername().equals(controller.getCurrentUsername())) {
                advisors.add(user);
            }
        }

        if (advisors.isEmpty()) {
            JOptionPane.showMessageDialog(parentDialog,
                    "No other financial advisors available. You can schedule with yourself.",
                    "No Advisors", JOptionPane.WARNING_MESSAGE);

            // Schedule with current advisor
            ConsultationAppointment appointment = new ConsultationAppointment(
                    request.getClientUsername(),
                    request.getClientName(),
                    controller.getCurrentUsername(),
                    controller.getCurrentUser().getFullName(),
                    request.getReason(),
                    "Zoom",
                    request.getPreferredDate(),
                    new Date(),
                    "SCHEDULED");

            scheduledAppointments.add(appointment);
            consultationRequests.remove(request);

            JOptionPane.showMessageDialog(parentDialog,
                    "Appointment scheduled with yourself.",
                    "Appointment Scheduled", JOptionPane.INFORMATION_MESSAGE);

            refreshDashboardData();
            saveConsultationData();
            parentDialog.dispose();
            return;
        }

        // Create advisor selection dialog
        JDialog advisorDialog = new JDialog(parentDialog, "Select Financial Advisor", true);
        advisorDialog.setSize(400, 300);
        advisorDialog.setLocationRelativeTo(parentDialog);
        advisorDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Select a Financial Advisor:");
        label.setFont(new Font("SansSerif", Font.BOLD, 14));

        DefaultListModel<String> advisorListModel = new DefaultListModel<>();
        advisorListModel.addElement("Self - " + controller.getCurrentUser().getFullName());
        for (User advisor : advisors) {
            advisorListModel.addElement(advisor.getFullName() + " (" + advisor.getUsername() + ")");
        }

        JList<String> advisorList = new JList<>(advisorListModel);
        advisorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        advisorList.setSelectedIndex(0);

        JScrollPane scrollPane = new JScrollPane(advisorList);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton selectBtn = new JButton("Select");
        JButton cancelBtn = new JButton("Cancel");

        selectBtn.addActionListener(e -> {
            String selected = advisorList.getSelectedValue();
            if (selected != null) {
                String advisorUsername;
                String advisorName;

                if (selected.startsWith("Self")) {
                    advisorUsername = controller.getCurrentUsername();
                    advisorName = controller.getCurrentUser().getFullName();
                } else {
                    // Parse advisor info
                    int start = selected.lastIndexOf('(') + 1;
                    int end = selected.lastIndexOf(')');
                    advisorUsername = selected.substring(start, end);
                    advisorName = selected.substring(0, selected.lastIndexOf('(')).trim();
                }

                // Ask for platform and confirm date
                String[] platforms = { "Zoom", "Google Meet", "Microsoft Teams", "Phone Call", "In Person" };
                String platform = (String) JOptionPane.showInputDialog(
                        advisorDialog,
                        "Select platform for the appointment:",
                        "Select Platform",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        platforms,
                        platforms[0]);

                if (platform != null) {
                    ConsultationAppointment appointment = new ConsultationAppointment(
                            request.getClientUsername(),
                            request.getClientName(),
                            advisorUsername,
                            advisorName,
                            request.getReason(),
                            platform,
                            request.getPreferredDate(),
                            new Date(),
                            "SCHEDULED");

                    scheduledAppointments.add(appointment);
                    consultationRequests.remove(request);

                    log("Appointment scheduled: " + request.getClientName() +
                            " with " + advisorName + " via " + platform);

                    JOptionPane.showMessageDialog(advisorDialog,
                            "Appointment scheduled successfully!\n" +
                                    "Client: " + request.getClientName() + "\n" +
                                    "Advisor: " + advisorName + "\n" +
                                    "Platform: " + platform + "\n" +
                                    "Date: " + request.getPreferredDate(),
                            "Appointment Scheduled", JOptionPane.INFORMATION_MESSAGE);

                    refreshDashboardData();
                    saveConsultationData();
                    advisorDialog.dispose();
                    parentDialog.dispose();
                }
            }
        });

        cancelBtn.addActionListener(e -> advisorDialog.dispose());

        buttonPanel.add(selectBtn);
        buttonPanel.add(cancelBtn);

        contentPanel.add(label, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        advisorDialog.add(contentPanel);
        advisorDialog.setVisible(true);
    }

    private void rejectRequest(ConsultationRequest request, JDialog parentDialog) {
        int confirm = JOptionPane.showConfirmDialog(parentDialog,
                "Are you sure you want to reject " + request.getClientName() + "'s request?",
                "Confirm Rejection", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            consultationRequests.remove(request);
            totalFailed++;
            log("Appointment rejected for: " + request.getClientName());
            refreshDashboardData();
            saveConsultationData();
            parentDialog.dispose();
            showClientConsultationAppointment(); // Refresh the dialog
        }
    }

    private void showScheduledAppointments() {
        JDialog scheduledDialog = new JDialog(this, "Scheduled Appointments", true);
        scheduledDialog.setSize(700, 500);
        scheduledDialog.setLocationRelativeTo(this);
        scheduledDialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Scheduled Appointments (" + scheduledAppointments.size() + ")");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(10));

        if (scheduledAppointments.isEmpty()) {
            JLabel noAppointments = new JLabel("No scheduled appointments.");
            noAppointments.setFont(new Font("SansSerif", Font.ITALIC, 12));
            noAppointments.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(noAppointments);
        } else {
            for (ConsultationAppointment appointment : scheduledAppointments) {
                mainPanel.add(createAppointmentPanel(appointment, scheduledDialog));
                mainPanel.add(Box.createVerticalStrut(10));
            }
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scheduledDialog.add(scrollPane, BorderLayout.CENTER);
        scheduledDialog.setVisible(true);
    }

    private JPanel createAppointmentPanel(ConsultationAppointment appointment, JDialog parentDialog) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 2, 2));

        Color statusColor;
        switch (appointment.getStatus()) {
            case "SCHEDULED":
                statusColor = Color.BLUE;
                break;
            case "COMPLETED":
                statusColor = Color.GREEN.darker();
                break;
            case "CANCELLED":
                statusColor = Color.RED;
                break;
            default:
                statusColor = Color.GRAY;
        }

        JLabel statusLabel = new JLabel("Status: " + appointment.getStatus());
        statusLabel.setForeground(statusColor);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel clientLabel = new JLabel("Client: " + appointment.getClientName());
        JLabel advisorLabel = new JLabel("Advisor: " + appointment.getAdvisorName());
        JLabel reasonLabel = new JLabel("Reason: " + appointment.getReason());
        JLabel platformLabel = new JLabel("Platform: " + appointment.getPlatform());
        JLabel dateLabel = new JLabel("Date: " + appointment.getAppointmentDate());
        JLabel scheduledLabel = new JLabel("Scheduled on: " + dateFormat.format(appointment.getScheduledDate()));

        infoPanel.add(statusLabel);
        infoPanel.add(clientLabel);
        infoPanel.add(advisorLabel);
        infoPanel.add(reasonLabel);
        infoPanel.add(platformLabel);
        infoPanel.add(dateLabel);
        infoPanel.add(scheduledLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        // Action buttons
        if ("SCHEDULED".equals(appointment.getStatus())) {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

            JButton completeBtn = new JButton("Complete");
            completeBtn.setBackground(Color.GREEN.darker());
            completeBtn.setForeground(Color.WHITE);
            completeBtn.setFocusPainted(false);
            completeBtn.addActionListener(e -> completeAppointment(appointment, parentDialog));

            JButton cancelBtn = new JButton("Cancel");
            cancelBtn.setBackground(Color.RED);
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setFocusPainted(false);
            cancelBtn.addActionListener(e -> cancelAppointment(appointment, parentDialog));

            actionPanel.add(completeBtn);
            actionPanel.add(cancelBtn);
            panel.add(actionPanel, BorderLayout.EAST);
        }

        return panel;
    }

    private void completeAppointment(ConsultationAppointment appointment, JDialog parentDialog) {
        appointment.setStatus("COMPLETED");
        totalSolved++;

        // Create a debt entry for this completed appointment
        String debtName = appointment.getClientName() + " - " + appointment.getReason();
        Debt completedAppointmentDebt = new Debt(debtName, 0.00, 0.0, 0.00);

        // Move to paid-off immediately (no delay) at the beginning for LIFO
        paidOffDebts.add(0, completedAppointmentDebt);

        log("Appointment completed and moved to paid-off: " + appointment.getClientName() +
                " - " + appointment.getReason());

        refreshDashboardData();
        saveConsultationData();
        parentDialog.dispose();
        showScheduledAppointments();
    }

    private void cancelAppointment(ConsultationAppointment appointment, JDialog parentDialog) {
        int confirm = JOptionPane.showConfirmDialog(parentDialog,
                "Are you sure you want to cancel this appointment?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            appointment.setStatus("CANCELLED");
            totalFailed++;
            log("Appointment cancelled: " + appointment.getClientName() + " with " + appointment.getAdvisorName());
            refreshDashboardData();
            saveConsultationData();
            parentDialog.dispose();
            showScheduledAppointments();
        }
    }

    private void showHistory() {
        log("Opening Transaction History...");
        JDialog historyDialog = new JDialog(this, "Transaction History", true);
        historyDialog.setSize(700, 500);
        historyDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Transaction History", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(ORANGE_ACCENT);

        // Real data columns
        String[] columns = { "Date", "Type", "Client/Advisor", "Amount/Status", "Details" };

        // Create table model with real data
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Add appointment history
        for (ConsultationAppointment appointment : scheduledAppointments) {
            model.addRow(new Object[] {
                    dateFormat.format(appointment.getScheduledDate()),
                    "Consultation",
                    appointment.getClientName(),
                    appointment.getStatus(),
                    "With: " + appointment.getAdvisorName()
            });
        }

        // Add client statistics
        model.addRow(new Object[] {
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                "Statistics",
                "All Clients",
                String.valueOf(totalClients),
                "Total Clients"
        });

        model.addRow(new Object[] {
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                "Statistics",
                "Appointments",
                String.valueOf(totalAppointments),
                "Total: " + totalSolved + " solved, " + totalFailed + " failed"
        });

        JTable historyTable = new JTable(model);
        historyTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        historyTable.setRowHeight(25);

        JScrollPane tableScroll = new JScrollPane(historyTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh");
        JButton closeBtn = new JButton("Close");

        refreshBtn.addActionListener(e -> {
            log("Refreshing history data...");
            loadData(); // Reload data
            historyDialog.dispose();
            showHistory(); // Reopen dialog
        });

        closeBtn.addActionListener(e -> historyDialog.dispose());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(closeBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        historyDialog.add(panel);
        historyDialog.setVisible(true);
    }

    private void showProfile() {
        log("Opening User Profile...");
        JDialog profileDialog = new JDialog(this, "User Profile", true);
        profileDialog.setSize(500, 400);
        profileDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("User Profile", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(ORANGE_ACCENT);

        JPanel infoPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String username = controller.getCurrentUsername();
        String userType = controller.getCurrentUserType();

        infoPanel.add(new JLabel("Username:"));
        infoPanel.add(new JLabel(username != null ? username : "N/A"));

        infoPanel.add(new JLabel("Name:"));
        infoPanel.add(
                new JLabel(controller.getCurrentUser() != null ? controller.getCurrentUser().getFullName() : "N/A"));

        infoPanel.add(new JLabel("Email:"));
        infoPanel.add(new JLabel(controller.getCurrentUser() != null ? controller.getCurrentUser().getEmail() : "N/A"));

        infoPanel.add(new JLabel("Role:"));
        infoPanel.add(new JLabel(userType != null ? userType : "N/A"));

        infoPanel.add(new JLabel("Total Clients:"));
        infoPanel.add(new JLabel(String.valueOf(totalClients)));

        infoPanel.add(new JLabel("Total Appointments:"));
        infoPanel.add(new JLabel(String.valueOf(totalAppointments)));

        infoPanel.add(new JLabel("Solved Cases:"));
        infoPanel.add(new JLabel(String.valueOf(totalSolved)));

        infoPanel.add(new JLabel("Failed/Cancelled:"));
        infoPanel.add(new JLabel(String.valueOf(totalFailed)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editBtn = new JButton("Edit Profile");
        JButton logoutBtn = new JButton("Logout");
        JButton closeBtn = new JButton("Close");

        editBtn.addActionListener(e -> {
            log("Editing profile...");
            JOptionPane.showMessageDialog(profileDialog,
                    "Edit profile feature coming soon.",
                    "Feature Preview", JOptionPane.INFORMATION_MESSAGE);
        });

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(profileDialog,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                log("User logged out");
                profileDialog.dispose();
                logout();
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

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.saveAllData();
            saveConsultationData();
            controller.logout();
            dispose();

            // Return to login screen
            SwingUtilities.invokeLater(() -> {
                new Login(controller).setVisible(true);
            });
        }
    }

    private void createTopRow() {
        towerContainer = new RoundedPanel(20, Color.WHITE);
        towerContainer.setLayout(null);
        towerContainer.setBounds(towerX, towerY, towerWidth, towerHeight);

        towerVis = new TowerVisualizationPanel();
        towerVis.setBounds(0, 0, towerWidth, towerHeight);
        towerContainer.add(towerVis);
        mainLayer.add(towerContainer);

        reportCreationPanel = createReportCreationPanel();
        reportCreationPanel.setLayout(null);
        reportCreationPanel.setBounds(reportCreationX, reportCreationY, reportCreationWidth, reportCreationHeight);
        mainLayer.add(reportCreationPanel);
    }

    private void createBottomRow() {
        // Adjust reports panel position
        reportsPanel = createReportsPanel();
        reportsPanel.setBounds(reportsX, reportsY, reportsWidth, reportsHeight);
        mainLayer.add(reportsPanel);

        // Add auxiliary panel
        auxiliaryPanel = createAuxiliaryPanel();
        auxiliaryPanel.setBounds(auxiliaryX, auxiliaryY, auxiliaryWidth, auxiliaryHeight);
        mainLayer.add(auxiliaryPanel);

        appointmentSchedulePanel = createAppointmentSchedulePanel();
        appointmentSchedulePanel.setBounds(appointmentX, appointmentY, appointmentWidth, appointmentHeight);
        mainLayer.add(appointmentSchedulePanel);

        logsPanel = createLogsPanel();
        logsPanel.setBounds(logsX, logsY, logsWidth, logsHeight);
        mainLayer.add(logsPanel);
    }

    private JPanel createAuxiliaryPanel() {
        RoundedPanel panel = new RoundedPanel(15, AUXILIARY_COLOR);
        panel.setLayout(new BorderLayout());
        panel.setBounds(0, 0, auxiliaryWidth, auxiliaryHeight);

        JLabel title = new JLabel("Auxiliary Debts", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        auxiliaryText = new JTextArea();
        auxiliaryText.setEditable(false);
        auxiliaryText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        auxiliaryText.setForeground(Color.WHITE);
        auxiliaryText.setOpaque(false);
        auxiliaryText.setLineWrap(true);
        auxiliaryText.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(auxiliaryText);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        panel.add(scrollPane, BorderLayout.CENTER);

        updateAuxiliaryPanel();

        return panel;
    }

    private void updateAuxiliaryPanel() {
        if (auxiliaryText != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Auxiliary Debts (LIFO):\n");
            sb.append("========================\n\n");

            if (auxiliaryDebts.isEmpty()) {
                sb.append("No debts in auxiliary.\n");
            } else {
                for (int i = 0; i < auxiliaryDebts.size(); i++) {
                    Debt debt = auxiliaryDebts.get(i);
                    sb.append(i + 1).append(". ").append(debt.getName())
                            .append("\n   Balance: $").append(String.format("%.2f", debt.getCurrentBalance()))
                            .append("\n   Min Pay: $").append(String.format("%.2f", debt.getMinimumPayment()))
                            .append("\n   Rate: ").append(debt.getInterestRate()).append("%\n\n");
                }
            }

            auxiliaryText.setText(sb.toString());
        }
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
        int width = reportCreationWidth - 40;

        addLabel(panel, "Total Client Appointments:", 20, y - 20);
        appointmentsField = addTextField(panel, String.valueOf(totalAppointments), 20, y, width, fieldH);
        appointmentsField.setEditable(false);

        y += gap;
        addLabel(panel, "Total Solved:", 20, y - 20);
        solvedField = addTextField(panel, String.valueOf(totalSolved), 20, y, width, fieldH);
        solvedField.setEditable(false);

        y += gap;
        addLabel(panel, "Total Failed:", 20, y - 20);
        failedField = addTextField(panel, String.valueOf(totalFailed), 20, y, width, fieldH);
        failedField.setEditable(false);

        y += gap;
        addLabel(panel, "Total Added:", 20, y - 20);
        addedField = addTextField(panel, String.valueOf(totalAdded), 20, y, width, fieldH);
        addedField.setEditable(false);

        JButton pushBtn = createOrangeButton("GENERATE REPORT");
        pushBtn.setBounds(20, reportCreationHeight - 60, width, 40);
        pushBtn.addActionListener(e -> {
            log("Generating report...");
            log("Report Data:");
            log("  Appointments: " + totalAppointments);
            log("  Solved: " + totalSolved);
            log("  Failed: " + totalFailed);
            log("  Added: " + totalAdded);

            // Generate report text
            String reportText = generateReport();

            // Show report in dialog
            JTextArea reportArea = new JTextArea(reportText);
            reportArea.setEditable(false);
            reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(reportArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Generated Report",
                    JOptionPane.INFORMATION_MESSAGE);

            // Add to tower visualization after successful report generation
            // Using LIFO principle - new report becomes TOS
            String clientName = JOptionPane.showInputDialog(this,
                    "Enter client name for the report debt entry:");
            if (clientName != null && !clientName.trim().isEmpty()) {
                String reason = JOptionPane.showInputDialog(this,
                        "Enter reason for the report:");
                if (reason != null && !reason.trim().isEmpty()) {
                    String debtName = clientName + " - " + reason;
                    Debt reportDebt = new Debt(debtName, 0.00, 0.0, 0.00);

                    // Push to stack - becomes TOS (LIFO)
                    clientDebts.push(reportDebt);

                    log("Report debt added to tower as TOS: " + debtName);
                    log("Stack size: " + clientDebts.size() + ", TOS: " + clientDebts.peek().getName());

                    JOptionPane.showMessageDialog(this,
                            "Report added successfully!\n" +
                                    "Client: " + clientName + "\n" +
                                    "Reason: " + reason + "\n" +
                                    "Position: TOS (Top of Stack)\n" +
                                    "Stack Size: " + clientDebts.size(),
                            "Report Added",
                            JOptionPane.INFORMATION_MESSAGE);

                    towerVis.repaint();
                }
            }
        });
        panel.add(pushBtn);

        return panel;
    }

    private String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== FINANCIAL ADVISOR REPORT ===\n");
        report.append("Generated on: ").append(new Date()).append("\n");
        report.append("Advisor: ").append(controller.getCurrentUser().getFullName()).append("\n");
        report.append("================================\n\n");

        report.append("STATISTICS:\n");
        report.append("  Total Clients: ").append(totalClients).append("\n");
        report.append("  Total Appointments: ").append(totalAppointments).append("\n");
        report.append("  Completed: ").append(totalSolved).append("\n");
        report.append("  Failed/Cancelled: ").append(totalFailed).append("\n");
        report.append("  Success Rate: ").append(
                totalAppointments > 0 ? String.format("%.1f%%", (totalSolved * 100.0 / totalAppointments)) : "0%")
                .append("\n\n");

        report.append("PENDING REQUESTS: ").append(consultationRequests.size()).append("\n");
        for (ConsultationRequest request : consultationRequests) {
            report.append("  - ").append(request.getClientName()).append(": ").append(request.getReason()).append("\n");
        }
        report.append("\n");

        report.append("SCHEDULED APPOINTMENTS: ").append(
                scheduledAppointments.stream().filter(a -> "SCHEDULED".equals(a.getStatus())).count()).append("\n");
        for (ConsultationAppointment appointment : scheduledAppointments) {
            if ("SCHEDULED".equals(appointment.getStatus())) {
                report.append("  - ").append(appointment.getClientName())
                        .append(" with ").append(appointment.getAdvisorName())
                        .append(" on ").append(appointment.getAppointmentDate()).append("\n");
            }
        }

        return report.toString();
    }

    private void updateReportsPanel() {
        if (reportsPanel != null) {
            reportsPanel.removeAll();
            reportsPanel.add(createReportsPanel());
            reportsPanel.revalidate();
            reportsPanel.repaint();
        }
    }

    private JPanel createReportsPanel() {
        JPanel container = new JPanel(null);
        container.setOpaque(false);
        container.setBounds(0, 0, reportsWidth, reportsHeight);
        container.setBackground(Color.WHITE);

        JLabel reportsTitle = new JLabel("REPORTS");
        reportsTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        reportsTitle.setForeground(Color.BLACK);
        reportsTitle.setBounds(0, 0, 100, 20);
        container.add(reportsTitle);

        JPanel reportBoxes = new JPanel(new GridLayout(1, 3, 15, 0));
        reportBoxes.setOpaque(false);
        reportBoxes.add(createReportBox("TOTAL CLIENTS", String.valueOf(totalClients), DARK_ORANGE_REPORT));
        reportBoxes.add(createReportBox("TOTAL APPOINTMENTS", String.valueOf(totalAppointments), BLUE_ACCENT));
        reportBoxes.add(createReportBox("SUCCESS RATE",
                totalAppointments > 0 ? String.format("%.1f%%", (totalSolved * 100.0 / totalAppointments)) : "0%",
                RED_REPORT));
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

    private void updateAppointmentSchedule() {
        if (scheduleText != null) {
            StringBuilder sb = new StringBuilder();

            // Get today's scheduled appointments
            Date today = new Date();
            SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = dateOnly.format(today);

            List<ConsultationAppointment> todayAppointments = new ArrayList<>();
            for (ConsultationAppointment appointment : scheduledAppointments) {
                if ("SCHEDULED".equals(appointment.getStatus()) &&
                        appointment.getAppointmentDate().contains(todayStr)) {
                    todayAppointments.add(appointment);
                }
            }

            if (todayAppointments.isEmpty()) {
                sb.append("No appointments scheduled for today.\n\n");
            } else {
                sb.append("Today's Appointments (").append(todayAppointments.size()).append("):\n\n");
                for (ConsultationAppointment appointment : todayAppointments) {
                    sb.append("• ").append(appointment.getClientName())
                            .append(" - ").append(appointment.getAppointmentDate().split(" ")[1]) // Time only
                            .append("\n  Platform: ").append(appointment.getPlatform())
                            .append("\n  Reason: ").append(appointment.getReason())
                            .append("\n\n");
                }
            }

            // Add pending requests count
            sb.append("Pending Requests: ").append(consultationRequests.size());

            scheduleText.setText(sb.toString());
        }
    }

    private JPanel createAppointmentSchedulePanel() {
        RoundedPanel panel = new RoundedPanel(10, Color.WHITE);
        panel.setLayout(null);
        panel.setBounds(0, 0, appointmentWidth, appointmentHeight);

        JLabel title = new JLabel("Appointment Schedule");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBounds(20, 20, 300, 30);
        panel.add(title);

        scheduleText = new JTextArea();
        scheduleText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        scheduleText.setEditable(false);
        scheduleText.setLineWrap(true);
        scheduleText.setWrapStyleWord(true);
        scheduleText.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(scheduleText);
        scrollPane.setBounds(20, 60, appointmentWidth - 40, 350);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane);

        // Refresh button
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBounds(20, 420, 100, 30);
        refreshBtn.addActionListener(e -> updateAppointmentSchedule());
        panel.add(refreshBtn);

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
                " > Dashboard loaded successfully.\n" +
                " > Loaded " + consultationRequests.size() + " pending consultation requests.\n" +
                " > Loaded " + scheduledAppointments.size() + " scheduled appointments.");

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

    private void refreshAllPanels() {
        updateAuxiliaryPanel();
        if (towerVis != null) {
            towerVis.repaint();
        }
        log("All panels refreshed. Stack size: " + clientDebts.size());
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
            super.paintComponent(g);
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

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 24));
            String title = "HANOI DEBT TOWER - ADVISOR VIEW";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, (w - fm.stringWidth(title)) / 2, 40);

            int baseY = h - 60;
            g2.setColor(new Color(150, 80, 150));
            g2.fillRoundRect(50, baseY, w - 100, 15, 10, 10);

            String[] labels = { "CLIENT DEBTS", "AUXILIARY", "PAID-OFF" };
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

            // Draw Client Debts (shows client name and reason) - LIFO Stack
            drawClientDebts(g2, colW / 2, baseY);

            // Draw Auxiliary Debts
            drawAuxiliaryDebts(g2, colW + colW / 2, baseY);

            // Draw Paid-off Debts (includes solved reports)
            drawPaidOffDebts(g2, colW * 2 + colW / 2, baseY);
        }

        private void drawClientDebts(Graphics2D g2, int centerX, int baseY) {
            if (clientDebts.isEmpty())
                return;

            int brickH = 40;
            int gap = 5;
            int currentY = baseY - gap - brickH;

            // Convert stack to list for visualization (TOS on top)
            List<Debt> debtsForDisplay = new ArrayList<>(clientDebts);
            Collections.reverse(debtsForDisplay); // Reverse to show TOS on top

            int totalDebts = Math.min(debtsForDisplay.size(), 6);

            for (int i = 0; i < totalDebts; i++) {
                Debt d = debtsForDisplay.get(i); // TOS is at index 0 after reversal
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                Color c;
                if (i == 0) // TOS
                    c = new Color(220, 53, 69); // Red
                else if (i == 1)
                    c = new Color(253, 126, 20); // Orange
                else
                    c = new Color(255, 193, 7); // Yellow

                g2.setColor(c);

                int width = 250 + ((totalDebts - i - 1) * 20);
                if (i == 0)
                    width += 50; // Make TOS wider

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 15, 15);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));

                // Split the debt name (client - reason) for display
                String[] parts = d.getName().split(" - ", 2);
                String clientName = parts.length > 0 ? parts[0] : d.getName();
                String reason = parts.length > 1 ? parts[1] : "";

                // Draw client name
                String nameText = clientName;
                if (nameText.length() > 20) {
                    nameText = nameText.substring(0, 17) + "...";
                }
                g2.drawString(nameText, centerX - width / 2 + 10, yPos + 15);

                // Draw reason below
                g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
                if (!reason.isEmpty()) {
                    String reasonText = reason;
                    if (reasonText.length() > 25) {
                        reasonText = reasonText.substring(0, 22) + "...";
                    }
                    g2.drawString(reasonText, centerX - width / 2 + 10, yPos + 28);
                }

                // Draw TOS indicator and balance
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                if (i == 0) {
                    // TOS indicator
                    g2.drawString("TOS", centerX - width / 2 + 10, yPos + 38);
                }

                // Draw balance on the right
                String balanceText = "$" + (int) d.getCurrentBalance();
                g2.drawString(balanceText, centerX + width / 2 - 30, yPos + 25);
            }
        }

        private void drawAuxiliaryDebts(Graphics2D g2, int centerX, int baseY) {
            if (auxiliaryDebts.isEmpty())
                return;

            int brickH = 35;
            int gap = 5;
            int currentY = baseY - gap - brickH;

            int totalDebts = Math.min(auxiliaryDebts.size(), 6);

            for (int i = 0; i < totalDebts; i++) {
                Debt d = auxiliaryDebts.get(i); // Already in LIFO order
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                Color c = AUXILIARY_COLOR;
                g2.setColor(c);

                int width = 200 + ((totalDebts - i - 1) * 15);

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 10, 10);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));

                String displayName = d.getName();
                if (displayName.length() > 25) {
                    displayName = displayName.substring(0, 22) + "...";
                }
                g2.drawString(displayName, centerX - width / 2 + 10, yPos + 22);

                // Draw balance on the right
                String balanceText = "$" + (int) d.getCurrentBalance();
                g2.drawString(balanceText, centerX + width / 2 - 25, yPos + 22);
            }
        }

        private void drawPaidOffDebts(Graphics2D g2, int centerX, int baseY) {
            if (paidOffDebts.isEmpty())
                return;

            int brickH = 30;
            int gap = 5;
            int currentY = baseY - gap - brickH;

            int totalDebts = Math.min(paidOffDebts.size(), 8); // Can show more since they're smaller

            for (int i = 0; i < totalDebts; i++) {
                Debt d = paidOffDebts.get(i); // Already in LIFO order
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                // Different color for reports vs regular debts
                Color c;
                if (d.getName().contains(" - ")) {
                    c = SOLVED_COLOR; // Green for solved reports
                } else {
                    c = new Color(100, 200, 100); // Regular green for paid-off
                }
                g2.setColor(c);

                int width = 180 + ((totalDebts - i - 1) * 10);

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 8, 8);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 8));

                String displayName = d.getName();
                if (displayName.length() > 30) {
                    displayName = displayName.substring(0, 27) + "...";
                }
                g2.drawString(displayName, centerX - width / 2 + 5, yPos + 18);

                // Add "SOLVED" indicator for reports
                if (d.getName().contains(" - ")) {
                    g2.setFont(new Font("SansSerif", Font.BOLD, 7));
                    g2.drawString("SOLVED", centerX - width / 2 + 5, yPos + 28);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            new FADashboard(controller).setVisible(true);
        });
    }
}