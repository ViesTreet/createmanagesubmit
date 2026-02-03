package com.vt.createmanagesubmit.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vt.createmanagesubmit.modelos.Cliente;

public interface RepositorioCliente extends JpaRepository<Cliente,Long>,JpaSpecificationExecutor<Cliente>{

    List<Cliente> findAll();

    Optional<Cliente> findByNombreClienteIgnoreCase(String nombre);

    // Para combos o listados simples
    List<Cliente> findAllByOrderByNombreClienteAsc();

    List<Cliente> findAllByOrderByUpdatedAtDesc();

    List<Cliente> findTop10ByNombreClienteContainingIgnoreCase(String q);

    Optional<Cliente> findByIdentificador(String identificador);
}
