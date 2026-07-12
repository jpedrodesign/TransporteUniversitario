package services;

import algoritmos.BFS;
import algoritmos.AlgoritmoUtils;
import algoritmos.DFS;
import algoritmos.Dijkstra;
import algoritmos.Guloso;
import algoritmos.Kruskal;
import algoritmos.Prim;
import algoritmos.TSP;
import model.Aresta;
import model.Grafo;
import model.Ponto;
import model.Rota;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Fachada dos algoritmos de roteamento.
 */
public class RouteService {

    private final Grafo grafo;
    private final Prim prim = new Prim();
    private final Kruskal kruskal = new Kruskal();
    private final TSP tsp = new TSP();
    private final Guloso guloso = new Guloso();
    private final BFS bfs = new BFS();
    private final DFS dfs = new DFS();
    private final OsrmRouteService osrm = new OsrmRouteService();

    private Rota rotaAtual;
    private List<Aresta> arestasAtual = new ArrayList<>();
    private String algoritmoAtual;

    public RouteService(Grafo grafo) {
        this.grafo = grafo;
    }

    public Rota executarDijkstra(Ponto origem, Ponto destino) {
        List<Ponto> caminho = Dijkstra.encontrarCaminho(grafo, origem, destino);
        double distancia = AlgoritmoUtils.calcularDistancia(grafo, caminho);
        arestasAtual = construirArestas(caminho);
        rotaAtual = new Rota(caminho, arestasAtual, origem, destino, "Dijkstra", distancia,
                AlgoritmoUtils.calcularTempo(distancia), distancia, AlgoritmoUtils.calcularCusto(distancia));
        enriquecerComRuas(rotaAtual);
        algoritmoAtual = "Dijkstra";
        return rotaAtual;
    }

    public List<Aresta> executarPrim(Ponto inicio) {
        arestasAtual = prim.executar(grafo, inicio);
        algoritmoAtual = "Prim";
        rotaAtual = criarRotaDeArestas(arestasAtual, inicio, "Prim");
        enriquecerComRuas(rotaAtual);
        return arestasAtual;
    }

    public List<Aresta> executarKruskal() {
        arestasAtual = kruskal.executar(grafo);
        algoritmoAtual = "Kruskal";
        rotaAtual = criarRotaDeArestas(arestasAtual, null, "Kruskal");
        enriquecerComRuas(rotaAtual);
        return arestasAtual;
    }

    public Rota executarGuloso(Ponto origem) {
        rotaAtual = normalizarPercurso(guloso.executar(grafo, origem));
        arestasAtual = construirArestas(rotaAtual.getPercurso());
        algoritmoAtual = "Guloso";
        enriquecerComRuas(rotaAtual);
        return rotaAtual;
    }

    public Rota executarTSP(Ponto origem) {
        rotaAtual = normalizarPercurso(tsp.otimizarRota(grafo, origem));
        arestasAtual = construirArestas(rotaAtual.getPercurso());
        algoritmoAtual = "Caixeiro Viajante";
        enriquecerComRuas(rotaAtual);
        return rotaAtual;
    }

    public Rota executarBFS(Ponto origem) {
        rotaAtual = normalizarPercurso(bfs.executar(grafo, origem));
        arestasAtual = construirArestas(rotaAtual.getPercurso());
        algoritmoAtual = "BFS";
        enriquecerComRuas(rotaAtual);
        return rotaAtual;
    }

    public Rota executarDFS(Ponto origem) {
        rotaAtual = normalizarPercurso(dfs.executar(grafo, origem));
        arestasAtual = construirArestas(rotaAtual.getPercurso());
        algoritmoAtual = "DFS";
        enriquecerComRuas(rotaAtual);
        return rotaAtual;
    }

    public Rota getRotaAtual() {
        return rotaAtual;
    }

    public List<Aresta> getArestasAtuais() {
        return arestasAtual;
    }

    public String getAlgoritmoAtual() {
        return algoritmoAtual;
    }

    public void atualizarTrajetoViario(Rota rota) {
        enriquecerComRuas(rota);
    }

