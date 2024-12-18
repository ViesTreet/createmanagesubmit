package com.vt.createmanagesubmit.servicios;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;

@Service
public class Servicio {

    @Autowired
    private RepositorioAlumnos repoAlum;

    public Alumno registrarNuevoAlumno(Alumno nuevoAlumno){

        return repoAlum.save(nuevoAlumno);
    }

    /*public void registrarNuevoAlumnoExcel(String rutaExcel){
        ServicioArchivos servicioArchivos = new ServicioArchivos();
        File excelFile = new File(rutaExcel);

        try {
            // Llamar al método procesarExcel
            Map<String, Object> resultado = servicioArchivos.procesarExcel(excelFile);

            // Extraer la lista de alumnos
            List<Alumno> alumnos = (List<Alumno>) resultado.get("alumnos");

            // Imprimir los alumnos
            for (Alumno alumno : alumnos) {
                registrarNuevoAlumno(alumno);
                System.out.println(alumno);
            }

            // Extraer la lista de errores
            List<String> errores = (List<String>) resultado.get("errores");

            // Imprimir los errores
            for (String error : errores) {
                System.out.println(error);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}


