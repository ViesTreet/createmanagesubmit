package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vt.createmanagesubmit.modelos.Curso;


public interface RepositorioCurso extends JpaRepository<Curso,Long>{

    Optional<Curso> findById(Long id);

    List<Curso> findAllByOrderByUpdatedAtDesc();

    List<Curso> findByPlantillaDiplomaId(Long plantillaId);

    List<Curso> findByPlantillaFlyerId(Long plantillaId);

}
