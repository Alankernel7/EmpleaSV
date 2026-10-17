package com.empleasv.dao;

import com.empleasv.model.Archivo;
import com.empleasv.repository.ArchivoRepository;

import java.util.List;

// DAO para operaciones de la tabla archivo (metadatos de archivos subidos)
public class ArchivoDAO {

    private final ArchivoRepository repository;

    public ArchivoDAO() {
        this.repository = new ArchivoRepository();
    }

    // Obtiene un archivo por su ID
    public Archivo obtenerPorId(Integer id) {
        return repository.obtenerPorId(id);
    }

    // Indica si el archivo pertenece al usuario indicado
    public boolean esPropietario(Integer archivoId, Integer usuarioId) {
        return repository.esPropietario(archivoId, usuarioId);
    }

    // Indica si el archivo es un curriculum propio del usuario
    public boolean esCurriculumPropio(Integer archivoId, Integer usuarioId) {
        return repository.esCurriculumPropio(archivoId, usuarioId);
    }

    // Lista todos los curriculum subidos por el usuario
    public List<Archivo> listarCurriculums(Integer usuarioId) {
        return repository.listarCurriculums(usuarioId);
    }

    // Persiste los metadatos de un archivo
    public Archivo guardar(Archivo archivo) {
        return repository.guardar(archivo);
    }

    // Elimina los metadatos de un archivo
    public boolean eliminar(Integer id) {
        return repository.eliminar(id);
    }
}
