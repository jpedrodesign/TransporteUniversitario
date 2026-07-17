import gui.MainWindow;
import gui.UiTheme;

import javax.swing.*;
import java.awt.GraphicsEnvironment;

/**
 * Ponto de entrada do sistema de planejamento
 * de rotas de transporte universitário.
 *
 * Cruz das Almas - Bahia - Brasil
 */
public class Main {

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Este sistema precisa de um ambiente grafico para abrir a interface.");
            return;
        }

        // Tema nativo do sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException
                 | InstantiationException
                 | IllegalAccessException
                 | UnsupportedLookAndFeelException ignored) {
        }
        UiTheme.aplicar();

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
