package com.empleasv.api.dto;

import com.empleasv.model.OfertaEmpleo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representación de una oferta de empleo.
 */
public final class OfertaDto {

    private OfertaDto() {
    }

    public static Map<String, Object> aMapa(OfertaEmpleo oferta, String empresaNombre) {
        if (oferta == null) {
            return null;
        }
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", oferta.getId());
        mapa.put("titulo", oferta.getTitulo());
        mapa.put("descripcion", oferta.getDescripcion());
        mapa.put("requisitos", oferta.getRequisitos());
        mapa.put("ubicacion", oferta.getUbicacion());
        mapa.put("salario", oferta.getSalario());
        mapa.put("tipoContrato", oferta.getTipoContrato());
        mapa.put("horario", oferta.getHorario());
        mapa.put("fechaPublicacion", oferta.getFechaPublicacion() != null
                ? oferta.getFechaPublicacion().toString() : null);
        mapa.put("estado", oferta.getEstado());
        mapa.put("empresaId", oferta.getEmpresaId());
        mapa.put("empresaNombre", empresaNombre);
        return mapa;
    }
}
