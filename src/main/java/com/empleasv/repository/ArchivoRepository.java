package com.empleasv.repository;

import com.empleasv.config.JpaConfig;
import com.empleasv.model.Archivo;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Repositorio JPA para los metadatos de archivos subidos
 * (fotografía de perfil, currículum y logotipo empresarial).
 */
public class ArchivoRepository {

    /**
     * Obtiene un archivo por su ID.
     *
     * @param id ID del archivo
     * @return archivo o null
     */
    public Archivo obtenerPorId(Integer id) {
        if (id == null) {
            return null;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            return em.find(Archivo.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Verifica que un archivo pertenezca al usuario indicado.
     *
     * @param archivoId ID del archivo
     * @param usuarioId ID del usuario
     * @return true si el archivo es del usuario
     */
    public boolean esPropietario(Integer archivoId, Integer usuarioId) {
        if (archivoId == null || usuarioId == null) {
            return false;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            Long cantidad = em.createQuery(
                    "SELECT COUNT(a) FROM Archivo a WHERE a.id = :archivoId AND a.usuario.id = :usuarioId",
                    Long.class)
                    .setParameter("archivoId", archivoId)
                    .setParameter("usuarioId", usuarioId)
                    .getSingleResult();
            return cantidad != null && cantidad > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Verifica que un archivo sea un curriculum propio del usuario indicado.
     *
     * @param archivoId ID del archivo
     * @param usuarioId ID del usuario
     * @return true si el archivo pertenece al usuario y es un curriculum
     */
    public boolean esCurriculumPropio(Integer archivoId, Integer usuarioId) {
        if (archivoId == null || usuarioId == null) {
            return false;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            Long cantidad = em.createQuery(
                    "SELECT COUNT(a) FROM Archivo a WHERE a.id = :archivoId"
                            + " AND a.usuario.id = :usuarioId"
                            + " AND a.categoria = :categoria",
                    Long.class)
                    .setParameter("archivoId", archivoId)
                    .setParameter("usuarioId", usuarioId)
                    .setParameter("categoria", Archivo.CategoriaArchivo.CURRICULUM)
                    .getSingleResult();
            return cantidad != null && cantidad > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Lista todos los curriculum subidos por un usuario, del mas reciente al
     * mas antiguo. El curriculum principal se resuelve por separado a partir
     * de {@code perfil_candidato.curriculum_id}, por lo que esta consulta no
     * modifica el esquema ni necesita columnas nuevas.
     *
     * @param usuarioId ID del usuario propietario
     * @return archivos CURRICULUM del usuario (vacia si no hay ninguno)
     */
    public List<Archivo> listarCurriculums(Integer usuarioId) {
        if (usuarioId == null) {
            return Collections.emptyList();
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM Archivo a WHERE a.usuario.id = :usuarioId"
                            + " AND a.categoria = :categoria"
                            + " ORDER BY a.fechaSubida DESC, a.id DESC",
                    Archivo.class)
                    .setParameter("usuarioId", usuarioId)
                    .setParameter("categoria", Archivo.CategoriaArchivo.CURRICULUM)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Persiste un archivo dentro de la transacción indicada.
     *
     * @param em      EntityManager activo
     * @param archivo archivo a persistir
     * @return archivo con ID generado
     */
    public Archivo guardar(EntityManager em, Archivo archivo) {
        if (archivo == null) {
            throw new IllegalArgumentException("Archivo no puede ser null");
        }
        if (archivo.getFechaSubida() == null) {
            archivo.setFechaSubida(LocalDateTime.now());
        }
        em.persist(archivo);
        return archivo;
    }

    /**
     * Persiste un archivo con su propia transacción.
     *
     * @param archivo archivo a persistir
     * @return archivo persistido
     */
    public Archivo guardar(Archivo archivo) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            Archivo resultado = guardar(em, archivo);
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
     * Elimina la referencia metadatos de un archivo.
     * El borrado físico del archivo se realiza por el servicio correspondiente.
     *
     * @param id ID del archivo
     * @return true si se eliminó
     */
    public boolean eliminar(Integer id) {
        if (id == null) {
            return false;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();
            Archivo archivo = em.find(Archivo.class, id);
            boolean eliminado = false;
            if (archivo != null) {
                em.remove(archivo);
                eliminado = true;
            }
            em.getTransaction().commit();
            return eliminado;
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
     * Elimina un archivo dentro de la transacción indicada.
     *
     * @param em      EntityManager activo
     * @param archivo archivo a eliminar
     */
    public void eliminar(EntityManager em, Archivo archivo) {
        if (archivo != null && archivo.getId() != null) {
            Archivo referenciado = em.find(Archivo.class, archivo.getId());
            if (referenciado != null) {
                em.remove(referenciado);
            }
        }
    }
}
