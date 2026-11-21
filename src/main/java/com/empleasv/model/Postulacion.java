package com.empleasv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Postulacion de un candidato a una oferta de empleo.
 *
 * Conserva una referencia al archivo (curriculum) exacto que se envio en el
 * momento de postularse. Aunque el candidato despues actualice o cambie el
 * curriculum de su perfil, la empresa sigue viendo el documento original.
 */
@Entity
@Table(name = "postulacion",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_postulacion_oferta_usuario",
                columnNames = {"oferta_id", "usuario_id"}))
public class Postulacion {

    public enum EstadoPostulacion {
        PENDIENTE,
        REVISADA,
        ACEPTADA,
        RECHAZADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false, referencedColumnName = "id")
    private OfertaEmpleo oferta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, referencedColumnName = "id")
    private Usuario candidato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", nullable = false, referencedColumnName = "id")
    private Archivo curriculum;

    @Column(name = "mensaje_presentacion", columnDefinition = "TEXT")
    private String mensajePresentacion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPostulacion estado;

    @Column(name = "fecha_postulacion", nullable = false)
    private LocalDateTime fechaPostulacion;

    public Postulacion() {
    }

    public Postulacion(Integer id, OfertaEmpleo oferta, Usuario candidato, Archivo curriculum,
                       String mensajePresentacion, EstadoPostulacion estado,
                       LocalDateTime fechaPostulacion) {
        this.id = id;
        this.oferta = oferta;
        this.candidato = candidato;
        this.curriculum = curriculum;
        this.mensajePresentacion = mensajePresentacion;
        this.estado = estado;
        this.fechaPostulacion = fechaPostulacion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OfertaEmpleo getOferta() {
        return oferta;
    }

    public void setOferta(OfertaEmpleo oferta) {
        this.oferta = oferta;
    }

    public Usuario getCandidato() {
        return candidato;
    }

    public void setCandidato(Usuario candidato) {
        this.candidato = candidato;
    }

    public Archivo getCurriculum() {
        return curriculum;
    }

    public void setCurriculum(Archivo curriculum) {
        this.curriculum = curriculum;
    }

    public String getMensajePresentacion() {
        return mensajePresentacion;
    }

    public void setMensajePresentacion(String mensajePresentacion) {
        this.mensajePresentacion = mensajePresentacion;
    }

    public EstadoPostulacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPostulacion estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPostulacion() {
        return fechaPostulacion;
    }

    public void setFechaPostulacion(LocalDateTime fechaPostulacion) {
        this.fechaPostulacion = fechaPostulacion;
    }

    @Override
    public String toString() {
        return "Postulacion{" +
                "id=" + id +
                ", ofertaId=" + (oferta != null ? oferta.getId() : null) +
                ", candidatoId=" + (candidato != null ? candidato.getId() : null) +
                ", curriculumId=" + (curriculum != null ? curriculum.getId() : null) +
                ", estado=" + estado +
                '}';
    }
}
