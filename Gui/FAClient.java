package Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

// --- 1. Custom Panel for Background Image ---
/**
 * A JPanel that draws a BufferedImage as its background, scaling it to fill the
 * panel.
 */
class BackgroundPanel extends JPanel {
    private BufferedImage backgroundImage;

    public BackgroundPanel(BufferedImage image) {
        this.backgroundImage = image;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // Draw the background image, scaled to fill the entire panel
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

// --- 2. Main Application Class ---
public class FAClient extends JFrame {

    // Define colors used in the design
    private static final Color PUSH_BUTTON_COLOR = new Color(248, 107, 98);
    private static final Color SIDEBAR_ICON_COLOR = new Color(74, 52, 27);
    private static final Color PRIMARY_PANEL_COLOR = Color.WHITE;

    public FAClient(BufferedImage bgImage) {
        // Set up the main window
        setTitle("Client Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Full Screen Setup: This makes the frame cover the entire screen ---
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // This stops the user from changing the size
        setResizable(false);

        // Get the actual screen size for calculating bounds
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;

        // --- Setup Layered Pane to hold background and content ---
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(screenSize);

        // 1. Create and add the Background Panel to the lowest layer
        BackgroundPanel backgroundPanel = new BackgroundPanel(bgImage);
        backgroundPanel.setBounds(0, 0, width, height);
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        // 2. Create the Content Panel for the UI elements
        JPanel contentPanel = createContentPanel();
        contentPanel.setBounds(0, 0, width, height);
        contentPanel.setOpaque(false); // Make it transparent so background shows through
        layeredPane.add(contentPanel, JLayeredPane.PALETTE_LAYER);

        // Add the layered pane to the frame
        add(layeredPane);
    }

    // --- 3. Method to create the main content layout ---
    private JPanel createContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        // --- Top Bar (Search) ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(30, 180, 20, 20));

        // Create the Search bar
        JTextField searchField = new JTextField("Search here");
        searchField.setPreferredSize(new Dimension(500, 40));
        searchField.setMaximumSize(new Dimension(500, 40));
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        searchField.setFont(new Font("Arial", Font.PLAIN, 16));
        searchField.setForeground(Color.GRAY);

        // Make the search bar look like the image
        JPanel searchBarPanel = new JPanel(new BorderLayout(5, 0));
        searchBarPanel.setBackground(Color.WHITE);
        searchBarPanel.setMaximumSize(new Dimension(500, 40));
        searchBarPanel.add(new JLabel("  🔍"), BorderLayout.WEST);
        searchBarPanel.add(searchField, BorderLayout.CENTER);

        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(searchBarPanel);
        topPanel.add(Box.createHorizontalStrut(500));

        // --- Side Bar (Navigation) ---
        JPanel sidePanel = createSidebar();

        // --- Center and Right Content Area ---
        JPanel centerAndRightPanel = new JPanel(new GridBagLayout());
        centerAndRightPanel.setOpaque(false);
        centerAndRightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        // Left Column (Consultation Appointment & Operation Logs)
        gbc.gridx = 0;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        centerAndRightPanel.add(createLeftContentColumn(), gbc);

        // Right Column (Create Report & Appointment Schedule)
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        centerAndRightPanel.add(createRightContentColumn(), gbc);

        // --- Assemble Main Panel ---
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(sidePanel, BorderLayout.WEST);
        mainPanel.add(centerAndRightPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    // --- 4. Method to create the Navigation Sidebar ---
    private JPanel createSidebar() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setOpaque(false);
        sidePanel.setPreferredSize(new Dimension(100, 600));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));

        // Logo/Text
        JLabel logo = new JLabel("H E N O i");
        logo.setFont(new Font("Arial", Font.BOLD, 18));
        logo.setForeground(SIDEBAR_ICON_COLOR);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidePanel.add(logo);
        sidePanel.add(Box.createVerticalStrut(50));

        // Navigation items
        sidePanel.add(createSidebarButton("DASH BOARD", "▣"));
        sidePanel.add(createSidebarButton("CLIENT", "👤"));
        sidePanel.add(createSidebarButton("HISTORY", "🕒"));
        sidePanel.add(createSidebarButton("PROFILE", "👨"));

        return sidePanel;
    }

    // Helper for creating sidebar buttons
    private JPanel createSidebarButton(String text, String icon) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 28));
        iconLabel.setForeground(SIDEBAR_ICON_COLOR);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Text
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        textLabel.setForeground(SIDEBAR_ICON_COLOR);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(iconLabel);
        panel.add(textLabel);

        return panel;
    }

    // --- 5. Method to create the left column content ---
    private JPanel createLeftContentColumn() {
        JPanel leftColumn = new JPanel(new GridBagLayout());
        leftColumn.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // 1. Consultation Appointment Panel
        gbc.gridy = 0;
        gbc.weighty = 0.6;
        leftColumn.add(createConsultationPanel(), gbc);

        // 2. Operation Logs Panel
        gbc.gridy = 1;
        gbc.weighty = 0.4;
        gbc.insets = new Insets(20, 0, 0, 0);
        leftColumn.add(createOperationLogsPanel(), gbc);

        return leftColumn;
    }

    // --- 6. Method to create the right column content ---
    private JPanel createRightContentColumn() {
        JPanel rightColumn = new JPanel(new GridBagLayout());
        rightColumn.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // 1. Create Report Panel
        gbc.gridy = 0;
        gbc.weighty = 0.4;
        rightColumn.add(createCreateReportPanel(), gbc);

        // 2. Appointment Schedule Panel
        gbc.gridy = 1;
        gbc.weighty = 0.6;
        gbc.insets = new Insets(20, 0, 0, 0);
        rightColumn.add(createSchedulePanel(), gbc);

        return rightColumn;
    }

    // --- 7. Panel Creation Helpers ---

    private JPanel createConsultationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PRIMARY_PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel title = new JLabel("Client Consultation Appointment");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        // Content (Maria's Request)
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel requestText = new JLabel("Maria request a Debt Consultation.");
        requestText.setFont(new Font("Arial", Font.PLAIN, 14));
        content.add(requestText, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttons.setOpaque(false);

        // Add button
        JButton addButton = new JButton("Add");
        addButton.setForeground(new Color(60, 179, 113));
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);

        // Reject button
        JButton rejectButton = new JButton("Reject");
        rejectButton.setForeground(new Color(255, 69, 0));
        rejectButton.setFont(new Font("Arial", Font.BOLD, 14));
        rejectButton.setBorderPainted(false);
        rejectButton.setFocusPainted(false);

        buttons.add(addButton);
        buttons.add(rejectButton);

        content.add(buttons, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
        centerPanel.add(content, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOperationLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("OPERATION LOGS");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(title, BorderLayout.NORTH);

        // Logs content
        JTextArea logArea = new JTextArea();
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GRAY);
        logArea.setEditable(false);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCreateReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PRIMARY_PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Create Report");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        // Text Fields area
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        // 4 placeholder text fields
        for (int i = 0; i < 4; i++) {
            JTextField field = new JTextField();
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            fieldsPanel.add(field);
            fieldsPanel.add(Box.createVerticalStrut(15));
        }

        // Push Button
        JButton pushButton = new JButton("PUSH");
        pushButton.setBackground(PUSH_BUTTON_COLOR);
        pushButton.setForeground(Color.WHITE);
        pushButton.setFont(new Font("Arial", Font.BOLD, 16));
        pushButton.setBorderPainted(false);
        pushButton.setFocusPainted(false);
        pushButton.setMinimumSize(new Dimension(100, 50));
        pushButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(pushButton, BorderLayout.SOUTH);

        panel.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PRIMARY_PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Appointment Schedule");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        // Schedule list
        JPanel scheduleList = new JPanel();
        scheduleList.setLayout(new BoxLayout(scheduleList, BoxLayout.Y_AXIS));
        scheduleList.setOpaque(false);

        // 8 placeholder lines
        for (int i = 0; i < 8; i++) {
            scheduleList.add(Box.createVerticalStrut(5));
            JLabel rowLabel = new JLabel("  " + (i + 1) + ". Appointment Placeholder");
            rowLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            rowLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
            scheduleList.add(rowLabel);
            scheduleList.add(Box.createVerticalStrut(5));
        }

        panel.add(scheduleList, BorderLayout.CENTER);

        return panel;
    }

    // --- 8. Main method to run the application ---
    public static void main(String[] args) {
        // Load the background image safely
        BufferedImage bgImage = null;
        try {
            // Your image file path: Images/DashboardMainBackground.png
            bgImage = ImageIO.read(new File("Images/DashboardMainBackground.png"));
        } catch (IOException e) {
            System.err.println("Error loading background image. Check your file path: " + e.getMessage());
            // Fallback image if the file is not found
            bgImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }

        final BufferedImage finalBgImage = bgImage;

        // Start the GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            FAClient frame = new FAClient(finalBgImage);
            frame.setVisible(true);
        });
    }
}   