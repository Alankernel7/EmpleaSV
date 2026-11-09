package com.empleasv.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Excepción de negocio o de protocolo de la API.
 *
 * Lleva el código HTTP y el código de error que se devolverán al cliente.
 */
public class ApiExcepcion extends RuntimeException {

    private final int estado;
    private final String codigo;
    private final List<ApiError.Detalle> detalles;

    public ApiExcepcion(int estado, String codigo, String mensaje) {
        this(estado, codigo, mensaje, null);
    }

    public ApiExcepcion(int estado, String codigo, String mensaje,
                        List<ApiError.Detalle> detalles) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
    }

    public int getEstado() {
        return estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public List<ApiError.Detalle> getDetalles() {
        return new ArrayList<>(detalles);
    }

    public ApiRespuesta aRespuesta() {
        return ApiRespuesta.error(estado, codigo, getMessage(), detalles);
    }
}
