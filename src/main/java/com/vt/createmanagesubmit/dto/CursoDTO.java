package com.vt.createmanagesubmit.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.vt.createmanagesubmit.modelos.Curso;

public class CursoDTO {

    private Long id;

    private String nombreCurso;

    private String diasCursos;

    private String duracion;

    private String cliente;

    private String modalidad;

    private String ubicacionSubida;

    private String ciudad;

    private String ubicacionDelCurso;

    private String ubicacionCliente;

    private List<JornadaDTO> jornadas;

    private Float horasRelatorCurso;

    private String lugarYfechaEmision;

    private int asistenciaMin;

    private float notaMin;

    private String plantillaDiploma;

    private String plantillaFlyer;

    private int alumnos;

    private int tareaProgramadas;

    private int alumnosTemporales;

    private String relator;

    private boolean asistenciaQr;

    private Date createdAt;

    private Date updatedAt;

    public CursoDTO() {
    }

    public CursoDTO(Curso curso) {

        this.id = curso.getId();
        this.nombreCurso = curso.getNombreCurso();
        this.diasCursos = curso.getDiasCursos();
        this.duracion = curso.getDuracion();

        this.cliente = curso.getCliente() != null
                ? curso.getCliente().getNombreCliente()
                : null;

        this.modalidad = curso.getModalidad();
        this.ubicacionSubida = curso.getUbicacionSubida();
        this.ciudad = curso.getCiudad();
        this.ubicacionDelCurso = curso.getUbicacionDelCurso();
        this.ubicacionCliente = curso.getUbicacionCliente();

        // 🔥 NUEVO: mapear jornadas
        if (curso.getJornadas() != null) {
            this.jornadas = curso.getJornadas()
                    .stream()
                    .map(j -> new JornadaDTO(
                            j.getId(),
                            j.getFechaInicio(),
                            j.getFechaFin()))
                    .collect(Collectors.toList());
        }

        this.horasRelatorCurso = curso.getHorasRelatorCurso();
        this.lugarYfechaEmision = curso.getLugarYfechaEmision();
        this.asistenciaMin = curso.getAsistenciaMin();
        this.notaMin = curso.getNotaMin();

        this.plantillaDiploma = curso.getPlantillaDiploma() != null
                ? curso.getPlantillaDiploma().getNombreCertificado()
                : null;

        this.plantillaFlyer = curso.getPlantillaFlyer() != null
                ? curso.getPlantillaFlyer().getNombreCertificado()
                : null;

        this.alumnos = curso.getAlumnos() != null
                ? curso.getAlumnos().size()
                : 0;

        this.tareaProgramadas = curso.getTareaProgramadas() != null
                ? curso.getTareaProgramadas().size()
                : 0;

        this.alumnosTemporales = curso.getAlumnosTemporales() != null
                ? curso.getAlumnosTemporales().size()
                : 0;

        this.relator = curso.getRelator() != null
                ? curso.getRelator().getNombre()
                : null;

        this.createdAt = curso.getCreatedAt();
        this.updatedAt = curso.getUpdatedAt();
        this.asistenciaQr = curso.isAsistenciaQr();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCurso() {
        return nombreCurso;
    }

    public void setNombreCurso(String nombreCurso) {
        this.nombreCurso = nombreCurso;
    }

    public String getDiasCursos() {
        return diasCursos;
    }

    public void setDiasCursos(String diasCursos) {
        this.diasCursos = diasCursos;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getUbicacionSubida() {
        return ubicacionSubida;
    }

    public void setUbicacionSubida(String ubicacionSubida) {
        this.ubicacionSubida = ubicacionSubida;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getUbicacionDelCurso() {
        return ubicacionDelCurso;
    }

    public void setUbicacionDelCurso(String ubicacionDelCurso) {
        this.ubicacionDelCurso = ubicacionDelCurso;
    }

    public String getUbicacionCliente() {
        return ubicacionCliente;
    }

    public void setUbicacionCliente(String ubicacionCliente) {
        this.ubicacionCliente = ubicacionCliente;
    }

    public List<JornadaDTO> getJornadas() {
        return jornadas;
    }

    public void setJornadas(List<JornadaDTO> jornadas) {
        this.jornadas = jornadas;
    }

    public Float getHorasRelatorCurso() {
        return horasRelatorCurso;
    }

    public void setHorasRelatorCurso(Float horasRelatorCurso) {
        this.horasRelatorCurso = horasRelatorCurso;
    }

    public String getLugarYfechaEmision() {
        return lugarYfechaEmision;
    }

    public void setLugarYfechaEmision(String lugarYfechaEmision) {
        this.lugarYfechaEmision = lugarYfechaEmision;
    }

    public int getAsistenciaMin() {
        return asistenciaMin;
    }

    public void setAsistenciaMin(int asistenciaMin) {
        this.asistenciaMin = asistenciaMin;
    }

    public float getNotaMin() {
        return notaMin;
    }

    public void setNotaMin(float notaMin) {
        this.notaMin = notaMin;
    }

    public String getPlantillaDiploma() {
        return plantillaDiploma;
    }

    public void setPlantillaDiploma(String plantillaDiploma) {
        this.plantillaDiploma = plantillaDiploma;
    }

    public String getPlantillaFlyer() {
        return plantillaFlyer;
    }

    public void setPlantillaFlyer(String plantillaFlyer) {
        this.plantillaFlyer = plantillaFlyer;
    }

    public int getAlumnos() {
        return alumnos;
    }

    public void setAlumnos(int alumnos) {
        this.alumnos = alumnos;
    }

    public int getTareaProgramadas() {
        return tareaProgramadas;
    }

    public void setTareaProgramadas(int tareaProgramadas) {
        this.tareaProgramadas = tareaProgramadas;
    }

    public int getAlumnosTemporales() {
        return alumnosTemporales;
    }

    public void setAlumnosTemporales(int alumnosTemporales) {
        this.alumnosTemporales = alumnosTemporales;
    }

    public String getRelator() {
        return relator;
    }

    public void setRelator(String relator) {
        this.relator = relator;
    }

    public boolean isAsistenciaQr() {
        return asistenciaQr;
    }

    public void setAsistenciaQr(boolean asistenciaQr) {
        this.asistenciaQr = asistenciaQr;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    

}