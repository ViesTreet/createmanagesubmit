package com.vt.createmanagesubmit.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vt.createmanagesubmit.modelos.Jornada;

public interface RepositorioJornada extends JpaRepository<Jornada,Long>,JpaSpecificationExecutor<Jornada>{

}
