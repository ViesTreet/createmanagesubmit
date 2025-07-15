package com.vt.createmanagesubmit.modelos;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="TareaProgramada")
public class TareaProgramada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long IDCurso; 

    private Long IDQuiz;       

    private String nombreCurso;

    private String diasCursos;

    private String duracion;

    private String modalidad;

    private String cliente;

    private String relator;

    private String lugarYfechaEmision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_id")
    private Plantilla plantilla;     

    private LocalDateTime fechaEjecucion;

    private String estado;         

    private String ubicacionSubida;
    
    @Column(updatable=false)
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createdAt;
	
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date updatedAt;

    public TareaProgramada() {
    }

    public TareaProgramada(Long id, Long iDCurso, Long iDQuiz, String nombreCurso, String diasCursos, String duracion,
            String modalidad, String cliente, String relator, String lugarYfechaEmision, Plantilla plantilla,
            LocalDateTime fechaEjecucion, String estado, String ubicacionSubida , Date createdAt, Date updatedAt) {
        this.id = id;
        IDCurso = iDCurso;
        IDQuiz = iDQuiz;
        this.nombreCurso = nombreCurso;
        this.diasCursos = diasCursos;
        this.duracion = duracion;
        this.modalidad = modalidad;
        this.cliente = cliente;
        this.relator = relator;
        this.lugarYfechaEmision = lugarYfechaEmision;
        this.plantilla = plantilla;
        this.fechaEjecucion = fechaEjecucion;
        this.estado = estado;
        this.ubicacionSubida = ubicacionSubida;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIDCurso() {
        return IDCurso;
    }

    public void setIDCurso(Long iDCurso) {
        IDCurso = iDCurso;
    }

    public Long getIDQuiz() {
        return IDQuiz;
    }

    public void setIDQuiz(Long iDQuiz) {
        IDQuiz = iDQuiz;
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

    public String getLugarYfechaEmision() {
        return lugarYfechaEmision;
    }

    public void setLugarYfechaEmision(String lugarYfechaEmision) {
        this.lugarYfechaEmision = lugarYfechaEmision;
    }

    public Plantilla getPlantilla() {
        return plantilla;
    }

    public void setPlantilla(Plantilla plantilla) {
        this.plantilla = plantilla;
    }

    public LocalDateTime getFechaEjecucion() {
        return fechaEjecucion;
    }

    public void setFechaEjecucion(LocalDateTime fechaEjecucion) {
        this.fechaEjecucion = fechaEjecucion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUbicacionSubida(){
        return ubicacionSubida;
    }

    public void setUbicacionSubida(String ubicacionSubida){
        this.ubicacionSubida=ubicacionSubida;
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

    @Override
    public String toString() {
        return "TareaProgramada [id=" + id + ", IDCurso=" + IDCurso + ", IDQuiz=" + IDQuiz + ", nombreCurso="
                + nombreCurso + ", diasCursos=" + diasCursos + ", duracion=" + duracion + ", modalidad=" + modalidad
                + ", cliente=" + cliente + ", relator=" + relator + ", lugarYfechaEmision=" + lugarYfechaEmision
                + ", plantilla=" + plantilla + ", fechaEjecucion=" + fechaEjecucion + ", estado=" + estado
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
    }

    
    
}
