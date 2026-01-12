package com.vt.createmanagesubmit.modelos;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="cursoTemporal")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "plantilla" })
public class CursoTemporal {

    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

    @Column(length = 500)
    private String nombreCurso;

    private String diasCursos;

    private String duracion;

    private String modalidad;

    private String relator;

    private String identificador;

    private String cliente;

    private String ubicacionSubida;

    private String lugarYfechaEmision;

    private String estado;

    @Column(updatable=false)
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createdAt;
	
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date updatedAt;

    public CursoTemporal() {
    }

    public CursoTemporal(Long id, String nombreCurso, String diasCursos, String duracion, String modalidad,
            String relator, String identificador, String cliente, String ubicacionSubida, String lugarYfechaEmision,
            String estado, Date createdAt, Date updatedAt) {
        this.id = id;
        this.nombreCurso = nombreCurso;
        this.diasCursos = diasCursos;
        this.duracion = duracion;
        this.modalidad = modalidad;
        this.relator = relator;
        this.identificador = identificador;
        this.cliente = cliente;
        this.ubicacionSubida = ubicacionSubida;
        this.lugarYfechaEmision = lugarYfechaEmision;
        this.estado = estado;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getRelator() {
        return relator;
    }

    public void setRelator(String relator) {
        this.relator = relator;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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
        return "CursoTemporal [id=" + id + ", nombreCurso=" + nombreCurso + ", diasCursos=" + diasCursos + ", duracion="
                + duracion + ", modalidad=" + modalidad + ", relator=" + relator + ", identificador=" + identificador
                + ", cliente=" + cliente + ", ubicacionSubida=" + ubicacionSubida + ", lugarYfechaEmision="
                + lugarYfechaEmision + ", estado=" + estado + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
                + "]";
    }

    


}
