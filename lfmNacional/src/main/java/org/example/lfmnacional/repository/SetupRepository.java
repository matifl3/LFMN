package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Setup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SetupRepository extends JpaRepository<Setup, Long> {

    List<Setup> findByCircuitoContainingIgnoreCaseAndVehiculoContainingIgnoreCase(String circuito, String vehiculo);

    List<Setup> findByCircuitoContainingIgnoreCase(String circuito);

    List<Setup> findByVehiculoContainingIgnoreCase(String vehiculo);

    List<Setup> findByAutor_Id(Long autorId);

    List<Setup> findByCategoria_Id(Long categoriaId);

    boolean existsByCategoria_Id(Long categoriaId);

    Page<Setup> findByCircuitoContainingIgnoreCaseAndVehiculoContainingIgnoreCase(String circuito, String vehiculo, Pageable pageable);

    Page<Setup> findByCircuitoContainingIgnoreCase(String circuito, Pageable pageable);

    Page<Setup> findByVehiculoContainingIgnoreCase(String vehiculo, Pageable pageable);

    Page<Setup> findAll(Pageable pageable);

    @Modifying
    @Query("delete from Setup s where s.autor.id = ?1")
    void deleteByAutor_Id(Long autorId);
}
