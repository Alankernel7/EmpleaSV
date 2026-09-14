package com.empleasv.model;

import java.time.LocalDateTime;

// Representa una oferta de empleo
public class OfertaEmpleo {

    // Datos principales de la oferta
    private Integer id;
    private String titulo;
    private String descripcion;
    private String requisitos;
    private String ubicacion;
    private Double salario;
    private String tipoContrato;
    private String horario;
    private LocalDateTime fechaPublicacion;
    private String estado;

    // ID de la empresa en la base de datos
    private Integer empresaId;

    // Empresa relacionada con la oferta
    private Empresa empresa;

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
        this.empresaId = empresaId;
        this.empresa = empresa;
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

    // Obtiene el ID de la empresa
    public Integer getEmpresaId() {
        return empresaId;
    }

    // Asigna el ID de la empresa
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
    }
}