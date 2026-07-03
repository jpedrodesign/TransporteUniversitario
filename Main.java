import gui.TelaPrincipal;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        configurarLookAndFeel();

        SwingUtilities.invokeLater(() -> {

            try {

                TelaPrincipal tela =
                        new TelaPrincipal();

                tela.setVisible(true);

                System.out.println(
                        "Sistema iniciado com sucesso."
                );

            } catch (Exception e) {

                JOptionPane.showMessageDialog(
                        null,
                        "Erro ao iniciar o sistema:\\n"
                                + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );

                e.printStackTrace();
            }
        });
    }

    private static void configurarLookAndFeel() {

        try {

            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );

        } catch (Exception e) {

            System.err.println(
                    "Não foi possível aplicar o tema do sistema."
            );
        }
    }
}