package Gui;

import Model.AppController;
import Model.Debt;
import Model.DebtManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DebtStackMainFrame extends JFrame {
    private AppController controller;
    private DebtTowerPanel towerPanel;
    private JLabel tosLabel;

    public DebtStackMainFrame(AppController controller) {
        this.controller = controller;
        setTitle("Dashboard - Hanoi DebtStack");

        // --------------------------------------------------------
        // ⭐ KEY CHANGE 1: Full-Screen and Disable Resizing ⭐
        // Set the frame to maximize the screen size
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Disable resizing (locks the window size, effectively disabling
        // minimize/maximize buttons)
        setResizable(false);
        // --------------------------------------------------------

        setLocationRelativeTo(null); // This is good practice even when maximized
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(15, 40, 15, 40)); // Increased padding for maximized view

        JLabel title = new JLabel("The Hanoi Debt Tower");
        title.setFont(new Font("SansSerif", Font.BOLD, 28)); // Slightly larger font for full screen

        JPanel stratPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); // Add spacing to buttons
        stratPanel.setBackground(Color.WHITE);

        JButton avBtn = new JButton("Avalanche");
        JButton snBtn = new JButton("Snowball");
        styleBtn(avBtn, true); // Set Avalanche as default active style
        styleBtn(snBtn, false);

        stratPanel.add(avBtn);
        stratPanel.add(snBtn);
        header.add(title, BorderLayout.WEST);
        header.add(stratPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- Center and Right Panel Container ---
        // We use a JSplitPane to ensure the Debt Tower scales correctly
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerSize(0); // Hide the divider for a clean look
        mainSplit.setBorder(BorderFactory.createEmptyBorder());
        mainSplit.setResizeWeight(0.75); // Give 75% space to the Debt Tower, 25% to the Sidebar
        mainSplit.setBackground(Color.WHITE);

        // --- Center (Visualization) ---
        towerPanel = new DebtTowerPanel(controller.getManager());
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(new Color(245, 247, 250)); // Light grey background
        centerContainer.setBorder(new EmptyBorder(30, 40, 30, 40));
        centerContainer.add(towerPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20));
        actionPanel.setBackground(Color.WHITE);
        JButton peek = createActionButton("PEEK (View TOS)", new Color(0, 123, 255));
        JButton pop = createActionButton("POP (Remove Debt)", new Color(220, 53, 69));

        actionPanel.add(peek);
        actionPanel.add(pop);
        centerContainer.add(actionPanel, BorderLayout.SOUTH);
        mainSplit.setLeftComponent(centerContainer);

        // --- Right Sidebar (Forms) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        // No need to set preferred size explicitly as SplitPane handles it
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(new EmptyBorder(40, 30, 40, 30));

        // Add Debt Form
        sidebar.add(createHeaderLabel("Add New Debt"));
        JTextField nameF = new JTextField();
        JTextField amtF = new JTextField();
        JTextField rateF = new JTextField();
        JTextField minF = new JTextField();

        addFormItem(sidebar, "Debt Name", nameF);
        addFormItem(sidebar, "Total Amount ($)", amtF);
        addFormItem(sidebar, "Interest Rate (%)", rateF);
        addFormItem(sidebar, "Minimum Payment ($)", minF);

        JButton pushBtn = createSubmitButton("PUSH TO STACK", new Color(255, 120, 0));
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(pushBtn);

        // Payment Section
        sidebar.add(Box.createVerticalStrut(40));
        sidebar.add(createHeaderLabel("Make Payment"));

        tosLabel = new JLabel("Current TOS: Loading...");
        tosLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        tosLabel.setForeground(new Color(234, 88, 12));
        tosLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(tosLabel);

        JTextField payF = new JTextField();
        addFormItem(sidebar, "Payment Amount ($)", payF);

        JButton payBtn = createSubmitButton("PAY NOW", new Color(34, 197, 94));
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(payBtn);

        mainSplit.setRightComponent(sidebar);
        add(mainSplit, BorderLayout.CENTER);

        // --- Functionality ---
        refreshUI();

        // Action Listeners (Same logic as before, using the new buttons)
        avBtn.addActionListener(e -> {
            controller.getManager().setStrategy(DebtManager.Strategy.AVALANCHE);
            refreshUI();
        });
        snBtn.addActionListener(e -> {
            controller.getManager().setStrategy(DebtManager.Strategy.SNOWBALL);
            refreshUI();
        });

        pushBtn.addActionListener(e -> {
            try {
                controller.getManager().pushDebt(new Debt(nameF.getText(), Double.parseDouble(amtF.getText()),
                        Double.parseDouble(rateF.getText()), Double.parseDouble(minF.getText())));
                refreshUI();
                clearFormFields(nameF, amtF, rateF, minF);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Check numbers.");
            }
        });

        payBtn.addActionListener(e -> {
            Debt tos = controller.getManager().peekTOS();
            if (tos != null) {
                try {
                    tos.makePayment(Double.parseDouble(payF.getText()));
                    refreshUI();
                    payF.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid payment amount.");
                }
            }
        });

        pop.addActionListener(e -> {
            controller.getManager().popDebt();
            refreshUI();
        });
        peek.addActionListener(e -> {
            Debt t = controller.getManager().peekTOS();
            JOptionPane.showMessageDialog(this, t == null ? "The stack is empty." : t.toString());
        });
    }

    private void refreshUI() {
        towerPanel.repaint();
        Debt t = controller.getManager().peekTOS();
        tosLabel.setText("Current TOS: " + (t == null ? "None" : t.getName()));
    }

    // --- Helper Methods for Cleaner Code ---

    private JLabel createHeaderLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 16));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 10, 0));
        return l;
    }

    private void addFormItem(JPanel p, String title, JTextField f) {
        p.add(Box.createVerticalStrut(5));
        JLabel l = new JLabel(title);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(300, 30));
        p.add(l);
        p.add(f);
    }

    private JButton createSubmitButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        return btn;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 40));
        return btn;
    }

    private void styleBtn(JButton b, boolean filled) {
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setFocusPainted(false);
        if (filled) {
            b.setBackground(new Color(255, 120, 0));
            b.setForeground(Color.WHITE);
        } else {
            b.setBackground(Color.WHITE);
            b.setForeground(Color.BLACK);
            b.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        }
    }

    private void clearFormFields(JTextField... fields) {
        for (JTextField f : fields) {
            f.setText("");
        }
    }
}