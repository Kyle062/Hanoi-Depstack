package Gui;

import Model.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FADashboard extends JFrame {
    private AppController controller;
    private Stack<Debt> clientDebts = new Stack<>();
    private Stack<Debt> auxiliaryDebts = new Stack<>();
    private Stack<Debt> paidOffDebts = new Stack<>();

    private JLayeredPane layeredPane;
    private JPanel mainLayer;
    private JLabel backgroundLabel;
    private JPanel towerContainer;
    private TowerVisualizationPanel towerVis;
    private JTextArea logsArea;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    // New components for analytics and right panel
    private JPanel analyticsPanel;
    private JPanel rightSidePanel;
    private JLabel totalReportsLabel;
    private JLabel solvedReportsLabel;
    private JLabel totalAppointmentsLabel;
    private JLabel totalClientsLabel;
    private JTextArea appointmentScheduleArea;

    // Manual report fields
    private JTextField reportClientNameField;
    private JTextField reportTypeField;
    private JTextField reportAmountField;
    private JTextField reportInterestField;
    private JTextField reportMinPaymentField;

    public FADashboard(AppController controller) {
        this.controller = controller;

        setTitle("Hanoi Debt Tower Dashboard - Financial Advisor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set a reasonable default size instead of just maximized
        setSize(1400, 900);
        setLocationRelativeTo(null); // Center the window

        initializeSampleDebtData();
        initUI();

        // Make sure window is visible
        setVisible(true);

        // Maximize after UI is created
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void initializeSampleDebtData() {
        clientDebts.push(new Debt("John Doe - Credit Card", 5000.00, 18.5, 100.00));
        clientDebts.push(new Debt("Sarah Smith - Student Loan", 15000.00, 6.8, 200.00));
        clientDebts.push(new Debt("Mike Johnson - Car Loan", 10000.00, 5.5, 250.00));
        paidOffDebts.push(new Debt("Robert Brown - Personal Loan", 0.00, 0.0, 0.00));
        paidOffDebts.push(new Debt("Emma Wilson - Medical Bill", 0.00, 0.0, 0.00));
    }

    private void initUI() {
        // Use BorderLayout for the frame
        setLayout(new BorderLayout());

        // Create layered pane
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null); // Use absolute positioning
        add(layeredPane, BorderLayout.CENTER);

        setupBackground();

        mainLayer = new JPanel(null);
        mainLayer.setOpaque(false);
        mainLayer.setBounds(0, 0, 1920, 1080);
        layeredPane.add(mainLayer, JLayeredPane.PALETTE_LAYER);

        createSidebar();
        createMainContent();
        createAnalyticsPanel(); // New analytics panel
        createRightSidePanel(); // New right-side panel

        // Load appointments for display
        loadAppointmentSchedule();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.saveAllData();
            }
        });
    }

    private void setupBackground() {
        try {
            File imageFile = new File("Images/DashboardMainBackground.png");
            BufferedImage bg;
            if (imageFile.exists()) {
                bg = ImageIO.read(imageFile);
            } else {
                // Create a simple gradient background as fallback
                bg = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = bg.createGraphics();
                GradientPaint gradient = new GradientPaint(0, 0, new Color(60, 40, 30),
                        1920, 1080, new Color(40, 20, 10));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, 1920, 1080);
                g2d.dispose();
            }

            backgroundLabel = new JLabel(new ImageIcon(bg)) {
                @Override
                protected void paintComponent(Graphics g) {
                    g.drawImage(((ImageIcon) getIcon()).getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            };
            backgroundLabel.setBounds(0, 0, 1920, 1080);
        } catch (IOException e) {
            System.err.println("Error loading background image: " + e.getMessage());
            backgroundLabel = new JLabel();
            backgroundLabel.setOpaque(true);
            backgroundLabel.setBackground(new Color(60, 40, 30));
            backgroundLabel.setBounds(0, 0, 1920, 1080);
        }
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);
    }

    private void createSidebar() {
        // Adjust these values to change sidebar position and size
        int sidebarX = 20;
        int sidebarY = 200;
        int sidebarWidth = 155;
        int sidebarHeight = 500;

        RoundedPanel sidebarContainer = new RoundedPanel(15, new Color(40, 40, 40, 200));
        sidebarContainer.setBounds(sidebarX, sidebarY, sidebarWidth, sidebarHeight);
        sidebarContainer.setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel hanoi = new JLabel("HANOI");
        hanoi.setForeground(Color.WHITE);
        hanoi.setFont(new Font("SansSerif", Font.BOLD, 16));
        hanoi.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        hanoi.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(hanoi);
        sidebar.add(Box.createVerticalStrut(20));

        // Sidebar buttons with improved error handling
        sidebar.add(createSidebarButton("Dashboard", "Refresh Dashboard", e -> {
            try {
                refreshDashboard();
            } catch (Exception ex) {
                showError("Error refreshing dashboard", ex);
            }
        }));
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createSidebarButton("Client Requests", "View Client Consultation Requests", e -> {
            try {
                showClientConsultationRequests();
            } catch (Exception ex) {
                showError("Error showing client requests", ex);
            }
        }));
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createSidebarButton("Show TOS", "Show Top of Stack Details", e -> {
            try {
                showCurrentReport();
            } catch (Exception ex) {
                showError("Error showing TOS", ex);
            }
        }));
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createSidebarButton("To Auxiliary", "Move TOS to Auxiliary", e -> {
            try {
                moveToAuxiliary();
            } catch (Exception ex) {
                showError("Error moving to auxiliary", ex);
            }
        }));
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createSidebarButton("Solve Report", "Mark report as solved", e -> {
            try {
                solveReport();
            } catch (Exception ex) {
                showError("Error solving report", ex);
            }
        }));
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createSidebarButton("Profile", "User Profile", e -> {
            try {
                showProfile();
            } catch (Exception ex) {
                showError("Error showing profile", ex);
            }
        }));
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createSidebarButton("Logout", "Logout from system", e -> {
            try {
                logout();
            } catch (Exception ex) {
                showError("Error during logout", ex);
            }
        }));

        sidebar.add(Box.createVerticalGlue());
        sidebarContainer.add(sidebar, BorderLayout.NORTH);
        mainLayer.add(sidebarContainer);
    }

    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(this,
                message + ": " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    private JButton createSidebarButton(String text, String tooltip, ActionListener action) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(130, 40));
        button.setBackground(new Color(241, 122, 80));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.addActionListener(action);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 140, 100));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(241, 122, 80));
            }
        });

        return button;
    }

    private void createMainContent() {
        // Tower Visualization - Adjust these values to change position and size
        int towerX = 180;
        int towerY = 30;
        int towerWidth = 1300; // Reduced width to make room for right panel
        int towerHeight = 620;

        towerContainer = new RoundedPanel(20, Color.WHITE);
        towerContainer.setLayout(new BorderLayout());
        towerContainer.setBounds(towerX, towerY, towerWidth, towerHeight);

        towerVis = new TowerVisualizationPanel(clientDebts, auxiliaryDebts, paidOffDebts);
        towerContainer.add(towerVis, BorderLayout.CENTER);
        mainLayer.add(towerContainer);

        // Logs Panel - Adjust these values to change position and size
        int logsX = 180;
        int logsY = 830;
        int logsWidth = 1300;
        int logsHeight = 150;

        JPanel logContainer = new JPanel(new BorderLayout());
        logContainer.setBackground(Color.BLACK);
        logContainer.setBounds(logsX, logsY, logsWidth, logsHeight);

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
        logsArea.setText(" > Financial Advisor Dashboard Initialized\n" +
                " > Ready to handle client consultation requests\n" +
                " > Client debt stack loaded: " + clientDebts.size() + " items\n" +
                " > Logged in as: "
                + (controller.getCurrentUser() != null ? controller.getCurrentUser().getFullName() : "Unknown"));

        JScrollPane logsScrollPane = new JScrollPane(logsArea);
        logsScrollPane.setBorder(null);
        logsScrollPane.setBackground(Color.BLACK);
        logsScrollPane.getViewport().setBackground(Color.BLACK);
        logContainer.add(logsScrollPane, BorderLayout.CENTER);
        mainLayer.add(logContainer);

        // Add a welcome message
        log("Welcome to Financial Advisor Dashboard!");
        log("Current time: " + new Date());
    }

    private void createAnalyticsPanel() {
        // Position analytics panel below logs panel
        int analyticsX = 180;
        int analyticsY = 670;
        int analyticsWidth = 1300;
        int analyticsHeight = 120;

        analyticsPanel = new RoundedPanel(15, new Color(40, 40, 40, 200));
        analyticsPanel.setBounds(analyticsX, analyticsY, analyticsWidth, analyticsHeight);
        analyticsPanel.setLayout(new GridLayout(1, 4, 10, 0));
        analyticsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create the stat panels and store references to the value labels
        StatPanelData totalReportsData = createStatPanelWithData("Total Reports",
                String.valueOf(clientDebts.size() + auxiliaryDebts.size() + paidOffDebts.size()),
                new Color(52, 152, 219));
        totalReportsLabel = totalReportsData.valueLabel;
        analyticsPanel.add(totalReportsData.panel);

        StatPanelData solvedReportsData = createStatPanelWithData("Solved Reports",
                String.valueOf(paidOffDebts.size()), new Color(46, 204, 113));
        solvedReportsLabel = solvedReportsData.valueLabel;
        analyticsPanel.add(solvedReportsData.panel);

        StatPanelData totalAppointmentsData = createStatPanelWithData("Appointments", "0",
                new Color(155, 89, 182));
        totalAppointmentsLabel = totalAppointmentsData.valueLabel;
        analyticsPanel.add(totalAppointmentsData.panel);

        StatPanelData totalClientsData = createStatPanelWithData("Active Clients",
                String.valueOf(getUniqueClientCount()), new Color(241, 196, 15));
        totalClientsLabel = totalClientsData.valueLabel;
        analyticsPanel.add(totalClientsData.panel);

        mainLayer.add(analyticsPanel);

        // Update appointment count
        updateAnalytics();
    }

    // Helper class to return both panel and label reference
    private StatPanelData createStatPanelWithData(String title, String value, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(color);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(5));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(5));

        return new StatPanelData(panel, valueLabel);
    }

    // Simple data class to hold panel and label reference
    private class StatPanelData {
        JPanel panel;
        JLabel valueLabel;

        StatPanelData(JPanel panel, JLabel valueLabel) {
            this.panel = panel;
            this.valueLabel = valueLabel;
        }
    }

    private void createRightSidePanel() {
        // Right side panel - positioned to the right of tower
        int rightPanelX = 1500; // 180 + 900 + 20
        int rightPanelY = 30;
        int rightPanelWidth = 400;
        int rightPanelHeight = 500; // 30 + 620 + 270 (analytics height)

        rightSidePanel = new RoundedPanel(20, Color.WHITE);
        rightSidePanel.setBounds(rightPanelX, rightPanelY, rightPanelWidth, rightPanelHeight);
        rightSidePanel.setLayout(new BorderLayout(0, 10));

        // Create tabbed pane for right panel
        JTabbedPane rightTabs = new JTabbedPane();
        rightTabs.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Tab 1: Manual Report Creation
        JPanel createReportPanel = new JPanel();
        createReportPanel.setLayout(new BoxLayout(createReportPanel, BoxLayout.Y_AXIS));
        createReportPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel createReportTitle = new JLabel("Create Report Manually");
        createReportTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        createReportTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        createReportPanel.add(createReportTitle);
        createReportPanel.add(Box.createVerticalStrut(10));

        // Form fields for manual report creation
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 1, 5, 5));

        // Client Name
        JPanel clientNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel clientNameLabel = new JLabel("Client Name:");
        clientNameLabel.setPreferredSize(new Dimension(120, 25));
        reportClientNameField = new JTextField(20);
        clientNamePanel.add(clientNameLabel);
        clientNamePanel.add(reportClientNameField);
        formPanel.add(clientNamePanel);

        // Report Type
        JPanel reportTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel reportTypeLabel = new JLabel("Report Type:");
        reportTypeLabel.setPreferredSize(new Dimension(120, 25));
        reportTypeField = new JTextField(20);
        reportTypePanel.add(reportTypeLabel);
        reportTypePanel.add(reportTypeField);
        formPanel.add(reportTypePanel);

        // Amount
        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel amountLabel = new JLabel("Amount ($):");
        amountLabel.setPreferredSize(new Dimension(120, 25));
        reportAmountField = new JTextField(20);
        amountPanel.add(amountLabel);
        amountPanel.add(reportAmountField);
        formPanel.add(amountPanel);

        // Interest Rate
        JPanel interestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel interestLabel = new JLabel("Interest Rate (%):");
        interestLabel.setPreferredSize(new Dimension(120, 25));
        reportInterestField = new JTextField(20);
        interestPanel.add(interestLabel);
        interestPanel.add(reportInterestField);
        formPanel.add(interestPanel);

        // Minimum Payment
        JPanel minPaymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel minPaymentLabel = new JLabel("Min Payment ($):");
        minPaymentLabel.setPreferredSize(new Dimension(120, 25));
        reportMinPaymentField = new JTextField(20);
        minPaymentPanel.add(minPaymentLabel);
        minPaymentPanel.add(reportMinPaymentField);
        formPanel.add(minPaymentPanel);

        createReportPanel.add(formPanel);
        createReportPanel.add(Box.createVerticalStrut(10));

        // Create Report Button
        JButton createReportButton = new JButton("Create Report");
        createReportButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createReportButton.setBackground(new Color(46, 204, 113));
        createReportButton.setForeground(Color.WHITE);
        createReportButton.setFocusPainted(false);
        createReportButton.setBorderPainted(false);
        createReportButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        createReportButton.setPreferredSize(new Dimension(200, 40));
        createReportButton.addActionListener(e -> createManualReport());

        createReportPanel.add(createReportButton);
        createReportPanel.add(Box.createVerticalStrut(10));

        // Info label
        JLabel infoLabel = new JLabel(
                "<html><center>Report will be added to the<br>top of the client debt stack (TOS)</center></html>");
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        createReportPanel.add(infoLabel);

        JScrollPane createReportScroll = new JScrollPane(createReportPanel);
        rightTabs.addTab("Create Report", createReportScroll);

        // Tab 2: Appointment Schedule
        JPanel appointmentPanel = new JPanel(new BorderLayout());
        appointmentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel appointmentTitle = new JLabel("Appointment Schedule");
        appointmentTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        appointmentTitle.setHorizontalAlignment(SwingConstants.CENTER);
        appointmentPanel.add(appointmentTitle, BorderLayout.NORTH);

        appointmentScheduleArea = new JTextArea();
        appointmentScheduleArea.setEditable(false);
        appointmentScheduleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        appointmentScheduleArea.setLineWrap(true);
        appointmentScheduleArea.setWrapStyleWord(true);
        appointmentScheduleArea.setText("Loading appointments...");

        JScrollPane appointmentScroll = new JScrollPane(appointmentScheduleArea);
        appointmentPanel.add(appointmentScroll, BorderLayout.CENTER);

        JButton refreshAppointmentsButton = new JButton("Refresh Appointments");
        refreshAppointmentsButton.addActionListener(e -> loadAppointmentSchedule());
        appointmentPanel.add(refreshAppointmentsButton, BorderLayout.SOUTH);

        rightTabs.addTab("Appointments", appointmentPanel);

        rightSidePanel.add(rightTabs, BorderLayout.CENTER);
        mainLayer.add(rightSidePanel);
    }

    private void createManualReport() {
        String clientName = reportClientNameField.getText().trim();
        String reportType = reportTypeField.getText().trim();
        String amountText = reportAmountField.getText().trim();
        String interestText = reportInterestField.getText().trim();
        String minPaymentText = reportMinPaymentField.getText().trim();

        // Validation
        if (clientName.isEmpty() || reportType.isEmpty() ||
                amountText.isEmpty() || interestText.isEmpty() || minPaymentText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount, interest, minPayment;
        try {
            amount = Double.parseDouble(amountText);
            interest = Double.parseDouble(interestText);
            minPayment = Double.parseDouble(minPaymentText);

            if (amount <= 0 || interest < 0 || minPayment <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for amount, interest, and minimum payment.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create the debt
        String debtName = clientName + " - " + reportType;
        Debt newDebt = new Debt(debtName, amount, interest, minPayment);

        // Add to TOS (top of client debt stack)
        clientDebts.push(newDebt);

        // Update UI
        refreshTowerVisualization();
        updateAnalytics();

        // Log the action
        log("CREATED MANUAL REPORT: " + debtName + " ($" + String.format("%.2f", amount) + ") added to TOS");

        // Show success message
        JOptionPane.showMessageDialog(this,
                "Report created successfully!\n" +
                        "Client: " + clientName + "\n" +
                        "Type: " + reportType + "\n" +
                        "Amount: $" + String.format("%.2f", amount) + "\n" +
                        "Added to TOS position",
                "Report Created",
                JOptionPane.INFORMATION_MESSAGE);

        // Clear fields
        reportClientNameField.setText("");
        reportTypeField.setText("");
        reportAmountField.setText("");
        reportInterestField.setText("");
        reportMinPaymentField.setText("");
    }

    private void loadAppointmentSchedule() {
        try {
            ArrayList<ConsultationAppointment> appointments = DataManager.loadScheduledAppointments();
            String currentUsername = controller.getCurrentUsername();

            if (currentUsername == null) {
                appointmentScheduleArea.setText("Not logged in.");
                return;
            }

            StringBuilder scheduleText = new StringBuilder();
            scheduleText.append("Your Appointment Schedule:\n");
            scheduleText.append("===========================\n\n");

            int yourAppointments = 0;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");

            for (ConsultationAppointment appointment : appointments) {
                if (appointment.getAdvisorUsername().equals(currentUsername)) {
                    yourAppointments++;
                    scheduleText.append("Client: " + appointment.getClientName() + "\n");
                    scheduleText.append("Request: " + appointment.getReason() + "\n");
                    scheduleText.append("Platform: " + appointment.getPlatform() + "\n");
                    scheduleText.append("Date: " + appointment.getAppointmentDate() + "\n");
                    scheduleText.append("Status: " + appointment.getStatus() + "\n");
                    scheduleText.append("------------------------------------\n");
                }
            }

            if (yourAppointments == 0) {
                scheduleText.append("No scheduled appointments.\n");
                scheduleText.append("Check 'Client Requests' for pending requests.");
            } else {
                scheduleText.append("\nTotal Appointments: " + yourAppointments);
            }

            appointmentScheduleArea.setText(scheduleText.toString());
            updateAnalytics();

        } catch (Exception e) {
            appointmentScheduleArea.setText("Error loading appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateAnalytics() {
        // Update reports count
        int totalReports = clientDebts.size() + auxiliaryDebts.size() + paidOffDebts.size();
        totalReportsLabel.setText(String.valueOf(totalReports));

        // Update solved reports count
        solvedReportsLabel.setText(String.valueOf(paidOffDebts.size()));

        // Update appointments count
        try {
            ArrayList<ConsultationAppointment> appointments = DataManager.loadScheduledAppointments();
            String currentUsername = controller.getCurrentUsername();
            int yourAppointments = 0;

            if (currentUsername != null) {
                for (ConsultationAppointment appointment : appointments) {
                    if (appointment.getAdvisorUsername().equals(currentUsername)) {
                        yourAppointments++;
                    }
                }
            }

            totalAppointmentsLabel.setText(String.valueOf(yourAppointments));
        } catch (Exception e) {
            totalAppointmentsLabel.setText("0");
        }

        // Update unique clients count
        totalClientsLabel.setText(String.valueOf(getUniqueClientCount()));
    }

    private int getUniqueClientCount() {
        Set<String> uniqueClients = new HashSet<>();

        // Extract client names from debts
        for (Debt debt : clientDebts) {
            String[] parts = debt.getName().split(" - ");
            if (parts.length > 0) {
                uniqueClients.add(parts[0]);
            }
        }

        for (Debt debt : auxiliaryDebts) {
            String[] parts = debt.getName().split(" - ");
            if (parts.length > 0) {
                uniqueClients.add(parts[0]);
            }
        }

        for (Debt debt : paidOffDebts) {
            String[] parts = debt.getName().split(" - ");
            if (parts.length > 0) {
                uniqueClients.add(parts[0]);
            }
        }

        return uniqueClients.size();
    }

    private void refreshDashboard() {
        if (towerVis != null) {
            towerVis.repaint();
        }
        updateAnalytics();
        log("Dashboard refreshed at: " + new Date());
    }

    private void showClientConsultationRequests() {
        try {
            ArrayList<ConsultationRequest> requests = DataManager.loadConsultationRequests();
            log("Loaded " + requests.size() + " consultation requests from storage");

            ArrayList<ConsultationRequest> myRequests = new ArrayList<>();
            String currentUsername = controller.getCurrentUsername();

            if (currentUsername == null) {
                JOptionPane.showMessageDialog(this, "Not logged in!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (ConsultationRequest request : requests) {
                if (request.getAdvisorUsername().equals(currentUsername)) {
                    myRequests.add(request);
                }
            }

            if (myRequests.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No pending consultation requests.",
                        "Client Requests",
                        JOptionPane.INFORMATION_MESSAGE);
                log("No pending consultation requests found");
                return;
            }

            JDialog requestDialog = new JDialog(this, "Client Consultation Requests", true);
            requestDialog.setSize(800, 500);
            requestDialog.setLocationRelativeTo(this);
            requestDialog.setLayout(new BorderLayout());

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel title = new JLabel("Client Consultation Requests (" + myRequests.size() + ")");
            title.setFont(new Font("SansSerif", Font.BOLD, 16));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(title);
            mainPanel.add(Box.createVerticalStrut(10));

            for (ConsultationRequest request : myRequests) {
                mainPanel.add(createRequestPanel(request, requestDialog));
                mainPanel.add(Box.createVerticalStrut(10));
            }

            JScrollPane scrollPane = new JScrollPane(mainPanel);
            scrollPane.setBorder(null);
            requestDialog.add(scrollPane, BorderLayout.CENTER);
            requestDialog.setVisible(true);

            log("Showing " + myRequests.size() + " consultation requests");

            // Refresh appointments after handling requests
            loadAppointmentSchedule();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading consultation requests: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createRequestPanel(ConsultationRequest request, JDialog parentDialog) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 2, 2));

        JLabel clientLabel = new JLabel("Client: " + request.getClientName() +
                " (" + request.getClientUsername() + ")");
        clientLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel reasonLabel = new JLabel("Reason: " + request.getReason());
        reasonLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JLabel platformLabel = new JLabel("Platform: " + request.getPlatform());
        platformLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JLabel dateLabel = new JLabel("Requested: " +
                new SimpleDateFormat("MMM dd, yyyy").format(request.getRequestDate()));
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JLabel statusLabel = new JLabel("Status: " + request.getStatus());
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusLabel.setForeground(request.getStatus().equals("PENDING") ? Color.ORANGE
                : request.getStatus().equals("SCHEDULED") ? Color.GREEN : Color.RED);

        infoPanel.add(clientLabel);
        infoPanel.add(reasonLabel);
        infoPanel.add(platformLabel);
        infoPanel.add(dateLabel);
        infoPanel.add(statusLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        if (request.getStatus().equals("PENDING")) {
            JButton confirmBtn = new JButton("Confirm Schedule");
            confirmBtn.setBackground(new Color(0, 150, 0));
            confirmBtn.setForeground(Color.WHITE);
            confirmBtn.setFocusPainted(false);
            confirmBtn.setBorderPainted(false);
            confirmBtn.addActionListener(e -> confirmSchedule(request, parentDialog));

            JButton rejectBtn = new JButton("Reject");
            rejectBtn.setBackground(Color.RED);
            rejectBtn.setForeground(Color.WHITE);
            rejectBtn.setFocusPainted(false);
            rejectBtn.setBorderPainted(false);
            rejectBtn.addActionListener(e -> rejectRequest(request, parentDialog));

            buttonGroup.add(confirmBtn);
            buttonGroup.add(rejectBtn);
        } else {
            JLabel actionLabel = new JLabel("Request " + request.getStatus().toLowerCase());
            actionLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
            actionLabel.setForeground(Color.GRAY);
            buttonGroup.add(actionLabel);
        }

        panel.add(buttonGroup, BorderLayout.EAST);
        return panel;
    }

    private void confirmSchedule(ConsultationRequest request, JDialog parentDialog) {
        try {
            request.setStatus("SCHEDULED");

            ConsultationAppointment appointment = new ConsultationAppointment(
                    request.getClientUsername(),
                    request.getClientName(),
                    request.getAdvisorUsername(),
                    request.getAdvisorName(),
                    request.getReason(),
                    request.getPlatform(),
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                    new Date(),
                    "SCHEDULED");

            // Save the appointment
            ArrayList<ConsultationAppointment> appointments = DataManager.loadScheduledAppointments();
            appointments.add(appointment);
            DataManager.saveScheduledAppointments(appointments);

            // Get all consultation requests
            ArrayList<ConsultationRequest> allRequests = DataManager.loadConsultationRequests();

            // Find and remove the specific request
            ConsultationRequest requestToRemove = null;
            for (ConsultationRequest req : allRequests) {
                if (req.getClientUsername().equals(request.getClientUsername()) &&
                        req.getAdvisorUsername().equals(request.getAdvisorUsername()) &&
                        req.getReason().equals(request.getReason()) &&
                        req.getRequestDate().equals(request.getRequestDate())) {
                    requestToRemove = req;
                    break;
                }
            }

            if (requestToRemove != null) {
                allRequests.remove(requestToRemove);
            }

            // Save the updated requests list
            DataManager.saveConsultationRequests(allRequests);

            // Update client's personal request list
            DataManager.deleteConsultationRequest(request);

            // Add consultation fee to client debts
            String debtName = request.getClientName() + " - Consultation (" + request.getReason() + ")";
            Debt consultationDebt = new Debt(debtName, 150.00, 0.0, 150.00);
            clientDebts.push(consultationDebt);

            log("SCHEDULED: Consultation with " + request.getClientName() +
                    " - Added to TOS: " + debtName);

            JOptionPane.showMessageDialog(parentDialog,
                    "Appointment scheduled successfully!\n" +
                            "Consultation fee ($150.00) added to TOS.\n" +
                            "Client has been notified.",
                    "Appointment Scheduled",
                    JOptionPane.INFORMATION_MESSAGE);

            refreshTowerVisualization();
            parentDialog.dispose();
            showClientConsultationRequests(); // This will refresh the list

            // Update analytics and appointments
            updateAnalytics();
            loadAppointmentSchedule();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentDialog,
                    "Error scheduling appointment: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void rejectRequest(ConsultationRequest request, JDialog parentDialog) {
        int confirm = JOptionPane.showConfirmDialog(parentDialog,
                "Are you sure you want to reject " + request.getClientName() + "'s request?",
                "Confirm Rejection", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                request.setStatus("REJECTED");

                // Get all consultation requests
                ArrayList<ConsultationRequest> allRequests = DataManager.loadConsultationRequests();

                // Find and remove the specific request
                ConsultationRequest requestToRemove = null;
                for (ConsultationRequest req : allRequests) {
                    if (req.getClientUsername().equals(request.getClientUsername()) &&
                            req.getAdvisorUsername().equals(request.getAdvisorUsername()) &&
                            req.getReason().equals(request.getReason()) &&
                            req.getRequestDate().equals(request.getRequestDate())) {
                        requestToRemove = req;
                        break;
                    }
                }

                if (requestToRemove != null) {
                    allRequests.remove(requestToRemove);
                }

                // Save the updated requests list
                DataManager.saveConsultationRequests(allRequests);

                // Set status first
                request.setStatus("REJECTED");
                // Then call deleteConsultationRequest which will update both lists
                DataManager.deleteConsultationRequest(request);

                log("REJECTED: Consultation request from " + request.getClientName());

                JOptionPane.showMessageDialog(parentDialog,
                        "Request rejected. Client has been notified.",
                        "Request Rejected",
                        JOptionPane.INFORMATION_MESSAGE);

                parentDialog.dispose();
                showClientConsultationRequests(); // This will refresh the list

                // Update appointments
                loadAppointmentSchedule();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(parentDialog,
                        "Error rejecting request: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void updateClientRequestStatus(String clientUsername, ConsultationRequest originalRequest,
            String newStatus) {
        try {
            ArrayList<ConsultationRequest> clientRequests = DataManager.loadClientRequests(clientUsername);

            for (int i = 0; i < clientRequests.size(); i++) {
                ConsultationRequest cr = clientRequests.get(i);
                if (cr.getClientUsername().equals(originalRequest.getClientUsername()) &&
                        cr.getAdvisorUsername().equals(originalRequest.getAdvisorUsername()) &&
                        cr.getReason().equals(originalRequest.getReason())) {

                    cr.setStatus(newStatus);
                    break;
                }
            }

            DataManager.saveClientRequests(clientUsername, clientRequests);
        } catch (Exception e) {
            System.err.println("Error updating client request status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showCurrentReport() {
        if (!clientDebts.isEmpty()) {
            Debt topDebt = clientDebts.peek();

            JOptionPane.showMessageDialog(this,
                    "Current TOS Report Details:\n" +
                            "=====================\n" +
                            "Client/Report: " + topDebt.getName() + "\n" +
                            "Current Balance: $" + String.format("%.2f", topDebt.getCurrentBalance()) + "\n" +
                            "Original Amount: $" + String.format("%.2f", topDebt.getOriginalAmount()) + "\n" +
                            "Interest Rate: " + topDebt.getInterestRate() + "%\n" +
                            "Minimum Payment: $" + String.format("%.2f", topDebt.getMinimumPayment()) + "\n" +
                            "Position: TOS (Top of Stack)\n" +
                            "Stack Size: " + clientDebts.size() + " reports\n" +
                            "=====================",
                    "Current TOS Report",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No active reports in stack!\n" +
                            "The client debt stack is empty.",
                    "No Reports Available",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void moveToAuxiliary() {
        if (!clientDebts.isEmpty()) {
            Debt topDebt = clientDebts.peek();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Move TOS to auxiliary?\n" +
                            "Client: " + topDebt.getName() + "\n" +
                            "Balance: $" + String.format("%.2f", topDebt.getCurrentBalance()) + "\n" +
                            "Position: TOS (Top of Stack)",
                    "Confirm Move to Auxiliary",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Debt movedDebt = clientDebts.pop();
                auxiliaryDebts.push(movedDebt);

                log("MOVED: " + movedDebt.getName() + " from TOS to auxiliary (LIFO)");
                JOptionPane.showMessageDialog(this,
                        "Successfully moved to auxiliary:\n" +
                                movedDebt.getName() + "\n" +
                                "Balance: $" + String.format("%.2f", movedDebt.getCurrentBalance()) + "\n" +
                                "New TOS: " + (clientDebts.isEmpty() ? "None" : clientDebts.peek().getName()),
                        "Moved to Auxiliary",
                        JOptionPane.INFORMATION_MESSAGE);

                refreshTowerVisualization();
                updateAnalytics();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No active debt to move!\n" +
                            "Client debt stack is empty.",
                    "No TOS Available",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void solveReport() {
        if (!clientDebts.isEmpty()) {
            Debt topReport = clientDebts.peek();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Mark this report as SOLVED?\n" +
                            "Report: " + topReport.getName() + "\n" +
                            "Balance: $" + String.format("%.2f", topReport.getCurrentBalance()) + "\n" +
                            "Position: TOS (Top of Stack)\n\n" +
                            "This will also move all auxiliary debts back to client debts pillar.",
                    "Confirm Solve Report",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Debt solvedReport = clientDebts.pop();
                solvedReport.makePayment(solvedReport.getCurrentBalance());
                paidOffDebts.push(solvedReport);

                int movedCount = 0;
                Stack<Debt> tempStack = new Stack<>();

                while (!auxiliaryDebts.isEmpty()) {
                    tempStack.push(auxiliaryDebts.pop());
                }

                while (!tempStack.isEmpty()) {
                    Debt auxDebt = tempStack.pop();
                    clientDebts.push(auxDebt);
                    movedCount++;
                }

                log("SOLVED: Report marked as solved - " + solvedReport.getName());
                if (movedCount > 0) {
                    log("MOVED: " + movedCount + " auxiliary debts back to client debts pillar");
                }

                JOptionPane.showMessageDialog(this,
                        "Report marked as SOLVED:\n" +
                                solvedReport.getName() + "\n" +
                                "Moved to Paid-Off section\n" +
                                movedCount + " auxiliary debts moved back to client debts pillar\n" +
                                "New TOS: " + (clientDebts.isEmpty() ? "None" : clientDebts.peek().getName()),
                        "Report Solved",
                        JOptionPane.INFORMATION_MESSAGE);

                refreshTowerVisualization();
                updateAnalytics();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No reports to solve!\n" +
                            "Client debt stack is empty.",
                    "No Reports Available",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showProfile() {
        User currentUser = controller.getCurrentUser();
        if (currentUser != null) {
            JOptionPane.showMessageDialog(this,
                    "Financial Advisor Profile:\n" +
                            "Name: " + currentUser.getFullName() + "\n" +
                            "Username: " + controller.getCurrentUsername() + "\n" +
                            "Email: " + currentUser.getEmail() + "\n" +
                            "User Type: " + currentUser.getUserType() + "\n" +
                            "Active Reports: " + clientDebts.size() + "\n" +
                            "Auxiliary Reports: " + auxiliaryDebts.size() + "\n" +
                            "Solved Reports: " + paidOffDebts.size() + "\n" +
                            "Total Unique Clients: " + getUniqueClientCount(),
                    "User Profile",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "User information not available!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
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
            SwingUtilities.invokeLater(() -> {
                try {
                    new Login(controller).setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void refreshTowerVisualization() {
        if (towerVis != null) {
            towerVis.repaint();
        }
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
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
    }

    private class TowerVisualizationPanel extends JPanel {
        private Stack<Debt> clientDebts;
        private Stack<Debt> auxiliaryDebts;
        private Stack<Debt> paidOffDebts;

        public TowerVisualizationPanel(Stack<Debt> clientDebts, Stack<Debt> auxiliaryDebts, Stack<Debt> paidOffDebts) {
            this.clientDebts = clientDebts;
            this.auxiliaryDebts = auxiliaryDebts;
            this.paidOffDebts = paidOffDebts;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Draw background
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillRoundRect(0, 0, w, h, 20, 20);

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

            g2.setColor(Color.BLACK);
            for (int i = 0; i < 3; i++) {
                int cx = colW * i + colW / 2;
                g2.fillRoundRect(cx - 10, 80, 15, baseY - 80, 10, 10);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                String lbl = labels[i];
                g2.drawString(lbl, cx - fm.stringWidth(lbl) / 2 + 30, baseY + 30);
            }

            drawClientDebts(g2, colW / 2, baseY);
            drawAuxiliaryDebts(g2, colW + colW / 2, baseY);
            drawPaidOffDebts(g2, colW * 2 + colW / 2, baseY);
        }

        private void drawClientDebts(Graphics2D g2, int centerX, int baseY) {
            if (clientDebts.isEmpty()) {
                g2.setColor(Color.LIGHT_GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
                g2.drawString("No Client Debts", centerX - 50, baseY - 100);
                return;
            }

            int brickH = 50;
            int gap = 5;
            Debt[] debtsArray = clientDebts.toArray(new Debt[0]);

            for (int i = 0; i < Math.min(debtsArray.length, 6); i++) {
                Debt d = debtsArray[i];
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                Color c;
                if (i == 0)
                    c = new Color(220, 53, 69);
                else if (i == 1)
                    c = new Color(253, 126, 20);
                else
                    c = new Color(255, 193, 7);

                g2.setColor(c);
                int width = 250 + ((debtsArray.length - i - 1) * 20);
                if (i == 0)
                    width += 50;

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 15, 15);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));

                String[] parts = d.getName().split(" - ", 2);
                String clientName = parts.length > 0 ? parts[0] : d.getName();
                String reportType = parts.length > 1 ? parts[1] : d.getName();

                String nameText = clientName;
                if (nameText.length() > 20)
                    nameText = nameText.substring(0, 17) + "...";
                g2.drawString(nameText, centerX - width / 2 + 10, yPos + 15);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                String typeText = reportType;
                if (typeText.length() > 25)
                    typeText = typeText.substring(0, 22) + "...";
                g2.drawString(typeText, centerX - width / 2 + 10, yPos + 28);

                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                if (i == 0)
                    g2.drawString("", centerX - width / 2 + 10, yPos + 38);

                String balanceText = "$" + (int) d.getCurrentBalance();
                g2.drawString(balanceText, centerX + width / 2 - 50, yPos + 25);
            }
        }

        private void drawAuxiliaryDebts(Graphics2D g2, int centerX, int baseY) {
            if (auxiliaryDebts.isEmpty()) {
                g2.setColor(Color.LIGHT_GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
                g2.drawString("No Auxiliary Debts", centerX - 50, baseY - 100);
                return;
            }

            int brickH = 50;
            int gap = 5;
            Debt[] debtsArray = auxiliaryDebts.toArray(new Debt[0]);

            for (int i = 0; i < Math.min(debtsArray.length, 6); i++) { // FIXED: changed debitsArray to debtsArray
                Debt d = debtsArray[i];
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                Color c = new Color(150, 150, 200);
                g2.setColor(c);
                int width = 200 + ((debtsArray.length - i - 1) * 15 + 20);

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));

                String displayName = d.getName();
                if (displayName.length() > 25)
                    displayName = displayName.substring(0, 22) + "...";
                g2.drawString(displayName, centerX - width / 2 + 10, yPos + 22);

                String balanceText = "$" + (int) d.getCurrentBalance();
                g2.drawString(balanceText, centerX + width / 2 - 50, yPos + 22);
            }
        }

        private void drawPaidOffDebts(Graphics2D g2, int centerX, int baseY) {
            if (paidOffDebts.isEmpty()) {
                g2.setColor(Color.LIGHT_GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
                g2.drawString("No Paid-off Debts", centerX - 50, baseY - 100);
                return;
            }

            int brickH = 50;
            int gap = 5;
            Debt[] debtsArray = paidOffDebts.toArray(new Debt[0]);

            for (int i = 0; i < Math.min(debtsArray.length, 8); i++) {
                Debt d = debtsArray[i];
                int yPos = baseY - gap - brickH - (i * (brickH + gap));

                Color c = new Color(100, 200, 100);
                g2.setColor(c);
                int width = 180 + ((debtsArray.length - i - 1) * 10 + 20);

                g2.fillRoundRect(centerX - width / 2, yPos, width, brickH, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));

                String displayName = d.getName();
                if (displayName.length() > 30)
                    displayName = displayName.substring(0, 27) + "...";
                g2.drawString(displayName, centerX - width / 2 + 5, yPos + 18);

                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2.drawString("SOLVED", centerX - width / 2 + 5, yPos + 28 + 10);
            }
        }
    }

    public static void main(String[] args) {
        // Test the FADashboard directly
        SwingUtilities.invokeLater(() -> {
            try {
                AppController controller = new AppController();
                // Test login
                if (controller.login("advisor1", "password123")) {
                    System.out.println("Login successful!");
                    new FADashboard(controller).setVisible(true);
                } else {
                    System.out.println("Login failed. Creating new dashboard anyway for testing...");
                    new FADashboard(controller).setVisible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}