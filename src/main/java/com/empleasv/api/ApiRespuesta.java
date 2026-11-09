package com.empleasv.api;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Respuesta uniforme de la API.
 *
 * Formato exitoso:
 * { "exito": true, "mensaje": "...", "datos": ... }
 *
 * Formato de error:
 * { "exito": false, "error": { "codigo": "...", "mensaje": "...", "detalles": [...] } }
 */
public final class ApiRespuesta {

    private final int estado;
    private final boolean exito;
    private final String mensaje;
    private final Object datos;
    private final ApiError error;
    private final Map<String, String> cabeceras = new LinkedHashMap<>();

    private ApiRespuesta(int estado, boolean exito, String mensaje,
                         Object datos, ApiError error) {
        this.estado = estado;
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
        this.error = error;
    }

    public static ApiRespuesta ok(Object datos) {
        return new ApiRespuesta(200, true, "OK", datos, null);
    }

    public static ApiRespuesta ok(String mensaje, Object datos) {
        return new ApiRespuesta(200, true, mensaje, datos, null);
    }

    public static ApiRespuesta creado(String mensaje, Object datos) {
        return new ApiRespuesta(201, true, mensaje, datos, null);
    }

    public static ApiRespuesta sinContenido() {
        return new ApiRespuesta(204, true, null, null, null);
    }

    public static ApiRespuesta error(int estado, String codigo, String mensaje) {
        return new ApiRespuesta(estado, false, null, null, new ApiError(codigo, mensaje, null));
    }

    public static ApiRespuesta error(int estado, String codigo, String mensaje,
                                     List<ApiError.Detalle> detalles) {
        return new ApiRespuesta(estado, false, null, null,
                new ApiError(codigo, mensaje, detalles));
    }

    public ApiRespuesta cabecera(String nombre, String valor) {
        if (nombre != null && valor != null) {
            cabeceras.put(nombre, valor);
        }
        return this;
    }

    public int getEstado() {
        return estado;
    }

    public boolean isExito() {
        return exito;
    }

    public String aJson() {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("exito", exito);
        if (exito) {
            if (mensaje != null) {
                cuerpo.put("mensaje", mensaje);
            }
            cuerpo.put("datos", datos);
        } else if (error != null) {
            cuerpo.put("error", error.aMapa());
        }
        return ApiJson.serializar(cuerpo);
    }

    /** Escribe la respuesta HTTP completa. */
    public void escribir(HttpServletResponse response) throws IOException {
        for (Map.Entry<String, String> cabecera : cabeceras.entrySet()) {
            response.setHeader(cabecera.getKey(), cabecera.getValue());
        }
        response.setCharacterEncoding(ApiConstantes.CHARSET);
        response.setContentType(ApiConstantes.JSON);
        response.setStatus(estado);
        if (estado == 204) {
            return;
        }
        response.getWriter().write(aJson());
        response.getWriter().flush();
    }
}
