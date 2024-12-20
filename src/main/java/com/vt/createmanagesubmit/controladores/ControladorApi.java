package com.vt.createmanagesubmit.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.servicios.Servicio;

@RestController
@RequestMapping("/api")
public class ControladorApi {

    @Autowired
    private Servicio ser;

    @GetMapping("/datos")
    public List<Alumno> getDatos() {
        return ser.todosLosAlumnos();
    }
}

