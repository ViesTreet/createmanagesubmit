package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vt.createmanagesubmit.modelos.CursoTemporal;

public interface RepositorioCursoTemporal extends JpaRepository<CursoTemporal,Long>{

    Page<CursoTemporal> findAll(Pageable pageable);

    Optional<CursoTemporal> findById(Long id);

    List<CursoTemporal> findAllById(Long id);

    Page<CursoTemporal> findByNombreCursoContaining(String nombreCurso, Pageable pageable);

    Page<CursoTemporal> findByClienteContaining(String cliente, Pageable pageable);

    Page<CursoTemporal> findByRelatorContaining(String relator, Pageable pageable);

    Page<CursoTemporal> findByIdentificadorContaining(String identificador, Pageable pageable);
}
