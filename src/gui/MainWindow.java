package gui;

import model.Grafo;
import model.Ponto;
import model.Rota;
import model.TipoPonto;
import services.ProjetoService;
import services.RouteService;
import services.SimulationService;
import util.CSVExporter;
import util.PDFExporter;
import util.TXTExporter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import javax.swing.JToolBar;
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
    private final JLabel statusLabel = new JLabel("Pronto");
    private final JProgressBar progressBar = new JProgressBar();
    private final JSpinner velocidadeAnimacao = new JSpinner(new SpinnerNumberModel(40, 5, 120, 5));

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
    }

    private void configurarLayout() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setContentPane(root);

        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                criarPainelEsquerdo(),
                criarPainelCentroEDireito());
        centro.setResizeWeight(0.18);
        centro.setDividerLocation(260);

        JPanel south = new JPanel(new BorderLayout(6, 6));
        south.add(bottomRoutePanel, BorderLayout.CENTER);
        south.add(criarBarraStatus(), BorderLayout.SOUTH);

        root.add(criarToolbar(), BorderLayout.NORTH);
        root.add(centro, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
    }

    private JPanel criarPainelEsquerdo() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.add(treePanel, BorderLayout.CENTER);
        painel.setPreferredSize(new Dimension(260, 0));
        return painel;
    }

    private JSplitPane criarPainelCentroEDireito() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapPanel, detailsPanel);
        split.setResizeWeight(0.78);
        split.setDividerLocation(980);
        return split;
    }

    private JPanel criarBarraStatus() {
        JPanel status = new JPanel(new BorderLayout(8, 8));
        status.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        progressBar.setIndeterminate(false);
        progressBar.setPreferredSize(new Dimension(180, 18));
        status.add(statusLabel, BorderLayout.CENTER);
        status.add(progressBar, BorderLayout.EAST);
        return status;
    }

    private JToolBar criarToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        adicionarBotao(toolbar, "Novo ponto", e -> novoPonto());
        adicionarBotao(toolbar, "Editar ponto", e -> editarPontoSelecionado());
        adicionarBotao(toolbar, "Excluir ponto", e -> removerPontoSelecionado());
        toolbar.addSeparator();
        adicionarBotao(toolbar, "Dijkstra", e -> executarDijkstra());
        adicionarBotao(toolbar, "Prim", e -> executarPrim());
        adicionarBotao(toolbar, "Kruskal", e -> executarKruskal());
        adicionarBotao(toolbar, "BFS", e -> executarBFS());
        adicionarBotao(toolbar, "DFS", e -> executarDFS());
        adicionarBotao(toolbar, "Guloso", e -> executarGuloso());
        adicionarBotao(toolbar, "Caixeiro", e -> executarTSP());
        toolbar.addSeparator();
        adicionarBotao(toolbar, "Limpar rota", e -> limparRota());
        adicionarBotao(toolbar, "Iniciar veículo", e -> iniciarAnimacao());
        adicionarBotao(toolbar, "Pausar", e -> pausarAnimacao());
        adicionarBotao(toolbar, "Parar", e -> pararAnimacao());
        toolbar.add(new JLabel(" Velocidade: "));
        velocidadeAnimacao.setMaximumSize(new Dimension(65, 28));
        velocidadeAnimacao.setToolTipText("Velocidade média do veículo em km/h");
        toolbar.add(velocidadeAnimacao);
        toolbar.add(new JLabel(" km/h "));
        adicionarBotao(toolbar, "Centralizar mapa", e -> mapPanel.centralizarCruzDasAlmas());
        adicionarBotao(toolbar, "Recarregar mapa", e -> mapPanel.recarregarMapa());

        return toolbar;
    }

    private void adicionarBotao(JToolBar toolbar, String texto, java.awt.event.ActionListener listener) {
        JButton button = new JButton(texto);
        button.addActionListener(listener);
        toolbar.add(button);
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
            mostrarDetalhes(ponto);
        });
        treePanel.setOnSelecionado(ponto -> {
            pontoSelecionado = ponto;
            mostrarDetalhes(ponto);
        });
        mapPanel.setOnAnimationProgress(progresso -> {
            int percentual = (int) Math.round(progresso * 100.0);
            progressBar.setIndeterminate(false);
            progressBar.setValue(percentual);
            statusLabel.setText("Veículo em movimento: " + percentual + "% da rota");
        });
    }

    private void iniciarAnimacao() {
        if (rotaAtual == null || rotaAtual.getPercurso().size() < 2) {
            JOptionPane.showMessageDialog(this, "Calcule uma rota com pelo menos dois pontos antes de iniciar.");
            return;
        }
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
        } else {
            mapPanel.limparRota();
            bottomRoutePanel.limpar();
        }
        statusLabel.setText("Pontos: " + grafo.getPontos().size() + " | Arestas: " + grafo.getArestas().size());
        if (ultimaOperacao == Operacao.NENHUMA) {
            mostrarDetalhes(null);
        }
    }

    private List<Ponto> pontosVisiveis() {
        List<Ponto> lista = new ArrayList<>();
        for (Ponto ponto : grafo.getPontos()) {
            if (Boolean.TRUE.equals(tiposVisiveis.get(ponto.getTipo()))) {
                lista.add(ponto);
            }
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
            simulationService.adicionarPonto(result.nome, result.bairro, result.latitude, result.longitude,
                    result.alunos, result.capacidade, result.prioridade, result.turno, result.tipo);
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
            selecionado.setNome(result.nome);
            selecionado.setBairro(result.bairro);
            selecionado.setLatitude(result.latitude);
            selecionado.setLongitude(result.longitude);
            selecionado.setQuantidadeAlunos(result.alunos);
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
        simulationService.removerPonto(selecionado);
        pontoSelecionado = null;
        rotaAtual = null;
        ultimaOperacao = Operacao.NENHUMA;
        setStatus("Ponto removido: " + selecionado.getNome());
    }

    private Ponto pontoSelecionado() {
        return pontoSelecionado;
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
        adicionarCampo(painel, c, row++, "Alunos", alunos);
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
        Ponto origem = escolherPonto("Selecionar inicio");
        if (origem == null) {
            return;
        }
        executarComProgresso(() -> {
            routeService.executarPrim(origem);
            rotaAtual = routeService.getRotaAtual();
            ultimaOperacao = Operacao.PRIM;
            ultimaOrigem = origem;
            ultimoDestino = null;
        }, new Color(30, 150, 60), "Prim calculado");
    }

    private void executarKruskal() {
        executarComProgresso(() -> {
            routeService.executarKruskal();
            rotaAtual = routeService.getRotaAtual();
            ultimaOperacao = Operacao.KRUSKAL;
            ultimaOrigem = null;
            ultimoDestino = null;
        }, new Color(210, 140, 40), "Kruskal calculado");
    }

    private void executarBFS() {
        Ponto origem = escolherPonto("Selecionar inicio");
        if (origem == null) {
            return;
        }
        executarComProgresso(() -> {
            rotaAtual = routeService.executarBFS(origem);
            ultimaOperacao = Operacao.BFS;
            ultimaOrigem = origem;
            ultimoDestino = null;
        }, Color.CYAN.darker(), "BFS calculado");
    }

    private void executarDFS() {
        Ponto origem = escolherPonto("Selecionar inicio");
        if (origem == null) {
            return;
        }
        executarComProgresso(() -> {
            rotaAtual = routeService.executarDFS(origem);
            ultimaOperacao = Operacao.DFS;
            ultimaOrigem = origem;
            ultimoDestino = null;
        }, Color.PINK.darker(), "DFS calculado");
    }

    private void executarGuloso() {
        Ponto origem = escolherPonto("Selecionar inicio");
        if (origem == null) {
            return;
        }
        executarComProgresso(() -> {
            rotaAtual = routeService.executarGuloso(origem);
            ultimaOperacao = Operacao.GULOSO;
            ultimaOrigem = origem;
            ultimoDestino = null;
        }, new Color(140, 60, 170), "Guloso calculado");
    }

    private void executarTSP() {
        Ponto origem = escolherPonto("Selecionar inicio");
        if (origem == null) {
            return;
        }
        executarComProgresso(() -> {
            rotaAtual = routeService.executarTSP(origem);
            ultimaOperacao = Operacao.TSP;
            ultimaOrigem = origem;
            ultimoDestino = null;
        }, Color.RED, "Caixeiro viajante calculado");
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
        final int capacidade;
        final int prioridade;
        final String turno;
        final TipoPonto tipo;

        private PontoEditResult(String nome, String bairro, double latitude, double longitude, int alunos,
                                int capacidade, int prioridade, String turno, TipoPonto tipo) {
            this.nome = nome;
            this.bairro = bairro;
            this.latitude = latitude;
            this.longitude = longitude;
            this.alunos = alunos;
            this.capacidade = capacidade;
            this.prioridade = prioridade;
            this.turno = turno;
            this.tipo = tipo;
        }
    }
}
