package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.ArchivoCarrera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchivoCarreraRepository extends JpaRepository<ArchivoCarrera, Long> {

    List<ArchivoCarrera> findByCarrera_Id(Long carreraId);
}
