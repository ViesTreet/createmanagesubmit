package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vt.createmanagesubmit.modelos.AlumnoTemporal;

public interface RepositorioAlumnoTemporal extends JpaRepository<AlumnoTemporal,Long>{

    Page<AlumnoTemporal> findAll(Pageable pageable);

    Optional<AlumnoTemporal> findById(Long id);

    List<AlumnoTemporal> findAllById(Long id);

    List<AlumnoTemporal> findByCursoTemporalId(Long cursoTemporalId); 
}
