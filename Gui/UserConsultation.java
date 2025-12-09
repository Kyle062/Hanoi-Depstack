package Gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.border.EmptyBorder;

public class UserConsultation extends JFrame {

    private BufferedImage backgroundImage;
    private BackgroundPanel mainPanel;
    private JPanel sidebar;
    private JPanel formCard;
    private JPanel operationLogPanel;
    private JPanel searchPanel;

    /**
     * Custom JPanel to draw the background image.
     */
    private class BackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;

        public BackgroundPanel(BufferedImage bgImage) {
            this.backgroundImage = bgImage;
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // Scale image to fit the panel
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(240, 248, 255));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    public UserConsultation() {
        // Configure frame for full screen
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Start maximized
        setResizable(true);

        // Load background image
        try {
            // *Note: You must ensure this file path is correct in your project structure*
            File imageFile = new File("Images/DashboardMainBackground.png");
            if (!imageFile.exists()) {
                imageFile = new File("src/Images/DashboardMainBackground.png");
            }
            backgroundImage = ImageIO.read(imageFile);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load background image: " + e.getMessage());
            // Create a placeholder image if loading fails
            backgroundImage = createPlaceholderBackground(new Dimension(1920, 1080));
        }

        setTitle("Consultation Appointment");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create main panel
        mainPanel = new BackgroundPanel(backgroundImage);
        mainPanel.setLayout(new BorderLayout());

        // Create components
        createComponents();

        // Add components to main panel
        mainPanel.add(sidebar, BorderLayout.WEST);

        // Create content area
        JPanel contentArea = new JPanel();
        contentArea.setOpaque(false);
        contentArea.setLayout(new BorderLayout(0, 20));

        // Top search panel
        JPanel topSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 40));
        topSearchPanel.setOpaque(false);
        topSearchPanel.add(searchPanel);
        contentArea.add(topSearchPanel, BorderLayout.NORTH);

        // Center content
        JPanel centerContent = new JPanel(new GridBagLayout());
        centerContent.setOpaque(false);
        centerContent.add(formCard);
        contentArea.add(centerContent, BorderLayout.CENTER);

        // Bottom log panel
        JPanel bottomLogPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 20));
        bottomLogPanel.setOpaque(false);
        bottomLogPanel.add(operationLogPanel);
        contentArea.add(bottomLogPanel, BorderLayout.SOUTH);

        mainPanel.add(contentArea, BorderLayout.CENTER);

        // Add escape key listener to close the application
        setupKeyboardShortcuts();

        // Add component listener for resizing
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustComponentSizes();
            }
        });

        setContentPane(mainPanel);

        // Initial adjustment of component sizes
        SwingUtilities.invokeLater(() -> adjustComponentSizes());
    }

    // Placeholder image creation logic to simulate the background if the file isn't
    // found
    private BufferedImage createPlaceholderBackground(Dimension size) {
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(new Color(110, 70, 30)); // Dark brown base
        g2d.fillRect(0, 0, size.width, size.height);
        g2d.setColor(new Color(160, 100, 50, 150));
        for (int i = 0; i < 50; i++) {
            g2d.fillOval((int) (Math.random() * size.width), (int) (Math.random() * size.height), 200, 500);
        }
        g2d.dispose();
        return img;
    }

    private void createComponents() {
        sidebar = createSidebar();
        searchPanel = createSearchBar();
        formCard = createConsultationForm();
        operationLogPanel = createOperationLogPanel();
    }

    private void adjustComponentSizes() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Adjust sidebar width (percentage of screen width)
        int sidebarWidth = (int) (screenWidth * 0.08);
        sidebarWidth = Math.max(80, Math.min(sidebarWidth, 120));
        sidebar.setPreferredSize(new Dimension(sidebarWidth, screenHeight));

        // Adjust form card size
        int formWidth = (int) (screenWidth * 0.35);
        int formHeight = (int) (screenHeight * 0.55);
        formWidth = Math.max(400, Math.min(formWidth, 600));
        formHeight = Math.max(300, Math.min(formHeight, 550));
        formCard.setPreferredSize(new Dimension(formWidth, formHeight));

        // Adjust search bar width
        int searchWidth = (int) (screenWidth * 0.5);
        searchWidth = Math.max(400, Math.min(searchWidth, 800));
        searchPanel.setPreferredSize(new Dimension(searchWidth, 50));

        // Adjust operation log panel
        int logWidth = (int) (screenWidth * 0.55);
        int logHeight = (int) (screenHeight * 0.15);
        logWidth = Math.max(500, Math.min(logWidth, 1000));
        logHeight = Math.max(80, Math.min(logHeight, 150));
        operationLogPanel.setPreferredSize(new Dimension(logWidth, logHeight));

        // Update font sizes based on screen resolution
        updateFontSizes(screenWidth);

        // Revalidate and repaint
        sidebar.revalidate();
        formCard.revalidate();
        operationLogPanel.revalidate();
        searchPanel.revalidate();
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void updateFontSizes(int screenWidth) {
        float baseFontSize = screenWidth > 1920 ? 14 : 12;

        // Update sidebar fonts
        Component[] sidebarComponents = sidebar.getComponents();
        for (Component comp : sidebarComponents) {
            if (comp instanceof JButton) {
                // Adjusting sidebar button font size within HTML tag in createSidebarButton
            }
        }

        // Update form card fonts
        updateComponentFonts(formCard, baseFontSize);

        // Update search panel font
        Component[] searchComponents = searchPanel.getComponents();
        for (Component comp : searchComponents) {
            if (comp instanceof JTextField) {
                Font currentFont = comp.getFont();
                comp.setFont(currentFont.deriveFont(baseFontSize));
            }
        }
    }

    private void updateComponentFonts(Container container, float baseSize) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                Font currentFont = comp.getFont();
                if (currentFont.getStyle() == Font.BOLD && currentFont.getSize() > 16) {
                    comp.setFont(currentFont.deriveFont(baseSize + 6));
                } else {
                    comp.setFont(currentFont.deriveFont(baseSize));
                }
            } else if (comp instanceof JTextField) {
                Font currentFont = comp.getFont();
                comp.setFont(currentFont.deriveFont(baseSize));
            } else if (comp instanceof JButton) {
                Font currentFont = comp.getFont();
                comp.setFont(currentFont.deriveFont(baseSize + 2));
            } else if (comp instanceof Container) {
                updateComponentFonts((Container) comp, baseSize);
            }
        }
    }

    private void setupKeyboardShortcuts() {
        KeyStroke escapeKey = KeyStroke.getKeyStroke("ESCAPE");
        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mainPanel.getActionMap();

        inputMap.put(escapeKey, "closeApplication");
        actionMap.put("closeApplication", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        UserConsultation.this,
                        "Are you sure you want to exit?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        KeyStroke fullscreenKey = KeyStroke.getKeyStroke("F11");
        inputMap.put(fullscreenKey, "toggleFullscreen");
        actionMap.put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setExtendedState(getExtendedState() ^ JFrame.MAXIMIZED_BOTH);
            }
        });
    }

    // --- Component Creation Methods ---

    /**
     * Implements the sidebar design from the picture (transparent, centered
     * buttons, specific icons).
     */
    private JPanel createSidebar() {
        // Use a container to hold the sidebar and manage its top padding
        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setOpaque(false);
        wrapperPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 150)); // Center alignment, 150px top space
        wrapperPanel.setPreferredSize(new Dimension(100, 0)); // Give it a max width

        JPanel sidebar = new JPanel();
        // Use BoxLayout for vertical stacking
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Add buttons using the helper method. Icons are replaced with Unicode/emojis.
        // Dashboard
        sidebar.add(createSidebarButton("DASHBOARD", "\u2689", e -> System.out.println("Dashboard clicked")));
        sidebar.add(Box.createVerticalStrut(20));
        // Peek
        sidebar.add(createSidebarButton("PEEK", "\uD83D\uDC41", e -> System.out.println("PEEK clicked")));
        sidebar.add(Box.createVerticalStrut(20));
        // Settle Payment
        sidebar.add(createSidebarButton("SETTLE\nPAYMENT", "\uD83D\uDCB3", e -> System.out.println("Settle Payment clicked")));
        sidebar.add(Box.createVerticalStrut(20));
        // History
        sidebar.add(createSidebarButton("HISTORY", "\u231B", e -> System.out.println("History clicked")));
        sidebar.add(Box.createVerticalStrut(20));
        // Delete
        sidebar.add(createSidebarButton("DELETE", "\u274C", e -> System.out.println("Delete clicked")));
        sidebar.add(Box.createVerticalStrut(20));
        // Profile
        sidebar.add(createSidebarButton("PROFILE", "\uD83D\uDC64", e -> System.out.println("Profile clicked")));

        wrapperPanel.add(sidebar);
        return wrapperPanel;
    }

    /**
     * Styled button to look like an icon over text (no background fill) as seen in
     * the image.
     */
    private JButton createSidebarButton(String text, String icon, ActionListener action) {
        // Use HTML to stack the large icon above the small, bold, and uppercase text
        String displayIcon = "<div style='font-size:24px;'>" + icon + "</div>";
        String displayText = "<div style='font-size:9px; font-weight:bold;'>" + text.toUpperCase().replace("\n", "<br>")
                + "</div>";

        JButton button = new JButton("<html><center>" + displayIcon + displayText + "</center></html>");

        // Set the color to white/off-white (based on the image)
        button.setForeground(Color.WHITE); 
        
        // Remove all default styling
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setContentAreaFilled(false); // No background fill by default
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(10, 5, 10, 5));
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Center button alignment

        // Hover effect to simulate selection (small semi-transparent white box)
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                // Slight gray background on hover
                button.setBackground(new Color(255, 255, 255, 30));
                button.setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setContentAreaFilled(false);
            }
        });

        button.addActionListener(action);
        return button;
    }

    private JPanel createSearchBar() {
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));

        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                // Remove LineBorder to match the image's clean, rounded look (simulated by
                // rounded corners in formCard but here we stick to simple rectangular)
                BorderFactory.createEmptyBorder(5, 15, 5, 15),
                BorderFactory.createLineBorder(new Color(180, 180, 180), 0))); // Removed border to match picture

        JLabel searchIcon = new JLabel("\uD83D\uDD0D");
        searchIcon.setFont(new Font("Arial", Font.PLAIN, 16));
        searchIcon.setForeground(Color.GRAY);

        JTextField searchField = new JTextField();
        searchField.setBorder(null);
        searchField.setBackground(Color.WHITE);
        searchField.setFont(new Font("Arial", Font.PLAIN, 16));

        searchField.setText("Search here...");
        searchField.setForeground(Color.GRAY);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("Search here...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search here...");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        return searchPanel;
    }

    private JPanel createConsultationForm() {
        JPanel formCard = new JPanel();
        // Slightly off-white background with transparency to match the card in the image
        formCard.setBackground(new Color(255, 255, 255, 230)); 
        formCard.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25)); // Inner padding
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridwidth = 1;

        // Title
        JLabel title = new JLabel("Consultation Appointment");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(50, 50, 50));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        formCard.add(title, gbc);

        gbc.insets = new Insets(10, 0, 2, 0);

        // Reason for Consultation
        gbc.gridy++;
        JLabel reasonLabel = new JLabel("Reason for Consultation:");
        reasonLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        reasonLabel.setForeground(new Color(50, 50, 50));
        formCard.add(reasonLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(2, 0, 15, 0);
        JTextField reasonField = createSimpleTextField();
        formCard.add(reasonField, gbc);

        // Choose available Financial Advisor
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 2, 0);
        JLabel advisorLabel = new JLabel("Choose available Financial Advisor:");
        advisorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        advisorLabel.setForeground(new Color(50, 50, 50));
        formCard.add(advisorLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(2, 0, 15, 0);
        JTextField advisorField = createSimpleTextField();
        formCard.add(advisorField, gbc);

        // Platform to use
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 2, 0);
        JLabel platformLabel = new JLabel("Platform to use:");
        platformLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        platformLabel.setForeground(new Color(50, 50, 50));
        formCard.add(platformLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(2, 0, 15, 0);
        JTextField platformField = createSimpleTextField();
        formCard.add(platformField, gbc);

        // Date for Consultation
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 2, 0);
        JLabel dateLabel = new JLabel("Date for Consultation:");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(50, 50, 50));
        formCard.add(dateLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(2, 0, 30, 0);
        JTextField dateField = createSimpleTextField();
        formCard.add(dateField, gbc);

        // Spacer to push the button down
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        formCard.add(Box.createVerticalGlue(), gbc);

        // Request Button - styled to match the image's green text button
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.EAST;

        JButton requestButton = new JButton("Request Consultation");
        requestButton.setForeground(new Color(34, 139, 34)); // Match the green color
        requestButton.setFont(new Font("Arial", Font.PLAIN, 14));
        requestButton.setBorderPainted(false);
        requestButton.setFocusPainted(false);
        requestButton.setContentAreaFilled(false);
        requestButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        requestButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                requestButton.setText("<html><u>Request Consultation</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                requestButton.setText("Request Consultation");
            }
        });

        formCard.add(requestButton, gbc);

        return formCard;
    }

    private JTextField createSimpleTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        textField.setBackground(Color.WHITE);
        textField.setPreferredSize(new Dimension(300, 35));
        return textField;
    }

    private JPanel createOperationLogPanel() {
        JPanel logPanel = new JPanel(new BorderLayout(0, 5));
        logPanel.setBackground(new Color(20, 20, 20, 220));
        logPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // Corrected title to match the image: "OPERATION LOGS" in white
        JLabel logTitle = new JLabel("OPERATION LOGS"); 
        logTitle.setForeground(new Color(255, 255, 255)); // White text
        logTitle.setFont(new Font("Arial", Font.BOLD, 14));

        JTextArea logArea = new JTextArea();
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(220, 220, 220));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setText(
                "> " + new java.util.Date() + " - Operation data feed started...\n" +
                        "> " + new java.util.Date() + " - Calculating system metrics...\n" +
                        "> " + new java.util.Date() + " - System integrity check: OK\n" +
                        "> " + new java.util.Date() + " - Ready for user input");
        logArea.setCaretPosition(logArea.getDocument().getLength());

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(80, 80, 80);
                this.trackColor = new Color(40, 40, 40);
            }
        });

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(logTitle, BorderLayout.WEST);

        logPanel.add(titlePanel, BorderLayout.NORTH);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        return logPanel;
    }

    // Removed the incorrectly styled createIconButton method.

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Make dialogs transparent
                UIManager.put("OptionPane.background", new Color(255, 255, 255, 200));
                UIManager.put("Panel.background", new Color(255, 255, 255, 200));
            } catch (Exception e) {
                e.printStackTrace();
            }

            UserConsultation frame = new UserConsultation();
            frame.setVisible(true);
        });
    }
}