package com.vt.createmanagesubmit.dto;

import java.util.Date;

import com.vt.createmanagesubmit.modelos.Alumno;

public class AlumnoDTO {

	private Long id;

    private String nombreAsistente;

    private String nombreCurso;

    private String diasCursos;

    private String numeroHoras;

    private String numeroCorrelativoInterno;

    private String cliente;

    private String obra;

    private String codigo;

    private String notaAprovacion;

    private String relator;

    private String asistencia;

    private String estado;

    private String diploma;

    private String rut;

    private String correo;

    private String plantilla;

	private Date createdAt;
	
	private Date updatedAt;

    public AlumnoDTO(Alumno alumno) {
        this.id = alumno.getId();
        this.nombreAsistente = alumno.getNombreAsistente();
        this.nombreCurso = alumno.getNombreCurso();
        this.diasCursos = alumno.getDiasCursos();
        this.numeroHoras = alumno.getDuracion();
        this.numeroCorrelativoInterno = alumno.getNumeroCorrelativoInterno();
        this.cliente = alumno.getCliente();
        this.obra = alumno.getObra();
        this.codigo = alumno.getCodigo();
        this.notaAprovacion = alumno.getNotaAprobacion();
        this.relator = alumno.getRelator();
        this.asistencia = alumno.getAsistencia();
        this.estado = alumno.getEstado();
        this.diploma = alumno.getDiploma();
        this.rut = alumno.getRut();
        this.correo = alumno.getCorreo();
        this.plantilla = alumno.getPlantilla().getNombreCertificado();
        this.createdAt = alumno.getCreatedAt();
        this.updatedAt = alumno.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreAsistente() {
        return nombreAsistente;
    }

    public void setNombreAsistente(String nombreAsistente) {
        this.nombreAsistente = nombreAsistente;
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

    public String getNumeroHoras() {
        return numeroHoras;
    }

    public void setNumeroHoras(String numeroHoras) {
        this.numeroHoras = numeroHoras;
    }

    public String getNumeroCorrelativoInterno() {
        return numeroCorrelativoInterno;
    }

    public void setNumeroCorrelativoInterno(String numeroCorrelativoInterno) {
        this.numeroCorrelativoInterno = numeroCorrelativoInterno;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getObra() {
        return obra;
    }

    public void setObra(String obra) {
        this.obra = obra;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNotaAprovacion() {
        return notaAprovacion;
    }

    public void setNotaAprovacion(String notaAprovacion) {
        this.notaAprovacion = notaAprovacion;
    }

    public String getRelator() {
        return relator;
    }

    public void setRelator(String relator) {
        this.relator = relator;
    }

    public String getAsistencia() {
        return asistencia;
    }

    public void setAsistencia(String asistencia) {
        this.asistencia = asistencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDiploma() {
        return diploma;
    }

    public void setDiploma(String diploma) {
        this.diploma = diploma;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPlantilla() {
        return plantilla;
    }

    public void setPlantilla(String plantilla) {
        this.plantilla = plantilla;
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
