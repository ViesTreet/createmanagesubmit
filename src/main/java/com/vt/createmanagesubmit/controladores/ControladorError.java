package com.vt.createmanagesubmit.controladores;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ControladorError implements ErrorController{

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == 404) {
                model.addAttribute("message", "Página no encontrada.");
                return "error404"; // Página específica para errores 404
            } else if (statusCode == 500) {
                model.addAttribute("message", "Error interno del servidor.");
                return "error500"; // Página específica para errores 500
            }
        }
        model.addAttribute("message", "Ocurrió un error inesperado.");
        return "error"; // Página genérica de error
    }

}
