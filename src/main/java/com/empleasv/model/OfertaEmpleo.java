package com.empleasv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;

// Representa una oferta de empleo
@Entity
@Table(name = "oferta_empleo")
public class OfertaEmpleo {

    // Datos principales de la oferta
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "requisitos", columnDefinition = "TEXT")
    private String requisitos;

    @Column(name = "ubicacion", nullable = false, length = 150)
    private String ubicacion;

    @Column(name = "salario", precision = 10, scale = 2)
    private Double salario;

    @Column(name = "tipo_contrato", nullable = false, length = 50)
    private String tipoContrato;

    @Column(name = "horario", length = 100)
    private String horario;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    // Relación con empresa (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false, referencedColumnName = "id")
    private Empresa empresa;

    // Campo transitorio para compatibilidad con código existente que use empresaId directamente
    // No se persiste en BD, se deriva de la relación empresa
    @Transient
    private Integer empresaId;

    // Constructor vacío
    public OfertaEmpleo() {
    }

    // Constructor para crear una oferta con todos sus datos
    public OfertaEmpleo(Integer id, String titulo, String descripcion, String requisitos,
                        String ubicacion, Double salario, String tipoContrato, String horario,
                        LocalDateTime fechaPublicacion, String estado, Integer empresaId,
                        Empresa empresa) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.requisitos = requisitos;
        this.ubicacion = ubicacion;
        this.salario = salario;
        this.tipoContrato = tipoContrato;
        this.horario = horario;
        this.fechaPublicacion = fechaPublicacion;
        this.estado = estado;
        this.empresa = empresa;
        // empresaId se deriva de empresa si no se proporciona
        if (empresa != null) {
            this.empresaId = empresa.getId();
        } else {
            this.empresaId = empresaId;
        }
    }

    // Obtiene el ID
    public Integer getId() {
        return id;
    }

    // Asigna el ID
    public void setId(Integer id) {
        this.id = id;
    }

    // Obtiene el título
    public String getTitulo() {
        return titulo;
    }

    // Asigna el título
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // Obtiene la descripción
    public String getDescripcion() {
        return descripcion;
    }

    // Asigna la descripción
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Obtiene los requisitos
    public String getRequisitos() {
        return requisitos;
    }

    // Asigna los requisitos
    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    // Obtiene la ubicación
    public String getUbicacion() {
        return ubicacion;
    }

    // Asigna la ubicación
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    // Obtiene el salario
    public Double getSalario() {
        return salario;
    }

    // Asigna el salario
    public void setSalario(Double salario) {
        this.salario = salario;
    }

    // Obtiene el tipo de contrato
    public String getTipoContrato() {
        return tipoContrato;
    }

    // Asigna el tipo de contrato
    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    // Obtiene el horario
    public String getHorario() {
        return horario;
    }

    // Asigna el horario
    public void setHorario(String horario) {
        this.horario = horario;
    }

    // Obtiene la fecha de publicación
    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    // Asigna la fecha de publicación
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    // Obtiene el estado
    public String getEstado() {
        return estado;
    }

    // Asigna el estado
    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Obtiene el ID de la empresa (compatibilidad)
    // Si empresa está cargada, devuelve su ID; si no, devuelve empresaId guardado
    public Integer getEmpresaId() {
        if (empresa != null && empresa.getId() != null) {
            return empresa.getId();
        }
        return empresaId;
    }

    // Asigna el ID de la empresa (compatibilidad)
    public void setEmpresaId(Integer empresaId) {
        this.empresaId = empresaId;
    }

    // Obtiene la empresa relacionada
    public Empresa getEmpresa() {
        return empresa;
    }

    // Asigna la empresa relacionada
    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
        // Sincronizar empresaId cuando se asigna la empresa
        if (empresa != null) {
            this.empresaId = empresa.getId();
        }
    }

    @Override
    public String toString() {
        return "OfertaEmpleo{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", estado='" + estado + '\'' +
                ", empresaId=" + getEmpresaId() +
                '}';
    }
}