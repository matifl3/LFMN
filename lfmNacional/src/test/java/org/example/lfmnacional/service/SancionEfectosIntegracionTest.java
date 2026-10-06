package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.campeonato.TablaPosicionResponse;
import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.OrigenSancion;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.enums.TipoSancion;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CampeonatoPosicionRepository;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.CategoriaRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Razon de existir: las sanciones que mueven la clasificacion (PUESTOS y
 * SEGUNDOS) tocaban el resultado de la carrera, pero la tabla del campeonato se
 * armaba una sola vez al cargar resultados y quedaba con los puntos de antes de
 * la penalizacion.
 *
 * <p>Van contra H2 y no con mocks porque lo que se comprueba es justamente el ida
 * y vuelta a la base: que el {@code deleteByCampeonato_Id} + reinsercion de
 * {@code recalcularPuntos} no deje filas duplicadas ni huerfanas, y que las
 * validaciones de las entidades no revienten la transaccion de la sancion.
 *
 * <p>Carrera de 4 pilotos para que "perder 2 puestos" se vea como caer dos
 * lugares: con 3 el sancionado siempre cae ultimo y no se distingue un bug de un
 * clamp legitimo. Los tiempos estan separados (Beto a 30s de Caro) para que la
 * reclasificacion por tiempo de una penalizacion de segundos tambien se note.
 */
@SpringBootTest
@ActiveProfiles("test")
class SancionEfectosIntegracionTest {

    @Autowired
    private SancionService sancionService;
    @Autowired
    private CampeonatoService campeonatoService;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private CampeonatoRepository campeonatoRepository;
    @Autowired
    private CarreraRepository carreraRepository;
    @Autowired
    private ResultadoCarreraRepository resultadoCarreraRepository;
    @Autowired
    private CampeonatoPosicionRepository campeonatoPosicionRepository;

    private Usuario ana;
    private Usuario beto;
    private Usuario caro;
    private Usuario dani;
    private Carrera carrera;
    private Long campeonatoId;

