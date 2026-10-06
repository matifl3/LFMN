package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.campeonato.EstadisticasCampeonatoResponse;
import org.example.lfmnacional.dto.campeonato.PilotoCampeonatoEstadisticasResponse;
import org.example.lfmnacional.dto.campeonato.RondaCampeonatoEstadisticasResponse;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.CampeonatoMiembro;
import org.example.lfmnacional.entity.CampeonatoPosicion;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.Incidente;
import org.example.lfmnacional.entity.IncidentePiloto;
import org.example.lfmnacional.entity.Inscripcion;
import org.example.lfmnacional.entity.ResolucionIncidente;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.Sancion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.example.lfmnacional.enums.EstadoInscripcion;
import org.example.lfmnacional.enums.OrigenSancion;
import org.example.lfmnacional.enums.RolPilotoIncidente;
import org.example.lfmnacional.enums.TipoSancion;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.repository.CampeonatoMiembroRepository;
import org.example.lfmnacional.repository.CampeonatoPosicionRepository;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.IncidentePilotoRepository;
import org.example.lfmnacional.repository.IncidenteRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SancionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampeonatoEstadisticasServiceTest {

    @Mock
    private CarreraRepository carreraRepository;
    @Mock
    private ResultadoCarreraRepository resultadoCarreraRepository;
    @Mock
    private InscripcionRepository inscripcionRepository;
    @Mock
    private IncidenteRepository incidenteRepository;
    @Mock
    private IncidentePilotoRepository incidentePilotoRepository;
    @Mock
    private SancionRepository sancionRepository;
    @Mock
    private CampeonatoMiembroRepository miembroRepository;
    @Mock
    private CampeonatoPosicionRepository posicionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CampeonatoEstadisticasService service;

    private Campeonato campeonato;
    private Usuario piloto1;
    private Usuario piloto2;
    private Usuario piloto3;
    private Carrera carrera1;
    private Carrera carrera2;

    @BeforeEach
    void setUp() {
        Categoria categoria = Categoria.builder().id(1L).nombre("GT3").build();
        campeonato = Campeonato.builder()
                .id(1L).nombre("Copa").temporada("2026").categoria(categoria)
                .estado(EstadoCampeonato.ACTIVO).visibilidad(VisibilidadCampeonato.PRIVADO)
                .build();
        piloto1 = Usuario.builder().id(1L).nombrePiloto("Ana").elo(1500).safetyRating(100).build();
        piloto2 = Usuario.builder().id(2L).nombrePiloto("Beto").elo(1400).safetyRating(90).build();
        piloto3 = Usuario.builder().id(3L).nombrePiloto("Caro").elo(1300).safetyRating(80).build();
        carrera1 = Carrera.builder().id(10L).nombre("Fecha 1").campeonato(campeonato)
                .circuito("San Luis").estado(EstadoCarrera.FINALIZADA).cupoMaximo(24)
                .fecha(LocalDateTime.of(2026, 3, 1, 20, 0)).build();
        carrera2 = Carrera.builder().id(11L).nombre("Fecha 2").campeonato(campeonato)
                .circuito("Rosario").estado(EstadoCarrera.PROGRAMADA).cupoMaximo(24)
                .fecha(LocalDateTime.of(2026, 3, 15, 20, 0)).build();

        lenient().when(miembroRepository.findByCampeonato_IdOrderByFechaAltaAsc(1L))
                .thenReturn(List.of(miembro(piloto1), miembro(piloto2), miembro(piloto3)));
        lenient().when(carreraRepository.findByCampeonato_IdOrderByFechaDesc(1L))
                .thenReturn(List.of(carrera2, carrera1));
        lenient().when(incidenteRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());
        lenient().when(incidentePilotoRepository.findByIncidente_Carrera_Campeonato_Id(1L))
                .thenReturn(List.of());
        lenient().when(sancionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());
        lenient().when(sancionRepository.findByResolucion_Incidente_Carrera_Campeonato_Id(1L))
                .thenReturn(List.of());
        lenient().when(posicionRepository.findByCampeonato_IdOrderByPuntosDesc(1L))
                .thenReturn(List.of());
    }

    private CampeonatoMiembro miembro(Usuario u) {
        return CampeonatoMiembro.builder().id(u.getId()).campeonato(campeonato).usuario(u).build();
    }

    private ResultadoCarrera resultado(Carrera c, Usuario u, Integer posicion, boolean finalizo,
                                       Long vueltaRapida, boolean pole) {
        return ResultadoCarrera.builder()
                .carrera(c).usuario(u).posicionFinal(posicion).finalizo(finalizo)
                .vueltaRapida(vueltaRapida).poles(pole)
                .eloGanado(posicion != null && posicion == 1 ? 5 : -1)
                .srGanado(finalizo ? 1 : -2)
                .build();
    }

    private Inscripcion inscripcion(Carrera c, Usuario u, EstadoInscripcion estado) {
        return Inscripcion.builder().carrera(c).usuario(u).estado(estado).build();
    }

    private PilotoCampeonatoEstadisticasResponse piloto(EstadisticasCampeonatoResponse r,
                                                       String nombre) {
        return r.pilotos().stream().filter(p -> p.nombrePiloto().equals(nombre)).findFirst()
                .orElseThrow(() -> new AssertionError("falta el piloto " + nombre));
    }

    private RondaCampeonatoEstadisticasResponse ronda(EstadisticasCampeonatoResponse r, String nombre) {
        return r.rondas().stream().filter(x -> x.nombre().equals(nombre)).findFirst()
                .orElseThrow(() -> new AssertionError("falta la ronda " + nombre));
    }

    @Test
    void cuentaCarrerasPorEstado() {
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(r.carrerasTotales()).isEqualTo(2);
        assertThat(r.carrerasFinalizadas()).isEqualTo(1);
        assertThat(r.carrerasProgramadas()).isEqualTo(1);
        assertThat(r.pilotosHabilitados()).isEqualTo(3);
        assertThat(r.participaciones()).isZero();
        assertThat(r.porcentajeFinalizacion()).isZero();
    }

    @Test
    void cuentaComoPendienteUnaCarreraConInscripcionesAbiertas() {
        // Si solo se contara el estado PROGRAMADA, una carrera con inscripciones
        // abiertas no sumaria en ninguna parte y los KPIs no cerrarían.
        Carrera abriendo = Carrera.builder().id(12L).nombre("Fecha 3").campeonato(campeonato)
                .circuito("Buenos Aires").estado(EstadoCarrera.INSCRIPCIONES_ABIERTAS)
                .cupoMaximo(24).fecha(LocalDateTime.of(2026, 3, 29, 20, 0)).build();
        when(carreraRepository.findByCampeonato_IdOrderByFechaDesc(1L))
                .thenReturn(List.of(abriendo, carrera2, carrera1));
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(r.carrerasTotales()).isEqualTo(3);
        assertThat(r.carrerasFinalizadas()).isEqualTo(1);
        assertThat(r.carrerasProgramadas()).isEqualTo(2);
    }

    @Test
    void agregaVictoriasPodiosYVueltasRapidasPorPiloto() {
        // Ana gana la fecha 1 y hace la vuelta rapida; Beto es segundo; Caro no
        // termino. La fecha 2 todavia no se corrio.
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                resultado(carrera1, piloto1, 1, true, 90000L, true),
                resultado(carrera1, piloto2, 2, true, 91000L, false),
                resultado(carrera1, piloto3, 4, false, null, false)));
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                inscripcion(carrera1, piloto1, EstadoInscripcion.INSCRIPTO),
                inscripcion(carrera1, piloto2, EstadoInscripcion.INSCRIPTO),
                inscripcion(carrera1, piloto3, EstadoInscripcion.INSCRIPTO),
                // Una baja no cuenta como inscripto para el calculo de ausentes.
                inscripcion(carrera2, piloto3, EstadoInscripcion.CANCELADA)));

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(r.participaciones()).isEqualTo(3);
        assertThat(r.abandono()).isEqualTo(1);
        assertThat(r.porcentajeFinalizacion()).isCloseTo(66.667, within(0.001));
        assertThat(r.pilotosConParticipacion()).isEqualTo(3);

        PilotoCampeonatoEstadisticasResponse ana = piloto(r, "Ana");
        assertThat(ana.victorias()).isEqualTo(1);
        assertThat(ana.podios()).isEqualTo(1);
        assertThat(ana.poles()).isEqualTo(1);
        assertThat(ana.vueltasRapidas()).isEqualTo(1);
        assertThat(ana.abandono()).isZero();
        assertThat(ana.porcentajeFinalizacion()).isEqualTo(100.0);
        assertThat(ana.eloGanado()).isEqualTo(5);
        assertThat(ana.srGanado()).isEqualTo(1);

        PilotoCampeonatoEstadisticasResponse caro = piloto(r, "Caro");
        assertThat(caro.victorias()).isZero();
        assertThat(caro.podios()).isZero();
        assertThat(caro.abandono()).isEqualTo(1);
        assertThat(caro.porcentajeFinalizacion()).isZero();
        assertThat(caro.srGanado()).isEqualTo(-2);
    }

    @Test
    void laVueltaRapidaEsElMinimoDeLaCarreraYNoElQueGana() {
        // Beto es segundo pero hace el menor tiempo: la vuelta rapida es suya.
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                resultado(carrera1, piloto1, 1, true, 91000L, false),
                resultado(carrera1, piloto2, 2, true, 88000L, false),
                resultado(carrera1, piloto3, 3, true, 90000L, false)));
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(piloto(r, "Beto").vueltasRapidas()).isEqualTo(1);
        assertThat(piloto(r, "Ana").vueltasRapidas()).isZero();
        assertThat(piloto(r, "Caro").vueltasRapidas()).isZero();
        assertThat(ronda(r, "Fecha 1").mejorVueltaMs()).isEqualTo(88000L);
        assertThat(ronda(r, "Fecha 1").mejorVueltaPiloto()).isEqualTo("Beto");
    }

    @Test
    void cuentaATodosLosEmpatadosEnLaVueltaRapida() {
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                resultado(carrera1, piloto1, 1, true, 88000L, false),
                resultado(carrera1, piloto2, 2, true, 88000L, false),
                resultado(carrera1, piloto3, 3, true, 90000L, false)));
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(piloto(r, "Ana").vueltasRapidas()).isEqualTo(1);
        assertThat(piloto(r, "Beto").vueltasRapidas()).isEqualTo(1);
        assertThat(piloto(r, "Caro").vueltasRapidas()).isZero();
    }

    @Test
    void cuentaInscriptosAusentesYGanadorPorRonda() {
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                resultado(carrera1, piloto1, 1, true, 90000L, false),
                resultado(carrera1, piloto2, 5, false, null, false)));
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                inscripcion(carrera1, piloto1, EstadoInscripcion.INSCRIPTO),
                inscripcion(carrera1, piloto2, EstadoInscripcion.INSCRIPTO),
                inscripcion(carrera1, piloto3, EstadoInscripcion.INSCRIPTO),
                inscripcion(carrera2, piloto1, EstadoInscripcion.INSCRIPTO),
                inscripcion(carrera2, piloto2, EstadoInscripcion.INSCRIPTO),
                inscripcion(carrera2, piloto3, EstadoInscripcion.LISTA_ESPERA)));

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        RondaCampeonatoEstadisticasResponse f1 = ronda(r, "Fecha 1");
        assertThat(f1.inscriptos()).isEqualTo(3);
        assertThat(f1.presentes()).isEqualTo(2);
        assertThat(f1.ausentes()).isEqualTo(1);
        assertThat(f1.abandono()).isEqualTo(1);
        assertThat(f1.ganador()).isEqualTo("Ana");

        // La lista de espera no cuenta como inscripto, y sin resultados los dos
        // anotados de la fecha 2 estan ausentes.
        RondaCampeonatoEstadisticasResponse f2 = ronda(r, "Fecha 2");
        assertThat(f2.inscriptos()).isEqualTo(2);
        assertThat(f2.presentes()).isZero();
        assertThat(f2.ausentes()).isEqualTo(2);
        assertThat(f2.ganador()).isNull();
        assertThat(f2.mejorVueltaMs()).isNull();
    }

    @Test
    void losAusentesNoSonNegativosConResultadosSinInscripcion() {
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                resultado(carrera1, piloto1, 1, true, 90000L, false)));
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(ronda(r, "Fecha 1").inscriptos()).isZero();
        assertThat(ronda(r, "Fecha 1").presentes()).isEqualTo(1);
        assertThat(ronda(r, "Fecha 1").ausentes()).isZero();
    }

    @Test
    void cuentaIncidentesYSancionesPorRondaYPorPiloto() {
        Incidente incidente = incidente(100L, EstadoIncidente.PENDIENTE);
        when(incidenteRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(incidente));
        when(incidentePilotoRepository.findByIncidente_Carrera_Campeonato_Id(1L)).thenReturn(List.of(
                IncidentePiloto.builder().id(1L).incidente(incidente).usuario(piloto1)
                        .rol(RolPilotoIncidente.CAUSANTE).build(),
                IncidentePiloto.builder().id(2L).incidente(incidente).usuario(piloto2)
                        .rol(RolPilotoIncidente.AFECTADO).build()));
        Sancion sancion = Sancion.builder().id(200L).usuario(piloto1).carrera(carrera1)
                .tipo(TipoSancion.PUESTOS).valor(2).origen(OrigenSancion.COMISARIO)
                .fecha(LocalDateTime.of(2026, 3, 2, 10, 0)).build();
        when(sancionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(sancion));
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                resultado(carrera1, piloto1, 1, true, 90000L, false),
                resultado(carrera1, piloto2, 2, true, 91000L, false)));
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(r.incidentes()).isEqualTo(1);
        assertThat(r.incidentesPendientes()).isEqualTo(1);
        assertThat(r.sanciones()).isEqualTo(1);
        assertThat(ronda(r, "Fecha 1").incidentes()).isEqualTo(1);
        assertThat(ronda(r, "Fecha 1").sanciones()).isEqualTo(1);

        // Solo el CAUSANTE cuenta como incidente del piloto.
        assertThat(piloto(r, "Ana").incidentes()).isEqualTo(1);
        assertThat(piloto(r, "Beto").incidentes()).isZero();
        assertThat(piloto(r, "Ana").sanciones()).isEqualTo(1);
        assertThat(piloto(r, "Beto").sanciones()).isZero();
    }

    @Test
    void losIncidentesResueltosNoCuentanComoPendientes() {
        when(incidenteRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                incidente(100L, EstadoIncidente.RESUELTO),
                incidente(101L, EstadoIncidente.EN_ANALISIS),
                incidente(102L, EstadoIncidente.PENDIENTE)));
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(r.incidentes()).isEqualTo(3);
        assertThat(r.incidentesPendientes()).isEqualTo(1);
    }

    @Test
    void sumaLasSancionesGeneradasPorIncidenteQueNoCuelganDeUnaCarrera() {
        // Es el caso de la importacion de sesiones: la sancion nace de la
        // resolucion del incidente y no tiene carrera, pero es del campeonato.
        Incidente incidente = incidente(100L, EstadoIncidente.RESUELTO);
        when(incidenteRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(incidente));
        ResolucionIncidente resolucion = ResolucionIncidente.builder().id(300L)
                .incidente(incidente).comisario(piloto1).explicacion("contacto").build();
        Sancion generada = Sancion.builder().id(400L).usuario(piloto2).carrera(null)
                .resolucion(resolucion).tipo(TipoSancion.SEGUNDOS).valor(5)
                .origen(OrigenSancion.REAL_PENALTY).build();
        when(sancionRepository.findByResolucion_Incidente_Carrera_Campeonato_Id(1L))
                .thenReturn(List.of(generada));
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                resultado(carrera1, piloto1, 1, true, 90000L, false),
                resultado(carrera1, piloto2, 2, true, 91000L, false)));
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(r.sanciones()).isEqualTo(1);
        assertThat(piloto(r, "Beto").sanciones()).isEqualTo(1);
        // Se le atribuye a la carrera del incidente, aunque la sancion no la tenga.
        assertThat(ronda(r, "Fecha 1").sanciones()).isEqualTo(1);
    }

    @Test
    void noCuentaDosVecesUnaSancionQueTieneCarreraYResolucion() {
        Incidente incidente = incidente(100L, EstadoIncidente.RESUELTO);
        ResolucionIncidente resolucion = ResolucionIncidente.builder().id(300L)
                .incidente(incidente).comisario(piloto1).explicacion("contacto").build();
        Sancion sancion = Sancion.builder().id(400L).usuario(piloto1).carrera(carrera1)
                .resolucion(resolucion).tipo(TipoSancion.SEGUNDOS).valor(5)
                .origen(OrigenSancion.COMISARIO).build();
        when(sancionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(sancion));
        when(sancionRepository.findByResolucion_Incidente_Carrera_Campeonato_Id(1L))
                .thenReturn(List.of(sancion));
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(r.sanciones()).isEqualTo(1);
        assertThat(piloto(r, "Ana").sanciones()).isEqualTo(1);
        assertThat(ronda(r, "Fecha 1").sanciones()).isEqualTo(1);
    }

    private Incidente incidente(Long id, EstadoIncidente estado) {
        return Incidente.builder().id(id).carrera(carrera1).reportante(piloto1).estado(estado)
                .descripcion("contacto").fecha(LocalDateTime.of(2026, 3, 1, 21, 0)).build();
    }

    @Test
    void incluyeMiembrosQueNuncaCorrieronConFilaEnCero() {
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(r.pilotos()).hasSize(3);
        assertThat(r.pilotosConParticipacion()).isZero();
        for (PilotoCampeonatoEstadisticasResponse p : r.pilotos()) {
            assertThat(p.carrerasDisputadas()).isZero();
            assertThat(p.puntos()).isZero();
            assertThat(p.posicion()).isZero();
            assertThat(p.victorias()).isZero();
            assertThat(p.porcentajeFinalizacion()).isZero();
        }
    }

    @Test
    void buscaLaFichaDelPilotoQueCorrioYNoEsMiembro() {
        // Caro no esta inscripto (puede haberse dado de baja despues de correr):
        // igual tiene que salir con su nombre y no como "Piloto #3".
        when(miembroRepository.findByCampeonato_IdOrderByFechaAltaAsc(1L))
                .thenReturn(List.of(miembro(piloto1), miembro(piloto2)));
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                resultado(carrera1, piloto3, 1, true, 90000L, false)));
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());
        when(usuarioRepository.findAllById(any())).thenReturn(List.of(piloto3));

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        PilotoCampeonatoEstadisticasResponse caro = piloto(r, "Caro");
        assertThat(caro.usuarioId()).isEqualTo(3L);
        assertThat(caro.elo()).isEqualTo(1300);
        assertThat(caro.safetyRating()).isEqualTo(80);
        assertThat(caro.victorias()).isEqualTo(1);
        verify(usuarioRepository).findAllById(any());
    }

    @Test
    void noInventaNombreSiElUsuarioYaNoExiste() {
        when(miembroRepository.findByCampeonato_IdOrderByFechaAltaAsc(1L)).thenReturn(List.of());
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                resultado(carrera1, piloto1, 1, true, 90000L, false)));
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());
        when(usuarioRepository.findAllById(any())).thenReturn(List.of());

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(r.pilotos()).hasSize(1);
        assertThat(r.pilotos().get(0).nombrePiloto()).isEqualTo("Piloto #1");
        assertThat(r.pilotos().get(0).elo()).isZero();
    }

    @Test
    void ordenaPilotosPorLaTablaDePosicionesYNoPorId() {
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                resultado(carrera1, piloto3, 5, true, null, false),
                resultado(carrera1, piloto1, 1, true, 90000L, false),
                resultado(carrera1, piloto2, 3, true, 91000L, false)));
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());
        // La tabla dice que Caro va tercera aunque su id sea el mas bajo: el orden
        // tiene que salir de ahi y no del id.
        when(posicionRepository.findByCampeonato_IdOrderByPuntosDesc(1L)).thenReturn(List.of(
                CampeonatoPosicion.builder().campeonato(campeonato).usuario(piloto2)
                        .puntos(18).posicion(1).build(),
                CampeonatoPosicion.builder().campeonato(campeonato).usuario(piloto1)
                        .puntos(25).posicion(2).build(),
                CampeonatoPosicion.builder().campeonato(campeonato).usuario(piloto3)
                        .puntos(12).posicion(3).build()));

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        assertThat(r.pilotos()).extracting(PilotoCampeonatoEstadisticasResponse::nombrePiloto)
                .containsExactly("Beto", "Ana", "Caro");
        assertThat(piloto(r, "Beto").posicion()).isEqualTo(1);
        assertThat(piloto(r, "Beto").puntos()).isEqualTo(18);
    }

    @Test
    void losPilotosSinPuntosQuedanAlFinal() {
        when(resultadoCarreraRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of(
                resultado(carrera1, piloto1, 1, true, 90000L, false),
                resultado(carrera1, piloto2, 2, true, 91000L, false)));
        when(inscripcionRepository.findByCarrera_Campeonato_Id(1L)).thenReturn(List.of());
        when(posicionRepository.findByCampeonato_IdOrderByPuntosDesc(1L)).thenReturn(List.of(
                CampeonatoPosicion.builder().campeonato(campeonato).usuario(piloto1)
                        .puntos(25).posicion(1).build(),
                CampeonatoPosicion.builder().campeonato(campeonato).usuario(piloto2)
                        .puntos(18).posicion(2).build()));

        EstadisticasCampeonatoResponse r = service.estadisticas(campeonato);

        // Caro esta habilitado pero sin puntuar: ultimo, no primero.
        assertThat(r.pilotos()).extracting(PilotoCampeonatoEstadisticasResponse::nombrePiloto)
                .containsExactly("Ana", "Beto", "Caro");
        assertThat(r.pilotos().get(2).posicion()).isZero();
    }
}