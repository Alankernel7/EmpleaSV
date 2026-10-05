package com.empleasv.dao;

import com.empleasv.model.OfertaEmpleo;
import com.empleasv.repository.OfertaEmpleoRepository;

import java.util.List;

// DAO para operaciones CRUD de la tabla oferta_empleo
// Refactorizado para usar JPA/Hibernate manteniendo la misma interfaz pública
public class OfertaEmpleoDAO {

    private final OfertaEmpleoRepository repository;

    public OfertaEmpleoDAO() {
        this.repository = new OfertaEmpleoRepository();
    }

    // Lista todas las ofertas
    public List<OfertaEmpleo> listarTodas() {
        return repository.listarTodas();
    }

    // Obtiene una oferta por su ID
    public OfertaEmpleo obtenerPorId(int id) {
        return repository.obtenerPorId(id);
    }

    // Lista las ofertas activas más recientes para mostrar en el inicio
    public List<OfertaEmpleo> listarDestacadas() {
        return repository.listarDestacadas();
    }

    // Busca ofertas por título (búsqueda parcial con LIKE)
    public List<OfertaEmpleo> buscarPorTitulo(String titulo) {
        return repository.buscarPorTitulo(titulo);
    }

    // Filtra ofertas por ubicación exacta
    public List<OfertaEmpleo> filtrarPorUbicacion(String ubicacion) {
        return repository.filtrarPorUbicacion(ubicacion);
    }

    // Lista todas las ofertas pertenecientes a una empresa
    public List<OfertaEmpleo> listarPorEmpresa(int empresaId) {
        return repository.listarPorEmpresa(empresaId);
    }

    // Registra una nueva oferta en la base de datos
    public boolean registrar(OfertaEmpleo oferta) {
        return repository.registrar(oferta);
    }

    // Actualiza una oferta existente en la base de datos
    public boolean actualizar(OfertaEmpleo oferta) {
        return repository.actualizar(oferta);
    }

    // Elimina una oferta por su ID
    public boolean eliminar(int id) {
        return repository.eliminar(id);
    }
}