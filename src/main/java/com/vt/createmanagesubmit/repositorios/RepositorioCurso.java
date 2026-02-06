package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vt.createmanagesubmit.modelos.Curso;



public interface RepositorioCurso extends JpaRepository<Curso,Long>,JpaSpecificationExecutor<Curso>{

    Page<Curso> findAll(Pageable pageable);

    Optional<Curso> findById(Long id);

    List<Curso> findAllByOrderByUpdatedAtDesc();

    List<Curso> findTop10ByNombreCursoContainingIgnoreCase(String q);

    List<Curso> findByPlantillaDiplomaId(Long plantillaId);

    List<Curso> findByPlantillaFlyerId(Long plantillaId);

    List<Curso> findByClienteId(Long id);

    List<Curso> findByRelatorId(Long id);
}
