package Gui;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("Hanoi DeptStack");
        setSize(1500 , 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        JPanel loginPanel = new JPanel();
        loginPanel.setBounds(535 , 200 , 500 , 500);
        loginPanel.setBackground(Color.orange);
        add(loginPanel);


        setVisible(true);
    }
}
