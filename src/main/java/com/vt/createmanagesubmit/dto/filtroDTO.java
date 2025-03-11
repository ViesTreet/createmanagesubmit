package com.vt.createmanagesubmit.dto;

public class filtroDTO {
    private String campo;
    private String valor;
    
    public filtroDTO() {
    }

    public filtroDTO(String campo, String valor) {
        this.campo = campo;
        this.valor = valor;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    
}
