package com.empleasv.api.dto;

import com.empleasv.model.PerfilEmpresa;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representación del perfil empresarial.
 * Solo debe devolverse a la propia empresa o a un administrador.
 */
public final class PerfilEmpresaDto {

    private PerfilEmpresaDto() {
    }

    public static Map<String, Object> aMapa(PerfilEmpresa perfil) {
        if (perfil == null) {
            return null;
        }
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", perfil.getId());
        mapa.put("usuario", UsuarioDto.aMapa(perfil.getUsuario()));
        mapa.put("nombreResponsable", perfil.getNombreResponsable());
        mapa.put("nombreEmpresa", perfil.getNombreEmpresa());
        mapa.put("nit", perfil.getNit());
        mapa.put("telefonoEmpresa", perfil.getTelefonoEmpresa());
        mapa.put("direccionEmpresa", perfil.getDireccionEmpresa());
        mapa.put("municipio", perfil.getMunicipio());
        mapa.put("departamento", perfil.getDepartamento());
        mapa.put("sector", perfil.getSector());
        mapa.put("sitioWeb", perfil.getSitioWeb());
        mapa.put("descripcion", perfil.getDescripcion());
        mapa.put("logo", ArchivoDto.aMapa(perfil.getLogo()));
        mapa.put("empresaId", perfil.getEmpresa() != null
                ? perfil.getEmpresa().getId() : null);
        mapa.put("fechaActualizacion", perfil.getFechaActualizacion() != null
                ? perfil.getFechaActualizacion().toString() : null);
        return mapa;
    }
}
