package com.vt.createmanagesubmit.servicios;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

//import org.apache.poi.ss.usermodel.Color;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vt.createmanagesubmit.dto.filtroDTO;
import com.vt.createmanagesubmit.exceptions.MissingAdminIdException;
import com.vt.createmanagesubmit.exceptions.MissingAlumnoIdException;
import com.vt.createmanagesubmit.exceptions.MissingNameOrRutException;
import com.vt.createmanagesubmit.exceptions.MissingTemplateException;
import com.vt.createmanagesubmit.modelos.Admin;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.AlumnoTemporal;
import com.vt.createmanagesubmit.modelos.Cliente;
import com.vt.createmanagesubmit.modelos.Curso;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.modelos.Relator;
import com.vt.createmanagesubmit.modelos.TareaProgramada;
import com.vt.createmanagesubmit.repositorios.RepositorioAdmin;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnoTemporal;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;
import com.vt.createmanagesubmit.repositorios.RepositorioCliente;
import com.vt.createmanagesubmit.repositorios.RepositorioCurso;
import com.vt.createmanagesubmit.repositorios.RepositorioPlantillas;
import com.vt.createmanagesubmit.repositorios.RepositorioRelator;
import com.vt.createmanagesubmit.repositorios.RepositorioTareasProgramadas;

import jakarta.persistence.criteria.Predicate;

@Service
public class Servicio {

    @Autowired
    private RepositorioAlumnos repoAlum;

    @Autowired
    private RepositorioPlantillas repoPlanti;

    @Autowired
    private RepositorioAdmin repoAdmin;

    @Autowired
    private RepositorioAlumnoTemporal repoAlumTemp;

    @Autowired
    private RepositorioCurso repoCurso;

    @Autowired
    private RepositorioCliente repoCliente;

    @Autowired
    private RepositorioRelator repoRelator;

    @Autowired
    @Lazy
    private ServicioArchivos servicioAr;

    @Autowired
    private RepositorioTareasProgramadas repoTarea;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String CORREO_EMPRESA = System.getenv("SP_MAIL_USERNAME");

    private final String STATIC_DIRECTORY = System.getenv("ST_FOLDER");

    private String CORREO_ADMIN = System.getenv("AD_MAIL");

    private String SecretKeyVar = System.getenv("ENCRYPT_KEY_ASIS");

    public String urlAsisencia = System.getenv("URL_PATH");

    public Alumno registrarNuevoAlumno(Alumno nuevoAlumno) {
        numeroCorrelativoAuto(nuevoAlumno);
        return nuevoAlumno;
    }

    public Alumno alumnoPorId(Long id) {
        return repoAlum.findById(id).orElse(null);
    }

    public void borrarAlumnoPorId(Long id) {
        Optional<Alumno> optAlumno = repoAlum.findById(id);
        if (optAlumno.isPresent()) {
            Alumno alumno = optAlumno.get();
            alumno.setEstado("borrado");
            repoAlum.save(alumno);
        } else {
            throw new MissingAlumnoIdException("No se encontró el alumno a borrar.");
        }
    }

    public Page<Alumno> todosLosAlumnos() {
        return repoAlum.findAll(PageRequest.of(0, 200, Sort.by("updatedAt").descending()));
    }

    public Page<Alumno> buscarConMultiplesFiltros(List<filtroDTO> filtros) {
        Specification<Alumno> spec = Specification.unrestricted();

        for (filtroDTO filtro : filtros) {
            spec = spec.and(crearEspecificacion(filtro));
        }

        return repoAlum.findAll(spec, PageRequest.of(0, Integer.MAX_VALUE, Sort.by("updatedAt").descending()));
    }

