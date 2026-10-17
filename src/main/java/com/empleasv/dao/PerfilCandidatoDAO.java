package com.empleasv.dao;

import com.empleasv.model.PerfilCandidato;
import com.empleasv.repository.PerfilCandidatoRepository;

// DAO para operaciones del perfil de candidato (tabla perfil_candidato)
public class PerfilCandidatoDAO {

    private final PerfilCandidatoRepository repository;

    public PerfilCandidatoDAO() {
        this.repository = new PerfilCandidatoRepository();
    }

    // Obtiene el perfil asociado a un usuario
    public PerfilCandidato obtenerPorUsuarioId(Integer usuarioId) {
        return repository.obtenerPorUsuarioId(usuarioId);
    }

    // Obtiene el perfil por su ID
    public PerfilCandidato obtenerPorId(Integer id) {
        return repository.obtenerPorId(id);
    }

    // Crea el perfil si el usuario aun no tiene uno
    public PerfilCandidato crearSiNoExiste(Integer usuarioId) {
        return repository.crearSiNoExiste(usuarioId);
    }

    // Guarda o actualiza el perfil
    public PerfilCandidato guardar(PerfilCandidato perfil) {
        return repository.guardar(perfil);
    }
}
