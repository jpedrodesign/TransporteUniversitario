package gui;

import model.Ponto;
import model.Rota;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Painel responsavel pela exibicao
 * dos resultados do sistema.
 */
public class PainelResultado extends JPanel {

    private JTextArea areaResultado;

    public PainelResultado() {

        configurarPainel();
        inicializarComponentes();
    }

    private void configurarPainel() {

        setLayout(new BorderLayout());
        setBorder(
                BorderFactory.createTitledBorder(
                        null,
                        "Resultados das Rotas",
                        TitledBorder.LEADING,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12),
                        Color.DARK_GRAY
                )
        );
        setPreferredSize(new Dimension(1000, 220));
        setBackground(Color.WHITE);
    }

    private void inicializarComponentes() {

        areaResultado = new JTextArea();
        areaResultado.setEditable(false);
        areaResultado.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        14
                )
        );
        areaResultado.setBackground(new Color(248, 248, 248));
        areaResultado.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        areaResultado.setText(
                "===== SISTEMA DE TRANSPORTE =====\n\n"
                        + "Aguardando calculo das rotas...\n"
        );

        JScrollPane scroll = new JScrollPane(areaResultado);
        add(scroll, BorderLayout.CENTER);
    }

    public void atualizarResultado(Rota rota) {

        atualizarResultado(rota, 2, "BUS-01", "Joao");
    }

    public void atualizarResultado(
            Rota rota,
            int onibusAtivos,
            String placaOnibus,
            String motorista
    ) {

        if (rota == null) {
            areaResultado.setText("Nenhuma rota calculada.");
            return;
        }

        List<Ponto> percurso =
                normalizarPercurso(rota.getPercurso());

        StringBuilder sb = new StringBuilder();
        sb.append("===== RESULTADO DO PLANEJAMENTO =====\n\n");
        sb.append("Percurso principal:\n");

        for (int i = 0; i < percurso.size(); i++) {

            Ponto ponto = percurso.get(i);

            sb.append(String.format("%02d. %s", i + 1, ponto.getNome()));

            if (i < percurso.size() - 1) {
                sb.append("  ->");
            }

            sb.append("\n");
        }

        sb.append("\n");
        sb.append("Distancia total: ")
                .append(String.format("%.2f km", rota.getDistanciaTotal()))
                .append("\n");
        sb.append("Tempo estimado: ")
                .append(String.format("%.2f min", rota.getTempoTotal()))
                .append("\n");
        sb.append("Velocidade media: 40.00 km/h\n");
        sb.append("Quantidade de alunos: ")
                .append(rota.calcularTotalAlunos())
                .append("\n");
        sb.append("Paradas registradas: ")
                .append(percurso.size())
                .append("\n");
        sb.append("Onibus ativos: ")
                .append(onibusAtivos)
                .append("\n");
        sb.append("Onibus principal: ")
                .append(placaOnibus)
                .append(" | Motorista: ")
                .append(motorista)
                .append("\n");
        sb.append("Rota em tempo real: ativa\n");

        areaResultado.setText(sb.toString());
    }

    public void exibirMensagem(String mensagem) {

        areaResultado.setText(mensagem);
    }

    public void limpar() {

        areaResultado.setText("Resultados limpos.\n");
    }

    private List<Ponto> normalizarPercurso(List<Ponto> percurso) {

        List<Ponto> resultado = new ArrayList<Ponto>();

        for (Ponto ponto : percurso) {

            if (resultado.isEmpty()
                    || !resultado.get(resultado.size() - 1).equals(ponto)) {
                resultado.add(ponto);
            }
        }

        return resultado;
    }
}
