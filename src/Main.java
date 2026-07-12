import gui.MainWindow;

import javax.swing.*;

/**
 * Ponto de entrada do sistema de planejamento
 * de rotas de transporte universitário.
 *
 * Cruz das Almas - Bahia - Brasil
 */
public class Main {

    public static void main(String[] args) {
        // Tema nativo do sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
