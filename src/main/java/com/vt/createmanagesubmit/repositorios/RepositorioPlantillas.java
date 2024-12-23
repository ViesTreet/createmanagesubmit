package com.vt.createmanagesubmit.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import com.vt.createmanagesubmit.modelos.Plantilla;

public interface RepositorioPlantillas extends JpaRepository<Plantilla, Long>{

    List<Plantilla> findAll();

    List<Plantilla> findAllById(Long id);
}    
