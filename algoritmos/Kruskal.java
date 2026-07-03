package algoritmos;

import model.*;

import java.util.*;

public class Kruskal {

    public List<Aresta> executar(
            List<Aresta> arestas
    ) {

        arestas.sort(
                Comparator.comparingDouble(
                        Aresta::getDistancia
                )
        );

        return arestas;
    }
}