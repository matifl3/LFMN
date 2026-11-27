package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.incidente.IncidenteRequest;
import org.example.lfmnacional.dto.incidente.ResolucionRequest;
import org.example.lfmnacional.dto.incidente.VotoRequest;
import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.DecisionComisario;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.example.lfmnacional.enums.OrigenSancion;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.enums.TipoSancion;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.CategoriaRepository;
import org.example.lfmnacional.repository.SancionRepository;
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
 * Razon de existir: el segundo voto A_FAVOR cerraba el incidente solo (RESUELTO
 * sin resolucion ni sanciones) y la UI escondia el formulario de resolver en
 * cuanto eso pasaba; resolver con votos divididos lo dejaba en EN_ANALISIS. Ademas
 * el comisario venia en el body, asi que uno solo podia votar por dos.
 */
@SpringBootTest
@ActiveProfiles("test")
class IncidenteResolucionIntegracionTest {

    @Autowired
    private IncidenteService incidenteService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CarreraRepository carreraRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private CampeonatoRepository campeonatoRepository;
    @Autowired
    private SancionRepository sancionRepository;

    private Usuario reportante;
    private Usuario causante;
    private Usuario comisario1;
    private Usuario comisario2;
    private Long incidenteId;

    @BeforeEach
    void setUp() {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        reportante = usuario("rep", sufijo, Rol.USUARIO);
        causante = usuario("cau", sufijo, Rol.USUARIO);
        comisario1 = usuario("com1", sufijo, Rol.COMISARIO);
        comisario2 = usuario("com2", sufijo, Rol.COMISARIO);
        Categoria categoria = categoriaRepository.save(
                Categoria.builder().nombre("IncCat " + sufijo).eloMinimo(null).eloMaximo(null).build());
        Campeonato campeonato = campeonatoRepository.save(Campeonato.builder()
                .nombre("Copa Incidentes " + sufijo).temporada("2026").categoria(categoria)
                .estado(EstadoCampeonato.ACTIVO).sistemaPuntos("F1 STANDARD")
                .visibilidad(VisibilidadCampeonato.PUBLICO).build());
        Carrera carrera = carreraRepository.save(Carrera.builder()
                .nombre("Incidentes " + sufijo).circuito("Monza")
                .fecha(LocalDateTime.now().minusDays(1))
                .estado(EstadoCarrera.FINALIZADA).campeonato(campeonato).build());
        incidenteId = incidenteService.reportar(
                new IncidenteRequest(carrera.getId(), 3, "Toque en la T1", null), reportante).id();
    }

    private Usuario usuario(String nombre, String sufijo, Rol rol) {
        return usuarioRepository.save(Usuario.builder()
                .email("inc-" + nombre + "-" + sufijo + "@lfm.local")
                .password("secreto123").nombrePiloto(nombre + " " + sufijo)
                .rol(rol).elo(1200).safetyRating(1200).build());
    }

    private void votar(Usuario comisario, DecisionComisario decision) {
        incidenteService.votar(incidenteId, new VotoRequest(decision, null), comisario);
    }

    private ResolucionRequest resolucionConSancion() {
        return new ResolucionRequest("Causante penalizado", List.of(new SancionRequest(
                causante.getId(), null, null, TipoSancion.SAFETY_RATING, -10, "Toque T1",
                OrigenSancion.ADMIN, null, null)));
    }

    @Test
    @DisplayName("dos votos a favor no cierran el incidente: queda EN_ANALISIS esperando la resolucion")
    void votosNoCierranElIncidente() {
        votar(comisario1, DecisionComisario.A_FAVOR);
        votar(comisario2, DecisionComisario.A_FAVOR);

        assertThat(incidenteService.getById(incidenteId, comisario1).estado()).isEqualTo(EstadoIncidente.EN_ANALISIS);
    }

    @Test
    @DisplayName("el mismo comisario votando dos veces cambia su voto, no suma quorum")
    void mismoComisarioNoSumaQuorum() {
        votar(comisario1, DecisionComisario.A_FAVOR);
        votar(comisario1, DecisionComisario.EN_CONTRA);

        assertThatThrownBy(() -> incidenteService.guardarResolucion(incidenteId, resolucionConSancion(), comisario1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Faltan votos");
    }

    @Test
    @DisplayName("resolver con quorum cierra el incidente, aplica la sancion como COMISARIO y no admite otra")
    void resolverCierraYAplicaSanciones() {
        votar(comisario1, DecisionComisario.A_FAVOR);
        votar(comisario2, DecisionComisario.EN_CONTRA);

        var resolucion = incidenteService.guardarResolucion(incidenteId, resolucionConSancion(), comisario2);

        assertThat(resolucion.comisarioId()).isEqualTo(comisario2.getId());
        assertThat(incidenteService.getById(incidenteId, comisario1).estado()).isEqualTo(EstadoIncidente.RESUELTO);
        var sanciones = sancionRepository.findByResolucion_Id(resolucion.id());
        assertThat(sanciones).hasSize(1);
        assertThat(sanciones.get(0).getOrigen()).isEqualTo(OrigenSancion.COMISARIO);

        assertThatThrownBy(() -> incidenteService.guardarResolucion(incidenteId, resolucionConSancion(), comisario1))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> votar(comisario1, DecisionComisario.A_FAVOR))
                .isInstanceOf(BusinessException.class);
    }
}
