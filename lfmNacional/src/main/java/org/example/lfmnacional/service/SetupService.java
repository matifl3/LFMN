package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.dto.setup.SetupRequest;
import org.example.lfmnacional.dto.setup.SetupResponse;
import org.example.lfmnacional.entity.Setup;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.SetupCalificacionRepository;
import org.example.lfmnacional.repository.SetupComentarioRepository;
import org.example.lfmnacional.repository.SetupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;

import org.example.lfmnacional.util.FileUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class SetupService {

    private static final String SUBDIRECTORIO = "setups";
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(".ini", ".acd", ".json", ".rar", ".zip");

    private final SetupRepository setupRepository;
    private final SetupCalificacionRepository setupCalificacionRepository;
    private final SetupComentarioRepository setupComentarioRepository;
    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;

    @Value("${archivos.base-dir}")
    private String baseDir;

    public Setup getEntity(Long id) {
        return setupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setup no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public SetupResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<SetupResponse> listAll(Pageable pageable) {
        return setupRepository.findAll(pageable).map(this::toResponse);
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
    public Page<SetupResponse> buscar(String circuito, String vehiculo, Pageable pageable) {
        boolean hayCircuito = circuito != null && !circuito.isBlank();
        boolean hayVehiculo = vehiculo != null && !vehiculo.isBlank();
        if (hayCircuito && hayVehiculo) {
            return setupRepository.findByCircuitoContainingIgnoreCaseAndVehiculoContainingIgnoreCase(circuito, vehiculo, pageable).map(this::toResponse);
        } else if (hayCircuito) {
            return setupRepository.findByCircuitoContainingIgnoreCase(circuito, pageable).map(this::toResponse);
        } else if (hayVehiculo) {
            return setupRepository.findByVehiculoContainingIgnoreCase(vehiculo, pageable).map(this::toResponse);
        } else {
            return setupRepository.findAll(pageable).map(this::toResponse);
        }
    }

    @Transactional
    public SetupResponse create(SetupRequest request, Usuario autor) {
        Setup setup = Setup.builder()
                .titulo(request.titulo())
                .descripcion(request.descripcion())
                .circuito(request.circuito())
                .vehiculo(request.vehiculo())
                .archivo(request.archivo())
                .autor(autor)
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
        setup.setCategoria(request.categoriaId() != null ? categoriaService.getEntity(request.categoriaId()) : null);
        return toResponse(setupRepository.save(setup));
    }

    @Transactional
    public void delete(Long id) {
        Setup setup = getEntity(id);
        eliminarArchivo(setup);
        setupCalificacionRepository.deleteBySetup_Id(setup.getId());
        setupComentarioRepository.deleteBySetup_Id(setup.getId());
        setupRepository.delete(setup);
    }

    @Transactional
    public void guardarArchivo(Long setupId, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("El archivo no puede estar vacio");
        }
        Setup setup = getEntity(setupId);
        eliminarArchivo(setup);
        String extension = FileUtil.obtenerExtension(archivo.getOriginalFilename()).toLowerCase();
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new BusinessException("Tipo de archivo no permitido. Extensiones aceptadas: .ini, .acd, .json, .rar, .zip");
        }
        String nombreAlmacenado = UUID.randomUUID() + extension;
        Path destino = Paths.get(baseDir, SUBDIRECTORIO, nombreAlmacenado).toAbsolutePath().normalize();
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error al guardar archivo de setup: {}", e.getMessage());
            throw new BusinessException("No se pudo guardar el archivo en el servidor");
        }
        setup.setArchivo(nombreAlmacenado);
        setupRepository.save(setup);
    }

    public Path resolverRuta(Setup setup) {
        if (setup.getArchivo() == null) {
            throw new BusinessException("Este setup no tiene archivo");
        }
        Path base = Paths.get(baseDir, SUBDIRECTORIO).toAbsolutePath().normalize();
        Path resuelta = base.resolve(setup.getArchivo()).normalize();
        if (!resuelta.startsWith(base)) {
            throw new BusinessException("Ruta de archivo no valida");
        }
        if (Files.notExists(resuelta) || Files.isDirectory(resuelta)) {
            throw new ResourceNotFoundException("Archivo de setup no encontrado en el servidor");
        }
        return resuelta;
    }

    private void eliminarArchivo(Setup setup) {
        if (setup.getArchivo() == null) return;
        Path base = Paths.get(baseDir, SUBDIRECTORIO).toAbsolutePath().normalize();
        Path archivo = base.resolve(setup.getArchivo()).normalize();
        if (archivo.startsWith(base) && Files.exists(archivo)) {
            try {
                Files.delete(archivo);
            } catch (IOException e) {
                log.warn("No se pudo eliminar el archivo de setup: {}", e.getMessage());
            }
        }
        setup.setArchivo(null);
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
                setup.getAutor().getNombrePiloto(),
                setup.getAutor().getFotoPerfil(),
                setup.getCategoria() != null ? setup.getCategoria().getId() : null,
                setup.getCategoria() != null ? setup.getCategoria().getNombre() : null,
                setup.getFechaPublicacion(),
                setup.getPromedioCalificacion());
    }
}
