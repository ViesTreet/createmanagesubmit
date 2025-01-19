package com.vt.createmanagesubmit.servicios;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
// Otras importaciones necesarias
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.jodconverter.local.JodConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;


@Service
public class ServicioGenerarCertificado {

    @Autowired
    private RepositorioAlumnos alumnoRepo;

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> generateCertificateForAlumno(Alumno alumno) throws Exception {
        // Obtén la plantilla asociada al alumno
        Plantilla plantilla = alumno.getPlantilla();
        if (plantilla == null || plantilla.getNombreCertificado().trim().equals("Error en encontrar plantilla")) {
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
        if(!plantilla.getLugarYFecha().trim().isEmpty() || plantilla.getLugarYFecha() != null){
            alumnoData.put("lugar y fecha", plantilla.getLugarYFecha());
        }
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
        JodConverter
                .convert(new File(tempPptxPath))
                .to(new File(tempPdfPath))
                .execute();

        

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
        sendEmailWithAttachments(alumno.getCorreo(), "Certificado de Curso",alumno, pdfBytes, qrCodeBytes);

        return CompletableFuture.completedFuture(null);
    }

    private void adjustFontSizeToFit(XSLFTextShape textShape, XSLFTextRun textRun, String text) {
        if (text == null || text.isEmpty()) {
            // Log o manejo del caso
            System.out.println("Texto vacío o nulo detectado: " + text);
            return; // No hay nada que ajustar
        }
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
        boolean needsAdjustment = false;
        while ((size.getWidth() > shapeWidth || size.getHeight() > shapeHeight) && fontSize > minFontSize) {
            fontSize -= 0.5;
            size = getTextSize(textShape, textRun, text, fontSize);
            needsAdjustment = true;
        }
        if (needsAdjustment && fontSize > minFontSize) {
            fontSize -= 2.0; // Margen de seguridad
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
                boolean seReemplazoAlgo = false;
    
                // Recorremos todos los placeholders que estén en 'data'
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    String placeholder = "${" + entry.getKey() + "}";
                    if (text.contains(placeholder)) {
                        placeholderFound = true;
                        placeholderKey = entry.getKey();
                        replacementText = entry.getValue() != null ? entry.getValue() : "";
    
                        // Reemplazar todas las ocurrencias de este placeholder
                        text = text.replace(placeholder, replacementText);
                        seReemplazoAlgo = true;
    
                        // Guardamos el párrafo original solo la primera vez
                        if (sourceParagraph == null) {
                            sourceParagraph = paragraph;
                        }
                    }
                }
    
                // Si se hizo algún reemplazo en este textRun
                if (seReemplazoAlgo) {
                    // Asignamos el texto ya con todos los placeholders reemplazados
                    textRun.setText(text);
    
                    // Verificamos si hay que dividir en múltiples líneas (si hay '|')
                    if (text.contains("|")) {
                        shapesToRemove.add(textShape); // Eliminar shape original
                        String[] lines = text.split("\\|");
    
                        // Obtiene la posición y dimensiones del shape original
                        Rectangle2D anchor = textShape.getAnchor();
                        double totalHeight = anchor.getHeight();
                        double shapeWidth = anchor.getWidth();
                        double shapeX = anchor.getX();
                        double shapeY = anchor.getY();
    
                        // Ajusta la altura de cada nuevo shape, considerando un pequeño margen entre líneas
                        double margin = 0.5; // Ajusta este valor según sea necesario
                        double shapeHeight = totalHeight / lines.length;
    
                        // Para cada línea, crea un nuevo shape
                        for (int i = 0; i < lines.length; i++) {
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
                        // Texto simple, ajustamos la fuente
                        adjustFontSizeToFit(textShape, textRun, text);
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

    private String capitalizeName(String name) {
        if (name == null || name.isEmpty()) return name;
        String[] parts = name.toLowerCase().split(" ");
        StringBuilder capitalized = new StringBuilder();
        for (String part : parts) {
            capitalized.append(part.substring(0, 1).toUpperCase())
                    .append(part.substring(1)).append(" ");
        }
        return capitalized.toString().trim();
    }
    
    private String formatCourseName(String courseName) {
        if (courseName != null && courseName.contains("|")) {
            return courseName.replace("|", ", ");
        }
        return courseName;
    }

    private void sendEmailWithAttachments(String toEmail, String subject, Alumno alumno, byte[] pdfBytes, byte[] qrCodeBytes) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        byte[] logoBytes=null;
        try {
            logoBytes = Files.readAllBytes(Paths.get("src/main/resources/static/images/Logobgremove.png"));
        } catch (IOException e) {

            e.printStackTrace();
        }
        ByteArrayResource logoResource = new ByteArrayResource(logoBytes);
        String htmlContent = "<!DOCTYPE html>" +
        "<html lang='es'>" +
        "<head>" +
        "<meta charset='UTF-8'>" +
        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
        "<title>Document</title>" +
        "</head>" +
        "<body style='font-family: Arial, sans-serif; background-color: #f9f9f9; margin: 0; padding: 0;'>" +
        "<div style='max-width: 600px; margin: 0 auto; background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);'>" +
        "<table role='presentation' width='100%' style='border-spacing: 0;'>" +
        "<tr>" +
        "<td style='text-align: center;'>" +
        "<img style='width: 234px; height: 58px;' src='cid:logoImage' alt='LOGO'>" +
        "</td>" +
        "</tr>" +
        "<tr>" +
        "<td style='text-align: center; color: black; padding: 10px 0; border-radius: 8px 8px 0 0;'>" +
        "<h2>¡Felicitaciones!</h2>" +
        "<h4>Le hemos emitido un certificado.</h4>" +
        "</td>" +
        "</tr>" +
        "<tr>" +
        "<td style='text-align: center; margin: 20px 0;'>" +
        "<p>Estimado/a <strong>" + capitalizeName(alumno.getNombreAsistente()) + "</strong>:</p>" +
        "<p>Adjunto encontrarás tu certificado de participación en el/los curso(s) <strong>" + formatCourseName(alumno.getNombreCurso()) + "</strong>. Junto con un código QR que te permitirá descargarlo nuevamente desde nuestra página web.</p>" +
        "<p>También puedes descargar tu certificado escaneando el siguiente código QR:</p>" +
        "</td>" +
        "</tr>" +
        "<tr>" +
        "<td style='text-align: center; margin: 20px 0;'>" +
        "<img style='width: 200px; height: 200px;' src='cid:qrCodeImage' alt='QR Code'>" +
        "</td>" +
        "</tr>" +
        "<tr>" +
        "<td style='text-align: center; font-size: 12px; color: #777; margin-top: 20px;'>" +
        "<p>Si tienes dudas, no dudes en contactarnos a través de <a href='mailto:contacto@e-volution.cl'>contacto@e-volution.cl</a>.</p>" +
        "</td>" +
        "</tr>" +
        "</table>" +
        "</div>" +
        "</body>" +
        "</html>";

        helper.setText(htmlContent, true); // true indica que es formato HTML

        // Adjuntar el PDF
        ByteArrayResource pdfResource = new ByteArrayResource(pdfBytes);
        helper.addAttachment("certificado.pdf", pdfResource);

        ByteArrayResource qrCodeResourceHtml = new ByteArrayResource(qrCodeBytes);
        helper.addInline("qrCodeImage", qrCodeResourceHtml, "image/png");

        // Adjuntar el código QR
        ByteArrayResource qrCodeResource = new ByteArrayResource(qrCodeBytes);
        helper.addAttachment("codigoQR.png", qrCodeResource);

        helper.addInline("logoImage", logoResource, "image/png");

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> generateCertificateQR(String idEncriptada, HttpServletResponse response) throws Exception {
        Long alumnoId = decryptStudentId(idEncriptada);

        // Obtén el alumno por ID
        Alumno alumno = alumnoRepo.findById(alumnoId).orElseThrow(() -> new Exception("Alumno no encontrado con ID " + alumnoId));

        // Obtén la plantilla asociada al alumno
        Plantilla plantilla = alumno.getPlantilla();
        if (plantilla == null || plantilla.getNombreCertificado().trim().equals("Error en encontrar plantilla")) {
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
        if(!plantilla.getLugarYFecha().trim().isEmpty() || plantilla.getLugarYFecha() != null){
            alumnoData.put("lugar y fecha", plantilla.getLugarYFecha());
        }
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

        JodConverter
                .convert(tempPptxFile)
                .to(tempPdfFile)
                .execute();

        // Leer el PDF generado como array de bytes
        byte[] pdfBytes = Files.readAllBytes(tempPdfFile.toPath());
        // Configurar la respuesta HTTP para enviar el PDF como archivo descargable
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"certificado-" + alumno.getId() + ".pdf\"");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
         // Eliminar los archivos temporales
        tempPptxFile.delete();
        tempPdfFile.delete();
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<byte[]> descargarCertificadosServicio(Alumno alumno) throws Exception {

        // Obtén la plantilla asociada al alumno
        Plantilla plantilla = alumno.getPlantilla();
        if (plantilla == null || plantilla.getNombreCertificado().trim().equals("Error en encontrar plantilla")) {
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
        if(!plantilla.getLugarYFecha().trim().isEmpty() || plantilla.getLugarYFecha() != null){
            alumnoData.put("lugar y fecha", plantilla.getLugarYFecha());
        }
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

        JodConverter
                .convert(tempPptxFile)
                .to(tempPdfFile)
                .execute();


        pdfBytes = Files.readAllBytes(tempPdfFile.toPath());
        // Eliminar los archivos temporales
        tempPptxFile.delete();
        tempPdfFile.delete();
         

         return CompletableFuture.completedFuture(pdfBytes);
    }

}
