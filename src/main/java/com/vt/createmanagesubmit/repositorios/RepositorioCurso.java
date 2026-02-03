package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vt.createmanagesubmit.modelos.Curso;



public interface RepositorioCurso extends JpaRepository<Curso,Long>,JpaSpecificationExecutor<Curso>{

    List<Curso> findAll();

    Optional<Curso> findById(Long id);

    List<Curso> findAllByOrderByUpdatedAtDesc();

    List<Curso> findByPlantillaDiplomaId(Long plantillaId);

    List<Curso> findByPlantillaFlyerId(Long plantillaId);

    List<Curso> findByClienteId(Long id);

    List<Curso> findByRelatorId(Long id);
}
