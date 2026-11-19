package com.empleasv.api.dto;

import com.empleasv.model.Archivo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Metadatos públicos de un archivo.
 *
 * No expone nombre_almacenado ni ruta_relativa: esos datos son internos del
 * servidor. El acceso al contenido se hace por /api/archivos/{id}.
 */
public final class ArchivoDto {

    private ArchivoDto() {
    }

    public static Map<String, Object> aMapa(Archivo archivo) {
        if (archivo == null) {
            return null;
        }
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", archivo.getId());
        mapa.put("nombreOriginal", archivo.getNombreOriginal());
        mapa.put("tipoMime", archivo.getTipoMime());
        mapa.put("extension", archivo.getExtension());
        mapa.put("tamanoBytes", archivo.getTamanoBytes());
        mapa.put("categoria", archivo.getCategoria() != null
                ? archivo.getCategoria().name() : null);
        mapa.put("fechaSubida", archivo.getFechaSubida() != null
                ? archivo.getFechaSubida().toString() : null);
        if (archivo.getId() != null) {
            mapa.put("urlDescarga", "/api/archivos/" + archivo.getId());
        }
        return mapa;
    }
}
