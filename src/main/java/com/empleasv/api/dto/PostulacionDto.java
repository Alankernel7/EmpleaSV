package com.empleasv.api.dto;

import com.empleasv.dao.PerfilCandidatoDAO;
import com.empleasv.model.Archivo;
import com.empleasv.model.OfertaEmpleo;
import com.empleasv.model.PerfilCandidato;
import com.empleasv.model.Postulacion;
import com.empleasv.model.Usuario;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representación de una postulación para la API REST.
 *
 * Incluye los datos del candidato y de la oferta que las vistas necesitan,
 * además de la URL autenticada para descargar el CV exacto que se envió.
 */
public final class PostulacionDto {

    private static final PerfilCandidatoDAO PERFIL_CANDIDATO_DAO = new PerfilCandidatoDAO();

    private PostulacionDto() {
    }

    public static Map<String, Object> aMapa(Postulacion postulacion) {
        if (postulacion == null) {
            return null;
        }

        OfertaEmpleo oferta = postulacion.getOferta();
        Usuario candidato = postulacion.getCandidato();
        Archivo curriculum = postulacion.getCurriculum();

        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", postulacion.getId());
        mapa.put("estado", postulacion.getEstado() != null
                ? postulacion.getEstado().name() : null);
        mapa.put("mensajePresentacion", postulacion.getMensajePresentacion());
        mapa.put("fechaPostulacion", postulacion.getFechaPostulacion() != null
                ? postulacion.getFechaPostulacion().toString() : null);

        mapa.put("ofertaId", oferta != null ? oferta.getId() : null);
        mapa.put("ofertaTitulo", oferta != null ? oferta.getTitulo() : null);
        mapa.put("empresaId", oferta != null && oferta.getEmpresa() != null
                ? oferta.getEmpresa().getId() : null);
        mapa.put("empresaNombre", oferta != null && oferta.getEmpresa() != null
                ? oferta.getEmpresa().getNombre() : null);

        mapa.put("candidatoId", candidato != null ? candidato.getId() : null);
        mapa.put("candidatoNombre", candidato != null ? candidato.getNombre() : null);
        mapa.put("candidatoEmail", candidato != null ? candidato.getEmail() : null);

        if (candidato != null) {
            PerfilCandidato perfil = PERFIL_CANDIDATO_DAO
                    .obtenerPorUsuarioId(candidato.getId());
            mapa.put("candidatoTelefono", perfil != null ? perfil.getTelefono() : null);
            mapa.put("candidatoMunicipio", perfil != null ? perfil.getMunicipio() : null);
            mapa.put("candidatoDepartamento", perfil != null ? perfil.getDepartamento() : null);
        }

        mapa.put("curriculumId", curriculum != null ? curriculum.getId() : null);
        mapa.put("curriculumNombre", curriculum != null
                ? curriculum.getNombreOriginal() : null);
        mapa.put("urlCv", "/api/postulaciones/" + postulacion.getId() + "/cv");

        return mapa;
    }
}
