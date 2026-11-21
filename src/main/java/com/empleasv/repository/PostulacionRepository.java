package com.empleasv.repository;

import com.empleasv.config.JpaConfig;
import com.empleasv.model.Postulacion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Repositorio JPA de postulaciones.
 *
 * Las consultas hacen JOIN FETCH de las relaciones que usan las vistas
 * (oferta, empresa, candidato, perfil del candidato y curriculum) para que
 * puedan leerse despues de cerrar el EntityManager.
 */
public class PostulacionRepository {

    /**
     * Persiste una postulacion en su propia transaccion.
     */
    public Postulacion guardar(Postulacion postulacion) {
        if (postulacion == null) {
            throw new IllegalArgumentException("Postulacion no puede ser null");
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(postulacion);
            em.getTransaction().commit();
            return postulacion;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza una postulacion existente.
     */
    public Postulacion actualizar(Postulacion postulacion) {
        if (postulacion == null || postulacion.getId() == null) {
            throw new IllegalArgumentException("Postulacion invalida");
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            Postulacion resultado = em.merge(postulacion);
            em.getTransaction().commit();
            return resultado;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene una postulacion con todas sus relaciones para las vistas.
     */
    public Postulacion obtenerPorId(Integer id) {
        if (id == null) {
            return null;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Postulacion> query = em.createQuery(
                    "SELECT p FROM Postulacion p"
                            + " JOIN FETCH p.oferta o"
                            + " JOIN FETCH o.empresa"
                            + " JOIN FETCH p.candidato c"
                            + " LEFT JOIN FETCH c.perfilCandidato"
                            + " JOIN FETCH p.curriculum"
                            + " WHERE p.id = :id",
                    Postulacion.class);
            query.setParameter("id", id);
            List<Postulacion> resultados = query.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Indica si el candidato ya se postulo a la oferta indicada.
     */
    public boolean existe(Integer ofertaId, Integer usuarioId) {
        if (ofertaId == null || usuarioId == null) {
            return false;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            Long cantidad = em.createQuery(
                    "SELECT COUNT(p) FROM Postulacion p"
                            + " WHERE p.oferta.id = :ofertaId"
                            + " AND p.candidato.id = :usuarioId",
                    Long.class)
                    .setParameter("ofertaId", ofertaId)
                    .setParameter("usuarioId", usuarioId)
                    .getSingleResult();
            return cantidad != null && cantidad > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Postulaciones de un candidato, de la mas reciente a la mas antigua.
     */
    public List<Postulacion> listarPorCandidato(Integer usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Postulacion> query = em.createQuery(
                    "SELECT p FROM Postulacion p"
                            + " JOIN FETCH p.oferta o"
                            + " JOIN FETCH o.empresa"
                            + " JOIN FETCH p.candidato c"
                            + " LEFT JOIN FETCH c.perfilCandidato"
                            + " JOIN FETCH p.curriculum"
                            + " WHERE p.candidato.id = :usuarioId"
                            + " ORDER BY p.fechaPostulacion DESC, p.id DESC",
                    Postulacion.class);
            query.setParameter("usuarioId", usuarioId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Postulaciones recibidas por todas las ofertas de una empresa.
     */
    public List<Postulacion> listarPorEmpresa(Integer empresaId) {
        if (empresaId == null) {
            return List.of();
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Postulacion> query = em.createQuery(
                    "SELECT p FROM Postulacion p"
                            + " JOIN FETCH p.oferta o"
                            + " JOIN FETCH o.empresa e"
                            + " JOIN FETCH p.candidato c"
                            + " LEFT JOIN FETCH c.perfilCandidato"
                            + " JOIN FETCH p.curriculum"
                            + " WHERE e.id = :empresaId"
                            + " ORDER BY p.fechaPostulacion DESC, p.id DESC",
                    Postulacion.class);
            query.setParameter("empresaId", empresaId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Todas las postulaciones (uso exclusivo del administrador).
     */
    public List<Postulacion> listarTodas() {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Postulacion> query = em.createQuery(
                    "SELECT p FROM Postulacion p"
                            + " JOIN FETCH p.oferta o"
                            + " JOIN FETCH o.empresa"
                            + " JOIN FETCH p.candidato c"
                            + " LEFT JOIN FETCH c.perfilCandidato"
                            + " JOIN FETCH p.curriculum"
                            + " ORDER BY p.fechaPostulacion DESC, p.id DESC",
                    Postulacion.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Indica si un archivo ya esta referenciado por alguna postulacion.
     * Se usa para impedir que el candidato borre el CV historico enviado.
     */
    public boolean archivoEnUso(Integer archivoId) {
        if (archivoId == null) {
            return false;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            Long cantidad = em.createQuery(
                    "SELECT COUNT(p) FROM Postulacion p"
                            + " WHERE p.curriculum.id = :archivoId",
                    Long.class)
                    .setParameter("archivoId", archivoId)
                    .getSingleResult();
            return cantidad != null && cantidad > 0;
        } finally {
            em.close();
        }
    }
}
