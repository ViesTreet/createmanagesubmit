package com.vt.createmanagesubmit.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;




@Controller
public class ControladorBase {

    @GetMapping("/")
    public String index() {
        return "index.jsp";
    }
    

    @GetMapping("/add")
    public String subirABaseDeDatos() {
        return "add.jsp";
    }
    

}
