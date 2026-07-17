package gui;

import model.Aresta;
import model.Ponto;
import model.Rota;
import model.TipoPonto;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import javax.swing.SwingUtilities;
import java.util.function.Consumer;

@SuppressWarnings({"serial", "this-escape"})
public class MapPanel extends JPanel {

    private final JXMapViewer mapViewer = new JXMapViewer();
    private final Map<Ponto, PontoWaypoint> waypoints = new LinkedHashMap<>();
    private final Map<TipoPonto, JToggleButton> botoesLegenda = new LinkedHashMap<>();
    private JPanel corpoLegenda;
    private JButton minimizarLegenda;
    private final List<Aresta> arestasAtuais = new ArrayList<>();
    private Consumer<Ponto> onPontoSelecionado;
    private Consumer<TipoPonto> onTipoLegendaAlternado;
    private Ponto pontoSelecionado;
    private Ponto origemRota;
    private Ponto destinoRota;
    private RoutePainter routePainter;
    private RouteAnimation.Frame animationFrame;
    private Consumer<Double> onAnimationProgress;
    private final RouteAnimation animation = new RouteAnimation(
            frame -> {
                animationFrame = frame;
                if (onAnimationProgress != null) onAnimationProgress.accept(frame.progress);
                mapViewer.repaint();
            },
            () -> mapViewer.repaint());

    public MapPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        setBorder(javax.swing.BorderFactory.createLineBorder(UiTheme.BORDER));
        setBackground(UiTheme.SURFACE);

