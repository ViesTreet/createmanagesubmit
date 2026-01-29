package com.vt.createmanagesubmit.dto;

import com.vt.createmanagesubmit.modelos.Curso;

public class CursoTemporalDTO {
    public Long id;
    public String nombreCurso;
    public String relator;
    public String identificador;
    public String cliente;
    public String ubicacionSubida;
    public String estado;
    public Long plantilla;

    public static CursoTemporalDTO fromEntity(Curso c){
        CursoTemporalDTO d = new CursoTemporalDTO();
        d.id = c.getId();
        d.nombreCurso = c.getNombreCurso();
        d.relator = c.getRelator().getNombre();
        d.identificador = c.getCliente().getIdentificador();
        d.cliente = c.getCliente().getNombreCliente();
        d.ubicacionSubida = c.getUbicacionSubida();
        d.plantilla = c.getPlantillaDiploma().getId();
        return d;
    }
}
