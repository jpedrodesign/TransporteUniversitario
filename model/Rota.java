package model;

import java.util.List;

public class Rota {

    private List<Ponto> percurso;

    private double distanciaTotal;

    private double tempoTotal;

    public Rota(
            List<Ponto> percurso,
            double distanciaTotal,
            double tempoTotal
    ) {

        this.percurso = percurso;
        this.distanciaTotal = distanciaTotal;
        this.tempoTotal = tempoTotal;
    }

    public List<Ponto> getPercurso() {
        return percurso;
    }

    public double getDistanciaTotal() {
        return distanciaTotal;
    }

    public double getTempoTotal() {
        return tempoTotal;
    }

    public int calcularTotalAlunos() {

        int total = 0;

        for (Ponto ponto : percurso) {

            total +=
                    ponto.getQuantidadeAlunos();
        }

        return total;
    }
}