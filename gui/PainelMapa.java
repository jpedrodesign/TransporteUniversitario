package gui;

import model.Aresta;
import model.Ponto;
import model.Rota;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PainelMapa extends JPanel {

    private static final double LAT_MIN = -12.6900;
    private static final double LAT_MAX = -12.6600;
    private static final double LON_MIN = -39.1180;
    private static final double LON_MAX = -39.0860;

    private List<Ponto> pontos;
    private List<Aresta> arestas;
    private List<Ponto> rotaAtual;
    private List<List<Ponto>> rotasColoridas;
    private List<Aresta> arestasDestaque;
    private final Timer timerAnimacao;
    private int animacaoIndex;
    private int animacaoProgresso;
    private int zoom;

    private static final Color[] PALETTE_COLORS = {
            new Color(255, 102, 102),
            new Color(102, 178, 255),
            new Color(102, 255, 178),
            new Color(255, 204, 102),
            new Color(204, 102, 255),
            new Color(255, 102, 255),
            new Color(102, 255, 255),
            new Color(255, 153, 153),
            new Color(153, 204, 255),
            new Color(153, 255, 204)
    };

    public PainelMapa() {

        pontos = new ArrayList<Ponto>();
        arestas = new ArrayList<Aresta>();
        rotaAtual = new ArrayList<Ponto>();
        rotasColoridas = new ArrayList<List<Ponto>>();
        arestasDestaque = new ArrayList<Aresta>();
        animacaoIndex = 0;
        animacaoProgresso = 0;
        zoom = 0;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Mapa - Cruz das Almas"));
        setPreferredSize(new Dimension(900, 500));
        setBackground(new Color(255, 255, 255));

        timerAnimacao = new Timer(100, e -> atualizarAnimacao());
        timerAnimacao.start();
    }

    public void adicionarPonto(Ponto ponto) {

        pontos.add(ponto);
        repaint();
    }

    public void adicionarAresta(Aresta aresta) {

        arestas.add(aresta);
        repaint();
    }

    public void sincronizarDados(
            List<Ponto> pontosVisiveis,
            List<Aresta> arestasVisiveis
    ) {

        pontos = new ArrayList<Ponto>(pontosVisiveis);
        arestas = new ArrayList<Aresta>(arestasVisiveis);
        rotaAtual = new ArrayList<Ponto>(Collections.<Ponto>emptyList());
        rotasColoridas.clear();
        arestasDestaque = new ArrayList<Aresta>(Collections.<Aresta>emptyList());
        animacaoIndex = 0;
        animacaoProgresso = 0;
        repaint();
    }

    public void definirRota(List<Ponto> rota) {

        rotaAtual = new ArrayList<Ponto>(rota);
        rotasColoridas.clear();
        arestasDestaque.clear();
        animacaoIndex = 0;
        animacaoProgresso = 0;
        repaint();
    }

    public void definirRotas(List<List<Ponto>> rotas) {

        rotasColoridas = new ArrayList<List<Ponto>>(rotas);
        rotaAtual.clear();
        arestasDestaque.clear();
        animacaoIndex = 0;
        animacaoProgresso = 0;
        repaint();
    }

    public void atualizarResumoRota(
            Rota rota,
            int quantidadeOnibus,
            String placaOnibus,
            String motorista
    ) {

        repaint();
    }

    public void destacarArestas(List<Aresta> arestas) {

        arestasDestaque = new ArrayList<Aresta>(arestas);
        rotaAtual.clear();
        repaint();
    }

    public void removerPonto(Ponto ponto) {

        pontos.remove(ponto);
        arestas.removeIf(
                aresta -> aresta.getOrigem().equals(ponto)
                        || aresta.getDestino().equals(ponto)
        );
        rotaAtual.remove(ponto);
        arestasDestaque.removeIf(
                aresta -> aresta.getOrigem().equals(ponto)
                        || aresta.getDestino().equals(ponto)
        );

        repaint();
    }

    public void limparMapa() {

        pontos.clear();
        arestas.clear();
        rotaAtual.clear();
        arestasDestaque.clear();
        animacaoIndex = 0;
        animacaoProgresso = 0;
        repaint();
    }

    public void aumentarZoom() {

        zoom = Math.min(6, zoom + 1);
        repaint();
    }

    public void diminuirZoom() {

        zoom = Math.max(0, zoom - 1);
        repaint();
    }

    public void ajustarZoom() {

        zoom = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        desenharFundo(g2);
        desenharRuaBase(g2);
        desenharArestas(g2);
        desenharRotasColoridas(g2);
        desenharArestasDestaque(g2);
        desenharRota(g2);
        desenharPontos(g2);
        desenharOnibus(g2);

        g2.dispose();
    }

    private void desenharFundo(Graphics2D g2) {

        int w = getWidth();
        int h = getHeight();

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(220, 220, 220, 120));
        for (int x = 0; x < w; x += 56) {
            g2.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 56) {
            g2.drawLine(0, y, w, y);
        }
    }

    private void desenharRuaBase(Graphics2D g2) {

        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(240, 240, 240));
        g2.fillRoundRect(14, 14, w - 28, h - 28, 18, 18);

        g2.setColor(new Color(200, 200, 200, 150));
        g2.drawRoundRect(14, 14, w - 28, h - 28, 18, 18);
    }

    private void desenharArestas(Graphics2D g2) {

        g2.setColor(new Color(180, 180, 180, 180));
        g2.setStroke(new BasicStroke(2f));

        for (Aresta aresta : arestas) {

            java.awt.Point origem = converterParaTela(aresta.getOrigem());
            java.awt.Point destino = converterParaTela(aresta.getDestino());

            g2.drawLine(origem.x, origem.y, destino.x, destino.y);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(Color.BLACK);
            g2.drawString(
                    String.format("%.1f km", aresta.getDistancia()),
                    (origem.x + destino.x) / 2,
                    (origem.y + destino.y) / 2
            );
            g2.setColor(new Color(180, 180, 180, 180));
        }
    }

    private void desenharRotasColoridas(Graphics2D g2) {

        if (rotasColoridas == null || rotasColoridas.isEmpty()) {
            return;
        }

        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int index = 0; index < rotasColoridas.size(); index++) {

            List<Ponto> rota = rotasColoridas.get(index);

            if (rota == null || rota.size() < 2) {
                continue;
            }

            Color cor = PALETTE_COLORS[index % PALETTE_COLORS.length];
            g2.setColor(new Color(
                    cor.getRed(),
                    cor.getGreen(),
                    cor.getBlue(),
                    200
            ));

            for (int i = 0; i < rota.size() - 1; i++) {

                java.awt.Point origem = converterParaTela(rota.get(i));
                java.awt.Point destino = converterParaTela(rota.get(i + 1));
                g2.drawLine(origem.x, origem.y, destino.x, destino.y);
            }
        }
    }

    private void desenharArestasDestaque(Graphics2D g2) {

        if (arestasDestaque.isEmpty()) {
            return;
        }

        g2.setColor(new Color(0, 180, 96, 220));
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (Aresta aresta : arestasDestaque) {

            java.awt.Point origem = converterParaTela(aresta.getOrigem());
            java.awt.Point destino = converterParaTela(aresta.getDestino());

            g2.drawLine(origem.x, origem.y, destino.x, destino.y);
        }
    }

    private void desenharRota(Graphics2D g2) {

        if (rotaAtual.size() < 2) {
            return;
        }

        g2.setColor(new Color(0, 120, 255, 235));
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 0; i < rotaAtual.size() - 1; i++) {

            java.awt.Point origem = converterParaTela(rotaAtual.get(i));
            java.awt.Point destino = converterParaTela(rotaAtual.get(i + 1));

            g2.drawLine(origem.x, origem.y, destino.x, destino.y);
        }
    }

    private void desenharPontos(Graphics2D g2) {

        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));

        for (Ponto ponto : pontos) {

            java.awt.Point posicao = converterParaTela(ponto);
            boolean universidade = ponto.getQuantidadeAlunos() == 0;

            Color corPonto = universidade
                    ? new Color(255, 83, 83)
                    : new Color(0, 180, 96);

            g2.setColor(new Color(0, 0, 0, 130));
            g2.fillOval(posicao.x - 11, posicao.y - 9, 22, 22);

            g2.setColor(Color.WHITE);
            g2.fillOval(posicao.x - 10, posicao.y - 10, 20, 20);

            g2.setColor(corPonto);
            g2.fillOval(posicao.x - 8, posicao.y - 8, 16, 16);

            g2.setColor(Color.BLACK);
            g2.drawString(
                    ponto.getNome() + " (" + ponto.getQuantidadeAlunos() + ")",
                    posicao.x + 12,
                    posicao.y - 10
            );
        }
    }

    private void desenharOnibus(Graphics2D g2) {

        if (rotaAtual.size() < 2) {
            return;
        }

        int indexAtual = Math.min(animacaoIndex, rotaAtual.size() - 2);
        int indexProximo = indexAtual + 1;

        Ponto atual = rotaAtual.get(indexAtual);
        Ponto proximo = rotaAtual.get(indexProximo);

        java.awt.Point origem = converterParaTela(atual);
        java.awt.Point destino = converterParaTela(proximo);

        double t = animacaoProgresso / 100.0;
        int x = (int) Math.round(origem.x + (destino.x - origem.x) * t);
        int y = (int) Math.round(origem.y + (destino.y - origem.y) * t);

        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRoundRect(x - 13, y - 9, 30, 20, 8, 8);

        g2.setColor(Color.ORANGE);
        g2.fillRoundRect(x - 15, y - 11, 30, 20, 8, 8);

        g2.setColor(new Color(255, 230, 170));
        g2.fillRoundRect(x - 11, y - 7, 18, 6, 4, 4);

        g2.setColor(new Color(40, 40, 40));
        g2.fillOval(x - 11, y + 6, 7, 7);
        g2.fillOval(x + 5, y + 6, 7, 7);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.drawString("BUS-01", x - 12, y - 14);
    }

    private void atualizarAnimacao() {

        if (rotaAtual.size() < 2) {
            return;
        }

        animacaoProgresso += 10;

        if (animacaoProgresso >= 100) {
            animacaoProgresso = 0;
            animacaoIndex = (animacaoIndex + 1) % (rotaAtual.size() - 1);
        }

        repaint();
    }

    private java.awt.Point converterParaTela(Ponto ponto) {

        double largura = Math.max(1, getWidth() - 60);
        double altura = Math.max(1, getHeight() - 60);

        double fatorZoom = 1.0 + (zoom * 0.15);
        double centroLat = (LAT_MIN + LAT_MAX) / 2.0;
        double centroLon = (LON_MIN + LON_MAX) / 2.0;
        double faixaLat = (LAT_MAX - LAT_MIN) / fatorZoom;
        double faixaLon = (LON_MAX - LON_MIN) / fatorZoom;

        double xNorm = (ponto.getLongitude() - (centroLon - faixaLon / 2.0)) / faixaLon;
        double yNorm = ((centroLat + faixaLat / 2.0) - ponto.getLatitude()) / faixaLat;

        int x = 30 + (int) Math.round(xNorm * largura);
        int y = 30 + (int) Math.round(yNorm * altura);

        return new java.awt.Point(x, y);
    }
}
