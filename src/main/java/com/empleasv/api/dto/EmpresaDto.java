package com.empleasv.api.dto;

import com.empleasv.model.Empresa;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representación pública de una empresa. No incluye datos de la cuenta
 * propietaria ni información interna de auditoría.
 */
public final class EmpresaDto {

    private EmpresaDto() {
    }

    public static Map<String, Object> aMapa(Empresa empresa) {
        if (empresa == null) {
            return null;
        }
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", empresa.getId());
        mapa.put("nombre", empresa.getNombre());
        mapa.put("descripcion", empresa.getDescripcion());
        mapa.put("categoria", empresa.getCategoria());
        mapa.put("email", empresa.getEmail());
        mapa.put("telefono", empresa.getTelefono());
        if (empresa.getCantidadOfertas() != null) {
            mapa.put("cantidadOfertas", empresa.getCantidadOfertas());
        }
        return mapa;
    }
}
