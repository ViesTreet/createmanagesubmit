package com.vt.createmanagesubmit.repositorios;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vt.createmanagesubmit.modelos.Alumno;

public interface RepositorioAlumnos extends JpaRepository<Alumno, Long>, JpaSpecificationExecutor<Alumno> {

    Page<Alumno> findAll(Pageable pageable);

    Page<Alumno> findByNombreAsistenteContaining(String nombreAsistente, Pageable pageable);

    Page<Alumno> findByRutContaining(String rut, Pageable pageable);

    Page<Alumno> findByEstado(String estado, Pageable pageable);

    Page<Alumno> findByDiploma(String diploma, Pageable pageable);

    Page<Alumno> findByCursoId(Long id, Pageable pageable);
    
    Page<Alumno> findByNumeroCorrelativoInternoContaining(String correlativo, Pageable pageable);

    List<Alumno> findAllByDiploma(String Diploma);

    List<Alumno> findAllByDiplomaAndEstado(String diploma, String estado);

}

