package com.vt.createmanagesubmit.controladores;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vt.createmanagesubmit.dto.AlumnoDTO;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.servicios.Servicio;

@RestController
@RequestMapping("/api")
public class ControladorApi {

    @Autowired
    private Servicio ser;

    @GetMapping("/datos")
    public List<AlumnoDTO> getDatos() {
        List<Alumno> alumnos = ser.todosLosAlumnos();
        return alumnos.stream().map(AlumnoDTO::new).collect(Collectors.toList());
    }
}

