package com.empleasv.repository;

import com.empleasv.config.JpaConfig;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para operaciones de persistencia de Usuario.
 * Encapsula el uso de EntityManager y consultas JPQL.
 */
public class UsuarioRepository {

    /**
     * Lista todos los usuarios.
     *
     * @return lista de usuarios
     */
    public List<Usuario> listarTodos() {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u ORDER BY u.nombre", Usuario.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario
     * @return usuario o null si no existe
     */
    public Usuario obtenerPorId(Integer id) {
        if (id == null) {
            return null;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene un usuario por su email.
     *
     * @param email email del usuario
     * @return usuario o null si no existe
     */
    public Usuario obtenerPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Verifica si existe un usuario con el email dado.
     *
     * @param email email a verificar
     * @return true si existe
     */
    public boolean existePorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Guarda un nuevo usuario.
     *
     * @param usuario usuario a guardar
     * @return usuario guardado con ID generado
     */
    public Usuario guardar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser null");
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            if (usuario.getFechaCreacion() == null) {
                usuario.setFechaCreacion(LocalDateTime.now());
            }
            em.persist(usuario);
            em.getTransaction().commit();
            return usuario;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza un usuario existente.
     *
     * @param usuario usuario a actualizar
     * @return usuario actualizado
     */
    public Usuario actualizar(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuario o ID no puede ser null");
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            Usuario merged = em.merge(usuario);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Elimina un usuario por ID.
     *
     * @param id ID del usuario
     * @return true si se eliminó
     */
    public boolean eliminar(Integer id) {
        if (id == null) {
            return false;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            Usuario usuario = em.find(Usuario.class, id);
            if (usuario != null) {
                em.remove(usuario);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Lista usuarios por rol.
     *
     * @param rol rol a filtrar
     * @return lista de usuarios con ese rol
     */
    public List<Usuario> listarPorRol(UsuarioRol rol) {
        if (rol == null) {
            return List.of();
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.rol = :rol ORDER BY u.nombre", Usuario.class);
            query.setParameter("rol", rol);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lista los usuarios más recientes (panel administrativo).
     *
     * @param limite máximo de usuarios a devolver
     * @return lista de usuarios ordenados por fecha de creación descendente
     */
    public List<Usuario> listarRecientes(int limite) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u ORDER BY u.fechaCreacion DESC", Usuario.class);
            query.setMaxResults(Math.max(1, limite));
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Busca y filtra usuarios para el panel administrativo.
     *
     * @param texto  texto libre (nombre o email), puede ser null
     * @param rol    rol exacto o null para todos
     * @param estado estado (ACTIVO/INACTIVO) o null para todos
     * @return lista filtrada de usuarios
     */
    public List<Usuario> buscarFiltrado(String texto, UsuarioRol rol, String estado) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT u FROM Usuario u WHERE 1 = 1");
            if (texto != null && !texto.trim().isEmpty()) {
                jpql.append(" AND (LOWER(u.nombre) LIKE LOWER(:texto) OR LOWER(u.email) LIKE LOWER(:texto))");
            }
            if (rol != null) {
                jpql.append(" AND u.rol = :rol");
            }
            if (estado != null && !estado.trim().isEmpty()) {
                jpql.append(" AND u.estado = :estado");
            }
            jpql.append(" ORDER BY u.fechaCreacion DESC");
            TypedQuery<Usuario> query = em.createQuery(jpql.toString(), Usuario.class);
            if (texto != null && !texto.trim().isEmpty()) {
                query.setParameter("texto", "%" + texto.trim() + "%");
            }
            if (rol != null) {
                query.setParameter("rol", rol);
            }
            if (estado != null && !estado.trim().isEmpty()) {
                query.setParameter("estado", estado.trim());
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Cuenta usuarios por rol y opcionalmente por estado.
     *
     * @param rol    rol a contar
     * @param estado estado o null para cualquier estado
     * @return cantidad de usuarios
     */
    public long contarPorRol(UsuarioRol rol, String estado) {
        if (rol == null) {
            return 0;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol");
            if (estado != null && !estado.trim().isEmpty()) {
                jpql.append(" AND u.estado = :estado");
            }
            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            query.setParameter("rol", rol);
            if (estado != null && !estado.trim().isEmpty()) {
                query.setParameter("estado", estado.trim());
            }
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Cuenta usuarios en un estado dado (cualquier rol).
     *
     * @param estado estado a contar (ACTIVO/INACTIVO)
     * @return cantidad de usuarios
     */
    public long contarPorEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return 0;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.estado = :estado", Long.class);
            query.setParameter("estado", estado.trim());
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Cuenta todos los usuarios registrados.
     *
     * @return cantidad total de usuarios
     */
    public long contarTodos() {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM Usuario u", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}