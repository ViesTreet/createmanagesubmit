package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.vt.createmanagesubmit.modelos.Plantilla;

public interface RepositorioPlantillas extends JpaRepository<Plantilla, Long>{

    List<Plantilla> findAll();

    List<Plantilla> findAllById(Long id);

    Optional<Plantilla> findByNombreCertificado(String nombre);

    List<Plantilla> findAllByNombreCertificadoContaining(String nombre);
}    
