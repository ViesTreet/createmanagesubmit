package com.vt.createmanagesubmit.servicios;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import com.vt.createmanagesubmit.config.ShapeConfig;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Curso;
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

    private static final Map<String, String> regionFormateada = Map.ofEntries(
            Map.entry("arica", "Región de Arica y Parinacota"),
            Map.entry("tarapaca", "Región de Tarapacá"),
            Map.entry("antofagasta", "Región de Antofagasta"),
            Map.entry("atacama", "Región de Atacama"),
            Map.entry("coquimbo", "Región de Coquimbo"),
            Map.entry("valparaiso", "Región de Valparaíso"),
            Map.entry("metropolitana", "Región Metropolitana de Santiago"),
            Map.entry("ohiggins", "Región del Libertador General Bernardo O'Higgins"),
            Map.entry("maule", "Región del Maule"),
            Map.entry("nuble", "Región de Ñuble"),
            Map.entry("biobio", "Región del Biobío"),
            Map.entry("araucania", "Región de La Araucanía"),
            Map.entry("rios", "Región de Los Ríos"),
            Map.entry("lagos", "Región de Los Lagos"),
            Map.entry("aysen", "Región de Aysén"),
            Map.entry("magallanes", "Región de Magallanes y de la Antártica Chilena"));

    private String nombreRegionBonito(String regionDB) {
        return regionFormateada.getOrDefault(regionDB, regionDB);
    }

    private static final Map<String, ShapeConfig> REGION_SHAPES = new HashMap<>();

    static {
        REGION_SHAPES.put("arica", new ShapeConfig(new Color(255, 179, 186))); // #ffb3ba
        REGION_SHAPES.put("tarapaca", new ShapeConfig(new Color(255, 47, 148))); // #ff2f94
        REGION_SHAPES.put("antofagasta", new ShapeConfig(new Color(255, 214, 165))); // #ffd6a5
        REGION_SHAPES.put("atacama", new ShapeConfig(new Color(255, 173, 173))); // #ffadad
        REGION_SHAPES.put("coquimbo", new ShapeConfig(new Color(202, 255, 191))); // #caffbf
        REGION_SHAPES.put("valparaiso", new ShapeConfig(new Color(155, 246, 255))); // #9bf6ff
        REGION_SHAPES.put("metropolitana", new ShapeConfig(new Color(253, 220, 101))); // #fddc65 (RM)
        REGION_SHAPES.put("ohiggins", new ShapeConfig(new Color(232, 142, 255))); // #e88eff
        REGION_SHAPES.put("maule", new ShapeConfig(new Color(168, 255, 122))); // #a8ff7a
        REGION_SHAPES.put("nuble", new ShapeConfig(new Color(168, 255, 122))); // #a8ff7a
        REGION_SHAPES.put("biobio", new ShapeConfig(new Color(28, 254, 82))); // #1cfe52
        REGION_SHAPES.put("araucania", new ShapeConfig(new Color(189, 178, 255))); // #bdb2ff
        REGION_SHAPES.put("rios", new ShapeConfig(new Color(160, 196, 255))); // #a0c4ff
        REGION_SHAPES.put("lagos", new ShapeConfig(new Color(205, 180, 219))); // #cdb4db
        REGION_SHAPES.put("aysen", new ShapeConfig(new Color(189, 224, 254))); // #bde0fe
        REGION_SHAPES.put("magallanes", new ShapeConfig(new Color(208, 244, 222))); // #d0f4de
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> generateCertificateForAlumno(Long id) throws Exception {
        // Obtén la plantilla asociada al alumno
        Alumno alumno = servicio.alumnoPorId(id);
        Plantilla plantilla = alumno.getCurso().getPlantillaDiploma();
        if (plantilla == null || plantilla.getNombreCertificado().trim().equals("Error en encontrar plantilla")) {
            throw new Exception("No hay una plantilla asociada al Alumno " + alumno.getNombreAsistente());
        }
        Curso cursoAlumno = alumno.getCurso();
        String emision = "";
        if (cursoAlumno.getLugarYfechaEmision() == null) {
            LocalDateTime ahora = LocalDateTime.now();

            int dia = ahora.getDayOfMonth();
            String mes = ahora.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
            int anio = ahora.getYear();

            String regionBonita = nombreRegionBonito(cursoAlumno.getUbicacionSubida());

            emision = "Emitido el " + dia + " de " + mes + " de " + anio + ", en "
                    + cursoAlumno.getCiudad() + ", " + regionBonita;

            cursoAlumno.setLugarYfechaEmision(emision);
            servicio.guardarCurso(cursoAlumno);
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
        String qrCodeText = urlPath + "/generarCertificadoQr/" + encryptedId;

        ByteArrayOutputStream qrCodeOutputStream = generateQRCodeImage(qrCodeText, 200, 200);
        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        Map<String, String> alumnoData = new HashMap<>();
        alumnoData.put("nombre", alumno.getNombreAsistente());
        alumnoData.put("curso", alumno.getCurso().getNombreCurso());
        alumnoData.put("duracion", alumno.getCurso().getDuracion());
        alumnoData.put("nota", alumno.getNotaAprobacion());
        alumnoData.put("dias", alumno.getCurso().getDiasCursos());
        alumnoData.put("relator", alumno.getCurso().getRelator().getNombre());
        alumnoData.put("asistencia", alumno.getAsistencia());
        alumnoData.put("emision", alumno.getCurso().getLugarYfechaEmision());
        alumnoData.put("correlativo", alumno.getNumeroCorrelativoInterno());
        alumnoData.put("modalidad", alumno.getCurso().getModalidad());

        byte[] logoCliente = null;
        try {
            logoCliente = Files.readAllBytes(Paths.get(alumno.getCurso().getCliente().getPathLogo()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, byte[]> imageData = new HashMap<>();
        imageData.put("imagen_qr", qrCodeBytes);
        if (logoCliente != null) {
            imageData.put("imagen_cliente", logoCliente);
        }

        for (XSLFSlide slide : ppt.getSlides()) {
            processSlide(slide, alumnoData, imageData);
        }

        // Guarda el PPTX modificado en un archivo temporal
        // Crear carpeta temp
        Path tempDir = Paths.get(stPath, "temp");
        Files.createDirectories(tempDir);

        Path tempPptxPath = tempDir.resolve(alumno.getId() + ".pptx");
        Path tempPdfPath = tempDir.resolve(alumno.getId() + ".pdf");

        // Guardar PPTX
        try (OutputStream out = Files.newOutputStream(tempPptxPath)) {
            ppt.write(out);
        }

        ppt.close();

        // Convertir a PDF
        JodConverter
                .convert(tempPptxPath.toFile())
                .to(tempPdfPath.toFile())
                .execute();

        // Leer PDF
        byte[] pdfBytes = Files.readAllBytes(tempPdfPath);

        // Borrar temporales
        Files.deleteIfExists(tempPptxPath);
        Files.deleteIfExists(tempPdfPath);

        // Enviar correo electrónico al alumno con el PDF y el código QR como adjuntos

        sendEmailWithAttachments(alumno.getCorreo(), "Certificado de Curso", alumno, pdfBytes, qrCodeBytes);

        return CompletableFuture.completedFuture(null);
    }

    private ByteArrayOutputStream generateQRCodeImage(String text, int width, int height)
            throws WriterException, IOException {
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
                (double) maxLogoSize / originalHeight);

        int logoWidth = (int) (originalWidth * scale);
        int logoHeight = (int) (originalHeight * scale);

        // Escalar logo
        Image scaledLogo = logo.getScaledInstance(
                logoWidth,
                logoHeight,
                Image.SCALE_SMOOTH);

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
        if (name == null || name.isEmpty())
            return name;
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

    private void sendEmailWithAttachments(String toEmail, String subject, Alumno alumno, byte[] pdfBytes,
            byte[] qrCodeBytes) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(diplomasMail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        byte[] logoBytes = null;

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
                "<div style='max-width: 600px; margin: 0 auto; background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);'>"
                +
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
                "<p>Adjunto encontrarás tu certificado de participación en el/los curso(s) <strong>"
                + formatCourseName(alumno.getCurso().getNombreCurso())
                + "</strong>. Junto con un código QR que te permitirá descargarlo nuevamente desde nuestra página web.</p>"
                +
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
                "<p>Si tienes dudas, no dudes en contactarnos a través de <a href='mailto:contacto@e-volution.cl'>contacto@e-volution.cl</a>.</p>"
                +
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
    public CompletableFuture<Void> generateCertificateQR(String idEncriptada, HttpServletResponse response)
            throws Exception {
        Long alumnoId = decryptStudentId(idEncriptada);

        // Obtén el alumno por ID
        Alumno alumno = alumnoRepo.findById(alumnoId)
                .orElseThrow(() -> new Exception("Alumno no encontrado con ID " + alumnoId));

        // Obtén la plantilla asociada al alumno
        Plantilla plantilla = alumno.getCurso().getPlantillaDiploma();
        if (plantilla == null || plantilla.getNombreCertificado().trim().equals("Error en encontrar plantilla")) {
            throw new Exception("No hay una plantilla asociada al Alumno " + alumno.getNombreAsistente());
        }
        Curso cursoAlumno = alumno.getCurso();
        String emision = "";
        if (cursoAlumno.getLugarYfechaEmision() == null) {
            LocalDateTime ahora = LocalDateTime.now();

            int dia = ahora.getDayOfMonth();
            String mes = ahora.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
            int anio = ahora.getYear();

            String regionBonita = nombreRegionBonito(cursoAlumno.getUbicacionSubida());

            emision = "Emitido el " + dia + " de " + mes + " de " + anio + ", en "
                    + cursoAlumno.getCiudad() + ", " + regionBonita;

            cursoAlumno.setLugarYfechaEmision(emision);
            servicio.guardarCurso(cursoAlumno);
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
        String qrCodeText = urlPath + "/generarCertificadoQr/" + encryptedId;

        ByteArrayOutputStream qrCodeOutputStream = generateQRCodeImage(qrCodeText, 200, 200);
        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        // Crea un mapa de los datos del alumno que se usarán para reemplazar en los
        // placeholders
        Map<String, String> alumnoData = new HashMap<>();
        alumnoData.put("nombre", alumno.getNombreAsistente());
        alumnoData.put("curso", alumno.getCurso().getNombreCurso());
        alumnoData.put("duracion", alumno.getCurso().getDuracion());
        alumnoData.put("nota", alumno.getNotaAprobacion());
        alumnoData.put("dias", alumno.getCurso().getDiasCursos());
        alumnoData.put("relator", alumno.getCurso().getRelator().getNombre());
        alumnoData.put("asistencia", alumno.getAsistencia());
        alumnoData.put("emision", alumno.getCurso().getLugarYfechaEmision());
        alumnoData.put("correlativo", alumno.getNumeroCorrelativoInterno());
        alumnoData.put("modalidad", alumno.getCurso().getModalidad());

        byte[] logoCliente = null;
        try {
            logoCliente = Files.readAllBytes(Paths.get(alumno.getCurso().getCliente().getPathLogo()));
        } catch (Exception e) {
            // TODO: handle exception
        }

        Map<String, byte[]> imageData = new HashMap<>();
        imageData.put("imagen_qr", qrCodeBytes);
        if (logoCliente != null) {
            imageData.put("imagen_cliente", logoCliente);
        }

        // Procesa las slides y shapes
        for (XSLFSlide slide : ppt.getSlides()) {
            processSlide(slide, alumnoData, imageData);
        }

        Path tempDir = Paths.get(stPath, "temp");
        Files.createDirectories(tempDir);

        Path tempPptxPath = tempDir.resolve(alumno.getId() + ".pptx");
        Path tempPdfPath = tempDir.resolve(alumno.getId() + ".pdf");

        try (OutputStream out = Files.newOutputStream(tempPptxPath)) {
            ppt.write(out);
        }

        ppt.close();

        JodConverter
                .convert(tempPptxPath.toFile())
                .to(tempPdfPath.toFile())
                .execute();

        byte[] pdfBytes = Files.readAllBytes(tempPdfPath);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"certificado-" + alumno.getId() + ".pdf\"");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();

        Files.deleteIfExists(tempPptxPath);
        Files.deleteIfExists(tempPdfPath);

        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<byte[]> descargarCertificadosServicio(Long id) throws Exception {

        // Obtén la plantilla asociada al alumno
        Alumno alumno = servicio.alumnoPorId(id);
        Plantilla plantilla = alumno.getCurso().getPlantillaDiploma();
        if (plantilla == null || plantilla.getNombreCertificado().trim().equals("Error en encontrar plantilla")) {
            throw new Exception("No hay una plantilla asociada al Alumno " + alumno.getNombreAsistente());
        }
        Curso cursoAlumno = alumno.getCurso();
        String emision = "";
        if (cursoAlumno.getLugarYfechaEmision() == null) {
            LocalDateTime ahora = LocalDateTime.now();

            int dia = ahora.getDayOfMonth();
            String mes = ahora.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
            int anio = ahora.getYear();

            String regionBonita = nombreRegionBonito(cursoAlumno.getUbicacionSubida());

            emision = "Emitido el " + dia + " de " + mes + " de " + anio + ", en "
                    + cursoAlumno.getCiudad() + ", " + regionBonita;

            cursoAlumno.setLugarYfechaEmision(emision);
            servicio.guardarCurso(cursoAlumno);
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
        String qrCodeText = urlPath + "/generarCertificadoQr/" + encryptedId;

        ByteArrayOutputStream qrCodeOutputStream = generateQRCodeImage(qrCodeText, 200, 200);
        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        // Crea un mapa de los datos del alumno que se usarán para reemplazar en los
        // placeholders
        Map<String, String> alumnoData = new HashMap<>();
        alumnoData.put("nombre", alumno.getNombreAsistente());
        alumnoData.put("curso", alumno.getCurso().getNombreCurso());
        alumnoData.put("duracion", alumno.getCurso().getDuracion());
        alumnoData.put("nota", alumno.getNotaAprobacion());
        alumnoData.put("dias", alumno.getCurso().getDiasCursos());
        alumnoData.put("relator", alumno.getCurso().getRelator().getNombre());
        alumnoData.put("asistencia", alumno.getAsistencia());
        alumnoData.put("emision", alumno.getCurso().getLugarYfechaEmision());
        alumnoData.put("correlativo", alumno.getNumeroCorrelativoInterno());
        alumnoData.put("modalidad", alumno.getCurso().getModalidad());

        byte[] logoCliente = null;
        try {
            logoCliente = Files.readAllBytes(Paths.get(alumno.getCurso().getCliente().getPathLogo()));
        } catch (Exception e) {
            // TODO: handle exception
        }

        Map<String, byte[]> imageData = new HashMap<>();
        imageData.put("imagen_qr", qrCodeBytes);
        if (logoCliente != null) {
            imageData.put("imagen_cliente", logoCliente);
        }

        // Procesa las slides y shapes
        for (XSLFSlide slide : ppt.getSlides()) {
            processSlide(slide, alumnoData, imageData);
        }

        Path tempDir = Paths.get(stPath, "temp");
        Files.createDirectories(tempDir);

        Path tempPptxPath = tempDir.resolve(alumno.getId() + ".pptx");
        Path tempPdfPath = tempDir.resolve(alumno.getId() + ".pdf");

        try (OutputStream out = Files.newOutputStream(tempPptxPath)) {
            ppt.write(out);
        }

        ppt.close();

        JodConverter
                .convert(tempPptxPath.toFile())
                .to(tempPdfPath.toFile())
                .execute();

        byte[] pdfBytes = Files.readAllBytes(tempPdfPath);

        Files.deleteIfExists(tempPptxPath);
        Files.deleteIfExists(tempPdfPath);

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

    private void processTextShape(XSLFSlide slide, XSLFTextShape textShape, Map<String, String> data,
            Map<String, byte[]> imageData) {
        List<XSLFTextParagraph> paragraphs = new ArrayList<>(textShape.getTextParagraphs());

        for (XSLFTextParagraph paragraph : paragraphs) {
            String fullText = getFullParagraphText(paragraph);
            for (Map.Entry<String, byte[]> img : imageData.entrySet()) {
                String placeholder = "${" + img.getKey() + "}";
                if (fullText.contains(placeholder)) {
                    System.out.println(fullText);
                    try {
                        replaceTextShapeWithImage(
                                slide,
                                textShape,
                                img.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if (fullText.contains(placeholder)) {
                    if ("forma".equals(entry.getKey())) {
                        replaceShapePlaceholder(textShape, entry.getValue());
                        return;
                    }
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
            byte[] imageBytes) throws IOException {

        // 1. Leer dimensiones reales de la imagen
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IOException("No se pudo leer la imagen");
        }

        double imgWidth = image.getWidth();
        double imgHeight = image.getHeight();

        // 2. Anchor original (placeholder)
        Rectangle2D anchor = textShape.getAnchor();
        double targetHeight = anchor.getHeight();

        // 3. Calcular ancho proporcional
        double aspectRatio = imgWidth / imgHeight;
        double targetWidth = targetHeight * aspectRatio;

        // 4. Centrar horizontalmente (opcional pero recomendado)
        double x = anchor.getX() + (anchor.getWidth() - targetWidth) / 2;
        double y = anchor.getY();

        Rectangle2D newAnchor = new Rectangle2D.Double(
                x,
                y,
                targetWidth,
                targetHeight);

        // 5. Crear imagen en el slide
        XSLFPictureData pictureData = slide.getSlideShow()
                .addPicture(imageBytes, PictureData.PictureType.PNG);

        XSLFPictureShape pictureShape = slide.createPicture(pictureData);
        pictureShape.setAnchor(newAnchor);

        // 6. Eliminar el placeholder de texto
        slide.removeShape(textShape);
    }

    private void replaceShapePlaceholder(
            XSLFTextShape textShape,
            String regionKey) {

        ShapeConfig config = REGION_SHAPES.get(regionKey);
        if (config == null)
            return;

        // Cambiar color de relleno
        textShape.setFillColor(config.getColor());

        // Opcional: quitar borde
        textShape.setLineColor(null);

        // Eliminar el texto placeholder
        for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
            for (XSLFTextRun run : paragraph.getTextRuns()) {
                run.setText("");
            }
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<byte[]> descargarFlyerServicio(Curso curso) throws Exception {

        // Obtén la plantilla asociada al alumno
        Plantilla plantilla = curso.getPlantillaFlyer();
        if (plantilla == null || plantilla.getNombreCertificado().trim().equals("Error en encontrar plantilla")) {
            throw new Exception("No hay una plantilla asociada al Curso " + curso.getNombreCurso());
        }

        // Carga la plantilla PPTX desde pathArchivo
        String templatePath = plantilla.getPathArchivo();

        // Carga el archivo PPTX usando Apache POI
        XMLSlideShow ppt;
        try (FileInputStream inputStream = new FileInputStream(templatePath)) {
            ppt = new XMLSlideShow(inputStream);
        }
        DateTimeFormatter formatoDiaSemana = DateTimeFormatter.ofPattern("EEEE", new Locale("es", "ES"));

        DateTimeFormatter formatoDiaNumero = DateTimeFormatter.ofPattern("d");

        DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES"));

        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

        // Crea un mapa de los datos del alumno que se usarán para reemplazar en los
        // placeholders
        Map<String, String> cursoData = new HashMap<>();
        cursoData.put("forma", curso.getUbicacionSubida());
        cursoData.put("curso", curso.getNombreCurso());
        cursoData.put("ubicacion_del_curso", curso.getUbicacionDelCurso());
        cursoData.put("ubicacion_del_cliente", curso.getUbicacionCliente());
        cursoData.put("dia_semana", curso.getFechaInicio().format(formatoDiaSemana));
        cursoData.put("dia_numero", curso.getFechaInicio().format(formatoDiaNumero));
        cursoData.put("mes", curso.getFechaInicio().format(formatoMes));
        cursoData.put("hora_inicio", curso.getFechaInicio().format(formatoHora));
        cursoData.put("hora_termino", curso.getFechaFin().format(formatoHora));
        cursoData.put("relator", curso.getRelator().getNombre());
        cursoData.put("datos_relator", curso.getRelator().getDatosExtras());
        cursoData.put("modalidad", curso.getModalidad());

        byte[] logoClienteSup = null;
        byte[] logoClienteInf = null;
        byte[] fotoRelator = null;
        try {
            logoClienteSup = Files.readAllBytes(Paths.get(curso.getCliente().getPathLogo()));
        } catch (Exception e) {
            // TODO: handle exception
        }
        try {
            logoClienteInf = Files.readAllBytes(Paths.get(curso.getCliente().getPathLogoFooter()));
        } catch (Exception e) {
            // TODO: handle exception
        }
        try {
            fotoRelator = Files.readAllBytes(Paths.get(curso.getRelator().getFoto()));
        } catch (Exception e) {
            // TODO: handle exception
        }

        Map<String, byte[]> imageData = new HashMap<>();
        if (logoClienteSup != null) {
            imageData.put("imagen_cliente_superior", logoClienteSup);
        }
        if (logoClienteInf != null) {
            imageData.put("imagen_cliente_inferior", logoClienteInf);
        }
        if (fotoRelator != null) {
            imageData.put("foto_relator", fotoRelator);
        }

        // Procesa las slides y shapes
        for (XSLFSlide slide : ppt.getSlides()) {
            processSlide(slide, cursoData, imageData);
        }

        // Guarda el PPTX modificado en un archivo temporal
        File tempPptxFile = File.createTempFile("Flyer-", ".pptx");
        try (FileOutputStream out = new FileOutputStream(tempPptxFile)) {
            ppt.write(out);
        }

        // Cierra el PPTX para evitar problemas
        ppt.close();
        byte[] FlyerBytes = Files.readAllBytes(tempPptxFile.toPath());
        // Eliminar los archivos temporales
        tempPptxFile.delete();

        return CompletableFuture.completedFuture(FlyerBytes);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<byte[]> funcionParaPruebaDeDiplomas(Alumno alumno) throws Exception {

        // Obtén la plantilla asociada al alumno
        Plantilla plantilla = alumno.getCurso().getPlantillaDiploma();
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
        String qrCodeText = urlPath + "/generarCertificadoQr/" + encryptedId;

        ByteArrayOutputStream qrCodeOutputStream = generateQRCodeImage(qrCodeText, 200, 200);
        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        // Crea un mapa de los datos del alumno que se usarán para reemplazar en los
        // placeholders
        Map<String, String> alumnoData = new HashMap<>();
        alumnoData.put("nombre", alumno.getNombreAsistente());
        alumnoData.put("curso", alumno.getCurso().getNombreCurso());
        alumnoData.put("duracion", alumno.getCurso().getDuracion());
        alumnoData.put("nota", alumno.getNotaAprobacion());
        alumnoData.put("dias", alumno.getCurso().getDiasCursos());
        alumnoData.put("relator", alumno.getCurso().getRelator().getNombre());
        alumnoData.put("asistencia", alumno.getAsistencia());
        alumnoData.put("emision", alumno.getCurso().getLugarYfechaEmision());
        alumnoData.put("correlativo", alumno.getNumeroCorrelativoInterno());
        alumnoData.put("modalidad", alumno.getCurso().getModalidad());

        byte[] logoCliente = null;
        try {
            logoCliente = Files.readAllBytes(Paths.get(alumno.getCurso().getCliente().getPathLogo()));
        } catch (Exception e) {
            // TODO: handle exception
        }

        Map<String, byte[]> imageData = new HashMap<>();
        imageData.put("imagen_qr", qrCodeBytes);
        if (logoCliente != null) {
            imageData.put("imagen_cliente", logoCliente);
        }

        // Procesa las slides y shapes
        for (XSLFSlide slide : ppt.getSlides()) {
            processSlide(slide, alumnoData, imageData);
        }

        Path tempDir = Paths.get(stPath, "temp");
        Files.createDirectories(tempDir);

        Path tempPptxPath = tempDir.resolve(alumno.getId() + ".pptx");
        Path tempPdfPath = tempDir.resolve(alumno.getId() + ".pdf");

        try (OutputStream out = Files.newOutputStream(tempPptxPath)) {
            ppt.write(out);
        }

        ppt.close();

        JodConverter
                .convert(tempPptxPath.toFile())
                .to(tempPdfPath.toFile())
                .execute();

        byte[] pdfBytes = Files.readAllBytes(tempPdfPath);

        Files.deleteIfExists(tempPptxPath);
        Files.deleteIfExists(tempPdfPath);

        return CompletableFuture.completedFuture(pdfBytes);
    }

}
