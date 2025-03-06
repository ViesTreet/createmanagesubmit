package com.vt.createmanagesubmit.repositorios;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vt.createmanagesubmit.modelos.Alumno;

public interface RepositorioAlumnos extends JpaRepository<Alumno, Long> {

    Page<Alumno> findAll(Pageable pageable);

    Page<Alumno> findByNombreCursoContaining(String nombreCurso, Pageable pageable);

    Page<Alumno> findByNombreAsistenteContaining(String nombreAsistente, Pageable pageable);

    Page<Alumno> findByRutContaining(String rut, Pageable pageable);

    Page<Alumno> findByClienteContaining(String cliente, Pageable pageable);

    Page<Alumno> findByEstado(String estado, Pageable pageable);

    Page<Alumno> findByRelatorContaining(String relator, Pageable pageable);

    Page<Alumno> findByObraContaining(String obra, Pageable pageable);

    Page<Alumno> findByDiploma(String diploma, Pageable pageable);
    
    Page<Alumno> findByNumeroCorrelativoInternoContaining(String correlativo, Pageable pageable);

    List<Alumno> findByPlantillaId(Long plantillaId); 

    List<Alumno> findAllByDiploma(String Diploma);

    List<Alumno> findAllByDiplomaAndEstado(String diploma, String estado);

}

