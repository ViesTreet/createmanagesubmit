package com.vt.createmanagesubmit.servicios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vt.createmanagesubmit.modelos.Curso;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.modelos.TareaProgramada;
import com.vt.createmanagesubmit.repositorios.RepositorioTareasProgramadas;

@Service
public class ServicioTareasProgramadas {

    @Autowired
    private RepositorioTareasProgramadas repoTarea;

    @Autowired
    private Servicio ser;

    @Autowired
    private TareaMoodleService serTareaMoodle;


    public void CrearTarea(
            Long IDCurso,
            String accion,               
            LocalDateTime fechaDeEjecucion,
            Curso curso
    ) {
        TareaProgramada tarea = new TareaProgramada();

        // Campos del modelo
        tarea.setIDCurso(IDCurso);
        tarea.setAccion(accion);
        tarea.setFechaEjecucion(fechaDeEjecucion);
        tarea.setEstado("En proceso");
        tarea.setCurso(curso);

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
