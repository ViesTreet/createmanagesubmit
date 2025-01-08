package com.vt.createmanagesubmit.controladores;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.vt.createmanagesubmit.exceptions.MissingAdminIdException;
import com.vt.createmanagesubmit.exceptions.MissingAlumnoIdException;
import com.vt.createmanagesubmit.exceptions.MissingNameOrRutException;
import com.vt.createmanagesubmit.exceptions.MissingTemplateException;
import com.vt.createmanagesubmit.modelos.Admin;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.servicios.Servicio;
import com.vt.createmanagesubmit.servicios.ServicioApi;
import com.vt.createmanagesubmit.servicios.ServicioArchivos;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;









@Controller
public class ControladorBase {

    @Autowired
    @Lazy
    private Servicio servicio;

    @Autowired
    private ServicioArchivos servicioAr;

    @Autowired
    private ServicioApi servicioApi;

    //-----------------------Acciones comunes-----------------------------
    @GetMapping("/")
    public String index() {
        if(servicio.adminPorCorreo("admin@admin.com")==null){
            servicio.registrarAdmin("admin@admin.com", "admin", "qwerty");
        }
        if(!servicio.plantillaPorNombre("Error en encontrar plantilla").isPresent()){
            Plantilla nuevaPlantilla = new Plantilla();
            nuevaPlantilla.setNombreCertificado("Error en encontrar plantilla");
            servicio.guardarPlantilla(nuevaPlantilla);
        }
        return "index.jsp";
    }

