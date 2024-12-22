package com.vt.createmanagesubmit.modelos;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;


@Entity
@Table(name="plantillas")
public class Plantilla {

    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

    private String nombreCertificado;

    private String descripcion;

    private String tipoArchivo; 

    private String pathArchivo;

    private String pathLogo;

    @OneToMany(mappedBy = "plantilla", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Alumno> alumnos;

    @Column(updatable=false)
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createdAt;
	
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date updatedAt;

    public Plantilla() {
    }

    public Plantilla(List<Alumno> alumnos, Date createdAt, String descripcion, Long id, String nombreCertificado, String pathArchivo, String pathLogo, String tipoArchivo, Date updatedAt) {
        this.alumnos = alumnos;
        this.createdAt = createdAt;
        this.descripcion = descripcion;
        this.id = id;
        this.nombreCertificado = nombreCertificado;
        this.pathArchivo = pathArchivo;
        this.pathLogo = pathLogo;
        this.tipoArchivo = tipoArchivo;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCertificado() {
        return nombreCertificado;
    }

    public void setNombreCertificado(String nombreCertificado) {
        this.nombreCertificado = nombreCertificado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public String getPathArchivo() {
        return pathArchivo;
    }

    public void setPathArchivo(String pathArchivo) {
        this.pathArchivo = pathArchivo;
    }

    public String getPathLogo() {
        return pathLogo;
    }

    public void setPathLogo(String pathLogo) {
        this.pathLogo = pathLogo;
    }

    public List<Alumno> getAlumnos() {
        return alumnos;
    }

    public void setAlumnos(List<Alumno> alumnos) {
        this.alumnos = alumnos;
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
        return "Plantilla [id=" + id + ", nombreCertificado=" + nombreCertificado + ", descripcion=" + descripcion
                + ", tipoArchivo=" + tipoArchivo + ", pathArchivo=" + pathArchivo + ", pathLogo=" + pathLogo
                + ", alumnos=" + alumnos + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
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
