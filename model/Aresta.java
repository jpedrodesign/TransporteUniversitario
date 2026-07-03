package model;

public class Aresta {

    private Ponto origem;

    private Ponto destino;

    private double distancia;

    public Aresta(
            Ponto origem,
            Ponto destino,
            double distancia
    ) {

        this.origem = origem;
        this.destino = destino;
        this.distancia = distancia;
    }

    public Ponto getOrigem() {
        return origem;
    }

    public Ponto getDestino() {
        return destino;
    }

    public double getDistancia() {
        return distancia;
    }

    @Override
    public String toString() {

        return origem.getNome()
                + " -> "
                + destino.getNome()
                + " ("
                + distancia
                + " km)";
    }
}