    private Specification<Alumno> crearEspecificacion(filtroDTO filtro) {
        return (root, query, cb) -> {
            String campo = filtro.getCampo();
            String valor = filtro.getValor();

            switch (campo) {
                case "rut":
                    return cb.like(root.get("rut"), "%" + valor + "%");
                case "nombreAsistente":
                    return cb.like(root.get("nombreAsistente"), "%" + valor + "%");
                case "nombreCurso":
                    return cb.like(root.get("nombreCurso"), "%" + valor + "%");
                case "estado":
                    if (valor.trim().equalsIgnoreCase("no aprobado") || valor.trim().equalsIgnoreCase("noAprobado")) {
                        return cb.equal(root.get("estado"), "noAprobado");
                    } else if (valor.trim().equalsIgnoreCase("aprobado")) {
                        return cb.equal(root.get("estado"), "aprobado");
                    } else {
                        return cb.equal(root.get("estado"), "revisionManual");
                    }
                case "diploma":
                    if (valor.trim().equalsIgnoreCase("enviado")) {
                        return cb.equal(root.get("diploma"), "enviado");
                    } else {
                        return cb.equal(root.get("diploma"), "noEnviado");
                    }
                case "cliente":
                    return cb.like(root.get("cliente"), "%" + valor + "%");
                case "identificador":
                    return cb.like(root.get("identificador"), "%" + valor + "%");
                case "relator":
                    return cb.like(root.get("relator"), "%" + valor + "%");
                case "correlativo":
                    return cb.like(root.get("numeroCorrelativoInterno"), "%" + valor + "%");
                default:
                    return cb.conjunction();
            }
        };
    }

    public Page<Alumno> buscarAlumnosPorCriterio(String filtro, String dato) {
        Page<Alumno> listaResultante;
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("updatedAt").descending());

