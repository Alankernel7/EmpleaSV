package com.empleasv.api.dto;

import com.empleasv.model.Usuario;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representación pública y segura de una cuenta.
 * Nunca incluye password_hash.
 */
public final class UsuarioDto {

    private UsuarioDto() {
    }

    public static Map<String, Object> aMapa(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", usuario.getId());
        mapa.put("nombre", usuario.getNombre());
        mapa.put("email", usuario.getEmail());
        mapa.put("rol", usuario.getRol() != null ? usuario.getRol().name() : null);
        mapa.put("estado", usuario.getEstado());
        mapa.put("fechaCreacion", usuario.getFechaCreacion() != null
                ? usuario.getFechaCreacion().toString() : null);
        return mapa;
    }
}
