package algoritmos;

import model.*;

import java.util.*;

public class Dijkstra {

    public static Map<Ponto, Double> calcular(
            Grafo grafo,
            Ponto origem
    ) {

        validarEntrada(grafo, origem);

        Map<Ponto, Double> distancias = new HashMap<>();
        Map<Ponto, Ponto> anteriores = new HashMap<>();

        inicializarDistancias(
                grafo,
                distancias,
                origem
        );

        PriorityQueue<Ponto> fila = new PriorityQueue<>(
                Comparator.comparingDouble(distancias::get)
        );

        fila.add(origem);

        while (!fila.isEmpty()) {

            Ponto atual = fila.poll();

            for (Aresta aresta : grafo.getVizinhos(atual)) {

                Ponto vizinho = aresta.getDestino();

                double novaDistancia =
                        distancias.get(atual)
                                + aresta.getDistancia();

                if (novaDistancia < distancias.get(vizinho)) {

                    distancias.put(vizinho, novaDistancia);

                    anteriores.put(vizinho, atual);

                    fila.add(vizinho);
                }
            }
        }

        return distancias;
    }

    public static List<Ponto> encontrarCaminho(
            Grafo grafo,
            Ponto origem,
            Ponto destino
    ) {

        validarEntrada(grafo, origem);

        Map<Ponto, Double> distancias = new HashMap<>();
        Map<Ponto, Ponto> anteriores = new HashMap<>();

        inicializarDistancias(
                grafo,
                distancias,
                origem
        );

        PriorityQueue<Ponto> fila = new PriorityQueue<>(
                Comparator.comparingDouble(distancias::get)
        );

        fila.add(origem);

        while (!fila.isEmpty()) {

            Ponto atual = fila.poll();

            for (Aresta aresta : grafo.getVizinhos(atual)) {

                Ponto vizinho = aresta.getDestino();

                double novaDistancia =
                        distancias.get(atual)
                                + aresta.getDistancia();

                if (novaDistancia < distancias.get(vizinho)) {

                    distancias.put(vizinho, novaDistancia);

                    anteriores.put(vizinho, atual);

                    fila.add(vizinho);
                }
            }
        }

        return reconstruirCaminho(
                anteriores,
                origem,
                destino
        );
    }

    private static void inicializarDistancias(
            Grafo grafo,
            Map<Ponto, Double> distancias,
            Ponto origem
    ) {

        for (Ponto ponto : grafo.getPontos()) {
            distancias.put(ponto, Double.MAX_VALUE);
        }

        distancias.put(origem, 0.0);
    }

    private static List<Ponto> reconstruirCaminho(
            Map<Ponto, Ponto> anteriores,
            Ponto origem,
            Ponto destino
    ) {

        List<Ponto> caminho = new ArrayList<>();

        Ponto atual = destino;

        while (atual != null) {

            caminho.add(0, atual);

            atual = anteriores.get(atual);
        }

        if (!caminho.isEmpty()
                && caminho.get(0).equals(origem)) {

            return caminho;
        }

        return new ArrayList<>();
    }

    private static void validarEntrada(
            Grafo grafo,
            Ponto origem
    ) {

        if (grafo == null) {
            throw new IllegalArgumentException(
                    "O grafo não pode ser nulo."
            );
        }

        if (origem == null) {
            throw new IllegalArgumentException(
                    "O ponto de origem não pode ser nulo."
            );
        }
    }
}