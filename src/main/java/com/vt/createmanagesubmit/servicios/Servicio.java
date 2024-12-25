package com.vt.createmanagesubmit.servicios;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;
import com.vt.createmanagesubmit.repositorios.RepositorioPlantillas;

@Service
public class Servicio {

    @Autowired
    private RepositorioAlumnos repoAlum;

    @Autowired
    private RepositorioPlantillas repoPlanti;

    @Autowired
    @Lazy
    private ServicioArchivos servicioAr;

    public static String CORREO_EMPRESA = "example@example.com";

    public Alumno registrarNuevoAlumno(Alumno nuevoAlumno){
        return repoAlum.save(nuevoAlumno);
    }

    public Alumno alumnoPorId(Long id){
        return repoAlum.findById(id).orElse(null);
    }

    public void borrarAlumnoPorId(Long id){
        Alumno alumno = repoAlum.findById(id).orElse(null);
        repoAlum.delete(alumno);
    }

    public Page<Alumno> todosLosAlumnos(){
        return repoAlum.findAll(PageRequest.of(0, 200, Sort.by("updatedAt").descending()));
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
            case "cliente":
                listaResultante = repoAlum.findByClienteContaining(dato, pageable); // Usando Containing
                break;
            case "obra":
                listaResultante = repoAlum.findByObraContaining(dato, pageable); // Usando Containing
                break;
            case "relator":
                listaResultante = repoAlum.findByRelatorContaining(dato, pageable); // Usando Containing
                break;
            default:
                listaResultante = repoAlum.findAll(PageRequest.of(0, 200, Sort.by("updatedAt").descending()));
                break;
        }
        return listaResultante;
    }
    
    public List<Plantilla> todasLasPlantillas(){
        return repoPlanti.findAll();
    }

    public Plantilla plantillaPorId(Long id){
        return repoPlanti.findById(id).orElse(null);
    }

    public Optional<Plantilla> plantillaPorNombre(String nombre){
        return repoPlanti.findByNombreCertificado(nombre);
    }

    public Alumno funcionEstadoManual(Alumno nuevoAlumno){
        boolean tieneNota = nuevoAlumno.getNotaAprovacion() != null && !nuevoAlumno.getNotaAprovacion().trim().isEmpty();
        boolean tieneAsistencia = nuevoAlumno.getAsistencia() != null && !nuevoAlumno.getAsistencia().trim().isEmpty();
        boolean aprobado = false;
        try {
            if (tieneNota && !tieneAsistencia) {
                // Solo nota
                float nota = Float.parseFloat(nuevoAlumno.getNotaAprovacion().trim());
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
                float nota = Float.parseFloat(nuevoAlumno.getNotaAprovacion().trim());
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
        if(nuevoAlumno.getNotaAprovacion().trim().isEmpty()){
            nuevoAlumno.setNotaAprovacion(null);
        }
        if(nuevoAlumno.getNumeroCorrelativoInterno().trim().isEmpty()){
            nuevoAlumno.setNumeroCorrelativoInterno(null);
        }
        if(nuevoAlumno.getNumeroHoras().trim().isEmpty()){
            nuevoAlumno.setNumeroHoras(null);
        }
        if(nuevoAlumno.getObra().trim().isEmpty()){
            nuevoAlumno.setObra(null);
        }
        if(nuevoAlumno.getRelator().trim().isEmpty()){
            nuevoAlumno.setRelator(null);
        }
        if(nuevoAlumno.getRut().trim().isEmpty()){
            nuevoAlumno.setRut(null);
        }

        if (nuevoAlumno.getNombreAsistente() != null && !nuevoAlumno.getNombreAsistente().trim().isEmpty()) {
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
    
            // Formateo de la fecha
            if (nuevoAlumno.getDiasCursos() != null && nuevoAlumno.getDiasCursos().contains("-")) {
                String[] fechaPartes = nuevoAlumno.getDiasCursos().trim().split("-");
                String fechaFormateada = "entre el " + fechaPartes[0] + " al " + fechaPartes[1];
                nuevoAlumno.setDiasCursos(fechaFormateada);
            }
    
            // Añadir 'horas' al número de horas
            if (nuevoAlumno.getNumeroHoras() != null && !nuevoAlumno.getNumeroHoras().trim().isEmpty()) {
                nuevoAlumno.setNumeroHoras(nuevoAlumno.getNumeroHoras().trim() + " horas");
            }
            
            repoAlum.save(nuevoAlumno);
            return nuevoAlumno;
        }else{
            return null;
        }
    }

    public void editarAlumno(
            Long id,
            String nombreAsistente,
            String nombreCurso,
            String diasCursos,
            String numeroHoras,
            String numeroCorrelativoInterno,
            String cliente,
            String obra,
            String codigo,
            String notaAprovacion,
            String relator,
            String asistencia,
            String estado,
            String diploma,
            String rut,
            String correo,
            Long plantillaId) {

        // Crear o actualizar el objeto Alumno
        Alumno alumno = repoAlum.findById(id).orElse(null);

        alumno.setNombreAsistente(normalizarValor(nombreAsistente));
        alumno.setNombreCurso(normalizarValor(nombreCurso));
        alumno.setDiasCursos(normalizarValor(diasCursos));
        alumno.setNumeroHoras(normalizarValor(numeroHoras));
        alumno.setNumeroCorrelativoInterno(normalizarValor(numeroCorrelativoInterno));
        alumno.setCliente(normalizarValor(cliente));
        alumno.setObra(normalizarValor(obra));
        alumno.setCodigo(normalizarValor(codigo));
        alumno.setNotaAprovacion(normalizarValor(notaAprovacion));
        alumno.setRelator(normalizarValor(relator));
        alumno.setAsistencia(normalizarValor(asistencia));
        alumno.setDiploma(normalizarValor(diploma));
        alumno.setRut(normalizarValor(rut));
        alumno.setCorreo(normalizarValor(correo));
        Plantilla plantilla = repoPlanti.findById(plantillaId).orElse(null);
        alumno.setPlantilla((plantilla));
        if(!estado.equals("auto")){
            alumno.setEstado(normalizarValor(estado));
        }else{
            alumno = funcionEstadoManual(alumno);
        }

        // Lógica adicional, como guardar en la base de datos, etc.
        repoAlum.save(alumno);
    }


    private String normalizarValor(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }

}




