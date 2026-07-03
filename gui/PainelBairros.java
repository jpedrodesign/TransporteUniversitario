package gui;

import model.Ponto;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class PainelBairros extends JPanel {

    public interface AlteracaoListener {
        void onAlterado(Ponto ponto, boolean ativo);
    }

    private final JPanel lista;
    private final Map<Ponto, JCheckBox> caixas;

    public PainelBairros() {

        caixas = new LinkedHashMap<Ponto, JCheckBox>();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Bairros e loteamentos"));
        setPreferredSize(new Dimension(250, 260));
        setBackground(new Color(18, 24, 31));

        lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBackground(new Color(18, 24, 31));
        lista.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(18, 24, 31));
        add(scroll, BorderLayout.CENTER);
    }

    public void adicionarOuAtualizar(
            Ponto ponto,
            boolean ativo,
            AlteracaoListener listener
    ) {

        JCheckBox caixa = caixas.get(ponto);

        if (caixa == null) {
            caixa = criarCaixa(ponto, ativo, listener);
            caixas.put(ponto, caixa);
            lista.add(caixa);
            lista.revalidate();
            lista.repaint();
        } else {
            caixa.setSelected(ativo);
        }
    }

    public void remover(Ponto ponto) {

        JCheckBox caixa = caixas.remove(ponto);

        if (caixa != null) {
            lista.remove(caixa);
            lista.revalidate();
            lista.repaint();
        }
    }

    public void limpar() {

        caixas.clear();
        lista.removeAll();
        lista.revalidate();
        lista.repaint();
    }

    private JCheckBox criarCaixa(
            final Ponto ponto,
            boolean ativo,
            final AlteracaoListener listener
    ) {

        JCheckBox caixa = new JCheckBox(ponto.getNome(), ativo);
        caixa.setAlignmentX(LEFT_ALIGNMENT);
        caixa.setHorizontalAlignment(SwingConstants.LEFT);
        caixa.setOpaque(true);
        caixa.setBackground(new Color(27, 35, 44));
        caixa.setForeground(Color.WHITE);
        caixa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        caixa.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        caixa.addItemListener(event -> {

            if (listener != null) {
                listener.onAlterado(
                        ponto,
                        event.getStateChange() == ItemEvent.SELECTED
                );
            }
        });

        return caixa;
    }
}
