package com.vt.createmanagesubmit.controladores;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import com.vt.createmanagesubmit.dto.AlumnoDTO;
import com.vt.createmanagesubmit.modelos.Admin;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.servicios.Servicio;
import com.vt.createmanagesubmit.servicios.ServicioArchivos;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;



@RestController
@RequestMapping("/api")
public class ControladorApi {

    @Autowired
    @Lazy
    private Servicio ser;

    @Autowired
    @Lazy
    private ServicioArchivos servicioAr;

    @GetMapping("/datosAlumno")
    public List<AlumnoDTO> getDatosAlumno(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            Page<Alumno> alumnos = ser.todosLosAlumnos();
            return alumnos.getContent().stream().map(AlumnoDTO::new).collect(Collectors.toList());
        }
        return null;
    }

    @GetMapping("/datosAlumno/busquedaAlumno")
    public List<AlumnoDTO> getDatosBusquedaAlumno(@RequestParam String filtro, @RequestParam String busqueda,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
	    
            Page<Alumno> alumnos = ser.buscarAlumnosPorCriterio(filtro, busqueda); // Implementa este método en tu servicio
            return alumnos.getContent().stream().map(AlumnoDTO::new).collect(Collectors.toList());
        }
        return null;
    }

    @GetMapping("/datosPlantilla")
    public List<Plantilla> getDatosPlantilla(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            List<Plantilla> plantilla = ser.todasLasPlantillas();
            return plantilla;
        }
        return null;
    }

    @GetMapping("/datosPlantilla/busquedaPlantilla")
    public List<Plantilla> getDatosBusquedaPlantilla(@RequestParam String busqueda,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            List<Plantilla> plantilla = ser.buscarPlantillaPorCriterio(busqueda);
            return plantilla;
        }
        return null;
    }

    @GetMapping("/datosAdmin")
    public List<Admin> getDatosAdmin(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            List<Admin> admin = ser.todasLosAdmin();
            return admin;
        }
        return null;
    }

    @GetMapping("/dataBaseAdmin/{id}/borrar")
    public RedirectView borrarAdmin(@PathVariable("id")Long id,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            ser.borrarAdminPorId(id);
            return new RedirectView("/dataBaseAdmin");
        }
        return new RedirectView("/");
    }
    
    
    

    @PostMapping(value = "/uploadAlumnoExcel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RedirectView subirExcel(@RequestPart("file") MultipartFile file, 
     @RequestParam(value = "estadoDiplomaExcel", required = false) String estadoDiplomaExcel, 
     @RequestParam(value = "plantillaNombre") String plantilla, 
     @RequestParam(value = "estadoExcel") String estadoExcel,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            if (file.isEmpty()) {
                return new RedirectView("/error");
            }
            try {
                // Leer el contenido del archivo en un arreglo de bytes
                byte[] fileBytes = file.getBytes();

                // Llamar al método asíncrono y pasarle los bytes del archivo
                servicioAr.leerExcelYGuardarEnBD(fileBytes, estadoDiplomaExcel, plantilla, estadoExcel);

                // Redirigir inmediatamente sin esperar a que termine el procesamiento
                return new RedirectView("/dataBaseAlumno");
            } catch (Exception e) {
                e.printStackTrace();
                return new RedirectView("/error");
            }
        }
        return new RedirectView("/");
    }


    @GetMapping("/generateCertificates")
    public ResponseEntity<?> generateCertificates(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            try {
                servicioAr.generateCertificatesAll();
                return ResponseEntity.ok("Certificados generados exitosamente.");
            } catch(Exception e) {
                return ResponseEntity.status(500).body("Error al generar certificados: " + e.getMessage());
            }
        }
        return null;
    }

    @GetMapping("/enviarRestantes")
    public RedirectView enviarRestantes(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            try {
                servicioAr.generateCertificatesAll();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            return new RedirectView("/dataBaseAlumno");
        }
        return new RedirectView("/");
    }
    
    @GetMapping("/generateCert/{id}")
    public RedirectView certificadoPorIdApi(@PathVariable("id")Long id,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            try {
                servicioAr.generateCertificatesById(id);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return new RedirectView("/dataBaseAlumno/alumno/"+id);
        }
        return new RedirectView("/");
    }
    
    @GetMapping("/dataBaseAlumno/alumno/{id}/borrar")
    public RedirectView borrarAlumnoIdApi(@PathVariable("id")Long id,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            ser.borrarAlumnoPorId(id);
            return new RedirectView("/dataBaseAlumno");
        }
        return new RedirectView("/");
    }
    
    @GetMapping("/dataBasePlantilla/Plantilla/{id}/borrar")
    public RedirectView borrarPlantillaIdApi(@PathVariable("id")Long id,HttpSession session){
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            ser.borrarPlantillaPorId(id);
            return new RedirectView("/dataBasePlantilla");
        }
        return new RedirectView("/");
    }

    @PostMapping("/editarPlantilla")
    public RedirectView editarPlantilla(@RequestParam("id") Long id,@RequestParam(value = "cambiarLogo", required = false) boolean cambiarLogo,@RequestParam(value = "cambiarPlantilla", required = false) boolean cambiarPlantilla,@RequestParam(value = "pathLogo", required = false) MultipartFile nuevoLogo,@RequestParam(value = "pathArchivo", required = false) MultipartFile nuevaPlantilla,@RequestParam(value = "nombreCertificado")String nombre,@RequestParam(value = "descripcion")String descripcion,@RequestParam(value = "asistenciaMin")String asistencia,@RequestParam(value = "notaMin")String nota ,HttpSession session) {  
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {   
            Plantilla plantilla = ser.plantillaPorId(id);
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

            ser.guardarPlantilla(plantilla);   
            try {
                if (cambiarLogo && nuevoLogo != null && !nuevoLogo.isEmpty()) {
                    ser.cambiarLogo(id, nuevoLogo);
                }
                if (cambiarPlantilla && nuevaPlantilla != null && !nuevaPlantilla.isEmpty()) {
                    ser.cambiarPlantilla(id, nuevaPlantilla);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new RedirectView("/dataBasePlantilla"); // Redirigir a la página de edición
        }
        return new RedirectView("/");
    }

    @PostMapping("/nuevaPlantilla")
    public RedirectView crearNuevaPlantilla(
            @RequestParam String nombreCertificado,
            @RequestParam String descripcion,
            @RequestParam String asistenciaMin,
            @RequestParam String notaMin,
            @RequestParam(required = false) MultipartFile pathLogo,
            @RequestParam(required = false) MultipartFile pathArchivo,
            @RequestParam(required = false) String pathLogoS,
            @RequestParam(required = false) String pathArchivoS,
            @RequestParam(defaultValue = "false") boolean clonarLogo,
            @RequestParam(defaultValue = "false") boolean clonarPlantilla,HttpSession session) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            try {
                // Crear una nueva plantilla
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
                        nuevaPlantilla.setPathLogo(ser.clonarArchivo(pathLogoS, "/logos/"));
                    } else {
                        nuevaPlantilla.setPathLogo(null);
                    }
                } else if (pathLogo != null && !pathLogo.isEmpty()) {
                    nuevaPlantilla.setPathLogo(ser.guardarArchivo(pathLogo, "/logos/"));
                } else {
                    nuevaPlantilla.setPathLogo(null);
                }

                // Manejar plantilla
                if (clonarPlantilla) {
                    if (pathArchivoS != null && !pathArchivoS.isEmpty()) {
                        nuevaPlantilla.setPathArchivo(ser.clonarArchivo(pathArchivoS, "/plantillas/"));
                    } else {
                        throw new IllegalArgumentException("Debe proporcionar una plantilla existente si desea clonar.");
                    }
                } else if (pathArchivo != null && !pathArchivo.isEmpty()) {
                    nuevaPlantilla.setPathArchivo(ser.guardarArchivo(pathArchivo, "/plantillas/"));
                } else {
                    throw new IllegalArgumentException("Debe proporcionar una plantilla válida para guardar.");
                }

                // Guardar en la base de datos
                ser.guardarPlantilla(nuevaPlantilla);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return new RedirectView("/dataBasePlantilla"); 
        }
        return new RedirectView("/");
    }

    @PostMapping("/nuevoAdmin")
    public RedirectView crearNuevoAdmin(@RequestParam(value = "correo")String correo,@RequestParam(value = "nombre")String nombre,@RequestParam(value = "contrasena")String password,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
	    if (usuarioTemporal != null) {
            ser.registrarAdmin(correo,nombre,password);
        
            return new RedirectView("/dataBaseAdmin");
        }
        return new RedirectView("/");
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



