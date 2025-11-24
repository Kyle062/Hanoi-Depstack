package Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class DebtStackUserLoginUI extends JFrame {

    // Define fixed dimensions for the frame
    private static final int FRAME_WIDTH = 1300;
    private static final int FRAME_HEIGHT = 900;
    private static final int LEFT_PANEL_WIDTH = 650;
    private static final int RIGHT_PANEL_WIDTH = 650;

    // Components for the right panel
    private JTextField userIdField;
    private JPasswordField passwordField;

    public DebtStackUserLoginUI() {
        setTitle("HENOI DEBTSTACK User Login");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Required: Use null layout
        setLayout(null);

        // Call the method to set up all UI components
        setupComplexUI();

        // Center the frame on the screen
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupComplexUI() {
        // --- 1. LEFT PANEL (Aesthetics & Marketing) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setBounds(0, 0, LEFT_PANEL_WIDTH, FRAME_HEIGHT);
        leftPanel.setLayout(null);
        add(leftPanel);

        // --- 2. RIGHT PANEL (Login Form) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setBounds(LEFT_PANEL_WIDTH, 0, RIGHT_PANEL_WIDTH, FRAME_HEIGHT);
        rightPanel.setLayout(null);
        rightPanel.setBackground(Color.WHITE);
        add(rightPanel);

        try {
            // --- A. Background Image (Stretched to fill the panel) ---
            ImageIcon backgroundIcon = new ImageIcon("Images/leftPanelBackground.png");
            Image backgroundImage = backgroundIcon.getImage().getScaledInstance(
                    LEFT_PANEL_WIDTH, FRAME_HEIGHT, Image.SCALE_SMOOTH);
            JLabel backgroundLabel = new JLabel(new ImageIcon(backgroundImage));
            backgroundLabel.setBounds(0, 0, LEFT_PANEL_WIDTH, FRAME_HEIGHT);
            leftPanel.add(backgroundLabel);
            ImageIcon centerIcon = new ImageIcon("Images/leftPaneHumans.png");
            int centerImageWidth = 500;
            int centerImageHeight = 525;
            Image centerImage = centerIcon.getImage().getScaledInstance(
                    centerImageWidth, centerImageHeight, Image.SCALE_SMOOTH);
            JLabel centerImageLabel = new JLabel(new ImageIcon(centerImage));
            int xPos = (LEFT_PANEL_WIDTH - centerImageWidth) / 2;
            int yPos = 100;
            centerImageLabel.setBounds(xPos, yPos - 50, centerImageWidth, centerImageHeight);

            ImageIcon logo2 = new ImageIcon("Images/logo2Right.png");
            int centerLogoWidth = 250;
            int centerLogoHeight = 150;
            Image centerLogo = logo2.getImage().getScaledInstance(
                    centerLogoWidth, centerLogoHeight, Image.SCALE_SMOOTH);
            JLabel centerLogoLabel = new JLabel(new ImageIcon(centerLogo));
            centerLogoLabel.setBounds(220, 40, centerLogoWidth, centerLogoHeight);
            rightPanel.add(centerLogoLabel, 0);
            leftPanel.add(centerImageLabel, 0);

        } catch (Exception e) {
            leftPanel.setBackground(new Color(210, 80, 0));
            JLabel placeholder = new JLabel("Image Failed to Load", SwingConstants.CENTER);
            placeholder.setBounds(50, 300, 400, 50);
            placeholder.setForeground(Color.WHITE);
            placeholder.setFont(new Font("Arial", Font.BOLD, 18));
            leftPanel.add(placeholder);
        }

        // Title Text (Added AFTER the image elements, so it appears on top)
        JLabel title = new JLabel("Take control of your financial future with intelligent debt \n management solutions",
                SwingConstants.LEFT);
        title.setBounds(15, 625, 700, 30);
        title.setForeground(Color.BLACK);
        title.setFont(new Font("Sans-Serif", Font.PLAIN, 18));
        leftPanel.add(title, 0);

        JLabel title2 = new JLabel("management solutions", SwingConstants.CENTER);
        title2.setBounds(20, 470, 460, 20);
        title2.setForeground(Color.WHITE);
        title2.setFont(new Font("Arial", Font.BOLD, 14));
        leftPanel.add(title2);

        // Bullet points (simplified with JLabels, added on top of background)
        addBulletPoint(leftPanel, "Smart debt consolidation strategies", 680);
        addBulletPoint(leftPanel, "Automated payment scheduling", 705);
        addBulletPoint(leftPanel, "Real-time financial insights", 730);

        // Welcome Text
        JLabel welcome = new JLabel("<html>Welcome to Optimal Debt <br>&nbsp;&nbsp;&nbsp;&nbsp;Mangement System</html>",
                SwingConstants.CENTER);
        welcome.setBounds(100, 190, 500, 60);
        welcome.setFont(new Font("Arial", Font.BOLD, 25));
        rightPanel.add(welcome);

        JLabel signInMsg = new JLabel(
                "<html>Please Sign up to begin your journey in debt management <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dashboard</html>",
                SwingConstants.CENTER);
        signInMsg.setBounds(145, 245, 400, 60);
        signInMsg.setFont(new Font("Arial", Font.PLAIN, 15));
        signInMsg.setForeground(Color.BLACK);
        rightPanel.add(signInMsg);

        // UserID Field
        JLabel fullnameLabel = new JLabel("Full Name:");
        fullnameLabel.setBounds(175, 310, 250, 20);
        fullnameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        rightPanel.add(fullnameLabel);

        JTextField fullnameField = new JTextField();
        fullnameField.setBounds(175, 335, 340, 40);
        fullnameField.setFont(new Font("Arial", Font.BOLD, 15));
        rightPanel.add(fullnameField);

        // Password Field
        JLabel ageLabel = new JLabel("Age: ");
        ageLabel.setBounds(175, 390, 200, 20);
        ageLabel.setFont(new Font("Arial", Font.BOLD, 15));
        rightPanel.add(ageLabel);

        JTextField ageField = new JTextField();
        ageField.setBounds(175, 415, 340, 40);
        ageField.setFont(new Font("Arial", Font.BOLD, 15));
        rightPanel.add(ageField);

        JLabel purposeLabel = new JLabel("Purpose of Usage:"); 
        purposeLabel.setBounds(175, 465, 250, 20);
        purposeLabel.setFont(new Font("Arial", Font.BOLD, 15));
        rightPanel.add(purposeLabel);

        JTextArea purposeArea = new JTextArea();
        purposeArea.setLineWrap(true);
        purposeArea.setWrapStyleWord(true); 
        purposeArea.setFont(new Font("Arial", Font.BOLD, 15));

        JScrollPane purposeScrollPane = new JScrollPane(purposeArea);
        purposeScrollPane.setBounds(175, 490, 340, 50); 
        rightPanel.add(purposeScrollPane);



        JLabel passwordLabel = new JLabel("Create Password: ");
        passwordLabel.setBounds(175, 550, 200, 20);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 15));
        rightPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(175, 575, 340, 40);
        rightPanel.add(passwordField);

        // Forgot Password Link
        JLabel forgotPass = new JLabel("<html><u>Forgot Password?</u></html>", SwingConstants.RIGHT);
        forgotPass.setBounds(215, 620, 300, 15);
        forgotPass.setForeground(new Color(255, 120, 0));
        forgotPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(forgotPass);

        // Login Button
        JButton loginButton = new JButton("LOGIN");
        loginButton.setBounds(250, 670, 200, 40);
        loginButton.setBackground(new Color(255, 120, 0)); // Orange button
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        rightPanel.add(loginButton);

        // Don't have an account link
        JLabel signUp = new JLabel("<html>Don't have account? <u>Sign Up</u></html>", SwingConstants.CENTER);
        signUp.setBounds(255, 720, 200, 15);
        signUp.setFont(new Font("Arial", Font.PLAIN, 11));
        signUp.setForeground(new Color(255, 120, 0));
        signUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(signUp);

        // Example Action Listener for the login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Dummy login logic
                String user = userIdField.getText();
                char[] pass = passwordField.getPassword();
                String message = "Attempting to log in with UserID: " + user;
                JOptionPane.showMessageDialog(DebtStackUserLoginUI.this, message, "Login Info",
                        JOptionPane.INFORMATION_MESSAGE);
                // Clear password field for security
                passwordField.setText("");
            }
        });
    }

    private void addBulletPoint(JPanel panel, String text, int yPos) {
        JLabel bullet = new JLabel("<html>&bull;</html>");
        bullet.setBounds(190, yPos, 20, 20);
        bullet.setForeground(Color.WHITE);
        bullet.setFont(new Font("Arial", Font.PLAIN, 40));
        panel.add(bullet, 0);

        JLabel label = new JLabel(text);
        label.setBounds(220, yPos, 400, 20);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(label, 0);
    }

    public void exportPhoto(String filename) {
        try {
            // Get the content pane's size
            Rectangle rect = this.getBounds();
            // Create a BufferedImage to capture the frame content
            BufferedImage image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            this.paint(g); // Paint the entire frame onto the buffered image

            // Write the image to a file
            File outputFile = new File(filename + ".png");
            ImageIO.write(image, "png", outputFile);
            System.out.println("Screenshot exported successfully to: " + outputFile.getAbsolutePath());

            // Optional: Show success message (using a custom dialog instead of alert)
            JOptionPane.showMessageDialog(this, "The UI screenshot was saved as " + outputFile.getName(),
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting screenshot: " + ex.getMessage(), "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new DebtStackUserLoginUI().setVisible(true);
    }
}
