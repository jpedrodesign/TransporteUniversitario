package algoritmos;

import model.*;

import java.util.*;

public class Prim {

    public List<Aresta> executar(
            Grafo grafo,
            Ponto inicio
    ) {

        if (grafo == null || inicio == null) {
            throw new IllegalArgumentException(
                    "Grafo e ponto inicial sao obrigatorios."
            );
        }

        List<Aresta> resultado =
                new ArrayList<Aresta>();

        Set<Ponto> visitados =
                new HashSet<Ponto>();

        PriorityQueue<Aresta> fila =
                new PriorityQueue<Aresta>(
                        Comparator.comparingDouble(
                                Aresta::getDistancia
                        )
                );

        visitados.add(inicio);

        fila.addAll(
                grafo.getVizinhos(inicio)
        );

        while (!fila.isEmpty()) {

            Aresta aresta =
                    fila.poll();

            Ponto destino =
                    aresta.getDestino();

            if (!visitados.contains(destino)) {

                visitados.add(destino);

                resultado.add(aresta);

                fila.addAll(
                        grafo.getVizinhos(destino)
                );
            }
        }

        return resultado;
    }
}
