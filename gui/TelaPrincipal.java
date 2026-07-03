package gui;

import algoritmos.Dijkstra;
import algoritmos.Prim;
import algoritmos.VRP;
import algoritmos.TSP;
import mapa.CalculadoraDistancia;
import model.Aresta;
import model.Grafo;
import model.Onibus;
import model.Ponto;
import model.Rota;

import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Tela principal do sistema de planejamento de rotas
 * para transporte universitario em Cruz das Almas.
 */
public class TelaPrincipal extends JFrame {

    private Grafo grafo;
    private PainelMapa painelMapa;
    private PainelResultado painelResultado;
    private PainelCadastro painelCadastro;
    private PainelBairros painelBairros;
    private List<Ponto> pontos;
    private List<Aresta> arestasBase;
    private Map<Ponto, Boolean> pontosAtivos;
    private List<Onibus> onibusAtivos;

    public TelaPrincipal() {

        grafo = new Grafo();
        pontos = new ArrayList<Ponto>();
        arestasBase = new ArrayList<Aresta>();
        pontosAtivos = new LinkedHashMap<Ponto, Boolean>();
        onibusAtivos = new ArrayList<Onibus>();

        configurarJanela();
        inicializarComponentes();
        carregarMapaInicial();
    }

    private void configurarJanela() {

        setTitle("Transporte Universitario - Cruz das Almas");
        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {

        add(criarMenuLateral(), BorderLayout.WEST);

        painelMapa = new PainelMapa();
        add(painelMapa, BorderLayout.CENTER);

        painelResultado = new PainelResultado();
        add(painelResultado, BorderLayout.SOUTH);

        painelCadastro = new PainelCadastro();
        painelCadastro.getBtnCadastrar().addActionListener(
                e -> cadastrarNovoPonto()
        );

        painelBairros = new PainelBairros();

        JPanel painelLateral = new JPanel(new BorderLayout());
        painelLateral.setPreferredSize(new Dimension(260, 700));
        painelLateral.setBackground(new Color(18, 24, 31));
        painelLateral.add(painelCadastro, BorderLayout.NORTH);
        painelLateral.add(painelBairros, BorderLayout.CENTER);

        add(painelLateral, BorderLayout.EAST);
    }

    private JPanel criarMenuLateral() {

        JPanel painel = new JPanel();
        painel.setPreferredSize(new Dimension(250, 700));
        painel.setBackground(new Color(28, 38, 48));
        painel.setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));
        painel.setLayout(new GridBagLayout());

        JButton btnDijkstra = criarBotao("Dijkstra");
        JButton btnTSP = criarBotao("Rota Otimizada");
        JButton btnPrim = criarBotao("Arvore Minima");
        JButton btnPriorizar = criarBotao("Priorizar Alunos");
        JButton btnRemover = criarBotao("Remover Ultimo");
        JButton btnRecarregar = criarBotao("Recarregar Mapa");
        JButton btnZoomMais = criarBotaoSecundario("Zoom +");
        JButton btnZoomMenos = criarBotaoSecundario("Zoom -");
        JButton btnAjustarZoom = criarBotaoSecundario("Ver Tudo");
        JButton btnSair = criarBotao("Sair");

        btnDijkstra.addActionListener(e -> calcularMenoresRotas());
        btnTSP.addActionListener(e -> otimizarRotas());
        btnPrim.addActionListener(e -> calcularArvoreMinima());
        btnPriorizar.addActionListener(e -> priorizarPontosComMaisAlunos());
        btnRemover.addActionListener(e -> removerUltimoPonto());
        btnRecarregar.addActionListener(e -> limparSistema());
        btnZoomMais.addActionListener(e -> painelMapa.aumentarZoom());
        btnZoomMenos.addActionListener(e -> painelMapa.diminuirZoom());
        btnAjustarZoom.addActionListener(e -> painelMapa.ajustarZoom());
        btnSair.addActionListener(e -> System.exit(0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 10, 0);

        adicionarTituloMenu(painel, gbc, "Algoritmos");
        adicionarBotaoMenu(painel, gbc, btnDijkstra);
        adicionarBotaoMenu(painel, gbc, btnTSP);
        adicionarBotaoMenu(painel, gbc, btnPrim);
        adicionarBotaoMenu(painel, gbc, btnPriorizar);

        adicionarTituloMenu(painel, gbc, "Simulacao");
        adicionarBotaoMenu(painel, gbc, btnRemover);
        adicionarBotaoMenu(painel, gbc, btnRecarregar);

        adicionarTituloMenu(painel, gbc, "Mapa");
        adicionarBotaoMenu(painel, gbc, btnZoomMais);
        adicionarBotaoMenu(painel, gbc, btnZoomMenos);
        adicionarBotaoMenu(painel, gbc, btnAjustarZoom);

        gbc.weighty = 1.0;
        painel.add(new JPanel() {{
            setOpaque(false);
        }}, gbc);

        gbc.weighty = 0.0;
        adicionarBotaoMenu(painel, gbc, btnSair);

        return painel;
    }

