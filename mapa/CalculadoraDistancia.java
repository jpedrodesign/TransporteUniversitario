package mapa;

/**
 * Classe responsável pelo cálculo
 * de distâncias geográficas entre
 * dois pontos utilizando a fórmula
 * de Haversine.
 *
 * A distância retornada é em quilômetros.
 */
public class CalculadoraDistancia {

    /**
     * Raio médio da Terra em quilômetros.
     */
    private static final double RAIO_TERRA = 6371.0;

    /**
     * Calcula a distância entre dois pontos.
     *
     * @param lat1 latitude do ponto 1
     * @param lon1 longitude do ponto 1
     * @param lat2 latitude do ponto 2
     * @param lon2 longitude do ponto 2
     * @return distância em quilômetros
     */
    public static double calcular(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {

        double latitude1 =
                Math.toRadians(lat1);

        double longitude1 =
                Math.toRadians(lon1);

        double latitude2 =
                Math.toRadians(lat2);

        double longitude2 =
                Math.toRadians(lon2);

        double diferencaLat =
                latitude2 - latitude1;

        double diferencaLon =
                longitude2 - longitude1;

        double a =
                Math.sin(diferencaLat / 2)
                        * Math.sin(diferencaLat / 2)
                        + Math.cos(latitude1)
                        * Math.cos(latitude2)
                        * Math.sin(diferencaLon / 2)
                        * Math.sin(diferencaLon / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return RAIO_TERRA * c;
    }

    /**
     * Calcula tempo estimado em minutos.
     *
     * @param distancia distância em km
     * @param velocidadeMedia velocidade média km/h
     * @return tempo em minutos
     */
    public static double calcularTempo(
            double distancia,
            double velocidadeMedia
    ) {

        if (velocidadeMedia <= 0) {

            throw new IllegalArgumentException(
                    "Velocidade inválida."
            );
        }

        return (distancia / velocidadeMedia) * 60;
    }

    /**
     * Formata distância.
     */
    public static String formatarDistancia(
            double distancia
    ) {

        return String.format(
                "%.2f km",
                distancia
        );
    }

    /**
     * Formata tempo.
     */
    public static String formatarTempo(
            double tempo
    ) {

        return String.format(
                "%.2f min",
                tempo
        );
    }
}