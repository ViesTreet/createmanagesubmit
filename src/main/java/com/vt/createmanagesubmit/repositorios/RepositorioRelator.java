package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vt.createmanagesubmit.modelos.Relator;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RepositorioRelator
        extends JpaRepository<Relator, Long>,
                JpaSpecificationExecutor<Relator> {


    List<Relator> findTop10ByNombreContainingIgnoreCaseOrderByNombreDesc(String nombre);

    // Exacto (cuando seleccionas uno existente)
    Optional<Relator> findByNombreIgnoreCase(String nombre);

    // Para combos o listados simples
    List<Relator> findAllByOrderByNombreAsc();

    List<Relator> findAllByOrderByUpdatedAtDesc();


}
