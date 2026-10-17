package com.empleasv.repository;

import com.empleasv.config.JpaConfig;
import com.empleasv.model.PerfilEmpresa;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;

/**
 * Repositorio JPA para el perfil empresarial asociado a una cuenta EMPRESA.
 */
public class PerfilEmpresaRepository {

    /**
     * Obtiene el perfil empresarial de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return perfil o null si aún no existe
     */
    public PerfilEmpresa obtenerPorUsuarioId(Integer usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            // JOIN FETCH para poder leer logo y empresa en las vistas después
            // de cerrar el EntityManager (evita LazyInitializationException).
            TypedQuery<PerfilEmpresa> query = em.createQuery(
                    "SELECT p FROM PerfilEmpresa p " +
                    "LEFT JOIN FETCH p.usuario u " +
                    "LEFT JOIN FETCH p.logo l " +
                    "LEFT JOIN FETCH p.empresa e " +
                    "WHERE p.usuario.id = :usuarioId",
                    PerfilEmpresa.class);
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
     * Devuelve únicamente el ID de la tabla empresa vinculado a la cuenta.
     * Evita problemas de inicialización perezosa.
     *
     * @param usuarioId ID del usuario
     * @return ID de la empresa o null si la cuenta no tiene empresa vinculada
     */
    public Integer obtenerEmpresaIdPorUsuarioId(Integer usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Integer> query = em.createQuery(
                    "SELECT e.id FROM PerfilEmpresa p JOIN p.empresa e WHERE p.usuario.id = :usuarioId",
                    Integer.class);
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
     * Busca el perfil de un usuario usando el EntityManager indicado.
     *
     * @param em        EntityManager activo
     * @param usuarioId ID del usuario
     * @return perfil o null
     */
    public PerfilEmpresa buscarPorUsuario(EntityManager em, Integer usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        TypedQuery<PerfilEmpresa> query = em.createQuery(
                "SELECT p FROM PerfilEmpresa p WHERE p.usuario.id = :usuarioId",
                PerfilEmpresa.class);
        query.setParameter("usuarioId", usuarioId);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Crea un perfil empresarial vacío si el usuario aún no tiene uno.
     *
     * @param usuarioId ID del usuario
     * @return perfil persistido
     */
    public PerfilEmpresa crearSiNoExiste(Integer usuarioId) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            PerfilEmpresa perfil = buscarPorUsuario(em, usuarioId);
            if (perfil == null) {
                perfil = new PerfilEmpresa();
                perfil.setUsuario(em.find(com.empleasv.model.Usuario.class, usuarioId));
                perfil.setNombreResponsable(perfil.getUsuario() != null
                        ? perfil.getUsuario().getNombre() : "");
                perfil.setNombreEmpresa("Empresa sin nombre");
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
     *
     * @param em     EntityManager activo
     * @param perfil perfil a persistir
     * @return perfil persistido
     */
    public PerfilEmpresa guardar(EntityManager em, PerfilEmpresa perfil) {
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
    public PerfilEmpresa guardar(PerfilEmpresa perfil) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            PerfilEmpresa resultado = guardar(em, perfil);
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
}
