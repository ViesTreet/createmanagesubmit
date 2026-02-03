package com.vt.createmanagesubmit.dto;

import java.time.LocalDateTime;
import java.util.Date;

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

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

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

	private Date createdAt;
	
	private Date updatedAt;

    public CursoDTO() {
    }

    public CursoDTO(Curso curso) {
        this.id = curso.getId();
        this.nombreCurso = curso.getNombreCurso();
        this.diasCursos = curso.getDiasCursos();
        this.duracion = curso.getDuracion();
        this.cliente = curso.getCliente().getNombreCliente();
        this.modalidad = curso.getModalidad();
        this.ubicacionSubida = curso.getUbicacionSubida();
        this.ciudad = curso.getCiudad();
        this.ubicacionDelCurso = curso.getUbicacionDelCurso();
        this.ubicacionCliente = curso.getUbicacionCliente();
        this.fechaInicio = curso.getFechaInicio();
        this.fechaFin = curso.getFechaFin();
        this.horasRelatorCurso = curso.getHorasRelatorCurso();
        this.lugarYfechaEmision = curso.getLugarYfechaEmision();
        this.asistenciaMin = curso.getAsistenciaMin();
        this.notaMin = curso.getNotaMin();
        this.plantillaDiploma = curso.getPlantillaDiploma().getNombreCertificado();
        this.plantillaFlyer = curso.getPlantillaFlyer().getNombreCertificado();
        this.alumnos = curso.getAlumnos().size();
        this.tareaProgramadas = curso.getTareaProgramadas().size();
        this.alumnosTemporales = curso.getAlumnosTemporales().size();
        this.relator = curso.getRelator().getNombre();
        this.createdAt = curso.getCreatedAt();
        this.updatedAt = curso.getUpdatedAt();
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

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
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