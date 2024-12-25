package com.vt.createmanagesubmit.controladores;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.servicios.Servicio;
import com.vt.createmanagesubmit.servicios.ServicioArchivos;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api")
public class ControladorApi {

    @Autowired
    @Lazy
    private Servicio ser;

    @Autowired
    @Lazy
    private ServicioArchivos servicioAr;

    @GetMapping("/datos")
    public List<AlumnoDTO> getDatos() {
        Page<Alumno> alumnos = ser.todosLosAlumnos();
        return alumnos.getContent().stream().map(AlumnoDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/datos/busquedaAlumno")
    public List<AlumnoDTO> getDatosBusquedaAlumno(@RequestParam String filtro, @RequestParam String busqueda) {
        Page<Alumno> alumnos = ser.buscarAlumnosPorCriterio(filtro, busqueda); // Implementa este método en tu servicio
        return alumnos.getContent().stream().map(AlumnoDTO::new).collect(Collectors.toList());
    }

    @PostMapping(value = "/uploadAlumnoExcel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> subirExcel(@RequestPart("file") MultipartFile file,@RequestParam(value = "estadoDiplomaExcel", required = false) String estadoDiplomaExcel,@RequestParam(value = "plantillaNombre")String plantilla,@RequestParam(value ="estadoExcel")String estadoExcel) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }

        try {
            // Guardar el archivo temporalmente
            Path tempDir = Files.createTempDirectory("");

            File tempFile = tempDir.resolve(file.getOriginalFilename()).toFile();
            file.transferTo(tempFile);

            // Procesar el archivo
            servicioAr.leerExcelYGuardarEnBD(tempFile.getAbsolutePath(),estadoDiplomaExcel,plantilla,estadoExcel);

            // Eliminar el archivo temporal
            tempFile.delete();

            return ResponseEntity.ok("Archivo procesado exitosamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el archivo");
        }
    }

    @GetMapping("/generateCertificates")
    public ResponseEntity<?> generateCertificates() {
        try {
            servicioAr.generateCertificatesAll();
            return ResponseEntity.ok("Certificados generados exitosamente.");
        } catch(Exception e) {
            return ResponseEntity.status(500).body("Error al generar certificados: " + e.getMessage());
        }
    }

    @PostMapping("/enviarRestantes")
    public RedirectView enviarRestantes() {
        try {
            servicioAr.generateCertificatesAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
        return new RedirectView("/dataBaseAlumno");
    }
    
    @GetMapping("/generateCert/{id}")
    public RedirectView certificadoPorIdApi(@PathVariable("id")Long id) {
        try {
            servicioAr.generateCertificatesById(id);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new RedirectView("/dataBaseAlumno/alumno/"+id);
    }
    
    @GetMapping("/dataBaseAlumno/alumno/{id}/borrar")
    public RedirectView borrarAlumnoIdApi(@PathVariable("id")Long id) {
        ser.borrarAlumnoPorId(id);
        return new RedirectView("/dataBaseAlumno");
    }
    
        
}

