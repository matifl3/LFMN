package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.setup.SetupRequest;
import org.example.lfmnacional.dto.setup.SetupResponse;
import org.example.lfmnacional.entity.Setup;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.SetupCalificacionRepository;
import org.example.lfmnacional.repository.SetupComentarioRepository;
import org.example.lfmnacional.repository.SetupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
public class SetupService {

    private final SetupRepository setupRepository;
    private final SetupCalificacionRepository setupCalificacionRepository;
    private final SetupComentarioRepository setupComentarioRepository;
    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;

    public Setup getEntity(Long id) {
        return setupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setup no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public SetupResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<SetupResponse> listAll() {
        return setupRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SetupResponse> listarPorAutor(Long autorId) {
        return setupRepository.findByAutor_Id(autorId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SetupResponse> listarPorCategoria(Long categoriaId) {
        return setupRepository.findByCategoria_Id(categoriaId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SetupResponse> buscar(String circuito, String vehiculo) {
        List<Setup> resultados;
        boolean hayCircuito = circuito != null && !circuito.isBlank();
        boolean hayVehiculo = vehiculo != null && !vehiculo.isBlank();
        if (hayCircuito && hayVehiculo) {
            resultados = setupRepository.findByCircuitoContainingIgnoreCaseAndVehiculoContainingIgnoreCase(circuito, vehiculo);
        } else if (hayCircuito) {
            resultados = setupRepository.findByCircuitoContainingIgnoreCase(circuito);
        } else if (hayVehiculo) {
            resultados = setupRepository.findByVehiculoContainingIgnoreCase(vehiculo);
        } else {
            resultados = setupRepository.findAll();
        }
        return resultados.stream().map(this::toResponse).toList();
    }

    @Transactional
    public SetupResponse create(SetupRequest request) {
        Setup setup = Setup.builder()
                .titulo(request.titulo())
                .descripcion(request.descripcion())
                .circuito(request.circuito())
                .vehiculo(request.vehiculo())
                .archivo(request.archivo())
                .autor(usuarioService.getEntity(request.autorId()))
                .categoria(request.categoriaId() != null ? categoriaService.getEntity(request.categoriaId()) : null)
                .fechaPublicacion(LocalDateTime.now())
                .build();
        return toResponse(setupRepository.save(setup));
    }

    @Transactional
    public SetupResponse update(Long id, SetupRequest request) {
        Setup setup = getEntity(id);
        setup.setTitulo(request.titulo());
        setup.setDescripcion(request.descripcion());
        setup.setCircuito(request.circuito());
        setup.setVehiculo(request.vehiculo());
        setup.setArchivo(request.archivo());
        setup.setAutor(usuarioService.getEntity(request.autorId()));
        setup.setCategoria(request.categoriaId() != null ? categoriaService.getEntity(request.categoriaId()) : null);
        return toResponse(setupRepository.save(setup));
    }

    @Transactional
    public void delete(Long id) {
        Setup setup = getEntity(id);
        setupCalificacionRepository.deleteBySetup_Id(setup.getId());
        setupComentarioRepository.deleteBySetup_Id(setup.getId());
        setupRepository.delete(setup);
    }

    @Transactional
    public void recalcularPromedio(Setup setup) {
        OptionalDouble promedio = setupCalificacionRepository.findBySetup_Id(setup.getId()).stream()
                .mapToDouble(calificacion -> calificacion.getPuntaje())
                .average();
        setup.setPromedioCalificacion(promedio.isPresent() ? promedio.getAsDouble() : null);
        setupRepository.save(setup);
    }

    private SetupResponse toResponse(Setup setup) {
        return new SetupResponse(
                setup.getId(),
                setup.getTitulo(),
                setup.getDescripcion(),
                setup.getCircuito(),
                setup.getVehiculo(),
                setup.getArchivo(),
                setup.getAutor().getId(),
                setup.getCategoria() != null ? setup.getCategoria().getId() : null,
                setup.getFechaPublicacion(),
                setup.getPromedioCalificacion());
    }
}
