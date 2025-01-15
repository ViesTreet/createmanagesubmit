package com.vt.createmanagesubmit.servicios;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.font.LineBreakMeasurer;
import java.text.AttributedString;
import java.awt.font.TextAttribute;
import java.awt.Dimension;
import java.awt.Color;
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
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Hibernate;
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
                            alumno.setCorreo(correoEmpresa);
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
                        String nombre = alumno.getNombreAsistente().trim().toUpperCase();
                        alumno.setNombreAsistente(nombre);
                        if(!alumno.getNombreAsistente().isEmpty()){
                            alumnoRepo.save(alumno);
                            servicio.numeroCorrelativoAuto(alumno);
                            if(estadoDiplomaExcel.equals("enviarApro")&&estadoExcel.equals("aprobado")){
                                estadoDiplomaExcel = "enviarTodos";
                            }
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
            case "días curso":
            case "días del curso":
            case "dias curso":
            case "dias del curso":
                alumno.setDiasCursos(valorCelda);
                break;
            case "nº de horas":
            case "numero horas":
            case "duracion del curso":
            case "duracion curso":
            case "duración del curso":
            case "duración curso":
                alumno.setDuracion(valorCelda);
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
                alumno.setNotaAprobacion(valorCelda);
                break;
            case "relator":
            case "profesor":
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
            case "modalidad":
                alumno.setModalidad(valorCelda);
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

    @Async
    @Transactional
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

    @Async
    @Transactional
    public void generateCertificatesById(Long id) throws Exception {
        Alumno alumno = alumnoRepo.findById(id).orElseThrow(() -> new Exception("Alumno no encontrado con ID: " + id));

        if (alumno != null) {
            Plantilla plantilla = alumno.getPlantilla();
            if (plantilla != null) {
                Hibernate.initialize(plantilla);
            }
            try {
                generateCertificateForAlumno(alumno);
                alumno.setDiploma("enviado");
                alumnoRepo.save(alumno);
            } catch (Exception ex) {
                ex.printStackTrace();
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
        alumnoData.put("nombre alumno", alumno.getNombreAsistente());
        alumnoData.put("curso", alumno.getNombreCurso());
        alumnoData.put("duracion", alumno.getDuracion());
        alumnoData.put("nota", alumno.getNotaAprobacion());
        alumnoData.put("dias curso", alumno.getDiasCursos());
        alumnoData.put("relator", alumno.getRelator());
        alumnoData.put("asistencia", alumno.getAsistencia());
        alumnoData.put("lugar y fecha", plantilla.getLugarYFecha());
        alumnoData.put("correlativo", alumno.getNumeroCorrelativoInterno());
        alumnoData.put("modalidad", alumno.getModalidad());

        // Procesa las slides y shapes
        for (XSLFSlide slide : ppt.getSlides()) {
            List<XSLFShape> shapesToRemove = new ArrayList<>();
        
            // Crea una copia de la lista de shapes
            List<XSLFShape> shapes = new ArrayList<>(slide.getShapes());
        
            for (XSLFShape shape : shapes) {
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape textShape = (XSLFTextShape) shape;
                    try {
                        processTextShape(slide, textShape, alumnoData, shapesToRemove);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        
            // Elimina las shapes marcadas después de la iteración
            for (XSLFShape shapeToRemove : shapesToRemove) {
                slide.removeShape(shapeToRemove);
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

    private void adjustFontSizeToFit(XSLFTextShape textShape, XSLFTextRun textRun, String text) {
        double minFontSize = 5.0; // Tamaño mínimo de fuente
        double maxFontSize = textRun.getFontSize();
        if (maxFontSize <= 0) {
            maxFontSize = 18.0;
        }
        double fontSize = maxFontSize;
        Rectangle2D textShapeBounds = textShape.getAnchor();
    
        Insets2D insets = textShape.getInsets();
        double shapeWidth = textShapeBounds.getWidth() - insets.left - insets.right;
        double shapeHeight = textShapeBounds.getHeight() - insets.top - insets.bottom;
    
        Dimension2D size = getTextSize(textShape, textRun, text, fontSize);
    
        while ((size.getWidth() > shapeWidth || size.getHeight() > shapeHeight) && fontSize > minFontSize) {
            fontSize -= 0.5;
            size = getTextSize(textShape, textRun, text, fontSize);
        }
    
        textRun.setFontSize(fontSize);
    }
    
    private Dimension2D getTextSize(XSLFTextShape textShape, XSLFTextRun textRun, String text, double fontSize) {
        Font font = new Font(textRun.getFontFamily(), Font.PLAIN, (int) fontSize);
    
        // Crear una imagen temporal para obtener el contexto gráfico
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
    
        FontRenderContext frc = g2d.getFontRenderContext();
        TextLayout layout = new TextLayout(text, font, frc);
        Rectangle2D bounds = layout.getBounds();
    
        g2d.dispose();
    
        // Añadir los márgenes internos del shape
        Insets2D insets = textShape.getInsets();
        double textWidth = bounds.getWidth() + insets.left + insets.right;
        double textHeight = bounds.getHeight() + insets.top + insets.bottom;
    
        return new Dimension2DDouble(textWidth, textHeight);
    }

    private void processTextShape(XSLFSlide slide, XSLFTextShape textShape, Map<String, String> data, List<XSLFShape> shapesToRemove) throws Exception {
        boolean placeholderFound = false;
        String placeholderKey = null;
        String replacementText = null;
        XSLFTextParagraph sourceParagraph = null; // Para almacenar el párrafo original
    
        // Recorre los párrafos y runs en busca de placeholders
        for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
            for (XSLFTextRun textRun : paragraph.getTextRuns()) {
                String text = textRun.getRawText();
                if (text.contains("${")) {
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        String placeholder = "${" + entry.getKey() + "}";
                        if (text.contains(placeholder)) {
                            placeholderFound = true;
                            placeholderKey = entry.getKey();
                            replacementText = entry.getValue() != null ? entry.getValue() : "";
                            sourceParagraph = paragraph; // Guarda el párrafo original
                            break;
                        }
                    }
                }
                if (placeholderFound) {
                    break;
                }
            }
            if (placeholderFound) {
                break;
            }
        }
    
        if (placeholderFound) {
            // Determina si el texto contiene '|'
            if (replacementText.contains("|")) {
                // El texto contiene saltos de línea, hay que dividir y crear nuevas shapes
                shapesToRemove.add(textShape); // Marca el shape original para eliminarlo
    
                String[] lines = replacementText.split("\\|");
                int numberOfLines = lines.length;
    
                // Obtiene la posición y dimensiones del shape original
                Rectangle2D anchor = textShape.getAnchor();
                double totalHeight = anchor.getHeight();
                double shapeWidth = anchor.getWidth();
                double shapeX = anchor.getX();
                double shapeY = anchor.getY(); // Eliminé el ajuste de -11.0
    
                // Ajusta la altura de cada nuevo shape, considerando un pequeño margen entre líneas
                double margin = 2.0; // Ajusta este valor según sea necesario
                double shapeHeight = totalHeight / numberOfLines;
    
                // Para cada línea, crea un nuevo shape
                for (int i = 0; i < numberOfLines; i++) {
                    String line = lines[i];
    
                    // Calcula la nueva posición Y considerando el margen entre líneas
                    double adjustedMargin = i == 0 ? 0 : margin; // Sin margen en el primer shape
                    double newY = shapeY + i * (shapeHeight + adjustedMargin);
                    
                    // Establece la posición y tamaño del nuevo shape
                    Rectangle2D newAnchor = new Rectangle2D.Double(
                        shapeX, newY, shapeWidth, shapeHeight
                    );
    
                    // Crea un nuevo shape
                    XSLFTextBox newShape = slide.createTextBox();
                    newShape.setAnchor(newAnchor);
    
                    // Copia las propiedades del shape original
                    copyShapeProperties(textShape, newShape);
    
                    // Establece el texto
                    XSLFTextParagraph newParagraph = newShape.addNewTextParagraph();
                    copyParagraphProperties(sourceParagraph, newParagraph); // Copia las propiedades del párrafo
    
                    XSLFTextRun newRun = newParagraph.addNewTextRun();
                    newRun.setText(line);
    
                    // Aplica las propiedades de fuente y ajusta el tamaño si es necesario
                    applyFontProperties(textShape, newRun);
                    adjustFontSizeToFit(newShape, newRun, line);
                }
            } else {
                // El texto no contiene saltos de línea, simplemente reemplaza el placeholder y ajusta el tamaño
                for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
                    for (XSLFTextRun textRun : paragraph.getTextRuns()) {
                        String text = textRun.getRawText();
                        String placeholder = "${" + placeholderKey + "}";
                        if (text.contains(placeholder)) {
                            String newText = text.replace(placeholder, replacementText);
                            textRun.setText(newText);
    
                            // Ajustar el tamaño de la fuente
                            adjustFontSizeToFit(textShape, textRun, newText);
                        }
                    }
                }
            }
        }
    }
    
    private void copyShapeProperties(XSLFTextShape sourceShape, XSLFTextShape targetShape) {
        // Copia las propiedades del shape original al nuevo shape
        targetShape.setFillColor(sourceShape.getFillColor());
        targetShape.setLineColor(sourceShape.getLineColor());
        targetShape.setLineWidth(sourceShape.getLineWidth());
        targetShape.setVerticalAlignment(sourceShape.getVerticalAlignment());
        targetShape.setInsets(sourceShape.getInsets());
        targetShape.setTextDirection(sourceShape.getTextDirection());
        targetShape.setTextAutofit(sourceShape.getTextAutofit());
        targetShape.setRotation(sourceShape.getRotation());
        // Copia el nombre del shape si es necesario
        //targetShape.setShapeName(sourceShape.getShapeName());
        // Copia otras propiedades adicionales si lo necesitas
    }
    
    private void copyParagraphProperties(XSLFTextParagraph sourceParagraph, XSLFTextParagraph targetParagraph) {
        // Copiar alineación y otras propiedades del párrafo
        targetParagraph.setTextAlign(sourceParagraph.getTextAlign());
        targetParagraph.setBullet(sourceParagraph.isBullet());
        //targetParagraph.setBulletAutoNumber(sourceParagraph.isBulletAutoNumber());
        targetParagraph.setBulletCharacter(sourceParagraph.getBulletCharacter());
        targetParagraph.setBulletFont(sourceParagraph.getBulletFont());
        targetParagraph.setIndent(sourceParagraph.getIndent());
        targetParagraph.setLeftMargin(sourceParagraph.getLeftMargin());
        targetParagraph.setRightMargin(sourceParagraph.getRightMargin());
        targetParagraph.setIndentLevel(sourceParagraph.getIndentLevel());
        targetParagraph.setLineSpacing(sourceParagraph.getLineSpacing());
        targetParagraph.setSpaceAfter(sourceParagraph.getSpaceAfter());
        targetParagraph.setSpaceBefore(sourceParagraph.getSpaceBefore());
        //targetParagraph.setDefaultTabSize(sourceParagraph.getDefaultTabSize());
        // Copia otras propiedades adicionales si lo necesitas
    }
    
    private void applyFontProperties(XSLFTextShape sourceShape, XSLFTextRun targetRun) {
        // Copia las propiedades de fuente del shape original
        XSLFTextParagraph sourceParagraph = sourceShape.getTextParagraphs().get(0);
        XSLFTextRun sourceRun = sourceParagraph.getTextRuns().get(0);
    
        targetRun.setFontFamily(sourceRun.getFontFamily());
        targetRun.setFontColor(sourceRun.getFontColor());
        targetRun.setBold(sourceRun.isBold());
        targetRun.setItalic(sourceRun.isItalic());
        targetRun.setUnderlined(sourceRun.isUnderlined());
        targetRun.setFontSize(sourceRun.getFontSize());
        targetRun.setCharacterSpacing(sourceRun.getCharacterSpacing());
        // Copia otras propiedades adicionales si lo necesitas
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
        alumnoData.put("nombre alumno", alumno.getNombreAsistente());
        alumnoData.put("curso", alumno.getNombreCurso());
        alumnoData.put("duracion", alumno.getDuracion());
        alumnoData.put("nota", alumno.getNotaAprobacion());
        alumnoData.put("dias curso", alumno.getDiasCursos());
        alumnoData.put("relator", alumno.getRelator());
        alumnoData.put("asistencia", alumno.getAsistencia());
        alumnoData.put("lugar y fecha", plantilla.getLugarYFecha());
        alumnoData.put("correlativo", alumno.getNumeroCorrelativoInterno());
        alumnoData.put("modalidad", alumno.getModalidad());

        // Procesa las slides y shapes
        for (XSLFSlide slide : ppt.getSlides()) {
            List<XSLFShape> shapesToRemove = new ArrayList<>();
            List<XSLFShape> shapes = new ArrayList<>(slide.getShapes()); // Crea una copia de la lista de shapes

            for (XSLFShape shape : shapes) {
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape textShape = (XSLFTextShape) shape;
                    try {
                        processTextShape(slide, textShape, alumnoData, shapesToRemove);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Elimina las shapes marcadas después de la iteración
            for (XSLFShape shapeToRemove : shapesToRemove) {
                slide.removeShape(shapeToRemove);
            }
        }

        // Guarda el PPTX modificado en un archivo temporal
        File tempPptxFile = File.createTempFile("certificado-", ".pptx");
        try (FileOutputStream out = new FileOutputStream(tempPptxFile)) {
            ppt.write(out);
        }

        // Cierra el PPTX para evitar problemas
        ppt.close();

        // Convierte el PPTX a PDF usando JODConverter
        File tempPdfFile = File.createTempFile("certificado-", ".pdf");

        LocalOfficeManager officeManager = LocalOfficeManager.builder()
                .install()
                .build();

        try {
            officeManager.start();
            JodConverter.convert(tempPptxFile)
                    .to(tempPdfFile)
                    .execute();

            // Leer el PDF generado como array de bytes
            byte[] pdfBytes = Files.readAllBytes(tempPdfFile.toPath());

            // Configurar la respuesta HTTP para enviar el PDF como archivo descargable
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"certificado-" + alumno.getId() + ".pdf\"");

            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

         } catch (OfficeException e) {
             throw new OfficeException("Ocurrió un error inesperado al generar el PDF", e);
         } finally {
             if (officeManager != null) {
                 officeManager.stop();
             }

             // Eliminar los archivos temporales
             tempPptxFile.delete();
             tempPdfFile.delete();
         }

         return CompletableFuture.completedFuture(null);
    }

    @Async
    @Transactional
    public CompletableFuture<byte[]> probarCertificadosServicio(Alumno alumno) throws Exception {

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
        alumnoData.put("nombre alumno", alumno.getNombreAsistente());
        alumnoData.put("curso", alumno.getNombreCurso());
        alumnoData.put("duracion", alumno.getDuracion());
        alumnoData.put("nota", alumno.getNotaAprobacion());
        alumnoData.put("dias curso", alumno.getDiasCursos());
        alumnoData.put("relator", alumno.getRelator());
        alumnoData.put("asistencia", alumno.getAsistencia());
        alumnoData.put("lugar y fecha", plantilla.getLugarYFecha());
        alumnoData.put("correlativo", alumno.getNumeroCorrelativoInterno());
        alumnoData.put("modalidad", alumno.getModalidad());

        // Procesa las slides y shapes
        for (XSLFSlide slide : ppt.getSlides()) {
            List<XSLFShape> shapesToRemove = new ArrayList<>();
            List<XSLFShape> shapes = new ArrayList<>(slide.getShapes()); // Crea una copia de la lista de shapes

            for (XSLFShape shape : shapes) {
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape textShape = (XSLFTextShape) shape;
                    try {
                        processTextShape(slide, textShape, alumnoData, shapesToRemove);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Elimina las shapes marcadas después de la iteración
            for (XSLFShape shapeToRemove : shapesToRemove) {
                slide.removeShape(shapeToRemove);
            }
        }

        // Guarda el PPTX modificado en un archivo temporal
        File tempPptxFile = File.createTempFile("certificado-", ".pptx");
        try (FileOutputStream out = new FileOutputStream(tempPptxFile)) {
            ppt.write(out);
        }

        // Cierra el PPTX para evitar problemas
        ppt.close();
        byte[] pdfBytes = null;
        // Convierte el PPTX a PDF usando JODConverter
        File tempPdfFile = File.createTempFile("certificado-", ".pdf");

        LocalOfficeManager officeManager = LocalOfficeManager.builder()
                .install()
                .build();

        try {
            officeManager.start();
            JodConverter.convert(tempPptxFile)
                    .to(tempPdfFile)
                    .execute();

            pdfBytes = Files.readAllBytes(tempPdfFile.toPath());

         } catch (OfficeException e) {
             throw new OfficeException("Ocurrió un error inesperado al generar el PDF", e);
         } finally {
             if (officeManager != null) {
                 officeManager.stop();
             }

             // Eliminar los archivos temporales
             tempPptxFile.delete();
             tempPdfFile.delete();
         }

         return CompletableFuture.completedFuture(pdfBytes);
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

    
