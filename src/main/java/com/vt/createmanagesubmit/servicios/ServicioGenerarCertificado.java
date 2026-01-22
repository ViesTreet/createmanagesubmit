package com.vt.createmanagesubmit.servicios;

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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

import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.TextShape.TextAutofit;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.jodconverter.local.JodConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
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

    @Value("${DIP_MAIL}")
    private String correoEmpresa;

    @Value("${ENCRYPT_KEY_GEN}")
    private String DecryptKeyGene;

    @Value("${URL_PATH}")
    public String urlPath;

    @Value("${DIP_MAIL}")
    public String diplomasMail;

    @Value("${ST_FOLDER}")
    public String stPath;

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

        // Generar código QR con la URL y el ID encriptado
        String encryptedId = encryptStudentId(alumno.getId().toString());
        String qrCodeText = urlPath+"/generarCertificadoQr/" + encryptedId;

        ByteArrayOutputStream qrCodeOutputStream = generateQRCodeImage(qrCodeText, 200, 200);
        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        

        // Crea un mapa de los datos del alumno que se usarán para reemplazar en los placeholders
        Map<String, String> alumnoData = new HashMap<>();
        alumnoData.put("nombre", alumno.getNombreAsistente());
        alumnoData.put("curso", alumno.getNombreCurso());
        alumnoData.put("duracion", alumno.getDuracion());
        alumnoData.put("nota", alumno.getNotaAprobacion());
        alumnoData.put("dias", alumno.getDiasCursos());
        alumnoData.put("relator", alumno.getRelator());
        alumnoData.put("asistencia", alumno.getAsistencia());
        alumnoData.put("emision", alumno.getLugarYfechaEmision());
        alumnoData.put("correlativo", alumno.getNumeroCorrelativoInterno());
        alumnoData.put("modalidad", alumno.getModalidad());

        Map<String, byte[]> imageData = new HashMap<>();
        imageData.put("imagen_qr", qrCodeBytes);

        for (XSLFSlide slide : ppt.getSlides()) {
            processSlide(slide, alumnoData, imageData);
        }
    

        // Guarda el PPTX modificado en un archivo temporal
        String tempPptxPath = stPath + "/temp/" + alumno.getId() + ".pptx";
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
        String tempPdfPath = stPath + "/temp/" + alumno.getId() + ".pdf";
        JodConverter
                .convert(new File(tempPptxPath))
                .to(new File(tempPdfPath))
                .execute();

        

        // Leer el PDF generado como array de bytes
        byte[] pdfBytes = Files.readAllBytes(Paths.get(tempPdfPath));

        // Eliminar los archivos temporales
        
        new File(tempPptxPath).delete();
        new File(tempPdfPath).delete();

        // Enviar correo electrónico al alumno con el PDF y el código QR como adjuntos
        System.out.println("iniciando correo");
        sendEmailWithAttachments(alumno.getCorreo(), "Certificado de Curso",alumno, pdfBytes, qrCodeBytes);
        System.out.println("termino");
        return CompletableFuture.completedFuture(null);
    }

    private ByteArrayOutputStream generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        BitMatrix bitMatrix = qrCodeWriter.encode(
            text,
            BarcodeFormat.QR_CODE,
            width,
            height,
            hints);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        // Cargar logo
        BufferedImage logo = ImageIO.read(new File("src/main/resources/static/images/Logo.png"));

        int originalWidth = logo.getWidth();
        int originalHeight = logo.getHeight();
        double maxLogoSize = width / 2.2; 
        double scale = Math.min(
                (double) maxLogoSize / originalWidth,
                (double) maxLogoSize / originalHeight
        );

        int logoWidth = (int) (originalWidth * scale);
        int logoHeight = (int) (originalHeight * scale);



        // Escalar logo
        Image scaledLogo = logo.getScaledInstance(
                logoWidth,
                logoHeight,
                Image.SCALE_SMOOTH
        );

        // Posición centrada
        int x = (width - logoWidth) / 2;
        int y = (height - logoHeight) / 2;

        // Dibujar logo sobre el QR
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(scaledLogo, x, y, null);
        g.dispose();

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
        helper.setFrom(diplomasMail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        byte[] logoBytes=null;
        System.out.println("entro en el servicio email");
        ClassPathResource logoResourceDir = new ClassPathResource("static/images/Logobgremove.png");
        try (InputStream is = logoResourceDir.getInputStream()) {
            logoBytes = is.readAllBytes();
        } catch (IOException e) {
            throw new MessagingException("Error cargando logo", e);
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

    public String encryptStudentId(String studentId) throws Exception {
        String secretKey = DecryptKeyGene;
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

    public Long decryptStudentId(String encryptedId) throws Exception {
        String secretKey = DecryptKeyGene; // Usa una clave más segura y almacénala adecuadamente

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
        // Generar código QR con la URL y el ID encriptado
        String encryptedId = encryptStudentId(alumno.getId().toString());
        String qrCodeText = urlPath+"/generarCertificadoQr/" + encryptedId;

        ByteArrayOutputStream qrCodeOutputStream = generateQRCodeImage(qrCodeText, 200, 200);
        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        // Crea un mapa de los datos del alumno que se usarán para reemplazar en los placeholders
        Map<String, String> alumnoData = new HashMap<>();
        alumnoData.put("nombre", alumno.getNombreAsistente());
        alumnoData.put("curso", alumno.getNombreCurso());
        alumnoData.put("duracion", alumno.getDuracion());
        alumnoData.put("nota", alumno.getNotaAprobacion());
        alumnoData.put("dias", alumno.getDiasCursos());
        alumnoData.put("relator", alumno.getRelator());
        alumnoData.put("asistencia", alumno.getAsistencia());
        alumnoData.put("emision", alumno.getLugarYfechaEmision());
        alumnoData.put("correlativo", alumno.getNumeroCorrelativoInterno());
        alumnoData.put("modalidad", alumno.getModalidad());

        Map<String, byte[]> imageData = new HashMap<>();
        imageData.put("imagen_qr", qrCodeBytes);

        // Procesa las slides y shapes
        for (XSLFSlide slide : ppt.getSlides()) {
            processSlide(slide, alumnoData, imageData);
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
        // Generar código QR con la URL y el ID encriptado
        String encryptedId = encryptStudentId(alumno.getId().toString());
        String qrCodeText = urlPath+"/generarCertificadoQr/" + encryptedId;

        ByteArrayOutputStream qrCodeOutputStream = generateQRCodeImage(qrCodeText, 200, 200);
        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        // Crea un mapa de los datos del alumno que se usarán para reemplazar en los placeholders
        Map<String, String> alumnoData = new HashMap<>();
        alumnoData.put("nombre", alumno.getNombreAsistente());
        alumnoData.put("curso", alumno.getNombreCurso());
        alumnoData.put("duracion", alumno.getDuracion());
        alumnoData.put("nota", alumno.getNotaAprobacion());
        alumnoData.put("dias", alumno.getDiasCursos());
        alumnoData.put("relator", alumno.getRelator());
        alumnoData.put("asistencia", alumno.getAsistencia());
        alumnoData.put("emision", alumno.getLugarYfechaEmision());
        alumnoData.put("correlativo", alumno.getNumeroCorrelativoInterno());
        alumnoData.put("modalidad", alumno.getModalidad());
        
        Map<String, byte[]> imageData = new HashMap<>();
        imageData.put("imagen_qr", qrCodeBytes);


        // Procesa las slides y shapes
        for (XSLFSlide slide : ppt.getSlides()) {
            processSlide(slide, alumnoData, imageData);
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

    private void processSlide(XSLFSlide slide, Map<String, String> data, Map<String, byte[]> imageData) {
        List<XSLFShape> shapes = new ArrayList<>(slide.getShapes());
        
        for (XSLFShape shape : shapes) {
            if (shape instanceof XSLFTextShape) {
                processTextShape(slide, (XSLFTextShape) shape, data, imageData);
            }
        }
    }
    
    private void processTextShape(XSLFSlide slide, XSLFTextShape textShape, Map<String, String> data, Map<String, byte[]> imageData) {
        List<XSLFTextParagraph> paragraphs = new ArrayList<>(textShape.getTextParagraphs());
        
        for (XSLFTextParagraph paragraph : paragraphs) {
            String fullText = getFullParagraphText(paragraph);
            for (Map.Entry<String, byte[]> img : imageData.entrySet()) {
                String placeholder = "${" + img.getKey() + "}";
                if (fullText.contains(placeholder)) {
                    try {
                        replaceTextShapeWithImage(
                                slide,
                                textShape,
                                img.getValue()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if (fullText.contains(placeholder)) {
                    String replacement = entry.getValue() != null ? entry.getValue() : "";
                    replacePlaceholder(paragraph, placeholder, replacement, textShape);
                }
            }
        }
    }
    
    private String getFullParagraphText(XSLFTextParagraph paragraph) {
        StringBuilder sb = new StringBuilder();
        for (XSLFTextRun run : paragraph.getTextRuns()) {
            sb.append(run.getRawText());
        }
        return sb.toString();
    }
    
    private void replacePlaceholder(XSLFTextParagraph paragraph, String placeholder, 
                                String replacement, XSLFTextShape textShape) { // Añadir textShape como parámetro
    
    String originalText = getFullParagraphText(paragraph);
    String newText = originalText.replace(placeholder, replacement);
    
    if (!paragraph.getTextRuns().isEmpty()) {
        // Conservar primer run como referencia de formato
        XSLFTextRun sourceRun = paragraph.getTextRuns().get(0);
        
        // Eliminar runs adicionales (corregido)
        List<XSLFTextRun> runs = new ArrayList<>(paragraph.getTextRuns());
        for (int i = runs.size() - 1; i > 0; i--) { // Eliminar desde el último al primero
            paragraph.removeTextRun(runs.get(i));
        }
        
        // Dividir texto y manejar saltos de línea
        String[] lines = newText.split("\\|");
        sourceRun.setText(lines[0]);
        
        // Añadir líneas adicionales (corregido)
        for (int i = 1; i < lines.length; i++) {
            // Añadir salto de línea al párrafo (no al text run)
            paragraph.addLineBreak();
            XSLFTextRun newRun = paragraph.addNewTextRun();
            newRun.setText(lines[i]);
            copyRunProperties(sourceRun, newRun); // Copiar propiedades al nuevo run
        }
        
        adjustParagraphAlignment(paragraph);
        enableAutoFit(textShape); // Ahora textShape está disponible
    }
}
    
    private void copyRunProperties(XSLFTextRun source, XSLFTextRun target) {
        target.setFontFamily(source.getFontFamily());
        target.setFontSize(source.getFontSize());
        target.setBold(source.isBold());
        target.setItalic(source.isItalic());
        target.setUnderlined(source.isUnderlined());
        target.setFontColor(source.getFontColor());
        target.setCharacterSpacing(source.getCharacterSpacing());
    }
    
    private void adjustParagraphAlignment(XSLFTextParagraph paragraph) {
        paragraph.setTextAlign(paragraph.getTextAlign());
        paragraph.setBullet(paragraph.isBullet());
        // Mantener otras propiedades de alineación
    }
    
    private void enableAutoFit(XSLFTextShape shape) {
        shape.setTextAutofit(TextAutofit.SHAPE);
        shape.setWordWrap(true);
    }

    private void replaceTextShapeWithImage(
            XSLFSlide slide,
            XSLFTextShape textShape,
            byte[] imageBytes
    ) throws IOException {

        XSLFPictureData pictureData = slide.getSlideShow()
                .addPicture(imageBytes, PictureData.PictureType.PNG);

        Rectangle2D anchor = textShape.getAnchor();

        XSLFPictureShape pictureShape = slide.createPicture(pictureData);
        pictureShape.setAnchor(anchor);

        slide.removeShape(textShape);
    }



}
