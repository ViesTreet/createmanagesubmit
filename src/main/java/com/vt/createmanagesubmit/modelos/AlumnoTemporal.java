package com.vt.createmanagesubmit.modelos;

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
@Table(name="alumnoTemporal")
public class AlumnoTemporal {
    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

    private String nombreAsistente;

    private String rut;

    private String correo;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "cursoTemporal_id") 
    private CursoTemporal cursoTemporal;

    @Column(updatable=false)
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createdAt;
	
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date updatedAt;
    
    public AlumnoTemporal() {
    }
    
    public AlumnoTemporal(Long id, String nombreAsistente, String rut, String correo, CursoTemporal cursoTemporal, Date createdAt, Date updatedAt) {
        this.id = id;
        this.nombreAsistente = nombreAsistente;
        this.rut = rut;
        this.correo = correo;
        this.cursoTemporal = cursoTemporal;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
	protected void onCreated() {
		this.createdAt = new Date();
        this.updatedAt = new Date();
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

    public CursoTemporal getCursoTemporal() {
        return cursoTemporal;
    }

    public void setCursoTemporal(CursoTemporal cursoTemporal) {
        this.cursoTemporal = cursoTemporal;
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

    @PreUpdate
	protected void onUpdate() {
		this.updatedAt = new Date();
	}
}
