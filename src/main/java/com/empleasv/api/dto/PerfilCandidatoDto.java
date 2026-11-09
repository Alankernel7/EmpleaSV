package com.empleasv.api.dto;

import com.empleasv.model.Archivo;
import com.empleasv.model.PerfilCandidato;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Representación del perfil de candidato.
 * Solo debe devolverse al propio candidato o a un administrador.
 */
public final class PerfilCandidatoDto {

    private PerfilCandidatoDto() {
    }

    public static Map<String, Object> aMapa(PerfilCandidato perfil,
                                            List<Archivo> curriculums) {
        if (perfil == null) {
            return null;
        }
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", perfil.getId());
        mapa.put("usuario", UsuarioDto.aMapa(perfil.getUsuario()));
        mapa.put("telefono", perfil.getTelefono());
        mapa.put("direccionResidencia", perfil.getDireccionResidencia());
        mapa.put("municipio", perfil.getMunicipio());
        mapa.put("departamento", perfil.getDepartamento());
        mapa.put("foto", ArchivoDto.aMapa(perfil.getFotoPerfil()));
        mapa.put("curriculumPrincipal", ArchivoDto.aMapa(perfil.getCurriculum()));
        mapa.put("fechaActualizacion", perfil.getFechaActualizacion() != null
                ? perfil.getFechaActualizacion().toString() : null);

        List<Map<String, Object>> lista = new ArrayList<>();
        if (curriculums != null) {
            for (Archivo archivo : curriculums) {
                lista.add(ArchivoDto.aMapa(archivo));
            }
        }
        mapa.put("curriculums", lista);
        return mapa;
    }
}
