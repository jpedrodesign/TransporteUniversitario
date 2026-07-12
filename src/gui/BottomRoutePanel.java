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
        UiTheme.card(this, "Resumo da rota");
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setForeground(UiTheme.TEXT);
        area.setBackground(new java.awt.Color(248, 250, 252));
        area.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        add(scroll, BorderLayout.CENTER);
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
