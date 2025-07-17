package com.vt.createmanagesubmit.repositorios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vt.createmanagesubmit.modelos.TareaProgramada;

public interface RepositorioTareasProgramadas extends JpaRepository<TareaProgramada, Long>{
    
    Page<TareaProgramada> findAll(Pageable pageable);

    Optional<TareaProgramada> findById(Long id);

    List<TareaProgramada> findByEstadoAndFechaEjecucionLessThanEqual(String estado,LocalDateTime fechaEjecucion);
}
