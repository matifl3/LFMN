package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.dto.sesion.SesionServerData;
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
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Razon de existir: la clasificacion se armaba solo por tiempo total. En AC el
 * {@code TotalTime} de un doblado es lo que tardo en hacer <em>sus</em> vueltas,
 * que son menos, asi que puede ser menor que el del ganador y el doblado
 * quedaba 1ro. Pasaba al importar y otra vez al aplicar una sancion de segundos,
 * que ademas metia al DNF ({@code TotalTime = 0}) adelante de todos y deshacia
 * las sanciones de puestos ya aplicadas.
 *
 * <p>Usa {@code sesiones/2026_8_8_RACE_doblado_y_dnf.json}: carrera a 10 vueltas,
 * Ana gana en 1.000.000 ms, Beto y Caro en la misma vuelta, Dani doblado (9
 * vueltas en 990.000 ms, menos que Ana) y Eze abandona en la vuelta 3.
 */
@SpringBootTest
@ActiveProfiles("test")
class ClasificacionDobladosIntegracionTest {

    private static final String SESION = "/sesiones/2026_8_8_RACE_doblado_y_dnf.json";

    @Autowired
    private ResultadoImportService resultadoImportService;
    @Autowired
    private SancionService sancionService;

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

    private Carrera carrera;
    private Map<String, Usuario> pilotos;

    @BeforeEach
    void setUp() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        SesionServerData sesion = leer();

        // Los GUID del JSON son fijos y guid_steam es unico: se liberan los que
        // haya dejado un test anterior en el mismo contexto de Spring.
        for (var dato : sesion.result()) {
            usuarioRepository.findByGuidSteam(dato.driverGuid()).ifPresent(u -> {
                u.setGuidSteam(null);
                usuarioRepository.save(u);
            });
        }
        pilotos = sesion.result().stream().collect(Collectors.toMap(
                r -> r.driverName(),
                r -> usuarioRepository.save(Usuario.builder()
                        .email("doblado-" + r.driverName().toLowerCase() + "-" + sufijo + "@lfm.local")
                        .password("secreto123").nombrePiloto(r.driverName() + " " + sufijo)
                        .guidSteam(r.driverGuid())
                        .rol(Rol.USUARIO).elo(1200).safetyRating(1200).build())));

        Categoria categoria = categoriaRepository.save(
                Categoria.builder().nombre("DobCat " + sufijo).eloMinimo(null).eloMaximo(null).build());
        Campeonato campeonato = campeonatoRepository.save(Campeonato.builder()
                .nombre("Copa Doblados " + sufijo).temporada("2026").categoria(categoria)
                .estado(EstadoCampeonato.ACTIVO).sistemaPuntos("F1 STANDARD")
                .visibilidad(VisibilidadCampeonato.PUBLICO).build());
        carrera = carreraRepository.save(Carrera.builder()
                .nombre("Round doblados").circuito("Balcarce")
                .fecha(LocalDateTime.now().minusDays(1))
                .estado(EstadoCarrera.FINALIZADA).campeonato(campeonato).build());

        resultadoImportService.importarResultados(carrera, sesion);
    }

    private SesionServerData leer() throws Exception {
        try (InputStream in = getClass().getResourceAsStream(SESION)) {
            assertThat(in).describedAs("recurso " + SESION).isNotNull();
            return new ObjectMapper().readValue(in, SesionServerData.class);
        }
    }

    /** Nombres de los pilotos en el orden de la clasificacion de la carrera. */
    private List<String> clasificacion() {
        Map<Long, String> nombrePorId = pilotos.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getValue().getId(), Map.Entry::getKey));
        return resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carrera.getId()).stream()
                .map(r -> nombrePorId.get(r.getUsuario().getId()))
                .toList();
    }

    private List<Integer> posiciones() {
        return resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carrera.getId()).stream()
                .map(ResultadoCarrera::getPosicionFinal)
                .toList();
    }

    private SancionRequest sancion(String piloto, TipoSancion tipo, int valor) {
        return new SancionRequest(pilotos.get(piloto).getId(), carrera.getId(), null, tipo, valor,
                "test", OrigenSancion.ADMIN, null, null);
    }

    @Test
    @DisplayName("Importar: el doblado queda detras de los que hicieron todas las vueltas y el DNF ultimo")
    void importarOrdenaPorVueltasYDespuesPorTiempo() {
        assertThat(clasificacion()).containsExactly("Ana", "Beto", "Caro", "Dani", "Eze");
        assertThat(posiciones()).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @DisplayName("SEGUNDOS: el sancionado cae por tiempo, pero no detras del doblado ni hay puestos repetidos")
    void segundosNoMueveAlDobladoNiAlDnf() {
        // Beto 1.020.000 + 40s = 1.060.000: queda detras de Caro (1.050.000).
        sancionService.create(sancion("Beto", TipoSancion.SEGUNDOS, 40));

        assertThat(clasificacion()).containsExactly("Ana", "Caro", "Beto", "Dani", "Eze");
        assertThat(posiciones()).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @DisplayName("SEGUNDOS a otro piloto no deshace una perdida de puestos ya aplicada")
    void segundosRespetaLaPerdidaDePuestos() {
        sancionService.create(sancion("Ana", TipoSancion.PUESTOS, 1));
        assertThat(clasificacion()).containsExactly("Beto", "Ana", "Caro", "Dani", "Eze");

        // Caro 1.050.000 + 5s: sigue 3ra por tiempo; Ana tiene que seguir 2da.
        sancionService.create(sancion("Caro", TipoSancion.SEGUNDOS, 5));

        assertThat(clasificacion()).containsExactly("Beto", "Ana", "Caro", "Dani", "Eze");
    }

    @Test
    @DisplayName("Revertir PUESTOS despues de SEGUNDOS vuelve al orden que corresponde")
    void revertirPuestosDespuesDeSegundos() {
        var puestos = sancionService.create(sancion("Ana", TipoSancion.PUESTOS, 1));
        sancionService.create(sancion("Beto", TipoSancion.SEGUNDOS, 40));

        sancionService.delete(puestos.id());

        // Solo queda la de Beto: Ana gana y Beto cae detras de Caro.
        assertThat(clasificacion()).containsExactly("Ana", "Caro", "Beto", "Dani", "Eze");
        assertThat(posiciones()).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @DisplayName("Revertir todas las sanciones devuelve la clasificacion de la importacion")
    void revertirTodoDevuelveElOrdenOriginal() {
        var puestos = sancionService.create(sancion("Ana", TipoSancion.PUESTOS, 2));
        var segundos = sancionService.create(sancion("Beto", TipoSancion.SEGUNDOS, 40));

        sancionService.delete(segundos.id());
        sancionService.delete(puestos.id());

        assertThat(clasificacion()).containsExactly("Ana", "Beto", "Caro", "Dani", "Eze");
    }
}
