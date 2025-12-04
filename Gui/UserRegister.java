package Gui;

import javax.swing.*;
import Model.AppController;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class UserRegister extends JFrame {

    // Define fixed dimensions for the frame
    private static final int FRAME_WIDTH = 1250;
    private static final int FRAME_HEIGHT = 850;

    private AppController controller;
    private JRadioButton debtorRadio;
    private JRadioButton advisorRadio;

    public UserRegister() {
        this.controller = new AppController();
        setTitle("HANOI DEBTSTACK User Registration");
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
        // --- SINGLE BACKGROUND PANEL ---
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        backgroundPanel.setLayout(null);
        backgroundPanel.setBackground(Color.WHITE);
        add(backgroundPanel);

        try {
            // --- Background Image (Stretched to fill the entire frame) ---
            ImageIcon backgroundIcon = new ImageIcon("Images/Registerbackground1.png");
            Image backgroundImage = backgroundIcon.getImage().getScaledInstance(
                    FRAME_WIDTH, FRAME_HEIGHT, Image.SCALE_SMOOTH);
            JLabel backgroundLabel = new JLabel(new ImageIcon(backgroundImage));
            backgroundLabel.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
            backgroundPanel.add(backgroundLabel);

        } catch (Exception e) {
            backgroundPanel.setBackground(new Color(210, 80, 0));
            JLabel placeholder = new JLabel("Image Failed to Load", SwingConstants.CENTER);
            placeholder.setBounds(50, 300, 400, 50);
            placeholder.setForeground(Color.WHITE);
            placeholder.setFont(new Font("Arial", Font.BOLD, 18));
            backgroundPanel.add(placeholder);
        }

        JLabel LogInLabel = new JLabel("LogIn");
        LogInLabel.setBounds(FRAME_WIDTH / 2 + 440, 23, 90, 20);
        LogInLabel.setFont(new Font("Arial", Font.ROMAN_BASELINE, 17));
        LogInLabel.setForeground(Color.BLACK);
        backgroundPanel.add(LogInLabel, 0);
        // Add a MouseListener to handle the click event
        LogInLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new Login(controller).setVisible(true);
                dispose();
            }
        });

        JLabel SignupLabel = new JLabel("SignUp");
        SignupLabel.setBounds(FRAME_WIDTH / 2 + 513, 23, 190, 20);
        SignupLabel.setFont(new Font("Arial", Font.BOLD, 17));
        SignupLabel.setForeground(Color.BLACK);
        backgroundPanel.add(SignupLabel, 0);

        // Form components - positioned on the right side
        JLabel fullnameLabel = new JLabel("Full Name:");
        fullnameLabel.setBounds(FRAME_WIDTH / 2 + 160, 205, 250, 20);
        fullnameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        fullnameLabel.setForeground(Color.BLACK);
        backgroundPanel.add(fullnameLabel, 0);

        JTextField fullnameField = new JTextField();
        fullnameField.setBounds(FRAME_WIDTH / 2 + 160, 235, 340, 37);
        fullnameField.setFont(new Font("Arial", Font.BOLD, 15));
        backgroundPanel.add(fullnameField, 0);

        JLabel emailLabel = new JLabel("Email Address: ");
        emailLabel.setBounds(FRAME_WIDTH / 2 + 160, 285, 200, 20);
        emailLabel.setFont(new Font("Arial", Font.BOLD, 15));
        emailLabel.setForeground(Color.BLACK);
        backgroundPanel.add(emailLabel, 0);

        JTextField emailField = new JTextField();
        emailField.setBounds(FRAME_WIDTH / 2 + 160, 315, 340, 37);
        emailField.setFont(new Font("Arial", Font.BOLD, 15));
        backgroundPanel.add(emailField, 0);

        JLabel usernameLabel = new JLabel("UserName:");
        usernameLabel.setBounds(FRAME_WIDTH / 2 + 160, 365, 250, 20);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        usernameLabel.setForeground(Color.BLACK);
        backgroundPanel.add(usernameLabel, 0);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(FRAME_WIDTH / 2 + 160, 395, 340, 37);
        backgroundPanel.add(usernameField, 0);

        JLabel passwordLabel = new JLabel("Create Password: ");
        passwordLabel.setBounds(FRAME_WIDTH / 2 + 160, 445, 200, 20);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 15));
        passwordLabel.setForeground(Color.BLACK);
        backgroundPanel.add(passwordLabel, 0);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(FRAME_WIDTH / 2 + 160, 475, 340, 37);
        backgroundPanel.add(passwordField, 0);

        // User Type Selection
        JLabel userTypeLabel = new JLabel("I am a:");
        userTypeLabel.setBounds(FRAME_WIDTH / 2 + 160, 525, 200, 20);
        userTypeLabel.setFont(new Font("Arial", Font.BOLD, 15));
        userTypeLabel.setForeground(Color.BLACK);
        backgroundPanel.add(userTypeLabel, 0);

        // Radio buttons for user type
        ButtonGroup userTypeGroup = new ButtonGroup();

        debtorRadio = new JRadioButton("Debtor", true);
        debtorRadio.setBounds(FRAME_WIDTH / 2 + 160, 555, 100, 25);
        debtorRadio.setFont(new Font("Arial", Font.PLAIN, 14));
        debtorRadio.setOpaque(false);
        debtorRadio.setFocusPainted(false);

        advisorRadio = new JRadioButton("Financial Advisor");
        advisorRadio.setBounds(FRAME_WIDTH / 2 + 270, 555, 150, 25);
        advisorRadio.setFont(new Font("Arial", Font.PLAIN, 14));
        advisorRadio.setOpaque(false);
        advisorRadio.setFocusPainted(false);

        userTypeGroup.add(debtorRadio);
        userTypeGroup.add(advisorRadio);

        backgroundPanel.add(debtorRadio, 0);
        backgroundPanel.add(advisorRadio, 0);

        // Signup Button
        JButton signupButton = new JButton("SIGNUP");
        signupButton.setBounds(FRAME_WIDTH / 2 + 226, 615, 200, 40);
        signupButton.setBackground(new Color(255, 120, 0));
        signupButton.setForeground(Color.ORANGE);
        signupButton.setFont(new Font("Arial", Font.BOLD, 16));
        signupButton.setFocusPainted(false);
        backgroundPanel.add(signupButton, 0);

        // Already have an account link
        JLabel loginLink = new JLabel("<html>Already have an account? <u>Log In</u></html>", SwingConstants.CENTER);
        loginLink.setBounds(FRAME_WIDTH / 2 + 220, 665, 200, 15);
        loginLink.setFont(new Font("Arial", Font.PLAIN, 11));
        loginLink.setForeground(new Color(255, 120, 0));
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backgroundPanel.add(loginLink, 0);

        // Action Listener for the signup button
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fullName = fullnameField.getText().trim();
                String email = emailField.getText().trim();
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                String userType = debtorRadio.isSelected() ? "DEBTOR" : "ADVISOR";

                // Validation
                if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(UserRegister.this,
                            "Please fill in all fields.", "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!email.contains("@") || !email.contains(".")) {
                    JOptionPane.showMessageDialog(UserRegister.this,
                            "Please enter a valid email address.", "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (password.length() < 6) {
                    JOptionPane.showMessageDialog(UserRegister.this,
                            "Password must be at least 6 characters long.", "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Attempt registration
                boolean success = controller.register(fullName, email, username, password, userType);
                if (success) {
                    JOptionPane.showMessageDialog(UserRegister.this,
                            "Registration successful!\nPlease login with your credentials.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Clear fields
                    fullnameField.setText("");
                    emailField.setText("");
                    usernameField.setText("");
                    passwordField.setText("");

                    // Go to login page (NOT dashboard directly)
                    dispose();
                    new Login(controller).setVisible(true);
                }
            }
        });
    }

    private void openDashboard(String userType) {
        SwingUtilities.invokeLater(() -> {
            if (userType.equals("DEBTOR")) {
                // For debtor, open the UserDashboard directly
                try {
                    // Close registration window
                    dispose();

                    // Open the UserDashboard directly (since user is already logged in via
                    // registration)
                    UserDashboard dashboard = new UserDashboard(controller);
                    dashboard.setVisible(true);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error opening dashboard: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    // Fallback: Open login
                    new Login(controller).setVisible(true);
                }
            } else if (userType.equals("ADVISOR")) {
                // For advisor, also open UserDashboard (or create a separate dashboard)
                try {
                    dispose();
                    UserDashboard dashboard = new UserDashboard(controller);
                    dashboard.setVisible(true);

                    JOptionPane.showMessageDialog(null,
                            "Logged in as Financial Advisor",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error opening dashboard: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    new Login(controller).setVisible(true);
                }
            }
        });
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

    // You might also want to add this method to handle window closing
    @Override
    public void dispose() {
        // Clean up resources if needed
        super.dispose();
    }

    public static void main(String[] args) {
        // Set look and feel for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new UserRegister().setVisible(true));
    }
}