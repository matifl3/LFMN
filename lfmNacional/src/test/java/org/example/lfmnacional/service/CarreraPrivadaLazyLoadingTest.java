package org.example.lfmnacional.service;

import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.SesionClasificacion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.entity.VueltaCarrera;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CampeonatoMiembroRepository;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.CategoriaRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SesionClasificacionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.repository.VueltaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code Carrera.campeonato} es LAZY y {@code open-in-view} esta en false, asi que
 * tocar la relacion fuera de una transaccion tira LazyInitializationException. Los
 * controladores llamaban a {@code exigirVeCarrera} con una Carrera despegada y las
 * pestañas de resultados, clasificacion y analisis devolvian 500. Estos tests
 * comprueban que el check de acceso ocurra adentro del servicio transaccional.
 *
 * <p>Los metodos de test NO son transaccionales a proposito: reproducen el mismo
 * limite transaccional que tiene el controller.
 */
@SpringBootTest
@ActiveProfiles("test")
class CarreraPrivadaLazyLoadingTest {

    @Autowired
    private ResultadoCarreraService resultadoCarreraService;
    @Autowired
    private SesionClasificacionService sesionClasificacionService;
    @Autowired
    private VueltaService vueltaService;
    @Autowired
    private CampeonatoAccesoService accesoService;
    @Autowired
    private CarreraService carreraService;

    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private CampeonatoMiembroRepository miembroRepository;
    @Autowired
    private CampeonatoRepository campeonatoRepository;
    @Autowired
    private CarreraRepository carreraRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ResultadoCarreraRepository resultadoCarreraRepository;
    @Autowired
    private SesionClasificacionRepository sesionClasificacionRepository;
    @Autowired
    private VueltaRepository vueltaRepository;
    @Autowired
    private InscripcionRepository inscripcionRepository;

    private Usuario piloto;
    private Usuario pilotoAjeno;
    private Carrera carreraPrivada;
    private Long campeonatoId;

    @BeforeEach
    void setUp() {
        // email y nombrePiloto son unicos y el contexto es compartido entre tests.
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        piloto = usuarioRepository.save(Usuario.builder()
                .email("lazy-piloto-" + sufijo + "@lfm.local").password("secreto123")
                .nombrePiloto("Lazy Piloto " + sufijo)
                .rol(Rol.USUARIO).elo(1200).safetyRating(1200).build());
        pilotoAjeno = usuarioRepository.save(Usuario.builder()
                .email("lazy-ajeno-" + sufijo + "@lfm.local").password("secreto123")
                .nombrePiloto("Lazy Ajeno " + sufijo)
                .rol(Rol.USUARIO).elo(1200).safetyRating(1200).build());
        Usuario dueno = usuarioRepository.save(Usuario.builder()
                .email("lazy-dueno-" + sufijo + "@lfm.local").password("secreto123")
                .nombrePiloto("Lazy Dueno " + sufijo)
                .rol(Rol.ADMIN_CAMPEONATO).elo(1200).safetyRating(1200).build());

        Categoria categoria = categoriaRepository.save(
                Categoria.builder().nombre("LazyCat " + sufijo).eloMinimo(null).eloMaximo(null).build());
        Campeonato privado = campeonatoRepository.save(Campeonato.builder()
                .nombre("Copa Lazy " + sufijo).temporada("2026").categoria(categoria)
                .estado(EstadoCampeonato.ACTIVO).sistemaPuntos("estandar")
                .visibilidad(VisibilidadCampeonato.PRIVADO).admin(dueno).build());
        campeonatoId = privado.getId();
        carreraPrivada = carreraRepository.save(Carrera.builder()
                .nombre("Privada Lazy").circuito("Monza")
                .fecha(LocalDateTime.now().minusDays(1))
                .estado(EstadoCarrera.FINALIZADA).campeonato(privado).build());

        resultadoCarreraRepository.save(ResultadoCarrera.builder()
                .carrera(carreraPrivada).usuario(piloto)
                .posicionFinal(1).tiempoTotal(900000L).finalizo(true).build());
        sesionClasificacionRepository.save(SesionClasificacion.builder()
                .carrera(carreraPrivada).usuario(piloto)
                .fecha(LocalDateTime.now().minusDays(1)).tiempo(148150L).build());
        for (int i = 1; i <= 3; i++) {
            vueltaRepository.save(VueltaCarrera.builder()
                    .carrera(carreraPrivada).usuario(piloto)
                    .numeroVuelta(i).tiempoMs(180000L + i).tipo("RACE")
                    .sector1(50000L).sector2(60000L).sector3(70000L).build());
        }
    }

