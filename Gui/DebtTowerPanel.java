package Gui;

import Model.Debt;
import Model.DebtManager;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DebtTowerPanel extends JPanel {
    private DebtManager manager;

    public DebtTowerPanel(DebtManager manager) {
        this.manager = manager;
        setBackground(new Color(245, 247, 250));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Debt> debts = manager.getStackForVisualization();
        int w = getWidth(), h = getHeight();

        // Draw Pole
        g2.setColor(new Color(220, 220, 230));
        g2.fillRoundRect(w / 2 - 5, 20, 10, h - 40, 5, 5);

        if (debts.isEmpty())
            return;

        double max = manager.getMaxDebtAmount();
        int y = h - 50;

        // Draw debts in LIFO order (TOS on top)
        for (int i = 0; i < debts.size(); i++) {
            // For LIFO: i=0 is TOS (top of stack)
            Debt d = debts.get(i);
            boolean isTop = (i == 0); // First element is TOS in LIFO

            int blockW = Math.max(150, (int) ((d.getCurrentBalance() / max) * (w - 80)));
            int x = (w - blockW) / 2;

            // Colors: Red for Top (TOS), Orange/Yellow for others
            g2.setColor(isTop ? new Color(220, 53, 69) : new Color(253, 126, 20));
            g2.fillRoundRect(x, y, blockW, 40, 10, 10);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));

            // Add TOS indicator
            String displayText = d.getName() + " ($" + (int) d.getCurrentBalance() + ")";
            if (isTop) {
                displayText = "TOS: " + displayText;
            }
            g2.drawString(displayText, x + 15, y + 25);

            y -= 45;
        }
    }
}