
import Gui.Login;
import Model.AppController;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
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