package com.vt.createmanagesubmit.servicios;

import java.awt.Font;
import java.awt.Color;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
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
    @Lazy
    private ServicioApi servicioApi;

    @Autowired
    private JavaMailSender javaMailSender;

    String correoEmpresa = Servicio.CORREO_EMPRESA;

    @Async
    public CompletableFuture<Void> leerExcelYGuardarEnBD(byte[] fileBytes, String estadoDiplomaExcel, String plantilla, String estadoExcel, String rutificador) throws IOException {
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
                            asignarValorAtributo(alumno, nombreColumna, celda, rutificador);
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

                        if (alumno.getEstado() == null || alumno.getEstado().trim().isEmpty()) {
                            alumno.setEstado("revisionManual");
                        }

                        if (!estadoExcel.equals("Eexcel")) {
                            if (estadoExcel.equals("Eauto")) {
                                alumno = servicio.funcionEstadoManual(alumno);
                            } else {
                                alumno.setEstado(estadoExcel);
                            }
                        }

                        if (alumno.getEstado() == null || alumno.getEstado().trim().isEmpty() || alumno.getEstado().trim().equals("Eexcel")) {
                            alumno.setEstado("revisionManual");
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
                        if(rutificador.trim().equals("rutiTodos")){
                            if(!alumno.getRut().trim().isEmpty() && alumno.getRut() != null){
                                String rutFormateado = formatearRut(alumno.getRut());
                                String nombreRutificado = servicioApi.obtenerNombrePorRut(rutFormateado);
                                if(!nombreRutificado.trim().equals("nombreNoEncontrado")){
                                    alumno.setNombreAsistente(nombreRutificado);
                                }
        
                            }
                        }

                        if(alumno.getPlantilla() == null){
                            Optional<Plantilla> optPlantilla = servicio.plantillaPorNombre("Error en encontrar plantilla");
                            if(optPlantilla.isPresent()){
                                Plantilla plantilla3 = optPlantilla.get();
                                alumno.setPlantilla(plantilla3);
                            }else{
                                Plantilla plantilla3 = servicio.plantillaPorId(1L);
                                alumno.setPlantilla(plantilla3);
                            }
                        }

                        if(alumno.getDiploma() == null || alumno.getDiploma().trim().isEmpty()){
                            alumno.setDiploma("noEnviado");
                        }

                        alumnoRepo.save(alumno);

                        if (estadoDiplomaExcel.equals("enviarTodos")) {
                            try {
                                generateCertificateForAlumno(alumno);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (estadoDiplomaExcel.equals("enviarApro")) {
                            if (alumno.getEstado().equals("aprobado")&&alumno.getDiploma().equals("noEnviado")) {
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

    private void asignarValorAtributo(Alumno alumno, String nombreColumna, Cell celda, String rutificar) {
        // Obtén el valor de la celda como String
        String valorCelda = obtenerValorCeldaComoString(celda);

        switch (nombreColumna.toLowerCase()) {
            case "nº":
            case "número":
                break;
            case "nombre asistente":
                valorCelda = servicioApi.formatearNombre(valorCelda);
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
                String rutForma = formatearRut(valorCelda);
                alumno.setRut(rutForma);
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
            case "rutificador":
            case "Rutificador":
                if(rutificar.trim().equals("rutiExcel")){
                    if((valorCelda.trim().equalsIgnoreCase("si")) && (!alumno.getRut().trim().isEmpty() && alumno.getRut() != null)){
                        String rutFormateado = formatearRut(alumno.getRut());
                        String nombreRutificado = servicioApi.obtenerNombrePorRut(rutFormateado);
                        if(!nombreRutificado.trim().equals("nombreNoEncontrado")){
                            alumno.setNombreAsistente(nombreRutificado);
                        }

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
                    double valorNumerico = celda.getNumericCellValue();
                    if (valorNumerico == Math.floor(valorNumerico)) {
                    // Si el número no tiene decimales (entero), lo convertimos sin decimales
                        return String.format("%.0f", valorNumerico);
                    } else {
                    // Si tiene decimales, se conserva el formato decimal
                        return String.valueOf(valorNumerico);
                }
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

    public String formatearRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return rut;  
        }

        
        String rutSinPuntos = rut.replaceAll("\\.", "");

        
        if (!rutSinPuntos.contains("-")) {
            int largoRut = rutSinPuntos.length();
            if (largoRut > 1) {
                rutSinPuntos = rutSinPuntos.substring(0, largoRut - 1) + "-" + rutSinPuntos.charAt(largoRut - 1);
            }
        }

        return rutSinPuntos;
    }

    public void generateCertificatesAll() throws Exception {
        List<Alumno> alumnos = alumnoRepo.findAllByDiplomaAndEstado("noEnviado", "aprobado");
        Optional<Plantilla> optPlantilla = servicio.plantillaPorNombre("Error en encontrar plantilla");
        Plantilla plantillaError = new Plantilla();
        if(optPlantilla.isPresent()){
            plantillaError = optPlantilla.get();
        }
        for (Alumno alumno : alumnos) {
            if(alumno.getPlantilla() != plantillaError){
                generateCertificateForAlumno(alumno);
                alumno.setDiploma("enviado");
                alumnoRepo.save(alumno);
            }
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

        // Crea un mapa de los datos del alumno que se usarán para reemplazar en los placeholders
        Map<String, String> alumnoData = new HashMap<>();
        alumnoData.put("nombreAsistente", alumno.getNombreAsistente());
        alumnoData.put("nombreCurso", alumno.getNombreCurso());
        alumnoData.put("numeroHoras", alumno.getNumeroHoras());
        alumnoData.put("notaAprovacion", alumno.getNotaAprovacion());
        alumnoData.put("diasCursos", alumno.getDiasCursos());
        alumnoData.put("relator", alumno.getRelator());
        alumnoData.put("asistencia", alumno.getAsistencia());

        // Modifica las shapes reemplazando los placeholders ${atributo} por los valores correspondientes
        for (XSLFSlide slide : ppt.getSlides()) {
            for (XSLFShape shape : slide.getShapes()) {
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape textShape = (XSLFTextShape) shape;
                    List<XSLFTextParagraph> paragraphs = textShape.getTextParagraphs();
                    for (XSLFTextParagraph paragraph : paragraphs) {
                        List<XSLFTextRun> textRuns = paragraph.getTextRuns();
                        for (XSLFTextRun textRun : textRuns) {
                            String text = textRun.getRawText();
                            if (text.contains("${")) {
                                for (Map.Entry<String, String> entry : alumnoData.entrySet()) {
                                    String placeholder = "${" + entry.getKey() + "}";
                                    if (text.contains(placeholder)) {
                                        text = text.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
                                        textRun.setText(text);

                                        // Ajustar tamaño de fuente para que el texto se ajuste a la forma
                                        ajustarTamanoFuente(textShape, textRun);
                                    }
                                }
                            }
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

        // Cierra el PPTX para eliminar el warning
        ppt.close();

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
            throw new OfficeException("Ocurrió un error inesperado al generar el PPT", e);
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
        String qrCodeText = "http://localhost:8080/generarCertificadoQr/" + encryptedId;

        ByteArrayOutputStream qrCodeOutputStream = generateQRCodeImage(qrCodeText, 200, 200);
        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        // Enviar correo electrónico al alumno con el PDF y el código QR como adjuntos
        sendEmailWithAttachments(alumno.getCorreo(), "Certificado de Curso", "Estimado " + alumno.getNombreAsistente() + ", adjuntamos su certificado y código QR.", pdfBytes, qrCodeBytes);

        return CompletableFuture.completedFuture(null);
    }

    private void ajustarTamanoFuente(XSLFTextShape textShape, XSLFTextRun textRun) {
        Double fontSize = textRun.getFontSize();
        if (fontSize == null || fontSize <= 0) {
            fontSize = 12.0; // Tamaño por defecto si no está establecido
        }
    
        // Obtener las dimensiones del contenedor
        double shapeWidth = textShape.getAnchor().getWidth();
        double shapeHeight = textShape.getAnchor().getHeight();
        
        // Reemplazar el carácter especial por saltos de línea
        String text = textRun.getRawText().replace("|", "\n");
    
        // Ajustar el tamaño de fuente mientras el texto no quepa en la forma
        while (!textoCabeEnForma(text, fontSize, shapeWidth, shapeHeight) && fontSize > 5) {
            fontSize -= 1;
            textRun.setFontSize(fontSize);
        }
    }
    
    
    private boolean textoCabeEnForma(String text, double fontSize, double shapeWidth, double shapeHeight) {
        // Crear un objeto Font para medir el texto
        String fontFamily = "Arial"; // Usa una fuente por defecto o toma del textRun si es necesario
        Font font = new Font(fontFamily, Font.PLAIN, (int) fontSize);
    
        // Crear un objeto FontRenderContext
        FontRenderContext frc = new FontRenderContext(null, true, true);
    
        // Calcular las dimensiones del texto
        Rectangle2D textBounds = font.getStringBounds(text, frc);
    
        // Comparar las dimensiones del texto con las de la forma
        return textBounds.getWidth() <= shapeWidth && textBounds.getHeight() <= shapeHeight;
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
        String secretKey = "eFSan7jbftsl2P6";
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

            // Codifica en Base64
            String base64Encrypted = Base64.getEncoder().encodeToString(encrypted);

            // Reemplaza caracteres para hacerla segura para URL
            base64Encrypted = base64Encrypted.replace("/", "_").replace("+", "-");
            return base64Encrypted;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error al encriptar el ID del alumno.");
        }
    }
    

    @Async
    private Long decryptStudentId(String encryptedId) throws Exception {
        String secretKey = "eFSan7jbftsl2P6"; // Usa una clave más segura y almacénala adecuadamente

        try {
            // Reemplaza los caracteres seguros para URL por los originales
            encryptedId = encryptedId.replace("_", "/").replace("-", "+");

            // Decodifica el ID en Base64
            byte[] decodedEncryptedId = Base64.getDecoder().decode(encryptedId);

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
    public CompletableFuture<Void> generateCertificateQR(String idEncriptada, HttpServletResponse response) throws Exception {
        Long alumnoId = decryptStudentId(idEncriptada);

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

        // Crea un mapa de los datos del alumno que se usarán para reemplazar en los placeholders
        Map<String, String> alumnoData = new HashMap<>();
        alumnoData.put("nombreAsistente", alumno.getNombreAsistente());
        alumnoData.put("nombreCurso", alumno.getNombreCurso());
        alumnoData.put("numeroHoras", alumno.getNumeroHoras());
        alumnoData.put("notaAprovacion", alumno.getNotaAprovacion());
        alumnoData.put("diasCursos", alumno.getDiasCursos());
        alumnoData.put("relator", alumno.getRelator());
        alumnoData.put("asistencia", alumno.getAsistencia());

        // Modifica las shapes reemplazando los placeholders ${atributo} por los valores correspondientes
        for (XSLFSlide slide : ppt.getSlides()) {
            for (XSLFShape shape : slide.getShapes()) {
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape textShape = (XSLFTextShape) shape;
                    List<XSLFTextParagraph> paragraphs = textShape.getTextParagraphs();
                    for (XSLFTextParagraph paragraph : paragraphs) {
                        List<XSLFTextRun> textRuns = paragraph.getTextRuns();
                        for (XSLFTextRun textRun : textRuns) {
                            String text = textRun.getRawText();
                            if (text.contains("${")) {
                                for (Map.Entry<String, String> entry : alumnoData.entrySet()) {
                                    String placeholder = "${" + entry.getKey() + "}";
                                    if (text.contains(placeholder)) {
                                        text = text.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
                                        textRun.setText(text);

                                        // Ajustar tamaño de fuente para que el texto se ajuste a la forma
                                        ajustarTamanoFuente(textShape, textRun);
                                    }
                                }
                            }
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

        // Cierra el PPTX para eliminar el warning
        ppt.close();

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

    
