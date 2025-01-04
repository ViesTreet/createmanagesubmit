package com.vt.createmanagesubmit.controladores;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vt.createmanagesubmit.dto.AlumnoDTO;
import com.vt.createmanagesubmit.modelos.Admin;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.servicios.Servicio;
import com.vt.createmanagesubmit.servicios.ServicioArchivos;

import jakarta.servlet.http.HttpServletRequest;
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

    private static final int MAX_DOWNLOADS = 5;
    private static final long TIME_FRAME = 60 * 60 * 1000; // 1 hora

    private final Map<String, List<Long>> downloadTracker = new ConcurrentHashMap<>();


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

    @PostMapping("/dataBaseAlumno/downloadForQr")
    public CompletableFuture<ResponseEntity<?>> downloadCertificateQr(@RequestBody Map<String, String> data, HttpServletResponse response) {
        String id = data.get("id");
        try {
            return servicioAr.generateCertificateQR(id, response)
                .thenApply(result -> ResponseEntity.ok().build());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor"));
        }
    }


    @GetMapping("/getIP")
    public ResponseEntity<String> getClientIP(HttpServletRequest request) {
        String clientIP = request.getHeader("X-Forwarded-For");
        if (clientIP == null || clientIP.isEmpty()) {
            clientIP = request.getRemoteAddr();
        }
        return ResponseEntity.ok(clientIP);
    }

    @PostMapping("/checkIP")
    public ResponseEntity<String> checkDownloadLimit(@RequestBody String clientIP) {
        long currentTime = System.currentTimeMillis();

        // Obtener o inicializar el historial de descargas de la IP
        downloadTracker.putIfAbsent(clientIP, new ArrayList<>());
        List<Long> timestamps = downloadTracker.get(clientIP);

        // Limpiar las descargas fuera del intervalo
        timestamps.removeIf(timestamp -> currentTime - timestamp > TIME_FRAME);

        if (timestamps.size() >= MAX_DOWNLOADS) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Límite de descargas alcanzado.");
        }

        // Registrar la descarga
        timestamps.add(currentTime);
        return ResponseEntity.ok("Descarga permitida.");
    }
    
}



