package Gui;

import javax.swing.*;
import Model.AppController;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Login extends JFrame {

    private AppController controller;

    // Define fixed dimensions for the frame
    private static final int FRAME_WIDTH = 1250;
    private static final int FRAME_HEIGHT = 850;

    // Components for the login form
    private JTextField userIdField;
    private JPasswordField passwordField;

    public Login(AppController controller) {
        this.controller = controller;
        setTitle("HANOI DEBTSTACK Login");
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

    public void setupComplexUI() {
        // --- SINGLE BACKGROUND PANEL ---
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        backgroundPanel.setLayout(null);
        backgroundPanel.setBackground(Color.WHITE);
        add(backgroundPanel);

        try {
            // --- Background Image (Stretched to fill the entire frame) ---
            ImageIcon backgroundIcon = new ImageIcon("Images/Loginbackground.png");
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
        LogInLabel.setBounds(FRAME_WIDTH / 2 + 440, 23, 190, 20);
        LogInLabel.setFont(new Font("Arial", Font.BOLD, 17));
        LogInLabel.setForeground(Color.BLACK);
        backgroundPanel.add(LogInLabel, 0);

        JLabel SignupLabel = new JLabel("SignUp");
        SignupLabel.setBounds(FRAME_WIDTH / 2 + 513, 23, 190, 20);
        SignupLabel.setFont(new Font("Arial", Font.BOLD, 17));
        SignupLabel.setForeground(Color.WHITE);
        backgroundPanel.add(SignupLabel, 0);
        // Add a MouseListener to handle the click event
        SignupLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new UserRegister().setVisible(true);
                dispose();
            }
        });

        // UserID Field on right side
        JLabel userLabel = new JLabel("UserID/ UserName:");
        userLabel.setBounds(FRAME_WIDTH / 2 + -339, 320, 190, 20);
        userLabel.setFont(new Font("Arial", Font.BOLD, 15));
        userLabel.setForeground(Color.BLACK);
        backgroundPanel.add(userLabel, 0);

        userIdField = new JTextField();
        userIdField.setBounds(FRAME_WIDTH / 2 + -339, 350, 320, 37);
        userIdField.setFont(new Font("Arial", Font.BOLD, 15));
        backgroundPanel.add(userIdField, 0);

        // Password Field on right side
        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(FRAME_WIDTH / 2 + -339, 407, 190, 20);
        passLabel.setFont(new Font("Arial", Font.BOLD, 15));
        passLabel.setForeground(Color.BLACK);
        backgroundPanel.add(passLabel, 0);

        passwordField = new JPasswordField();
        passwordField.setBounds(FRAME_WIDTH / 2 + -339, 437, 320, 37);
        backgroundPanel.add(passwordField, 0);

        // Forgot Password Link on right side
        JLabel forgotPass = new JLabel("<html><u>Forgot Password?</u></html>", SwingConstants.RIGHT);
        forgotPass.setFont(new Font("Arial", Font.ITALIC, 10));
        forgotPass.setBounds(FRAME_WIDTH / 2 + -310, 480, 300, 15);
        forgotPass.setForeground(new Color(255, 120, 0));
        forgotPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backgroundPanel.add(forgotPass, 0);

        // Login Button on right side
        JButton loginButton = new JButton("LOGIN");
        loginButton.setBounds(FRAME_WIDTH / 2 + -284, 545, 200, 40);
        loginButton.setBackground(new Color(255, 120, 0));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        backgroundPanel.add(loginButton, 0);

        // Don't have an account link on right side
        JLabel signUp = new JLabel("<html>Don't have account? <u>Sign Up</u></html>", SwingConstants.CENTER);
        signUp.setBounds(FRAME_WIDTH / 2 + -288, 598, 200, 15);
        signUp.setFont(new Font("Arial", Font.PLAIN, 11));
        signUp.setForeground(new Color(255, 120, 0));
        signUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backgroundPanel.add(signUp, 0);

        // Add a MouseListener to handle the click event
        signUp.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new UserRegister().setVisible(true);
                dispose();
            }
        });

        // SIMPLIFIED ACTION - DIRECTLY OPENS MAIN DASHBOARD
        loginButton.addActionListener(e -> {
            String u = userIdField.getText().trim();
            String p = new String(passwordField.getPassword()).trim();
            
            // Check if fields are empty
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter both username and password.", 
                    "Login Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Attempt login
            if (controller.login(u, p)) {
                // SUCCESSFUL LOGIN - DIRECTLY OPEN MAIN DASHBOARD
                dispose(); // Close login window
                
                // Open main dashboard - assuming you have DebtStackUserDashboard
                SwingUtilities.invokeLater(() -> {
                    try {
                        // Create and show the main dashboard
                        UserDashboard mainDashboard = new UserDashboard(controller);
                        mainDashboard.setVisible(true);
                    } catch (Exception ex) {
                        // If error occurs, show message and reopen login
                        JOptionPane.showMessageDialog(null, 
                            "Error opening dashboard: " + ex.getMessage() + 
                            "\nPlease try logging in again.", 
                            "Dashboard Error", JOptionPane.ERROR_MESSAGE);
                        new Login(controller).setVisible(true);
                    }
                });
            } else {
                // FAILED LOGIN
                JOptionPane.showMessageDialog(this, 
                    "Invalid username or password. Please try again.", 
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText(""); // Clear password field
            }
        });
        
        // Add Enter key support for password field
        passwordField.addActionListener(e -> loginButton.doClick());
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
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start the application
        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            new Login(controller).setVisible(true);
        });
    }
}