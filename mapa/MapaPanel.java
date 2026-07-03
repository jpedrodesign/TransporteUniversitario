package mapa;

import model.Aresta;
import model.Ponto;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Painel gráfico responsável
 * por desenhar o mapa,
 * os pontos e as rotas.
 */
public class MapaPanel extends JPanel {

    private List<Ponto> pontos;
    private List<Aresta> arestas;
    private List<Ponto> rotaAtual;

    public MapaPanel() {

        pontos = new ArrayList<>();
        arestas = new ArrayList<>();
        rotaAtual = new ArrayList<>();

        configurarPainel();
    }

    /**
     * Configuração visual do painel.
     */
    private void configurarPainel() {

        setBackground(Color.WHITE);

        setBorder(
                new TitledBorder(
                        "Mapa Inteligente"
                )
        );

        setPreferredSize(
                new Dimension(900, 600)
        );
    }

    /**
     * Adiciona um ponto no mapa.
     */
    public void adicionarPonto(
            Ponto ponto
    ) {

        if (ponto != null) {

            pontos.add(ponto);

            repaint();
        }
    }

    /**
     * Adiciona conexão entre pontos.
     */
    public void adicionarAresta(
            Aresta aresta
    ) {

        if (aresta != null) {

            arestas.add(aresta);

            repaint();
        }
    }

    /**
     * Define rota para destaque visual.
     */
    public void definirRota(
            List<Ponto> rota
    ) {

        rotaAtual = rota;

        repaint();
    }

    /**
     * Remove todos os elementos.
     */
    public void limparMapa() {

        pontos.clear();

        arestas.clear();

        rotaAtual.clear();

        repaint();
    }

    /**
     * Desenho principal do mapa.
     */
    @Override
    protected void paintComponent(
            Graphics g
    ) {

        super.paintComponent(g);

        Graphics2D g2 =
                (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        desenharGrade(g2);

        desenharArestas(g2);

        desenharRota(g2);

        desenharPontos(g2);

        desenharLegenda(g2);
    }

    /**
     * Desenha grade de fundo.
     */
    private void desenharGrade(
            Graphics2D g2
    ) {

        g2.setColor(
                new Color(240, 240, 240)
        );

        for (int x = 0;
             x < getWidth();
             x += 50) {

            g2.drawLine(
                    x,
                    0,
                    x,
                    getHeight()
            );
        }

        for (int y = 0;
             y < getHeight();
             y += 50) {

            g2.drawLine(
                    0,
                    y,
                    getWidth(),
                    y
            );
        }
    }

    /**
     * Desenha conexões do grafo.
     */
    private void desenharArestas(
            Graphics2D g2
    ) {

        g2.setStroke(
                new BasicStroke(2)
        );

        g2.setColor(Color.GRAY);

        for (Aresta aresta : arestas) {

            int x1 =
                    (int) aresta
                            .getOrigem()
                            .getLatitude();

            int y1 =
                    (int) aresta
                            .getOrigem()
                            .getLongitude();

            int x2 =
                    (int) aresta
                            .getDestino()
                            .getLatitude();

            int y2 =
                    (int) aresta
                            .getDestino()
                            .getLongitude();

            g2.drawLine(x1, y1, x2, y2);

            int meioX = (x1 + x2) / 2;
            int meioY = (y1 + y2) / 2;

            g2.drawString(
                    String.format(
                            "%.1f km",
                            aresta.getDistancia()
                    ),
                    meioX,
                    meioY
            );
        }
    }

    /**
     * Destaca rota calculada.
     */
    private void desenharRota(
            Graphics2D g2
    ) {

        if (rotaAtual == null
                || rotaAtual.size() < 2) {

            return;
        }

        g2.setColor(
                new Color(0, 102, 255)
        );

        g2.setStroke(
                new BasicStroke(4)
        );

        for (int i = 0;
             i < rotaAtual.size() - 1;
             i++) {

            Ponto origem =
                    rotaAtual.get(i);

            Ponto destino =
                    rotaAtual.get(i + 1);

            int x1 =
                    (int) origem.getLatitude();

            int y1 =
                    (int) origem.getLongitude();

            int x2 =
                    (int) destino.getLatitude();

            int y2 =
                    (int) destino.getLongitude();

            g2.drawLine(
                    x1,
                    y1,
                    x2,
                    y2
            );
        }
    }

    /**
     * Desenha pontos do mapa.
     */
    private void desenharPontos(
            Graphics2D g2
    ) {

        for (Ponto ponto : pontos) {

            int x =
                    (int) ponto.getLatitude();

            int y =
                    (int) ponto.getLongitude();

            g2.setColor(
                    new Color(0, 153, 76)
            );

            g2.fillOval(
                    x - 8,
                    y - 8,
                    16,
                    16
            );

            g2.setColor(Color.BLACK);

            g2.drawString(
                    ponto.getNome(),
                    x + 10,
                    y
            );

            g2.drawString(
                    ponto.getQuantidadeAlunos()
                            + " alunos",
                    x + 10,
                    y + 15
            );
        }
    }

    /**
     * Desenha legenda do sistema.
     */
    private void desenharLegenda(
            Graphics2D g2
    ) {

        int x = 20;
        int y = 20;

        g2.setColor(Color.BLACK);

        g2.drawString(
                "Legenda:",
                x,
                y
        );

        g2.setColor(
                new Color(0, 153, 76)
        );

        g2.fillOval(
                x,
                y + 10,
                12,
                12
        );

        g2.setColor(Color.BLACK);

        g2.drawString(
                "Pontos de embarque",
                x + 20,
                y + 20
        );

        g2.setColor(
                new Color(0, 102, 255)
        );

        g2.drawLine(
                x,
                y + 40,
                x + 20,
                y + 40
        );

        g2.setColor(Color.BLACK);

        g2.drawString(
                "Rota otimizada",
                x + 30,
                y + 45
        );
    }
}