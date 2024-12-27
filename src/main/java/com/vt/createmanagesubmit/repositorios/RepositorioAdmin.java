package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vt.createmanagesubmit.modelos.Admin;

public interface RepositorioAdmin extends JpaRepository<Admin,Long>{

    List<Admin> findAll();

    Optional<Admin> findByCorreo(String correo);
}
