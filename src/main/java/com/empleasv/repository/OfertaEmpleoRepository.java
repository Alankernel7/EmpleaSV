package com.empleasv.repository;

import com.empleasv.config.JpaConfig;
import com.empleasv.model.OfertaEmpleo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para operaciones de persistencia de OfertaEmpleo.
 * Encapsula el uso de EntityManager y consultas JPQL.
 */
public class OfertaEmpleoRepository {

    /**
     * Lista todas las ofertas.
     *
     * @return lista de ofertas
     */
    public List<OfertaEmpleo> listarTodas() {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<OfertaEmpleo> query = em.createQuery(
                    "SELECT o FROM OfertaEmpleo o JOIN FETCH o.empresa ORDER BY o.fechaPublicacion DESC", OfertaEmpleo.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene una oferta por su ID.
     *
     * @param id ID de la oferta
     * @return oferta o null si no existe
     */
    public OfertaEmpleo obtenerPorId(Integer id) {
        if (id == null) {
            return null;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<OfertaEmpleo> query = em.createQuery(
                    "SELECT o FROM OfertaEmpleo o JOIN FETCH o.empresa WHERE o.id = :id",
                    OfertaEmpleo.class);
            query.setParameter("id", id);
            List<OfertaEmpleo> resultados = query.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Lista las ofertas activas más recientes para mostrar en el inicio.
     *
     * @return lista de ofertas destacadas (máx 6)
     */
    public List<OfertaEmpleo> listarDestacadas() {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<OfertaEmpleo> query = em.createQuery(
                    "SELECT o FROM OfertaEmpleo o " +
                    "JOIN FETCH o.empresa e " +
                    "WHERE o.estado = 'ACTIVA' " +
                    "ORDER BY o.fechaPublicacion DESC", OfertaEmpleo.class);
            query.setMaxResults(6);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Busca ofertas por título (búsqueda parcial con LIKE).
     *
     * @param titulo texto a buscar
     * @return lista de ofertas coincidentes
     */
    public List<OfertaEmpleo> buscarPorTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            return listarTodas();
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<OfertaEmpleo> query = em.createQuery(
                    "SELECT o FROM OfertaEmpleo o " +
                    "JOIN FETCH o.empresa " +
                    "WHERE LOWER(o.titulo) LIKE LOWER(:titulo) " +
                    "ORDER BY o.fechaPublicacion DESC", OfertaEmpleo.class);
            query.setParameter("titulo", "%" + titulo + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Filtra ofertas por ubicación exacta.
     *
     * @param ubicacion ubicación a filtrar
     * @return lista de ofertas en esa ubicación
     */
    public List<OfertaEmpleo> filtrarPorUbicacion(String ubicacion) {
        if (ubicacion == null || ubicacion.trim().isEmpty()) {
            return listarTodas();
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<OfertaEmpleo> query = em.createQuery(
                    "SELECT o FROM OfertaEmpleo o " +
                    "JOIN FETCH o.empresa " +
                    "WHERE o.ubicacion = :ubicacion " +
                    "ORDER BY o.fechaPublicacion DESC", OfertaEmpleo.class);
            query.setParameter("ubicacion", ubicacion);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lista todas las ofertas pertenecientes a una empresa.
     *
     * @param empresaId ID de la empresa
     * @return lista de ofertas de la empresa
     */
    public List<OfertaEmpleo> listarPorEmpresa(Integer empresaId) {
        if (empresaId == null) {
            return List.of();
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<OfertaEmpleo> query = em.createQuery(
                    "SELECT o FROM OfertaEmpleo o " +
                    "JOIN FETCH o.empresa " +
                    "WHERE o.empresa.id = :empresaId " +
                    "ORDER BY o.fechaPublicacion DESC", OfertaEmpleo.class);
            query.setParameter("empresaId", empresaId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Registra una nueva oferta en la base de datos.
     *
     * @param oferta oferta a registrar
     * @return true si se registró correctamente
     */
    public boolean registrar(OfertaEmpleo oferta) {
        if (oferta == null) {
            return false;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(oferta);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza una oferta existente en la base de datos.
     *
     * @param oferta oferta a actualizar
     * @return true si se actualizó correctamente
     */
    public boolean actualizar(OfertaEmpleo oferta) {
        if (oferta == null || oferta.getId() == null) {
            return false;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(oferta);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Elimina una oferta por su ID.
     *
     * @param id ID de la oferta
     * @return true si se eliminó correctamente
     */
    public boolean eliminar(Integer id) {
        if (id == null) {
            return false;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            OfertaEmpleo oferta = em.find(OfertaEmpleo.class, id);
            if (oferta != null) {
                em.remove(oferta);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Busca y filtra ofertas para el panel administrativo.
     *
     * @param texto  título o nombre de empresa, puede ser null
     * @param estado estado (ACTIVA/PAUSADA/CERRADA) o null para todos
     * @return lista filtrada de ofertas
     */
    public List<OfertaEmpleo> buscarAdministrativa(String texto, String estado) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT o FROM OfertaEmpleo o JOIN FETCH o.empresa e WHERE 1 = 1");
            if (texto != null && !texto.trim().isEmpty()) {
                jpql.append(" AND (LOWER(o.titulo) LIKE LOWER(:texto) OR LOWER(e.nombre) LIKE LOWER(:texto))");
            }
            if (estado != null && !estado.trim().isEmpty()) {
                jpql.append(" AND o.estado = :estado");
            }
            jpql.append(" ORDER BY o.fechaPublicacion DESC");
            TypedQuery<OfertaEmpleo> query = em.createQuery(jpql.toString(), OfertaEmpleo.class);
            if (texto != null && !texto.trim().isEmpty()) {
                query.setParameter("texto", "%" + texto.trim() + "%");
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
     * Cuenta ofertas por estado.
     *
     * @param estado estado a contar o null para el total
     * @return cantidad de ofertas
     */
    public long contarPorEstado(String estado) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT COUNT(o) FROM OfertaEmpleo o WHERE 1 = 1");
            if (estado != null && !estado.trim().isEmpty()) {
                jpql.append(" AND o.estado = :estado");
            }
            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            if (estado != null && !estado.trim().isEmpty()) {
                query.setParameter("estado", estado.trim());
            }
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Lista las ofertas más recientes (panel administrativo).
     *
     * @param limite máximo de ofertas a devolver
     * @return lista de ofertas
     */
    public List<OfertaEmpleo> listarRecientes(int limite) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<OfertaEmpleo> query = em.createQuery(
                    "SELECT o FROM OfertaEmpleo o JOIN FETCH o.empresa " +
                    "ORDER BY o.fechaPublicacion DESC", OfertaEmpleo.class);
            query.setMaxResults(Math.max(1, limite));
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}