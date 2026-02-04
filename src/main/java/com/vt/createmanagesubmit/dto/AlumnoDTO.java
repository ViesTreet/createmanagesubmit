package com.vt.createmanagesubmit.dto;

import java.util.Date;

import com.vt.createmanagesubmit.modelos.Alumno;


public class AlumnoDTO {

    private Long id;

    private String nombreAsistente;

    private String numeroCorrelativoInterno;

    private String notaAprobacion;

    private String asistencia;

    private String estado;

    private String diploma;

    private String rut;

    private String correo;

    private String curso;

    private String cliente;

    private String relator;

	private Date createdAt;
	
	private Date updatedAt;

    public AlumnoDTO() {
    }

    public AlumnoDTO(Alumno alumno) {
        this.id = alumno.getId() ;
        this.nombreAsistente = alumno.getNombreAsistente() ;
        this.numeroCorrelativoInterno = alumno.getNumeroCorrelativoInterno() ;
        this.notaAprobacion = alumno.getNotaAprobacion() ;
        this.asistencia = alumno.getAsistencia() ;
        this.estado = alumno.getEstado() ;
        this.diploma = alumno.getDiploma() ;
        this.rut = alumno.getRut() ;
        this.correo = alumno.getCorreo() ;
        this.curso = alumno.getCurso().getNombreCurso() ;
        this.cliente = alumno.getCurso().getCliente().getNombreCliente();
        this.relator = alumno.getCurso().getRelator().getNombre();
        this.createdAt = alumno.getCreatedAt() ;
        this.updatedAt = alumno.getUpdatedAt() ;
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

    public String getNumeroCorrelativoInterno() {
        return numeroCorrelativoInterno;
    }

    public void setNumeroCorrelativoInterno(String numeroCorrelativoInterno) {
        this.numeroCorrelativoInterno = numeroCorrelativoInterno;
    }

    public String getNotaAprobacion() {
        return notaAprobacion;
    }

    public void setNotaAprobacion(String notaAprobacion) {
        this.notaAprobacion = notaAprobacion;
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

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
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

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getRelator() {
        return relator;
    }

    public void setRelator(String relator) {
        this.relator = relator;
    }
    
    
}