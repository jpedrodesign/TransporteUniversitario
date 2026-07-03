package model;

import java.util.Objects;

/**
 * Classe responsável por representar
 * um ponto de embarque/desembarque
 * do sistema de transporte universitário.
 */
public class Ponto {

    private String nome;
    private double latitude;
    private double longitude;
    private int quantidadeAlunos;

    public Ponto(
            String nome,
            double latitude,
            double longitude,
            int quantidadeAlunos
    ) {

        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "O nome do ponto não pode ser vazio."
            );
        }

        if (quantidadeAlunos < 0) {
            throw new IllegalArgumentException(
                    "Quantidade de alunos inválida."
            );
        }

        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.quantidadeAlunos = quantidadeAlunos;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getQuantidadeAlunos() {
        return quantidadeAlunos;
    }

    public void setQuantidadeAlunos(int quantidadeAlunos) {

        if (quantidadeAlunos < 0) {
            throw new IllegalArgumentException(
                    "Quantidade inválida."
            );
        }

        this.quantidadeAlunos = quantidadeAlunos;
    }

    @Override
    public String toString() {

        return nome +
                " (" +
                quantidadeAlunos +
                " alunos)";
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Ponto)) {
            return false;
        }

        Ponto outro = (Ponto) obj;

        return Objects.equals(nome, outro.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }
}
