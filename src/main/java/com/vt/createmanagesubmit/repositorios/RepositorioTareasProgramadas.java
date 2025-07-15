package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vt.createmanagesubmit.modelos.TareaProgramada;

public interface RepositorioTareasProgramadas extends JpaRepository<TareaProgramada, Long>{
    
    List<TareaProgramada> findAllByOrderByUpdatedAtDesc();

    Optional<TareaProgramada> findById(Long id);
}
