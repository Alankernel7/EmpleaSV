package com.empleasv.model;

// Representa una empresa del sistema
public class Empresa {

    // Datos de la empresa
    private Integer id;
    private String nombre;
    private String descripcion;
    private String email;
    private String telefono;

    // Constructor vacío
    public Empresa() {
    }

    // Constructor para crear una empresa con todos sus datos
    public Empresa(Integer id, String nombre, String descripcion, String email, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.email = email;
        this.telefono = telefono;
    }

    // Obtiene el ID
    public Integer getId() {
        return id;
    }

    // Asigna el ID
    public void setId(Integer id) {
        this.id = id;
    }

    // Obtiene el nombre
    public String getNombre() {
        return nombre;
    }

    // Asigna el nombre
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Obtiene la descripción
    public String getDescripcion() {
        return descripcion;
    }

    // Asigna la descripción
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Obtiene el correo
    public String getEmail() {
        return email;
    }

    // Asigna el correo
    public void setEmail(String email) {
        this.email = email;
    }

    // Obtiene el teléfono
    public String getTelefono() {
        return telefono;
    }

    // Asigna el teléfono
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}