package gui;

import model.Grafo;
import model.Ponto;
import model.Rota;
import model.TipoPonto;
import services.ProjetoService;
import services.RouteService;
import services.SimulationService;
import services.DadosIniciaisService;
import util.CSVExporter;
import util.PDFExporter;
import util.TXTExporter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"serial", "this-escape"})
public class MainWindow extends JFrame {

    private enum Operacao {
        NENHUMA,
        DIJKSTRA,
        PRIM,
        KRUSKAL,
        BFS,
        DFS,
        GULOSO,
        TSP
    }

    private final Grafo grafo = new Grafo();
    private final SimulationService simulationService = new SimulationService(grafo);
    private final RouteService routeService = new RouteService(grafo);
    private final ProjetoService projetoService = new ProjetoService();

    private final MapPanel mapPanel = new MapPanel();
    private final NavigationTreePanel treePanel = new NavigationTreePanel();
    private final DetailsPanel detailsPanel = new DetailsPanel();
    private final BottomRoutePanel bottomRoutePanel = new BottomRoutePanel();
    private final StatisticsPanel statisticsPanel = new StatisticsPanel();
    private final JTabbedPane telas = new JTabbedPane();
    private final JLabel statusLabel = new JLabel("Pronto");
    private final JProgressBar progressBar = new JProgressBar();
    private final JSpinner velocidadeAnimacao = new JSpinner(new SpinnerNumberModel(40, 5, 120, 5));
    private final JPanel painelResumoConteudo = new JPanel(new BorderLayout(8, 8));
    private final JLabel lblMapaDistancia = new JLabel("Distância percorrida: 0.00 km");
    private final JLabel lblMapaAlunos = new JLabel("Alunos: 0");
    private JButton btnAlternarResumo;
    private JPanel painelResumoCompacto;
    private JPanel painelResumoLateral;
    private boolean resumoExpandido = false;

    private final EnumMap<TipoPonto, Boolean> tiposVisiveis = new EnumMap<>(TipoPonto.class);

    private Operacao ultimaOperacao = Operacao.NENHUMA;
    private Ponto ultimaOrigem;
    private Ponto ultimoDestino;
    private Rota rotaAtual;
    private Ponto pontoSelecionado;

    public MainWindow() {
        super("Sistema de Planejamento de Rotas - Cruz das Almas/BA");
        configurarVisual();
        configurarMenu();
        configurarLayout();
        configurarEventos();

        for (TipoPonto tipo : TipoPonto.values()) {
            tiposVisiveis.put(tipo, Boolean.TRUE);
        }

        simulationService.adicionarObservador(this::atualizarTudo);
        simulationService.carregarPadrao();
        mapPanel.centralizarCruzDasAlmas();
    }

