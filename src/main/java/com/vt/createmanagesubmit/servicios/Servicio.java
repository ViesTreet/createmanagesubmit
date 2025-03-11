package com.vt.createmanagesubmit.servicios;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.vt.createmanagesubmit.dto.filtroDTO;
import com.vt.createmanagesubmit.exceptions.MissingAdminIdException;
import com.vt.createmanagesubmit.exceptions.MissingAlumnoIdException;
import com.vt.createmanagesubmit.exceptions.MissingNameOrRutException;
import com.vt.createmanagesubmit.exceptions.MissingTemplateException;
import com.vt.createmanagesubmit.modelos.Admin;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.repositorios.RepositorioAdmin;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;
import com.vt.createmanagesubmit.repositorios.RepositorioPlantillas;



@Service
public class Servicio {

    @Autowired
    private RepositorioAlumnos repoAlum;

    @Autowired
    private RepositorioPlantillas repoPlanti;

    @Autowired
    private RepositorioAdmin repoAdmin;

    @Autowired
    @Lazy
    private ServicioArchivos servicioAr;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public static String CORREO_EMPRESA = "javito12ulloa@gmail.com";

    private static final String STATIC_DIRECTORY = "src/main/resources/static";


    public Alumno registrarNuevoAlumno(Alumno nuevoAlumno){
        return repoAlum.save(nuevoAlumno);
    }

    public Alumno alumnoPorId(Long id){
        return repoAlum.findById(id).orElse(null);
    }

    public void borrarAlumnoPorId(Long id){
        Optional<Alumno> optAlumno = repoAlum.findById(id);
        if(optAlumno.isPresent()){
            Alumno alumno = optAlumno.get();
            repoAlum.delete(alumno);
        }else{
            throw new MissingAlumnoIdException("No se encontró el alumno a borrar.");
        }
    }

    public Page<Alumno> todosLosAlumnos(){
        return repoAlum.findAll(PageRequest.of(0, 200, Sort.by("updatedAt").descending()));
    }

