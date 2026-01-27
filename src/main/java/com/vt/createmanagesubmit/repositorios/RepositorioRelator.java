package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vt.createmanagesubmit.modelos.Relator;

public interface RepositorioRelator extends JpaRepository<Relator,Long>{

    List<Relator> findTop10ByNombreContainingIgnoreCaseOrderByNombreDesc(String nombre);

    List<Relator> findByNombre(String nombre);

    Optional<Relator> findById(Long id);

}