    private void configurarVisual() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 780));
        getContentPane().setBackground(UiTheme.BACKGROUND);
    }

    private void configurarLayout() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        setContentPane(root);

        root.add(criarCabecalho(), BorderLayout.NORTH);
        root.add(criarTelas(), BorderLayout.CENTER);
        root.add(criarBarraStatus(), BorderLayout.SOUTH);
    }

    private JTabbedPane criarTelas() {
        telas.setFont(UiTheme.FONT_BOLD.deriveFont(14f));
        telas.setBackground(UiTheme.SURFACE);
        telas.setForeground(UiTheme.TEXT);
        telas.setBorder(BorderFactory.createEmptyBorder());
        telas.addTab("  Mapa  ", criarTelaMapa());
        telas.setToolTipTextAt(0, "Mapa, estatísticas, percurso e animação do veículo");
        return telas;
    }

    private JPanel criarTelaPrincipal() {
        JPanel direita = new JPanel(new BorderLayout(8, 8));
        direita.setOpaque(false);
        statisticsPanel.setPreferredSize(new Dimension(300, 210));
        direita.add(statisticsPanel, BorderLayout.NORTH);
        direita.add(detailsPanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, direita);
        split.setResizeWeight(0.68);
        split.setDividerLocation(760);
        configurarDivisor(split);

        JPanel tela = new JPanel(new BorderLayout());
        tela.setBackground(UiTheme.BACKGROUND);
        tela.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        tela.add(split, BorderLayout.CENTER);
        return tela;
    }

    private JPanel criarTelaMapa() {
        JPanel tela = new JPanel(new BorderLayout());
        tela.setBackground(UiTheme.BACKGROUND);
        tela.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        painelResumoCompacto = criarResumoCompactoMapa();
        JLayeredPane mapaComResumo = new JLayeredPane() {
            @Override
            public void doLayout() {
                Dimension size = getSize();
                mapPanel.setBounds(0, 0, size.width, size.height);
                Dimension pref = painelResumoCompacto.getPreferredSize();
                int x = Math.max(12, size.width - pref.width - 12);
                painelResumoCompacto.setBounds(x, 12, pref.width, pref.height);
            }
        };
        mapaComResumo.add(mapPanel, JLayeredPane.DEFAULT_LAYER);
        mapaComResumo.add(painelResumoCompacto, JLayeredPane.PALETTE_LAYER);

        painelResumoLateral = criarResumoLateralMapa();
        painelResumoLateral.setVisible(false);

        tela.add(mapaComResumo, BorderLayout.CENTER);
        tela.add(painelResumoLateral, BorderLayout.EAST);
        return tela;
    }

    private JPanel criarResumoCompactoMapa() {
        JPanel painel = new JPanel(new BorderLayout(10, 0));
        painel.setBackground(UiTheme.SURFACE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JPanel indicadores = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        indicadores.setOpaque(false);
        lblMapaDistancia.setFont(UiTheme.FONT_BOLD.deriveFont(13f));
        lblMapaAlunos.setFont(UiTheme.FONT_BOLD.deriveFont(13f));
        lblMapaDistancia.setForeground(UiTheme.PRIMARY_DARK);
        lblMapaAlunos.setForeground(UiTheme.PRIMARY_DARK);
        indicadores.add(lblMapaDistancia);
        indicadores.add(lblMapaAlunos);

        btnAlternarResumo = new RoundedButton("Expandir", new Color(234, 239, 245), UiTheme.TEXT);
        btnAlternarResumo.addActionListener(e -> alternarResumoSuperior());
        painel.add(indicadores, BorderLayout.CENTER);
        painel.add(btnAlternarResumo, BorderLayout.EAST);
        return painel;
    }

    private JPanel criarResumoLateralMapa() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBackground(UiTheme.SURFACE);
        painel.setPreferredSize(new Dimension(390, 0));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JPanel topo = new JPanel(new BorderLayout(8, 0));
        topo.setOpaque(false);
        JLabel titulo = new JLabel("Estatísticas e resumo da rota");
        titulo.setFont(UiTheme.FONT_BOLD.deriveFont(14f));
        titulo.setForeground(UiTheme.PRIMARY_DARK);
        JButton recolher = new RoundedButton("Recolher", new Color(234, 239, 245), UiTheme.TEXT);
        recolher.addActionListener(e -> alternarResumoSuperior());
        topo.add(titulo, BorderLayout.WEST);
        topo.add(recolher, BorderLayout.EAST);

        painelResumoConteudo.setOpaque(false);
        statisticsPanel.setPreferredSize(new Dimension(360, 180));
        bottomRoutePanel.setPreferredSize(new Dimension(360, 0));
        painelResumoConteudo.add(statisticsPanel, BorderLayout.NORTH);
        painelResumoConteudo.add(bottomRoutePanel, BorderLayout.CENTER);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(painelResumoConteudo, BorderLayout.CENTER);
        return painel;
    }

    private void alternarResumoSuperior() {
        resumoExpandido = !resumoExpandido;
        if (painelResumoCompacto != null) {
            painelResumoCompacto.setVisible(!resumoExpandido);
        }
        if (painelResumoLateral != null) {
            painelResumoLateral.setVisible(resumoExpandido);
        }
        revalidate();
        repaint();
    }

    private JPanel criarTelaRota() {
        JPanel tela = new JPanel(new BorderLayout());
        tela.setBackground(UiTheme.BACKGROUND);
        tela.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        tela.add(bottomRoutePanel, BorderLayout.CENTER);
        return tela;
    }

    private void configurarDivisor(JSplitPane split) {
        split.setBorder(null);
        split.setDividerSize(8);
        split.setContinuousLayout(true);
        split.setOpaque(false);
    }

    private JPanel criarBarraStatus() {
        JPanel status = new JPanel(new BorderLayout(8, 8));
        status.setBackground(UiTheme.SURFACE);
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(7, 10, 3, 10)));
        statusLabel.setForeground(UiTheme.TEXT_MUTED);
        progressBar.setIndeterminate(false);
        progressBar.setPreferredSize(new Dimension(190, 12));
        progressBar.setBorderPainted(false);
        status.add(statusLabel, BorderLayout.CENTER);
        status.add(progressBar, BorderLayout.EAST);
        return status;
    }

    private JPanel criarCabecalho() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setBackground(UiTheme.PRIMARY_DARK);
        brand.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 12));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Transporte Universitário");
        titulo.setFont(UiTheme.FONT_BOLD.deriveFont(20f));
        titulo.setForeground(Color.WHITE);
        JLabel subtitulo = new JLabel("Planejamento inteligente de rotas • Cruz das Almas/BA");
        subtitulo.setForeground(new Color(205, 222, 238));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);
        brand.add(textos, BorderLayout.WEST);
        brand.add(criarAcoesDeCadastro(), BorderLayout.EAST);

        container.add(brand);
        container.add(Box.createVerticalStrut(7));
        container.add(criarToolbar());
        return container;
    }

    private JToolBar criarAcoesDeCadastro() {
        JToolBar toolbar = novaToolbar();
        toolbar.setBackground(UiTheme.PRIMARY_DARK);
        adicionarBotao(toolbar, "+ Novo ponto", UiTheme.ACCENT, Color.WHITE, e -> novoPonto());
        adicionarBotao(toolbar, "Editar", new Color(54, 91, 128), Color.WHITE, e -> editarPontoSelecionado());
        adicionarBotao(toolbar, "Excluir", UiTheme.DANGER, Color.WHITE, e -> removerPontoSelecionado());
        return toolbar;
    }

    private JToolBar criarToolbar() {
        JToolBar toolbar = novaToolbar();
        toolbar.setBackground(UiTheme.SURFACE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        adicionarSecao(toolbar, "ROTAS");
        adicionarBotao(toolbar, "Dijkstra", UiTheme.PRIMARY, Color.WHITE, e -> executarDijkstra());
        adicionarBotao(toolbar, "Prim", e -> executarPrim());
        adicionarBotao(toolbar, "Kruskal", e -> executarKruskal());
        adicionarBotao(toolbar, "BFS", e -> executarBFS());
        adicionarBotao(toolbar, "DFS", e -> executarDFS());
        adicionarBotao(toolbar, "Guloso", e -> executarGuloso());
        adicionarBotao(toolbar, "TSP", e -> executarTSP());
        toolbar.addSeparator();
        adicionarSecao(toolbar, "SIMULAÇÃO");
        adicionarBotao(toolbar, "▶ Iniciar", UiTheme.ACCENT, Color.WHITE, e -> iniciarAnimacao());
        adicionarBotao(toolbar, "Pausar", e -> pausarAnimacao());
        adicionarBotao(toolbar, "Parar", e -> pararAnimacao());
        JLabel velocidade = new JLabel("  Velocidade ");
        velocidade.setForeground(UiTheme.TEXT_MUTED);
        toolbar.add(velocidade);
        velocidadeAnimacao.setMaximumSize(new Dimension(65, 28));
        velocidadeAnimacao.setToolTipText("Velocidade média do veículo em km/h");
        toolbar.add(velocidadeAnimacao);
        toolbar.add(new JLabel(" km/h"));
        toolbar.add(Box.createHorizontalGlue());
        adicionarBotao(toolbar, "Limpar rota", e -> limparRota());
        adicionarBotao(toolbar, "Centralizar", e -> mapPanel.centralizarCruzDasAlmas());
        adicionarBotao(toolbar, "Atualizar mapa", e -> mapPanel.recarregarMapa());
        adicionarBotao(toolbar, "Restaurar mapa", e -> restaurarMapaPadrao());
        adicionarBotao(toolbar, "Ponto", e -> abrirJanelaPontos());
        return toolbar;
    }

    private JToolBar novaToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(null);
        return toolbar;
    }

    private void adicionarSecao(JToolBar toolbar, String texto) {
        JLabel label = new JLabel(texto + "  ");
        label.setFont(UiTheme.FONT_BOLD.deriveFont(10f));
        label.setForeground(UiTheme.TEXT_MUTED);
        toolbar.add(label);
    }

    private void adicionarBotao(JToolBar toolbar, String texto, java.awt.event.ActionListener listener) {
        adicionarBotao(toolbar, texto, new Color(234, 239, 245), UiTheme.TEXT, listener);
    }

    private void adicionarBotao(JToolBar toolbar, String texto, Color fundo, Color frente,
                                 java.awt.event.ActionListener listener) {
        JButton button = new RoundedButton(texto, fundo, frente);
        button.addActionListener(listener);
        toolbar.add(button);
        toolbar.add(Box.createHorizontalStrut(4));
    }

    private void configurarMenu() {
        JMenuBar bar = new JMenuBar();

        JMenu arquivo = new JMenu("Arquivo");
        arquivo.add(item("Novo Projeto", e -> novoProjeto()));
        arquivo.add(item("Abrir", e -> abrirProjeto()));
        arquivo.add(item("Salvar", e -> salvarProjeto()));
        arquivo.addSeparator();
        arquivo.add(item("Exportar PDF", e -> exportarPdf()));
        arquivo.add(item("Exportar CSV", e -> exportarCsv()));
        arquivo.add(item("Exportar TXT", e -> exportarTxt()));
        arquivo.addSeparator();
        arquivo.add(item("Sair", e -> dispose()));

        JMenu visualizar = new JMenu("Visualizar");
        visualizar.add(item("Centralizar mapa", e -> mapPanel.centralizarCruzDasAlmas()));
        visualizar.add(item("Recarregar mapa", e -> mapPanel.recarregarMapa()));
        visualizar.addSeparator();
        visualizar.add(checkItem("Mostrar bairros", TipoPonto.BAIRRO));
        visualizar.add(checkItem("Mostrar escolas", TipoPonto.ESCOLA));
        visualizar.add(checkItem("Mostrar universidades", TipoPonto.UNIVERSIDADE));
        visualizar.add(checkItem("Mostrar pontos", TipoPonto.PONTO_EMBARQUE));

        JMenu navegar = new JMenu("Telas");
        navegar.add(item("Mapa", e -> mostrarMapa()));

        JMenu algoritmos = new JMenu("Algoritmos");
        algoritmos.add(item("Dijkstra", e -> executarDijkstra()));
        algoritmos.add(item("Prim", e -> executarPrim()));
        algoritmos.add(item("Kruskal", e -> executarKruskal()));
        algoritmos.add(item("BFS", e -> executarBFS()));
        algoritmos.add(item("DFS", e -> executarDFS()));
        algoritmos.add(item("Guloso", e -> executarGuloso()));
        algoritmos.add(item("Caixeiro Viajante", e -> executarTSP()));

        JMenu ajuda = new JMenu("Ajuda");
        ajuda.add(item("Sobre", e -> JOptionPane.showMessageDialog(this,
                "Sistema de Planejamento de Rotas\nCruz das Almas - BA\nJava Swing + JXMapViewer2",
                "Sobre", JOptionPane.INFORMATION_MESSAGE)));

        bar.add(arquivo);
        bar.add(navegar);
        bar.add(visualizar);
        bar.add(algoritmos);
        bar.add(ajuda);
        setJMenuBar(bar);
    }

    private JMenuItem item(String texto, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(listener);
        return item;
    }

    private JCheckBoxMenuItem checkItem(String texto, TipoPonto tipo) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(texto, true);
        item.addActionListener(e -> {
            tiposVisiveis.put(tipo, item.isSelected());
            atualizarTudo();
        });
        return item;
    }

    private void configurarEventos() {
        mapPanel.setOnPontoSelecionado(ponto -> {
            pontoSelecionado = ponto;
            mapPanel.selecionarPonto(ponto);
            mostrarDetalhes(ponto);
        });
        treePanel.setOnSelecionado(ponto -> {
            pontoSelecionado = ponto;
            mapPanel.selecionarPonto(ponto);
            mostrarDetalhes(ponto);
        });
        mapPanel.setOnAnimationProgress(progresso -> {
            int percentual = (int) Math.round(progresso * 100.0);
            progressBar.setIndeterminate(false);
            progressBar.setValue(percentual);
            statusLabel.setText("Veículo em movimento: " + percentual + "% da rota");
        });
        mapPanel.setOnTipoLegendaAlternado(tipo -> {
            boolean atual = Boolean.TRUE.equals(tiposVisiveis.get(tipo));
            tiposVisiveis.put(tipo, !atual);
            atualizarTudo();
        });
    }

    private void iniciarAnimacao() {
        if (rotaAtual == null || rotaAtual.getPercurso().size() < 2) {
            JOptionPane.showMessageDialog(this, "Calcule uma rota com pelo menos dois pontos antes de iniciar.");
            return;
        }
        mostrarMapa();
        mapPanel.iniciarAnimacao(((Number) velocidadeAnimacao.getValue()).doubleValue());
    }

    private void pausarAnimacao() {
        mapPanel.pausarAnimacao();
        setStatus("Animação pausada");
    }

    private void pararAnimacao() {
        mapPanel.pararAnimacao();
        progressBar.setValue(0);
        setStatus("Animação reiniciada");
    }

    private void mostrarMapa() {
        telas.setSelectedIndex(0);
    }

    private void mostrarDetalhes(Ponto ponto) {
        if (ponto == null) {
            detailsPanel.limpar();
            return;
        }
        detailsPanel.mostrar(ponto, grafo);
    }

    private void atualizarTudo() {
        List<Ponto> pontosVisiveis = pontosVisiveis();
        treePanel.atualizar(pontosVisiveis);
        mapPanel.setPontos(pontosVisiveis);
        if (rotaAtual != null) {
            // Pontos são mutáveis: garante atualização imediata após editar latitude/longitude.
            if (!rotaAtual.isTrajetoViario()) rotaAtual.atualizarGeometriaLocal();
            mapPanel.destacarRota(rotaAtual, corDaOperacao(ultimaOperacao));
            bottomRoutePanel.mostrar(rotaAtual);
            statisticsPanel.atualizar(grafo, rotaAtual, algoritmoAtivo());
            atualizarIndicadoresMapa(rotaAtual);
        } else {
            mapPanel.limparRota();
            bottomRoutePanel.limpar();
            statisticsPanel.limpar();
            statisticsPanel.atualizar(grafo, null, null);
            atualizarIndicadoresMapa(null);
        }
        statusLabel.setText("Pontos: " + grafo.getPontos().size() + " | Arestas: " + grafo.getArestas().size());
        if (pontoSelecionado != null && grafo.getPontos().contains(pontoSelecionado)) {
            mapPanel.selecionarPonto(pontoSelecionado);
            mostrarDetalhes(pontoSelecionado);
        } else if (ultimaOperacao == Operacao.NENHUMA) {
            mostrarDetalhes(null);
        }
    }

    private void atualizarIndicadoresMapa(Rota rota) {
        if (rota != null) {
            lblMapaDistancia.setText(String.format("Distância percorrida: %.2f km", rota.getDistanciaTotal()));
            lblMapaAlunos.setText("Alunos: " + rota.calcularTotalAlunos());
            return;
        }
        int totalAlunos = grafo.getPontos().stream().mapToInt(Ponto::getQuantidadeAlunos).sum();
        lblMapaDistancia.setText("Distância percorrida: 0.00 km");
        lblMapaAlunos.setText("Alunos: " + totalAlunos);
    }

    private List<Ponto> pontosVisiveis() {
        List<Ponto> lista = new ArrayList<>();
        for (Ponto ponto : grafo.getPontos()) {
            if (Boolean.TRUE.equals(tiposVisiveis.get(ponto.getTipo()))) {
                lista.add(ponto);
            }
        }
        for (TipoPonto tipo : TipoPonto.values()) {
            mapPanel.setTipoLegendaVisivel(tipo, Boolean.TRUE.equals(tiposVisiveis.get(tipo)));
        }
        return lista;
    }

    private void novoProjeto() {
        grafo.limpar();
        rotaAtual = null;
        routeService.limpar();
        ultimaOperacao = Operacao.NENHUMA;
        ultimaOrigem = null;
        ultimoDestino = null;
        pontoSelecionado = null;
        simulationService.carregarPadrao();
    }

    private void restaurarMapaPadrao() {
        if (JOptionPane.showConfirmDialog(this,
                "Restaurar mapa e dados padrao de 1500 alunos?",
                "Restaurar mapa",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        grafo.limpar();
        rotaAtual = null;
        routeService.limpar();
        ultimaOperacao = Operacao.NENHUMA;
        ultimaOrigem = null;
        ultimoDestino = null;
        pontoSelecionado = null;
        simulationService.carregarPadrao();
        mapPanel.centralizarCruzDasAlmas();
        setStatus("Mapa restaurado com dados padrao");
    }

    private void abrirProjeto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Projeto de rotas (*.rotas)", "rotas", "txt"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Grafo carregado = projetoService.carregar(chooser.getSelectedFile());
            substituirGrafo(carregado);
            setStatus("Projeto carregado: " + chooser.getSelectedFile().getName());
        } catch (IOException ex) {
            erro("Erro ao abrir projeto", ex);
        }
    }

    private void salvarProjeto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Projeto de rotas (*.rotas)", "rotas", "txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File arquivo = ajustarExtensao(chooser.getSelectedFile(), "rotas");
        try {
            projetoService.salvar(grafo, arquivo);
            setStatus("Projeto salvo: " + arquivo.getName());
        } catch (IOException ex) {
            erro("Erro ao salvar projeto", ex);
        }
    }

    private void exportarCsv() {
        Rota rota = rotaAtual;
        if (rota == null) {
            JOptionPane.showMessageDialog(this, "Calcule uma rota antes de exportar.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File arquivo = ajustarExtensao(chooser.getSelectedFile(), "csv");
        try {
            CSVExporter.exportarResultado(rota, algoritmoAtivo(), arquivo);
            setStatus("CSV exportado: " + arquivo.getName());
        } catch (IOException ex) {
            erro("Erro ao exportar CSV", ex);
        }
    }

    private void exportarPdf() {
        Rota rota = rotaAtual;
        if (rota == null) {
            JOptionPane.showMessageDialog(this, "Calcule uma rota antes de exportar.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File arquivo = ajustarExtensao(chooser.getSelectedFile(), "pdf");
        try {
            PDFExporter.exportarRelatorio(rota, algoritmoAtivo(), new ArrayList<>(grafo.getPontos()), arquivo);
            setStatus("PDF exportado: " + arquivo.getName());
        } catch (IOException ex) {
            erro("Erro ao exportar PDF", ex);
        }
    }

    private void exportarTxt() {
        Rota rota = rotaAtual;
        if (rota == null) {
            JOptionPane.showMessageDialog(this, "Calcule uma rota antes de exportar.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("TXT (*.txt)", "txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File arquivo = ajustarExtensao(chooser.getSelectedFile(), "txt");
        try {
            TXTExporter.exportar(rota, algoritmoAtivo(), new ArrayList<>(grafo.getPontos()), arquivo);
            setStatus("TXT exportado: " + arquivo.getName());
        } catch (IOException ex) {
            erro("Erro ao exportar TXT", ex);
        }
    }

    private void novoPonto() {
        PontoEditResult result = dialogoPonto(null);
        if (result == null) {
            return;
        }
        try {
            int embarque = valorEmbarquePermitido(result.tipo, result.alunos);
            int desembarque = valorDesembarquePermitido(result.tipo, result.desembarque);
            Ponto novo = simulationService.adicionarPonto(result.nome, result.bairro, result.latitude, result.longitude,
                    embarque, result.capacidade, result.prioridade, result.turno, result.tipo);
            novo.setQuantidadeDesembarque(desembarque);
            pontoSelecionado = novo;
            mapPanel.selecionarPonto(novo);
            mostrarDetalhes(novo);
            setStatus("Ponto adicionado: " + result.nome);
        } catch (IllegalArgumentException ex) {
            erro("Erro ao adicionar ponto", ex);
        }
    }

    private void editarPontoSelecionado() {
        Ponto selecionado = pontoSelecionado();
        if (selecionado == null) {
            return;
        }
        PontoEditResult result = dialogoPonto(selecionado);
        if (result == null) {
            return;
        }
        try {
            int embarque = valorEmbarquePermitido(result.tipo, result.alunos);
            int desembarque = valorDesembarquePermitido(result.tipo, result.desembarque);
            selecionado.setNome(result.nome);
            selecionado.setBairro(result.bairro);
            selecionado.setLatitude(result.latitude);
            selecionado.setLongitude(result.longitude);
            selecionado.setQuantidadeAlunos(embarque);
            selecionado.setQuantidadeDesembarque(desembarque);
            selecionado.setCapacidade(result.capacidade);
            selecionado.setPrioridade(result.prioridade);
            selecionado.setTurno(result.turno);
            selecionado.setTipo(result.tipo);
            if (rotaAtual != null) rotaAtual.atualizarGeometriaLocal();
            simulationService.atualizarPonto(selecionado);
            atualizarRotaViariaEmSegundoPlano();
            setStatus("Ponto atualizado: " + selecionado.getNome());
        } catch (IllegalArgumentException ex) {
            erro("Erro ao editar ponto", ex);
        }
    }

    private int valorEmbarquePermitido(TipoPonto tipo, int valor) {
        return DadosIniciaisService.isTipoEmbarque(tipo) ? valor : 0;
    }

    private int valorDesembarquePermitido(TipoPonto tipo, int valor) {
        return DadosIniciaisService.isTipoDesembarque(tipo) ? valor : 0;
    }

    private void atualizarRotaViariaEmSegundoPlano() {
        Rota rota = rotaAtual;
        if (rota == null || rota.getPercurso().size() < 2) return;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                routeService.atualizarTrajetoViario(rota);
                return null;
            }
            @Override protected void done() {
                if (rotaAtual == rota) atualizarTudo();
            }
        };
        worker.execute();
    }

    private void removerPontoSelecionado() {
        Ponto selecionado = pontoSelecionado();
        if (selecionado == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Remover \"" + selecionado.getNome() + "\"?", "Confirmar",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        pontoSelecionado = null;
        rotaAtual = null;
        ultimaOperacao = Operacao.NENHUMA;
        ultimaOrigem = null;
        ultimoDestino = null;
        routeService.limpar();
        simulationService.removerPonto(selecionado);
        mapPanel.selecionarPonto(null);
        detailsPanel.limpar();
        atualizarTudo();
        setStatus("Ponto removido: " + selecionado.getNome());
    }

    private Ponto pontoSelecionado() {
        if (pontoSelecionado != null && grafo.getPontos().contains(pontoSelecionado)) {
            return pontoSelecionado;
        }
        Ponto escolhido = escolherPonto("Selecione um ponto");
        if (escolhido != null) {
            pontoSelecionado = escolhido;
            mapPanel.selecionarPonto(escolhido);
            mostrarDetalhes(escolhido);
        }
        return escolhido;
    }

    private void abrirJanelaPontos() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Pontos", false);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(UiTheme.BACKGROUND);

        JTabbedPane abasPontos = new JTabbedPane();
        Map<java.awt.Component, JTable> tabelasPorAba = new java.util.LinkedHashMap<>();
        Map<java.awt.Component, PontoTableModel> modelosPorAba = new java.util.LinkedHashMap<>();
        adicionarAbaPontos(abasPontos, tabelasPorAba, modelosPorAba, "Escolas", TipoPonto.ESCOLA);
        adicionarAbaPontos(abasPontos, tabelasPorAba, modelosPorAba, "Universidades", TipoPonto.UNIVERSIDADE);
        adicionarAbaPontos(abasPontos, tabelasPorAba, modelosPorAba, "Bairros", TipoPonto.BAIRRO);
        adicionarAbaPontos(abasPontos, tabelasPorAba, modelosPorAba, "Embarques", TipoPonto.PONTO_EMBARQUE);
        adicionarAbaPontos(abasPontos, tabelasPorAba, modelosPorAba, "Outros", TipoPonto.OUTRO);
        atualizarAbasPontos(modelosPorAba);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        JLabel titulo = new JLabel("Selecione um ponto para destacar no mapa");
        titulo.setFont(UiTheme.FONT_BOLD.deriveFont(15f));
        titulo.setForeground(UiTheme.TEXT);
        topo.add(titulo, BorderLayout.WEST);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acoes.setOpaque(false);
        JButton novo = new RoundedButton("+ Novo", UiTheme.ACCENT, Color.WHITE);
        JButton editar = new RoundedButton("Editar", new Color(54, 91, 128), Color.WHITE);
        JButton remover = new RoundedButton("Remover", UiTheme.DANGER, Color.WHITE);
        JButton fechar = new RoundedButton("Fechar", new Color(234, 239, 245), UiTheme.TEXT);
        novo.addActionListener(e -> {
            novoPonto();
            atualizarAbasPontos(modelosPorAba);
        });
        editar.addActionListener(e -> {
            Ponto ponto = pontoDaAba(abasPontos, tabelasPorAba, modelosPorAba);
            if (ponto != null) pontoSelecionado = ponto;
            editarPontoSelecionado();
            atualizarAbasPontos(modelosPorAba);
        });
        remover.addActionListener(e -> {
            Ponto ponto = pontoDaAba(abasPontos, tabelasPorAba, modelosPorAba);
            if (ponto != null) pontoSelecionado = ponto;
            removerPontoSelecionado();
            atualizarAbasPontos(modelosPorAba);
        });
        fechar.addActionListener(e -> dialog.dispose());
        acoes.add(novo);
        acoes.add(editar);
        acoes.add(remover);
        acoes.add(fechar);

        JPanel rodape = new JPanel(new BorderLayout(8, 8));
        rodape.setOpaque(false);
        rodape.add(criarLegendaPontos(), BorderLayout.CENTER);
        rodape.add(acoes, BorderLayout.SOUTH);

        dialog.add(topo, BorderLayout.NORTH);
        dialog.add(abasPontos, BorderLayout.CENTER);
        dialog.add(rodape, BorderLayout.SOUTH);
        dialog.setSize(760, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void adicionarAbaPontos(JTabbedPane abas,
                                    Map<java.awt.Component, JTable> tabelasPorAba,
                                    Map<java.awt.Component, PontoTableModel> modelosPorAba,
                                    String titulo,
                                    TipoPonto tipo) {
        PontoTableModel model = new PontoTableModel();
        JTable tabela = new JTable(model);
        tabela.setRowHeight(26);
        tabela.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Ponto ponto = pontoDaTabela(tabela, model);
            if (ponto != null) {
                pontoSelecionado = ponto;
                mapPanel.selecionarPonto(ponto);
                mostrarDetalhes(ponto);
                mostrarMapa();
            }
        });
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarPontoSelecionado();
                    atualizarAbasPontos(modelosPorAba);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabela);
        abas.addTab(titulo, scroll);
        tabelasPorAba.put(scroll, tabela);
        modelosPorAba.put(scroll, model);
    }

    private void atualizarAbasPontos(Map<java.awt.Component, PontoTableModel> modelosPorAba) {
        for (Map.Entry<java.awt.Component, PontoTableModel> entry : modelosPorAba.entrySet()) {
            int index = ((JTabbedPane) entry.getKey().getParent()).indexOfComponent(entry.getKey());
            TipoPonto tipo = tipoPorIndiceAba(index);
            List<Ponto> pontos = new ArrayList<>();
            for (Ponto ponto : grafo.getPontos()) {
                if (ponto.getTipo() == tipo) {
                    pontos.add(ponto);
                }
            }
            entry.getValue().setPontos(pontos);
        }
    }

    private TipoPonto tipoPorIndiceAba(int index) {
        switch (index) {
            case 0:
                return TipoPonto.ESCOLA;
            case 1:
                return TipoPonto.UNIVERSIDADE;
            case 2:
                return TipoPonto.BAIRRO;
            case 3:
                return TipoPonto.PONTO_EMBARQUE;
            default:
                return TipoPonto.OUTRO;
        }
    }

    private Ponto pontoDaAba(JTabbedPane abas,
                             Map<java.awt.Component, JTable> tabelasPorAba,
                             Map<java.awt.Component, PontoTableModel> modelosPorAba) {
        java.awt.Component aba = abas.getSelectedComponent();
        JTable tabela = tabelasPorAba.get(aba);
        PontoTableModel model = modelosPorAba.get(aba);
        return tabela != null && model != null ? pontoDaTabela(tabela, model) : null;
    }

    private JPanel criarLegendaPontos() {
        JPanel legenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        legenda.setOpaque(false);
        adicionarItemLegenda(legenda, "E", "Escola", new Color(210, 50, 50));
        adicionarItemLegenda(legenda, "U", "Universidade", new Color(45, 90, 220));
        adicionarItemLegenda(legenda, "B", "Bairro", new Color(190, 140, 30));
        adicionarItemLegenda(legenda, "P", "Ponto de embarque", new Color(50, 160, 70));
        adicionarItemLegenda(legenda, "S", "Inicio da rota", new Color(40, 120, 220));
        adicionarItemLegenda(legenda, "D", "Destino", new Color(230, 60, 60));
        return legenda;
    }

    private void adicionarItemLegenda(JPanel legenda, String letra, String texto, Color cor) {
        JLabel item = new JLabel(letra + " " + texto);
        item.setOpaque(true);
        item.setBackground(cor);
        item.setForeground(Color.WHITE);
        item.setFont(UiTheme.FONT_BOLD.deriveFont(11f));
        item.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
        legenda.add(item);
    }

    private Ponto pontoDaTabela(JTable tabela, PontoTableModel model) {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.getPonto(tabela.convertRowIndexToModel(viewRow));
    }

    private PontoEditResult dialogoPonto(Ponto ponto) {
        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        javax.swing.JTextField nome = new javax.swing.JTextField(ponto != null ? ponto.getNome() : "");
        javax.swing.JTextField bairro = new javax.swing.JTextField(ponto != null ? ponto.getBairro() : "");
        javax.swing.JTextField latitude = new javax.swing.JTextField(ponto != null ? String.valueOf(ponto.getLatitude()) : "-12.6700");
        javax.swing.JTextField longitude = new javax.swing.JTextField(ponto != null ? String.valueOf(ponto.getLongitude()) : "-39.1000");
        javax.swing.JTextField alunos = new javax.swing.JTextField(ponto != null ? String.valueOf(ponto.getQuantidadeAlunos()) : "0");
        javax.swing.JTextField desembarque = new javax.swing.JTextField(ponto != null ? String.valueOf(ponto.getQuantidadeDesembarque()) : "0");
        javax.swing.JTextField capacidade = new javax.swing.JTextField(ponto != null ? String.valueOf(ponto.getCapacidade()) : "0");
        javax.swing.JTextField prioridade = new javax.swing.JTextField(ponto != null ? String.valueOf(ponto.getPrioridade()) : "1");
        javax.swing.JTextField turno = new javax.swing.JTextField(ponto != null ? ponto.getTurno() : "");
        javax.swing.JComboBox<TipoPonto> tipo = new javax.swing.JComboBox<>(TipoPonto.values());
        tipo.setSelectedItem(ponto != null ? ponto.getTipo() : TipoPonto.PONTO_EMBARQUE);

        int row = 0;
        adicionarCampo(painel, c, row++, "Nome", nome);
        adicionarCampo(painel, c, row++, "Bairro", bairro);
        adicionarCampo(painel, c, row++, "Latitude", latitude);
        adicionarCampo(painel, c, row++, "Longitude", longitude);
        adicionarCampo(painel, c, row++, "Alunos embarque", alunos);
        adicionarCampo(painel, c, row++, "Alunos desembarque", desembarque);
        adicionarCampo(painel, c, row++, "Capacidade", capacidade);
        adicionarCampo(painel, c, row++, "Prioridade", prioridade);
        adicionarCampo(painel, c, row++, "Turno", turno);
        adicionarCampo(painel, c, row++, "Tipo", tipo);

        int confirm = JOptionPane.showConfirmDialog(this, painel, ponto == null ? "Novo ponto" : "Editar ponto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) {
            return null;
        }

        try {
            return new PontoEditResult(
                    nome.getText().trim(),
                    bairro.getText().trim(),
                    Double.parseDouble(latitude.getText().trim()),
                    Double.parseDouble(longitude.getText().trim()),
                    Integer.parseInt(alunos.getText().trim()),
                    Integer.parseInt(desembarque.getText().trim()),
                    Integer.parseInt(capacidade.getText().trim()),
                    Integer.parseInt(prioridade.getText().trim()),
                    turno.getText().trim(),
                    (TipoPonto) tipo.getSelectedItem());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valores numericos invalidos.", "Erro", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void adicionarCampo(JPanel painel, GridBagConstraints c, int row, String rotulo, java.awt.Component comp) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0.0;
        painel.add(new JLabel(rotulo), c);
        c.gridx = 1;
        c.weightx = 1.0;
        painel.add(comp, c);
    }

    private File ajustarExtensao(File arquivo, String extensao) {
        if (arquivo.getName().toLowerCase().endsWith("." + extensao)) {
            return arquivo;
        }
        return new File(arquivo.getAbsolutePath() + "." + extensao);
    }

    private void executarDijkstra() {
        Ponto origem = escolherPonto("Selecionar origem");
        if (origem == null) {
            return;
        }
        Ponto destino = escolherPonto("Selecionar destino");
        if (destino == null) {
            return;
        }
        executarComProgresso(() -> {
            rotaAtual = routeService.executarDijkstra(origem, destino);
            ultimaOperacao = Operacao.DIJKSTRA;
            ultimaOrigem = origem;
            ultimoDestino = destino;
        }, Color.BLUE, "Dijkstra calculado");
    }

    private void executarPrim() {
        List<Ponto> pontos = escolherPontosOuTodos("Selecionar pontos para Prim", 2);
        if (pontos == null) {
            return;
        }
        Ponto origem = escolherPontoDaLista("Selecionar ponto inicial do Prim", pontos);
        if (origem == null) {
            return;
        }
        executarComProgresso(() -> {
            routeService.executarPrim(origem, pontos);
            rotaAtual = routeService.getRotaAtual();
            ultimaOperacao = Operacao.PRIM;
            ultimaOrigem = origem;
            ultimoDestino = null;
        }, new Color(30, 150, 60), "Prim calculado para " + pontos.size() + " pontos");
    }

    private void executarKruskal() {
        List<Ponto> pontos = escolherPontosOuTodos("Selecionar pontos para Kruskal", 2);
        if (pontos == null) {
            return;
        }
        Ponto origem = escolherPontoDaLista("Selecionar ponto inicial do Kruskal", pontos);
        if (origem == null) {
            return;
        }
        List<Ponto> pontosOrdenados = new ArrayList<>();
        pontosOrdenados.add(origem);
        for (Ponto ponto : pontos) {
            if (!ponto.equals(origem)) {
                pontosOrdenados.add(ponto);
            }
        }
        executarComProgresso(() -> {
            routeService.executarKruskal(pontosOrdenados);
            rotaAtual = routeService.getRotaAtual();
            ultimaOperacao = Operacao.KRUSKAL;
            ultimaOrigem = origem;
            ultimoDestino = null;
        }, new Color(210, 140, 40), "Kruskal calculado a partir de " + origem.getNome());
    }

    private void executarBFS() {
        List<Ponto> pontos = escolherPontosOuTodos("Selecionar pontos para BFS", 2);
        if (pontos == null) {
            return;
        }
        Ponto origem = escolherPontoDaLista("Selecionar ponto inicial do BFS", pontos);
        if (origem == null) {
            return;
        }
        executarComProgresso(() -> {
            rotaAtual = routeService.executarBFS(origem, pontos);
            ultimaOperacao = Operacao.BFS;
            ultimaOrigem = origem;
            ultimoDestino = null;
        }, Color.CYAN.darker(), "BFS calculado para " + pontos.size() + " pontos");
    }

    private void executarDFS() {
        List<Ponto> pontos = escolherPontosOuTodos("Selecionar pontos para DFS", 2);
        if (pontos == null) {
            return;
        }
        Ponto origem = escolherPontoDaLista("Selecionar ponto inicial do DFS", pontos);
        if (origem == null) {
            return;
        }
        executarComProgresso(() -> {
            rotaAtual = routeService.executarDFS(origem, pontos);
            ultimaOperacao = Operacao.DFS;
            ultimaOrigem = origem;
            ultimoDestino = null;
        }, Color.PINK.darker(), "DFS calculado para " + pontos.size() + " pontos");
    }

    private void executarGuloso() {
        List<Ponto> pontos = escolherPontosOuTodos("Selecionar pontos para Guloso", 2);
        if (pontos == null) {
            return;
        }
        Ponto origem = escolherPontoDaLista("Selecionar ponto inicial do Guloso", pontos);
        if (origem == null) {
            return;
        }
        executarComProgresso(() -> {
            rotaAtual = routeService.executarGuloso(origem, pontos);
            ultimaOperacao = Operacao.GULOSO;
            ultimaOrigem = origem;
            ultimoDestino = null;
        }, new Color(140, 60, 170), "Guloso calculado para " + pontos.size() + " pontos");
    }

    private void executarTSP() {
        List<Ponto> pontos = escolherPontosOuTodos("Selecionar pontos para TSP", 2);
        if (pontos == null) {
            return;
        }
        Ponto origem = escolherPontoDaLista("Selecionar ponto inicial do TSP", pontos);
        if (origem == null) {
            return;
        }
        executarComProgresso(() -> {
            rotaAtual = routeService.executarTSP(origem, pontos);
            ultimaOperacao = Operacao.TSP;
            ultimaOrigem = origem;
            ultimoDestino = null;
        }, Color.RED, "Caixeiro viajante calculado para " + pontos.size() + " pontos");
    }

    private void executarComProgresso(Runnable tarefa, Color cor, String mensagem) {
        progressBar.setIndeterminate(true);
        statusLabel.setText("Processando...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                tarefa.run();
                return null;
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                mapPanel.destacarRota(rotaAtual, cor);
                bottomRoutePanel.mostrar(rotaAtual);
                atualizarTudo();
                mostrarMapa();
                setStatus(mensagem);
            }
        };
        worker.execute();
    }

    private void limparRota() {
        mapPanel.pararAnimacao();
        routeService.limpar();
        rotaAtual = null;
        ultimaOperacao = Operacao.NENHUMA;
        ultimaOrigem = null;
        ultimoDestino = null;
        pontoSelecionado = null;
        atualizarTudo();
        setStatus("Rota limpa");
    }

    private void substituirGrafo(Grafo novoGrafo) {
        List<Ponto> atuais = new ArrayList<>(grafo.getPontos());
        for (Ponto ponto : atuais) {
            grafo.removerPonto(ponto);
        }
        for (Ponto ponto : new ArrayList<>(novoGrafo.getPontos())) {
            grafo.adicionarPonto(ponto);
        }
        for (var aresta : novoGrafo.getArestas()) {
            grafo.adicionarAresta(aresta.getOrigem(), aresta.getDestino(), aresta.getDistancia());
        }
        rotaAtual = null;
        routeService.limpar();
        pontoSelecionado = null;
        atualizarTudo();
    }

    private Ponto escolherPonto(String titulo) {
        List<Ponto> pontos = new ArrayList<>(grafo.getPontos());
        if (pontos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nao ha pontos cadastrados.");
            return null;
        }
        Object escolhido = JOptionPane.showInputDialog(this, titulo, titulo,
                JOptionPane.QUESTION_MESSAGE, null, pontos.toArray(), pontos.get(0));
        return escolhido instanceof Ponto ? (Ponto) escolhido : null;
    }

    private List<Ponto> escolherPontos(String titulo) {
        List<Ponto> pontos = new ArrayList<>(grafo.getPontos());
        if (pontos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nao ha pontos cadastrados.");
            return null;
        }
        javax.swing.JList<Ponto> lista = new javax.swing.JList<>(pontos.toArray(new Ponto[0]));
        lista.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        int[] indices = new int[pontos.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        lista.setSelectedIndices(indices);
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setPreferredSize(new Dimension(460, 320));

        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.add(new JLabel("Marque os pontos que a arvore geradora minima deve conectar:"), BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);

        int confirm = JOptionPane.showConfirmDialog(this, painel, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) {
            return null;
        }
        return lista.getSelectedValuesList();
    }

    private List<Ponto> escolherPontosOuTodos(String titulo, int minimo) {
        List<Ponto> todos = new ArrayList<>(grafo.getPontos());
        if (todos.size() < minimo) {
            JOptionPane.showMessageDialog(this, "Cadastre pelo menos " + minimo + " pontos.");
            return null;
        }

        Object[] opcoes = {"Todos", "Escolher pontos", "Cancelar"};
        int escolha = JOptionPane.showOptionDialog(this,
                "Quais pontos deseja usar neste algoritmo?",
                titulo,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[0]);

        if (escolha == 0) {
            return todos;
        }
        if (escolha != 1) {
            return null;
        }

        List<Ponto> selecionados = escolherPontos(titulo);
        if (selecionados == null) {
            return null;
        }
        if (selecionados.size() < minimo) {
            JOptionPane.showMessageDialog(this, "Selecione pelo menos " + minimo + " pontos.");
            return null;
        }
        return selecionados;
    }

    private Ponto escolherPontoDaLista(String titulo, List<Ponto> pontos) {
        if (pontos == null || pontos.isEmpty()) {
            return null;
        }
        Object escolhido = JOptionPane.showInputDialog(this, titulo, titulo,
                JOptionPane.QUESTION_MESSAGE, null, pontos.toArray(), pontos.get(0));
        return escolhido instanceof Ponto ? (Ponto) escolhido : null;
    }

    private Ponto selecionadoPeloMapaOuDetalhes() {
        return null;
    }

    private String algoritmoAtivo() {
        return routeService.getAlgoritmoAtual() != null ? routeService.getAlgoritmoAtual() : "-";
    }

    private Color corDaOperacao(Operacao operacao) {
        switch (operacao) {
            case DIJKSTRA:
                return Color.BLUE;
            case PRIM:
                return new Color(30, 150, 60);
            case KRUSKAL:
                return new Color(210, 140, 40);
            case BFS:
                return Color.CYAN.darker();
            case DFS:
                return Color.PINK.darker();
            case GULOSO:
                return new Color(140, 60, 170);
            case TSP:
                return Color.RED;
            default:
                return Color.BLUE;
        }
    }

    private void setStatus(String texto) {
        statusLabel.setText(texto + " | Cruz das Almas - BA");
    }

    private void erro(String contexto, Exception ex) {
        JOptionPane.showMessageDialog(this, contexto + ":\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private static class PontoEditResult {
        final String nome;
        final String bairro;
        final double latitude;
        final double longitude;
        final int alunos;
        final int desembarque;
        final int capacidade;
        final int prioridade;
        final String turno;
        final TipoPonto tipo;

        private PontoEditResult(String nome, String bairro, double latitude, double longitude, int alunos,
                                int desembarque, int capacidade, int prioridade, String turno, TipoPonto tipo) {
            this.nome = nome;
            this.bairro = bairro;
            this.latitude = latitude;
            this.longitude = longitude;
            this.alunos = alunos;
            this.desembarque = desembarque;
            this.capacidade = capacidade;
            this.prioridade = prioridade;
            this.turno = turno;
            this.tipo = tipo;
        }
    }
}
