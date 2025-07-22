package com.vt.createmanagesubmit.servicios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.modelos.TareaProgramada;
import com.vt.createmanagesubmit.repositorios.RepositorioTareasProgramadas;

@Service
public class ServicioTareasProgramadas {

    @Autowired
    private ServicioApi SerApi;

    @Autowired
    private RepositorioTareasProgramadas repoTarea;

    @Autowired
    private Servicio ser;

    @Autowired
    private TareaMoodleService serTareaMoodle;


    public void CrearTarea(
            Long IDCurso,
            String accion,               
            Long plantillaId,
            String nombreCurso,
            String diasCursos,
            String duracion,
            String modalidad,
            String cliente,
            String relator,
            String lugarYfechaEmision,
            LocalDateTime fechaDeEjecucion,
            String lugarSubida
    ) {
        TareaProgramada tarea = new TareaProgramada();

        // Campos del modelo
        tarea.setIDCurso(IDCurso);
        tarea.setAccion(accion);
        tarea.setNombreCurso(nombreCurso);
        tarea.setDiasCursos(diasCursos);
        tarea.setDuracion(duracion);
        tarea.setModalidad(modalidad);
        tarea.setCliente(cliente);
        tarea.setRelator(relator);
        tarea.setLugarYfechaEmision(lugarYfechaEmision);
        tarea.setUbicacionSubida(lugarSubida);
        Plantilla plantilla = ser.plantillaPorId(plantillaId);
        tarea.setPlantilla(plantilla);
        tarea.setFechaEjecucion(fechaDeEjecucion);
        tarea.setEstado("En proceso");

        repoTarea.save(tarea);
    }

    @Transactional
    public void ejecutarTareasPendientes() {
        LocalDateTime ahora = LocalDateTime.now();
        List<TareaProgramada> lista = repoTarea.findByEstadoAndFechaEjecucionLessThanEqual("En proceso", ahora);
        lista.forEach(t -> {
          try {
            serTareaMoodle.procesarTarea(t);
            t.setEstado("Completado");
          } catch (Exception e) {
            t.setEstado("Error");
          }
          repoTarea.save(t);
        });
    }
}
