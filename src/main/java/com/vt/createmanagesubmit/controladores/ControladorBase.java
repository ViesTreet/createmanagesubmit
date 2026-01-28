package com.vt.createmanagesubmit.controladores;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.vt.createmanagesubmit.modelos.Cliente;
import com.vt.createmanagesubmit.modelos.Curso;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.modelos.Relator;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;
import com.vt.createmanagesubmit.repositorios.RepositorioRelator;
import com.vt.createmanagesubmit.servicios.Servicio;
import com.vt.createmanagesubmit.servicios.ServicioApi;
import com.vt.createmanagesubmit.servicios.ServicioArchivos;
import com.vt.createmanagesubmit.servicios.ServicioGenerarCertificado;
import com.vt.createmanagesubmit.servicios.ServicioTareasProgramadas;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@Controller
public class ControladorBase {

    private final RepositorioAlumnos repositorioAlumnos;

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

    @Autowired
    private RepositorioRelator repoRelator;

    @Value("${DIP_MAIL}")
    String correoEmpresa;

    ControladorBase(RepositorioAlumnos repositorioAlumnos) {
        this.repositorioAlumnos = repositorioAlumnos;
    }

    //-----------------------Acciones comunes-----------------------------
    @GetMapping("/")
    public String index() {
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
    public String agregarAlumno(
            @RequestParam String nombreAsistente,
            @RequestParam(required = true) Long cursoId,
            @RequestParam(required = false) String notaAprobacion,
            @RequestParam(required = false) String asistencia,
            @RequestParam String estado,
            @RequestParam String diploma,
            @RequestParam String rut,
            @RequestParam String correo,
            @RequestParam(defaultValue = "false") boolean rutificador,
            @RequestParam(required = false) Long cursoID,
            @RequestParam(required = false) Long relatorID,
            HttpSession session, Model model
    ) {

        Admin admin = (Admin) session.getAttribute("usuarioEnSesion");
        if (admin == null) {
            return "redirect:/";  
        }

        Alumno nuevoAlumno = new Alumno();
        nuevoAlumno.setAsistencia(asistencia);
        nuevoAlumno.setCorreo(correo);
        nuevoAlumno.setEstado(estado);
        nuevoAlumno.setNotaAprobacion(notaAprobacion);
        nuevoAlumno.setRut(rut);
        nuevoAlumno.setDiploma(diploma);
        Curso curso = servicio.cursoPorId(cursoID);
        nuevoAlumno.setCurso(curso);
        // -------------------------
        // Nombre / Rutificador
        // -------------------------
        nombreAsistente = servicioApi.formatearNombre(nombreAsistente);

        if (rutificador && rut != null && !rut.trim().isEmpty()) {
            String nombreRutificado = servicioApi.obtenerNombrePorRut(rut);
            if (!"nombreNoEncontrado".equals(nombreRutificado)) {
                nuevoAlumno.setNombreAsistente(nombreRutificado);
            } else if (nombreAsistente != null && !nombreAsistente.trim().isEmpty()) {
                nuevoAlumno.setNombreAsistente(nombreAsistente);
            } else {
                model.addAttribute("error", "El nombre no pudo ser encontrado.");
                return "addAlumno";
            }
        } else {
            if (nombreAsistente != null && !nombreAsistente.trim().isEmpty()) {
                nuevoAlumno.setNombreAsistente(nombreAsistente);
            } else {
                model.addAttribute("error", "El nombre esta vacio");
                return "addAlumno";
            }
        }

        try {
            nuevoAlumno = servicio.comprobarYGuardar(nuevoAlumno, diploma);

            if ("enviar".equalsIgnoreCase(diploma)
                    && "aprobado".equalsIgnoreCase(nuevoAlumno.getEstado())) {
                servicioGenerarCertificado.generateCertificateForAlumno(nuevoAlumno);
                nuevoAlumno.setDiploma("enviado");
            } else {
                nuevoAlumno.setDiploma("noEnviado");
            }

            servicio.registrarNuevoAlumno(nuevoAlumno);

            return "redirect/dataBaseAlumno/addAlumnoBase";

        } catch (Exception ex) {
            model.addAttribute("error", ex);
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
    public String subirExcel(@RequestPart("file") MultipartFile file, @RequestParam(value = "estadoDiplomaExcel", required = false) String estadoDiplomaExcel, @RequestParam(value = "cursoId") Long cursoId, @RequestParam(value = "estadoExcel") String estadoExcel,@RequestParam(value="rutificador")String rutificador,HttpSession session,Model model) {
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

                Curso curso = servicio.cursoPorId(cursoId);
                servicioAr.leerExcelYGuardarEnBD(fileBytes, estadoDiplomaExcel, estadoExcel, rutificador, curso);

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
    public String editarAlumno(@RequestParam("id") Long id,@RequestParam("nombreAsistente") String nombreAsistente,@RequestParam("nombreCurso") String nombreCurso,@RequestParam("diasCursos") String diasCursos,@RequestParam("numeroHoras") String numeroHoras,@RequestParam("cliente") String cliente,@RequestParam("identificador") String identificador,@RequestParam("notaAprobacion") String notaAprobacion,@RequestParam("relator") String relator,@RequestParam("modalidad") String modalidad,@RequestParam("asistencia") String asistencia,@RequestParam("estado") String estado,@RequestParam("diploma") String diploma,@RequestParam("rut") String rut,@RequestParam("correo") String correo,@RequestParam("plantilla") Long plantillaId,@RequestParam("lugarYfechaEmision")String lugarYfechaEmision,Model model,HttpSession session) {
        
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";  
        }
        model.addAttribute("admin", usuarioTemporal);
        try {
            nombreAsistente = servicioApi.formatearNombre(nombreAsistente);
            servicio.editarAlumno(id, nombreAsistente, nombreCurso, diasCursos, numeroHoras, cliente, identificador, notaAprobacion,relator, asistencia, estado, diploma, rut, modalidad, correo, plantillaId, lugarYfechaEmision);
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
            @RequestParam(name = "cursoId", required = true) Long cursoId,
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
    

        if (idCurso == null || accion.isBlank()){
            return "redirect:/programarCertificadoMoodle";
        }
        Curso curso = servicio.cursoPorId(idCurso);
        servicioTareaP.CrearTarea(
            idCurso,
            accion,
            fechaDeEjecucion,
            curso
        );

        return "redirect:/programarCertificadoMoodle";
    }

    @GetMapping("/programarCertificadoMoodleManual")
    public String moodleManual(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin", usuarioTemporal);
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas",plantillas);
        List<Map<String, Object>> cursos=servicioApi.obtenerCursosMoodle();
        model.addAttribute("cursosMoodle",cursos);
        return "moodleManual";
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
    public String crearNuevaPlantilla(@RequestParam String nombreCertificado,@RequestParam String descripcion,@RequestParam(required = false) MultipartFile pathArchivo,@RequestParam(required = false) String pathArchivoS,@RequestParam(defaultValue = "false") boolean clonarPlantilla,HttpSession session,Model model) {

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
    public String editarPlantilla(@RequestParam("id") Long id,@RequestParam(value = "cambiarPlantilla", required = false) boolean cambiarPlantilla,@RequestParam(value = "pathArchivo", required = false) MultipartFile nuevaPlantilla,@RequestParam(value = "nombreCertificado")String nombre,@RequestParam(value = "descripcion")String descripcion,HttpSession session,Model model) {  
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
    public String crearNuevoAdmin(@RequestParam(value = "correo")String correo,@RequestParam(value = "nombre")String nombre,@RequestParam(value = "contrasena")String password,@RequestParam(value = "rol")String rol,HttpSession session,Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            if(servicio.adminPorCorreo(correo)==null){
                servicio.registrarAdmin(correo,nombre,password,rol);
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
        Alumno alumnoError = new Alumno();
        Curso cursoError = new Curso();
        cursoError.setNombreCurso("No válido");
        alumnoError.setNombreAsistente("No válido");
        alumnoError.setNumeroCorrelativoInterno("No válido");
        try {
            Alumno alumno;
            Long idAlumno = servicioGenerarCertificado.decryptStudentId(idEncriptada);
            alumno = servicio.alumnoPorId(idAlumno);
            if(alumno != null){
                model.addAttribute("alumno",alumno);
                model.addAttribute("curso",alumno.getCurso().getNombreCurso());
                model.addAttribute("val","Válido");
                model.addAttribute("idEnc",idEncriptada);
            }else{
                model.addAttribute("alumno",alumnoError);
                model.addAttribute("curso",cursoError.getNombreCurso());
                model.addAttribute("val","No válido");
            }
        } catch (Exception e) {
            model.addAttribute("alumno",alumnoError);
            model.addAttribute("curso",cursoError.getNombreCurso());
            model.addAttribute("val","No válido");
            e.printStackTrace();
        }
        return "generarCertificadoQr";
    }

    @GetMapping("/seccionAsistencia")
    public String generarQrDeAsistencia(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            // Cargar plantillas para el select inicial opcional (aunque el frontend también pide /api/plantillas)
            model.addAttribute("plantillas", servicio.todasLasPlantillas());
            model.addAttribute("admin", usuarioTemporal);
            return "databaseCursoTemporales"; 
        }
        return "redirect:/";
    }

    @GetMapping("/seccionAsistencia/revision/{id}")
    public String alumnoTemporalPorCurso(@PathVariable("id")String id,HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            Long idR=Long.valueOf(id);
            Curso cursoTemporal = servicio.cursoPorId(idR);
            model.addAttribute("idCurso",id);
            model.addAttribute("cursoTemporal",cursoTemporal);
            model.addAttribute("admin", usuarioTemporal);
            return "databaseAlumnoTemporal";
        }
        return "redirect:/";
        
    }
    

    @GetMapping("/marcarAsistenciaCurso/{id}")
    public String marcarAsistenciaCurso(HttpSession session, Model model,@PathVariable("id") String idEncriptada) {
        model.addAttribute("id",idEncriptada);
        return "asistenciaParaCertificados"; 
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

    //-----------------------------------------------------------------------

    //-------------------------OneDrive---------------------------------------

    @GetMapping("/subirOneDrive")
    public String subirArchivosOneDrive(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin",usuarioTemporal);
        return "subirArchivosDrive";
    }

    //-------------------------Cliente---------------------------------------
    @GetMapping("dataBaseCliente")
    public String dataBaseCliente(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin",usuarioTemporal);
        return "databaseCliente";
    }

    @PostMapping("/dataBaseCliente/nuevoCliente")
    public String crearNuevoCliente(@RequestParam String nombreCliente,@RequestParam String identificador,@RequestParam(required = true) MultipartFile pathLogo,HttpSession session,Model model) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
        model.addAttribute("admin",usuarioTemporal);
            try {
                Cliente cliente = new Cliente();
                cliente.setNombreCliente(nombreCliente);
                cliente.setIdentificador(identificador);
                cliente.setPathLogo(servicio.guardarArchivo(pathLogo, "/logos/"));
                
                servicio.guardarCliente(cliente);
                return "redirect:/dataBaseCliente"; 


            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Ocurrió un error al guardar el nuevo cliente");
                return "databaseCliente";
            }
            
        }
        return "redirect:/";
    }
    
    //-------------------------Relator---------------------------------------
    @GetMapping("dataBaseRelator")
    public String dataBaseRelator(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal == null) {
	        return "redirect:/";  
	    }
        model.addAttribute("admin",usuarioTemporal);
        return "databaseRelator";
    }

    @PostMapping("/dataBaseRelator/nuevoRelator")
    public String crearNuevoRelator(@RequestParam String nombre,@RequestParam String contacto,@RequestParam String datosExtras,@RequestParam(required = true) MultipartFile foto,HttpSession session,Model model) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
        model.addAttribute("admin",usuarioTemporal);
            try {
                Relator relator = new Relator();
                relator.setNombre(nombre);
                relator.setHorasTrabajados(0f);
                relator.setContacto(contacto);
                relator.setDatosExtras(datosExtras);
                relator.setFoto(servicio.guardarArchivo(foto, "/fotos/"));
                
                servicio.guardarRelator(relator);
                return "redirect:/dataBaseRelator"; 


            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Ocurrió un error al guardar el nuevo cliente");
                return "databaseRelator";
            }
            
        }
        return "redirect:/";
    }
    
}
