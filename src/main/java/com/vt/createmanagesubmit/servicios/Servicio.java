package com.vt.createmanagesubmit.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;
import com.vt.createmanagesubmit.repositorios.RepositorioPlantillas;

@Service
public class Servicio {

    @Autowired
    private RepositorioAlumnos repoAlum;

    @Autowired
    private RepositorioPlantillas repoPlanti;

    String correoEmpresa = "example@example.com";

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
    
    public List<Plantilla> todasLasPlantillas(){
        return repoPlanti.findAll();
    }

    public Plantilla plantillaPorId(Long id){
        return repoPlanti.findById(id).orElse(null);
    }

    public void comprobarYGuardar(Alumno nuevoAlumno){ //correo,nota aprobacion,asistencia, horas arreglar
        if(!nuevoAlumno.getNombreAsistente().trim().isBlank()||nuevoAlumno.getNombreAsistente()!=null){
            if(nuevoAlumno.getEstado().trim().equals("auto")||nuevoAlumno.getEstado().trim()=="auto"){
                if(!nuevoAlumno.getNotaAprovacion().trim().isBlank() && nuevoAlumno.getAsistencia().trim().isBlank()){
                    float nota = Float.parseFloat(nuevoAlumno.getNotaAprovacion());
                    if(nota>=nuevoAlumno.getPlantilla().getNotaMin()){
                        nuevoAlumno.setEstado("aprobado");
                    }
                }
                else if(!nuevoAlumno.getAsistencia().trim().isBlank() && nuevoAlumno.getNotaAprovacion().trim().isBlank()){
                    int asistenciaAlumno = Integer.parseInt(nuevoAlumno.getAsistencia());
                    if (asistenciaAlumno >= nuevoAlumno.getPlantilla().getAsistenciaMin()){
                        nuevoAlumno.setEstado("aprobado");
                    }
                }

                else if(!nuevoAlumno.getNotaAprovacion().trim().isBlank() && !nuevoAlumno.getAsistencia().trim().isBlank()){
                    float nota = Float.parseFloat(nuevoAlumno.getNotaAprovacion());
                    int asistenciaAlumno = Integer.parseInt(nuevoAlumno.getAsistencia());
                    if(asistenciaAlumno >= nuevoAlumno.getPlantilla().getAsistenciaMin() && nota>=nuevoAlumno.getPlantilla().getNotaMin()){
                        nuevoAlumno.setEstado("aprobado");
                    }
                }
                else{
                    nuevoAlumno.setEstado("revisionManual");
                }
            }

            if(nuevoAlumno.getCorreo().trim().isEmpty()){
                nuevoAlumno.setCorreo(correoEmpresa);
            }

            String[] fechaPartes = nuevoAlumno.getDiasCursos().trim().split("-");
            String fechaFormateada = "entre el "+fechaPartes[0]+" al "+fechaPartes[1];
            nuevoAlumno.setDiasCursos(fechaFormateada);

            nuevoAlumno.setNumeroHoras(nuevoAlumno.getNumeroHoras()+" horas");

            repoAlum.save(nuevoAlumno);
        }
    }
}


