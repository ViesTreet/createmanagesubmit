package com.vt.createmanagesubmit.exceptions;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingNameOrRutException.class)
    public String handleMissingNameOrRutException(MissingNameOrRutException ex,Model model){
        model.addAttribute("error", ex);
        return "addAlumno.jsp";
    }

}
