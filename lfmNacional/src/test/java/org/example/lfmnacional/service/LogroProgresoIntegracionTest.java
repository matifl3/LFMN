package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.logro.LogroRequest;
import org.example.lfmnacional.dto.logro.UsuarioLogroResponse;
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
import org.example.lfmnacional.enums.TipoCondicionLogro;
import org.example.lfmnacional.enums.TipoNotificacion;
import org.example.lfmnacional.enums.TipoSancion;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.CategoriaRepository;
import org.example.lfmnacional.repository.NotificacionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Razon de existir: el progreso de los logros solo se recalculaba al cargar
 * resultados, asi que sanciones y logros nuevos dejaban la barra vieja; y un logro
 * ganado se perdia si la metrica bajaba (Elo), para "ganarse" otra vez despues.
 */
@SpringBootTest
@ActiveProfiles("test")
class LogroProgresoIntegracionTest {

    @Autowired
    private LogroService logroService;
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
    private NotificacionRepository notificacionRepository;

    private String sufijo;
    private Usuario ana;
    private Usuario beto;
    private Carrera carrera;

    @BeforeEach
    void setUp() {
        sufijo = UUID.randomUUID().toString().substring(0, 8);
        ana = usuario("ana");
        beto = usuario("beto");

        Categoria categoria = categoriaRepository.save(
                Categoria.builder().nombre("LogCat " + sufijo).eloMinimo(null).eloMaximo(null).build());
        Campeonato campeonato = campeonatoRepository.save(Campeonato.builder()
                .nombre("Copa Logros " + sufijo).temporada("2026").categoria(categoria)
                .estado(EstadoCampeonato.ACTIVO).sistemaPuntos("F1 STANDARD")
                .visibilidad(VisibilidadCampeonato.PUBLICO).build());
        carrera = carreraRepository.save(Carrera.builder()
                .nombre("Round 1").circuito("Monza")
                .fecha(LocalDateTime.now().minusDays(1))
                .estado(EstadoCarrera.FINALIZADA).campeonato(campeonato).build());
        resultado(ana, 1, 100000L);
        resultado(beto, 2, 110000L);
        campeonatoService.actualizarPuntos(carrera,
                resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carrera.getId()));
    }

    private Usuario usuario(String nombre) {
        return usuarioRepository.save(Usuario.builder()
                .email("logro-" + nombre + "-" + sufijo + "@lfm.local")
                .password("secreto123").nombrePiloto(nombre + " " + sufijo)
                .rol(Rol.USUARIO).elo(1200).safetyRating(1200).build());
    }

    private void resultado(Usuario piloto, int posicion, long tiempo) {
        resultadoCarreraRepository.save(ResultadoCarrera.builder()
                .carrera(carrera).usuario(piloto).posicionFinal(posicion)
                .tiempoTotal(tiempo).finalizo(true).build());
    }

    private Carrera otraCarrera(String nombre) {
        return carreraRepository.save(Carrera.builder()
                .nombre(nombre).circuito("Monza")
                .fecha(LocalDateTime.now().minusDays(1))
                .estado(EstadoCarrera.FINALIZADA).campeonato(carrera.getCampeonato()).build());
    }

    private Long crearLogro(TipoCondicionLogro tipo, int valor) {
        return logroService.create(new LogroRequest("Test " + tipo + " " + valor + " " + sufijo,
                "logro de test", tipo, valor, "flag")).id();
    }

    private UsuarioLogroResponse logroDe(Usuario usuario, Long logroId) {
        return logroService.listarLogrosUsuario(usuario.getId()).stream()
                .filter(l -> l.logroId().equals(logroId)).findFirst().orElseThrow();
    }

    private long notificacionesDe(Usuario usuario, Long logroId) {
        String nombre = logroDe(usuario, logroId).nombre();
        return notificacionRepository.findByUsuario_IdAndTipo(usuario.getId(), TipoNotificacion.LOGRO).stream()
                .filter(n -> n.getMensaje().contains(nombre)).count();
    }

    @Test
    @DisplayName("un logro nuevo se evalua contra el historial que ya existe")
    void logroNuevoSeEvaluaConElHistorial() {
        Long logroId = crearLogro(TipoCondicionLogro.CARRERAS_COMPLETADAS, 1);

        UsuarioLogroResponse deAna = logroDe(ana, logroId);
        assertThat(deAna.obtenido()).isTrue();
        assertThat(deAna.fechaObtencion()).isNotNull();
    }

    @Test
    @DisplayName("el progreso se calcula en vivo aunque nadie haya llamado a evaluarLogros")
    void progresoEnVivo() {
        Long logroId = crearLogro(TipoCondicionLogro.CARRERAS_COMPLETADAS, 10);
        assertThat(logroDe(ana, logroId).progreso()).isEqualTo(1);

        Carrera otra = carreraRepository.save(Carrera.builder()
                .nombre("Round 2").circuito("Imola").fecha(LocalDateTime.now())
                .estado(EstadoCarrera.FINALIZADA).campeonato(carrera.getCampeonato()).build());
        resultadoCarreraRepository.save(ResultadoCarrera.builder()
                .carrera(otra).usuario(ana).posicionFinal(1).tiempoTotal(1L).finalizo(true).build());

        assertThat(logroDe(ana, logroId).progreso()).isEqualTo(2);
    }

    @Test
    @DisplayName("un logro de Elo ganado no se pierde si el Elo baja, ni se vuelve a notificar")
    void logroGanadoNoSePierde() {
        ana.setElo(9100);
        ana = usuarioRepository.save(ana);
        Long logroId = crearLogro(TipoCondicionLogro.ELO, 9000);
        assertThat(logroDe(ana, logroId).obtenido()).isTrue();

        ana.setElo(8000);
        ana = usuarioRepository.save(ana);
        logroService.evaluarLogros(ana);
        assertThat(logroDe(ana, logroId).obtenido()).isTrue();

        ana.setElo(9200);
        ana = usuarioRepository.save(ana);
        logroService.evaluarLogros(ana);
        assertThat(notificacionesDe(ana, logroId)).isEqualTo(1);
    }

    @Test
    @DisplayName("una sancion de puestos que le da la victoria a otro le desbloquea el logro")
    void sancionDePuestosReevaluaALosQueSuben() {
        Long logroId = crearLogro(TipoCondicionLogro.VICTORIAS, 1);
        assertThat(logroDe(beto, logroId).obtenido()).isFalse();

        sancionService.create(new SancionRequest(ana.getId(), carrera.getId(), null, TipoSancion.PUESTOS, 1,
                "corte de pista", OrigenSancion.ADMIN, null, null));

        assertThat(notificacionesDe(beto, logroId)).isEqualTo(1);
        assertThat(logroDe(beto, logroId).fechaObtencion()).isNotNull();
        // Ana la perdio en pista, pero el logro ya era suyo.
        assertThat(logroDe(ana, logroId).obtenido()).isTrue();
    }

    @Test
    @DisplayName("el resumen de una sola query cuenta igual que los COUNT que reemplaza")
    void resumenDeResultadosEnUnaQuery() {
        // Ana: gano Round 1 (en setUp). Le sumamos un 3ro con pole y un abandono.
        resultadoCarreraRepository.save(ResultadoCarrera.builder()
                .carrera(otraCarrera("Round 2")).usuario(ana).posicionFinal(3)
                .tiempoTotal(120000L).finalizo(true).build());
        resultadoCarreraRepository.save(ResultadoCarrera.builder()
                .carrera(otraCarrera("Round 3")).usuario(ana).posicionFinal(4)
                .poles(true).finalizo(false).build());

        var r = resultadoCarreraRepository.resumenPorUsuario(ana.getId());

        assertThat(r.carreras()).isEqualTo(3);
        assertThat(r.finalizadas()).isEqualTo(2);
        assertThat(r.victorias()).isEqualTo(1);
        assertThat(r.podios()).isEqualTo(2);
        assertThat(r.poles()).isEqualTo(1);
    }

    @Test
    @DisplayName("un piloto sin resultados da todo en cero, no null")
    void resumenSinResultados() {
        Usuario nuevo = usuario("nuevo");

        var r = resultadoCarreraRepository.resumenPorUsuario(nuevo.getId());

        assertThat(r.carreras()).isZero();
        assertThat(r.victorias()).isZero();
        assertThat(r.poles()).isZero();
    }
}
