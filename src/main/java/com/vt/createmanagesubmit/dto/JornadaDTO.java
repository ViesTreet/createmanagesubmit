package com.vt.createmanagesubmit.dto;

import java.time.LocalDateTime;

public class JornadaDTO {

    private Long id;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    public JornadaDTO() {
    }

    public JornadaDTO(Long id, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.id = id;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }
    
}
