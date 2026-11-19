package com.empleasv.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base para los servlets REST de EmpleaSV.
 *
 * Centraliza:
 *  - lectura y límite del cuerpo JSON,
 *  - escritura de respuestas uniformes,
 *  - manejo de errores,
 *  - soporte de PATCH (no incluido en HttpServlet de Servlet 6.0),
 *  - lectura de identificadores desde el path.
 *
 * Los endpoints concretos los implementa el Integrante 2.
 */
public abstract class ApiServletBase extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(request.getMethod())) {
            doPatch(request, response);
        } else {
            super.service(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        metodoNoPermitido(response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        metodoNoPermitido(response, null);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        metodoNoPermitido(response, null);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        metodoNoPermitido(response, null);
    }

    /** PATCH no existe en HttpServlet de Jakarta Servlet 6.0. */
    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        metodoNoPermitido(response, null);
    }

    // ==================== Respuestas ====================

    protected void responder(HttpServletResponse response, ApiRespuesta respuesta)
            throws IOException {
        respuesta.escribir(response);
    }

    protected void responderOk(HttpServletResponse response, Object datos) throws IOException {
        ApiRespuesta.ok(datos).escribir(response);
    }

    protected void responderOk(HttpServletResponse response, String mensaje, Object datos)
            throws IOException {
        ApiRespuesta.ok(mensaje, datos).escribir(response);
    }

    protected void responderCreado(HttpServletResponse response, String mensaje, Object datos)
            throws IOException {
        ApiRespuesta.creado(mensaje, datos).escribir(response);
    }

    protected void responderNoContenido(HttpServletResponse response) throws IOException {
        ApiRespuesta.sinContenido().escribir(response);
    }

    protected void responderError(HttpServletResponse response, int estado,
                                  String codigo, String mensaje) throws IOException {
        ApiRespuesta.error(estado, codigo, mensaje).escribir(response);
    }

    protected void metodoNoPermitido(HttpServletResponse response, String permitidos)
            throws IOException {
        ApiRespuesta respuesta = ApiRespuesta.error(405, "METODO_NO_PERMITIDO",
                "Método HTTP no permitido para este recurso.");
        if (permitidos != null) {
            respuesta.cabecera("Allow", permitidos);
        }
        respuesta.escribir(response);
    }

    /** Convierte una excepción en la respuesta JSON correspondiente. */
    protected void manejar(Throwable error, HttpServletResponse response) throws IOException {
        if (error instanceof ApiExcepcion) {
            ((ApiExcepcion) error).aRespuesta().escribir(response);
            return;
        }
        getServletContext().log("Error no controlado en la API", error);
        ApiRespuesta.error(500, "ERROR_INTERNO",
                "Ocurrió un error interno. Intenta nuevamente más tarde.").escribir(response);
    }

    // ==================== Lectura de la solicitud ====================

    /** Lee el cuerpo de la solicitud como texto UTF-8 con límite de tamaño. */
    protected String leerCuerpo(HttpServletRequest request) throws IOException {
        long longitud = request.getContentLengthLong();
        if (longitud > ApiConstantes.MAX_CUERPO_BYTES) {
            throw new ApiExcepcion(413, "CUERPO_DEMASIADO_GRANDE",
                    "El cuerpo de la solicitud excede el límite permitido.");
        }
        request.setCharacterEncoding(ApiConstantes.CHARSET);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            char[] buffer = new char[1024];
            int leidos;
            while ((leidos = lector.read(buffer)) != -1) {
                sb.append(buffer, 0, leidos);
                if (sb.length() > ApiConstantes.MAX_CUERPO_BYTES) {
                    throw new ApiExcepcion(413, "CUERPO_DEMASIADO_GRANDE",
                            "El cuerpo de la solicitud excede el límite permitido.");
                }
            }
        }
        return sb.toString();
    }

    /** Lee el cuerpo JSON y exige que sea un objeto. */
    protected Map<String, Object> leerObjeto(HttpServletRequest request) throws IOException {
        return ApiJsonReader.objeto(leerCuerpo(request));
    }

    // ==================== Path ====================

    /** Devuelve los segmentos no vacíos de {@code pathInfo}. */
    protected List<String> segmentos(HttpServletRequest request) {
        List<String> segmentos = new ArrayList<>();
        String info = request.getPathInfo();
        if (info == null) {
            return segmentos;
        }
        for (String parte : info.split("/")) {
            if (!parte.isEmpty()) {
                segmentos.add(parte);
            }
        }
        return segmentos;
    }

    /** Devuelve un segmento del path o null si no existe. */
    protected String segmento(HttpServletRequest request, int indice) {
        List<String> partes = segmentos(request);
        return indice >= 0 && indice < partes.size() ? partes.get(indice) : null;
    }

    /** Lee el primer segmento del path como ID numérico. */
    protected Integer idDeRuta(HttpServletRequest request) {
        return idDeRuta(request, 0);
    }

    /** Lee el segmento indicado como ID numérico. */
    protected Integer idDeRuta(HttpServletRequest request, int indice) {
        String valor = segmento(request, indice);
        if (valor == null || valor.isEmpty()) {
            throw new ApiExcepcion(400, "ID_REQUERIDO",
                    "El identificador del recurso es obligatorio en la ruta.");
        }
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            throw new ApiExcepcion(400, "ID_INVALIDO",
                    "El identificador del recurso debe ser numérico.");
        }
    }
}
