package com.empleasv.repository;

import com.empleasv.config.JpaConfig;
import com.empleasv.model.Empresa;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio JPA para operaciones de persistencia de Empresa.
 * Encapsula el uso de EntityManager y consultas JPQL/Criteria.
 */
public class EmpresaRepository {

    /**
     * Lista todas las empresas.
     *
     * @return lista de empresas
     */
    public List<Empresa> listarTodas() {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Empresa> query = em.createQuery("SELECT e FROM Empresa e ORDER BY e.nombre", Empresa.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene una empresa por su ID.
     *
     * @param id ID de la empresa
     * @return empresa o null si no existe
     */
    public Empresa obtenerPorId(Integer id) {
        if (id == null) {
            return null;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            return em.find(Empresa.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Lista las 4 empresas con mayor cantidad de ofertas publicadas.
     *
     * @return lista de empresas destacadas
     */
    public List<Empresa> listarDestacadas() {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            String jpql = "SELECT e FROM Empresa e " +
                          "LEFT JOIN e.ofertas o " +
                          "GROUP BY e " +
                          "ORDER BY COUNT(o) DESC";
            TypedQuery<Empresa> query = em.createQuery(jpql, Empresa.class);
            query.setMaxResults(4);
            List<Empresa> result = query.getResultList();

            // Calcular cantidadOfertas para cada empresa (compatibilidad Sprint 1)
            for (Empresa e : result) {
                Long count = contarOfertasPorEmpresa(e.getId());
                e.setCantidadOfertas(count.intValue());
            }

            return result;
        } finally {
            em.close();
        }
    }

    /**
     * Lista todas las empresas con su cantidad de ofertas.
     *
     * @return lista de empresas con cantidadOfertas calculada
     */
    public List<Empresa> listarConCantidadOfertas() {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            // Consulta que trae empresas y cuenta ofertas en una sola query
            String jpql = "SELECT NEW com.empleasv.model.Empresa(e.id, e.nombre, e.descripcion, e.categoria, e.email, e.telefono, COUNT(o)) " +
                          "FROM Empresa e " +
                          "LEFT JOIN e.ofertas o " +
                          "GROUP BY e.id, e.nombre, e.descripcion, e.categoria, e.email, e.telefono " +
                          "ORDER BY e.nombre ASC";

            // Como no podemos usar constructor con COUNT en JPQL fácilmente sin DTO,
            // hacemos dos pasos: traer empresas y luego calcular count
            List<Empresa> empresas = listarTodas();

            for (Empresa e : empresas) {
                Long count = contarOfertasPorEmpresa(e.getId());
                e.setCantidadOfertas(count.intValue());
            }

            return empresas;
        } finally {
            em.close();
        }
    }

    /**
     * Cuenta las ofertas de una empresa.
     *
     * @param empresaId ID de la empresa
     * @return número de ofertas
     */
    private Long contarOfertasPorEmpresa(Integer empresaId) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(o) FROM OfertaEmpleo o WHERE o.empresa.id = :empresaId", Long.class);
            query.setParameter("empresaId", empresaId);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Guarda una nueva empresa.
     *
     * @param empresa empresa a guardar
     * @return empresa guardada con ID generado
     */
    public Empresa guardar(Empresa empresa) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(empresa);
            em.getTransaction().commit();
            return empresa;
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
     * Actualiza una empresa existente.
     *
     * @param empresa empresa a actualizar
     * @return empresa actualizada
     */
    public Empresa actualizar(Empresa empresa) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            Empresa merged = em.merge(empresa);
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
     * Elimina una empresa por ID.
     *
     * @param id ID de la empresa
     * @return true si se eliminó
     */
    public boolean eliminar(Integer id) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            Empresa empresa = em.find(Empresa.class, id);
            if (empresa != null) {
                em.remove(empresa);
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
}