        switch (filtro) {
            case "rut":
                listaResultante = repoAlum.findByRutContaining(dato, pageable); // Usando Containing para búsquedas
                                                                                // parciales
                break;
            case "nombreAsistente":
                listaResultante = repoAlum.findByNombreAsistenteContaining(dato, pageable);
                break;
            case "estado":
                if (dato.trim().equals("No aprobado") || dato.trim().equals("no aprobado")
                        || dato.trim().equals("noAprobado")) {
                    listaResultante = repoAlum.findByEstado("noAprobado", pageable);
                } else if (dato.trim().equals("aprobado") || dato.trim().equals("Aprobado")) {
                    listaResultante = repoAlum.findByEstado("aprobado", pageable);
                } else {
                    listaResultante = repoAlum.findByEstado("revisionManual", pageable);
                }
                break;
            case "diploma":
                if (dato.trim().equals("enviado") || dato.trim().equals("Enviado")) {
                    listaResultante = repoAlum.findByDiploma("enviado", pageable);
                } else {
                    listaResultante = repoAlum.findByDiploma("noEnviado", pageable);
                }
                break;
            case "correlativo":
                listaResultante = repoAlum.findByNumeroCorrelativoInternoContaining(dato, pageable);
                break;
            default:
                listaResultante = repoAlum.findAll(PageRequest.of(0, 200, Sort.by("updatedAt").descending()));
                break;
        }
        return listaResultante;
    }

    public List<Plantilla> buscarPlantillaPorCriterio(String dato) {
        List<Plantilla> plantillas = repoPlanti.findAllByNombreCertificadoContainingOrderByUpdatedAtDesc(dato);
        return plantillas;
    }

    public List<Plantilla> todasLasPlantillas() {
        return repoPlanti.findAllByOrderByUpdatedAtDesc();
    }

    public Plantilla plantillaPorId(Long id) {
        if (id == null) {
            throw new MissingTemplateException("La plantilla no existe o no hay ninguna seleccionada");
        }
        Optional<Plantilla> optPlantilla = repoPlanti.findById(id);
        if (optPlantilla.isPresent()) {
            Plantilla plantilla = optPlantilla.get();
            return plantilla;
        } else {
            throw new MissingTemplateException("La plantilla no existe.");
        }
    }

    public void guardarPlantilla(Plantilla plantilla) {
        repoPlanti.save(plantilla);
    }

    public Relator buscarRelatorPorId(Long relator) {
        Optional<Relator> relatoropt = repoRelator.findById(relator);
        if (relatoropt.isPresent()) {
            return relatoropt.get();
        } else {
            return null;
        }
    }

    public Curso cursoPorId(Long id) {
        Optional<Curso> cursOptional = repoCurso.findById(id);
        if (cursOptional.isPresent()) {
            return cursOptional.get();
        } else {
            return null;
        }
    }

    public Curso crearCurso(String nombre, String diasCursos, String duracion, Cliente cliente, String modalidad,
            String ubicacionSubida, String lugarYfechaEmision, Relator relator, Plantilla plantilla) {
        Curso curso = new Curso();
        curso.setCliente(cliente);
        curso.setDiasCursos(diasCursos);
        curso.setDuracion(duracion);
        curso.setLugarYfechaEmision(lugarYfechaEmision);
        curso.setModalidad(modalidad);
        curso.setNombreCurso(nombre);
        curso.setUbicacionSubida(ubicacionSubida);
        curso.setRelator(relator);
        curso.setPlantillaDiploma(plantilla);
        repoCurso.save(curso);
        return curso;
    }

    public Relator crearRelator(String nombre, String contacto, String foto, Float horasTrabajados) {
        Relator relator = new Relator();
        relator.setContacto(contacto);
        relator.setFoto(foto);
        relator.setHorasTrabajados(horasTrabajados);
        relator.setNombre(nombre);
        repoRelator.save(relator);
        return relator;
    }

    public void borrarPlantillaPorId(Long id) throws IOException {
        if (id == null) {
            throw new IOException("No se encontró la plantilla.");
        }
        Plantilla plantilla = plantillaPorId(id);
        if (!plantilla.getNombreCertificado().trim().equals("Error en encontrar plantilla")) {
            Path deletePlantillaPath = Paths.get(plantilla.getPathArchivo());
            try {
                Files.deleteIfExists(deletePlantillaPath);
            } catch (IOException ex) {
                throw new IOException("No se encontró la ruta de la plantilla.", ex);
            }
            if(plantilla.getTipo().equalsIgnoreCase("diploma")){
                List<Curso> cursos = repoCurso.findByPlantillaDiplomaId(id);
                for (Curso curso : cursos) {
                    repoAlum.deleteAll(curso.getAlumnos());
                }
                repoCurso.deleteAll(cursos);

            }else{
                List<Curso> cursos = repoCurso.findByPlantillaFlyerId(id);
                for (Curso curso : cursos) {
                    repoAlum.deleteAll(curso.getAlumnos());
                }
                repoCurso.deleteAll(cursos);

            }
            
            repoPlanti.delete(plantilla);
        }
    }

    public Optional<Plantilla> plantillaPorNombre(String nombre) {
        return repoPlanti.findByNombreCertificado(nombre);
    }

    public void guardarAdmin(Admin admin) {
        repoAdmin.save(admin);
    }

    public List<Admin> todasLosAdmin() {
        return repoAdmin.findAllByOrderByUpdatedAtDesc();
    }

    public Admin adminPorId(Long id) {
        Admin admin = repoAdmin.findById(id).orElse(null);
        return admin;

    }

    public Admin adminPorCorreo(String correo) {
        Optional<Admin> optAdmin = repoAdmin.findByCorreo(correo);
        if (optAdmin.isPresent()) {
            Admin admin = optAdmin.get();
            return admin;
        } else {
            return null;
        }
    }

    public void borrarAdminPorId(Long id) {
        Optional<Admin> optAdmin = repoAdmin.findById(id);
        if (optAdmin.isPresent()) {
            Admin admin = optAdmin.get();
            if (!admin.getCorreo().trim().equals(CORREO_ADMIN)) {
                repoAdmin.delete(admin);
            }
        } else {
            throw new MissingAdminIdException("No se encontro al administrador");
        }

    }

    public Alumno funcionEstadoManual(Alumno nuevoAlumno) {
        boolean tieneNota = nuevoAlumno.getNotaAprobacion() != null
                && !nuevoAlumno.getNotaAprobacion().trim().isEmpty();
        boolean tieneAsistencia = nuevoAlumno.getAsistencia() != null && !nuevoAlumno.getAsistencia().trim().isEmpty();
        boolean aprobado = false;
        try {
            if (tieneNota && !tieneAsistencia) {
                // Solo nota
                float nota = Float.parseFloat(nuevoAlumno.getNotaAprobacion().trim());
                if (nota >= nuevoAlumno.getCurso().getNotaMin()) {
                    aprobado = true;
                }
            } else if (!tieneNota && tieneAsistencia) {
                // Solo asistencia
                int asistenciaAlumno = Integer.parseInt(nuevoAlumno.getAsistencia().trim());
                if (asistenciaAlumno >= nuevoAlumno.getCurso().getAsistenciaMin()) {
                    aprobado = true;
                }
            } else if (tieneNota && tieneAsistencia) {
                // Ambos
                float nota = Float.parseFloat(nuevoAlumno.getNotaAprobacion().trim());
                int asistenciaAlumno = Integer.parseInt(nuevoAlumno.getAsistencia().trim());
                if (nota >= nuevoAlumno.getCurso().getNotaMin()
                        && asistenciaAlumno >= nuevoAlumno.getCurso().getAsistenciaMin()) {
                    aprobado = true;
                }
            } else {
                // No hay nota ni asistencia
                nuevoAlumno.setEstado("revisionManual");
            }
            if (aprobado) {
                nuevoAlumno.setEstado("aprobado");
            } else if (!nuevoAlumno.getEstado().equals("revisionManual")) {
                nuevoAlumno.setEstado("noAprobado");
            }
        } catch (NumberFormatException e) {
            // Manejo de errores en caso de formato incorrecto
            nuevoAlumno.setEstado("revisionManual");
        }
        return nuevoAlumno;
    }

    public void numeroCorrelativoAuto(Alumno alumno) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH) + 1; // Se suma 1 porque enero es 0
        Long idC = alumno.getId();

        String numeroCorrelativo = String.format("%d-%02d-%d", year, month, idC);
        alumno.setNumeroCorrelativoInterno(numeroCorrelativo);

        repoAlum.save(alumno);
    }

    public Alumno comprobarYGuardar(Alumno nuevoAlumno, String orden) {
        if (nuevoAlumno.getAsistencia().isEmpty()) {
            nuevoAlumno.setAsistencia(null);
        }
        if (nuevoAlumno.getCorreo().isEmpty()) {
            nuevoAlumno.setCorreo(CORREO_EMPRESA);
        }
        if (nuevoAlumno.getEstado().isEmpty()) {
            nuevoAlumno.setEstado(null);
        }
        if (nuevoAlumno.getNombreAsistente().trim().isEmpty()) {
            nuevoAlumno.setNombreAsistente(null);
        }
        if (nuevoAlumno.getNotaAprobacion().isEmpty()) {
            nuevoAlumno.setNotaAprobacion(null);
        }
        if (nuevoAlumno.getRut().trim().isEmpty()) {
            nuevoAlumno.setRut(null);
        }

        if ((nuevoAlumno.getNombreAsistente() != null && !nuevoAlumno.getNombreAsistente().trim().isEmpty())
                || (nuevoAlumno.getRut() != null && !nuevoAlumno.getRut().trim().isEmpty())) {
            String estadoFormulario = nuevoAlumno.getEstado().trim();

            // Si el estado es 'auto', realizamos la evaluación automática
            if (estadoFormulario.equals("auto")) {
                nuevoAlumno = funcionEstadoManual(nuevoAlumno);
            }
            // Si el estado es 'aprobado' o 'noAprobado', no hacemos nada (se respeta la
            // elección manual)

            // Validación del correo
            if (nuevoAlumno.getCorreo() == null || nuevoAlumno.getCorreo().trim().isEmpty()) {
                nuevoAlumno.setCorreo(CORREO_EMPRESA);
            }

            String nombre = nuevoAlumno.getNombreAsistente().trim().toUpperCase();
            nuevoAlumno.setNombreAsistente(nombre);
            repoAlum.save(nuevoAlumno);
            numeroCorrelativoAuto(nuevoAlumno);
            return nuevoAlumno;
        } else {
            throw new MissingNameOrRutException(
                    "Uno de los dos campos(Nombre o Rut) debe tener contenido para guardar un alumno.");
        }
    }

    public void editarAlumno(Long id, String nombreAsistente, String nombreCurso, String diasCursos, String numeroHoras,
            String cliente, String identificador, String notaAprovacion, String relator, String asistencia,
            String estado, String diploma, String rut, String modalidad, String correo, Long plantillaId,
            String lugarYfechaEmision) {

        Optional<Alumno> optalumno = repoAlum.findById(id);
        if (optalumno.isPresent()) {
            Alumno alumno = optalumno.get();
            nombreAsistente = nombreAsistente.trim().toUpperCase();
            if (correo.trim().isEmpty()) {
                correo = CORREO_EMPRESA;
            }
            alumno.setNombreAsistente(normalizarValor(nombreAsistente));
            alumno.setNotaAprobacion(normalizarValor(notaAprovacion));
            alumno.setAsistencia(normalizarValor(asistencia));
            alumno.setDiploma(normalizarValor(diploma));
            alumno.setRut(normalizarValor(rut));
            alumno.setCorreo(normalizarValor(correo));
            if ((alumno.getNombreAsistente() != null && !alumno.getNombreAsistente().trim().isEmpty())
                    || (alumno.getRut() != null && !alumno.getRut().trim().isEmpty())) {
                Optional<Plantilla> optplantilla = repoPlanti.findById(plantillaId);
                if (optplantilla.isPresent()) {
                    Plantilla plantilla = optplantilla.get();
                    alumno.getCurso().setPlantillaDiploma(plantilla);;
                    if (!estado.equals("auto")) {
                        alumno.setEstado(normalizarValor(estado));
                    } else {
                        alumno = funcionEstadoManual(alumno);
                    }

                    repoAlum.save(alumno);
                } else {
                    throw new MissingTemplateException("No se encontró la plantilla seleccionada");
                }
            } else {
                throw new MissingNameOrRutException(
                        "Uno de los dos campos(Nombre o Rut) debe tener contenido para guardar un alumno.");
            }
        } else {
            throw new MissingAlumnoIdException("No se encontró al alumno a editar");
        }
    }

    private String normalizarValor(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }

    public void cambiarPlantilla(Long idPlantilla, MultipartFile nuevaPlantilla) throws IOException {
        // Obtener la plantilla desde la BD
        Plantilla plantilla = repoPlanti.findById(idPlantilla)
                .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada"));

        // Verificar si ya existe un archivo con el mismo nombre
        String originalFilename = nuevaPlantilla.getOriginalFilename();
        String relativePath = "/plantillas/" + generarNombreUnico(originalFilename, "/plantillas/");
        Path plantillaPath = Paths.get(STATIC_DIRECTORY + relativePath);

        // Crear directorios si no existen
        Files.createDirectories(plantillaPath.getParent());

        // Guardar el archivo en el sistema
        Files.write(plantillaPath, nuevaPlantilla.getBytes());

        // Borrar el archivo anterior
        if (plantilla.getPathArchivo() != null && !plantilla.getPathArchivo().equals(relativePath)) {
            Path oldPlantillaPath = Paths.get(STATIC_DIRECTORY + plantilla.getPathArchivo());
            Files.deleteIfExists(oldPlantillaPath);
        }

        // Actualizar la ruta en la BD
        plantilla.setPathArchivo(STATIC_DIRECTORY + relativePath);
        repoPlanti.save(plantilla);
    }

    public String guardarArchivo(MultipartFile archivo, String subdirectorio) throws IOException {
        String originalFilename = archivo.getOriginalFilename();
        String relativePath = subdirectorio + generarNombreUnico(originalFilename, subdirectorio);
        Path fullPath = Paths.get(STATIC_DIRECTORY + relativePath);

        // Crear directorios si no existen
        Files.createDirectories(fullPath.getParent());

        // Guardar el archivo
        Files.write(fullPath, archivo.getBytes());

        return STATIC_DIRECTORY + relativePath;
    }

    private String generarNombreUnico(String originalFilename, String subdirectorio) throws IOException {
        Path directoryPath = Paths.get(STATIC_DIRECTORY + subdirectorio);

        // Asegurarnos de que el directorio existe
        Files.createDirectories(directoryPath);

        // Ruta del archivo inicial
        Path filePath = directoryPath.resolve(originalFilename);

        // Si el archivo ya existe, generar un nuevo nombre
        if (Files.exists(filePath)) {
            String baseName = originalFilename.contains(".")
                    ? originalFilename.substring(0, originalFilename.lastIndexOf('.'))
                    : originalFilename;
            String extension = originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                    : "";
            String timestamp = String.valueOf(System.currentTimeMillis());
            return baseName + "_" + timestamp + extension;
        }

        // Si no existe, devolver el nombre original
        return originalFilename;
    }

    public String clonarArchivo(String archivoExistente, String subdirectorio) throws IOException {
        Path sourcePath = Paths.get(archivoExistente);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String newFileName = "clon_" + timestamp + "_" + sourcePath.getFileName().toString();
        String relativePath = subdirectorio + newFileName;
        Path destinationPath = Paths.get(STATIC_DIRECTORY + relativePath);

        // Crear directorios si no existen
        Files.createDirectories(destinationPath.getParent());

        // Copiar el archivo
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

        return STATIC_DIRECTORY + relativePath;
    }

    public void registrarAdmin(String correo, String nombre, String password, String role) {
        // Encriptar la contraseña
        String passwordEncriptada = passwordEncoder.encode(password);

        Admin admin = new Admin();

        admin.setCorreo(correo);
        admin.setNombre(nombre);
        admin.setContrasena(passwordEncriptada);
        admin.setRol(role);
        repoAdmin.save(admin);
    }

    public Admin passwordConfirmacion(String correoLogin, String passwordLogin) {
        Optional<Admin> adminOptional = repoAdmin.findByCorreo(correoLogin);
        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            if (BCrypt.checkpw(passwordLogin, admin.getContrasena())) {
                return admin;
            }
        }
        return null;
    }

    public Page<TareaProgramada> todasLasTareas() {
        return repoTarea.findAll(PageRequest.of(0, 20, Sort.by("updatedAt").descending()));
    }

    public void borrarTareaPorId(Long id) {
        repoTarea.deleteById(id);
    }

    public String decryptId(String encryptedId) throws Exception {
        String secretKey = SecretKeyVar;
        try {
            // Revertir cambios del Base64 URL-safe
            encryptedId = encryptedId.replace("_", "/").replace("-", "+");

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedId);

            byte[] key = secretKey.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // 128 bits

            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] decrypted = cipher.doFinal(encryptedBytes);

            return new String(decrypted, "UTF-8");

        } catch (Exception e) {
            throw new Exception("Error al desencriptar el ID.", e);
        }
    }

    public String encryptId(String id) throws Exception {
        String secretKey = SecretKeyVar;
        MessageDigest sha = null;
        try {
            byte[] key = secretKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // 128 bits
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(id.getBytes("UTF-8"));
            String base64Encrypted = Base64.getEncoder().encodeToString(encrypted);
            base64Encrypted = base64Encrypted.replace("/", "_").replace("+", "-");
            return base64Encrypted;
        } catch (Exception e) {
            throw new Exception("Error al encriptar el ID.", e);
        }
    }

    // Genera el QR como byte[] imagen PNG
    public byte[] generateQrImageBytes(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB();
                bufferedImage.setRGB(x, y, rgb);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }

    // Genera QR por id de curso: devuelve base64+link
    public Map<String, String> generarQrParaCurso(Long cursoId) throws Exception {
        Curso c = cursoPorId(cursoId);
        String encrypted = encryptId(String.valueOf(c.getId()));
        String link = urlAsisencia + "/marcarAsistenciaCurso/" + encrypted;
        byte[] png = generateQrImageBytes(link, 300, 300);
        String base64 = Base64.getEncoder().encodeToString(png);
        Map<String, String> res = new HashMap<>();
        res.put("imageBase64", base64);
        res.put("link", link);
        res.put("nombreCurso", c.getNombreCurso());
        return res;
    }

    // Para crear curso desde params y devolver QR (uso simple)
    @Transactional
    public Map<String, String> createCursoAndGenerateQr(Map<String, String> params) throws Exception {
        String cursoTemporal = params.get("idCurso");
        String encrypted = encryptId(String.valueOf(cursoTemporal));
        String link = urlAsisencia + "/marcarAsistenciaCurso/" + encrypted;
        byte[] png = generateQrImageBytes(link, 300, 300);
        String base64 = Base64.getEncoder().encodeToString(png);
        Curso curso = cursoPorId(Long.valueOf(cursoTemporal));
        Map<String, String> res = new HashMap<>();
        res.put("imageBase64", base64);
        res.put("link", link);
        res.put("nombreCurso", curso.getNombreCurso());
        res.put("id", String.valueOf(cursoTemporal));
        return res;
    }

    public void procesarAsistencia(String nombre, String mail, String rut, String idEncriptada) {
        validarRut(rut);
        Long id;
        try {
            id = Long.valueOf(decryptId(idEncriptada));
            Curso curso = repoCurso.findById(id)
                    .orElseThrow(() -> new RuntimeException("Curso temporal no encontrado"));

            AlumnoTemporal alumnoTemporal = new AlumnoTemporal();
            alumnoTemporal.setNombreAsistente(nombre.toUpperCase());
            alumnoTemporal.setCorreo(mail);
            alumnoTemporal.setRut(rut);
            alumnoTemporal.setCursoTemporal(curso);
            repoAlumTemp.save(alumnoTemporal);
        } catch (NumberFormatException e) {
            new RuntimeException("Error de ID");
            e.printStackTrace();
        } catch (Exception e) {
            new RuntimeException("Error");
            e.printStackTrace();
        }

    }

    public void borrarAlumnoTemporalPorId(Long id) {
        Optional<AlumnoTemporal> alumnoTempOpt = repoAlumTemp.findById(id);
        if (alumnoTempOpt.isPresent()) {
            AlumnoTemporal alumnoTemporal = alumnoTempOpt.get();
            repoAlumTemp.delete(alumnoTemporal);
        }

    }

    public void alumnoVerificado(Long alumnoId, String asistencia, String nota) {
        Optional<AlumnoTemporal> alumnoTempOpt = repoAlumTemp.findById(alumnoId);
        if (alumnoTempOpt.isPresent()) {
            AlumnoTemporal alumnoTemporal = alumnoTempOpt.get();
            Alumno alumno = new Alumno();
            alumno.setAsistencia(asistencia);
            alumno.setNotaAprobacion(nota);
            alumno.setNombreAsistente(alumnoTemporal.getNombreAsistente());
            alumno.setCorreo(alumnoTemporal.getCorreo());
            alumno.setRut(alumnoTemporal.getRut());
            alumno.setDiploma("noEnviado");
            alumno.setEstado("aprobado");
            alumno.setCurso(alumnoTemporal.getCursoTemporal());
            comprobarYGuardar(alumno, "No");
            repoAlumTemp.delete(alumnoTemporal);
        }
    }

    private void validarRut(String rut) {

        if (rut == null || rut.isBlank()) {
            throw new IllegalArgumentException("RUT vacío");
        }

        rut = rut.replace(".", "")
                .replace(" ", "")
                .toUpperCase();

        if (!rut.contains("-")) {
            throw new IllegalArgumentException("Formato de RUT inválido");
        }

        String[] partes = rut.split("-");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Formato de RUT inválido");
        }

        String cuerpo = partes[0];
        String dvIngresado = partes[1];

        // Máx 8 dígitos (sin considerar puntos)
        if (!cuerpo.matches("\\d{1,8}")) {
            throw new IllegalArgumentException("RUT con cantidad de dígitos inválida");
        }

        if (!dvIngresado.matches("[0-9K]")) {
            throw new IllegalArgumentException("Dígito verificador inválido");
        }

        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int resto = 11 - (suma % 11);

        String dvCalculado;
        if (resto == 11)
            dvCalculado = "0";
        else if (resto == 10)
            dvCalculado = "K";
        else
            dvCalculado = String.valueOf(resto);

        if (!dvCalculado.equals(dvIngresado)) {
            throw new IllegalArgumentException("Dígito verificador incorrecto");
        }
    }

    public List<Cliente> todosLosClientes() {
        return repoCliente.findAllByOrderByUpdatedAtDesc();
    }

    public void guardarCliente(Cliente cliente) {
        repoCliente.save(cliente);
    }

    public Cliente clientePorId(Long id)throws Exception{
        
        Optional<Cliente> clienteOpt= repoCliente.findById(id);

        return clienteOpt.get();

    }

    public Page<Cliente> buscarConMultiplesFiltrosCliente(List<Map<String, String>> filtros) {

        Specification<Cliente> spec = Specification.unrestricted();

        for (Map<String, String> filtro : filtros) {
            String campo = filtro.get("campo");
            String valor = filtro.get("valor");

            if (campo != null && valor != null && !valor.isBlank()) {
                spec = spec.and(crearEspecificacionCliente(campo, valor));
            }
        }

        return repoCliente.findAll(
                spec,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("updatedAt").descending())
        );
    }

    private Specification<Cliente> crearEspecificacionCliente(String campo, String valor) {

        return (root, query, cb) -> {

            switch (campo) {

                case "nombreCliente":
                    return cb.like(
                            cb.lower(root.get("nombreCliente")),
                            "%" + valor.toLowerCase() + "%"
                    );

                case "identificador":
                    return cb.like(
                            cb.lower(root.get("identificador")),
                            "%" + valor.toLowerCase() + "%"
                    );

                case "pathLogo":
                    return cb.like(
                            cb.lower(root.get("pathLogo")),
                            "%" + valor.toLowerCase() + "%"
                    );

                default:
                    return cb.conjunction();
            }
        };
    }



    public List<Relator> todosLosRelatores() {
        return repoRelator.findAllByOrderByUpdatedAtDesc();
    }

    public void guardarRelator(Relator relator) {
        repoRelator.save(relator);
    }

    public Relator relatorPorId(Long Id) throws Exception{
        Optional<Relator> relatorOpt = repoRelator.findById(Id);
        return relatorOpt.get();
    }

    public Page<Relator> buscarConMultiplesFiltrosRelator(List<Map<String, String>> filtros) {

        Specification<Relator> spec = Specification.unrestricted();

        for (Map<String, String> filtro : filtros) {
            String campo = filtro.get("campo");
            String valor = filtro.get("valor");

            if (campo != null && valor != null && !valor.isBlank()) {
                spec = spec.and(crearEspecificacionRelator(campo, valor));
            }
        }

        return repoRelator.findAll(
                spec,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by("updatedAt").descending()));
    }

    private Specification<Relator> crearEspecificacionRelator(String campo, String valor) {

        return (root, query, cb) -> {

            switch (campo) {

                case "nombre":
                    return cb.like(
                            cb.lower(root.get("nombre")),
                            "%" + valor.toLowerCase() + "%");

                case "contacto":
                    return cb.like(
                            cb.lower(root.get("contacto")),
                            "%" + valor.toLowerCase() + "%");

                case "horasMin":
                    return cb.greaterThanOrEqualTo(
                            root.get("horasTrabajados"),
                            Float.valueOf(valor));

                case "horasMax":
                    return cb.lessThanOrEqualTo(
                            root.get("horasTrabajados"),
                            Float.valueOf(valor));

                case "datosExtras":
                    return cb.like(
                            cb.lower(root.get("datosExtras")),
                            "%" + valor.toLowerCase() + "%");

                default:
                    return cb.conjunction();
            }
        };
    }

    public List<Curso> todosLosCursos(){
        return repoCurso.findAllByOrderByUpdatedAtDesc();
    }

    public void guardarCurso(Curso curso){
        repoCurso.save(curso);
    }


}
