package com.vt.createmanagesubmit.controladores;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vt.createmanagesubmit.servicios.ServicioArchivos;

@RestController
@RequestMapping("/api/excel")
public class ControladorPrueba {

    @Autowired
    private ServicioArchivos servicioAr;
/* 
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> subirExcel(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }

        try {
            // Guardar el archivo temporalmente
            Path tempDir = Files.createTempDirectory("");

            File tempFile = tempDir.resolve(file.getOriginalFilename()).toFile();
            file.transferTo(tempFile);

            // Procesar el archivo
            servicioAr.leerExcelYGuardarEnBD(tempFile.getAbsolutePath());

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
            servicioAr.generateCertificates();
            return ResponseEntity.ok("Certificados generados exitosamente.");
        } catch(Exception e) {
            return ResponseEntity.status(500).body("Error al generar certificados: " + e.getMessage());
        }
    }
        */
}

