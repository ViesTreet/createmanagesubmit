package com.vt.createmanagesubmit.modelos;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="alumnos")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "plantilla" })
public class Alumno {
    
    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

    private String nombreAsistente;

    private String numeroCorrelativoInterno;

    private String notaAprobacion;

    private String asistencia;

    private String estado;

    private String diploma;

    private String rut;

    private String correo;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "curso_id") 
    private Curso curso;

    @Column(updatable=false)
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createdAt;
	
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date updatedAt;

    public Alumno() {
    }
    
    public Alumno(Long id, String nombreAsistente, String numeroCorrelativoInterno,
            String notaAprobacion, String asistencia, String estado, String diploma, String rut, String correo,
            Curso curso, Date createdAt, Date updatedAt) {
        this.id = id;
        this.nombreAsistente = nombreAsistente;
        this.numeroCorrelativoInterno = numeroCorrelativoInterno;
        this.notaAprobacion = notaAprobacion;
        this.asistencia = asistencia;
        this.estado = estado;
        this.diploma = diploma;
        this.rut = rut;
        this.correo = correo;
        this.curso = curso;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
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

    @PrePersist
	protected void onCreated() {
		this.createdAt = new Date();
        this.updatedAt = new Date();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = new Date();
	}

    
}
