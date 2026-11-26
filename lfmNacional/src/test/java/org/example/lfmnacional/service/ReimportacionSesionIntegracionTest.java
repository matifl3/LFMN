package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.campeonato.TablaPosicionResponse;
import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.dto.sesion.ResultadoSesionData;
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
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.CategoriaRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SesionProcesadaRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Razon de existir: la clave de idempotencia solo miraba las cantidades de
 * autos, resultados, vueltas y eventos. Un JSON corregido (un tiempo arreglado)
 * daba la misma clave y se rechazaba como duplicado, y la unica salida era
 * tocar la base. Ahora la clave es por contenido y hay una reimportacion
 * explicita que deshace lo anterior.
 *
 * <p>Usa {@code sesiones/2026_8_8_RACE_doblado_y_dnf.json} (Ana, Beto, Caro, Dani
 * doblado, Eze DNF). La "version corregida" le baja el tiempo a Caro para que
 * quede 2da.
 */
@SpringBootTest
@ActiveProfiles("test")
class ReimportacionSesionIntegracionTest {

    private static final String SESION = "/sesiones/2026_8_8_RACE_doblado_y_dnf.json";
    private static final int ELO_INICIAL = 1200;

    @Autowired
    private SesionServidorService sesionServidorService;
    @Autowired
    private SancionService sancionService;
    @Autowired
    private CampeonatoService campeonatoService;
    @Autowired
    private TablaPosicionesService tablaPosicionesService;

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
    private SesionProcesadaRepository sesionProcesadaRepository;

    private SesionServerData original;
    private SesionServerData corregida;
    private Carrera carrera;
    private Long campeonatoId;
    private Map<String, Usuario> pilotos;

    @BeforeEach
    void setUp() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        original = leer();
        corregida = conTiempo(original, "Caro", 1010000L);

        // Los GUID del JSON son fijos y guid_steam es unico: se liberan los que
        // haya dejado otro test en el mismo contexto de Spring.
        for (var dato : original.result()) {
            usuarioRepository.findByGuidSteam(dato.driverGuid()).ifPresent(u -> {
                u.setGuidSteam(null);
                usuarioRepository.save(u);
            });
        }
        pilotos = original.result().stream().collect(Collectors.toMap(
                ResultadoSesionData::driverName,
                r -> usuarioRepository.save(Usuario.builder()
                        .email("reimp-" + r.driverName().toLowerCase() + "-" + sufijo + "@lfm.local")
                        .password("secreto123").nombrePiloto(r.driverName() + " " + sufijo)
                        .guidSteam(r.driverGuid())
                        .rol(Rol.USUARIO).elo(ELO_INICIAL).safetyRating(ELO_INICIAL).build())));

        Categoria categoria = categoriaRepository.save(
                Categoria.builder().nombre("ReimpCat " + sufijo).eloMinimo(null).eloMaximo(null).build());
        Campeonato campeonato = campeonatoRepository.save(Campeonato.builder()
                .nombre("Copa Reimport " + sufijo).temporada("2026").categoria(categoria)
                .estado(EstadoCampeonato.ACTIVO).sistemaPuntos("F1 STANDARD")
                .visibilidad(VisibilidadCampeonato.PUBLICO).build());
        campeonatoId = campeonato.getId();
        carrera = carreraRepository.save(Carrera.builder()
                .nombre("Round reimport").circuito("Balcarce")
                .fecha(LocalDateTime.now().minusDays(1))
                .estado(EstadoCarrera.FINALIZADA).campeonato(campeonato).build());

