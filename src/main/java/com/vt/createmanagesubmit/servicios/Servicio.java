package com.vt.createmanagesubmit.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<Alumno> todosLosAlumnos(){
        return repoAlum.findAll(PageRequest.of(0, 200, Sort.by("updatedAt").descending()));
    }
 
    public Page<Alumno> buscarAlumnosPorCriterio(String filtro, String dato){
        Page<Alumno> listaResultante;
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("updatedAt").descending());
        
        switch (filtro) {
            case "rut":
                listaResultante = repoAlum.findByRutContaining(dato, pageable); // Usando Containing para búsquedas parciales
                break;
            case "nombreAsistente":
                listaResultante = repoAlum.findByNombreAsistenteContaining(dato, pageable);
                break;
            case "nombreCurso":
                listaResultante = repoAlum.findByNombreCursoContaining(dato, pageable); // Usando Containing
                break;
            case "cliente":
                listaResultante = repoAlum.findByClienteContaining(dato, pageable); // Usando Containing
                break;
            case "obra":
                listaResultante = repoAlum.findByObraContaining(dato, pageable); // Usando Containing
                break;
            case "relator":
                listaResultante = repoAlum.findByRelatorContaining(dato, pageable); // Usando Containing
                break;
            default:
                listaResultante = repoAlum.findAll(PageRequest.of(0, 200, Sort.by("updatedAt").descending()));
                break;
        }
        return listaResultante;
    }
    
}


