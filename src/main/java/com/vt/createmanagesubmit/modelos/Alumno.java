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

    @Column(length = 500)
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

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "plantilla_id") 
    private Plantilla plantilla;

    @Column(updatable=false)
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createdAt;
	
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date updatedAt;

    
    public Alumno() {
    }

    public Alumno(String asistencia, String cliente, String codigo, String correo, Date createdAt, String diasCursos, String diploma, String estado, Long id, String nombreAsistente, String nombreCurso, String notaAprovacion, String numeroCorrelativoInterno, String numeroHoras, String obra, Plantilla plantilla, String relator, String rut, Date updatedAt) {
        this.asistencia = asistencia;
        this.cliente = cliente;
        this.codigo = codigo;
        this.correo = correo;
        this.createdAt = createdAt;
        this.diasCursos = diasCursos;
        this.diploma = diploma;
        this.estado = estado;
        this.id = id;
        this.nombreAsistente = nombreAsistente;
        this.nombreCurso = nombreCurso;
        this.notaAprovacion = notaAprovacion;
        this.numeroCorrelativoInterno = numeroCorrelativoInterno;
        this.numeroHoras = numeroHoras;
        this.obra = obra;
        this.plantilla = plantilla;
        this.relator = relator;
        this.rut = rut;
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

    public Plantilla getPlantilla() {
        return plantilla;
    }

    public void setPlantilla(Plantilla plantilla) {
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

    @PrePersist
	protected void onCreated() {
		this.createdAt = new Date();
        this.updatedAt = new Date();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = new Date();
	}

    @Override
    public String toString() {
        return "Alumno [id=" + id + ", nombreAsistente=" + nombreAsistente + ", nombreCurso=" + nombreCurso
                + ", diasCursos=" + diasCursos + ", numeroHoras=" + numeroHoras + ", numeroCorrelativoInterno="
                + numeroCorrelativoInterno + ", cliente=" + cliente + ", obra=" + obra + ", codigo=" + codigo
                + ", notaAprovacion=" + notaAprovacion + ", relator=" + relator + ", asistencia=" + asistencia
                + ", estado=" + estado + ", diploma=" + diploma + ", rut=" + rut + ", correo=" + correo + ", plantilla="
                + plantilla + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
    }

    
}
