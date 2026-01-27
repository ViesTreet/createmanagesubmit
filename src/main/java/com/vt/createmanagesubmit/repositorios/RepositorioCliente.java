package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vt.createmanagesubmit.modelos.Cliente;

public interface RepositorioCliente extends JpaRepository<Cliente,Long>{

    Optional<Cliente> findById(long id);
    List<Cliente> findAll();
}
