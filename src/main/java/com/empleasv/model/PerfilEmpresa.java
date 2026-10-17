package com.empleasv.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "perfil_empresa")
public class PerfilEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true, referencedColumnName = "id")
    private Usuario usuario;

    @Column(name = "nombre_responsable", nullable = false, length = 150)
    private String nombreResponsable;

    @Column(name = "nombre_empresa", nullable = false, length = 150)
    private String nombreEmpresa;

    @Column(name = "nit", length = 50)
    private String nit;

    @Column(name = "telefono_empresa", length = 30)
    private String telefonoEmpresa;

    @Column(name = "direccion_empresa", length = 255)
    private String direccionEmpresa;

    @Column(name = "municipio", length = 100)
    private String municipio;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "sitio_web", length = 255)
    private String sitioWeb;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo_id", referencedColumnName = "id")
    private Archivo logo;

    /**
     * Registro de la tabla 'empresa' al que pertenece esta cuenta empresarial.
     * Es el mismo registro que usan las ofertas de empleo, por lo que cada
     * cuenta EMPRESA solo puede gestionar las ofertas de esta empresa.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", referencedColumnName = "id", unique = true)
    private Empresa empresa;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public PerfilEmpresa() {
    }

    public PerfilEmpresa(Integer id, Usuario usuario, String nombreResponsable, String nombreEmpresa,
                         String nit, String telefonoEmpresa, String direccionEmpresa, String municipio,
                         String departamento, String sector, String sitioWeb, String descripcion,
                         Archivo logo, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.usuario = usuario;
        this.nombreResponsable = nombreResponsable;
        this.nombreEmpresa = nombreEmpresa;
        this.nit = nit;
        this.telefonoEmpresa = telefonoEmpresa;
        this.direccionEmpresa = direccionEmpresa;
        this.municipio = municipio;
        this.departamento = departamento;
        this.sector = sector;
        this.sitioWeb = sitioWeb;
        this.descripcion = descripcion;
        this.logo = logo;
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

    public String getNombreResponsable() {
        return nombreResponsable;
    }

    public void setNombreResponsable(String nombreResponsable) {
        this.nombreResponsable = nombreResponsable;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getTelefonoEmpresa() {
        return telefonoEmpresa;
    }

    public void setTelefonoEmpresa(String telefonoEmpresa) {
        this.telefonoEmpresa = telefonoEmpresa;
    }

    public String getDireccionEmpresa() {
        return direccionEmpresa;
    }

    public void setDireccionEmpresa(String direccionEmpresa) {
        this.direccionEmpresa = direccionEmpresa;
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

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Archivo getLogo() {
        return logo;
    }

    public void setLogo(Archivo logo) {
        this.logo = logo;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return "PerfilEmpresa{" +
                "id=" + id +
                ", usuarioId=" + (usuario != null ? usuario.getId() : null) +
                ", nombreEmpresa='" + nombreEmpresa + '\'' +
                ", sector='" + sector + '\'' +
                '}';
    }
}