package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vt.createmanagesubmit.modelos.Cliente;

public interface RepositorioCliente extends JpaRepository<Cliente,Long>,JpaSpecificationExecutor<Cliente>{

    Optional<Cliente> findById(long id);
    List<Cliente> findAll();

    Optional<Cliente> findByNombreClienteIgnoreCase(String nombre);

    // Para combos o listados simples
    List<Cliente> findAllByOrderByNombreClienteAsc();

    List<Cliente> findAllByOrderByUpdatedAtDesc();
}
