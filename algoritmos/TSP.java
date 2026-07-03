package algoritmos;

import model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TSP {

    public Rota otimizarRota(
            Grafo grafo,
            Ponto origem
    ) {

        List<Ponto> percurso =
                new ArrayList<Ponto>();

        Set<Ponto> visitados =
                new HashSet<Ponto>();

        Ponto atual = origem;

        percurso.add(atual);

        visitados.add(atual);

        double distanciaTotal = 0;

        while (visitados.size()
                < grafo.getPontos().size()) {

            Ponto melhorPonto = null;
            double melhorDistancia = Double.MAX_VALUE;
            List<Ponto> melhorCaminho = new ArrayList<Ponto>();

            Map<Ponto, Double> distancias =
                    Dijkstra.calcular(grafo, atual);

            for (Ponto candidato : grafo.getPontos()) {

                if (!visitados.contains(candidato)
                        && distancias.get(candidato) < melhorDistancia) {

                    melhorPonto = candidato;
                    melhorDistancia = distancias.get(candidato);
                    melhorCaminho = Dijkstra.encontrarCaminho(
                            grafo,
                            atual,
                            candidato
                    );
                }
            }

            if (melhorPonto == null
                    || melhorDistancia == Double.MAX_VALUE) {
                break;
            }

            for (int i = 1; i < melhorCaminho.size(); i++) {
                Ponto ponto = melhorCaminho.get(i);

                if (percurso.isEmpty()
                        || !percurso.get(percurso.size() - 1).equals(ponto)) {
                    percurso.add(ponto);
                }

                visitados.add(ponto);
            }

            atual = melhorPonto;

            distanciaTotal += melhorDistancia;
        }

        double tempo =
                calcularTempo(distanciaTotal);

        return new Rota(
                percurso,
                distanciaTotal,
                tempo
        );
    }

    public Rota otimizarRotaComDestinoFinal(
            Grafo grafo,
            Ponto destinoFinal
    ) {

        List<Ponto> percurso =
                new ArrayList<Ponto>();

        Set<Ponto> naoVisitados =
                new HashSet<Ponto>();

        for (Ponto ponto : grafo.getPontos()) {

            if (!ponto.equals(destinoFinal)) {
                naoVisitados.add(ponto);
            }
        }

        Ponto atual =
                destinoFinal;

        double distanciaTotal = 0;

        percurso.add(destinoFinal);

        while (atual != null
                && !naoVisitados.isEmpty()) {

            Map<Ponto, Double> distancias =
                    Dijkstra.calcular(grafo, atual);

            Ponto melhorPonto = null;
            double melhorDistancia = Double.MAX_VALUE;

            for (Ponto candidato : naoVisitados) {

                double distancia =
                        distancias.get(candidato);

                if (distancia < melhorDistancia) {
                    melhorPonto = candidato;
                    melhorDistancia = distancia;
                }
            }

            if (melhorPonto == null
                    || melhorDistancia == Double.MAX_VALUE) {
                break;
            }

            List<Ponto> caminho =
                    Dijkstra.encontrarCaminho(
                            grafo,
                            atual,
                            melhorPonto
                    );

            adicionarCaminhoAoPercurso(
                    percurso,
                    caminho,
                    destinoFinal,
                    false
            );

            atual = melhorPonto;
            naoVisitados.removeAll(caminho);
            distanciaTotal += melhorDistancia;
        }

        if (!atual.equals(destinoFinal)) {

            Map<Ponto, Double> distancias =
                    Dijkstra.calcular(grafo, atual);

            List<Ponto> caminhoFinal =
                    Dijkstra.encontrarCaminho(
                            grafo,
                            atual,
                            destinoFinal
                    );

            adicionarCaminhoAoPercurso(
                    percurso,
                    caminhoFinal,
                    destinoFinal,
                    true
            );

            distanciaTotal +=
                    distancias.get(destinoFinal);
        }

        double tempo =
                calcularTempo(distanciaTotal);

        return new Rota(
                percurso,
                distanciaTotal,
                tempo
        );
    }

    private void adicionarCaminhoAoPercurso(
            List<Ponto> percurso,
            List<Ponto> caminho,
            Ponto destinoFinal,
            boolean permitirDestinoFinal
    ) {

        for (int i = 1; i < caminho.size(); i++) {

            Ponto ponto =
                    caminho.get(i);

            if (!permitirDestinoFinal
                    && ponto.equals(destinoFinal)) {
                continue;
            }

            if (percurso.isEmpty()
                    || !percurso.get(percurso.size() - 1).equals(ponto)) {
                percurso.add(ponto);
            }
        }
    }

    private double calcularTempo(
            double distancia
    ) {

        double velocidadeMedia = 40.0;

        return (distancia / velocidadeMedia)
                * 60;
    }
}
