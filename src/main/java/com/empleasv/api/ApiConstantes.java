package com.empleasv.api;

/**
 * Constantes compartidas por la capa REST de EmpleaSV.
 *
 * Esta capa es aditiva: no reemplaza los servlets JSP existentes.
 */
public final class ApiConstantes {

    /** Prefijo base de todos los endpoints REST. */
    public static final String RUTA_BASE = "/api";

    /** Tipo de contenido usado en solicitudes y respuestas JSON. */
    public static final String JSON = "application/json";

    /** Codificación usada en solicitudes y respuestas. */
    public static final String CHARSET = "UTF-8";

    /** Límite de lectura para cuerpos JSON (1 MB). */
    public static final int MAX_CUERPO_BYTES = 1024 * 1024;

    /** Límite de anidamiento aceptado por el lector JSON. */
    public static final int MAX_PROFUNDIDAD_JSON = 32;

    private ApiConstantes() {
    }
}
