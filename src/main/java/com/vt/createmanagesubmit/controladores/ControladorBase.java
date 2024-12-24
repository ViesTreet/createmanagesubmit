package com.vt.createmanagesubmit.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.servicios.Servicio;
import com.vt.createmanagesubmit.servicios.ServicioArchivos;








@Controller
public class ControladorBase {

    @Autowired
    @Lazy
    private Servicio servicio;

    @Autowired
    private ServicioArchivos servicioAr;

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

    @GetMapping("/dataBaseAlumno")
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

    @GetMapping("/addAlumnoBase")
    public String AgregarAlumno(Model model) {
        List<Plantilla> plantillas=servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        return "addAlumno.jsp";
    }

    @PostMapping("/agregarAlumno")
public String agregarAlumno(
    @RequestParam(name = "nombreAsistente") String nombreAsistente,
    @RequestParam("curso")String curso,
    @RequestParam(name = "diasCursos") String diasCursos,
    @RequestParam(name = "numeroHoras") String numeroHoras,
    @RequestParam(name = "numeroCorrelativoInterno") String numeroCorrelativoInterno,
    @RequestParam(name = "cliente") String cliente,
    @RequestParam(name = "obra") String obra,
    @RequestParam(name = "codigo") String codigo,
    @RequestParam(name = "notaAprovacion") String notaAprovacion,
    @RequestParam(name = "relator") String relator,
    @RequestParam(name = "asistencia") String asistencia,
    @RequestParam(name = "estado") String estado,
    @RequestParam(name = "diploma") String diploma,
    @RequestParam(name = "rut") String rut,
    @RequestParam(name = "correo") String correo,
    @RequestParam(name = "plantilla") Long plantilla,
    Model model
) {
    Alumno nuevoAlumno = new Alumno();
    nuevoAlumno.setAsistencia(asistencia);
    nuevoAlumno.setCliente(cliente);
    nuevoAlumno.setCodigo(codigo);
    nuevoAlumno.setCorreo(correo);
    nuevoAlumno.setDiasCursos(diasCursos);
    nuevoAlumno.setEstado(estado);
    nuevoAlumno.setNombreAsistente(nombreAsistente);
    nuevoAlumno.setNombreCurso(curso);
    nuevoAlumno.setNotaAprovacion(notaAprovacion);
    nuevoAlumno.setNumeroCorrelativoInterno(numeroCorrelativoInterno);
    nuevoAlumno.setNumeroHoras(numeroHoras);
    nuevoAlumno.setObra(obra);
    nuevoAlumno.setRelator(relator);
    nuevoAlumno.setRut(rut);
    Plantilla plantillausuario = servicio.plantillaPorId(plantilla);
    nuevoAlumno.setPlantilla(plantillausuario);

    nuevoAlumno=servicio.comprobarYGuardar(nuevoAlumno,diploma);
    if(diploma.equals("enviar")){
        if(nuevoAlumno.getEstado().equals("aprobado")){
            try {
                System.out.println("llegamos");
                servicioAr.generateCertificateForAlumno(nuevoAlumno);
                nuevoAlumno.setDiploma("enviado");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            nuevoAlumno.setDiploma("noEnviado");
        }
    }else{
        nuevoAlumno.setDiploma("noEnviado");
    }
    servicio.registrarNuevoAlumno(nuevoAlumno);
    return "redirect:/addAlumnoBase";
    }

    
    @GetMapping("/addAlumnoBase/excel")
    public String getMethodName(Model model) {
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        return "addAlumnoExcel.jsp";
    }
    

}
