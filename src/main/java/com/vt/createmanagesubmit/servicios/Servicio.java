package com.vt.createmanagesubmit.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;

@Service
public class Servicio {

    @Autowired
    private RepositorioAlumnos repoAlum;

    public Alumno registrarNuevoAlumno(Alumno nuevoAlumno){
        return repoAlum.save(nuevoAlumno);
    }

    public List<Alumno> todosLosAlumnos(){
        return repoAlum.findAll();
    }

    
}