        var primera = sesionServidorService.importarConIdempotencia(carrera.getId(), original, "race.json");
        assertThat(primera.yaProcesada()).isFalse();
    }

    private SesionServerData leer() throws Exception {
        try (InputStream in = getClass().getResourceAsStream(SESION)) {
            assertThat(in).describedAs("recurso " + SESION).isNotNull();
            return new ObjectMapper().readValue(in, SesionServerData.class);
        }
    }

    private static SesionServerData conTiempo(SesionServerData sesion, String piloto, long totalTime) {
        List<ResultadoSesionData> resultados = sesion.result().stream()
                .map(r -> !r.driverName().equals(piloto) ? r : new ResultadoSesionData(
                        r.driverName(), r.driverGuid(), r.carId(), r.carModel(), r.bestLap(),
                        totalTime, r.ballastKG(), r.restrictor()))
                .toList();
        return new SesionServerData(sesion.trackName(), sesion.trackConfig(), sesion.type(),
                sesion.durationSecs(), sesion.raceLaps(), sesion.cars(), resultados, sesion.laps(),
                sesion.events());
    }

    private List<String> clasificacion() {
        Map<Long, String> nombrePorId = pilotos.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getValue().getId(), Map.Entry::getKey));
        return resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carrera.getId()).stream()
                .map(r -> nombrePorId.get(r.getUsuario().getId()))
                .toList();
    }

    @Test
    @DisplayName("Reenviar el mismo JSON sigue devolviendo 'ya procesada', por import y por reimport")
    void mismoContenidoEsYaProcesada() {
        assertThat(sesionServidorService.importarConIdempotencia(carrera.getId(), original, "otro-nombre.json")
                .yaProcesada()).isTrue();
        assertThat(sesionServidorService.reimportar(carrera.getId(), original, null).yaProcesada()).isTrue();
    }

    @Test
    @DisplayName("Un JSON corregido por la importacion normal no se mezcla: pide reimportar")
    void jsonCorregidoPorImportNormalPideReimportar() {
        assertThatThrownBy(() -> sesionServidorService.importarConIdempotencia(carrera.getId(), corregida, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("reimportarla");
        assertThat(clasificacion()).containsExactly("Ana", "Beto", "Caro", "Dani", "Eze");
    }

    @Test
    @DisplayName("Reimportar reemplaza los resultados y no deja la sesion vieja registrada")
    void reimportarReemplazaLosResultados() {
        var resultado = sesionServidorService.reimportar(carrera.getId(), corregida, "race-corregida.json");

        assertThat(resultado.yaProcesada()).isFalse();
        assertThat(clasificacion()).containsExactly("Ana", "Caro", "Beto", "Dani", "Eze");
        assertThat(sesionProcesadaRepository.findByCarrera_IdOrderByFechaProcesamientoDesc(carrera.getId()))
                .singleElement()
                .satisfies(s -> assertThat(s.getNombreArchivo()).isEqualTo("race-corregida.json"));
    }

    @Test
    @DisplayName("Reimportar revierte el Elo de la carga anterior: cada piloto queda con el inicial + lo nuevo")
    void reimportarNoAcumulaElo() {
        sesionServidorService.reimportar(carrera.getId(), corregida, null);

        for (ResultadoCarrera resultado :
                resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carrera.getId())) {
            Usuario usuario = usuarioRepository.findById(resultado.getUsuario().getId()).orElseThrow();
            assertThat(usuario.getElo()).isEqualTo(ELO_INICIAL + resultado.getEloGanado());
            assertThat(usuario.getSafetyRating()).isEqualTo(ELO_INICIAL + resultado.getSrGanado());
        }
    }

    @Test
    @DisplayName("Reimportar rehace la tabla del campeonato sin sumar dos veces la carrera")
    void reimportarNoDuplicaPuntos() {
        sesionServidorService.reimportar(carrera.getId(), corregida, null);

        List<TablaPosicionResponse> tabla = tablaPosicionesService.tabla(campeonatoId);
        assertThat(tabla).extracting(TablaPosicionResponse::usuarioId).containsExactly(
                pilotos.get("Ana").getId(), pilotos.get("Caro").getId(), pilotos.get("Beto").getId(),
                pilotos.get("Dani").getId(), pilotos.get("Eze").getId());
        assertThat(tabla).extracting(TablaPosicionResponse::puntos).containsExactly(25, 18, 15, 12, 10);
    }

    @Test
    @DisplayName("Con sanciones cargadas en la carrera no se reimporta y no se toca nada")
    void conSancionesNoSeReimporta() {
        sancionService.create(new SancionRequest(pilotos.get("Beto").getId(), carrera.getId(), null,
                TipoSancion.PUESTOS, 1, "test", OrigenSancion.ADMIN, null, null));
        List<String> antes = clasificacion();

        assertThatThrownBy(() -> sesionServidorService.reimportar(carrera.getId(), corregida, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sanciones");
        assertThat(clasificacion()).isEqualTo(antes);
    }
}
