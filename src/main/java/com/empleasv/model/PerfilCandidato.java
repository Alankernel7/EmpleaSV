package com.empleasv.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "perfil_candidato")
public class PerfilCandidato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true, referencedColumnName = "id")
    private Usuario usuario;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "direccion_residencia", length = 255)
    private String direccionResidencia;

    @Column(name = "municipio", length = 100)
    private String municipio;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foto_perfil_id", referencedColumnName = "id")
    private Archivo fotoPerfil;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", referencedColumnName = "id")
    private Archivo curriculum;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public PerfilCandidato() {
    }

    public PerfilCandidato(Integer id, Usuario usuario, String telefono, String direccionResidencia,
                           String municipio, String departamento, Archivo fotoPerfil, Archivo curriculum,
                           LocalDateTime fechaActualizacion) {
        this.id = id;
        this.usuario = usuario;
        this.telefono = telefono;
        this.direccionResidencia = direccionResidencia;
        this.municipio = municipio;
        this.departamento = departamento;
        this.fotoPerfil = fotoPerfil;
        this.curriculum = curriculum;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccionResidencia() {
        return direccionResidencia;
    }

    public void setDireccionResidencia(String direccionResidencia) {
        this.direccionResidencia = direccionResidencia;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public Archivo getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(Archivo fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public Archivo getCurriculum() {
        return curriculum;
    }

    public void setCurriculum(Archivo curriculum) {
        this.curriculum = curriculum;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return "PerfilCandidato{" +
                "id=" + id +
                ", usuarioId=" + (usuario != null ? usuario.getId() : null) +
                ", telefono='" + telefono + '\'' +
                ", municipio='" + municipio + '\'' +
                ", departamento='" + departamento + '\'' +
                '}';
    }
}