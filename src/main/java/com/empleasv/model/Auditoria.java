package com.empleasv.model;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Registro de auditoría de acciones administrativas.
 * Guarda quién ejecutó la acción, qué hizo, sobre qué objetivo y cuándo.
 * Nunca almacena contraseñas ni hashes.
 */
@Entity
@Table(name = "auditoria")
public class Auditoria {

    public enum TipoObjetivo {
        USUARIO,
        OFERTA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false, referencedColumnName = "id")
    private Usuario admin;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "objetivo_tipo", nullable = false, length = 30)
    private TipoObjetivo objetivoTipo;

    @Column(name = "objetivo_id")
    private Integer objetivoId;

    @Column(name = "detalle", length = 255)
    private String detalle;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    public Auditoria() {
    }

    public Auditoria(Usuario admin, String accion, TipoObjetivo objetivoTipo,
                     Integer objetivoId, String detalle) {
        this.admin = admin;
        this.accion = accion;
        this.objetivoTipo = objetivoTipo;
        this.objetivoId = objetivoId;
        this.detalle = detalle;
        this.fecha = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getAdmin() {
        return admin;
    }

    public void setAdmin(Usuario admin) {
        this.admin = admin;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public TipoObjetivo getObjetivoTipo() {
        return objetivoTipo;
    }

    public void setObjetivoTipo(TipoObjetivo objetivoTipo) {
        this.objetivoTipo = objetivoTipo;
    }

    public Integer getObjetivoId() {
        return objetivoId;
    }

    public void setObjetivoId(Integer objetivoId) {
        this.objetivoId = objetivoId;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
