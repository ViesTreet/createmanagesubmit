package com.vt.createmanagesubmit.dto;

import java.time.LocalDateTime;

import com.vt.createmanagesubmit.modelos.TareaProgramada;

public class TareaDTO {
    private Long id;

    private Long IDCurso; 

    private String accion;       

    private String nombreCurso;

    private LocalDateTime fechaEjecucion;

    private String estado;         

    private String ubicacionSubida;

    public TareaDTO() {
    }

    public TareaDTO(TareaProgramada tarea) {
        this.id = tarea.getId();
        this.IDCurso = tarea.getIDCurso();
        this.accion = tarea.getAccion();
        this.nombreCurso = tarea.getNombreCurso();
        this.fechaEjecucion = tarea.getFechaEjecucion();
        this.estado = tarea.getEstado();
        this.ubicacionSubida = tarea.getUbicacionSubida();
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

    public String getNombreCurso() {
        return nombreCurso;
    }

    public void setNombreCurso(String nombreCurso) {
        this.nombreCurso = nombreCurso;
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

    public String getUbicacionSubida() {
        return ubicacionSubida;
    }

    public void setUbicacionSubida(String ubicacionSubida) {
        this.ubicacionSubida = ubicacionSubida;
    }
    
    

}