    private JButton criarBotao(String texto) {

        JButton botao = criarBotaoCustomizado(texto);
        configurarBotao(
                botao,
                new Color(34, 94, 154),
                new Color(45, 116, 184)
        );

        return botao;
    }

    private JButton criarBotaoSecundario(String texto) {

        JButton botao = criarBotaoCustomizado(texto);
        configurarBotao(
                botao,
                new Color(46, 59, 72),
                new Color(61, 77, 92)
        );

        return botao;
    }

    private void configurarBotao(
            JButton botao,
            Color corNormal,
            Color corHover
    ) {

        botao.setFocusPainted(false);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setForeground(Color.WHITE);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.setPreferredSize(new Dimension(210, 42));
        botao.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        botao.setUI(new BasicButtonUI());
        botao.putClientProperty("botao.corNormal", corNormal);
        botao.putClientProperty("botao.corHover", corHover);

        botao.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                botao.setBackground(corHover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                botao.setBackground(corNormal);
            }
        });
    }

    private JButton criarBotaoCustomizado(String texto) {

        return new JButton(texto) {

            @Override
            protected void paintComponent(Graphics g) {

                Color corNormal =
                        (Color) getClientProperty("botao.corNormal");
                Color corHover =
                        (Color) getClientProperty("botao.corHover");
                Color corFundo =
                        getModel().isRollover() && corHover != null
                                ? corHover
                                : corNormal;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2.setColor(corFundo != null ? corFundo : getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();

                super.paintComponent(g);
            }
        };
    }

    private void adicionarTituloMenu(
            JPanel painel,
            GridBagConstraints gbc,
            String texto
    ) {

        JLabel titulo = new JLabel(texto.toUpperCase());
        titulo.setForeground(new Color(185, 197, 207));
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 11));

        gbc.gridy++;
        gbc.insets = new Insets(8, 2, 6, 0);
        painel.add(titulo, gbc);
    }

    private void adicionarBotaoMenu(
            JPanel painel,
            GridBagConstraints gbc,
            JButton botao
    ) {

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        painel.add(botao, gbc);
    }

    private void carregarMapaInicial() {
        Ponto ufrb = adicionarPontoInicial("UFRB", -12.6624, -39.0878, 0);
        Ponto albertoPassos = adicionarPontoInicial("Alberto Passos", -12.6640, -39.0912, 26);
        Ponto anaLucia = adicionarPontoInicial("Ana Lucia", -12.6664, -39.0892, 18);
        Ponto areal = adicionarPontoInicial("Areal", -12.6686, -39.0938, 24);
        Ponto assembleia = adicionarPontoInicial("Assembleia", -12.6717, -39.0918, 18);
        Ponto belaVista = adicionarPontoInicial("Bela Vista", -12.6738, -39.0958, 21);
        Ponto bonsucesso = adicionarPontoInicial("Bonsucesso", -12.6758, -39.0908, 16);
        Ponto centro = adicionarPontoInicial("Centro", -12.6700, -39.1019, 38);
        Ponto chapadinhaAldeia = adicionarPontoInicial("Chapadinha e Aldeia", -12.6782, -39.0988, 19);
        Ponto coplan = adicionarPontoInicial("Coplan", -12.6798, -39.1155, 24);
        Ponto donaRosa = adicionarPontoInicial("Dona Rosa", -12.6816, -39.0984, 22);
        Ponto edlaCosta = adicionarPontoInicial("Edla Costa", -12.6832, -39.0954, 20);
        Ponto fonteDoDoutor = adicionarPontoInicial("Fonte do Doutor", -12.6846, -39.1008, 15);
        Ponto inocoop = adicionarPontoInicial("Inocoop", -12.6632, -39.1098, 21);
        Ponto itapicuru = adicionarPontoInicial("Itapicuru", -12.6840, -39.0920, 32);
        Ponto jardimPlanalto = adicionarPontoInicial("Jardim Planalto", -12.6762, -39.0972, 17);
        Ponto linha = adicionarPontoInicial("Linha", -12.6660, -39.1044, 23);
        Ponto parqueArvores = adicionarPontoInicial("Parque das Arvores", -12.6792, -39.1038, 14);
        Ponto parqueSantaCruz = adicionarPontoInicial("Parque Santa Cruz", -12.6788, -39.0902, 11);
        Ponto parqueSaoFrancisco = adicionarPontoInicial("Parque Sao Francisco de Assis", -12.6760, -39.0932, 13);
        Ponto passinhos = adicionarPontoInicial("Passinhos", -12.6708, -39.1072, 12);
        Ponto primavera = adicionarPontoInicial("Primavera", -12.6744, -39.1104, 25);
        Ponto santoAntonio = adicionarPontoInicial("Santo Antonio", -12.6860, -39.0992, 27);
        Ponto saoJudas = adicionarPontoInicial("Sao Judas Tadeu", -12.6890, -39.0950, 18);
        Ponto sapucaia = adicionarPontoInicial("Sapucaia", -12.6868, -39.1062, 16);
        Ponto suzana = adicionarPontoInicial("Suzana", -12.6865, -39.1035, 15);
        Ponto tabela = adicionarPontoInicial("Tabela", -12.6668, -39.0976, 14);
        Ponto toquinha = adicionarPontoInicial("Toquinha", -12.6648, -39.0950, 17);
        Ponto vilaAlzira = adicionarPontoInicial("Vila Alzira", -12.6714, -39.1130, 19);
        Ponto vilarejo = adicionarPontoInicial("Vilarejo", -12.6828, -39.1110, 18);
        Ponto loteamentoGarcia = adicionarPontoInicial("Loteamento Garcia", -12.6692, -39.0872, 12);
        Ponto loteamentoHipolito = adicionarPontoInicial("Loteamento Hipolito", -12.6680, -39.0902, 11);
        Ponto loteamentoMiradouro = adicionarPontoInicial("Loteamento Miradouro", -12.6778, -39.1068, 15);
        Ponto loteamentoSantoAntonio = adicionarPontoInicial("Loteamento Santo Antonio", -12.6858, -39.1018, 13);
        Ponto loteamentoSaoJose = adicionarPontoInicial("Loteamento Sao Jose", -12.6748, -39.1042, 16);
        Ponto loteamento2Palmeiras = adicionarPontoInicial("Loteamento 2 Palmeiras", -12.6808, -39.0948, 14);
  
        conectarAosVizinhosMaisProximos(ufrb);
        conectarAosVizinhosMaisProximos(centro);
        conectarAosVizinhosMaisProximos(coplan);
        conectarAosVizinhosMaisProximos(assembleia);
        conectarAosVizinhosMaisProximos(itapicuru);
        conectarAosVizinhosMaisProximos(inocoop);
        conectarAosVizinhosMaisProximos(suzana);
        conectarAosVizinhosMaisProximos(albertoPassos);
        conectarAosVizinhosMaisProximos(anaLucia);
        conectarAosVizinhosMaisProximos(areal);
        conectarAosVizinhosMaisProximos(belaVista);
        conectarAosVizinhosMaisProximos(bonsucesso);
        conectarAosVizinhosMaisProximos(chapadinhaAldeia);
        conectarAosVizinhosMaisProximos(donaRosa);
        conectarAosVizinhosMaisProximos(edlaCosta);
        conectarAosVizinhosMaisProximos(fonteDoDoutor);
        conectarAosVizinhosMaisProximos(jardimPlanalto);
        conectarAosVizinhosMaisProximos(linha);
        conectarAosVizinhosMaisProximos(parqueArvores);
        conectarAosVizinhosMaisProximos(parqueSantaCruz);
        conectarAosVizinhosMaisProximos(parqueSaoFrancisco);
        conectarAosVizinhosMaisProximos(passinhos);
        conectarAosVizinhosMaisProximos(primavera);
        conectarAosVizinhosMaisProximos(santoAntonio);
        conectarAosVizinhosMaisProximos(saoJudas);
        conectarAosVizinhosMaisProximos(sapucaia);
        conectarAosVizinhosMaisProximos(tabela);
        conectarAosVizinhosMaisProximos(toquinha);
        conectarAosVizinhosMaisProximos(vilaAlzira);
        conectarAosVizinhosMaisProximos(vilarejo);
        conectarAosVizinhosMaisProximos(loteamentoGarcia);
        conectarAosVizinhosMaisProximos(loteamentoHipolito);
        conectarAosVizinhosMaisProximos(loteamentoMiradouro);
        conectarAosVizinhosMaisProximos(loteamentoSantoAntonio);
        conectarAosVizinhosMaisProximos(loteamentoSaoJose);
        conectarAosVizinhosMaisProximos(loteamento2Palmeiras);

        reconstruirEstruturaAtiva();
        atualizarOnibusAtivos();
        painelResultado.exibirMensagem(resumoInicial());
    }

    private Ponto adicionarPontoInicial(
            String nome,
            double latitude,
            double longitude,
            int alunos
    ) {

        Ponto ponto = new Ponto(nome, latitude, longitude, alunos);
        adicionarPontoSistema(ponto);
        return ponto;
    }

    private void adicionarPontoSistema(Ponto ponto) {

        pontos.add(ponto);
        pontosAtivos.put(ponto, Boolean.TRUE);

        if (painelBairros != null
                && !ponto.getNome().toUpperCase().contains("UFRB")) {
            painelBairros.adicionarOuAtualizar(
                    ponto,
                    true,
                    (p, ativo) -> {
                        pontosAtivos.put(p, ativo);
                        reconstruirEstruturaAtiva();
                    }
            );
        }
    }

    private void conectarPontos(
            Ponto origem,
            Ponto destino,
            double distancia
    ) {

        if (existeArestaEntre(origem, destino)) {
            return;
        }

        arestasBase.add(new Aresta(origem, destino, distancia));
    }

    private boolean existeArestaEntre(Ponto origem, Ponto destino) {

        for (Aresta aresta : arestasBase) {

            boolean mesmaDirecao =
                    aresta.getOrigem().equals(origem)
                            && aresta.getDestino().equals(destino);
            boolean direcaoInversa =
                    aresta.getOrigem().equals(destino)
                            && aresta.getDestino().equals(origem);

            if (mesmaDirecao || direcaoInversa) {
                return true;
            }
        }

        return false;
    }

    private void calcularMenoresRotas() {

        if (pontos.isEmpty()) {
            painelResultado.exibirMensagem("Nenhum ponto cadastrado.");
            return;
        }

        Ponto origem = getUfrb();
        Map<Ponto, Double> distancias = Dijkstra.calcular(grafo, origem);

        StringBuilder sb = new StringBuilder();
        sb.append("===== DIJKSTRA - MENOR CAMINHO =====\n\n");
        sb.append("Origem: ").append(origem.getNome()).append("\n");
        sb.append("Saida e retorno focados na UFRB.\n\n");

        List<List<Ponto>> rotas = new ArrayList<List<Ponto>>();

        for (Ponto destino : pontos) {

            if (destino.equals(origem)) {
                continue;
            }

            List<Ponto> caminho =
                    Dijkstra.encontrarCaminho(grafo, origem, destino);
            double distancia = distancias.get(destino);
            double tempo = calcularTempo(distancia);

            rotas.add(caminho);

            sb.append(destino.getNome())
                    .append("\n");
            sb.append("Percurso: ")
                    .append(formatarPercurso(caminho))
                    .append("\n");
            sb.append("Distancia: ")
                    .append(String.format("%.2f km", distancia))
                    .append(" | Tempo: ")
                    .append(String.format("%.2f min", tempo))
                    .append("\n");
        }

        painelMapa.definirRotas(rotas);
        painelResultado.exibirMensagem(sb.toString());
    }

    private void otimizarRotas() {

        if (pontos.isEmpty()) {
            painelResultado.exibirMensagem("Nenhum ponto cadastrado.");
            return;
        }

        TSP tsp = new TSP();
        Rota rota = tsp.otimizarRotaComDestinoFinal(
                grafo,
                getUfrb()
        );

        atualizarOnibusAtivos();

        if (!onibusAtivos.isEmpty()) {
            onibusAtivos.get(0).setRota(rota);
        }

        painelMapa.definirRota(rota.getPercurso());
        painelMapa.atualizarResumoRota(
                rota,
                onibusAtivos.size(),
                getPlacaPrincipal(),
                getMotoristaPrincipal()
        );
        painelResultado.atualizarResultado(
                rota,
                onibusAtivos.size(),
                getPlacaPrincipal(),
                getMotoristaPrincipal()
        );
    }

    private void calcularArvoreMinima() {

        if (pontos.isEmpty()) {
            painelResultado.exibirMensagem("Nenhum ponto cadastrado.");
            return;
        }

        Ponto origem = getUfrb();
        Prim prim = new Prim();
        List<Aresta> arvore = prim.executar(grafo, origem);
        List<Ponto> percurso = construirPercursoDaArvore(origem, arvore);
        double distanciaTotal = calcularDistanciaPercurso(percurso);
        double tempoTotal = calcularTempo(distanciaTotal);

        painelMapa.definirRota(percurso);

        StringBuilder sb = new StringBuilder();
        sb.append("===== ARVORE GERADORA MINIMA - PRIM =====\n\n");
        sb.append("Origem: ").append(origem.getNome()).append("\n");
        sb.append("Percurso aproximado sobre a arvore minima:\n");
        sb.append(formatarPercurso(percurso)).append("\n\n");
        sb.append("Distancia total: ")
                .append(String.format("%.2f km", distanciaTotal))
                .append("\n");
        sb.append("Tempo estimado: ")
                .append(String.format("%.2f min", tempoTotal))
                .append("\n\n");
        sb.append("Conexoes da arvore:\n");

        for (Aresta aresta : arvore) {

            sb.append(" - ")
                    .append(aresta.getOrigem().getNome())
                    .append(" -> ")
                    .append(aresta.getDestino().getNome())
                    .append(" | ")
                    .append(String.format("%.2f km", aresta.getDistancia()))
                    .append("\n");
        }

        painelResultado.exibirMensagem(sb.toString());
    }

    private void priorizarPontosComMaisAlunos() {

        List<Ponto> ordenados = new ArrayList<Ponto>(pontos);
        Ponto ufrb = getUfrb();

        ordenados.remove(ufrb);

        ordenados.sort(
                Comparator.comparingInt(Ponto::getQuantidadeAlunos)
                        .reversed()
        );

        List<Ponto> percurso =
                montarPercursoPorPrioridade(ufrb, ordenados);
        double distanciaTotal = calcularDistanciaPercurso(percurso);
        double tempoTotal = calcularTempo(distanciaTotal);

        StringBuilder sb = new StringBuilder();
        sb.append("===== PRIORIZACAO POR DEMANDA =====\n\n");
        sb.append("Pontos com maior quantidade de alunos entram primeiro na rota.\n");
        sb.append("Saida e retorno obrigatorios: UFRB - Campus.\n\n");
        sb.append("Percurso: ")
                .append(formatarPercurso(percurso))
                .append("\n\n");
        sb.append("Distancia total: ")
                .append(String.format("%.2f km", distanciaTotal))
                .append("\n");
        sb.append("Tempo estimado: ")
                .append(String.format("%.2f min", tempoTotal))
                .append("\n\n");
        sb.append("Ordem de prioridade:\n");

        sb.append("1. ")
                .append(ufrb.getNome())
                .append(" - partida e retorno\n");

        for (int i = 0; i < ordenados.size(); i++) {

            Ponto ponto = ordenados.get(i);

            sb.append(i + 2)
                    .append(". ")
                    .append(ponto.getNome())
                    .append(" - ")
                    .append(ponto.getQuantidadeAlunos())
                    .append(" alunos\n");
        }

        painelMapa.definirRota(percurso);
        painelResultado.exibirMensagem(sb.toString());
    }

    private Ponto getUfrb() {

        for (Ponto ponto : pontos) {

            if (ponto.getNome().toUpperCase().contains("UFRB")) {
                return ponto;
            }
        }

        return pontos.get(0);
    }

    private void cadastrarNovoPonto() {

        try {

            Ponto ponto = new Ponto(
                    painelCadastro.getNome(),
                    painelCadastro.getLatitude(),
                    painelCadastro.getLongitude(),
                    painelCadastro.getQuantidadeAlunos()
            );

            adicionarPontoSistema(ponto);
            conectarAosVizinhosMaisProximos(ponto);
            reconstruirEstruturaAtiva();
            painelCadastro.limparCampos();

            painelResultado.exibirMensagem(
                    "Ponto adicionado com sucesso: "
                            + ponto
                            + "\n\nUse Dijkstra, Rota Otimizada ou Arvore Minima para recalcular."
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Informe valores numericos validos para latitude, longitude e alunos.",
                    "Cadastro invalido",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Cadastro invalido",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void conectarAosVizinhosMaisProximos(Ponto novoPonto) {

        List<Ponto> candidatos = new ArrayList<Ponto>(pontos);
        candidatos.remove(novoPonto);

        candidatos.sort(
                Comparator.comparingDouble(
                        ponto -> distanciaVisual(novoPonto, ponto)
                )
        );

        int limite = Math.min(3, candidatos.size());

        for (int i = 0; i < limite; i++) {

            Ponto vizinho = candidatos.get(i);
            double distancia = Math.max(
                    0.6,
                    distanciaVisual(novoPonto, vizinho) * 1.25
            );

            conectarPontos(novoPonto, vizinho, distancia);
        }
    }

    private double distanciaVisual(Ponto a, Ponto b) {

        return CalculadoraDistancia.calcular(
                a.getLatitude(),
                a.getLongitude(),
                b.getLatitude(),
                b.getLongitude()
        );
    }

    private void removerUltimoPonto() {

        if (pontos.size() <= 1) {
            painelResultado.exibirMensagem("Nao ha ponto de embarque para remover.");
            return;
        }

        Ponto removido = pontos.remove(pontos.size() - 1);

        pontosAtivos.remove(removido);
        if (painelBairros != null) {
            painelBairros.remover(removido);
        }

        arestasBase.removeIf(
                aresta -> aresta.getOrigem().equals(removido)
                        || aresta.getDestino().equals(removido)
        );

        reconstruirEstruturaAtiva();

        painelResultado.exibirMensagem(
                "Simulacao concluida: ponto removido -> "
                        + removido.getNome()
                        + "\nRecalcule as rotas para comparar o impacto."
        );
    }

    private void limparSistema() {

        painelMapa.limparMapa();
        painelResultado.limpar();
        painelBairros.limpar();
        pontos.clear();
        grafo = new Grafo();
        arestasBase.clear();
        pontosAtivos.clear();
        onibusAtivos.clear();

        carregarMapaInicial();
    }

    private String resumoInicial() {

        int totalAlunos = 0;

        for (Ponto ponto : pontos) {
            totalAlunos += ponto.getQuantidadeAlunos();
        }

        return "===== PLANEJAMENTO DE ROTAS - CRUZ DAS ALMAS =====\n\n"
                + "Vertice central: UFRB - Campus\n"
                + "Vertices de embarque: bairros e pontos com estudantes\n"
                + "Arestas: ruas/ligacoes urbanas com peso em km\n"
                + "Total de pontos: " + pontos.size() + "\n"
                + "Total de alunos atendidos: " + totalAlunos + "\n\n"
                + "Acoes disponiveis:\n"
                + " - Dijkstra: menor caminho a partir da UFRB\n"
                + " - Rota Otimizada: caixeiro viajante simplificado\n"
                + " - Arvore Minima: conexao de todos os pontos com menor custo\n"
                + " - Priorizar Alunos: ordenacao gulosa pela demanda\n"
                + " - Adicionar/Remover ponto: simulacao dinamica\n";
    }

    private void atualizarOnibusAtivos() {

        VRP vrp = new VRP();
        onibusAtivos = vrp.distribuirOnibus(pontos);
    }

    private String getPlacaPrincipal() {

        if (onibusAtivos.isEmpty()) {
            return "BUS-01";
        }

        return onibusAtivos.get(0).getPlaca();
    }

    private String getMotoristaPrincipal() {

        return "Joao";
    }

    private void reconstruirEstruturaAtiva() {

        Grafo novoGrafo = new Grafo();
        List<Ponto> pontosVisiveis = new ArrayList<Ponto>();
        List<Aresta> arestasVisiveis = new ArrayList<Aresta>();

        for (Ponto ponto : pontos) {

            if (isPontoAtivo(ponto)) {
                novoGrafo.adicionarPonto(ponto);
                pontosVisiveis.add(ponto);
            }
        }

        for (Aresta aresta : arestasBase) {

            if (isPontoAtivo(aresta.getOrigem())
                    && isPontoAtivo(aresta.getDestino())) {
                novoGrafo.adicionarAresta(
                        aresta.getOrigem(),
                        aresta.getDestino(),
                        aresta.getDistancia()
                );
                arestasVisiveis.add(aresta);
            }
        }

        grafo = novoGrafo;
        painelMapa.sincronizarDados(pontosVisiveis, arestasVisiveis);
    }

    private boolean isPontoAtivo(Ponto ponto) {

        Boolean ativo = pontosAtivos.get(ponto);

        if (ativo == null) {
            return true;
        }

        return ativo;
    }

    private String formatarPercurso(List<Ponto> percurso) {

        List<Ponto> normalizado = new ArrayList<Ponto>();

        for (Ponto ponto : percurso) {

            if (normalizado.isEmpty()
                    || !normalizado.get(normalizado.size() - 1).equals(ponto)) {
                normalizado.add(ponto);
            }
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < normalizado.size(); i++) {

            sb.append(normalizado.get(i).getNome());

            if (i < normalizado.size() - 1) {
                sb.append(" -> ");
            }
        }

        return sb.toString();
    }

    private double calcularDistanciaPercurso(List<Ponto> percurso) {

        double total = 0;

        for (int i = 0; i < percurso.size() - 1; i++) {
            total += grafo.obterDistanciaDireta(
                    percurso.get(i),
                    percurso.get(i + 1)
            );
        }

        return total;
    }

    private double calcularTempo(double distancia) {

        double velocidadeMedia = 40.0;

        return (distancia / velocidadeMedia) * 60;
    }

    private List<Ponto> construirPercursoDaArvore(
            Ponto origem,
            List<Aresta> arvore
    ) {

        Map<Ponto, List<Ponto>> adjacencias =
                new HashMap<Ponto, List<Ponto>>();

        for (Aresta aresta : arvore) {

            adjacencias
                    .computeIfAbsent(
                            aresta.getOrigem(),
                            ponto -> new ArrayList<Ponto>()
                    )
                    .add(aresta.getDestino());

            adjacencias
                    .computeIfAbsent(
                            aresta.getDestino(),
                            ponto -> new ArrayList<Ponto>()
                    )
                    .add(aresta.getOrigem());
        }

        List<Ponto> percurso = new ArrayList<Ponto>();
        Set<Ponto> visitados = new HashSet<Ponto>();

        construirPercursoDaArvore(
                origem,
                null,
                adjacencias,
                percurso,
                visitados
        );

        return percurso;
    }

    private void construirPercursoDaArvore(
            Ponto atual,
            Ponto pai,
            Map<Ponto, List<Ponto>> adjacencias,
            List<Ponto> percurso,
            Set<Ponto> visitados
    ) {

        visitados.add(atual);
        percurso.add(atual);

        List<Ponto> vizinhos = adjacencias.get(atual);

        if (vizinhos == null) {
            return;
        }

        for (Ponto vizinho : vizinhos) {

            if (vizinho.equals(pai)
                    || visitados.contains(vizinho)) {
                continue;
            }

            construirPercursoDaArvore(
                    vizinho,
                    atual,
                    adjacencias,
                    percurso,
                    visitados
            );

            percurso.add(atual);
        }
    }

    private List<Ponto> montarPercursoPorPrioridade(
            Ponto origem,
            List<Ponto> ordenados
    ) {

        List<Ponto> percurso = new ArrayList<Ponto>();
        percurso.add(origem);

        Ponto atual = origem;

        for (Ponto destino : ordenados) {

            List<Ponto> caminho =
                    Dijkstra.encontrarCaminho(grafo, atual, destino);

            adicionarCaminhoAoPercurso(percurso, caminho);
            atual = destino;
        }

        List<Ponto> retorno =
                Dijkstra.encontrarCaminho(grafo, atual, origem);

        adicionarCaminhoAoPercurso(percurso, retorno);

        return percurso;
    }

    private void adicionarCaminhoAoPercurso(
            List<Ponto> percurso,
            List<Ponto> caminho
    ) {

        for (int i = 1; i < caminho.size(); i++) {

            Ponto ponto = caminho.get(i);

            if (percurso.isEmpty()
                    || !percurso.get(percurso.size() - 1).equals(ponto)) {
                percurso.add(ponto);
            }
        }
    }
}