    @Test
    @DisplayName("Resultados de una carrera privada: el check no tira LazyInitializationException")
    void resultadosDeCarreraPrivadaNoTiranLazyLoading() {
        assertThat(resultadoCarreraService.listarPorCarrera(carreraPrivada.getId(), piloto))
                .hasSize(1);
    }

    @Test
    @DisplayName("Clasificaciones de una carrera privada: idem")
    void clasificacionesDeCarreraPrivadaNoTiranLazyLoading() {
        assertThat(sesionClasificacionService.listarPorCarrera(carreraPrivada.getId(), piloto))
                .hasSize(1);
    }

    @Test
    @DisplayName("Analisis y resumen de vueltas de una carrera privada: idem")
    void analisisDeCarreraPrivadaNoTiraLazyLoading() {
        assertThat(vueltaService.analisisCarrera(carreraPrivada.getId(), piloto.getId(), piloto))
                .isNotEmpty();
        assertThat(vueltaService.resumenCarrera(carreraPrivada.getId(), piloto.getId(), piloto))
                .isNotNull();
    }

    @Test
    @DisplayName("El detalle por id tampoco toca la relacion fuera de transaccion")
    void getByIdNoTiraLazyLoading() {
        Long resultadoId = resultadoCarreraRepository
                .findByCarrera_IdOrderByPosicionFinalAsc(carreraPrivada.getId()).get(0).getId();
        Long sesionId = sesionClasificacionRepository
                .findByCarrera_IdOrderByTiempoAsc(carreraPrivada.getId()).get(0).getId();

        assertThat(resultadoCarreraService.getById(resultadoId, piloto)).isNotNull();
        assertThat(sesionClasificacionService.getById(sesionId, piloto)).isNotNull();
    }

    @Test
    @DisplayName("Un piloto que no corrio sigue sin ver la carrera privada: ahora con 400, no con 500")
    void unAjenoNoVeLaCarreraPrivada() {
        assertThatThrownBy(() -> resultadoCarreraService.listarPorCarrera(carreraPrivada.getId(), pilotoAjeno))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("privado");

        assertThat(inscripcionRepository.findByCarrera_IdAndUsuario_Id(
                carreraPrivada.getId(), pilotoAjeno.getId())).isEmpty();
    }

@Test
@DisplayName("El piloto que corrio la ve sin ser miembro: el acceso es por PARTICIPACION, no por membresia")
void elQueCorrioVeSuCarreraPrivadaSinSerMiembro() {
        // Sin inscripcion y sin membresia: solo tiene resultado cargado. Antes esto lo
        // escondia. Se verifica por el servicio porque veCarrera()/veContenido()
        // exigen sesion abierta: tocarlas fuera de una transaccion (como hacia el
        // controller) tira LazyInitializationException.
        assertThat(inscripcionRepository.findByCarrera_IdAndUsuario_Id(
                carreraPrivada.getId(), piloto.getId())).isEmpty();
        assertThat(miembroRepository.findByCampeonato_IdAndUsuario_Id(campeonatoId, piloto.getId()))
                .isEmpty();

        assertThat(resultadoCarreraService.listarPorCarrera(carreraPrivada.getId(), piloto)).hasSize(1);
        assertThat(sesionClasificacionService.listarPorCarrera(carreraPrivada.getId(), piloto)).hasSize(1);

        // Y sigue sin ver el contenido general del campeonato (roster, tabla).
        assertThat(accesoService.veContenido(piloto, campeonatoRepository.findById(campeonatoId).orElseThrow()))
                .isFalse();
    }
}