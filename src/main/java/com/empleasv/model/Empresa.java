package com.empleasv.model;

// Representa una empresa del sistema
public class Empresa {

    // Datos de la empresa
    private Integer id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String email;
    private String telefono;

    private Integer cantidadOfertas;

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

    // Obtiene la categoría
    public String getCategoria() {
        return categoria;
    }

    // Asigna la categoría
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    // Obtiene la cantidad de ofertas asociadas
    public Integer getCantidadOfertas() {
        return cantidadOfertas;
    }

    // Asigna la cantidad de ofertas asociadas
    public void setCantidadOfertas(Integer cantidadOfertas) {
        this.cantidadOfertas = cantidadOfertas;
    }
    // Obtiene las dos primeras letras que ya están en mayúscula
    public String getIniciales() {

        if (nombre == null || nombre.isEmpty()) {
            return "";
        }

        StringBuilder iniciales = new StringBuilder();

        for (int i = 0; i < nombre.length(); i++) {

            char letra = nombre.charAt(i);

            if (Character.isUpperCase(letra)) {
                iniciales.append(letra);
            }

            if (iniciales.length() == 2) {
                break;
            }
        }

        return iniciales.toString();
    }
}