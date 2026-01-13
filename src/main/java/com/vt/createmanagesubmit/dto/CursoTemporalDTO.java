package com.vt.createmanagesubmit.dto;

import com.vt.createmanagesubmit.modelos.CursoTemporal;

public class CursoTemporalDTO {
    public Long id;
    public String nombreCurso;
    public String relator;
    public String identificador;
    public String cliente;
    public String ubicacionSubida;
    public String estado;
    public Long plantilla;

    public static CursoTemporalDTO fromEntity(CursoTemporal c){
        CursoTemporalDTO d = new CursoTemporalDTO();
        d.id = c.getId();
        d.nombreCurso = c.getNombreCurso();
        d.relator = c.getRelator();
        d.identificador = c.getIdentificador();
        d.cliente = c.getCliente();
        d.ubicacionSubida = c.getUbicacionSubida();
        d.estado = c.getEstado();
        d.plantilla = c.getPlantilla();
        return d;
    }
}
