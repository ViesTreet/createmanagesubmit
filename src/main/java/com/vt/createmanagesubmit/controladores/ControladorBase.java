package com.vt.createmanagesubmit.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.vt.createmanagesubmit.servicios.Servicio;




@Controller
public class ControladorBase {

    @Autowired
    private Servicio servicio;

    @GetMapping("/")
    public String index() {
        //servicio.registrarNuevoAlumnoExcel("/home/vt/Documentos/GitHub/createmanagesubmit/src/main/resources/static/Diplomas Año 2018.xls");
        return "index.jsp";
    }
    

    @GetMapping("/add")
    public String subirABaseDeDatos() {
        return "add.jsp";
    }
    

}
