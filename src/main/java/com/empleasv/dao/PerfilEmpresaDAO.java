package com.empleasv.dao;

import com.empleasv.model.PerfilEmpresa;
import com.empleasv.repository.PerfilEmpresaRepository;

// DAO para operaciones del perfil empresarial (tabla perfil_empresa)
public class PerfilEmpresaDAO {

    private final PerfilEmpresaRepository repository;

    public PerfilEmpresaDAO() {
        this.repository = new PerfilEmpresaRepository();
    }

    // Obtiene el perfil asociado a una cuenta EMPRESA
    public PerfilEmpresa obtenerPorUsuarioId(Integer usuarioId) {
        return repository.obtenerPorUsuarioId(usuarioId);
    }

    // Devuelve el ID de la tabla empresa vinculada a la cuenta
    public Integer obtenerEmpresaIdPorUsuarioId(Integer usuarioId) {
        return repository.obtenerEmpresaIdPorUsuarioId(usuarioId);
    }

    // Crea el perfil si la cuenta aun no tiene uno
    public PerfilEmpresa crearSiNoExiste(Integer usuarioId) {
        return repository.crearSiNoExiste(usuarioId);
    }

    // Guarda o actualiza el perfil
    public PerfilEmpresa guardar(PerfilEmpresa perfil) {
        return repository.guardar(perfil);
    }
}
