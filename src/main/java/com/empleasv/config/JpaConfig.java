package com.empleasv.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Configuración centralizada para JPA/Hibernate.
 * Provee EntityManagerFactory singleton y gestión de EntityManager.
 * Evita fugas de recursos asegurando cierre correcto.
 */
public class JpaConfig {

    private static final String PERSISTENCE_UNIT_NAME = "empleasvPU";

    private static EntityManagerFactory entityManagerFactory;

    private JpaConfig() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Inicializa el EntityManagerFactory.
     * Debe llamarse una sola vez al iniciar la aplicación.
     */
    public static synchronized void init() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
    }

    /**
     * Obtiene el EntityManagerFactory.
     * Inicializa si no está creado.
     *
     * @return EntityManagerFactory configurado
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            init();
        }
        return entityManagerFactory;
    }

    /**
     * Crea un nuevo EntityManager.
     * El llamador es responsable de cerrarlo.
     *
     * @return nuevo EntityManager
     */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Verifica si el EntityManagerFactory está abierto.
     *
     * @return true si está inicializado y abierto
     */
    public static boolean isInitialized() {
        return entityManagerFactory != null && entityManagerFactory.isOpen();
    }

    /**
     * Cierra el EntityManagerFactory.
     * Debe llamarse al apagar la aplicación.
     */
    public static synchronized void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }
}