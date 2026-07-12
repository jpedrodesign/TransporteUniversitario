package gui;

import model.Rota;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;

@SuppressWarnings({"serial", "this-escape"})
public class BottomRoutePanel extends JPanel {

    private final JTextArea area = new JTextArea(8, 80);

    public BottomRoutePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Rota calculada"));
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        add(new JScrollPane(area), BorderLayout.CENTER);
    }

    public void mostrar(Rota rota) {
        if (rota == null) {
            area.setText("ROTA CALCULADA\n\nNenhuma rota ativa.");
            return;
        }
        area.setText(rota.formatarResumo());
        area.setCaretPosition(0);
    }

    public void limpar() {
        area.setText("ROTA CALCULADA\n\nNenhuma rota ativa.");
    }
}
