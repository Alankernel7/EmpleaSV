package com.empleasv.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;

// Representa una empresa del sistema
@Entity
@Table(name = "empresa")
public class Empresa {

    // Datos de la empresa
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "telefono", length = 30)
    private String telefono;

    // Relación con ofertas (no se persiste en BD, solo navegación)
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<OfertaEmpleo> ofertas = new ArrayList<>();

    @Transient
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

    // Obtiene la lista de ofertas
    public List<OfertaEmpleo> getOfertas() {
        return ofertas;
    }

    // Asigna la lista de ofertas
    public void setOfertas(List<OfertaEmpleo> ofertas) {
        this.ofertas = ofertas != null ? ofertas : new ArrayList<>();
    }

    // Obtiene la cantidad de ofertas asociadas (calculada o asignada manualmente)
    public Integer getCantidadOfertas() {
        return cantidadOfertas;
    }

    // Asigna la cantidad de ofertas asociadas
    public void setCantidadOfertas(Integer cantidadOfertas) {
        this.cantidadOfertas = cantidadOfertas;
    }

    // Método auxiliar para agregar oferta manteniendo consistencia bidireccional
    public void addOferta(OfertaEmpleo oferta) {
        if (oferta != null) {
            if (this.ofertas == null) {
                this.ofertas = new ArrayList<>();
            }
            if (!this.ofertas.contains(oferta)) {
                this.ofertas.add(oferta);
                oferta.setEmpresa(this);
            }
        }
    }

    // Método auxiliar para remover oferta manteniendo consistencia bidireccional
    public void removeOferta(OfertaEmpleo oferta) {
        if (oferta != null && this.ofertas != null) {
            if (this.ofertas.remove(oferta)) {
                oferta.setEmpresa(null);
            }
        }
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

    @Override
    public String toString() {
        return "Empresa{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}