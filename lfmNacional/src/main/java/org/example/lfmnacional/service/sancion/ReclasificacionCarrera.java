package org.example.lfmnacional.service.sancion;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.Sancion;
import org.example.lfmnacional.enums.TipoSancion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SancionRepository;
import org.example.lfmnacional.service.CampeonatoService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rehacer la clasificacion de una carrera a partir de las sanciones activas
 * (GRASP Fabricacion pura): lo comparten {@link EfectoPuestos} y
 * {@link EfectoSegundos}, y no es responsabilidad natural de ninguna entidad.
 */
@Component
@RequiredArgsConstructor
public class ReclasificacionCarrera {

    private final SancionRepository sancionRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final CampeonatoService campeonatoService;

    /**
     * Orden real de llegada: los que terminaron adelante de los que no, y entre
     * los que terminaron primero el que hizo mas vueltas y despues el de menor
     * tiempo. El tiempo solo no alcanza porque el de un doblado es el de sus
     * vueltas, que son menos, y puede ser menor que el del ganador. Entre los
     * que no terminaron no se compara nada: el sort es estable y quedan en el
     * orden original.
     */
    private static final Comparator<ResultadoCarrera> ORDEN_DE_LLEGADA = (a, b) -> {
        if (a.isFinalizo() != b.isFinalizo()) {
            return a.isFinalizo() ? -1 : 1;
        }
        if (!a.isFinalizo()) {
            return 0;
        }
        return Comparator
                .comparing(ResultadoCarrera::getVueltasCompletadas, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ResultadoCarrera::getTiempoTotal, Comparator.nullsLast(Comparator.naturalOrder()))
                .compare(a, b);
    };

    /**
     * Rehace la clasificacion de la carrera desde cero cada vez que se aplica o
     * revierte una sancion de PUESTOS o SEGUNDOS.
     *
     * <p>Antes cada sancion movia la clasificacion que habia dejado la anterior:
     * una de segundos reordenaba toda la carrera solo por tiempo (un doblado o un
     * DNF con tiempo 0 podian quedar 1ros) y deshacia en silencio una perdida de
     * puestos ya aplicada; al revertir esa perdida el piloto subia desde un lugar
     * que ya no era el penalizado. Partiendo siempre de la posicion original, el
     * resultado depende solo de que sanciones estan activas, no del orden en que
     * se aplicaron o revirtieron.
     *
     * <ol>
     *   <li>Orden base: {@code posicionOriginal}, la de la carga.</li>
     *   <li>Si hay alguna sancion de segundos activa, el tiempo cambio y se
     *       reordena por {@link #ORDEN_DE_LLEGADA}. Si no hay, no se toca: una
     *       carga manual puede tener un orden que no sale de los tiempos.</li>
     *   <li>Las perdidas de puestos activas, de la mas vieja a la mas nueva.</li>
     * </ol>
     *
     * <p>Los resultados sin posicion quedan afuera, como estaban.
     */
    public void reclasificar(Carrera carrera) {
        List<ResultadoCarrera> clasificacion = resultadoCarreraRepository
                .findByCarrera_IdOrderByPosicionFinalAsc(carrera.getId()).stream()
                .filter(r -> r.getPosicionOriginal() != null || r.getPosicionFinal() != null)
                .collect(Collectors.toCollection(ArrayList::new));
        for (ResultadoCarrera resultado : clasificacion) {
            // Filas cargadas antes de existir la columna: la posicion actual es
            // la original mientras no tengan perdidas de puestos ya aplicadas.
            if (resultado.getPosicionOriginal() == null) {
                resultado.setPosicionOriginal(resultado.getPosicionFinal());
            }
        }
        clasificacion.sort(Comparator.comparing(ResultadoCarrera::getPosicionOriginal));

        List<Sancion> activas = sancionRepository.findByCarrera_Id(carrera.getId()).stream()
                .filter(s -> Boolean.TRUE.equals(s.getEfectosAplicados()) && s.getValor() != null)
                .sorted(Comparator.comparing(Sancion::getFecha, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Sancion::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (activas.stream().anyMatch(s -> s.getTipo() == TipoSancion.SEGUNDOS)) {
            clasificacion.sort(ORDEN_DE_LLEGADA);
        }
        for (Sancion sancion : activas) {
            if (sancion.getTipo() == TipoSancion.PUESTOS) {
                bajarPuestos(clasificacion, sancion.getUsuario().getId(), Math.abs(sancion.getValor()));
            }
        }

        renumerar(clasificacion);
        resultadoCarreraRepository.saveAll(clasificacion);
        recalcularTablaDelCampeonato(carrera);
    }

    /** El resultado del sancionado en la carrera de la sancion; sin el, la sancion no aplica. */
    public ResultadoCarrera resultadoDe(Sancion sancion) {
        return resultadoCarreraRepository
                .findByCarrera_IdAndUsuario_Id(sancion.getCarrera().getId(), sancion.getUsuario().getId())
                .orElseThrow(() -> new BusinessException(
                        "El usuario no tiene resultado en la carrera indicada"));
    }

    /**
     * Perder puestos NO es sumar el valor a {@code posicionFinal}: si el
     * sancionado cae sobre la posicion de otro quedan empatados y el renumerado
     * le da el mejor puesto al sancionado. Se lo mueve N lugares en la lista y
     * despues se renumera. Si no le quedan N lugares, cae ultimo. El valor se
     * toma absoluto: escribir "-2" no deberia ser un premio de dos puestos.
     */
    private static void bajarPuestos(List<ResultadoCarrera> clasificacion, Long usuarioId, int puestos) {
        for (int origen = 0; origen < clasificacion.size(); origen++) {
            if (clasificacion.get(origen).getUsuario().getId().equals(usuarioId)) {
                int destino = Math.min(origen + puestos, clasificacion.size() - 1);
                clasificacion.add(destino, clasificacion.remove(origen));
                return;
            }
        }
    }

    private static void renumerar(List<ResultadoCarrera> clasificacion) {
        int pos = 1;
        for (ResultadoCarrera resultado : clasificacion) {
            resultado.setPosicionFinal(pos++);
        }
    }

    /**
     * Mover posiciones o tiempos de una carrera cambia los puntos que el
     * campeonato le da a ese piloto, asi que la tabla se rehace. Sin esto la
     * sancion se ve reflejada en el resultado de la carrera y no en el
     * campeonato, que es donde se juega el campeonato.
     *
     * <p>Va como llamada a otro bean a proposito: el {@code @CacheEvict} de la
     * tabla de posiciones vive en {@code CampeonatoService}, y Spring solo lo
     * aplica si la llamada entra por su proxy, no desde adentro de la misma
     * clase.
     */
    private void recalcularTablaDelCampeonato(Carrera carrera) {
        if (carrera.getCampeonato() == null) {
            return;
        }
        campeonatoService.recalcularPuntos(carrera.getCampeonato().getId());
    }
}
