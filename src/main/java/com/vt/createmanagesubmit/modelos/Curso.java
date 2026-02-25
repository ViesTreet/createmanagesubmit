package com.vt.createmanagesubmit.modelos;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "curso")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "plantilla" })
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String nombreCurso;

    private String diasCursos;

    private String duracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private String modalidad;

    private String ubicacionSubida;

    private String ciudad;

    private String ubicacionDelCurso;

    private String ubicacionCliente;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private Float horasRelatorCurso;

    private String lugarYfechaEmision;

    private int asistenciaMin;

    private float notaMin;

    private boolean asistenciaQr;

    private String subcontratoDelCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_diploma_id")
    private Plantilla plantillaDiploma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_flyer_id")
    private Plantilla plantillaFlyer;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Alumno> alumnos;

    @OneToMany(mappedBy = "cursoTareaProgramada", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TareaProgramada> tareaProgramadas;

    @OneToMany(mappedBy = "cursoTemporal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AlumnoTemporal> alumnosTemporales;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relator_id")
    private Relator relator;

    @Column(updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updatedAt;

    public Curso() {
    }
    
    public Curso(Long id, String nombreCurso, String diasCursos, String duracion, Cliente cliente, String modalidad,
            String ubicacionSubida, String ciudad, String ubicacionDelCurso, String ubicacionCliente,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, Float horasRelatorCurso, String lugarYfechaEmision,
            int asistenciaMin, float notaMin, boolean asistenciaQr, String subcontratoDelCliente,
            Plantilla plantillaDiploma, Plantilla plantillaFlyer, List<Alumno> alumnos,
            List<TareaProgramada> tareaProgramadas, List<AlumnoTemporal> alumnosTemporales, Relator relator,
            Date createdAt, Date updatedAt) {
        this.id = id;
        this.nombreCurso = nombreCurso;
        this.diasCursos = diasCursos;
        this.duracion = duracion;
        this.cliente = cliente;
        this.modalidad = modalidad;
        this.ubicacionSubida = ubicacionSubida;
        this.ciudad = ciudad;
        this.ubicacionDelCurso = ubicacionDelCurso;
        this.ubicacionCliente = ubicacionCliente;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.horasRelatorCurso = horasRelatorCurso;
        this.lugarYfechaEmision = lugarYfechaEmision;
        this.asistenciaMin = asistenciaMin;
        this.notaMin = notaMin;
        this.asistenciaQr = asistenciaQr;
        this.subcontratoDelCliente = subcontratoDelCliente;
        this.plantillaDiploma = plantillaDiploma;
        this.plantillaFlyer = plantillaFlyer;
        this.alumnos = alumnos;
        this.tareaProgramadas = tareaProgramadas;
        this.alumnosTemporales = alumnosTemporales;
        this.relator = relator;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCurso() {
        return nombreCurso;
    }

    public void setNombreCurso(String nombreCurso) {
        this.nombreCurso = nombreCurso;
    }

    public String getDiasCursos() {
        return diasCursos;
    }

    public void setDiasCursos(String diasCursos) {
        this.diasCursos = diasCursos;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getUbicacionSubida() {
        return ubicacionSubida;
    }

    public void setUbicacionSubida(String ubicacionSubida) {
        this.ubicacionSubida = ubicacionSubida;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getUbicacionDelCurso() {
        return ubicacionDelCurso;
    }

    public void setUbicacionDelCurso(String ubicacionDelCurso) {
        this.ubicacionDelCurso = ubicacionDelCurso;
    }

    public String getUbicacionCliente() {
        return ubicacionCliente;
    }

    public void setUbicacionCliente(String ubicacionCliente) {
        this.ubicacionCliente = ubicacionCliente;
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

    public Float getHorasRelatorCurso() {
        return horasRelatorCurso;
    }

    public void setHorasRelatorCurso(Float horasRelatorCurso) {
        this.horasRelatorCurso = horasRelatorCurso;
    }

    public String getLugarYfechaEmision() {
        return lugarYfechaEmision;
    }

    public void setLugarYfechaEmision(String lugarYfechaEmision) {
        this.lugarYfechaEmision = lugarYfechaEmision;
    }

    public int getAsistenciaMin() {
        return asistenciaMin;
    }

    public void setAsistenciaMin(int asistenciaMin) {
        this.asistenciaMin = asistenciaMin;
    }

    public float getNotaMin() {
        return notaMin;
    }

    public void setNotaMin(float notaMin) {
        this.notaMin = notaMin;
    }

    public boolean isAsistenciaQr() {
        return asistenciaQr;
    }

    public void setAsistenciaQr(boolean asistenciaQr) {
        this.asistenciaQr = asistenciaQr;
    }

    public String getSubcontratoDelCliente() {
        return subcontratoDelCliente;
    }

    public void setSubcontratoDelCliente(String subcontratoDelCliente) {
        this.subcontratoDelCliente = subcontratoDelCliente;
    }

    public Plantilla getPlantillaDiploma() {
        return plantillaDiploma;
    }

    public void setPlantillaDiploma(Plantilla plantillaDiploma) {
        this.plantillaDiploma = plantillaDiploma;
    }

    public Plantilla getPlantillaFlyer() {
        return plantillaFlyer;
    }

    public void setPlantillaFlyer(Plantilla plantillaFlyer) {
        this.plantillaFlyer = plantillaFlyer;
    }

    public List<Alumno> getAlumnos() {
        return alumnos;
    }

    public void setAlumnos(List<Alumno> alumnos) {
        this.alumnos = alumnos;
    }

    public List<TareaProgramada> getTareaProgramadas() {
        return tareaProgramadas;
    }

    public void setTareaProgramadas(List<TareaProgramada> tareaProgramadas) {
        this.tareaProgramadas = tareaProgramadas;
    }

    public List<AlumnoTemporal> getAlumnosTemporales() {
        return alumnosTemporales;
    }

    public void setAlumnosTemporales(List<AlumnoTemporal> alumnosTemporales) {
        this.alumnosTemporales = alumnosTemporales;
    }

    public Relator getRelator() {
        return relator;
    }

    public void setRelator(Relator relator) {
        this.relator = relator;
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
