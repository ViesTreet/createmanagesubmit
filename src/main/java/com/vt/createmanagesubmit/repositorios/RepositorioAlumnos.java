package com.vt.createmanagesubmit.repositorios;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.vt.createmanagesubmit.modelos.Alumno;

public interface RepositorioAlumnos extends CrudRepository<Alumno, Long>{

    List<Alumno> findAll();

    List<Alumno> findByNombreCurso(String nombreCurso);

    List<Alumno> findByNombreAsistente(String nombreAsistente);

    List<Alumno> findByRut(String rut);

    List<Alumno> findByCliente(String cliente);

    List<Alumno> findByEstado(String estado);

    List<Alumno> findByRelator(String relator);

    List<Alumno> findByObra(String obra);

    List<Alumno> findByPlantillaId(Long plantillaId);

}
