package com.vt.createmanagesubmit.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
        return "databaseAlumno.jsp";
    }
    
    @GetMapping("/buscarAlumno")
    public String busquedaAlumno(@RequestParam("filtro") String filtro, @RequestParam("busqueda") String busqueda, Model model) {
        // Pasar los parámetros al modelo para usarlos en el JSP
        model.addAttribute("filtro", filtro);
        model.addAttribute("busqueda", busqueda);
        return "databaseAlumnoBusqueda.jsp";
    }

}
