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

    private String duracion;

    private String numeroCorrelativoInterno;

    private String modalidad;

    private String cliente;

    private String identificador;

    private String codigo;

    private String notaAprobacion;

    private String relator;

    private String asistencia;

    private String estado;

    private String diploma;

    private String rut;

    private String correo;

    private String ubicacionSubida;

    private String lugarYfechaEmision;

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

    public Alumno(Long id, String nombreAsistente, String nombreCurso, String diasCursos, String duracion,
            String numeroCorrelativoInterno, String modalidad, String cliente, String identificador, String codigo,
            String notaAprobacion, String relator, String asistencia, String estado, String diploma, String rut,
            String correo, String ubicacionSubida, String lugarYfechaEmision, Plantilla plantilla, Date createdAt,
            Date updatedAt) {
        this.id = id;
        this.nombreAsistente = nombreAsistente;
        this.nombreCurso = nombreCurso;
        this.diasCursos = diasCursos;
        this.duracion = duracion;
        this.numeroCorrelativoInterno = numeroCorrelativoInterno;
        this.modalidad = modalidad;
        this.cliente = cliente;
        this.identificador = identificador;
        this.codigo = codigo;
        this.notaAprobacion = notaAprobacion;
        this.relator = relator;
        this.asistencia = asistencia;
        this.estado = estado;
        this.diploma = diploma;
        this.rut = rut;
        this.correo = correo;
        this.ubicacionSubida = ubicacionSubida;
        this.lugarYfechaEmision = lugarYfechaEmision;
        this.plantilla = plantilla;
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

    public void setDuracion(String numeroHoras) {
        this.duracion = numeroHoras;
    }

    public String getNumeroCorrelativoInterno() {
        return numeroCorrelativoInterno;
    }

    public void setNumeroCorrelativoInterno(String numeroCorrelativoInterno) {
        this.numeroCorrelativoInterno = numeroCorrelativoInterno;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String Modalidad) {
        this.modalidad = Modalidad;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNotaAprobacion() {
        return notaAprobacion;
    }

    public void setNotaAprobacion(String notaAprovacion) {
        this.notaAprobacion = notaAprovacion;
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
    public String getUbicacionSubida() {
        return ubicacionSubida;
    }

    public void setUbicacionSubida(String ubicacionSubida) {
        this.ubicacionSubida = ubicacionSubida;
    }

    public String getLugarYfechaEmision() {
        return lugarYfechaEmision;
    }

    public void setLugarYfechaEmision(String lugarYfechaEmision) {
        this.lugarYfechaEmision = lugarYfechaEmision;
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
                + ", diasCursos=" + diasCursos + ", duracion=" + duracion + ", numeroCorrelativoInterno="
                + numeroCorrelativoInterno + ", modalidad=" + modalidad + ", cliente=" + cliente + ", identificador="
                + identificador + ", codigo=" + codigo + ", notaAprobacion=" + notaAprobacion + ", relator=" + relator
                + ", asistencia=" + asistencia + ", estado=" + estado + ", diploma=" + diploma + ", rut=" + rut
                + ", correo=" + correo + ", ubicacionSubida=" + ubicacionSubida + ", lugarYfechaEmision="
                + lugarYfechaEmision + ", plantilla=" + plantilla + ", createdAt=" + createdAt + ", updatedAt="
                + updatedAt + "]";
    }

    
}
