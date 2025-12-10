package Gui;

import Model.AppController;
import Model.Debt;
import Model.DebtManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FADashboard extends JFrame {

    // --- Original State/Model variables kept for structure ---
    private final static AppController controller = new AppController();
    private DebtManager manager;

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

    // Controls
    private JTextField appointmentsField, solvedField, failedField, addedField;
    private JTextArea logsArea;
    private JScrollPane logsScrollPane;

    // Data lists
    private List<Debt> auxiliaryDebts = new ArrayList<>();
    private List<Debt> paidOffDebts = new ArrayList<>();
    
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

    public FADashboard(AppController controller2) {
        manager = controller.getManager();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setTitle("Hanoi Debt Tower Dashboard");
        setLayout(new BorderLayout());

        initUI();
        setVisible(true);
    }

    private void initUI() {
        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // Add resize listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustLayout(getWidth(), getHeight());
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
        
        // Initial layout adjustment
        adjustLayout(getWidth(), getHeight());
    }

    private void adjustLayout(int screenWidth, int screenHeight) {
        // Update background
        if (backgroundLabel != null) {
            backgroundLabel.setBounds(0, 0, screenWidth, screenHeight);
        }
        
        // Update main layer
        if (mainLayer != null) {
            mainLayer.setBounds(0, 0, screenWidth, screenHeight);
        }
        
        // Adjust all components based on screen size
        adjustComponentPositions(screenWidth, screenHeight);
    }

    private void adjustComponentPositions(int screenWidth, int screenHeight) {
        // Calculate positions based on screen size
        int sidebarWidth = 80;
        int leftMargin = 90; // Space from sidebar
        
        // Top row positions
        int topY = 30;
        int topRowHeight = 400;
        
        // Bottom row positions
        int bottomY = 450;
        int bottomRowHeight = 260;
        
        // Component widths
        int towerWidth = (int)(screenWidth * 0.35);
        towerWidth = Math.max(500, Math.min(towerWidth, 700));
        
        int reportWidth = (int)(screenWidth * 0.25);
        reportWidth = Math.max(350, Math.min(reportWidth, 500));
        
        // ADJUST: Sidebar position and size
        if (sidebar != null) {
            sidebar.setBounds(10, 100, sidebarWidth, screenHeight - 200);
        }
        
        // ADJUST: Tower container position and size
        if (towerContainer != null) {
            towerContainer.setBounds(leftMargin, topY, towerWidth, topRowHeight);
            if (towerVis != null) {
                towerVis.setBounds(0, 0, towerWidth, topRowHeight);
            }
        }
        
        // ADJUST: Report creation panel position and size
        if (reportCreationPanel != null) {
            int reportX = leftMargin + towerWidth + 40;
            reportCreationPanel.setBounds(reportX, topY, reportWidth, topRowHeight);
        }
        
        // ADJUST: Reports panel position and size
        if (reportsPanel != null) {
            reportsPanel.setBounds(leftMargin, bottomY, towerWidth, 100);
        }
        
        // ADJUST: Logs panel position and size
        if (logsPanel != null) {
            logsPanel.setBounds(leftMargin, bottomY + 120, towerWidth, bottomRowHeight - 120);
        }
        
        // ADJUST: Appointment schedule panel position and size
        if (appointmentSchedulePanel != null) {
            int scheduleX = leftMargin + towerWidth + 40;
            appointmentSchedulePanel.setBounds(scheduleX, bottomY, reportWidth, bottomRowHeight);
        }
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
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS)); 
        sidebar.setOpaque(false);
        
        // ADJUST: Sidebar position (x, y, width, height)
        sidebar.setBounds(10, 100, 80, 600);

        // Title/Header (HANOI)
        JLabel hanoi = new JLabel("HANOI");
        hanoi.setForeground(Color.WHITE);
        hanoi.setFont(new Font("SansSerif", Font.BOLD, 14));
        hanoi.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 0));
        hanoi.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(hanoi);
        
        sidebar.add(Box.createVerticalStrut(40));

        // DASHBOARD
        sidebar.add(createSidebarIcon("DASH", "Dashboard", e -> {}));
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(createSidebarLabel("BOARD", e -> {}));
        sidebar.add(Box.createVerticalStrut(40));
        
        // CLIENT
        sidebar.add(createSidebarIcon("CLIENT", "Client", e -> {}));
        sidebar.add(Box.createVerticalStrut(40));
        
        // HISTORY
        sidebar.add(createSidebarIcon("HISTORY", "History", e -> {}));
        sidebar.add(Box.createVerticalStrut(40));
        
        // PROFILE
        sidebar.add(createSidebarIcon("PROFILE", "Profile", e -> {}));

        mainLayer.add(sidebar);
    }

    private JPanel createSidebarIcon(String text, String tooltip, ActionListener action) {
        JPanel item = new JPanel(new BorderLayout());
        item.setToolTipText(tooltip);
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(80, 50));
        item.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel icon = new JLabel(text); 
        icon.setForeground(Color.WHITE);
        icon.setFont(new Font("SansSerif", Font.BOLD, 18));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        item.add(icon, BorderLayout.CENTER);
        
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(null);
            }
        });

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(item);
        container.setOpaque(false);
        container.setAlignmentX(Component.CENTER_ALIGNMENT);

        return container;
    }
    
    private JPanel createSidebarLabel(String text, ActionListener action) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(80, 20));
        item.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 10));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        item.add(label, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(null);
            }
        });

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(item);
        container.setOpaque(false);
        container.setAlignmentX(Component.CENTER_ALIGNMENT);

        return container;
    }

    private void createTopRow() {
        // Tower Panel
        towerContainer = new RoundedPanel(20, Color.WHITE);
        towerContainer.setLayout(null);
        
        // ADJUST: Tower container position (x, y, width, height)
        towerContainer.setBounds(90, 30, 600, 400);

        towerVis = new TowerVisualizationPanel();
        
        // ADJUST: Tower visualization position within container (x, y, width, height)
        towerVis.setBounds(0, 0, 600, 400);
        towerContainer.add(towerVis);
        mainLayer.add(towerContainer);

        // Create Report Panel
        reportCreationPanel = createReportCreationPanel();
        
        // ADJUST: Report creation panel position (x, y, width, height)
        reportCreationPanel.setBounds(710, 30, 450, 400);
        mainLayer.add(reportCreationPanel);
    }

    private void createBottomRow() {
        int startY = 450;
        int height = 260;

        // Reports Panel
        reportsPanel = createReportsPanel();
        
        // ADJUST: Reports panel position (x, y, width, height)
        reportsPanel.setBounds(90, startY, 600, 100);
        mainLayer.add(reportsPanel);
        
        // Appointment Schedule Panel
        appointmentSchedulePanel = createAppointmentSchedulePanel();
        
        // ADJUST: Appointment schedule panel position (x, y, width, height)
        appointmentSchedulePanel.setBounds(710, startY, 450, height);
        mainLayer.add(appointmentSchedulePanel);

        // Logs Panel
        logsPanel = createLogsPanel();
        
        // ADJUST: Logs panel position (x, y, width, height)
        logsPanel.setBounds(90, startY + 120, 600, height - 120);
        mainLayer.add(logsPanel);
    }
    
    private JPanel createReportCreationPanel() {
        RoundedPanel panel = new RoundedPanel(20, Color.WHITE);
        panel.setLayout(null);
        
        // ADJUST: Report creation panel position (x, y, width, height)
        panel.setBounds(710, 30, 450, 400);

        JLabel title = new JLabel("Create Report");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        // ADJUST: Title position within panel (x, y, width, height)
        title.setBounds(20, 20, 200, 30);
        panel.add(title);

        int y = 70;
        int gap = 55;
        int fieldH = 30;
        int width = 410;
        
        // Total Client Appointments
        addLabel(panel, "Total Client Appointments:", 20, y - 20);
        
        // ADJUST: Appointments field position (x, y, width, height)
        appointmentsField = addTextField(panel, "", 20, y, width, fieldH);

        // Total Solved
        y += gap;
        addLabel(panel, "Total Solved:", 20, y - 20);
        
        // ADJUST: Solved field position (x, y, width, height)
        solvedField = addTextField(panel, "", 20, y, width, fieldH);

        // Total Failed
        y += gap;
        addLabel(panel, "Total Failed:", 20, y - 20);
        
        // ADJUST: Failed field position (x, y, width, height)
        failedField = addTextField(panel, "", 20, y, width, fieldH);

        // Total Added
        y += gap;
        addLabel(panel, "Total Added:", 20, y - 20);
        
        // ADJUST: Added field position (x, y, width, height)
        addedField = addTextField(panel, "", 20, y, width, fieldH);

        // PUSH Button
        JButton pushBtn = createOrangeButton("PUSH");
        
        // ADJUST: Push button position (x, y, width, height)
        pushBtn.setBounds(20, 340, width, 40);
        pushBtn.addActionListener(e -> log("PUSH button pressed."));
        panel.add(pushBtn);

        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel container = new JPanel(null); 
        container.setOpaque(false);
        
        // ADJUST: Reports container position (x, y, width, height)
        container.setBounds(90, 450, 600, 100);

        JLabel reportsTitle = new JLabel("REPORTS");
        reportsTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        reportsTitle.setForeground(Color.WHITE);
        
        // ADJUST: Reports title position (x, y, width, height)
        reportsTitle.setBounds(0, 0, 100, 20);
        container.add(reportsTitle);
        
        JPanel reportBoxes = new JPanel(new GridLayout(1, 3, 15, 0));
        reportBoxes.setOpaque(false);
        reportBoxes.add(createReportBox("TOTAL CLIENTS", "23,113", DARK_ORANGE_REPORT)); 
        reportBoxes.add(createReportBox("TOTAL APPOINTMENT", "21", BLUE_ACCENT)); 
        reportBoxes.add(createReportBox("SOLVED", "11241 users", RED_REPORT));

        // ADJUST: Report boxes position (x, y, width, height)
        reportBoxes.setBounds(0, 25, 600, 75);
        container.add(reportBoxes);

        return container;
    }

    private JPanel createReportBox(String title, String value, Color bgColor) {
        RoundedPanel panel = new RoundedPanel(10, bgColor);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // ADJUST: Report box size (width, height)
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

    private JPanel createAppointmentSchedulePanel() {
        RoundedPanel panel = new RoundedPanel(10, Color.WHITE);
        panel.setLayout(null);
        
        // ADJUST: Appointment panel position (x, y, width, height)
        panel.setBounds(710, 450, 450, 260);

        JLabel title = new JLabel("Appointment Schedule");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        // ADJUST: Title position within panel (x, y, width, height)
        title.setBounds(20, 20, 300, 30);
        panel.add(title);

        JTextArea scheduleText = new JTextArea();
        scheduleText.setText(
                "Maria is set for a consultation within this\n" +
                "day, just clicked the zoom link below.\n\n" +
                "John is set for consultation for being\n" +
                "inactive.");

        scheduleText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        scheduleText.setEditable(false);
        scheduleText.setLineWrap(true);
        scheduleText.setWrapStyleWord(true);
        scheduleText.setOpaque(false);
        
        // ADJUST: Schedule text position (x, y, width, height)
        scheduleText.setBounds(20, 60, 410, 100);
        panel.add(scheduleText);
        
        int lineY = 170;
        for (int i = 0; i < 4; i++) {
            JLabel line = new JLabel();
            line.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY.brighter()));
            
            // ADJUST: Line position (x, y, width, height)
            line.setBounds(20, lineY + (i * 20), 410, 1);
            panel.add(line);
        }

        return panel;
    }

    private JPanel createLogsPanel() {
        JPanel logContainer = new JPanel(new BorderLayout());
        logContainer.setBackground(Color.BLACK);
        
        // ADJUST: Logs container position (x, y, width, height)
        logContainer.setBounds(90, 570, 600, 140);

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
                         " > Launched 3 debts to Active Debt Stack.");

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
        
        // ADJUST: Label position (x, y, width, height)
        l.setBounds(x, y, 300, 20);
        p.add(l);
    }

    private JTextField addTextField(JPanel p, String ph, int x, int y, int w, int h) {
        JTextField tf = new JTextField();
        
        // ADJUST: Text field position and size (x, y, width, height)
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

            // Title
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 24));
            String title = "THE HANOI DEBT TOWER";
            FontMetrics fm = g2.getFontMetrics();
            
            // ADJUST: Title position (center horizontally, y=40)
            g2.drawString(title, (w - fm.stringWidth(title)) / 2, 40);

            // Base Line
            int baseY = h - 60;
            g2.setColor(new Color(150, 80, 150));
            
            // ADJUST: Base line position and size (x, y, width, height)
            g2.fillRoundRect(50, baseY, w - 100, 15, 10, 10);

            // Columns
            String[] labels = { "APPOINTMENT", "AUXILIARY", "SOLVED" };
            int colW = w / 3;

            g2.setColor(TOWER_PILLAR_COLOR);
            for (int i = 0; i < 3; i++) {
                int cx = colW * i + colW / 2;
                
                // ADJUST: Pillar position and size (x, y, width, height)
                g2.fillRoundRect(cx - 5, 80, 10, baseY - 80, 10, 10);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                String lbl = labels[i];
                
                // ADJUST: Column label position
                g2.drawString(lbl, cx - fm.stringWidth(lbl) / 2, baseY + 30);
            }

            // Draw Debts
            int brickH = 30;
            int gap = 5;
            int cx = colW / 2;

            // Disk 4 (Bottom)
            // ADJUST: Disk 4 position (centerX, y, width, height)
            drawDisk(g2, cx, baseY - (4 * (brickH + gap)), 180, brickH, RED_DEBT, "Debt Due");

            // Disk 3
            // ADJUST: Disk 3 position (centerX, y, width, height)
            drawDisk(g2, cx, baseY - (3 * (brickH + gap)), 160, brickH, ORANGE_DEBT, "Media Complaince Lead");

            // Disk 2
            // ADJUST: Disk 2 position (centerX, y, width, height)
            drawDisk(g2, cx, baseY - (2 * (brickH + gap)), 140, brickH, YELLOW, "Agile Execution");

            // Disk 1 (Top)
            // ADJUST: Disk 1 position (centerX, y, width, height)
            drawDisk(g2, cx, baseY - (1 * (brickH + gap)), 120, brickH, LIGHT_GREEN, "Appointment");
        }

        private void drawDisk(Graphics2D g2, int centerX, int y, int width, int height, Color c, String text) {
            // ADJUST: Disk position (centerX - width/2, y, width, height)
            g2.setColor(c);
            g2.fillRoundRect(centerX - width / 2, y, width, height, 15, 15);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            
            // ADJUST: Text position (center horizontally, y + height/2 + 3)
            g2.drawString(text, centerX - fm.stringWidth(text) / 2, y + height / 2 + 3);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FADashboard(controller));
    }
}