    public Page<Alumno> buscarConMultiplesFiltros(List<filtroDTO> filtros) {
        Specification<Alumno> spec = Specification.where(null);
        
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


    public Page<Alumno> buscarAlumnosPorCriterio(String filtro, String dato){
        Page<Alumno> listaResultante;
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("updatedAt").descending());
        
        switch (filtro) {
            case "rut":
                listaResultante = repoAlum.findByRutContaining(dato, pageable); // Usando Containing para búsquedas parciales
                break;
            case "nombreAsistente":
                listaResultante = repoAlum.findByNombreAsistenteContaining(dato, pageable);
                break;
            case "nombreCurso":
                listaResultante = repoAlum.findByNombreCursoContaining(dato, pageable); // Usando Containing
                break;
            case "estado":
                if(dato.trim().equals("No aprobado")||dato.trim().equals("no aprobado")||dato.trim().equals("noAprobado")){
                    listaResultante = repoAlum.findByEstado("noAprobado", pageable);
                }else if(dato.trim().equals("aprobado")||dato.trim().equals("Aprobado")){
                    listaResultante = repoAlum.findByEstado("aprobado", pageable);
                }else{
                    listaResultante = repoAlum.findByEstado("revisionManual", pageable);
                }
                break;      
            case "diploma":
                if(dato.trim().equals("enviado")||dato.trim().equals("Enviado")){
                    listaResultante = repoAlum.findByDiploma("enviado", pageable);
                }else{
                    listaResultante=repoAlum.findByDiploma("noEnviado", pageable);
                }
                break;
            case "cliente":
                listaResultante = repoAlum.findByClienteContaining(dato, pageable); // Usando Containing
                break;
            case "identificador":
                listaResultante = repoAlum.findByIdentificadorContaining(dato, pageable); // Usando Containing
                break;
            case "relator":
                listaResultante = repoAlum.findByRelatorContaining(dato, pageable); // Usando Containing
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

    public List<Plantilla> buscarPlantillaPorCriterio(String dato){
        List<Plantilla> plantillas = repoPlanti.findAllByNombreCertificadoContainingOrderByUpdatedAtDesc(dato);
        return plantillas;
    }
    
    public List<Plantilla> todasLasPlantillas(){
        return repoPlanti.findAllByOrderByUpdatedAtDesc();
    }

    public Plantilla plantillaPorId(Long id){
        if(id == null){
            throw new MissingTemplateException("La plantilla no existe o no hay ninguna seleccionada");
        }
        Optional<Plantilla> optPlantilla = repoPlanti.findById(id);
        if(optPlantilla.isPresent()){
            Plantilla plantilla = optPlantilla.get();
            return plantilla;
        }else{
            throw new MissingTemplateException("La plantilla no existe.");
        }
    }

    public void guardarPlantilla(Plantilla plantilla){
        repoPlanti.save(plantilla);
    }

    public void borrarPlantillaPorId(Long id) throws IOException{
        if(id == null){
            throw new IOException("No se encontró la plantilla.");
        }
        Plantilla plantilla = plantillaPorId(id);
        if(!plantilla.getNombreCertificado().trim().equals("Error en encontrar plantilla")){
            Path deletePlantillaPath = Paths.get(plantilla.getPathArchivo());
            try {
                Files.deleteIfExists(deletePlantillaPath);
            } catch (IOException ex) {
                throw new IOException("No se encontró la ruta de la plantilla.",ex);
            }
            List<Alumno> alumnos = plantilla.getAlumnos();
            repoAlum.deleteAll(alumnos);
            repoPlanti.delete(plantilla);
        }
    }

    public Optional<Plantilla> plantillaPorNombre(String nombre){
        return repoPlanti.findByNombreCertificado(nombre);
    }

    public void guardarAdmin(Admin admin){
        repoAdmin.save(admin);
    }

    public List<Admin> todasLosAdmin(){
        return repoAdmin.findAllByOrderByUpdatedAtDesc();
    }

    public Admin adminPorId(Long id){
        Admin admin = repoAdmin.findById(id).orElse(null);
        return admin;

    }

    public Admin adminPorCorreo(String correo){
        Optional<Admin> optAdmin = repoAdmin.findByCorreo(correo);
        if(optAdmin.isPresent()){
            Admin admin = optAdmin.get();
            return admin;
        }else{
            return null;
        }
    }

    public void borrarAdminPorId(Long id){
        Optional<Admin> optAdmin = repoAdmin.findById(id);
        if(optAdmin.isPresent()){
            Admin admin = optAdmin.get();
            if(!admin.getCorreo().trim().equals("admin@admin.com")){
                repoAdmin.delete(admin);
            }
        }else{
            throw new MissingAdminIdException("No se encontro al administrador");
        }
        
    }

    public Alumno funcionEstadoManual(Alumno nuevoAlumno){
        boolean tieneNota = nuevoAlumno.getNotaAprobacion() != null && !nuevoAlumno.getNotaAprobacion().trim().isEmpty();
        boolean tieneAsistencia = nuevoAlumno.getAsistencia() != null && !nuevoAlumno.getAsistencia().trim().isEmpty();
        boolean aprobado = false;
        try {
            if (tieneNota && !tieneAsistencia) {
                // Solo nota
                float nota = Float.parseFloat(nuevoAlumno.getNotaAprobacion().trim());
                if (nota >= nuevoAlumno.getPlantilla().getNotaMin()) {
                    aprobado = true;
                }
            } else if (!tieneNota && tieneAsistencia) {
                // Solo asistencia
                int asistenciaAlumno = Integer.parseInt(nuevoAlumno.getAsistencia().trim());
                if (asistenciaAlumno >= nuevoAlumno.getPlantilla().getAsistenciaMin()) {
                    aprobado = true;
                }
            } else if (tieneNota && tieneAsistencia) {
                // Ambos
                float nota = Float.parseFloat(nuevoAlumno.getNotaAprobacion().trim());
                int asistenciaAlumno = Integer.parseInt(nuevoAlumno.getAsistencia().trim());
                if (nota >= nuevoAlumno.getPlantilla().getNotaMin() && asistenciaAlumno >= nuevoAlumno.getPlantilla().getAsistenciaMin()) {
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

    public Alumno comprobarYGuardar(Alumno nuevoAlumno,String orden) {
        if(nuevoAlumno.getAsistencia().trim().isEmpty()){
            nuevoAlumno.setAsistencia(null);
        }
        if(nuevoAlumno.getCliente().trim().isEmpty()){
            nuevoAlumno.setCliente(null);
        }
        if(nuevoAlumno.getCodigo().trim().isEmpty()){
            nuevoAlumno.setCodigo(null);
        }
        if(nuevoAlumno.getCorreo().trim().isEmpty()){
            nuevoAlumno.setCorreo(CORREO_EMPRESA);
        }
        if(nuevoAlumno.getDiasCursos().trim().isEmpty()){
            nuevoAlumno.setDiasCursos(null);
        }
        if(nuevoAlumno.getEstado().trim().isEmpty()){
            nuevoAlumno.setEstado(null);
        }
        if(nuevoAlumno.getNombreAsistente().trim().isEmpty()){
            nuevoAlumno.setNombreAsistente(null);
        }
        if(nuevoAlumno.getNombreCurso().trim().isEmpty()){
            nuevoAlumno.setNombreCurso(null);
        }
        if(nuevoAlumno.getNotaAprobacion().trim().isEmpty()){
            nuevoAlumno.setNotaAprobacion(null);
        }
        if(nuevoAlumno.getDuracion().trim().isEmpty()){
            nuevoAlumno.setDuracion(null);
        }
        if(nuevoAlumno.getIdentificador().trim().isEmpty()){
            nuevoAlumno.setIdentificador(null);
        }
        if(nuevoAlumno.getRelator().trim().isEmpty()){
            nuevoAlumno.setRelator(null);
        }
        if(nuevoAlumno.getRut().trim().isEmpty()){
            nuevoAlumno.setRut(null);
        }
        if(nuevoAlumno.getModalidad().trim().isEmpty()){
            nuevoAlumno.setModalidad(null);
        }

        if ((nuevoAlumno.getNombreAsistente() != null && !nuevoAlumno.getNombreAsistente().trim().isEmpty())||(nuevoAlumno.getRut() != null && !nuevoAlumno.getRut().trim().isEmpty())) {
            String estadoFormulario = nuevoAlumno.getEstado().trim();
    
            // Si el estado es 'auto', realizamos la evaluación automática
            if (estadoFormulario.equals("auto")) {
                nuevoAlumno = funcionEstadoManual(nuevoAlumno);
            }
            // Si el estado es 'aprobado' o 'noAprobado', no hacemos nada (se respeta la elección manual)
    
            // Validación del correo
            if (nuevoAlumno.getCorreo() == null || nuevoAlumno.getCorreo().trim().isEmpty()) {
                nuevoAlumno.setCorreo(CORREO_EMPRESA);
            }
    
            String nombre=nuevoAlumno.getNombreAsistente().trim().toUpperCase();
            nuevoAlumno.setNombreAsistente(nombre);
            repoAlum.save(nuevoAlumno);
            numeroCorrelativoAuto(nuevoAlumno);
            return nuevoAlumno;
        }else{
            throw new MissingNameOrRutException("Uno de los dos campos(Nombre o Rut) debe tener contenido para guardar un alumno.");
        }
    }

    public void editarAlumno(Long id,String nombreAsistente,String nombreCurso,String diasCursos,String numeroHoras,String cliente,String identificador,String codigo,String notaAprovacion,String relator,String asistencia,String estado,String diploma,String rut,String modalidad,String correo,Long plantillaId, String lugarYfechaEmision) {

        Optional<Alumno> optalumno = repoAlum.findById(id);
        if(optalumno.isPresent()){
            Alumno alumno = optalumno.get();
            nombreAsistente = nombreAsistente.trim().toUpperCase();
            if (correo.trim().isEmpty()){
                correo = CORREO_EMPRESA;
            }
            alumno.setNombreAsistente(normalizarValor(nombreAsistente));
            alumno.setNombreCurso(normalizarValor(nombreCurso));
            alumno.setDiasCursos(normalizarValor(diasCursos));
            alumno.setDuracion(normalizarValor(numeroHoras));
            alumno.setCliente(normalizarValor(cliente));
            alumno.setIdentificador(normalizarValor(identificador));
            alumno.setModalidad(normalizarValor(modalidad));
            alumno.setCodigo(normalizarValor(codigo));
            alumno.setNotaAprobacion(normalizarValor(notaAprovacion));
            alumno.setRelator(normalizarValor(relator));
            alumno.setAsistencia(normalizarValor(asistencia));
            alumno.setDiploma(normalizarValor(diploma));
            alumno.setRut(normalizarValor(rut));
            alumno.setCorreo(normalizarValor(correo));
            alumno.setLugarYfechaEmision(lugarYfechaEmision);
            if((alumno.getNombreAsistente()!=null && !alumno.getNombreAsistente().trim().isEmpty())||(alumno.getRut() != null && !alumno.getRut().trim().isEmpty())){
                Optional<Plantilla> optplantilla = repoPlanti.findById(plantillaId);
                if (optplantilla.isPresent()){
                    Plantilla plantilla = optplantilla.get();
                    alumno.setPlantilla((plantilla));
                    if(!estado.equals("auto")){
                        alumno.setEstado(normalizarValor(estado));
                    }else{
                        alumno = funcionEstadoManual(alumno);
                    }

                    repoAlum.save(alumno);
                }else{
                    throw new MissingTemplateException("No se encontró la plantilla seleccionada");
                }
            }else{
                throw new MissingNameOrRutException("Uno de los dos campos(Nombre o Rut) debe tener contenido para guardar un alumno.");
            }
        }else{
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
            String baseName = originalFilename.contains(".") ? originalFilename.substring(0, originalFilename.lastIndexOf('.')) : originalFilename;
            String extension = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
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
    

    public void registrarAdmin(String correo,String nombre, String password) {
        // Encriptar la contraseña
        String passwordEncriptada = passwordEncoder.encode(password);

        Admin admin = new Admin();

        admin.setCorreo(correo);
        admin.setNombre(nombre);
        admin.setContrasena(passwordEncriptada);

        repoAdmin.save(admin);
    }

    
    public Admin passwordConfirmacion(String correoLogin,String passwordLogin){
        Optional<Admin> adminOptional = repoAdmin.findByCorreo(correoLogin);
        if(adminOptional.isPresent()){
            Admin admin = adminOptional.get();
            if(BCrypt.checkpw(passwordLogin, admin.getContrasena())){
                return admin;
            }
        }
        return null;
    }

}




