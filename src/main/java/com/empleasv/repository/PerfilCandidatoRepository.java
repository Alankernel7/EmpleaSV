package com.empleasv.repository;

import com.empleasv.config.JpaConfig;
import com.empleasv.model.PerfilCandidato;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;

/**
 * Repositorio JPA para el perfil extendido del candidato (rol USUARIO).
 */
public class PerfilCandidatoRepository {

    /**
     * Obtiene el perfil de candidato asociado a un usuario.
     *
     * @param usuarioId ID del usuario
     * @return perfil o null si aún no existe
     */
    public PerfilCandidato obtenerPorUsuarioId(Integer usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            // JOIN FETCH de las relacionesToOne para poder leerlas en las vistas
            // después de cerrar el EntityManager (evita LazyInitializationException).
            TypedQuery<PerfilCandidato> query = em.createQuery(
                    "SELECT p FROM PerfilCandidato p " +
                    "LEFT JOIN FETCH p.usuario u " +
                    "LEFT JOIN FETCH p.fotoPerfil f " +
                    "LEFT JOIN FETCH p.curriculum c " +
                    "WHERE p.usuario.id = :usuarioId",
                    PerfilCandidato.class);
            query.setParameter("usuarioId", usuarioId);
            try {
                return query.getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene el perfil por ID.
     *
     * @param id ID del perfil
     * @return perfil o null
     */
    public PerfilCandidato obtenerPorId(Integer id) {
        if (id == null) {
            return null;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            return em.find(PerfilCandidato.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Crea un perfil vacío para el usuario indicado si aún no existe.
     *
     * @param usuarioId ID del usuario
     * @return perfil persistido
     */
    public PerfilCandidato crearSiNoExiste(Integer usuarioId) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            PerfilCandidato perfil = buscarPorUsuario(em, usuarioId);
            if (perfil == null) {
                perfil = new PerfilCandidato();
                perfil.setUsuario(em.find(com.empleasv.model.Usuario.class, usuarioId));
                perfil.setFechaActualizacion(LocalDateTime.now());
                em.persist(perfil);
            }
            em.getTransaction().commit();
            return perfil;
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
     * Guarda o actualiza el perfil dentro de la transacción indicada.
     * El llamador controla la transacción.
     *
     * @param em    EntityManager activo
     * @param perfil perfil a persistir
     * @return perfil persistido
     */
    public PerfilCandidato guardar(EntityManager em, PerfilCandidato perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil no puede ser null");
        }
        perfil.setFechaActualizacion(LocalDateTime.now());
        if (perfil.getId() == null) {
            em.persist(perfil);
        } else {
            em.merge(perfil);
        }
        return perfil;
    }

    /**
     * Guarda o actualiza el perfil con su propia transacción.
     *
     * @param perfil perfil a persistir
     * @return perfil persistido
     */
    public PerfilCandidato guardar(PerfilCandidato perfil) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            PerfilCandidato resultado = guardar(em, perfil);
            em.getTransaction().commit();
            return resultado;
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
     * Busca el perfil de un usuario usando el EntityManager indicado.
     *
     * @param em        EntityManager activo
     * @param usuarioId ID del usuario
     * @return perfil o null
     */
    public PerfilCandidato buscarPorUsuario(EntityManager em, Integer usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        TypedQuery<PerfilCandidato> query = em.createQuery(
                "SELECT p FROM PerfilCandidato p WHERE p.usuario.id = :usuarioId",
                PerfilCandidato.class);
        query.setParameter("usuarioId", usuarioId);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
