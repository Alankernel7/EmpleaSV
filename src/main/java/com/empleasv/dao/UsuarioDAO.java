package com.empleasv.dao;

import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.repository.UsuarioRepository;

import java.util.List;

// DAO para operaciones CRUD de la tabla usuario
// Refactorizado para usar JPA/Hibernate manteniendo la misma interfaz pública
public class UsuarioDAO {

    private final UsuarioRepository repository;

    public UsuarioDAO() {
        this.repository = new UsuarioRepository();
    }

    // Lista todos los usuarios
    public List<Usuario> listarTodos() {
        return repository.listarTodos();
    }

    // Obtiene un usuario por su ID
    public Usuario obtenerPorId(int id) {
        return repository.obtenerPorId(id);
    }

    // Obtiene un usuario por su email
    public Usuario obtenerPorEmail(String email) {
        return repository.obtenerPorEmail(email);
    }

    // Verifica si existe un usuario con el email dado
    public boolean existePorEmail(String email) {
        return repository.existePorEmail(email);
    }

    // Guarda un nuevo usuario
    public Usuario guardar(Usuario usuario) {
        return repository.guardar(usuario);
    }

    // Actualiza un usuario existente
    public Usuario actualizar(Usuario usuario) {
        return repository.actualizar(usuario);
    }

    // Elimina un usuario por ID
    public boolean eliminar(int id) {
        return repository.eliminar(id);
    }

    // Lista usuarios por rol
    public List<Usuario> listarPorRol(UsuarioRol rol) {
        return repository.listarPorRol(rol);
    }
}