    @PostMapping("/login")
    public String login(HttpSession session,@RequestParam("correo")String Correo, @RequestParam("contrasena")String password) {
        Admin admin = servicio.passwordConfirmacion(Correo, password);
        if(admin != null){
            session.setAttribute("usuarioEnSesion", admin);
            return "redirect:/home";
        }else{
            return "redirect:/";
        }
        
    }
    
    
    @GetMapping("/home")
    public String home(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        return "home.jsp";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    

    //-------------------------------------------------------------------------
    
    //--------------------------Alumnos----------------------------------------
    @GetMapping("/dataBaseAlumno")
    public String baseDeDatos(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        return "databaseAlumno.jsp";
    }
    
    @GetMapping("/dataBaseAlumno/buscarAlumno")
    public String busquedaAlumno(@RequestParam("filtro") String filtro, @RequestParam("busqueda") String busqueda, Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        // Pasar los parámetros al modelo para usarlos en el JSP
        model.addAttribute("filtro", filtro);
        model.addAttribute("busqueda", busqueda);
        return "databaseAlumnoBusqueda.jsp";
    }

    @GetMapping("/dataBaseAlumno/alumno/{id}")
    public String alumnoDatos(@PathVariable("id")Long id,Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        Alumno alumno = servicio.alumnoPorId(id);
        if(alumno != null){
            model.addAttribute("alumno",alumno);
            return "alumnoDatos.jsp";
        }else{
            return "redirect:/dataBaseAlumno";
        }
    }
    

    @GetMapping("/dataBaseAlumno/addAlumnoBase")
    public String AgregarAlumno(Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        List<Plantilla> plantillas=servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        return "addAlumno.jsp";
    }

    @PostMapping("/dataBaseAlumno/agregarAlumno")
    public String agregarAlumno(@RequestParam(name = "nombreAsistente") String nombreAsistente,@RequestParam("curso")String curso,@RequestParam(name = "diasCursos") String diasCursos,@RequestParam(name = "numeroHoras") String numeroHoras,@RequestParam(name = "numeroCorrelativoInterno") String numeroCorrelativoInterno,@RequestParam(name = "cliente") String cliente,@RequestParam(name = "obra") String obra,@RequestParam(name = "codigo") String codigo,@RequestParam(name = "notaAprovacion") String notaAprovacion,@RequestParam(name = "relator") String relator,@RequestParam(name = "asistencia") String asistencia,@RequestParam(name = "estado") String estado,@RequestParam(name = "diploma") String diploma,@RequestParam(name = "rut") String rut,@RequestParam(name = "correo") String correo,@RequestParam(name = "plantilla",required = false) Long plantilla,@RequestParam(value = "rutificador", defaultValue = "false") boolean rutificador,Model model,HttpSession session) {
    Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
    Alumno nuevoAlumno = new Alumno();
    nuevoAlumno.setAsistencia(asistencia);
    nuevoAlumno.setCliente(cliente);
    nuevoAlumno.setCodigo(codigo);
    nuevoAlumno.setCorreo(correo);
    nuevoAlumno.setDiasCursos(diasCursos);
    nuevoAlumno.setEstado(estado);
    nuevoAlumno.setNombreCurso(curso);
    nuevoAlumno.setNotaAprovacion(notaAprovacion);
    nuevoAlumno.setNumeroCorrelativoInterno(numeroCorrelativoInterno);
    nuevoAlumno.setNumeroHoras(numeroHoras);
    nuevoAlumno.setObra(obra);
    nuevoAlumno.setRelator(relator);
    nuevoAlumno.setRut(rut);
    List<Plantilla> plantillas=servicio.todasLasPlantillas();
    model.addAttribute("plantillas",plantillas);
    nombreAsistente = servicioApi.formatearNombre(nombreAsistente);
    if(rutificador && !rut.trim().isEmpty() && rut != null){
        String nombreRutificado = servicioApi.obtenerNombrePorRut(rut);
        if(!nombreRutificado.trim().equals("nombreNoEncontrado")){
            nuevoAlumno.setNombreAsistente(nombreRutificado);
        }else{
            if(!nombreAsistente.trim().isEmpty()&&nombreAsistente != null){
                nuevoAlumno.setNombreAsistente(nombreAsistente);
            }else{
                model.addAttribute("error", "El nombre no pudo ser encontrado.");
                return "addAlumno.jsp";
            }
        }
    }else{
        if(!nombreAsistente.trim().isEmpty()&&nombreAsistente != null){
            nuevoAlumno.setNombreAsistente(nombreAsistente);
        }else{
            model.addAttribute("error", "El nombre no fue ingresado.");
            return "addAlumno.jsp";
        }
    }
    
    try {
        Plantilla plantillausuario = servicio.plantillaPorId(plantilla);
        nuevoAlumno.setPlantilla(plantillausuario);
    } catch (MissingTemplateException ex) {
        model.addAttribute("error", ex.getMessage());
        return "addAlumno.jsp";
    }

    try {
        nuevoAlumno=servicio.comprobarYGuardar(nuevoAlumno,diploma);
    if(diploma.equals("enviar")){
        if(nuevoAlumno.getEstado().equals("aprobado")){
            try {
                servicioAr.generateCertificateForAlumno(nuevoAlumno);
                nuevoAlumno.setDiploma("enviado");
            } catch (Exception ex) {
                model.addAttribute("error", ex.getMessage());
                return "addAlumno.jsp";
            }
        }else{
            nuevoAlumno.setDiploma("noEnviado");
        }
    }else{
        nuevoAlumno.setDiploma("noEnviado");
    }
    servicio.registrarNuevoAlumno(nuevoAlumno);
    return "redirect:/dataBaseAlumno/addAlumnoBase";
    } catch (MissingNameOrRutException ex) {
        model.addAttribute("error", ex.getMessage());
        return "addAlumno.jsp";
    }
    
    }

    
    @GetMapping("/dataBaseAlumno/addAlumnoBase/excel")
    public String addAlumnoExcel(Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        return "addAlumnoExcel.jsp";
    }

    @PostMapping(value = "/dataBaseAlumno/uploadAlumnoExcel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String subirExcel(@RequestPart("file") MultipartFile file, @RequestParam(value = "estadoDiplomaExcel", required = false) String estadoDiplomaExcel, @RequestParam(value = "plantillaNombre") String plantilla, @RequestParam(value = "estadoExcel") String estadoExcel,@RequestParam(value="rutificador")String rutificador,HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            if (file.isEmpty()) {
                List<Plantilla> plantillas = servicio.todasLasPlantillas();
                model.addAttribute("plantillas",plantillas);
                model.addAttribute("error", "El excel esta vacio.");
            }
            try {
                // Leer el contenido del archivo en un arreglo de bytes
                byte[] fileBytes = file.getBytes();

                // Llamar al método asíncrono y pasarle los bytes del archivo
                servicioAr.leerExcelYGuardarEnBD(fileBytes, estadoDiplomaExcel, plantilla, estadoExcel, rutificador);

                // Redirigir inmediatamente sin esperar a que termine el procesamiento
                return "redirect:/dataBaseAlumno";
            } catch (IOException ex) {
                List<Plantilla> plantillas = servicio.todasLasPlantillas();
                model.addAttribute("plantillas",plantillas);
                model.addAttribute("error", ex.getMessage());
                return "addAlumnoExcel.jsp";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/dataBaseAlumno/alumno/{id}/editar")
    public String editarAlumno(@PathVariable("id")Long id,Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        Alumno alumno = servicio.alumnoPorId(id);
        model.addAttribute("alumno",alumno);
        model.addAttribute("plantillas",plantillas);
        return "editarAlumno.jsp";
    }
    
    @PostMapping("/dataBaseAlumno/editarAlumno")
    public String editarAlumno(@RequestParam("id") Long id,@RequestParam("nombreAsistente") String nombreAsistente,@RequestParam("nombreCurso") String nombreCurso,@RequestParam("diasCursos") String diasCursos,@RequestParam("numeroHoras") String numeroHoras,@RequestParam("numeroCorrelativoInterno") String numeroCorrelativoInterno,@RequestParam("cliente") String cliente,@RequestParam("obra") String obra,@RequestParam("codigo") String codigo,@RequestParam("notaAprovacion") String notaAprovacion,@RequestParam("relator") String relator,@RequestParam("asistencia") String asistencia,@RequestParam("estado") String estado,@RequestParam("diploma") String diploma,@RequestParam("rut") String rut,@RequestParam("correo") String correo,@RequestParam("plantilla") Long plantillaId,Model model,HttpSession session) {
        
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";  
        }
        try {
            nombreAsistente = servicioApi.formatearNombre(nombreAsistente);
            servicio.editarAlumno(id, nombreAsistente, nombreCurso, diasCursos, numeroHoras, numeroCorrelativoInterno, cliente, obra, codigo, notaAprovacion,relator, asistencia, estado, diploma, rut, correo, plantillaId);
            return "redirect:/dataBaseAlumno/alumno/"+id;  

        } catch (MissingTemplateException | MissingAlumnoIdException | MissingNameOrRutException ex) {
            List<Plantilla> plantillas = servicio.todasLasPlantillas();
            Alumno alumno = servicio.alumnoPorId(id);
            model.addAttribute("alumno",alumno);
            model.addAttribute("plantillas",plantillas);
            model.addAttribute("error", ex.getMessage());
            return "editarAlumno.jsp";
        }
    }

    @GetMapping("/dataBaseAlumno/alumno/{id}/borrar")
    public String borrarAlumnoId(@PathVariable("id")Long id,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            servicio.borrarAlumnoPorId(id);
            return "redirect:/dataBaseAlumno";
        }
        return "redirect:/";
    }

