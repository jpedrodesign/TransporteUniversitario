package model;

import java.util.*;

public class Grafo {

    private Map<Ponto, List<Aresta>>
            adjacencias =
            new LinkedHashMap<Ponto, List<Aresta>>();

    public void adicionarPonto(
            Ponto ponto
    ) {

        adjacencias.putIfAbsent(
                ponto,
                new ArrayList<Aresta>()
        );
    }

    public void adicionarAresta(
            Ponto origem,
            Ponto destino,
            double distancia
    ) {

        adicionarPonto(origem);
        adicionarPonto(destino);

        adjacencias.get(origem)
                .add(
                        new Aresta(
                                origem,
                                destino,
                                distancia
                        )
                );

        adjacencias.get(destino)
                .add(
                        new Aresta(
                                destino,
                                origem,
                                distancia
                        )
                );
    }

    public void removerPonto(
            Ponto ponto
    ) {

        adjacencias.remove(ponto);

        for (List<Aresta> arestas : adjacencias.values()) {
            arestas.removeIf(
                    aresta -> aresta.getDestino().equals(ponto)
            );
        }
    }

    public List<Aresta> getVizinhos(
            Ponto ponto
    ) {

        return adjacencias.getOrDefault(
                ponto,
                new ArrayList<Aresta>()
        );
    }

    public Set<Ponto> getPontos() {
        return adjacencias.keySet();
    }

    public List<Aresta> getArestas() {

        List<Aresta> arestas =
                new ArrayList<Aresta>();

        Set<String> vistas =
                new HashSet<String>();

        for (List<Aresta> lista : adjacencias.values()) {

            for (Aresta aresta : lista) {

                String origem =
                        aresta.getOrigem().getNome();

                String destino =
                        aresta.getDestino().getNome();

                String chave =
                        origem.compareTo(destino) < 0
                                ? origem + "|" + destino
                                : destino + "|" + origem;

                if (vistas.add(chave)) {
                    arestas.add(aresta);
                }
            }
        }

        return arestas;
    }

    public double obterDistanciaDireta(
            Ponto origem,
            Ponto destino
    ) {

        for (Aresta aresta : getVizinhos(origem)) {

            if (aresta.getDestino().equals(destino)) {
                return aresta.getDistancia();
            }
        }

        return Double.MAX_VALUE;
    }
}