    @BeforeEach
    void setUp() {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        ana = usuario("Ana", sufijo);
        beto = usuario("Beto", sufijo);
        caro = usuario("Caro", sufijo);
        dani = usuario("Dani", sufijo);

        Categoria categoria = categoriaRepository.save(
                Categoria.builder().nombre("SancCat " + sufijo).eloMinimo(null).eloMaximo(null).build());
        Campeonato campeonato = campeonatoRepository.save(Campeonato.builder()
                .nombre("Copa Sancion " + sufijo).temporada("2026").categoria(categoria)
                .estado(EstadoCampeonato.ACTIVO).sistemaPuntos("F1 STANDARD")
                .visibilidad(VisibilidadCampeonato.PUBLICO).build());
        campeonatoId = campeonato.getId();

        carrera = carreraRepository.save(Carrera.builder()
                .nombre("Round 1").circuito("Monza")
                .fecha(LocalDateTime.now().minusDays(1))
                .estado(EstadoCarrera.FINALIZADA).campeonato(campeonato).build());

        // Tiempos en ms: el de Beto queda a 30s de Caro, asi que una penalizacion
        // de 100s lo saca del 2do lugar. Con tiempos pegados la reclasificacion
        // por tiempo no se notaria.
        Long[] tiempos = { 100000L, 200000L, 250000L, 400000L };
        int pos = 1;
        int i = 0;
        for (Usuario piloto : List.of(ana, beto, caro, dani)) {
            resultadoCarreraRepository.save(ResultadoCarrera.builder()
                    .carrera(carrera).usuario(piloto).posicionFinal(pos++)
                    .tiempoTotal(tiempos[i++]).finalizo(true).build());
        }

        // Es lo que hace la importacion de la sesion: arma la tabla 25/18/15/12.
        campeonatoService.actualizarPuntos(carrera,
                resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carrera.getId()));
    }

    private Usuario usuario(String nombre, String sufijo) {
        return usuarioRepository.save(Usuario.builder()
                .email("sancion-" + nombre.toLowerCase() + "-" + sufijo + "@lfm.local")
                .password("secreto123").nombrePiloto(nombre + " " + sufijo)
                .rol(Rol.USUARIO).elo(1200).safetyRating(1200).build());
    }

    private SancionRequest puestos(Usuario piloto, int valor, String motivo) {
        return new SancionRequest(piloto.getId(), carrera.getId(), null, TipoSancion.PUESTOS, valor, motivo,
                OrigenSancion.ADMIN, null, null);
    }

    private SancionRequest segundos(Usuario piloto, int valor, String motivo) {
        return new SancionRequest(piloto.getId(), carrera.getId(), null, TipoSancion.SEGUNDOS, valor, motivo,
                OrigenSancion.ADMIN, null, null);
    }

    private Integer posicionEnCarrera(Usuario piloto) {
        return resultadoCarreraRepository
                .findByCarrera_IdAndUsuario_Id(carrera.getId(), piloto.getId())
                .orElseThrow()
                .getPosicionFinal();
    }

    private Long tiempoEnCarrera(Usuario piloto) {
        return resultadoCarreraRepository
                .findByCarrera_IdAndUsuario_Id(carrera.getId(), piloto.getId())
                .orElseThrow()
                .getTiempoTotal();
    }

    private List<Integer> posicionesDeLaCarrera() {
        return resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carrera.getId()).stream()
                .map(ResultadoCarrera::getPosicionFinal)
                .toList();
    }

    /**
     * Lee la tabla por el servicio y no a mano sobre las entidades del
     * repository: {@code usuario} es LAZY y {@code open-in-view} esta en false,
     * asi que mapearlas desde el test tira LazyInitializationException. El
     * servicio mapea adentro de su transaccion, y como la recalculacion evicta
     * el cache, lo que devuelve es la base recien escrita.
     */
    private List<TablaPosicionResponse> tabla() {
        return campeonatoService.getTabla(campeonatoId);
    }

    @Test
    @DisplayName("La tabla arranca con los puntos de la carrera importada: 25/18/15/12")
    void laTablaArrancaConLosPuntosDeLaCarrera() {
        assertThat(tabla()).extracting(TablaPosicionResponse::usuarioId)
                .containsExactly(ana.getId(), beto.getId(), caro.getId(), dani.getId());
        assertThat(tabla()).extracting(TablaPosicionResponse::puntos).containsExactly(25, 18, 15, 12);
    }

    @Test
    @DisplayName("PUESTOS mueve la posicion en la carrera: el sancionado cae y los de atras suben")
    void puestosMueveLaPosicionEnLaCarrera() {
        sancionService.create(puestos(beto, 2, "exceso de velocidad"));

        // Beto 2 -> 4, Caro 3 -> 2 y Dani 4 -> 3.
        assertThat(posicionEnCarrera(ana)).isEqualTo(1);
        assertThat(posicionEnCarrera(caro)).isEqualTo(2);
        assertThat(posicionEnCarrera(dani)).isEqualTo(3);
        assertThat(posicionEnCarrera(beto)).isEqualTo(4);
        assertThat(posicionesDeLaCarrera()).containsExactly(1, 2, 3, 4);
    }

    @Test
    @DisplayName("PUESTOS tambien corrige los puntos del campeonato")
    void puestosCorrigeLosPuntosDelCampeonato() {
        sancionService.create(puestos(beto, 2, "exceso de velocidad"));

        assertThat(tabla()).extracting(TablaPosicionResponse::usuarioId)
                .containsExactly(ana.getId(), caro.getId(), dani.getId(), beto.getId());
        assertThat(tabla()).extracting(TablaPosicionResponse::puntos).containsExactly(25, 18, 15, 12);
    }

    @Test
    @DisplayName("Lo que ve el usuario (getTabla) sale del cache viejo: ya viene recalculado")
    void elCacheDeLaTablaSeEvicta() {
        // Un puesto sola no cambia el multifacet de puntos, solo el orden de la
        // tabla, asi que lo que se mira es el orden de los pilotos.
        assertThat(tabla()).extracting(TablaPosicionResponse::usuarioId)
                .containsExactly(ana.getId(), beto.getId(), caro.getId(), dani.getId());

        sancionService.create(puestos(beto, 1, "exceso de velocidad"));

        assertThat(tabla()).extracting(TablaPosicionResponse::usuarioId)
                .containsExactly(ana.getId(), caro.getId(), beto.getId(), dani.getId());
    }

    @Test
    @DisplayName("Revertir la sancion deja la carrera y la tabla como estaban")
    void revertirDevuelveTodoAlEstadoOriginal() {
        var respuesta = sancionService.create(puestos(beto, 2, "exceso de velocidad"));

        // delete() es el camino que revierte efectos y borra la sancion.
        sancionService.delete(respuesta.id());

        assertThat(posicionEnCarrera(ana)).isEqualTo(1);
        assertThat(posicionEnCarrera(beto)).isEqualTo(2);
        assertThat(posicionEnCarrera(caro)).isEqualTo(3);
        assertThat(posicionEnCarrera(dani)).isEqualTo(4);
        assertThat(tabla()).extracting(TablaPosicionResponse::puntos).containsExactly(25, 18, 15, 12);
    }

    @Test
    @DisplayName("Dos sanciones seguidos no acumulan filas: la tabla se rehace, no se agrega")
    void dosSancionesNoDejanFilasDuplicadas() {
        sancionService.create(puestos(beto, 1, "primer aviso"));
        sancionService.create(puestos(beto, 1, "segundo aviso"));

        // Sin filas duplicadas ni huerfanas: la tabla se rehace entera.
        assertThat(campeonatoPosicionRepository.countByCampeonato_Id(campeonatoId)).isEqualTo(4);
        assertThat(tabla()).extracting(TablaPosicionResponse::usuarioId).doesNotHaveDuplicates();
        assertThat(tabla()).extracting(TablaPosicionResponse::puntos).containsExactly(25, 18, 15, 12);
    }

    @Test
    @DisplayName("Perder mas puestos de los que quedan no rompe la numeracion: cae ultimo")
    void perderMasPuestosDeLosQueQuedanCaeUltimo() {
        sancionService.create(puestos(beto, 10, "recargo absurdo"));

        assertThat(posicionesDeLaCarrera()).containsExactly(1, 2, 3, 4);
        assertThat(posicionEnCarrera(beto)).isEqualTo(4);
        assertThat(tabla()).extracting(TablaPosicionResponse::puntos).containsExactly(25, 18, 15, 12);
    }

    @Test
    @DisplayName("SEGUNDOS suma el tiempo Y reclasifica por tiempo total")
    void segundosCambiaElTiempoYReclasifica() {
        // Beto 2do con 200.000 ms; +100s lo deja en 300.000, detras de Caro.
        sancionService.create(segundos(beto, 100, "exceso de velocidad en pits"));

        assertThat(tiempoEnCarrera(beto)).isEqualTo(300000L);
        assertThat(posicionesDeLaCarrera()).containsExactly(1, 2, 3, 4);
        assertThat(posicionEnCarrera(caro)).isEqualTo(2);
        assertThat(posicionEnCarrera(beto)).isEqualTo(3);
        assertThat(tabla()).extracting(TablaPosicionResponse::usuarioId)
                .containsExactly(ana.getId(), caro.getId(), beto.getId(), dani.getId());
    }

    @Test
    @DisplayName("Revertir SEGUNDOS devuelve el tiempo y la clasificacion originales")
    void revertirSegundosDevuelveElTiempoYLaClasificacion() {
        var respuesta = sancionService.create(segundos(beto, 100, "exceso de velocidad en pits"));

        sancionService.delete(respuesta.id());

        assertThat(tiempoEnCarrera(beto)).isEqualTo(200000L);
        assertThat(posicionEnCarrera(beto)).isEqualTo(2);
        assertThat(posicionesDeLaCarrera()).containsExactly(1, 2, 3, 4);
        assertThat(tabla()).extracting(TablaPosicionResponse::usuarioId)
                .containsExactly(ana.getId(), beto.getId(), caro.getId(), dani.getId());
    }

    @Test
    @DisplayName("Sin tiempo total la penalizacion de segundos falla con un mensaje claro, no con NPE")
    void segundosSinTiempoTotalFallaConMensajeClaro() {
        ResultadoCarrera resultado = resultadoCarreraRepository
                .findByCarrera_IdAndUsuario_Id(carrera.getId(), dani.getId()).orElseThrow();
        resultado.setTiempoTotal(null);
        resultadoCarreraRepository.save(resultado);

        assertThatThrownBy(() -> sancionService.create(segundos(dani, 10, "sin tiempo cargado")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tiempo total");
    }
}
