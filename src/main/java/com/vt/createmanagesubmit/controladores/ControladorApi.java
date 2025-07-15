package com.vt.createmanagesubmit.controladores;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.vt.createmanagesubmit.dto.AlumnoDTO;
import com.vt.createmanagesubmit.dto.filtroDTO;
import com.vt.createmanagesubmit.modelos.Admin;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.servicios.Servicio;
import com.vt.createmanagesubmit.servicios.ServicioArchivos;
import com.vt.createmanagesubmit.servicios.ServicioGenerarCertificado;

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

    @Autowired
    @Lazy
    private ServicioGenerarCertificado servicioGenerarCertificado;

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

    @PostMapping("/datosAlumno/busquedaMultiFiltro")
    public List<AlumnoDTO> busquedaMultiFiltro(@RequestBody List<filtroDTO> filtros, HttpSession session) {
        Admin usuario = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuario != null) {
            Page<Alumno> alumnos = ser.buscarConMultiplesFiltros(filtros);
            return alumnos.getContent().stream().map(AlumnoDTO::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
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

    @GetMapping("/dataBasePlantilla/plantilla/{id}/descargar")
    public ResponseEntity<Resource> descargarPlantilla(@PathVariable Long id,HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            Plantilla plantilla = ser.plantillaPorId(id);
            
            if (plantilla == null) {
                // Si la plantilla no existe, devolver 404
                return ResponseEntity.notFound().build();
            }
        
            String pathArchivo = plantilla.getPathArchivo();
        
            // Asegúrate de que 'pathArchivo' es una ruta absoluta o está correctamente resuelta
            Path filePath = Paths.get(pathArchivo).toAbsolutePath();
        
            if (!Files.exists(filePath)) {
                // Si el archivo no existe, devolver 404
                return ResponseEntity.notFound().build();
            }
        
            try {
                // Cargar el archivo como un recurso
                Resource resource = new UrlResource(filePath.toUri());
            
                if (!resource.exists() || !resource.isReadable()) {
                    // Si el recurso no es accesible, lanzar excepción o manejar el error
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            
                // Determinar el tipo de contenido
                String contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            
                // Devolver la respuesta con el archivo
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            
            } catch (MalformedURLException e) {
                // Manejar la excepción
                return ResponseEntity.badRequest().build();
            }
        }else{
            return null;
        }
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

    @PostMapping("/dataBaseAlumno/accionAlumnos")
    @ResponseBody
    public ResponseEntity<Resource> accionAlumnos(@RequestParam("ids") List<Long> ids, @RequestParam("accionElegida") String accionElegida,HttpSession session) throws InterruptedException, ExecutionException, Exception {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
        
            if ("descarga".equals(accionElegida)) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                Path tempDir = Files.createTempDirectory("certificados_" + timestamp);

                try {
                    // Generar certificados y guardarlos en la carpeta
                    for (Long id : ids) {
                        Alumno alumno = ser.alumnoPorId(id); // Método que debes tener o implementar
                        byte[] certificadoBytes = servicioGenerarCertificado.descargarCertificadosServicio(alumno).get();

                        // Guardar cada certificado como un archivo PDF en la carpeta temporal
                        Path certificadoPath = tempDir.resolve("certificado_" + alumno.getNombreAsistente() +"_"+alumno.getNumeroCorrelativoInterno()+ ".pdf");
                        Files.write(certificadoPath, certificadoBytes);
                    }

                    // Comprimir la carpeta en un archivo ZIP
                    Path zipFile = Files.createTempFile("certificados_" + timestamp, ".zip");
                    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
                        Files.walk(tempDir).filter(Files::isRegularFile).forEach(file -> {
                            ZipEntry zipEntry = new ZipEntry(tempDir.relativize(file).toString());
                            try {
                                zos.putNextEntry(zipEntry);
                                Files.copy(file, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
                    }

                    // Preparar el archivo para la descarga
                    Resource resource = new UrlResource(zipFile.toUri());
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificados_" + timestamp + ".zip");

                    // Devolver el archivo ZIP como respuesta
                    return ResponseEntity.ok()
                                         .headers(headers)
                                         .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                         .body(resource);

                } finally {
                    // Limpiar archivos temporales
                    Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }

            } else if ("enviar".equals(accionElegida)) {
                for(Long id: ids){
                    try {
                        System.out.println(id);
                        servicioAr.generateCertificatesById(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
            return null;
        }else{
            return null;
        }
    }

    @PostMapping("/dataBaseAlumno/downloadForQr")
    public CompletableFuture<ResponseEntity<?>> downloadCertificateQr(@RequestBody Map<String, String> data, HttpServletResponse response) {
        String id = data.get("id");
        try {
            return servicioGenerarCertificado.generateCertificateQR(id, response)
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

    @PostMapping("/probarPlantilla")
    public CompletableFuture<ResponseEntity<byte[]>> probarPlantilla(@ModelAttribute Alumno alumno,@RequestParam("idPlantilla")Long idPlantilla,HttpSession session) throws Exception {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            Plantilla plantilla=ser.plantillaPorId(idPlantilla);
            alumno.setPlantilla(plantilla);
            alumno.setNombreAsistente(alumno.getNombreAsistente().toUpperCase());
            return servicioGenerarCertificado.descargarCertificadosServicio(alumno)
                    .thenApply(fileBytes -> {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_PDF); // Cambia al tipo de archivo que corresponda
                        headers.setContentDispositionFormData("attachment", "certificado.pdf");
                        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
                    })
                    .exceptionally(ex -> {
                        // Manejo de errores
                        ex.printStackTrace();
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    });
        }else{
            return null;
        }
    }

    @PostMapping("/dataBaseAlumno/eliminarSeleccionados")
    public ResponseEntity<?> eliminarSeleccionados(@RequestBody List<Long> ids) {
        try {
            for(Long id:ids){
                ser.borrarAlumnoPorId(id);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar");
        }
    }

        
}




