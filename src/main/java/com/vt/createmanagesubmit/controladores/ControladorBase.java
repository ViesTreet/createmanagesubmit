package com.vt.createmanagesubmit.controladores;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
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
import com.vt.createmanagesubmit.servicios.ServicioGenerarCertificado;
import com.vt.createmanagesubmit.servicios.ServicioTareasProgramadas;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestBody;











@Controller
public class ControladorBase {

    @Autowired
    @Lazy
    private Servicio servicio;

    @Autowired
    private ServicioArchivos servicioAr;

    @Autowired
    private ServicioApi servicioApi;

    @Autowired
    @Lazy
    private ServicioGenerarCertificado servicioGenerarCertificado;

    @Autowired
    private ServicioTareasProgramadas servicioTareaP;

    String correoEmpresa = Servicio.CORREO_EMPRESA;

    //-----------------------Acciones comunes-----------------------------
    @GetMapping("/")
    public String index() {
        if(servicio.adminPorCorreo("admin@admin.com")==null){
            servicio.registrarAdmin("admin@admin.com", "admin", "RcOqkObsJN");
        }
        if(!servicio.plantillaPorNombre("Error en encontrar plantilla").isPresent()){
            Plantilla nuevaPlantilla = new Plantilla();
            nuevaPlantilla.setNombreCertificado("Error en encontrar plantilla");
            servicio.guardarPlantilla(nuevaPlantilla);
        }
        return "index";
    }

    @PostMapping("/login")
    public String login(HttpSession session,@RequestParam("correo")String Correo, @RequestParam("contrasena")String password) {
        Admin admin = servicio.passwordConfirmacion(Correo, password);
        if(admin != null && admin.getUbicacion() != null){
            session.setAttribute("usuarioEnSesion", admin);
            return "redirect:/home";
        }else if(admin != null && admin.getUbicacion() == null){
            session.setAttribute("usuarioEnSesion", admin);
            return "redirect:/ubicacion";
        }else{
            return "redirect:/";
        }
        
    }
    
    @GetMapping("/ubicacion")
    public String ubicacion(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin",usuarioTemporal);
        return "ubicacion";
    }
    
    @PostMapping("/actualizarUbicacion")
    public String actualizarUbicacion(@RequestParam("ubi")String ubicacion, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        usuarioTemporal.setUbicacion(ubicacion);
        servicio.guardarAdmin(usuarioTemporal);
        return "redirect:/home";
    }
    

    @GetMapping("/home")
    public String home(HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin", usuarioTemporal);
        return "home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    

    //-------------------------------------------------------------------------
    
    //--------------------------Alumnos----------------------------------------
    @GetMapping("/dataBaseAlumno")
    public String baseDeDatos(HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin", usuarioTemporal);
        return "databaseAlumno";
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
        model.addAttribute("admin", usuarioTemporal);
        return "databaseAlumnoBusqueda";
    }

    @GetMapping("/dataBaseAlumno/alumno/{id}")
    public String alumnoDatos(@PathVariable("id")Long id,Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        Alumno alumno = servicio.alumnoPorId(id);
        model.addAttribute("admin", usuarioTemporal);
        if(alumno != null){
            model.addAttribute("alumno",alumno);
            return "alumnoDatos";
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
        model.addAttribute("admin", usuarioTemporal);
        List<Plantilla> plantillas=servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        return "addAlumno";
    }

    @PostMapping("/dataBaseAlumno/agregarAlumno")
    public String agregarAlumno(@RequestParam(name = "nombreAsistente") String nombreAsistente,@RequestParam("curso")String curso,@RequestParam(name = "diasCursos") String diasCursos,@RequestParam(name = "numeroHoras") String numeroHoras,@RequestParam(name = "cliente") String cliente,@RequestParam(name = "identificador") String identificador,@RequestParam(name = "codigo") String codigo,@RequestParam(name = "notaAprobacion") String notaAprobacion,@RequestParam(name = "relator") String relator,@RequestParam(name = "asistencia") String asistencia,@RequestParam(name = "estado") String estado,@RequestParam(name = "diploma") String diploma,@RequestParam(value="modalidad") String modalidad,@RequestParam(name = "rut") String rut,@RequestParam(name = "correo") String correo,@RequestParam(name = "plantilla",required = false) Long plantilla,@RequestParam(value = "rutificador", defaultValue = "false") boolean rutificador,@RequestParam("lugarYfechaEmision")String lugarYfechaEmision,Model model,HttpSession session) {
    Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin", usuarioTemporal);
        Alumno nuevoAlumno = new Alumno();
        nuevoAlumno.setAsistencia(asistencia);
        nuevoAlumno.setCliente(cliente);
        nuevoAlumno.setCodigo(codigo);
        nuevoAlumno.setCorreo(correo);
        nuevoAlumno.setDiasCursos(diasCursos);
        nuevoAlumno.setEstado(estado);
        nuevoAlumno.setNombreCurso(curso);
        nuevoAlumno.setNotaAprobacion(notaAprobacion);
        nuevoAlumno.setDuracion(numeroHoras);
        nuevoAlumno.setModalidad(modalidad);
        nuevoAlumno.setIdentificador(identificador);
        nuevoAlumno.setRelator(relator);
        nuevoAlumno.setRut(rut);
        nuevoAlumno.setLugarYfechaEmision(lugarYfechaEmision);
        nuevoAlumno.setUbicacionSubida(usuarioTemporal.getUbicacion());
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
                    return "addAlumno";
                }
            }
        }else{
            if(!nombreAsistente.trim().isEmpty()&&nombreAsistente != null){
                nuevoAlumno.setNombreAsistente(nombreAsistente);
            }else{
                model.addAttribute("error", "El nombre no fue ingresado.");
                return "addAlumno";
            }
        }