    public void limpar() {
        rotaAtual = null;
        arestasAtual = new ArrayList<>();
        algoritmoAtual = null;
    }

    private List<Aresta> construirArestas(List<Ponto> percurso) {
        List<Aresta> arestas = new ArrayList<>();
        if (percurso == null) {
            return arestas;
        }
        for (int i = 1; i < percurso.size(); i++) {
            Aresta aresta = grafo.obterAresta(percurso.get(i - 1), percurso.get(i));
            if (aresta != null) {
                arestas.add(aresta);
            }
        }
        return arestas;
    }

    /** Expande mudanças entre vértices não adjacentes para caminhos reais do grafo. */
    private Rota normalizarPercurso(Rota original) {
        List<Ponto> pontos = original.getPercurso();
        if (pontos.size() < 2) return original;
        List<Ponto> continuo = new ArrayList<>();
        continuo.add(pontos.get(0));
        for (int i = 1; i < pontos.size(); i++) {
            Ponto anterior = continuo.get(continuo.size() - 1);
            Ponto destino = pontos.get(i);
            List<Ponto> trecho = Dijkstra.encontrarCaminho(grafo, anterior, destino);
            for (int j = 1; j < trecho.size(); j++) continuo.add(trecho.get(j));
        }
        List<Aresta> arestas = construirArestas(continuo);
        double distancia = AlgoritmoUtils.calcularPeso(arestas);
        return new Rota(continuo, arestas, original.getOrigem(),
                continuo.get(continuo.size() - 1), original.getAlgoritmo(), distancia,
                AlgoritmoUtils.calcularTempo(distancia), distancia, AlgoritmoUtils.calcularCusto(distancia));
    }

    private void enriquecerComRuas(Rota rota) {
        if (rota == null || rota.getPercurso().size() < 2) return;
        try {
            OsrmRouteService.Result result = osrm.rotear(rota.getPercurso());
            rota.definirTrajetoViario(result.getGeometry(), result.getLegDistancesKm(),
                    result.getTotalDistanceKm(), result.getDurationMinutes());
        } catch (Exception ignored) {
            // Sem rede, mantém-se a geometria local e as distâncias do grafo.
        }
    }

    private Rota criarRotaDeArestas(List<Aresta> arestas, Ponto origem, String algoritmo) {
        List<Ponto> percurso = new ArrayList<>();
        if (!arestas.isEmpty()) {
            Map<Ponto, List<Ponto>> arvore = new LinkedHashMap<>();
            for (Aresta aresta : arestas) {
                arvore.computeIfAbsent(aresta.getOrigem(), key -> new ArrayList<>()).add(aresta.getDestino());
                arvore.computeIfAbsent(aresta.getDestino(), key -> new ArrayList<>()).add(aresta.getOrigem());
            }
            Ponto inicio = origem != null && arvore.containsKey(origem) ? origem : arestas.get(0).getOrigem();
            construirPasseioContinuo(inicio, null, arvore, new LinkedHashSet<>(), percurso);
        }
        double distancia = AlgoritmoUtils.calcularPeso(arestas);
        return new Rota(percurso, arestas, origem, percurso.isEmpty() ? null : percurso.get(percurso.size() - 1),
                algoritmo, distancia, AlgoritmoUtils.calcularTempo(distancia), distancia, AlgoritmoUtils.calcularCusto(distancia));
    }

    /** Converte uma árvore em um passeio contínuo, voltando pelas arestas em vez de saltar entre ramos. */
    private void construirPasseioContinuo(Ponto atual, Ponto anterior,
                                           Map<Ponto, List<Ponto>> arvore,
                                           Set<Ponto> visitados,
                                           List<Ponto> percurso) {
        visitados.add(atual);
        percurso.add(atual);
        for (Ponto vizinho : arvore.getOrDefault(atual, new ArrayList<>())) {
            if (vizinho.equals(anterior) || visitados.contains(vizinho)) continue;
            construirPasseioContinuo(vizinho, atual, arvore, visitados, percurso);
            percurso.add(atual);
        }
    }
}
