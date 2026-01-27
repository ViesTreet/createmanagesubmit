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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="TareaProgramada")
public class TareaProgramada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long IDCurso; 

    private String accion;       

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "curso_id") 
    private Curso cursoTareaProgramada;

    private LocalDateTime fechaEjecucion;

    private String estado;         
    
    @Column(updatable=false)
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createdAt;
	
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date updatedAt;

    public TareaProgramada() {
    }
    
    public TareaProgramada(Long id, Long iDCurso, String accion, Curso curso, LocalDateTime fechaEjecucion,
            String estado, Date createdAt, Date updatedAt) {
        this.id = id;
        IDCurso = iDCurso;
        this.accion = accion;
        this.cursoTareaProgramada = curso;
        this.fechaEjecucion = fechaEjecucion;
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

    public Long getIDCurso() {
        return IDCurso;
    }

    public void setIDCurso(Long iDCurso) {
        IDCurso = iDCurso;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public Curso getCurso() {
        return cursoTareaProgramada;
    }

    public void setCurso(Curso curso) {
        this.cursoTareaProgramada = curso;
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
