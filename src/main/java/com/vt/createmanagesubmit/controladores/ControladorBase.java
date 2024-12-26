package com.vt.createmanagesubmit.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    
    @GetMapping("/dataBaseAlumno/buscarAlumno")
    public String busquedaAlumno(@RequestParam("filtro") String filtro, @RequestParam("busqueda") String busqueda, Model model) {
        // Pasar los parámetros al modelo para usarlos en el JSP
        model.addAttribute("filtro", filtro);
        model.addAttribute("busqueda", busqueda);
        return "databaseAlumnoBusqueda.jsp";
    }

    @GetMapping("/dataBasePlantilla/buscarPlantilla")
    public String busquedaPlantilla(@RequestParam("busqueda")String busqueda,Model model) {
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        model.addAttribute("busqueda",busqueda);
        return "databasePlantillaBusqueda.jsp";
    }
    

    @GetMapping("/dataBaseAlumno/alumno/{id}")
    public String alumnoDatos(@PathVariable("id")Long id,Model model) {
        Alumno alumno = servicio.alumnoPorId(id);
        model.addAttribute("alumno",alumno);
        return "alumnoDatos.jsp";
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
                servicioAr.generateCertificateForAlumno(nuevoAlumno);
                nuevoAlumno.setDiploma("enviado");
            } catch (Exception e) {
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
    public String addAlumnoExcel(Model model) {
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        return "addAlumnoExcel.jsp";
    }

    @GetMapping("/dataBaseAlumno/alumno/{id}/editar")
    public String editarAlumno(@PathVariable("id")Long id,Model model) {
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        Alumno alumno = servicio.alumnoPorId(id);
        model.addAttribute("alumno",alumno);
        model.addAttribute("plantillas",plantillas);
        return "editarAlumno.jsp";
    }
    
    @PostMapping("/editarAlumno")
    public String editarAlumno(
            @RequestParam("id") Long id,
            @RequestParam("nombreAsistente") String nombreAsistente,
            @RequestParam("nombreCurso") String nombreCurso,
            @RequestParam("diasCursos") String diasCursos,
            @RequestParam("numeroHoras") String numeroHoras,
            @RequestParam("numeroCorrelativoInterno") String numeroCorrelativoInterno,
            @RequestParam("cliente") String cliente,
            @RequestParam("obra") String obra,
            @RequestParam("codigo") String codigo,
            @RequestParam("notaAprovacion") String notaAprovacion,
            @RequestParam("relator") String relator,
            @RequestParam("asistencia") String asistencia,
            @RequestParam("estado") String estado,
            @RequestParam("diploma") String diploma,
            @RequestParam("rut") String rut,
            @RequestParam("correo") String correo,
            @RequestParam("plantilla") Long plantillaId,
            Model model) {
        
        // Llamar al servicio para procesar los datos
        servicio.editarAlumno(
                id, nombreAsistente, nombreCurso, diasCursos, numeroHoras, 
                numeroCorrelativoInterno, cliente, obra, codigo, notaAprovacion,
                relator, asistencia, estado, diploma, rut, correo, plantillaId);

        // Redirigir o mostrar una página de confirmación
        return "redirect:/dataBaseAlumno/alumno/"+id;
    }
    
    @GetMapping("/dataBasePlantilla")
    public String dataBasePlantilla(Model model) {
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        return "databasePlantilla.jsp";
    }

    @GetMapping("/dataBasePlantilla/plantilla/{id}/borrar")
    public String borrarPlantilla(@PathVariable("id")Long id,Model model) {
        Plantilla plantilla = servicio.plantillaPorId(id);
        model.addAttribute("plantilla",plantilla);
        return "borrarPlantilla.jsp";
    }

    @GetMapping("/dataBasePlantilla/plantilla/{id}/editar")
    public String editarPlantilla(@PathVariable("id")Long id,Model model) {
        Plantilla plantilla = servicio.plantillaPorId(id);
        model.addAttribute("plantilla",plantilla);
        return "editarPlantilla.jsp";
    }
    
    
    
}
