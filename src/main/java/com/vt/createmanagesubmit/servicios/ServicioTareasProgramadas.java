package com.vt.createmanagesubmit.servicios;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.modelos.TareaProgramada;
import com.vt.createmanagesubmit.repositorios.RepositorioTareasProgramadas;

@Service
public class ServicioTareasProgramadas {

    @Autowired
    private ServicioApi SerApi;

    @Autowired
    private RepositorioTareasProgramadas repoTarea;


    public void CrearTarea(
            Long IDCurso,
            Long IDQuiz,               
            Long plantillaId,
            String nombreCurso,
            String diasCursos,
            String duracion,
            String modalidad,
            String cliente,
            String relator,
            String lugarYfechaEmision,
            LocalDate fechaDeEjecucion,
            String lugarSubida
    ) {
        TareaProgramada tarea = new TareaProgramada();

        // Campos del modelo
        tarea.setIDCurso(IDCurso);

        // TODO: buscar/setear el IDQuiz correcto
        // tarea.setIDQuiz(IDQuiz);

        tarea.setNombreCurso(nombreCurso);
        tarea.setDiasCursos(diasCursos);
        tarea.setDuracion(duracion);
        tarea.setModalidad(modalidad);
        tarea.setCliente(cliente);
        tarea.setRelator(relator);
        tarea.setLugarYfechaEmision(lugarYfechaEmision);
        tarea.setUbicacionSubida(lugarSubida);

        // Asignar la plantilla (solo con el ID)
        if (plantillaId != null) {
            Plantilla plantilla = new Plantilla();
            plantilla.setId(plantillaId);
            tarea.setPlantilla(plantilla);
            // Si necesitas más datos de la plantilla, usa plantillaRepository.findById(...)
        }

        // Fecha de ejecución → de LocalDate a LocalDateTime a medianoche
        if (fechaDeEjecucion != null) {
            tarea.setFechaEjecucion(fechaDeEjecucion.atStartOfDay());
        }
    }
}