        mapViewer.setTileFactory(new DefaultTileFactory(new SecureOSMTileFactoryInfo()));
        mapViewer.setCenterPosition(new GeoPosition(-12.6723, -39.1054));
        mapViewer.setZoom(4);
        mapViewer.setBackground(new Color(233, 236, 240));
        mapViewer.setPanEnabled(true);
        mapViewer.setRestrictOutsidePanning(false);
        mapViewer.setHorizontalWrapped(false);
        mapViewer.setToolTipText("");
        mapViewer.setInheritsPopupMenu(true);

        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        MouseAdapter pontoListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selecionarPonto(e.getPoint());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                atualizarTooltip(e.getPoint());
            }
        };
        mapViewer.addMouseListener(pontoListener);
        mapViewer.addMouseMotionListener(pontoListener);

        ToolTipManager.sharedInstance().registerComponent(mapViewer);
        JPanel legenda = criarLegendaDeMapa();
        JLayeredPane mapaComLegenda = new JLayeredPane() {
            @Override
            public void doLayout() {
                Dimension size = getSize();
                mapViewer.setBounds(0, 0, size.width, size.height);
                Dimension pref = legenda.getPreferredSize();
                int x = 12;
                int y = Math.max(12, size.height - pref.height - 12);
                legenda.setBounds(x, y, pref.width, pref.height);
            }
        };
        mapaComLegenda.add(mapViewer, JLayeredPane.DEFAULT_LAYER);
        mapaComLegenda.add(legenda, JLayeredPane.PALETTE_LAYER);
        add(mapaComLegenda, BorderLayout.CENTER);
        atualizarPaineis();
    }

    private JPanel criarLegendaDeMapa() {
        JPanel container = new JPanel(new BorderLayout(6, 4));
        container.setBackground(new Color(255, 255, 255, 238));
        container.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(175, 182, 190)),
                javax.swing.BorderFactory.createEmptyBorder(7, 8, 8, 8)));

        JPanel topo = new JPanel(new BorderLayout(8, 0));
        topo.setOpaque(false);
        JLabel titulo = new JLabel("Legenda");
        titulo.setFont(UiTheme.FONT_BOLD.deriveFont(12f));
        titulo.setForeground(new Color(40, 45, 52));
        minimizarLegenda = new JButton("-");
        minimizarLegenda.setMargin(new java.awt.Insets(0, 5, 0, 5));
        minimizarLegenda.setFocusable(false);
        minimizarLegenda.setToolTipText("Minimizar legenda");
        minimizarLegenda.addActionListener(e -> alternarLegenda());
        topo.add(titulo, BorderLayout.WEST);
        topo.add(minimizarLegenda, BorderLayout.EAST);

        corpoLegenda = new JPanel();
        corpoLegenda.setOpaque(false);
        corpoLegenda.setLayout(new BoxLayout(corpoLegenda, BoxLayout.Y_AXIS));
        adicionarTituloGrupo(corpoLegenda, "Pontos");
        adicionarBotaoLegenda(corpoLegenda, TipoPonto.ESCOLA, "E", "Escola", new Color(210, 50, 50));
        adicionarBotaoLegenda(corpoLegenda, TipoPonto.UNIVERSIDADE, "U", "Universidade", new Color(45, 90, 220));
        adicionarBotaoLegenda(corpoLegenda, TipoPonto.BAIRRO, "B", "Bairro", new Color(190, 140, 30));
        adicionarBotaoLegenda(corpoLegenda, TipoPonto.PONTO_EMBARQUE, "P", "Ponto de embarque", new Color(50, 160, 70));
        corpoLegenda.add(Box.createVerticalStrut(4));
        adicionarTituloGrupo(corpoLegenda, "Rota");
        adicionarItemLegenda(corpoLegenda, "S", "Inicio da rota", new Color(40, 120, 220));
        adicionarItemLegenda(corpoLegenda, "D", "Destino", new Color(230, 60, 60));

        container.add(topo, BorderLayout.NORTH);
        container.add(corpoLegenda, BorderLayout.CENTER);
        return container;
    }

    private void adicionarTituloGrupo(JPanel legenda, String texto) {
        JLabel label = new JLabel(texto);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(UiTheme.FONT_BOLD.deriveFont(10f));
        label.setForeground(new Color(60, 68, 78));
        legenda.add(label);
    }

    private void alternarLegenda() {
        boolean expandida = corpoLegenda.isVisible();
        corpoLegenda.setVisible(!expandida);
        minimizarLegenda.setText(expandida ? "+" : "-");
        minimizarLegenda.setToolTipText(expandida ? "Expandir legenda" : "Minimizar legenda");
        revalidate();
        if (getParent() != null) {
            getParent().doLayout();
        }
        repaint();
    }

    private void adicionarBotaoLegenda(JPanel legenda, TipoPonto tipo, String letra, String texto, Color cor) {
        JToggleButton item = new JToggleButton(texto, true);
        item.setIcon(new LegendaIcon(cor, letra));
        item.setHorizontalAlignment(JToggleButton.LEFT);
        item.setFocusPainted(false);
        item.setOpaque(false);
        item.setContentAreaFilled(false);
        item.setForeground(new Color(35, 42, 50));
        item.setFont(UiTheme.FONT.deriveFont(10f));
        item.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 0, 1, 0));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setToolTipText("Mostrar ou ocultar " + texto.toLowerCase());
        item.addActionListener(e -> {
            atualizarVisualBotaoLegenda(item, cor);
            if (onTipoLegendaAlternado != null) {
                onTipoLegendaAlternado.accept(tipo);
            }
        });
        botoesLegenda.put(tipo, item);
        legenda.add(item);
    }

    private void adicionarItemLegenda(JPanel legenda, String letra, String texto, Color cor) {
        JLabel item = new JLabel(texto, new LegendaIcon(cor, letra), JLabel.LEFT);
        item.setForeground(new Color(35, 42, 50));
        item.setFont(UiTheme.FONT.deriveFont(10f));
        item.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 0, 1, 0));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        legenda.add(item);
    }

    private void atualizarVisualBotaoLegenda(JToggleButton item, Color corAtiva) {
        if (item.isSelected()) {
            item.setIcon(new LegendaIcon(corAtiva, textoDoTipoLegenda(item.getText())));
            item.setForeground(new Color(35, 42, 50));
        } else {
            item.setIcon(new LegendaIcon(new Color(200, 205, 211), textoDoTipoLegenda(item.getText())));
            item.setForeground(new Color(90, 98, 108));
        }
    }

    private String textoDoTipoLegenda(String texto) {
        if ("Escola".equals(texto)) return "E";
        if ("Universidade".equals(texto)) return "U";
        if ("Bairro".equals(texto)) return "B";
        if ("Ponto de embarque".equals(texto)) return "P";
        return "?";
    }

    public void setOnPontoSelecionado(Consumer<Ponto> callback) {
        this.onPontoSelecionado = callback;
    }

    public void setOnTipoLegendaAlternado(Consumer<TipoPonto> callback) {
        this.onTipoLegendaAlternado = callback;
    }

    public void setTipoLegendaVisivel(TipoPonto tipo, boolean visivel) {
        JToggleButton botao = botoesLegenda.get(tipo);
        if (botao != null) {
            botao.setSelected(visivel);
            atualizarVisualBotaoLegenda(botao, corDoTipo(tipo));
        }
    }

    public void selecionarPonto(Ponto ponto) {
        pontoSelecionado = ponto;
        atualizarPaineis();
    }

    public void setPontos(List<Ponto> pontos) {
        waypoints.clear();
        if (pontos != null) {
            for (Ponto ponto : pontos) {
                waypoints.put(ponto, new PontoWaypoint(ponto));
            }
        }
        atualizarPaineis();
    }

    public void destacarRota(Rota rota, Color cor) {
        animation.pause();
        arestasAtuais.clear();
        routePainter = null;
        origemRota = null;
        destinoRota = null;
        if (rota != null) {
            arestasAtuais.addAll(rota.getArestas());
            if (!rota.getPercurso().isEmpty()) {
                origemRota = rota.getPercurso().get(0);
                destinoRota = rota.getPercurso().get(rota.getPercurso().size() - 1);
            }
            routePainter = new RoutePainter(rota, cor);
            if (rota.getGeometria().size() >= 2) {
                animation.setRoute(rota);
            } else {
                animationFrame = null;
            }
            enquadrarRota(rota);
        }
        atualizarPaineis();
    }

    public void limparRota() {
        animation.stop();
        animationFrame = null;
        arestasAtuais.clear();
        routePainter = null;
        atualizarPaineis();
    }

    public void centralizarCruzDasAlmas() {
        mapViewer.setCenterPosition(new GeoPosition(-12.6723, -39.1054));
        mapViewer.setZoom(4);
    }

    private void enquadrarRota(Rota rota) {
        if (rota.getGeometria().isEmpty()) return;
        SwingUtilities.invokeLater(() -> mapViewer.zoomToBestFit(
                new LinkedHashSet<>(rota.getGeometria()), 0.85));
    }

    public void recarregarMapa() {
        // A troca da fábrica descarta o cache de tiles sem alterar rota ou animação.
        mapViewer.setTileFactory(new DefaultTileFactory(new SecureOSMTileFactoryInfo()));
        mapViewer.repaint();
    }

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }

    public void iniciarAnimacao(double velocidadeKmh) {
        animation.setSpeedKmh(velocidadeKmh);
        animation.play();
    }

    public void pausarAnimacao() {
        animation.pause();
    }

    public void pararAnimacao() {
        animation.stop();
    }

    public boolean isAnimacaoExecutando() {
        return animation.isRunning();
    }

    public void setOnAnimationProgress(Consumer<Double> callback) {
        onAnimationProgress = callback;
    }

    private void atualizarPaineis() {
        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        if (routePainter != null) {
            painters.add(routePainter);
        }
        painters.add(criarPainterDePontos());
        painters.add(criarPainterDoVeiculo());

        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(compoundPainter);
        mapViewer.repaint();
    }

    private Painter<JXMapViewer> criarPainterDoVeiculo() {
        return (Graphics2D g, JXMapViewer map, int w, int h) -> {
            RouteAnimation.Frame frame = animationFrame;
            if (frame == null) return;
            Point2D world = map.getTileFactory().geoToPixel(frame.position, map.getZoom());
            Rectangle viewport = map.getViewportBounds();
            double x = world.getX() - viewport.getX();
            double y = world.getY() - viewport.getY();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            // Latitude cresce para cima, enquanto o eixo Y da tela cresce para baixo.
            g2.rotate(-frame.angle);
            Path2D bus = new Path2D.Double();
            bus.moveTo(14, 0);
            bus.lineTo(-10, -8);
            bus.lineTo(-7, 0);
            bus.lineTo(-10, 8);
            bus.closePath();
            g2.setColor(new Color(255, 193, 7));
            g2.fill(bus);
            g2.setColor(new Color(45, 45, 45));
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.draw(bus);
            g2.dispose();
        };
    }

    private void selecionarPonto(Point point) {
        Ponto clicado = localizarPonto(point);
        if (clicado != null) {
            pontoSelecionado = clicado;
            atualizarPaineis();
            if (onPontoSelecionado != null) {
                onPontoSelecionado.accept(clicado);
            }
        }
    }

    private void atualizarTooltip(Point point) {
        Ponto ponto = localizarPonto(point);
        if (ponto != null) {
            mapViewer.setToolTipText(ponto.getDescricaoCompleta());
        } else {
            mapViewer.setToolTipText(null);
        }
    }

    private Ponto localizarPonto(Point point) {
        for (Map.Entry<Ponto, PontoWaypoint> entry : waypoints.entrySet()) {
            Point2D pos = mapViewer.getTileFactory().geoToPixel(entry.getValue().getPosition(), mapViewer.getZoom());
            Rectangle viewport = mapViewer.getViewportBounds();
            Point2D ajustado = new Point2D.Double(pos.getX() - viewport.getX(), pos.getY() - viewport.getY());
            double distancia = ajustado.distance(point);
            if (distancia <= 14) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Painter<JXMapViewer> criarPainterDePontos() {
        return (Graphics2D g, JXMapViewer map, int w, int h) -> {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle viewport = map.getViewportBounds();

            for (Map.Entry<Ponto, PontoWaypoint> entry : waypoints.entrySet()) {
                Ponto ponto = entry.getKey();
                Point2D pixel = map.getTileFactory().geoToPixel(entry.getValue().getPosition(), map.getZoom());
                double x = pixel.getX() - viewport.getX();
                double y = pixel.getY() - viewport.getY();

                boolean selecionado = pontoSelecionado != null && pontoSelecionado.equals(ponto);
                boolean ehOrigem = origemRota != null && origemRota.equals(ponto);
                boolean ehDestino = destinoRota != null && destinoRota.equals(ponto);

                Color cor = corDoTipo(ponto.getTipo());
                if (ehOrigem) {
                    cor = new Color(40, 120, 220);
                } else if (ehDestino) {
                    cor = new Color(230, 60, 60);
                } else if (selecionado) {
                    cor = cor.brighter();
                }

                int size = selecionado ? 28 : (ehOrigem || ehDestino ? 24 : 18);
                int x0 = (int) Math.round(x - size / 2.0);
                int y0 = (int) Math.round(y - size / 2.0);

                g2.setColor(cor);
                g2.fill(new Ellipse2D.Double(x0, y0, size, size));
                g2.setColor(Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.draw(new Ellipse2D.Double(x0, y0, size, size));

                String texto = textoDoPonto(ponto, ehOrigem, ehDestino);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (int) Math.round(x - fm.stringWidth(texto) / 2.0);
                int ty = (int) Math.round(y + fm.getAscent() / 2.0 - 1);
                g2.drawString(texto, tx, ty);
            }

            g2.dispose();
        };
    }

    private Color corDoTipo(model.TipoPonto tipo) {
        if (tipo == model.TipoPonto.ESCOLA) {
            return new Color(210, 50, 50);
        }
        if (tipo == model.TipoPonto.UNIVERSIDADE) {
            return new Color(45, 90, 220);
        }
        if (tipo == model.TipoPonto.BAIRRO) {
            return new Color(190, 140, 30);
        }
        if (tipo == model.TipoPonto.PONTO_EMBARQUE) {
            return new Color(50, 160, 70);
        }
        return new Color(120, 120, 120);
    }

    private String textoDoPonto(Ponto ponto, boolean ehOrigem, boolean ehDestino) {
        if (ehOrigem) {
            return "S";
        }
        if (ehDestino) {
            return "D";
        }
        switch (ponto.getTipo()) {
            case ESCOLA:
                return "E";
            case UNIVERSIDADE:
                return "U";
            case BAIRRO:
                return "B";
            case PONTO_EMBARQUE:
                return "P";
            default:
                return "?";
        }
    }

    private static class LegendaIcon implements Icon {
        private final Color cor;
        private final String letra;

        private LegendaIcon(Color cor, String letra) {
            this.cor = cor;
            this.letra = letra;
        }

        @Override
        public int getIconWidth() {
            return 22;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.fill(new Ellipse2D.Double(x + 1, y + 1, 16, 16));
            g2.setColor(Color.WHITE);
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.draw(new Ellipse2D.Double(x + 1, y + 1, 16, 16));
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + 1 + (16 - fm.stringWidth(letra)) / 2;
            int ty = y + 1 + (16 + fm.getAscent()) / 2 - 2;
            g2.drawString(letra, tx, ty);
            g2.dispose();
        }
    }
}
