package com.empleasv.repository;

import com.empleasv.config.JpaConfig;
import com.empleasv.model.Auditoria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Repositorio JPA para registros de auditoría administrativa.
 */
public class AuditoriaRepository {

    /**
     * Registra una acción administrativa.
     *
     * @param registro registro a persistir
     * @return registro persistido
     */
    public Auditoria guardar(Auditoria registro) {
        if (registro == null) {
            throw new IllegalArgumentException("Registro de auditoria no puede ser null");
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(registro);
            em.getTransaction().commit();
            return registro;
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
     * Lista los últimos registros de auditoría (más recientes primero).
     *
     * @param limite máximo de registros a devolver
     * @return lista de registros
     */
    public List<Auditoria> listarUltimos(int limite) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Auditoria> query = em.createQuery(
                    "SELECT a FROM Auditoria a LEFT JOIN FETCH a.admin " +
                    "ORDER BY a.fecha DESC", Auditoria.class);
            query.setMaxResults(Math.max(1, limite));
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
