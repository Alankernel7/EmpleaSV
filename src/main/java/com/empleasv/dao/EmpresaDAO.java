package com.empleasv.dao;

import com.empleasv.model.Empresa;
import com.empleasv.repository.EmpresaRepository;

import java.util.List;

// DAO para operaciones CRUD de la tabla empresa
// Refactorizado para usar JPA/Hibernate manteniendo la misma interfaz pública
public class EmpresaDAO {

    private final EmpresaRepository repository;

    public EmpresaDAO() {
        this.repository = new EmpresaRepository();
    }

    // Lista todas las empresas
    public List<Empresa> listarTodas() {
        return repository.listarTodas();
    }

    // Obtiene una empresa por su ID
    public Empresa obtenerPorId(int id) {
        return repository.obtenerPorId(id);
    }

    // Lista las 4 empresas con mayor cantidad de ofertas publicadas
    public List<Empresa> listarDestacadas() {
        return repository.listarDestacadas();
    }

    // Lista todas las empresas junto con su cantidad de ofertas
    public List<Empresa> listarConCantidadOfertas() {
        return repository.listarConCantidadOfertas();
    }
}