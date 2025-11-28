package Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class DebtStackUserRegisterUI extends JFrame {

    // Define fixed dimensions for the frame
    private static final int FRAME_WIDTH = 1250;
    private static final int FRAME_HEIGHT = 850;

    // Components for the login form
    private JTextField userIdField;
    private JPasswordField passwordField;

    public DebtStackUserRegisterUI() {
        setTitle("HANOI DEBTSTACK User Login");
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
        LogInLabel.setBounds(FRAME_WIDTH/2 + 440, 23, 190, 20);
        LogInLabel.setFont(new Font("Arial", Font.ROMAN_BASELINE, 17));
        LogInLabel.setForeground(Color.BLACK);
        backgroundPanel.add(LogInLabel, 0);

        JLabel SignupLabel = new JLabel("SignUp");
        SignupLabel.setBounds(FRAME_WIDTH/2 + 513, 23, 190, 20);
        SignupLabel.setFont(new Font("Arial", Font.BOLD, 17));
        SignupLabel.setForeground(Color.BLACK);
        backgroundPanel.add(SignupLabel, 0);
        // Welcome Text - positioned on the right side
    

        // Form components - positioned on the right side
        JLabel fullnameLabel = new JLabel("Full Name:");
        fullnameLabel.setBounds(FRAME_WIDTH/2 + 160, 235, 250, 20);
        fullnameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        fullnameLabel.setForeground(Color.BLACK);
        backgroundPanel.add(fullnameLabel, 0);

        JTextField fullnameField = new JTextField();
        fullnameField.setBounds(FRAME_WIDTH/2 + 160, 265, 340, 37);
        fullnameField.setFont(new Font("Arial", Font.BOLD, 15));
        backgroundPanel.add(fullnameField, 0);

        JLabel ageLabel = new JLabel("Email Address: ");
        ageLabel.setBounds(FRAME_WIDTH/2 + 160, 315, 200, 20);
        ageLabel.setFont(new Font("Arial", Font.BOLD, 15));
        ageLabel.setForeground(Color.BLACK);
        backgroundPanel.add(ageLabel, 0);

        JTextField ageField = new JTextField();
        ageField.setBounds(FRAME_WIDTH/2 + 160, 345, 340, 37);
        ageField.setFont(new Font("Arial", Font.BOLD, 15));
        backgroundPanel.add(ageField, 0);

        JLabel purposeLabel = new JLabel("UserName:"); 
        purposeLabel.setBounds(FRAME_WIDTH/2 + 160, 393, 250, 20);
        purposeLabel.setFont(new Font("Arial", Font.BOLD, 15));
        purposeLabel.setForeground(Color.BLACK);
        backgroundPanel.add(purposeLabel, 0);

        JTextArea purposeArea = new JTextArea();
        purposeArea.setLineWrap(true);
        purposeArea.setWrapStyleWord(true); 
        purposeArea.setFont(new Font("Arial", Font.BOLD, 15));

        JScrollPane purposeScrollPane = new JScrollPane(purposeArea);
        purposeScrollPane.setBounds(FRAME_WIDTH/2 + 160, 423, 340, 37); 
        backgroundPanel.add(purposeScrollPane, 0);

        JLabel passwordLabel = new JLabel("Create Password: ");
        passwordLabel.setBounds(FRAME_WIDTH/2 + 160, 472, 200, 20);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 15));
        passwordLabel.setForeground(Color.BLACK);
        backgroundPanel.add(passwordLabel, 0);

        passwordField = new JPasswordField();
        passwordField.setBounds(FRAME_WIDTH/2 + 160, 502, 340, 37);
        backgroundPanel.add(passwordField, 0);


        // Login Button
        JButton loginButton = new JButton("SIGNUP");
        loginButton.setBounds(FRAME_WIDTH/2 + 226, 580, 200, 40);
        loginButton.setBackground(new Color(255, 120, 0));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        backgroundPanel.add(loginButton, 0);

        // Don't have an account link
        JLabel signUp = new JLabel("<html> Already have an account? <u>Log In</u></html>", SwingConstants.CENTER);
        signUp.setBounds(FRAME_WIDTH/2 + 220, 630, 200, 15);
        signUp.setFont(new Font("Arial", Font.PLAIN, 11));
        signUp.setForeground(new Color(255, 120, 0));
        signUp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backgroundPanel.add(signUp, 0);

        // Example Action Listener for the login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Dummy login logic
                String user = fullnameField.getText();
                char[] pass = passwordField.getPassword();
                String message = "Attempting to log in with User: " + user;
                JOptionPane.showMessageDialog(DebtStackUserRegisterUI.this, message, "Login Info",
                        JOptionPane.INFORMATION_MESSAGE);
                // Clear password field for security
                passwordField.setText("");
            }
        });
    }

    private void addBulletPoint(JPanel panel, String text, int yPos) {
        JLabel bullet = new JLabel("<html>&bull;</html>");
        bullet.setBounds(FRAME_WIDTH/2 - 300, yPos, 20, 20);
        bullet.setForeground(Color.WHITE);
        bullet.setFont(new Font("Arial", Font.PLAIN, 40));
        panel.add(bullet, 0);

        JLabel label = new JLabel(text);
        label.setBounds(FRAME_WIDTH/2 - 270, yPos, 400, 20);
        label.setForeground(Color.WHITE);
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
        new DebtStackUserRegisterUI().setVisible(true);
    }
}