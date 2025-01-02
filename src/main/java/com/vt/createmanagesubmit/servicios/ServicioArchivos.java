package com.vt.createmanagesubmit.servicios;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
// Otras importaciones necesarias
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.JodConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vt.createmanagesubmit.dto.AlumnoDTO;
import com.vt.createmanagesubmit.exceptions.CertificateGenerationException;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;
import com.vt.createmanagesubmit.repositorios.RepositorioPlantillas;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class ServicioArchivos {

    @Autowired
    private RepositorioAlumnos alumnoRepo;

    @Autowired
    private RepositorioPlantillas plantillaRepo;

    @Autowired
    @Lazy
    private Servicio servicio;

    @Autowired
    private JavaMailSender javaMailSender;

    String correoEmpresa = Servicio.CORREO_EMPRESA;

    @Async
    public CompletableFuture<Void> leerExcelYGuardarEnBD(byte[] fileBytes, String estadoDiplomaExcel, String plantilla, String estadoExcel) throws IOException {
        try (InputStream fileInputStream = new ByteArrayInputStream(fileBytes)){
            Workbook workbook = WorkbookFactory.create(fileInputStream);

        // Iterar sobre las hojas del libro
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet hoja = workbook.getSheetAt(i);

                // Obtener el encabezado (asumiendo que está en la primera fila)
                Row encabezado = hoja.getRow(0);
                Map<Integer, String> mapaColumnas = new HashMap<>();

                // Mapear los nombres de las columnas a sus índices
                for (Cell celda : encabezado) {
                    int indiceColumna = celda.getColumnIndex();
                    String nombreColumna = celda.getStringCellValue();
                    mapaColumnas.put(indiceColumna, nombreColumna.trim());
                }

                // Iterar sobre las filas de datos (empezando desde la segunda fila)
                for (int filaIndex = 1; filaIndex <= hoja.getLastRowNum(); filaIndex++) {
                    Row fila = hoja.getRow(filaIndex);
                    if (fila == null) {
                        continue; // Saltar filas vacías
                    }

                    Alumno alumno = new Alumno();

                    // Iterar sobre las celdas de la fila
                    for (Cell celda : fila) {
                        int indiceColumna = celda.getColumnIndex();
                        String nombreColumna = mapaColumnas.get(indiceColumna);
                        if (nombreColumna != null) {
                            asignarValorAtributo(alumno, nombreColumna, celda);
                        }
                    }

                    if (alumno.getNombreAsistente() != null) {
                        if (!plantilla.trim().equals("excel")) {
                            Optional<Plantilla> plantillaOp = servicio.plantillaPorNombre(plantilla);
                            if (plantillaOp.isPresent()) {
                                Plantilla plantillaAlumnoEstablecido = plantillaOp.get();
                                alumno.setPlantilla(plantillaAlumnoEstablecido);
                            }
                        }

                        if (alumno.getCorreo() == null || alumno.getCorreo().trim().isEmpty() || alumno.getCorreo().trim().isBlank()) {
                            alumno.setCorreo("javito12ulloa@gmail.com");
                        }

                        if (alumno.getEstado() == null) {
                            alumno.setEstado("revisionManual");
                        }

                        if (!alumno.getEstado().equals("Eexcel")) {
                            if (estadoExcel.equals("Eauto")) {
                                alumno = servicio.funcionEstadoManual(alumno);
                            } else {
                                alumno.setEstado(estadoExcel);
                            }
                        }

                        if (!estadoDiplomaExcel.equals("diploExcel")) {
                            if (estadoDiplomaExcel.equals("noEnviar")) {
                                alumno.setDiploma("noEnviado");
                            } else if (estadoDiplomaExcel.equals("enviarApro")) {
                                if (alumno.getEstado().equals("aprobado")) {
                                    alumno.setDiploma("enviado");
                                }
                            } else if (estadoDiplomaExcel.equals("enviarTodos")) {
                                alumno.setDiploma("enviado");
                            }
                        }

                        alumnoRepo.save(alumno);

                        if (estadoDiplomaExcel.equals("enviarTodos")) {
                            try {
                                generateCertificateForAlumno(alumno);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (estadoDiplomaExcel.equals("enviarApro")) {
                            if (alumno.getEstado().equals("aprobado")) {
                                try {
                                    generateCertificateForAlumno(alumno);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
            workbook.close();
            fileInputStream.close();
        } catch (Exception ex) {
            throw new IOException("Error al procesar el excel, verifique los datos.",ex);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    private void asignarValorAtributo(Alumno alumno, String nombreColumna, Cell celda) {
        // Obtén el valor de la celda como String
        String valorCelda = obtenerValorCeldaComoString(celda);

        switch (nombreColumna.toLowerCase()) {
            case "nº":
            case "número":
                break;
            case "nombre asistente":
                alumno.setNombreAsistente(valorCelda);
                break;
            case "nombre curso":
                alumno.setNombreCurso(valorCelda);
                break;
            case "dias curso":
                alumno.setDiasCursos(valorCelda);
                break;
            case "nº de horas":
            case "numero horas":
                alumno.setNumeroHoras(valorCelda);
                break;
            case "nº correlativo interno":
            case "numero correlativo interno":
                alumno.setNumeroCorrelativoInterno(valorCelda);
                break;
            case "cliente":
                alumno.setCliente(valorCelda);
                break;
            case "obra":
                alumno.setObra(valorCelda);
                break;
            case "codigo":
                alumno.setCodigo(valorCelda);
                break;
            case "nota aprobación":
            case "nota aprobacion":
                alumno.setNotaAprovacion(valorCelda);
                break;
            case "relator":
                alumno.setRelator(valorCelda);
                break;
            case "asistencia":
                alumno.setAsistencia(valorCelda);
                break;
            case "estado":
                if (valorCelda.trim().equalsIgnoreCase("aprobado")) {
                    alumno.setEstado("aprobado");
                } else {
                    alumno.setEstado("noAprobado");
                }
                break;
            case "diploma":
                if (valorCelda.trim().equalsIgnoreCase("no enviado")) {
                    alumno.setDiploma("noEnviado");
                } else if (valorCelda.trim().equalsIgnoreCase("enviado")) {
                    alumno.setDiploma("enviado");
                } else {
                    alumno.setDiploma("revisionManual");
                }
                break;
            case "rut":
                alumno.setRut(valorCelda);
                break;
            case "correo":
                alumno.setCorreo(valorCelda);
                break;
            case "plantilla":
                Optional<Plantilla> plantillaAlumnoOptional = servicio.plantillaPorNombre(valorCelda);
                if (plantillaAlumnoOptional.isPresent()) {
                    Plantilla plantillaAlumno = plantillaAlumnoOptional.get();
                    alumno.setPlantilla(plantillaAlumno);
                } else {
                    Optional<Plantilla> plantillaErrorOptional = servicio.plantillaPorNombre("Error en encontrar plantilla");
                    if (plantillaErrorOptional.isPresent()) {
                        Plantilla plantillaAlumno = plantillaErrorOptional.get();
                        alumno.setPlantilla(plantillaAlumno);
                    } else {
                        Plantilla plantillaAlumno = new Plantilla();
                        plantillaAlumno.setNombreCertificado("Error en encontrar plantilla");
                        plantillaRepo.save(plantillaAlumno);
                        alumno.setPlantilla(plantillaAlumno);
                    }
                }
                break;
            default:
                // Ignorar columnas no reconocidas
                break;
        }
    }

    private String obtenerValorCeldaComoString(Cell celda) {
        switch (celda.getCellType()) {
            case STRING:
                return celda.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(celda)) {
                    Date date = celda.getDateCellValue();
                    return new SimpleDateFormat("yyyy-MM-dd").format(date);
                } else {
                    return String.valueOf(celda.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(celda.getBooleanCellValue());
            case FORMULA:
                FormulaEvaluator evaluator = celda.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                CellValue cellValue = evaluator.evaluate(celda);
                return cellValue.formatAsString();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    
    public void generateCertificatesAll() throws Exception {
        List<Alumno> alumnos = alumnoRepo.findAllByDiplomaAndEstado("noEnviado", "aprobado");
        for (Alumno alumno : alumnos) {
            generateCertificateForAlumno(alumno);
            alumno.setDiploma("enviado");
            alumnoRepo.save(alumno);
        }
    }

    
    public void generateCertificatesById(Long id) throws Exception {
        Alumno alumno = alumnoRepo.findById(id).orElse(null);
        if (alumno != null) {
            try {
                generateCertificateForAlumno(alumno);
                alumno.setDiploma("enviado");
                alumnoRepo.save(alumno);
            } catch (Exception ex) {
                throw new CertificateGenerationException("Ocurrió un error al generar el certificado.");
            }
            
        }
    }

    @Async
    public CompletableFuture<Void> generateCertificateForAlumno(Alumno alumno) throws Exception {
        // Obtén la plantilla asociada al alumno
        Plantilla plantilla = alumno.getPlantilla();
        if (plantilla == null) {
            throw new Exception("No hay una plantilla asociada al Alumno " + alumno.getNombreAsistente());
        }

        // Carga la plantilla PPTX desde pathArchivo
        String templatePath = plantilla.getPathArchivo();

        // Carga el archivo PPTX usando Apache POI
        XMLSlideShow ppt;
        try (FileInputStream inputStream = new FileInputStream(templatePath)) {
            ppt = new XMLSlideShow(inputStream);
        }

        // Crea un mapa de los datos del alumno que se usarán para rellenar las shapes
        Map<String, String> alumnoData = new HashMap<>();
        alumnoData.put("nombreAsistente", alumno.getNombreAsistente());
        alumnoData.put("nombreCurso", alumno.getNombreCurso());
        alumnoData.put("numeroHoras", alumno.getNumeroHoras());
        alumnoData.put("notaAprovacion", alumno.getNotaAprovacion());
        alumnoData.put("diasCursos", alumno.getDiasCursos());
        alumnoData.put("relator", alumno.getRelator());
        alumnoData.put("asistencia", alumno.getAsistencia());

        // Modifica las shapes con los nombres correspondientes
        for (XSLFSlide slide : ppt.getSlides()) {
            for (XSLFShape shape : slide.getShapes()) {
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape textShape = (XSLFTextShape) shape;
                    String shapeName = textShape.getShapeName();
                    if (alumnoData.containsKey(shapeName)) {
                        String textToSet = alumnoData.get(shapeName);
                        textShape.clearText(); // Limpia el texto existente

                // Añade un nuevo párrafo y establece el texto
                        XSLFTextParagraph paragraph = textShape.addNewTextParagraph();
                        XSLFTextRun textRun = paragraph.addNewTextRun();
                        textRun.setText(textToSet != null ? textToSet : "");

                    // Aplica el formato basado en el nombre del shape
                        switch (shapeName) {
                            case "nombreAsistente":
                                textRun.setFontFamily("Arial");
                                textRun.setFontSize(24.0); // Tamaño específico para nombreAsistente
                                break;
                            case "nombreCurso":
                                textRun.setFontFamily("Times New Roman");
                                textRun.setFontSize(20.0); // Tamaño específico para nombreCurso
                                break;
                            // Agrega más casos según sea necesario para otros shapes
                            default:
                                textRun.setFontFamily("Arial");
                                textRun.setFontSize(12.0); // Tamaño por defecto
                                break;

                        }
                    }
                }
            }
        }

        // Guarda el PPTX modificado en un archivo temporal
        String tempPptxPath = "temp/" + alumno.getId() + ".pptx";
        File tempDir = new File("temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(tempPptxPath)) {
            ppt.write(out);
        }

        // Convierte el PPTX a PDF usando JODConverter
        String tempPdfPath = "temp/" + alumno.getId() + ".pdf";
        LocalOfficeManager officeManager = LocalOfficeManager.builder()
                .install()
                .build();
        try {
            officeManager.start();
            JodConverter.convert(new File(tempPptxPath))
                    .to(new File(tempPdfPath))
                    .execute();
        } catch (OfficeException e) {
            throw new OfficeException ("Ocurrió un error inesperado al generar el PPT",e);
        } finally {
            if (officeManager != null) {
                officeManager.stop();
            }
        }

        // Leer el PDF generado como array de bytes
        byte[] pdfBytes = Files.readAllBytes(Paths.get(tempPdfPath));

        // Eliminar los archivos temporales
        new File(tempPptxPath).delete();
        new File(tempPdfPath).delete();

        // Generar código QR con la URL y el ID encriptado
        String encryptedId = encryptStudentId(alumno.getId().toString());
        String qrCodeText = "http://localhost:8080/api/generar/" + encryptedId;
        System.out.println(qrCodeText);
        ByteArrayOutputStream qrCodeOutputStream = generateQRCodeImage(qrCodeText, 200, 200);
        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        // Enviar correo electrónico al alumno con el PDF y el código QR como adjuntos
        sendEmailWithAttachments(alumno.getCorreo(), "Certificado de Curso", "Estimado " + alumno.getNombreAsistente() + ", adjuntamos su certificado y código QR.", pdfBytes, qrCodeBytes);
        return CompletableFuture.completedFuture(null);
    }


    private ByteArrayOutputStream generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", pngOutputStream);
        return pngOutputStream;
    }

    private void sendEmailWithAttachments(String toEmail, String subject, String body, byte[] pdfBytes, byte[] qrCodeBytes) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body, true); // true indica que es formato HTML

        // Adjuntar el PDF
        ByteArrayResource pdfResource = new ByteArrayResource(pdfBytes);
        helper.addAttachment("certificado.pdf", pdfResource);

        // Adjuntar el código QR
        ByteArrayResource qrCodeResource = new ByteArrayResource(qrCodeBytes);
        helper.addAttachment("codigoQR.png", qrCodeResource);

        javaMailSender.send(message);

        
    }
    
    @Async
    private String encryptStudentId(String studentId) throws Exception {
        // Define una clave secreta para la encriptación
        String secretKey = "mySuperSecretKey"; // Debes usar una clave más segura y almacenarla de forma segura
        MessageDigest sha = null;
        try {
            byte[] key = secretKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // Usar solo los primeros 128 bits
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(studentId.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error al encriptar el ID del alumno.");
        }
    }

    @Async
    private Long decryptStudentId(String encryptedId) throws Exception {
        // Define una clave secreta para la encriptación
        String secretKey = "mySuperSecretKey"; // Usa una clave más segura y almacénala adecuadamente
    
        // Inicia la lógica de desencriptación
        try {
            byte[] decodedEncryptedId = Base64.getDecoder().decode(encryptedId); // Decodifica el ID en Base64
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] key = secretKey.getBytes("UTF-8");
            sha.update(key);
            key = sha.digest();
            key = Arrays.copyOf(key, 16); // Usar solo los primeros 128 bits
    
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
    
            // Desencripta el ID
            byte[] decrypted = cipher.doFinal(decodedEncryptedId);
    
            // Convierte el byte array desencriptado en un Long
            String decryptedString = new String(decrypted, "UTF-8");
            return Long.parseLong(decryptedString); // Devuelve el Long desencriptado
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error al desencriptar el ID del alumno.");
        }
    }
    
    @Async
    @Transactional
    public CompletableFuture<Void> generateCertificateQR(String IdEncriptada, HttpServletResponse response) throws Exception {
        Long alumnoId = decryptStudentId(IdEncriptada);
        // Obtén el alumno por ID
        Alumno alumno = alumnoRepo.findById(alumnoId).orElseThrow(() -> new Exception("Alumno no encontrado con ID " + alumnoId));

        // Obtén la plantilla asociada al alumno
        Plantilla plantilla = alumno.getPlantilla();
        if (plantilla == null) {
            throw new Exception("No hay una plantilla asociada al Alumno con ID " + alumno.getId());
        }

        // Carga la plantilla PPTX desde pathArchivo
        String templatePath = plantilla.getPathArchivo();

        // Carga el archivo PPTX usando Apache POI
        XMLSlideShow ppt;
        try (FileInputStream inputStream = new FileInputStream(templatePath)) {
            ppt = new XMLSlideShow(inputStream);
        }

        // Crea un mapa de los datos del alumno que se usarán para rellenar las shapes
        Map<String, String> alumnoData = new HashMap<>();
        alumnoData.put("nombreAsistente", alumno.getNombreAsistente());
        alumnoData.put("nombreCurso", alumno.getNombreCurso());
        alumnoData.put("numeroHoras", alumno.getNumeroHoras());
        alumnoData.put("notaAprovacion", alumno.getNotaAprovacion());
        alumnoData.put("diasCursos", alumno.getDiasCursos());
        alumnoData.put("relator", alumno.getRelator());
        alumnoData.put("asistencia", alumno.getAsistencia());

        // Modifica las shapes con los nombres correspondientes
        for (XSLFSlide slide : ppt.getSlides()) {
            for (XSLFShape shape : slide.getShapes()) {
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape textShape = (XSLFTextShape) shape;
                    String shapeName = textShape.getShapeName();
                    if (alumnoData.containsKey(shapeName)) {
                        String textToSet = alumnoData.get(shapeName);
                        textShape.setText(textToSet != null ? textToSet : "");
                    }
                }
            }
        }

        // Guarda el PPTX modificado en un archivo temporal
        String tempPptxPath = "temp/" + alumno.getId() + ".pptx";
        File tempDir = new File("temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(tempPptxPath)) {
            ppt.write(out);
        }

        // Convierte el PPTX a PDF usando JODConverter
        String tempPdfPath = "temp/" + alumno.getId() + ".pdf";
        LocalOfficeManager officeManager = LocalOfficeManager.builder()
                .install()
                .build();
        try {
            officeManager.start();
            JodConverter.convert(new File(tempPptxPath))
                    .to(new File(tempPdfPath))
                    .execute();
        } catch (OfficeException e) {
            e.printStackTrace();
        } finally {
            if (officeManager != null) {
                officeManager.stop();
            }
        }

        // Leer el PDF generado como array de bytes
        byte[] pdfBytes = Files.readAllBytes(Paths.get(tempPdfPath));

        // Eliminar los archivos temporales
        new File(tempPptxPath).delete();
        new File(tempPdfPath).delete();

        // Configurar la respuesta HTTP para enviar el PDF como archivo descargable
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"certificado-" + alumno.getId() + ".pdf\"");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
        response.getOutputStream().close();
        return CompletableFuture.completedFuture(null);

    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        // Obtener la lista de alumnos
        List<Alumno> alumnos = alumnoRepo.findAll();

        // Crear un nuevo libro de Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Alumnos");

        // Crear la fila de cabecera
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Nombre Asistente", "Nombre Curso", "Días Curso", "Número Horas", "Correlativo Interno", 
                            "Cliente", "Obra", "Código", "Nota Aprobación", "Relator", "Asistencia", "Estado", 
                            "Diploma", "RUT", "Correo", "Plantilla"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Llenar los datos de los alumnos
        int rowNum = 1;
        for (Alumno alumno : alumnos) {
            AlumnoDTO alumnoDTO = new AlumnoDTO(alumno);
            Row row = sheet.createRow(rowNum++);

            // Usar una función auxiliar para manejar los nulls
            row.createCell(0).setCellValue(safeGet(alumnoDTO.getNombreAsistente()));
            row.createCell(1).setCellValue(safeGet(alumnoDTO.getNombreCurso()));
            row.createCell(2).setCellValue(safeGet(alumnoDTO.getDiasCursos()));
            row.createCell(3).setCellValue(safeGet(alumnoDTO.getNumeroHoras()));
            row.createCell(4).setCellValue(safeGet(alumnoDTO.getNumeroCorrelativoInterno()));
            row.createCell(5).setCellValue(safeGet(alumnoDTO.getCliente()));
            row.createCell(6).setCellValue(safeGet(alumnoDTO.getObra()));
            row.createCell(7).setCellValue(safeGet(alumnoDTO.getCodigo()));
            row.createCell(8).setCellValue(safeGet(alumnoDTO.getNotaAprovacion()));
            row.createCell(9).setCellValue(safeGet(alumnoDTO.getRelator()));
            row.createCell(10).setCellValue(safeGet(alumnoDTO.getAsistencia()));
            row.createCell(11).setCellValue(safeGet(alumnoDTO.getEstado()));
            row.createCell(12).setCellValue(safeGet(alumnoDTO.getDiploma()));
            row.createCell(13).setCellValue(safeGet(alumnoDTO.getRut()));
            row.createCell(14).setCellValue(safeGet(alumnoDTO.getCorreo()));
            row.createCell(15).setCellValue(safeGet(alumnoDTO.getPlantilla()));
        }

        // Configuración de la respuesta HTTP para descargar el archivo Excel
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=alumnos.xlsx");

        // Escribir el archivo Excel en la respuesta HTTP
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // Función auxiliar para manejar nulls y devolver un espacio vacío
    private String safeGet(String value) {
        return value == null ? "" : value;
    }
}

    
