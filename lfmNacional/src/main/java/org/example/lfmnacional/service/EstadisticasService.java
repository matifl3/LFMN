package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.estadistica.EstadisticasResponse;
import org.example.lfmnacional.enums.EstadoApelacion;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.repository.AnuncioRepository;
import org.example.lfmnacional.repository.ApelacionRepository;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.CategoriaRepository;
import org.example.lfmnacional.repository.IncidenteRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SancionRepository;
import org.example.lfmnacional.repository.SetupRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private final UsuarioRepository usuarioRepository;
    private final CarreraRepository carreraRepository;
    private final IncidenteRepository incidenteRepository;
    private final SancionRepository sancionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final ApelacionRepository apelacionRepository;
    private final SetupRepository setupRepository;
    private final AnuncioRepository anuncioRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;

    @Transactional(readOnly = true)
    public EstadisticasResponse getEstadisticas() {
        return new EstadisticasResponse(
                usuarioRepository.count(),
                resultadoCarreraRepository.countUsuariosConResultados(),
                usuarioRepository.countByRol(Rol.ADMIN),
                usuarioRepository.countByRol(Rol.COMISARIO),
                usuarioRepository.countByRol(Rol.ADMIN_CAMPEONATO),
                carreraRepository.count(),
                carreraRepository.countByEstado(EstadoCarrera.PROGRAMADA),
                carreraRepository.countByEstado(EstadoCarrera.INSCRIPCIONES_ABIERTAS),
                carreraRepository.countByEstado(EstadoCarrera.INSCRIPCIONES_CERRADAS),
                carreraRepository.countByEstado(EstadoCarrera.EN_CURSO),
                carreraRepository.countByEstado(EstadoCarrera.FINALIZADA),
                carreraRepository.countByEstado(EstadoCarrera.CANCELADA),
                incidenteRepository.count(),
                incidenteRepository.countByEstado(EstadoIncidente.PENDIENTE),
                incidenteRepository.countByEstado(EstadoIncidente.EN_ANALISIS),
                incidenteRepository.countByEstado(EstadoIncidente.RESUELTO),
                sancionRepository.count(),
                inscripcionRepository.count(),
                apelacionRepository.countByEstado(EstadoApelacion.PENDIENTE),
                setupRepository.count(),
                anuncioRepository.count(),
                campeonatoRepository.count(),
                categoriaRepository.count()
        );
    }
}