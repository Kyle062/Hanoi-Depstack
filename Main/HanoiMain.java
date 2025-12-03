import Model.AppController;
import Gui.DebtStackLoginUI;
import javax.swing.SwingUtilities;

public class HanoiMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize the logic brain
            AppController controller = new AppController();

            // Open the Login Screen first
            new DebtStackLoginUI(controller).setVisible(true);
        });
    }
}