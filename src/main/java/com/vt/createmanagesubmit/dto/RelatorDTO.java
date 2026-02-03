package com.vt.createmanagesubmit.dto;

import java.util.Date;

import com.vt.createmanagesubmit.modelos.Relator;

public class RelatorDTO {

    private Long id;

    private String nombre;

    private String contacto;

    private String foto;

    private Float horasTrabajados;

    private String datosExtras;

    private int cursos;

	private Date createdAt;
	
	private Date updatedAt;

    public RelatorDTO() {
    }

    public RelatorDTO(Relator relator) {
        this.id = relator.getId();
        this.nombre = relator.getNombre();
        this.contacto = relator.getContacto();
        this.foto = relator.getFoto();
        this.horasTrabajados = relator.getHorasTrabajados();
        this.datosExtras = relator.getDatosExtras();
        this.cursos = relator.getCursos().size();
        this.createdAt = relator.getCreatedAt();
        this.updatedAt = relator.getUpdatedAt();
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

    public int getCursos() {
        return cursos;
    }

    public void setCursos(int cursos) {
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

    
}
