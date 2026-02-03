package com.vt.createmanagesubmit.dto;

import java.util.Date;

import com.vt.createmanagesubmit.modelos.Cliente;

public class ClienteDTO {

    private Long id;

    private String nombreCliente;

    private int cursos;

    private String pathLogo;

    private String pathLogoFooter;

    private String identificador;

	private Date createdAt;
	
	private Date updatedAt;

    public ClienteDTO() {
    }

    public ClienteDTO(Cliente cliente) {
        this.id = cliente.getId();
        this.nombreCliente = cliente.getNombreCliente();
        this.cursos = cliente.getCursos().size();
        this.pathLogo = cliente.getPathLogo();
        this.pathLogoFooter = cliente.getPathLogoFooter();
        this.identificador = cliente.getIdentificador();
        this.createdAt = cliente.getCreatedAt();
        this.updatedAt = cliente.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public int getCursos() {
        return cursos;
    }

    public void setCursos(int cursos) {
        this.cursos = cursos;
    }

    public String getPathLogo() {
        return pathLogo;
    }

    public void setPathLogo(String pathLogo) {
        this.pathLogo = pathLogo;
    }

    public String getPathLogoFooter() {
        return pathLogoFooter;
    }

    public void setPathLogoFooter(String pathLogoFooter) {
        this.pathLogoFooter = pathLogoFooter;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
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
