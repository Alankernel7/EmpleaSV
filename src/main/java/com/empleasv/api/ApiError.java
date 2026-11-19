package com.empleasv.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Error uniforme de la API.
 */
public final class ApiError {

    /** Detalle de validación asociado a un campo del cuerpo o de la URL. */
    public static final class Detalle {

        private final String campo;
        private final String mensaje;

        public Detalle(String campo, String mensaje) {
            this.campo = campo;
            this.mensaje = mensaje;
        }

        public String getCampo() {
            return campo;
        }

        public String getMensaje() {
            return mensaje;
        }

        public Map<String, Object> aMapa() {
            Map<String, Object> mapa = new LinkedHashMap<>();
            mapa.put("campo", campo);
            mapa.put("mensaje", mensaje);
            return mapa;
        }
    }

    private final String codigo;
    private final String mensaje;
    private final List<Detalle> detalles;

    public ApiError(String codigo, String mensaje, List<Detalle> detalles) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public List<Detalle> getDetalles() {
        return new ArrayList<>(detalles);
    }

    public Map<String, Object> aMapa() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("codigo", codigo);
        mapa.put("mensaje", mensaje);
        if (!detalles.isEmpty()) {
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Detalle detalle : detalles) {
                lista.add(detalle.aMapa());
            }
            mapa.put("detalles", lista);
        }
        return mapa;
    }
}
