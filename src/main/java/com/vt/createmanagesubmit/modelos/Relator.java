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
@Table(name="relator")
public class Relator {
    
    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

    private String nombre;

    private String contacto;

    private String foto;

    private Float horasTrabajados;

    @Column(columnDefinition = "TEXT")
    private String datosExtras;

    @OneToMany(mappedBy = "relator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Curso> cursos;

    @Column(updatable=false)
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date createdAt;
	
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private Date updatedAt;
    
    public Relator() {
    }
    
    public Relator(Long id, String nombre, String contacto, String foto, Float horasTrabajados, String datosExtras,
            List<Curso> cursos, Date createdAt, Date updatedAt) {
        this.id = id;
        this.nombre = nombre;
        this.contacto = contacto;
        this.foto = foto;
        this.horasTrabajados = horasTrabajados;
        this.datosExtras = datosExtras;
        this.cursos = cursos;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Float getHorasTrabajados() {
        return horasTrabajados;
    }

    public void setHorasTrabajados(Float horasTrabajados) {
        this.horasTrabajados = horasTrabajados;
    }

    public String getDatosExtras() {
        return datosExtras;
    }

    public void setDatosExtras(String datosExtras) {
        this.datosExtras = datosExtras;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
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
