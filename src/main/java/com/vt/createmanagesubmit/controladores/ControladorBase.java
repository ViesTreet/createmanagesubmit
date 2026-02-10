package com.vt.createmanagesubmit.controladores;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    // -----------------------Acciones comunes-----------------------------
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/login")
    public String login(HttpSession session, @RequestParam("correo") String Correo,
            @RequestParam("contrasena") String password) {
        Admin admin = servicio.passwordConfirmacion(Correo, password);
        if (admin != null && admin.getUbicacion() != null) {
            session.setAttribute("usuarioEnSesion", admin);
            return "redirect:/home";
        } else if (admin != null && admin.getUbicacion() == null) {
            session.setAttribute("usuarioEnSesion", admin);
            return "redirect:/ubicacion";
        } else {
            return "redirect:/";
        }

    }

    @GetMapping("/ubicacion")
    public String ubicacion(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        return "ubicacion";
    }

    @PostMapping("/actualizarUbicacion")
    public String actualizarUbicacion(@RequestParam("ubi") String ubicacion, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        usuarioTemporal.setUbicacion(ubicacion);
        servicio.guardarAdmin(usuarioTemporal);
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
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

    // -------------------------------------------------------------------------

    // --------------------------Alumnos----------------------------------------
    @GetMapping("/dataBaseAlumno")
    public String baseDeDatos(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        return "databaseAlumno";
    }

    @GetMapping("/dataBaseAlumno/buscarAlumno")
    public String busquedaAlumno(@RequestParam("filtro") String filtro, @RequestParam("busqueda") String busqueda,
            Model model, HttpSession session) {
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
    public String alumnoDatos(@PathVariable("id") Long id, Model model, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        Alumno alumno = servicio.alumnoPorId(id);
        model.addAttribute("admin", usuarioTemporal);
        if (alumno != null) {
            model.addAttribute("alumno", alumno);
            return "alumnoDatos";
        } else {
            return "redirect:/dataBaseAlumno";
        }
    }

    @GetMapping("/dataBaseAlumno/addAlumnoBase")
    public String AgregarAlumno(Model model, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
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
            HttpSession session, Model model) {

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
        Curso curso = servicio.cursoPorId(cursoId);
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

            return "redirect:/dataBaseAlumno/addAlumnoBase";

        } catch (Exception ex) {
            model.addAttribute("error", ex);
            return "addAlumno";
        }
    }

    @GetMapping("/dataBaseAlumno/addAlumnoBase/excel")
    public String addAlumnoExcel(Model model, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        return "addAlumnoExcel";
    }

    @PostMapping(value = "/dataBaseAlumno/uploadAlumnoExcel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String subirExcel(@RequestPart("file") MultipartFile file,
            @RequestParam(value = "estadoDiplomaExcel", required = false) String estadoDiplomaExcel,
            @RequestParam(value = "cursoId") Long cursoId,
            @RequestParam(value = "estadoExcel") String estadoExcel,
            @RequestParam(value = "rutificador") String rutificador,
            HttpSession session,
            Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            if (file.isEmpty()) {
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
                model.addAttribute("error", ex.getMessage());
                return "addAlumnoExcel";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/dataBaseAlumno/alumno/{id}/editar")
    public String editarAlumno(@PathVariable("id") Long id, Model model, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        Alumno alumno = servicio.alumnoPorId(id);
        model.addAttribute("alumno", alumno);
        return "editarAlumno";
    }

    @PostMapping("/dataBaseAlumno/editarAlumno")
    public String editarAlumno(@RequestParam Long id,
            @RequestParam String nombreAsistente,
            @RequestParam(required = true) Long cursoId,
            @RequestParam(required = false) String notaAprobacion,
            @RequestParam(required = false) String asistencia,
            @RequestParam String estado,
            @RequestParam String diploma,
            @RequestParam String rut,
            @RequestParam String correo, Model model, HttpSession session) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        try {
            nombreAsistente = servicioApi.formatearNombre(nombreAsistente);
            servicio.editarAlumno(id, nombreAsistente, cursoId, notaAprobacion, asistencia, estado, diploma, rut,
                    correo);
            return "redirect:/dataBaseAlumno/alumno/" + id;

        } catch (MissingTemplateException | MissingAlumnoIdException | MissingNameOrRutException ex) {
            Alumno alumno = servicio.alumnoPorId(id);
            model.addAttribute("alumno", alumno);
            model.addAttribute("error", ex.getMessage());
            return "editarAlumno";
        }
    }

    @GetMapping("/dataBaseAlumno/alumno/{id}/borrar")
    public String borrarAlumnoId(@PathVariable("id") Long id, HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        model.addAttribute("admin", usuarioTemporal);
        if (usuarioTemporal != null) {
            servicio.borrarAlumnoPorId(id);
            return "redirect:/dataBaseAlumno";
        }
        return "redirect:/";
    }

    @GetMapping("/dataBaseAlumno/download")
    public void downloadDataBaseAlumno(HttpServletResponse response, HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            throw new IllegalStateException("No autorizado"); // Manejar el caso de no autenticado
        }
        model.addAttribute("admin", usuarioTemporal);
    }

    @GetMapping("/dataBaseAlumno/generateCertificado/{id}")
    public String certificadoPorId(@PathVariable("id") Long id, HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            try {
                servicioAr.generateCertificatesById(id);
                return "redirect:/dataBaseAlumno/alumno/" + id;
            } catch (Exception ex) {
                Alumno alumno = servicio.alumnoPorId(id);
                model.addAttribute("alumno", alumno);
                model.addAttribute("error", ex.getMessage());
                return "alumnoDatos";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/dataBaseAlumno/enviarRestantes")
    public String enviarRestantes(HttpSession session, Model model) {
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
        model.addAttribute("plantillas", plantillas);
        List<Map<String, Object>> cursos = servicioApi.obtenerCursosMoodle();
        model.addAttribute("cursosMoodle", cursos);
        return "moodleProgramado";
    }

    @PostMapping("/programarCertificadoMoodle/crear")
    public String crearMoodleTarea(
            HttpSession session,
            @RequestParam(name = "cursoMoodle", required = true) String cursoMoodleParam,
            @RequestParam(name = "accion", required = true) String accion,
            @RequestParam(name = "cursoId", required = true) Long cursoId,
            @RequestParam(name = "fechaDeEjecucion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDeEjecucion) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }

        // 2) Parseo de IDs (vacío → null)
        Long idCurso = (cursoMoodleParam == null || cursoMoodleParam.isBlank())
                ? null
                : Long.valueOf(cursoMoodleParam);

        if (idCurso == null || accion.isBlank()) {
            return "redirect:/programarCertificadoMoodle";
        }
        Curso curso = servicio.cursoPorId(idCurso);
        servicioTareaP.CrearTarea(
                idCurso,
                accion,
                fechaDeEjecucion,
                curso);

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
        model.addAttribute("plantillas", plantillas);
        List<Map<String, Object>> cursos = servicioApi.obtenerCursosMoodle();
        model.addAttribute("cursosMoodle", cursos);
        return "moodleManual";
    }

    // ----------------------------------------------------------------------------------

    // ----------------------------------Plantilla---------------------------------------
    @GetMapping("/dataBasePlantilla/buscarPlantilla")
    public String busquedaPlantilla(@RequestParam("busqueda") String busqueda, Model model, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas", plantillas);
        model.addAttribute("busqueda", busqueda);
        return "databasePlantillaBusqueda";
    }

    @GetMapping("/dataBasePlantilla")
    public String dataBasePlantilla(Model model, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantillas", plantillas);
        return "databasePlantilla";
    }

    @PostMapping("/dataBasePlantilla/nuevaPlantilla")
    public String crearNuevaPlantilla(@RequestParam String nombreCertificado, @RequestParam String tipo,
            @RequestParam String descripcion, @RequestParam(required = false) MultipartFile pathArchivo,
            @RequestParam(required = false) String pathArchivoS,
            @RequestParam(defaultValue = "false") boolean clonarPlantilla, HttpSession session, Model model) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            Optional<Plantilla> optPlantilla = servicio.plantillaPorNombre(nombreCertificado);
            if (optPlantilla.isPresent()) {
                model.addAttribute("error", "El nombre de la plantilla tiene que ser único, no se puede repetir.");
                List<Plantilla> plantillas = servicio.todasLasPlantillas();
                model.addAttribute("plantillas", plantillas);
                return "databasePlantilla";
            }
            try {
                Plantilla nuevaPlantilla = new Plantilla();
                nuevaPlantilla.setTipo(tipo);
                nuevaPlantilla.setNombreCertificado(nombreCertificado);
                nuevaPlantilla.setDescripcion(descripcion);

                // Manejar plantilla
                if (clonarPlantilla) {
                    if (pathArchivoS != null && !pathArchivoS.isEmpty()) {
                        nuevaPlantilla.setPathArchivo(servicio.clonarArchivo(pathArchivoS, "/plantillas/"));
                    } else {
                        throw new IllegalArgumentException(
                                "Debe proporcionar una plantilla existente si desea clonar.");
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
                model.addAttribute("plantillas", plantillas);
                return "databasePlantilla";
            }

        }
        return "redirect:/";
    }

    @GetMapping("/dataBasePlantilla/plantilla/{id}/borrar")
    public String borrarPlantilla(@PathVariable("id") Long id, Model model, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        if (!usuarioTemporal.getCorreo().equals("admin@admin.com")) {
            return "redirect:/dataBasePlantilla";
        }
        model.addAttribute("admin", usuarioTemporal);
        Plantilla plantilla = servicio.plantillaPorId(id);
        model.addAttribute("plantilla", plantilla);
        return "borrarPlantilla";
    }

    @GetMapping("/dataBasePlantilla/borrar/{id}")
    public String borrarPlantillaId(@PathVariable("id") Long id, HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            if (!usuarioTemporal.getCorreo().equals("admin@admin.com")) {
                return "redirect:/dataBasePlantilla";
            }
            model.addAttribute("admin", usuarioTemporal);
            try {
                servicio.borrarPlantillaPorId(id);
                return "redirect:/dataBasePlantilla";
            } catch (IOException ex) {
                model.addAttribute("error", ex);
                List<Plantilla> plantillas = servicio.todasLasPlantillas();
                model.addAttribute("plantillas", plantillas);
                return "databasePlantilla";
            }

        }
        return "redirect:/";
    }

    @GetMapping("/dataBasePlantilla/plantilla/{id}/editar")
    public String editarPlantilla(@PathVariable("id") Long id, Model model, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        Plantilla plantilla = servicio.plantillaPorId(id);
        List<Plantilla> plantillas = servicio.todasLasPlantillas();
        model.addAttribute("plantilla", plantilla);
        model.addAttribute("plantillas", plantillas);
        return "editarPlantilla";
    }

    @PostMapping("/dataBasePlantilla/editarPlantilla")
    public String editarPlantilla(@RequestParam("id") Long id,
            @RequestParam String nombreCertificado, @RequestParam String tipo,
            @RequestParam String descripcion, @RequestParam(defaultValue = "false") boolean cplanti,
            @RequestParam(required = false) MultipartFile pathArchivo,
            @RequestParam(required = false) String pathArchivoS,
            @RequestParam(defaultValue = "false") boolean clonarPlantilla, HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            Plantilla plantilla = servicio.plantillaPorId(id);
            String nombreAntiguo = plantilla.getNombreCertificado();
            boolean error;
            if (nombreAntiguo.equals(nombreCertificado)) {
                error = false;
            } else {
                Optional<Plantilla> optPlantilla = servicio.plantillaPorNombre(nombreCertificado);
                if (optPlantilla.isPresent()) {
                    error = true;
                } else {
                    error = false;
                }
            }
            if (nombreCertificado.trim().isEmpty()) {
                error = true;
            }
            if (error) {
                model.addAttribute("error", "El nombre tiene que ser unicó y no puede estar vacio");
                Plantilla plantillaError = servicio.plantillaPorId(id);
                model.addAttribute("plantilla", plantillaError);
                return "editarPlantilla";

            }
            plantilla.setNombreCertificado(nombreCertificado);
            plantilla.setDescripcion(descripcion);
            plantilla.setTipo(tipo);
            if (cplanti) {
                if (clonarPlantilla) {
                    if (pathArchivoS != null && !pathArchivoS.isEmpty()) {
                        try {
                            Path deletePlantillaPath = Paths.get(plantilla.getPathArchivo());
                            try {
                                Files.deleteIfExists(deletePlantillaPath);
                            } catch (IOException ex) {
                                throw new IOException("No se encontró la ruta de la plantilla.", ex);
                            }
                            plantilla.setPathArchivo(servicio.clonarArchivo(pathArchivoS, "/plantillas/"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new IllegalArgumentException(
                                "Debe proporcionar una plantilla existente si desea clonar.");
                    }
                } else if (pathArchivo != null && !pathArchivo.isEmpty()) {
                    try {
                        Path deletePlantillaPath = Paths.get(plantilla.getPathArchivo());
                        try {
                            Files.deleteIfExists(deletePlantillaPath);
                        } catch (IOException ex) {
                            throw new IOException("No se encontró la ruta de la plantilla.", ex);
                        }
                        plantilla.setPathArchivo(servicio.guardarArchivo(pathArchivo, "/plantillas/"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new IllegalArgumentException("Debe proporcionar una plantilla válida para guardar.");
                }
            }
            servicio.guardarPlantilla(plantilla);
            return "redirect:/dataBasePlantilla";
        }
        return "redirect:/";
    }

    @GetMapping("/dataBasePlantilla/plantilla/{id}/probar")
    public String probarPlantilla(@PathVariable("id") Long id, Model model, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        Plantilla plantilla = servicio.plantillaPorId(id);
        model.addAttribute("plantilla", plantilla);
        return "probarPlantilla";
    }

    // -----------------------------------------------------------------------

    // -------------------------Admins----------------------------------------
    @GetMapping("/dataBaseAdmin")
    public String dataBaseAdministrador(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        return "databaseAdmin";
    }

    @GetMapping("/dataBaseAdmin/{id}/borrar")
    public String borrarAdmin(@PathVariable("id") Long id, HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            try {
                if (usuarioTemporal.getCorreo().equals("admin@admin.com")) {
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
    public String crearNuevoAdmin(@RequestParam(value = "correo") String correo,
            @RequestParam(value = "nombre") String nombre, @RequestParam(value = "contrasena") String password,
            @RequestParam(value = "rol") String rol, HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            if (servicio.adminPorCorreo(correo) == null) {
                servicio.registrarAdmin(correo, nombre, password, rol);
                return "redirect:/dataBaseAdmin";
            } else {
                model.addAttribute("error", "El correo de los administradores no se pueden repetir");
                return "databaseAdmin";
            }
        }
        return "redirect:/";
    }

    // ----------------------Otros-------------------------------------
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
            if (alumno != null) {
                model.addAttribute("alumno", alumno);
                model.addAttribute("curso", alumno.getCurso().getNombreCurso());
                model.addAttribute("val", "Válido");
                model.addAttribute("idEnc", idEncriptada);
            } else {
                model.addAttribute("alumno", alumnoError);
                model.addAttribute("curso", cursoError.getNombreCurso());
                model.addAttribute("val", "No válido");
            }
        } catch (Exception e) {
            model.addAttribute("alumno", alumnoError);
            model.addAttribute("curso", cursoError.getNombreCurso());
            model.addAttribute("val", "No válido");
            e.printStackTrace();
        }
        return "generarCertificadoQr";
    }

    @GetMapping("/seccionAsistencia")
    public String generarQrDeAsistencia(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            // Cargar plantillas para el select inicial opcional (aunque el frontend también
            // pide /api/plantillas)
            model.addAttribute("plantillas", servicio.todasLasPlantillas());
            model.addAttribute("admin", usuarioTemporal);
            return "databaseCursoTemporales";
        }
        return "redirect:/";
    }

    @GetMapping("/seccionAsistencia/revision/{id}")
    public String alumnoTemporalPorCurso(@PathVariable("id") String id, HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            Long idR = Long.valueOf(id);
            Curso cursoTemporal = servicio.cursoPorId(idR);
            model.addAttribute("idCurso", id);
            model.addAttribute("cursoTemporal", cursoTemporal);
            model.addAttribute("admin", usuarioTemporal);
            return "databaseAlumnoTemporal";
        }
        return "redirect:/";

    }

    @GetMapping("/marcarAsistenciaCurso/{id}")
    public String marcarAsistenciaCurso(HttpSession session, Model model, @PathVariable("id") String idEncriptada) {
        model.addAttribute("id", idEncriptada);
        try {
            Curso curso = servicio.cursoPorId(Long.valueOf(servicio.decryptId(idEncriptada)));
            if (!curso.isAsistenciaQr()) {
                return "error404";
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    // -----------------------------------------------------------------------

    // -------------------------OneDrive---------------------------------------

    @GetMapping("/subirOneDrive")
    public String subirArchivosOneDrive(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        return "subirArchivosDrive";
    }

    // -------------------------Cliente---------------------------------------
    @GetMapping("dataBaseCliente")
    public String dataBaseCliente(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        return "databaseCliente";
    }

    @PostMapping("/dataBaseCliente/nuevoCliente")
    public String crearNuevoCliente(@RequestParam String nombreCliente, @RequestParam String identificador,
            @RequestParam(required = true) MultipartFile pathLogo, @RequestParam MultipartFile pathLogoFoot,
            HttpSession session, Model model) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            if (servicio.clientePorIdentificador(identificador) == null) {
                try {
                    Cliente cliente = new Cliente();
                    cliente.setNombreCliente(nombreCliente);
                    cliente.setIdentificador(identificador);
                    if (!pathLogo.isEmpty()) {
                        cliente.setPathLogo(servicio.guardarArchivo(pathLogo, "/logos/"));
                    } else {
                        cliente.setPathLogo(null);
                    }
                    if (!pathLogoFoot.isEmpty()) {
                        cliente.setPathLogoFooter(servicio.guardarArchivo(pathLogoFoot, "/logos/"));

                    } else {
                        cliente.setPathLogoFooter(null);
                    }

                    servicio.guardarCliente(cliente);
                    return "redirect:/dataBaseCliente";

                } catch (Exception e) {
                    e.printStackTrace();
                    model.addAttribute("error", "Ocurrió un error al guardar el nuevo cliente");
                    return "databaseCliente";
                }

            } else {
                model.addAttribute("error", "El cliente ya existe");
                return "databaseCliente";

            }
        }
        return "redirect:/";
    }

    @GetMapping("/dataBaseCliente/Cliente/{id}/editar")
    public String editarDatosCliente(HttpSession session, Model model, @PathVariable("id") Long id) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        try {
            Cliente cliente = servicio.clientePorId(id);
            model.addAttribute("cliente", cliente);
            model.addAttribute("admin", usuarioTemporal);
            return "editarCliente";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
    }

    @PostMapping("/dataBaseCliente/editarCliente")
    public String editarCliente(
            @RequestParam("id") Long id,
            @RequestParam("nombreCliente") String nombreCliente,
            @RequestParam("identificador") String identificador,

            @RequestParam(value = "editarLogoP", required = false) Boolean editarLogoP,
            @RequestParam(value = "editarLogoI", required = false) Boolean editarLogoI,

            @RequestParam(value = "pathLogo", required = false) MultipartFile pathLogo,
            @RequestParam(value = "pathLogoFoot", required = false) MultipartFile pathLogoFoot, HttpSession session) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        try {
            Cliente clienteEditar = servicio.clientePorId(id);
            Cliente clienteComparador = servicio.clientePorIdentificador(identificador);

            if (clienteComparador == null || clienteComparador.getId() == clienteEditar.getId()) {
                clienteEditar.setNombreCliente(nombreCliente);
                clienteEditar.setIdentificador(identificador);
                if (Boolean.TRUE.equals(editarLogoP) && pathLogo != null && !pathLogo.isEmpty()) {
                    if (clienteEditar.getPathLogo() != null) {
                        Path deleteClientePath = Paths.get(clienteEditar.getPathLogo());
                        try {
                            Files.deleteIfExists(deleteClientePath);
                        } catch (IOException ex) {
                            throw new IOException("No se encontró la ruta de la plantilla.", ex);
                        }
                    }
                    clienteEditar.setPathLogo(servicio.guardarArchivo(pathLogo, "/logos/"));

                }

                if (Boolean.TRUE.equals(editarLogoI) && pathLogoFoot != null && !pathLogoFoot.isEmpty()) {
                    if (clienteEditar.getPathLogoFooter() != null) {
                        Path deleteClientePathFoot = Paths.get(clienteEditar.getPathLogoFooter());
                        try {
                            Files.deleteIfExists(deleteClientePathFoot);
                        } catch (IOException ex) {
                            throw new IOException("No se encontró la ruta de la plantilla.", ex);
                        }
                    }

                    clienteEditar.setPathLogoFooter(servicio.guardarArchivo(pathLogoFoot, "/logos/"));

                }
                servicio.guardarCliente(clienteEditar);
            }
            return "redirect:/dataBaseCliente";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dataBaseCliente";
        }
    }

    // -------------------------Relator---------------------------------------
    @GetMapping("dataBaseRelator")
    public String dataBaseRelator(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        return "databaseRelator";
    }

    @PostMapping("/dataBaseRelator/nuevoRelator")
    public String crearNuevoRelator(@RequestParam String nombre, @RequestParam String contacto,
            @RequestParam String datosExtras, @RequestParam(required = true) MultipartFile foto, HttpSession session,
            Model model) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            model.addAttribute("admin", usuarioTemporal);
            try {
                Relator relator = new Relator();
                relator.setNombre(nombre);
                relator.setHorasTrabajados(0f);
                relator.setContacto(contacto);
                relator.setDatosExtras(datosExtras);
                if (!foto.isEmpty()) {
                    relator.setFoto(servicio.guardarArchivo(foto, "/fotos/"));
                } else {
                    relator.setFoto(null);
                }

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

    @GetMapping("/dataBaseRelator/Relator/{id}/editar")
    public String editarDatosRelator(HttpSession session, Model model, @PathVariable("id") Long id) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        try {
            Relator relator = servicio.relatorPorId(id);
            model.addAttribute("relator", relator);
            model.addAttribute("admin", usuarioTemporal);
            return "editarRelator";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
    }

    @PostMapping("/dataBaseRelator/editarRelator")
    public String editarRelator(
            @RequestParam("id") Long id,
            @RequestParam("nombre") String nombre,
            @RequestParam("contacto") String contacto,
            @RequestParam("datosExtras") String datosExtras,

            @RequestParam(value = "editarFoto", required = false) boolean editarFoto,
            @RequestParam(value = "foto", required = false) MultipartFile foto,

            HttpSession session) {

        Admin usuario = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuario == null) {
            return "redirect:/";
        }

        try {
            Relator relator = servicio.relatorPorId(id);

            relator.setNombre(nombre);
            relator.setContacto(contacto);
            relator.setDatosExtras(datosExtras);

            if (editarFoto && foto != null && !foto.isEmpty()) {

                if (relator.getFoto() != null) {
                    Path oldPath = Paths.get(relator.getFoto());
                    Files.deleteIfExists(oldPath);
                }

                // guardar nueva
                relator.setFoto(servicio.guardarArchivo(foto, "/fotos/"));
            }

            servicio.guardarRelator(relator);
            return "redirect:/dataBaseRelator";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dataBaseRelator";
        }
    }

    // -------------------------Curso---------------------------------------
    @GetMapping("dataBaseCurso")
    public String dataBaseCurso(HttpSession session, Model model) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        model.addAttribute("admin", usuarioTemporal);
        return "databaseCurso";
    }


    @GetMapping("/dataBaseCurso/Cliente/{id}")
    public String dataBaseCursoPorCliente(HttpSession session, Model model, @PathVariable("id") Long id) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        String cliente = "default";
        try {
            cliente = servicio.clientePorId(id).getNombreCliente();
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("nombre", cliente);
        model.addAttribute("admin", usuarioTemporal);
        model.addAttribute("cliente", id);
        return "databaseCursoFiltroCliente";
    }

    @GetMapping("/dataBaseCurso/Relator/{id}")
    public String dataBaseCursoPorRelator(HttpSession session, Model model, @PathVariable("id") Long id) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        String relator = "default";
        try {
            relator = servicio.relatorPorId(id).getNombre();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ;
        model.addAttribute("nombre", relator);
        model.addAttribute("admin", usuarioTemporal);
        model.addAttribute("relator", id);
        return "databaseCursoFiltroRelator";
    }

    @GetMapping("/dataBaseCurso/Curso/{id}/editar")
    public String editarDatosCurso(HttpSession session, Model model, @PathVariable("id") Long id) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        Curso curso = servicio.cursoPorId(id);

        LocalDate fecha = curso.getFechaInicio().toLocalDate();
        LocalTime horaInicio = curso.getFechaInicio().toLocalTime();
        LocalTime horaFin = curso.getFechaFin().toLocalTime();
        model.addAttribute("admin", usuarioTemporal);
        model.addAttribute("curso", curso);
        model.addAttribute("fecha", fecha);
        model.addAttribute("horaI", horaInicio);
        model.addAttribute("horaF", horaFin);

        return "editarCurso";
    }

    @PostMapping("/dataBaseCurso/editarCurso")
    public String editarCurso(

            // ===== DIPLOMA =====

            @RequestParam String nombreCurso,

            @RequestParam String diasCursos,

            @RequestParam String duracion,

            @RequestParam Long clienteId,

            @RequestParam String modalidad,

            @RequestParam String ubicacionSubida,

            @RequestParam String ciudad,

            @RequestParam Long relatorId,

            @RequestParam Long plantillaDipId,

            @RequestParam Float NotaMin,

            @RequestParam Integer asistenciaMin,

            // ===== FLYER =====

            @RequestParam String ubicacionDelCurso,

            @RequestParam String ubicacionCliente,

            @RequestParam String fecha, // yyyy-MM-dd

            @RequestParam String horaI, // HH:mm

            @RequestParam String horaF, // HH:mm

            @RequestParam Float horasRelatorCurso,

            @RequestParam Long plantillaFlyId,

            @RequestParam Long id,

            @RequestParam boolean asistenciaQr,

            RedirectAttributes redirectAttrs) {

        try {

            LocalDate fechaCurso = LocalDate.parse(fecha);
            LocalTime horaInicio = LocalTime.parse(horaI);
            LocalTime horaFin = LocalTime.parse(horaF);

            LocalDateTime fechaDeInicio = LocalDateTime.of(fechaCurso, horaInicio);
            LocalDateTime fechaDeFinalizacion = LocalDateTime.of(fechaCurso, horaFin);
            Curso curso = servicio.cursoPorId(id);
            Cliente cliente = servicio.clientePorId(clienteId);

            Relator relator = servicio.buscarRelatorPorId(relatorId);
            relator.setHorasTrabajados(relator.getHorasTrabajados() + horasRelatorCurso);
            servicio.guardarRelator(relator);

            Relator relatorAntiguo = curso.getRelator();
            relatorAntiguo.setHorasTrabajados(relatorAntiguo.getHorasTrabajados() - curso.getHorasRelatorCurso());
            servicio.guardarRelator(relatorAntiguo);
            Plantilla plantillaDiploma = servicio.plantillaPorId(plantillaDipId);

            Plantilla plantillaFlyer = servicio.plantillaPorId(plantillaFlyId);

            curso.setNombreCurso(nombreCurso);
            curso.setDiasCursos(diasCursos);
            curso.setDuracion(duracion);

            curso.setCliente(cliente);
            curso.setModalidad(modalidad);
            curso.setUbicacionSubida(ubicacionSubida);
            curso.setCiudad(ciudad);

            curso.setRelator(relator);
            curso.setNotaMin(NotaMin);
            curso.setAsistenciaMin(asistenciaMin);

            curso.setUbicacionDelCurso(ubicacionDelCurso);
            curso.setUbicacionCliente(ubicacionCliente);
            curso.setFechaInicio(fechaDeInicio);
            curso.setFechaFin(fechaDeFinalizacion);

            curso.setPlantillaDiploma(plantillaDiploma);
            curso.setPlantillaFlyer(plantillaFlyer);
            curso.setHorasRelatorCurso(horasRelatorCurso);
            curso.setAsistenciaQr(asistenciaQr);
            servicio.guardarCurso(curso);
            return "redirect:/dataBaseCurso";

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/dataBaseCurso";
        }
    }

    @GetMapping("/dataBaseCurso/Curso/{id}/alumnos")
    public String dataBaseAlumnosPorCurso(HttpSession session, Model model, @PathVariable("id") Long id) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return "redirect:/";
        }
        String curso = "default";
        try {
            curso = servicio.cursoPorId(id).getNombreCurso();
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("nombre", curso);
        model.addAttribute("admin", usuarioTemporal);
        model.addAttribute("curso", id);
        return "databaseAlumnoCurso";
    }

}