        try {
            Plantilla plantillausuario = servicio.plantillaPorId(plantilla);
            nuevoAlumno.setPlantilla(plantillausuario);
        } catch (MissingTemplateException ex) {
            model.addAttribute("error", ex.getMessage());
            return "addAlumno";
        }

        try {
            nuevoAlumno=servicio.comprobarYGuardar(nuevoAlumno,diploma);
        if(diploma.equals("enviar")){
            if(nuevoAlumno.getEstado().equals("aprobado")){
                try {
                    servicioGenerarCertificado.generateCertificateForAlumno(nuevoAlumno);
                    nuevoAlumno.setDiploma("enviado");
                } catch (Exception ex) {
                    model.addAttribute("error", ex.getMessage());
                    return "addAlumno";
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
            return "addAlumno";
        }
    
    }

    
    @GetMapping("/dataBaseAlumno/addAlumnoBase/excel")
    public String addAlumnoExcel(Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin", usuarioTemporal);
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        return "addAlumnoExcel";
    }

    @PostMapping(value = "/dataBaseAlumno/uploadAlumnoExcel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String subirExcel(@RequestPart("file") MultipartFile file, @RequestParam(value = "estadoDiplomaExcel", required = false) String estadoDiplomaExcel, @RequestParam(value = "plantillaNombre") String plantilla, @RequestParam(value = "estadoExcel") String estadoExcel,@RequestParam(value="rutificador")String rutificador,HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            if (file.isEmpty()) {
                List<Plantilla> plantillas = servicio.todasLasPlantillas();
                model.addAttribute("plantillas",plantillas);
                model.addAttribute("error", "El excel esta vacio.");
            }
            try {
                // Leer el contenido del archivo en un arreglo de bytes
                byte[] fileBytes = file.getBytes();

                // Llamar al método asíncrono y pasarle los bytes del archivo
                servicioAr.leerExcelYGuardarEnBD(fileBytes, estadoDiplomaExcel, plantilla, estadoExcel, rutificador, usuarioTemporal.getUbicacion());

                // Redirigir inmediatamente sin esperar a que termine el procesamiento
                return "redirect:/dataBaseAlumno";
            } catch (IOException ex) {
                List<Plantilla> plantillas = servicio.todasLasPlantillas();
                model.addAttribute("plantillas",plantillas);
                model.addAttribute("error", ex.getMessage());
                return "addAlumnoExcel";
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
        model.addAttribute("admin", usuarioTemporal);
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        Alumno alumno = servicio.alumnoPorId(id);
        model.addAttribute("alumno",alumno);
        model.addAttribute("plantillas",plantillas);
        return "editarAlumno";
    }
    
    @PostMapping("/dataBaseAlumno/editarAlumno")
    public String editarAlumno(@RequestParam("id") Long id,@RequestParam("nombreAsistente") String nombreAsistente,@RequestParam("nombreCurso") String nombreCurso,@RequestParam("diasCursos") String diasCursos,@RequestParam("numeroHoras") String numeroHoras,@RequestParam("cliente") String cliente,@RequestParam("identificador") String identificador,@RequestParam("codigo") String codigo,@RequestParam("notaAprobacion") String notaAprobacion,@RequestParam("relator") String relator,@RequestParam("modalidad") String modalidad,@RequestParam("asistencia") String asistencia,@RequestParam("estado") String estado,@RequestParam("diploma") String diploma,@RequestParam("rut") String rut,@RequestParam("correo") String correo,@RequestParam("plantilla") Long plantillaId,@RequestParam("lugarYfechaEmision")String lugarYfechaEmision,Model model,HttpSession session) {
        
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";  
        }
        model.addAttribute("admin", usuarioTemporal);
        try {
            nombreAsistente = servicioApi.formatearNombre(nombreAsistente);
            servicio.editarAlumno(id, nombreAsistente, nombreCurso, diasCursos, numeroHoras, cliente, identificador, codigo, notaAprobacion,relator, asistencia, estado, diploma, rut, modalidad, correo, plantillaId, lugarYfechaEmision);
            return "redirect:/dataBaseAlumno/alumno/"+id;  

        } catch (MissingTemplateException | MissingAlumnoIdException | MissingNameOrRutException ex) {
            List<Plantilla> plantillas = servicio.todasLasPlantillas();
            Alumno alumno = servicio.alumnoPorId(id);
            model.addAttribute("alumno",alumno);
            model.addAttribute("plantillas",plantillas);
            model.addAttribute("error", ex.getMessage());
            return "editarAlumno";
        }
    }

    @GetMapping("/dataBaseAlumno/alumno/{id}/borrar")
    public String borrarAlumnoId(@PathVariable("id")Long id,HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        model.addAttribute("admin", usuarioTemporal);
	    if (usuarioTemporal != null) {
            servicio.borrarAlumnoPorId(id);
            return "redirect:/dataBaseAlumno";
        }
        return "redirect:/";
    }

    @GetMapping("/dataBaseAlumno/download")
    public void downloadDataBaseAlumno(HttpServletResponse response, HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            throw new IllegalStateException("No autorizado"); // Manejar el caso de no autenticado
        }
        model.addAttribute("admin", usuarioTemporal);
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
            model.addAttribute("admin", usuarioTemporal);
            try {
                servicioAr.generateCertificatesById(id);
                return "redirect:/dataBaseAlumno/alumno/"+id;
            } catch (Exception ex) {
                Alumno alumno = servicio.alumnoPorId(id);
                model.addAttribute("alumno",alumno);
                model.addAttribute("error", ex.getMessage());
                return "alumnoDatos";
            }
        }
        return "redirect:/";
    }
    
    @GetMapping("/dataBaseAlumno/enviarRestantes")
    public String enviarRestantes(HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            try {
                servicioAr.generateCertificatesAll();
                return "redirect:/dataBaseAlumno";
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Ocurrió un error al mandar los restantes.");
                return "databaseAlumno";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/programarCertificadoMoodle")
    public String moodleTarea(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin", usuarioTemporal);
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        List<Map<String, Object>> cursos=servicioApi.obtenerCursosMoodle();
        model.addAttribute("cursosMoodle",cursos);
        return"moodleProgramado";
    }

    @PostMapping("/programarCertificadoMoodle/crear")
    public String crearMoodleTarea(
            HttpSession session,
            @RequestParam(name = "cursoMoodle", required = true) String cursoMoodleParam,
            @RequestParam(name = "accion", required = true) String accion,
            @RequestParam(name = "plantilla",   required = true) String plantillaParam,
            @RequestParam(name = "curso",       required = false) String nombreCurso,
            @RequestParam(name = "diasCursos",  required = false) String diasCursos,
            @RequestParam(name = "numeroHoras", required = false) String duracion,
            @RequestParam(name = "modalidad",   required = false) String modalidad,
            @RequestParam(name = "cliente",     required = false) String cliente,
            @RequestParam(name = "relator",     required = false) String relator,
            @RequestParam(name = "lugarYfechaEmision", required = false) String lugarYfechaEmision,
            @RequestParam(name = "fechaDeEjecucion",   required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDeEjecucion
    ) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }

        // 2) Parseo de IDs (vacío → null)
        Long idCurso   = (cursoMoodleParam == null || cursoMoodleParam.isBlank())
                         ? null
                         : Long.valueOf(cursoMoodleParam);
        Long idPlantilla = (plantillaParam == null || plantillaParam.isBlank())
                         ? null
                         : Long.valueOf(plantillaParam);

        if (idCurso == null || idPlantilla == null || accion.isBlank()){
            return "redirect:/programarCertificadoMoodle";
        }
        // 3) Strings: cadenas vacías o en blanco → null
        nombreCurso         = (nombreCurso == null || nombreCurso.isBlank()) 
                              ? null : nombreCurso;
        diasCursos          = (diasCursos == null || diasCursos.isBlank())
                              ? null : diasCursos;
        duracion            = (duracion == null || duracion.isBlank())
                              ? null : duracion;
        modalidad           = (modalidad == null || modalidad.isBlank())
                              ? null : modalidad;
        cliente             = (cliente == null || cliente.isBlank())
                              ? null : cliente;
        relator             = (relator == null || relator.isBlank())
                              ? null : relator;
        lugarYfechaEmision  = (lugarYfechaEmision == null || lugarYfechaEmision.isBlank())
                              ? null : lugarYfechaEmision;

        String lugarSubida = usuarioTemporal.getUbicacion();
        servicioTareaP.CrearTarea(
            idCurso,
            accion,
            idPlantilla,
            nombreCurso,
            diasCursos,
            duracion,
            modalidad,
            cliente,
            relator,
            lugarYfechaEmision,
            fechaDeEjecucion,
            lugarSubida
        );

        return "redirect:/programarCertificadoMoodle";
    }

    
    

    //----------------------------------------------------------------------------------

    //----------------------------------Plantilla---------------------------------------
    @GetMapping("/dataBasePlantilla/buscarPlantilla")
    public String busquedaPlantilla(@RequestParam("busqueda")String busqueda,Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin", usuarioTemporal);
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        model.addAttribute("busqueda",busqueda);
        return "databasePlantillaBusqueda";
    }
    
    
    @GetMapping("/dataBasePlantilla")
    public String dataBasePlantilla(Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin", usuarioTemporal);
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        return "databasePlantilla";
    }

    @PostMapping("/dataBasePlantilla/nuevaPlantilla")
    public String crearNuevaPlantilla(@RequestParam String nombreCertificado,@RequestParam String descripcion,@RequestParam String asistenciaMin,@RequestParam String notaMin,@RequestParam(required = false) MultipartFile pathArchivo,@RequestParam(required = false) String pathArchivoS,@RequestParam(defaultValue = "false") boolean clonarPlantilla,HttpSession session,Model model) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            Optional<Plantilla> optPlantilla = servicio.plantillaPorNombre(nombreCertificado);
            if(optPlantilla.isPresent()){
                model.addAttribute("error", "El nombre de la plantilla tiene que ser único, no se puede repetir.");
                List<Plantilla> plantillas = servicio.todasLasPlantillas();
                model.addAttribute("plantillas",plantillas);
                return "databasePlantilla";
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
                return "databasePlantilla";
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
        if (!usuarioTemporal.getCorreo().equals("admin@admin.com")){
            return "redirect:/dataBasePlantilla";
        }
        model.addAttribute("admin", usuarioTemporal);
        Plantilla plantilla = servicio.plantillaPorId(id);
        model.addAttribute("plantilla",plantilla);
        return "borrarPlantilla";
    }

    @GetMapping("/dataBasePlantilla/borrar/{id}")
    public String borrarPlantillaId(@PathVariable("id")Long id,HttpSession session,Model model){
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            if (!usuarioTemporal.getCorreo().equals("admin@admin.com")){
                return "redirect:/dataBasePlantilla";
            }
            model.addAttribute("admin", usuarioTemporal);
            try {
                servicio.borrarPlantillaPorId(id);
                return "redirect:/dataBasePlantilla";
            } catch (IOException ex) {  
                model.addAttribute("error", ex);
                List<Plantilla> plantillas = servicio.todasLasPlantillas();
                model.addAttribute("plantillas",plantillas);
                return "databasePlantilla";
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
        model.addAttribute("admin", usuarioTemporal);
        Plantilla plantilla = servicio.plantillaPorId(id);
        model.addAttribute("plantilla",plantilla);
        return "editarPlantilla";
    }

    @PostMapping("/dataBasePlantilla/editarPlantilla")
    public String editarPlantilla(@RequestParam("id") Long id,@RequestParam(value = "cambiarPlantilla", required = false) boolean cambiarPlantilla,@RequestParam(value = "pathArchivo", required = false) MultipartFile nuevaPlantilla,@RequestParam(value = "nombreCertificado")String nombre,@RequestParam(value = "descripcion")String descripcion,@RequestParam(value = "asistenciaMin")String asistencia,@RequestParam(value = "notaMin")String nota,HttpSession session,Model model) {  
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {   
            model.addAttribute("admin", usuarioTemporal);
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
                return "editarPlantilla";

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
                return "editarPlantilla";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/dataBasePlantilla/plantilla/{id}/probar")
    public String probarPlantilla(@PathVariable("id")Long id,Model model,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin", usuarioTemporal);
        Plantilla plantilla = servicio.plantillaPorId(id);
        model.addAttribute("plantilla", plantilla);
        return "probarPlantilla";
    }

    @GetMapping("/dataBasePlantilla/download")
    public void descargarPlantilla(HttpServletResponse response, HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            throw new IllegalStateException("No autorizado"); // Manejar el caso de no autenticado
        }
        model.addAttribute("admin", usuarioTemporal);
        try {
            servicioAr.exportToExcel(response);
        } catch (Exception e) {
            e.printStackTrace();
            // Maneja el error aquí, por ejemplo, escribe un mensaje de error en el response.
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }    



    //-----------------------------------------------------------------------

    //-------------------------Admins----------------------------------------
    @GetMapping("/dataBaseAdmin")
    public String dataBaseAdministrador(HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin", usuarioTemporal);
        return "databaseAdmin";
    }

    @GetMapping("/dataBaseAdmin/{id}/borrar")
    public String borrarAdmin(@PathVariable("id")Long id,HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            try {
                if(usuarioTemporal.getCorreo().equals("admin@admin.com")){
                    servicio.borrarAdminPorId(id);
                }
                return "redirect:/dataBaseAdmin";
            } catch (MissingAdminIdException ex) {
                model.addAttribute("error", ex.getMessage());
                return "databaseAdmin";
            }
            
        }
        return "redirect:/";
    }

    @PostMapping("/dataBaseAdmin/nuevoAdmin")
    public String crearNuevoAdmin(@RequestParam(value = "correo")String correo,@RequestParam(value = "nombre")String nombre,@RequestParam(value = "contrasena")String password,HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            if(servicio.adminPorCorreo(correo)==null){
                servicio.registrarAdmin(correo,nombre,password);
                return "redirect:/dataBaseAdmin";
            }else{
                model.addAttribute("error", "El correo de los administradores no se pueden repetir");
                return "databaseAdmin";
            }
        }
        return "redirect:/";
    }
    
    //----------------------Otros-------------------------------------
    @GetMapping("/generarCertificadoQr/{id}")
    public String getMethodName(@PathVariable("id") String idEncriptada, HttpServletResponse response, Model model) {
        model.addAttribute("id", idEncriptada);
        return "generarCertificadoQr";
    }
    

    @GetMapping("/documentacion")
    public ResponseEntity<byte[]> getPdf() throws IOException {
        ClassPathResource pdfFile = new ClassPathResource("/static/documentacion/MANUAL E-VOLUTION APP.pdf");
        byte[] pdfBytes = pdfFile.getInputStream().readAllBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=MANUAL E-VOLUTION APP.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
