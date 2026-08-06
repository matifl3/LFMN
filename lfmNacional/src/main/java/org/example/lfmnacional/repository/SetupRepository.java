package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Setup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetupRepository extends JpaRepository<Setup, Long> {

    List<Setup> findByCircuitoContainingIgnoreCaseAndVehiculoContainingIgnoreCase(String circuito, String vehiculo);

    List<Setup> findByCircuitoContainingIgnoreCase(String circuito);

    List<Setup> findByVehiculoContainingIgnoreCase(String vehiculo);

    List<Setup> findByAutor_Id(Long autorId);

    List<Setup> findByCategoria_Id(Long categoriaId);
}
