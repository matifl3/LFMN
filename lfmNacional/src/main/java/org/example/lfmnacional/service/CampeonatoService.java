package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.campeonato.CampeonatoRequest;
import org.example.lfmnacional.dto.campeonato.CampeonatoResponse;
import org.example.lfmnacional.dto.campeonato.TablaPosicionResponse;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.CampeonatoPosicion;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.CampeonatoMiembroRepository;
import org.example.lfmnacional.repository.CampeonatoPosicionRepository;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.service.puntos.SistemaPuntos;
import org.example.lfmnacional.service.puntos.SistemaPuntosFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CampeonatoService {

    private final CampeonatoRepository campeonatoRepository;
    private final CampeonatoPosicionRepository campeonatoPosicionRepository;
    private final CampeonatoMiembroRepository miembroRepository;
    private final CategoriaService categoriaService;
    private final CampeonatoAccesoService accesoService;
    private final UsuarioRepository usuarioRepository;
    private final SistemaPuntosFactory sistemaPuntosFactory;

    public Campeonato getEntity(Long id) {
        return campeonatoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public CampeonatoResponse getById(Long id, Usuario usuario) {
        return toResponse(getEntity(id), usuario, Set.of());
    }

    /**
     * Sin cache a proposito: la respuesta incluye {@code soyAdmin} y
     * {@code soyMiembro}, que dependen de quien pregunta. Cachear esto por id
     * serviria el flags de un piloto a otro.
     */
    @Transactional(readOnly = true)
    public List<CampeonatoResponse> listAll(Usuario usuario) {
        Set<Long> membresias = accesoService.campeonatoIdsDondeEsMiembro(usuario != null ? usuario.getId() : null);
        return campeonatoRepository.findAll().stream()
                .map(c -> toResponse(c, usuario, membresias))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CampeonatoResponse> porCategoria(Long categoriaId, Usuario usuario) {
        Set<Long> membresias = accesoService.campeonatoIdsDondeEsMiembro(usuario != null ? usuario.getId() : null);
        return campeonatoRepository.findByCategoria_Id(categoriaId).stream()
                .map(c -> toResponse(c, usuario, membresias))
                .toList();
    }

    /** Campeonato que el usuario administra. Requiere ser ADMIN o ADMIN_CAMPEONATO. */
    @Transactional(readOnly = true)
    public List<CampeonatoResponse> misCampeonatos(Usuario usuario) {
        List<Campeonato> propios =
                accesoService.esAdminGlobal(usuario)
                        ? campeonatoRepository.findAll()
                        : campeonatoRepository.findByAdmin_IdOrderByIdDesc(usuario.getId());
        return propios.stream().map(c -> toResponse(c, usuario, Set.of())).toList();
    }

    /** Campeonato en los que el usuario esta como piloto miembro. */
    @Transactional(readOnly = true)
    public List<CampeonatoResponse> misMembresias(Usuario usuario) {
        Set<Long> ids = accesoService.campeonatoIdsDondeEsMiembro(usuario.getId());
        return campeonatoRepository.findAllById(ids).stream()
                .sorted(java.util.Comparator.comparing(Campeonato::getId))
                .map(c -> toResponse(c, usuario, ids))
                .toList();
    }

    /**
     * Crear un campeonato es solo del ADMIN global. El ADMIN_CAMPEONATO no crea:
     * el ADMIN global le asigna uno ya creado ({@code adminId}) y ahi empieza a
     * trabajar.
     */
    @Transactional
    @CacheEvict(value = "tabla_posiciones", allEntries = true)
    public CampeonatoResponse create(CampeonatoRequest request, Usuario usuario) {
        if (!accesoService.puedeCrearCampeonato(usuario)) {
            throw new BusinessException("Solo el administrador global puede crear campeonatos");
        }
        VisibilidadCampeonato visibilidad = request.visibilidad() != null
                ? request.visibilidad()
                : VisibilidadCampeonato.PUBLICO;
        Categoria categoria = categoriaService.getEntity(request.categoriaId());
        Usuario admin = resolverAdmin(request.adminId());
        // Sin esta guarda se creaba un campeonato privado sin dueno: nadie podia
        // administrarlo ni abrirlo a publico despues, porque cambiarVisibilidad exige
        // admin. Ademas dejaba a los pilotos que se inscribian sin poder ver su carrera.
        if (visibilidad == VisibilidadCampeonato.PRIVADO && admin == null) {
            throw new BusinessException(
                    "Un campeonato privado tiene que tener un administrador (rol ADMIN_CAMPEONATO)");
        }
        Campeonato campeonato = Campeonato.builder()
                .nombre(request.nombre())
                .temporada(request.temporada())
                .categoria(categoria)
                .estado(request.estado())
                .sistemaPuntos(request.sistemaPuntos())
                .visibilidad(visibilidad)
                .admin(admin)
                .build();
        return toResponse(campeonatoRepository.save(campeonato), usuario, Set.of());
    }

    @Transactional
    @CacheEvict(value = "tabla_posiciones", allEntries = true)
    public CampeonatoResponse update(Long id, CampeonatoRequest request, Usuario usuario) {
        Campeonato campeonato = getEntity(id);
        accesoService.exigirAdministra(usuario, campeonato);
        boolean adminGlobal = accesoService.esAdminGlobal(usuario);
        if (adminGlobal && request.visibilidad() != null
                && request.visibilidad() != campeonato.getVisibilidad()) {
            cambiarVisibilidad(campeonato, request.visibilidad(), usuario);
        }
        // El dueno solo lo mueve el ADMIN global: si lo pudiera cambiar el
        // ADMIN_CAMPEONATO, se traspasaria el campeonato a otro o se quedaria sin
        // dueno sin querer.
        if (adminGlobal) {
            campeonato.setAdmin(resolverAdmin(request.adminId()));
        }
        campeonato.setNombre(request.nombre());
        campeonato.setTemporada(request.temporada());
        campeonato.setCategoria(categoriaService.getEntity(request.categoriaId()));
        campeonato.setEstado(request.estado() != null ? request.estado() : campeonato.getEstado());
        campeonato.setSistemaPuntos(request.sistemaPuntos());
        return toResponse(campeonatoRepository.save(campeonato), usuario, Set.of());
    }

    /**
     * Traduce el {@code adminId} del request a un ADMIN_CAMPEONATO. {@code null}
     * deja el campeonato sin dueno. Rechaza cualquier otro rol a proposito: el
     * dueno administra el campeonato, y un USUARIO o COMISARIO no puede.
     */
    private Usuario resolverAdmin(Long adminId) {
        if (adminId == null) {
            return null;
        }
        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + adminId));
        if (admin.getRol() != Rol.ADMIN_CAMPEONATO) {
            throw new BusinessException("El campeonato solo se puede asignar a una cuenta con rol "
                    + "ADMIN_CAMPEONATO. " + admin.getNombrePiloto() + " es " + admin.getRol());
        }
        return admin;
    }

    @Transactional
    @CacheEvict(value = "tabla_posiciones", allEntries = true)
    public CampeonatoResponse cerrar(Long id, Usuario usuario) {
        Campeonato campeonato = getEntity(id);
        accesoService.exigirAdministra(usuario, campeonato);
        campeonato.setEstado(EstadoCampeonato.CERRADO);
        return toResponse(campeonatoRepository.save(campeonato), usuario, Set.of());
    }

    @Transactional
    @CacheEvict(value = "tabla_posiciones", allEntries = true)
    public void delete(Long id, Usuario usuario) {
        Campeonato campeonato = getEntity(id);
        accesoService.exigirAdministra(usuario, campeonato);
        if (campeonatoPosicionRepository.existsByCampeonato_Id(id)) {
            throw new BusinessException(
                    "No se puede eliminar el campeonato '" + campeonato.getNombre()
                            + "' porque ya tiene posiciones calculadas en la tabla de puntos");
        }
        miembroRepository.deleteByCampeonato_Id(id);
        campeonatoRepository.delete(campeonato);
    }

    /**
     * El gate de membresia NO puede vivir adentro: con {@code @Cacheable} el cache
     * se consulta antes de ejecutar el cuerpo, asi que un no miembro que ya warmio
     * la cache se saltearia el control. Por eso el control va en el controller,
     * contra {@link #getEntity(Long)}, y recien despues se llama este metodo.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tabla_posiciones", key = "#id")
    public List<TablaPosicionResponse> getTabla(Long id) {
        getEntity(id);
        return campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(id).stream()
                .map(this::toTablaPosicion)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "tabla_posiciones", key = "#carrera.campeonato.id")
    public void actualizarPuntos(Carrera carrera, List<ResultadoCarrera> resultados) {
        Campeonato campeonato = carrera.getCampeonato();
        if (campeonato.getEstado() != EstadoCampeonato.ACTIVO) {
            return;
        }
        List<CampeonatoPosicion> aGuardar = new java.util.ArrayList<>();
        SistemaPuntos sistema = sistemaPuntosFactory.de(campeonato.getSistemaPuntos());
        for (ResultadoCarrera resultado : resultados) {
            if (resultado.getPosicionFinal() == null) {
                continue;
            }
            int puntos = sistema.puntosPara(resultado.getPosicionFinal());
            CampeonatoPosicion posicion = campeonatoPosicionRepository
                    .findByCampeonato_IdAndUsuario_Id(campeonato.getId(), resultado.getUsuario().getId())
                    .orElseGet(() -> {
                        long total = campeonatoPosicionRepository.countByCampeonato_Id(campeonato.getId());
                        return CampeonatoPosicion.builder()
                                .campeonato(campeonato)
                                .usuario(resultado.getUsuario())
                                .puntos(0)
                                .posicion((int) total + 1)
                                .build();
                    });
            posicion.setPuntos(posicion.getPuntos() + puntos);
            aGuardar.add(posicion);
        }
        campeonatoPosicionRepository.saveAll(aGuardar);
        recalcularPosiciones(campeonato.getId());
    }

    private void cambiarVisibilidad(Campeonato campeonato,
                                    VisibilidadCampeonato visibilidad,
                                    Usuario usuario) {
        if (visibilidad == VisibilidadCampeonato.PUBLICO && !accesoService.esAdminGlobal(usuario)) {
            throw new BusinessException("Solo el admin global puede volver publico un campeonato");
        }
        if (visibilidad == VisibilidadCampeonato.PRIVADO && campeonato.getAdmin() == null) {
            throw new BusinessException("Para volver privado el campeonato tiene que tener un administrador");
        }
        campeonato.setVisibilidad(visibilidad);
    }

    private void recalcularPosiciones(Long campeonatoId) {
        List<CampeonatoPosicion> ordenadas =
                campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(campeonatoId);
        int rank = 1;
        for (CampeonatoPosicion posicion : ordenadas) {
            posicion.setPosicion(rank++);
        }
        campeonatoPosicionRepository.saveAll(ordenadas);
    }

    private TablaPosicionResponse toTablaPosicion(CampeonatoPosicion posicion) {
        return new TablaPosicionResponse(
                posicion.getUsuario().getId(),
                posicion.getUsuario().getNombrePiloto(),
                posicion.getPuntos(),
                posicion.getPosicion());
    }

    private CampeonatoResponse toResponse(Campeonato campeonato, Usuario usuario, Set<Long> membresias) {
        boolean privado = campeonato.getVisibilidad() == VisibilidadCampeonato.PRIVADO;
        return new CampeonatoResponse(
                campeonato.getId(),
                campeonato.getNombre(),
                campeonato.getTemporada(),
                campeonato.getCategoria().getId(),
                campeonato.getCategoria().getNombre(),
                campeonato.getEstado(),
                campeonato.getSistemaPuntos(),
                campeonato.getVisibilidad(),
                campeonato.getAdmin() != null ? campeonato.getAdmin().getId() : null,
                campeonato.getAdmin() != null ? campeonato.getAdmin().getNombrePiloto() : null,
                privado ? miembroRepository.countByCampeonato_Id(campeonato.getId()) : 0L,
                accesoService.administra(usuario, campeonato),
                esMiembro(usuario, campeonato, membresias));
    }

    /**
     * {@code Set.of()} y los sets inmutables de {@code SetN} lanzan NPE en
     * {@code contains(null)}, asi que un id sin persistir todavia no puede consultarse.
     */
    private boolean esMiembro(Usuario usuario, Campeonato campeonato, Set<Long> membresias) {
        if (accesoService.administra(usuario, campeonato)) {
            return true;
        }
        Long id = campeonato.getId();
        return id != null && membresias != null && membresias.contains(id);
    }
}
