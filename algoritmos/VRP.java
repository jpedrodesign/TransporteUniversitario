
package algoritmos;

import model.*;

import java.util.ArrayList;
import java.util.List;

public class VRP {

    public List<Onibus> distribuirOnibus(
            List<Ponto> pontos
    ) {

        List<Onibus> onibus =
                new ArrayList<Onibus>();

        Onibus bus1 =
                new Onibus(
                        "ABC-1234",
                        50
                );

        Onibus bus2 =
                new Onibus(
                        "DEF-5678",
                        50
                );

        onibus.add(bus1);
        onibus.add(bus2);

        return onibus;
    }
}