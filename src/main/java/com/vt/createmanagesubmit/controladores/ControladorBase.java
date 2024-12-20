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
        
        return "index.jsp";
    }
    
    @GetMapping("/home")
    public String home() {
        return "home.jsp";
    }
    
    @GetMapping("/add")
    public String subirABaseDeDatos() {
        return "add.jsp";
    }

    @GetMapping("/dataBase")
    public String baseDeDatos() {
        return "database.jsp";
    }
    
    

}
