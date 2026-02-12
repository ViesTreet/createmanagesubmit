package com.vt.createmanagesubmit.controladores;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.vt.createmanagesubmit.dto.AlumnoDTO;
import com.vt.createmanagesubmit.dto.AlumnosWrapper;
import com.vt.createmanagesubmit.dto.ClienteDTO;
import com.vt.createmanagesubmit.dto.CursoDTO;
import com.vt.createmanagesubmit.dto.RelatorDTO;
import com.vt.createmanagesubmit.dto.TareaDTO;
import com.vt.createmanagesubmit.modelos.Admin;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.AlumnoTemporal;
import com.vt.createmanagesubmit.modelos.Cliente;
import com.vt.createmanagesubmit.modelos.Curso;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.modelos.Relator;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;
import com.vt.createmanagesubmit.repositorios.RepositorioCliente;
import com.vt.createmanagesubmit.repositorios.RepositorioCurso;
import com.vt.createmanagesubmit.repositorios.RepositorioPlantillas;
import com.vt.createmanagesubmit.repositorios.RepositorioRelator;
import com.vt.createmanagesubmit.servicios.Servicio;
import com.vt.createmanagesubmit.servicios.ServicioArchivos;
import com.vt.createmanagesubmit.servicios.ServicioGenerarCertificado;
import com.vt.createmanagesubmit.servicios.TareaMoodleService;

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

    @Autowired
    private TareaMoodleService servicioTarea;

    @Autowired
    private RepositorioAlumnos repoAlum;

    @Autowired
    private RepositorioCliente repoCliente;

    @Autowired
    private RepositorioRelator repoRel;

    @Autowired
    private RepositorioCurso repoCurso;

    @Autowired
    private RepositorioPlantillas repoPlanti;

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

    @GetMapping("/datosAlumnoPorCurso/{id}")
    public List<AlumnoDTO> getDatosAlumnoPorCurso(HttpSession session, @PathVariable("id") Long id) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            Page<Alumno> alumnos = ser.alumnoPorCursoId(id);
            return alumnos.getContent().stream().map(AlumnoDTO::new).collect(Collectors.toList());
        }
        return null;
    }

    @GetMapping("/tareasProgramadas")
    public List<TareaDTO> getMethodName(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            return ser.todasLasTareas()
                    .getContent()
                    .stream()
                    .map(TareaDTO::new)
                    .collect(Collectors.toList());
        }
        return null;
    }

    @PostMapping("/datosAlumno/busquedaMultiFiltro")
    public List<AlumnoDTO> busquedaMultiFiltroAlumno(
            @RequestBody List<Map<String, String>> filtros,
            HttpSession session) {

        Admin usuario = (Admin) session.getAttribute("usuarioEnSesion");

        if (usuario != null) {
            Page<Alumno> alumnos = ser.buscarConMultiplesFiltrosAlumno(filtros);
            return alumnos.getContent()
                    .stream()
                    .map(AlumnoDTO::new)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @PostMapping("/datosAlumno/busquedaMultiFiltroPorCurso/{id}")
    public List<AlumnoDTO> busquedaMultiFiltroAlumnoPorCurso(
            @RequestBody List<Map<String, String>> filtros,
            HttpSession session,
            @PathVariable("id") Long id) {

        Admin usuario = (Admin) session.getAttribute("usuarioEnSesion");

        if (usuario != null) {
            Page<Alumno> alumnos = ser.buscarConMultiplesFiltrosAlumnoPorCurso(filtros, id);
            return alumnos.getContent()
                    .stream()
                    .map(AlumnoDTO::new)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @GetMapping("/datosAlumno/busquedaAlumno")
    public List<AlumnoDTO> getDatosBusquedaAlumno(@RequestParam String filtro, @RequestParam String busqueda,
            HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {

            Page<Alumno> alumnos = ser.buscarAlumnosPorCriterio(filtro, busqueda); // Implementa este método en tu
                                                                                   // servicio
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
    public List<Plantilla> getDatosBusquedaPlantilla(@RequestParam String busqueda, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            List<Plantilla> plantilla = ser.buscarPlantillaPorCriterio(busqueda);
            return plantilla;
        }
        return null;
    }

    @GetMapping("/dataBasePlantilla/plantilla/{id}/descargar")
    public ResponseEntity<Resource> descargarPlantilla(@PathVariable Long id, HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            Plantilla plantilla = ser.plantillaPorId(id);

            if (plantilla == null) {
                // Si la plantilla no existe, devolver 404
                return ResponseEntity.notFound().build();
            }

            String pathArchivo = plantilla.getPathArchivo();

            // Asegúrate de que 'pathArchivo' es una ruta absoluta o está correctamente
            // resuelta
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
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);

            } catch (MalformedURLException e) {
                // Manejar la excepción
                return ResponseEntity.badRequest().build();
            }
        } else {
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
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error al generar certificados: " + e.getMessage());
            }
        }
        return null;
    }

    @PostMapping("/dataBaseAlumno/accionAlumnos")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> accionAlumnos(@RequestParam("ids") List<Long> ids,
            @RequestParam("accionElegida") String accionElegida, HttpSession session)
            throws InterruptedException, ExecutionException, Exception {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {

            if ("descarga".equals(accionElegida)) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                Path tempDir = Files.createTempDirectory("certificados_" + timestamp);

                try {
                    // Generar certificados y guardarlos en la carpeta
                    for (Long id : ids) {
                        Alumno alumno = ser.alumnoPorId(id); // Método que debes tener o implementar

                        byte[] certificadoBytes = servicioGenerarCertificado
                                .descargarCertificadosServicio(alumno.getId())
                                .join();

                        // Guardar cada certificado como un archivo PDF en la carpeta temporal
                        Path certificadoPath = tempDir.resolve("certificado_" + alumno.getNombreAsistente() + "_"
                                + alumno.getNumeroCorrelativoInterno() + ".pdf");
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
                    headers.add(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=certificados_" + timestamp + ".zip");

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
                for (Long id : ids) {
                    try {
                        System.out.println(id);
                        servicioAr.generateCertificatesById(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return ResponseEntity.ok().build();

            } else if ("descargaJunto".equals(accionElegida)) {

                PDFMergerUtility merger = new PDFMergerUtility();
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                for (Long id : ids) {
                    Alumno alumno = ser.alumnoPorId(id);

                    byte[] certificadoBytes = servicioGenerarCertificado
                            .descargarCertificadosServicio(alumno.getId())
                            .join();

                    merger.addSource(new RandomAccessReadBuffer(certificadoBytes));
                }

                merger.setDestinationStream(out);
                merger.mergeDocuments(null);

                byte[] pdfUnico = out.toByteArray();

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=certificados.pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdfUnico);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } else {
            return ResponseEntity.badRequest().build();

        }
    }

    @PostMapping("/dataBaseAlumno/downloadForQr")
    public CompletableFuture<ResponseEntity<?>> downloadCertificateQr(@RequestBody Map<String, String> data,
            HttpServletResponse response) {
        try {
            String id = data.get("id");
            return servicioGenerarCertificado.generateCertificateQR(id, response)
                    .thenApply(result -> ResponseEntity.ok().build());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor"));
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

    @PostMapping("/probarPlantillaDiploma")
    public CompletableFuture<ResponseEntity<byte[]>> probarPlantillaDiploma(
            @RequestParam String nombreAsistente,
            @RequestParam Long cursoId,
            @RequestParam(required = false) String notaAprobacion,
            @RequestParam(required = false) String asistencia,
            @RequestParam Long idPlantilla,
            HttpSession session) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        Curso curso = ser.cursoPorId(cursoId);
        Plantilla plantilla = ser.plantillaPorId(idPlantilla);

        curso.setPlantillaDiploma(plantilla);

        Alumno alumno = new Alumno();
        alumno.setNombreAsistente(nombreAsistente);
        alumno.setCurso(curso);
        alumno.setNotaAprobacion(notaAprobacion);
        alumno.setAsistencia(asistencia);
        alumno.setId(1L);
        if (curso.getLugarYfechaEmision() == null) {
            curso.setLugarYfechaEmision("Emitido el 11 de febrero de 2026, en Concepcion, Región del Biobío");
        }

        try {
            return servicioGenerarCertificado.funcionParaPruebaDeDiplomas(alumno)
                    .thenApply(fileBytes -> {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_PDF);
                        headers.setContentDispositionFormData("attachment", "certificado.pdf");
                        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        }
    }

    @PostMapping("/probarPlantillaFlyer")
    public CompletableFuture<ResponseEntity<byte[]>> probarPlantillaFlyer(
            @RequestParam Long cursoId,
            @RequestParam Long idPlantilla,
            HttpSession session) {

        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal == null) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        Curso curso = ser.cursoPorId(cursoId);
        Plantilla plantilla = ser.plantillaPorId(idPlantilla);

        curso.setPlantillaFlyer(plantilla);

        try {
            return servicioGenerarCertificado.descargarFlyerServicio(curso)
                    .thenApply(fileBytes -> {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(
                                MediaType.parseMediaType(
                                        "application/vnd.openxmlformats-officedocument.presentationml.presentation"));
                        headers.setContentDispositionFormData("attachment", "flyer.pptx");

                        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        }
    }

    @PostMapping("/dataBaseAlumno/eliminarSeleccionados")
    public ResponseEntity<?> eliminarSeleccionados(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                ser.borrarAlumnoPorId(id);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar");
        }
    }

    @GetMapping("/programarCertificadoMoodleManual/alumnos/{courseId}")
    public ResponseEntity<?> getAlumnosCurso(@PathVariable Long courseId) {
        try {

            // 1. Obtener IDs de usuarios matriculados
            List<Long> userIds = servicioTarea.obtenerUsuariosMatriculados(courseId);
            System.out.println("IDs de usuarios encontrados: {}" + userIds);

            if (userIds.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // 2. Obtener información detallada de usuarios
            Map<Long, JsonNode> userInfoMap = servicioTarea.obtenerUsuariosInfoBatch(userIds);

            List<AlumnoDTO> alumnos = new ArrayList<>();

            for (Long userId : userIds) {
                try {
                    JsonNode userNode = userInfoMap.get(userId);
                    if (userNode == null) {
                        System.out.println("Usuario {} no encontrado en la respuesta" + userId);
                        continue;
                    }
                    System.out.println(userNode);
                    AlumnoDTO dto = new AlumnoDTO();
                    dto.setId(userId);

                    // 3. Procesar nombre (fullname o firstname + lastname)
                    if (userNode.has("fullname") && !userNode.get("fullname").isNull()) {
                        dto.setNombreAsistente(userNode.get("fullname").asText().trim());
                    } else {
                        String firstName = userNode.has("firstname") ? userNode.get("firstname").asText("") : "";
                        String lastName = userNode.has("lastname") ? userNode.get("lastname").asText("") : "";
                        dto.setNombreAsistente((firstName + " " + lastName).trim());
                    }
                    dto.setNombreAsistente(dto.getNombreAsistente().toUpperCase());
                    // 4. Procesar email
                    if (userNode.has("email")) {
                        dto.setCorreo(userNode.get("email").asText());
                    } else {
                        System.out.println("Usuario {} no tiene email" + userId);
                        dto.setCorreo("sin-email@ejemplo.com");
                    }

                    // 5. Obtener nota con manejo de errores
                    try {
                        double nota = (servicioTarea.obtenerPromedioNotas(courseId, userId));
                        dto.setNotaAprobacion(String.valueOf(nota));
                    } catch (Exception e) {
                        System.out.println("Error al obtener nota para usuario {}: {}" + userId +
                                e.getMessage());
                        dto.setNotaAprobacion(String.valueOf(7.0)); // Valor por defecto
                    }

                    alumnos.add(dto);

                } catch (Exception e) {
                    System.out.println("Error procesando usuario {}: {}" + userId +
                            e.getMessage());
                }
            }

            return ResponseEntity.ok(alumnos);

        } catch (Exception e) {
            System.out.println("Error crítico al obtener alumnos: {}" + e.getMessage() +
                    e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al obtener alumnos",
                            "detalle", e.getMessage(),
                            "cursoId", courseId));
        }
    }

    @DeleteMapping("/tareasProgramadas/borrar/{id}")
    public ResponseEntity<Void> borrarTarea(@PathVariable Long id) {
        ser.borrarTareaPorId(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * @GetMapping("/cursoTemporal")
     * public ResponseEntity<List<CursoTemporalDTO>> listAll(){
     * List<CursoTemporal> list = ser.todosLosCursosTemporales();
     * // mapear a DTO simple (o enviar entidad si quieres)
     * List<CursoTemporalDTO> dtos = new ArrayList<>();
     * for(CursoTemporal c : list){
     * CursoTemporalDTO d = CursoTemporalDTO.fromEntity(c);
     * dtos.add(d);
     * }
     * return ResponseEntity.ok(dtos);
     * }
     * 
     * 
     * @PostMapping("/cursoTemporal/busquedaMultiFiltro")
     * public ResponseEntity<List<CursoTemporalDTO>> buscarMultiFiltro(@RequestBody
     * List<Map<String,String>> filtros){
     * List<CursoTemporal> encontrados = ser.buscarConFiltros(filtros);
     * List<CursoTemporalDTO> dtos = new ArrayList<>();
     * for(CursoTemporal c : encontrados) dtos.add(CursoTemporalDTO.fromEntity(c));
     * return ResponseEntity.ok(dtos);
     * }
     */
    @GetMapping("/cursoTemporal/generarQr/{id}")
    public ResponseEntity<?> generarQr(@PathVariable Long id) {
        try {
            Map<String, String> res = ser.generarQrParaCurso(id);
            return ResponseEntity.ok(res);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // Endpoint que descarga el QR en PNG (por id)
    @GetMapping("/cursoTemporal/downloadQr/{id}")
    public ResponseEntity<byte[]> downloadQr(@PathVariable Long id,
            @RequestParam(value = "filename", required = false) String filename) {
        try {
            Map<String, String> res = ser.generarQrParaCurso(id);
            String base64 = res.get("imageBase64");
            byte[] data = Base64.getDecoder().decode(base64);
            if (filename == null || filename.isEmpty()) {
                filename = res.get("nombreCurso") + "_QR.png";
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Crear curso (recibe JSON con request params) y devuelve qr
    @PostMapping("/cursoTemporal/create")
    public ResponseEntity<?> createCursoAndGenerateQr(@RequestBody Map<String, String> params) {
        try {
            Map<String, String> res = ser.createCursoAndGenerateQr(params);
            return ResponseEntity.ok(res);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @GetMapping("/plantillas")
    public ResponseEntity<List<Map<String, Object>>> listPlantillas() {
        List<Plantilla> list = ser.todasLasPlantillas();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Plantilla p : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("nombre", p.getNombreCertificado());
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    @PostMapping("/alumnoTemporalSubir")
    public ResponseEntity<?> recibirAsistencia(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam String rut,
            @RequestParam String id) {
        try {
            Curso curso = ser.cursoPorId(Long.valueOf(ser.decryptId(id)));
            if (!curso.isAsistenciaQr()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error");
            }
            ser.procesarAsistencia(nombre, correo, rut, id);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error");
        }
    }

    @GetMapping("/datosAlumnoTemporal/{idCurso}")
    public List<AlumnoTemporal> obtenerAlumnosPorCurso(
            @PathVariable Long idCurso) {

        Curso curso = ser.cursoPorId(idCurso);
        List<AlumnoTemporal> alumnoTemporals = curso.getAlumnosTemporales();
        return alumnoTemporals;

    }

    @PostMapping("/alumnoTemporal/enviar")
    public ResponseEntity<Map<String, Object>> enviarDatos(
            @RequestParam Long alumnoId,
            @RequestParam Long cursoId,
            @RequestParam String asistencia,
            @RequestParam String nota) {

        ser.alumnoVerificado(alumnoId, cursoId, asistencia, nota);

        Map<String, Object> response = new HashMap<>();
        response.put("alumnoId", alumnoId);
        response.put("asistencia", asistencia);
        response.put("nota", nota);
        response.put("cursoId", cursoId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/alumnoTemporal/{idAlumno}")
    public ResponseEntity<Void> borrarAlumno(@PathVariable Long idAlumno) {
        ser.borrarAlumnoTemporalPorId(idAlumno);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/datosCliente")
    public List<ClienteDTO> getDatosCliente(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            List<Cliente> cliente = ser.todosLosClientes();
            return cliente.stream().map(ClienteDTO::new).collect(Collectors.toList());
        }
        return null;
    }

    @PostMapping("/datosCliente/busquedaMultiFiltro")
    public List<ClienteDTO> busquedaMultiFiltroCliente(
            @RequestBody List<Map<String, String>> filtros) {
        Page<Cliente> resultado = ser.buscarConMultiplesFiltrosCliente(filtros);
        return resultado.getContent().stream().map(ClienteDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/clientes/buscar")
    @ResponseBody
    public List<ClienteDTO> buscarClientes(@RequestParam String q) {
        return repoCliente.findTop10ByNombreClienteContainingIgnoreCase(q).stream().map(ClienteDTO::new)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/databaseCliente/borrarCliente/{id}")
    public ResponseEntity<?> borrarCliente(@PathVariable Long id) {
        try {
            ser.borrarCliente(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar curso");
        }
    }

    @GetMapping("/datosRelator")
    public List<RelatorDTO> getDatosRelator(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            List<Relator> relatores = ser.todosLosRelatores();
            return relatores.stream().map(RelatorDTO::new).collect(Collectors.toList());
        }
        return null;
    }

    @PostMapping("/datosRelator/busquedaMultiFiltro")
    public List<RelatorDTO> busquedaMultiFiltroRelator(@RequestBody List<Map<String, String>> filtros,
            HttpSession session) {
        Admin usuario = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuario != null) {
            Page<Relator> resultado = ser.buscarConMultiplesFiltrosRelator(filtros);
            return resultado.getContent().stream().map(RelatorDTO::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @GetMapping("/relator/buscar")
    @ResponseBody
    public List<RelatorDTO> buscarRelator(@RequestParam String q) {
        return repoRel.findTop10ByNombreContainingIgnoreCase(q).stream().map(RelatorDTO::new)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/dataBaseRelator/borrarRelator/{id}")
    public ResponseEntity<?> borrarRelator(@PathVariable Long id) {
        try {
            ser.borrarRelator(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar relator");
        }
    }

    @PostMapping("/dataBaseCurso/nuevoCurso")
    public ResponseEntity<byte[]> crearNuevoCurso(

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

            RedirectAttributes redirectAttrs) {

        try {

            LocalDate fechaCurso = LocalDate.parse(fecha);
            LocalTime horaInicio = LocalTime.parse(horaI);
            LocalTime horaFin = LocalTime.parse(horaF);

            LocalDateTime fechaDeInicio = LocalDateTime.of(fechaCurso, horaInicio);
            LocalDateTime fechaDeFinalizacion = LocalDateTime.of(fechaCurso, horaFin);

            Cliente cliente = ser.clientePorId(clienteId);

            Relator relator = ser.buscarRelatorPorId(relatorId);
            relator.setHorasTrabajados(relator.getHorasTrabajados() + horasRelatorCurso);
            ser.guardarRelator(relator);
            Plantilla plantillaDiploma = ser.plantillaPorId(plantillaDipId);

            Plantilla plantillaFlyer = ser.plantillaPorId(plantillaFlyId);

            Curso curso = new Curso();
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
            curso.setAsistenciaQr(true);
            ser.guardarCurso(curso);

            byte[] flyerBytes = servicioGenerarCertificado
                    .descargarFlyerServicio(curso)
                    .get();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=Flyer-" + curso.getNombreCurso() + ".pptx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(flyerBytes);

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/datosCurso")
    public List<CursoDTO> getDatosCurso(HttpSession session) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            Page<Curso> cursos = ser.todosLosCursos();
            return cursos.getContent().stream().map(CursoDTO::new).collect(Collectors.toList());
        }
        return null;
    }

    @GetMapping("/curso/buscar")
    @ResponseBody
    public List<CursoDTO> buscarCurso(@RequestParam String q) {
        return repoCurso.findTop10ByNombreCursoContainingIgnoreCase(q).stream().map(CursoDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/datosCurso/cliente/{id}")
    public List<CursoDTO> getDatosCursoPorCliente(HttpSession session, @PathVariable("id") Long id) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            List<Curso> cursos = ser.cursosPorCliente(id);
            return cursos.stream().map(CursoDTO::new).collect(Collectors.toList());
        }
        return null;
    }

    @GetMapping("/datosCurso/relator/{id}")
    public List<CursoDTO> getDatosCursoPorRelator(HttpSession session, @PathVariable("id") Long id) {
        Admin usuarioTemporal = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuarioTemporal != null) {
            List<Curso> cursos = ser.cursosPorRelator(id);
            return cursos.stream().map(CursoDTO::new).collect(Collectors.toList());
        }
        return null;
    }

    @PostMapping("/datosCurso/busquedaMultiFiltro")
    public List<CursoDTO> busquedaMultiFiltroCurso(@RequestBody List<Map<String, String>> filtros,
            HttpSession session) {
        Admin usuario = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuario != null) {
            Page<Curso> resultado = ser.buscarConMultiplesFiltrosCurso(filtros);
            return resultado.getContent().stream().map(CursoDTO::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @PostMapping("/datosCurso/busquedaMultiFiltro/cliente/{id}")
    public List<CursoDTO> busquedaMultiFiltroCursoPorCliente(@RequestBody List<Map<String, String>> filtros,
            HttpSession session, @PathVariable("id") Long id) {
        Admin usuario = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuario != null) {
            Page<Curso> resultado = ser.buscarConMultiplesFiltrosCursoPorCliente(filtros, id);
            return resultado.getContent().stream().map(CursoDTO::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @PostMapping("/dataBaseCurso/descargarFlyer")
    public ResponseEntity<byte[]> descargarFlyer(@RequestBody Map<String, Long> body) throws Exception {

        Long id = body.get("id");

        Curso curso = ser.cursoPorId(id);

        byte[] flyerBytes = servicioGenerarCertificado.descargarFlyerServicio(curso).get();
        // .get() espera el resultado del CompletableFuture

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        headers.setContentDisposition(ContentDisposition
                .attachment()
                .filename("Flyer-" + curso.getNombreCurso() + ".pptx")
                .build());

        return new ResponseEntity<>(flyerBytes, headers, HttpStatus.OK);
    }

    @DeleteMapping("/dataBaseCurso/borrarCurso/{id}")
    public ResponseEntity<String> borrarCurso(@PathVariable Long id) {

        try {
            ser.borrarCurso(id);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar cliente");
        }

    }

    @PostMapping("/datosCurso/busquedaMultiFiltro/relator/{id}")
    public List<CursoDTO> busquedaMultiFiltroCursoPorRelator(@RequestBody List<Map<String, String>> filtros,
            HttpSession session, @PathVariable("id") Long id) {
        Admin usuario = (Admin) session.getAttribute("usuarioEnSesion");
        if (usuario != null) {
            Page<Curso> resultado = ser.buscarConMultiplesFiltrosCursoPorRelator(filtros, id);
            return resultado.getContent().stream().map(CursoDTO::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @GetMapping("/plantilla/buscar")
    @ResponseBody
    public List<Plantilla> buscarPlantilla(@RequestParam String q, @RequestParam String t) {
        return repoPlanti.findTop10ByNombreCertificadoContainingIgnoreCaseAndTipo(q, t);
    }

}