    @GetMapping("/dataBaseAlumno/download")
    public void downloadDataBaseAlumno(HttpServletResponse response, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            throw new IllegalStateException("No autorizado"); // Manejar el caso de no autenticado
        }
        try {
            servicioAr.exportToExcel(response);
        } catch (Exception e) {
            e.printStackTrace();
            // Maneja el error aquí, por ejemplo, escribe un mensaje de error en el response.
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }    

    @GetMapping("/dataBaseAlumno/generateCertificado/{id}")
    public String certificadoPorId(@PathVariable("id")Long id,HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            try {
                servicioAr.generateCertificatesById(id);
                return "redirect:/dataBaseAlumno/alumno/"+id;
            } catch (Exception ex) {
                Alumno alumno = servicio.alumnoPorId(id);
                model.addAttribute("alumno",alumno);
                model.addAttribute("error", ex.getMessage());
                return "alumnoDatos.jsp";
            }
        }
        return "redirect:/";
    }
    
    @GetMapping("/dataBaseAlumno/enviarRestantes")
    public String enviarRestantes(HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            try {
                servicioAr.generateCertificatesAll();
                return "redirect:/dataBaseAlumno";
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Ocurrió un error al mandar los restantes.");
                return "databaseAlumno.jsp";
            }
        }
        return "redirect:/";
    }

    //----------------------------------------------------------------------------------

    //----------------------------------Plantilla---------------------------------------
    @GetMapping("/dataBasePlantilla/buscarPlantilla")
    public String busquedaPlantilla(@RequestParam("busqueda")String busqueda,Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        model.addAttribute("busqueda",busqueda);
        return "databasePlantillaBusqueda.jsp";
    }
    
    
    @GetMapping("/dataBasePlantilla")
    public String dataBasePlantilla(Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        return "databasePlantilla.jsp";
    }

    @PostMapping("/dataBasePlantilla/nuevaPlantilla")
    public String crearNuevaPlantilla(@RequestParam String nombreCertificado,@RequestParam String descripcion,@RequestParam String asistenciaMin,@RequestParam String notaMin,@RequestParam(required = false) MultipartFile pathLogo,@RequestParam(required = false) MultipartFile pathArchivo,@RequestParam(required = false) String pathLogoS,@RequestParam(required = false) String pathArchivoS,@RequestParam(defaultValue = "false") boolean clonarLogo,@RequestParam(defaultValue = "false") boolean clonarPlantilla,HttpSession session,Model model) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            Optional<Plantilla> optPlantilla = servicio.plantillaPorNombre(nombreCertificado);
            if(optPlantilla.isPresent()){
                model.addAttribute("error", "El nombre de la plantilla tiene que ser único, no se puede repetir.");
                List<Plantilla> plantillas = servicio.todasLasPlantillas();
                model.addAttribute("plantillas",plantillas);
                return "databasePlantilla.jsp";
            }
            try {
                Plantilla nuevaPlantilla = new Plantilla();
                nuevaPlantilla.setNombreCertificado(nombreCertificado);
                nuevaPlantilla.setDescripcion(descripcion);
                if(asistenciaMin.isBlank()){
                    nuevaPlantilla.setAsistenciaMin(0);
                }else{
                    nuevaPlantilla.setAsistenciaMin(Integer.parseInt(asistenciaMin));
                }

                if(notaMin.isBlank()){
                    nuevaPlantilla.setNotaMin(0);
                }else{
                    nuevaPlantilla.setNotaMin(Float.parseFloat(notaMin));
                }

                // Manejar logo
                if (clonarLogo) {
                    if (pathLogoS != null && !pathLogoS.isEmpty()) {
                        nuevaPlantilla.setPathLogo(servicio.clonarArchivo(pathLogoS, "/logos/"));
                    } else {
                        nuevaPlantilla.setPathLogo(null);
                    }
                } else if (pathLogo != null && !pathLogo.isEmpty()) {
                    nuevaPlantilla.setPathLogo(servicio.guardarArchivo(pathLogo, "/logos/"));
                } else {
                    nuevaPlantilla.setPathLogo(null);
                }

                // Manejar plantilla
                if (clonarPlantilla) {
                    if (pathArchivoS != null && !pathArchivoS.isEmpty()) {
                        nuevaPlantilla.setPathArchivo(servicio.clonarArchivo(pathArchivoS, "/plantillas/"));
                    } else {
                        throw new IllegalArgumentException("Debe proporcionar una plantilla existente si desea clonar.");
                    }
                } else if (pathArchivo != null && !pathArchivo.isEmpty()) {
                    nuevaPlantilla.setPathArchivo(servicio.guardarArchivo(pathArchivo, "/plantillas/"));
                } else {
                    throw new IllegalArgumentException("Debe proporcionar una plantilla válida para guardar.");
                }

                
                servicio.guardarPlantilla(nuevaPlantilla);
                return "redirect:/dataBasePlantilla"; 


            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Ocurrió un error al guardar la nueva plantilla");
                List<Plantilla> plantillas = servicio.todasLasPlantillas();
                model.addAttribute("plantillas",plantillas);
                return "databasePlantilla.jsp";
            }
            
        }
        return "redirect:/";
    }

    @GetMapping("/dataBasePlantilla/plantilla/{id}/borrar")
    public String borrarPlantilla(@PathVariable("id")Long id,Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        Plantilla plantilla = servicio.plantillaPorId(id);
        model.addAttribute("plantilla",plantilla);
        return "borrarPlantilla.jsp";
    }

    @GetMapping("/dataBasePlantilla/borrar/{id}")
    public String borrarPlantillaId(@PathVariable("id")Long id,HttpSession session,Model model){
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            try {
                servicio.borrarPlantillaPorId(id);
                return "redirect:/dataBasePlantilla";
            } catch (IOException ex) {  
                model.addAttribute("error", ex);
                List<Plantilla> plantillas = servicio.todasLasPlantillas();
                model.addAttribute("plantillas",plantillas);
                return "databasePlantilla.jsp";
            }
            
        }
        return "redirect:/";
    }

    @GetMapping("/dataBasePlantilla/plantilla/{id}/editar")
    public String editarPlantilla(@PathVariable("id")Long id,Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        Plantilla plantilla = servicio.plantillaPorId(id);
        model.addAttribute("plantilla",plantilla);
        return "editarPlantilla.jsp";
    }

    @PostMapping("/dataBasePlantilla/editarPlantilla")
    public String editarPlantilla(@RequestParam("id") Long id,@RequestParam(value = "cambiarLogo", required = false) boolean cambiarLogo,@RequestParam(value = "cambiarPlantilla", required = false) boolean cambiarPlantilla,@RequestParam(value = "pathLogo", required = false) MultipartFile nuevoLogo,@RequestParam(value = "pathArchivo", required = false) MultipartFile nuevaPlantilla,@RequestParam(value = "nombreCertificado")String nombre,@RequestParam(value = "descripcion")String descripcion,@RequestParam(value = "asistenciaMin")String asistencia,@RequestParam(value = "notaMin")String nota ,HttpSession session,Model model) {  
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {   
            Plantilla plantilla = servicio.plantillaPorId(id);
            String nombreAntiguo = plantilla.getNombreCertificado();
            boolean error;
            if(nombreAntiguo.equals(nombre)){
                error = false;
            }else{
                Optional<Plantilla> optPlantilla = servicio.plantillaPorNombre(nombre);
                if(optPlantilla.isPresent()){
                    error = true;
                }else{
                    error = false;
                }
            }
            if(nombre.trim().isEmpty()){
                error=true;
            }
            if(error){
                model.addAttribute("error", "El nombre tiene que ser unicó y no puede estar vacio");
                Plantilla plantillaError = servicio.plantillaPorId(id);
                model.addAttribute("plantilla",plantillaError);
                return "editarPlantilla.jsp";

            }
            plantilla.setNombreCertificado(nombre);
            plantilla.setDescripcion(descripcion);
            if(asistencia.isBlank()){
                plantilla.setAsistenciaMin(0);
            }else{
                plantilla.setAsistenciaMin(Integer.parseInt(asistencia));
            }

            if(nota.isBlank()){
                plantilla.setNotaMin(0);
            }else{
                plantilla.setNotaMin(Float.parseFloat(nota));
            }

            servicio.guardarPlantilla(plantilla);   
            try {
                if (cambiarLogo && nuevoLogo != null && !nuevoLogo.isEmpty()) {
                    Path logoPathAntiguo = Paths.get(plantilla.getPathLogo());
                    servicio.cambiarLogo(id, nuevoLogo);
                    Files.deleteIfExists(logoPathAntiguo);
                }
                if (cambiarPlantilla && nuevaPlantilla != null && !nuevaPlantilla.isEmpty()) {
                    Path plantillaPathAntiguo = Paths.get(plantilla.getPathArchivo());
                    servicio.cambiarPlantilla(id, nuevaPlantilla);
                    Files.deleteIfExists(plantillaPathAntiguo);
                }
                return "redirect:/dataBasePlantilla"; 
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Error al guardar la plantilla.");
                Plantilla plantillaError = servicio.plantillaPorId(id);
                model.addAttribute("plantilla",plantillaError);
                return "editarPlantilla.jsp";
            }
        }
        return "redirect:/";
    }

    //-----------------------------------------------------------------------

    //-------------------------Admins----------------------------------------
    @GetMapping("/dataBaseAdmin")
    public String dataBaseAdministrador(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        return "databaseAdmin.jsp";
    }

    @GetMapping("/dataBaseAdmin/{id}/borrar")
    public String borrarAdmin(@PathVariable("id")Long id,HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            try {
                servicio.borrarAdminPorId(id);
                return "redirect:/dataBaseAdmin";
            } catch (MissingAdminIdException ex) {
                model.addAttribute("error", ex.getMessage());
                return "databaseAdmin.jsp";
            }
            
        }
        return "redirect:/";
    }

    @PostMapping("/dataBaseAdmin/nuevoAdmin")
    public String crearNuevoAdmin(@RequestParam(value = "correo")String correo,@RequestParam(value = "nombre")String nombre,@RequestParam(value = "contrasena")String password,HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            if(servicio.adminPorCorreo(correo)==null){
                servicio.registrarAdmin(correo,nombre,password);
                return "redirect:/dataBaseAdmin";
            }else{
                model.addAttribute("error", "El correo de los administradores no se pueden repetir");
                return "databaseAdmin.jsp";
            }
        }
        return "redirect:/";
    }
    
    //----------------------Otros-------------------------------------
    @GetMapping("/generarCertificadoQr/{id}")
    public String getMethodName(@PathVariable("id") String idEncriptada, HttpServletResponse response, Model model) {
        model.addAttribute("id", idEncriptada);
        return "generarCertificadoQr.jsp";
    }
    
    
    @GetMapping("/generar/{id}")
    public CompletableFuture<ResponseEntity<String>> generarQR(@PathVariable("id") String idEncriptada, HttpServletResponse response) {
        try {
            return servicioAr.generateCertificateQR(idEncriptada, response).thenApply(result -> ResponseEntity.ok("Certificado generado correctamente"));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }
}
