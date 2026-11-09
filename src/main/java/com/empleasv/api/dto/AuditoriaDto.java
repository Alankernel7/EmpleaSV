package com.empleasv.api.dto;

import com.empleasv.model.Auditoria;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representación de un registro de auditoría para el panel administrativo.
 */
public final class AuditoriaDto {

    private AuditoriaDto() {
    }

    public static Map<String, Object> aMapa(Auditoria auditoria) {
        if (auditoria == null) {
            return null;
        }
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", auditoria.getId());
        mapa.put("adminId", auditoria.getAdmin() != null
                ? auditoria.getAdmin().getId() : null);
        mapa.put("accion", auditoria.getAccion());
        mapa.put("objetivoTipo", auditoria.getObjetivoTipo() != null
                ? auditoria.getObjetivoTipo().name() : null);
        mapa.put("objetivoId", auditoria.getObjetivoId());
        mapa.put("detalle", auditoria.getDetalle());
        mapa.put("fecha", auditoria.getFecha() != null
                ? auditoria.getFecha().toString() : null);
        return mapa;
    }
}
