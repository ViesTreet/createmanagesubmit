package com.vt.createmanagesubmit.servicios;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
// Otras importaciones necesarias
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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.poi.sl.usermodel.PictureData;
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
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.JodConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;
import com.vt.createmanagesubmit.repositorios.RepositorioPlantillas;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

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

    public void leerExcelYGuardarEnBD(String rutaArchivo, String estadoDiplomaExcel, String plantilla, String estadoExcel) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(rutaArchivo));
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

                    if (alumno.getCorreo() == null) {
                        alumno.setCorreo(correoEmpresa);
                    }

                    if (alumno.getEstado() == null) {
                        alumno.setEstado("revisionManual");
                    }

                    if (alumno.getEstado() != "Eexcel") {
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
                            if (alumno.getEstado() == "aprobado") {
                                try {
                                    generateCertificateForAlumno(alumno);
                                    alumno.setDiploma("enviado");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (estadoDiplomaExcel.equals("enviarTodos")) {
                            try {
                                generateCertificateForAlumno(alumno);
                                alumno.setDiploma("enviado");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    alumnoRepo.save(alumno);
                }
            }
        }
        workbook.close();
        fileInputStream.close();
    }

    private void asignarValorAtributo(Alumno alumno, String nombreColumna, Cell celda) {
        // Obtener el valor de la celda como String
        String valorCelda = obtenerValorCeldaComoString(celda);

        // Asignar el valor al atributo correspondiente
        switch (nombreColumna.toLowerCase()) {
            case "nº":
            case "número":
                break;
            case "NOMBRE ASISTENTE":
            case "nombre asistente":
                alumno.setNombreAsistente(valorCelda);
                break;
            case "nombre curso":
            case "Nombre curso":
                alumno.setNombreCurso(valorCelda);
                break;
            case "dias curso":
            case "Dias curso":
                alumno.setDiasCursos(valorCelda);
                break;
            case "Nº de Horas":
            case "numero horas":
                alumno.setNumeroHoras(valorCelda);
                break;
            case "Nº Correlativo Interno":
            case "numero correlativo interno":
                alumno.setNumeroCorrelativoInterno(valorCelda);
                break;
            case "cliente":
            case "Cliente":
                alumno.setCliente(valorCelda);
                break;
            case "obra":
            case "Obra":
                alumno.setObra(valorCelda);
                break;
            case "codigo":
            case "Codigo":
                alumno.setCodigo(valorCelda);
                break;
            case "nota aprobación":
            case "Nota Aprobación":
                alumno.setNotaAprovacion(valorCelda);
                break;
            case "relator":
            case "Relator":
                alumno.setRelator(valorCelda);
                break;
            case "asistencia":
            case "Asistencia":
                alumno.setAsistencia(valorCelda);
                break;
            case "estado":
            case "Estado":
                if (valorCelda.trim().equalsIgnoreCase("aprobado")) {
                    alumno.setEstado("aprobado");
                } else {
                    alumno.setEstado("noAprobado");
                }
                break;
            case "diploma":
            case "Diploma":
                if (valorCelda.trim().equalsIgnoreCase("No enviado")) {
                    alumno.setDiploma("noEnviado");
                } else if (valorCelda.trim().equalsIgnoreCase("Enviado")) {
                    alumno.setDiploma("enviado");
                } else {
                    alumno.setDiploma("revisionManual");
                }
                break;
            case "rut":
            case "Rut":
                alumno.setRut(valorCelda);
                break;
            case "correo":
            case "Correo":
                alumno.setCorreo(valorCelda);
                break;
            case "Plantilla":
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
            default:
                // Si hay columnas que no corresponden a ningún atributo, puedes ignorarlas
                break;
        }
    }

    private String obtenerValorCeldaComoString(Cell celda) {
        switch (celda.getCellType()) {
            case STRING:
                return celda.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(celda)) {
                    // Si es una fecha
                    Date date = celda.getDateCellValue();
                    return new SimpleDateFormat("yyyy-MM-dd").format(date);
                } else {
                    // Si es un número
                    return String.valueOf(celda.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(celda.getBooleanCellValue());
            case FORMULA:
                // Evaluar la fórmula
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
        generateCertificateForAlumno(alumno);
        alumno.setDiploma("enviado");
        alumnoRepo.save(alumno);
    }

    public void generateCertificateForAlumno(Alumno alumno) throws Exception {
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
                // Aquí se eliminó el bloque que manejaba XSLFPictureShape
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
            OfficeUtils.stopQuietly(officeManager);
        }

        // Leer el PDF generado como array de bytes
        byte[] pdfBytes = Files.readAllBytes(Paths.get(tempPdfPath));

        // Eliminar los archivos temporales
        new File(tempPptxPath).delete();
        new File(tempPdfPath).delete();

        // Generar código QR con la URL y el ID encriptado
        String encryptedId = encryptStudentId(alumno.getId().toString());
        String qrCodeText = "/api/generar/" + encryptedId;
        ByteArrayOutputStream qrCodeOutputStream = generateQRCodeImage(qrCodeText, 200, 200);
        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        // Enviar correo electrónico al alumno con el PDF y el código QR como adjuntos
        // Comentario: Aquí es donde se crea el correo electrónico. Puedes darle un formato más bonito.
        sendEmailWithAttachments(alumno.getCorreo(), "Certificado de Curso",
                "Estimado " + alumno.getNombreAsistente() + ", adjuntamos su certificado y código QR.",
                pdfBytes, qrCodeBytes);
    }

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
}