
package com.empleasv.api.controller;

import com.empleasv.api.*;
import com.empleasv.api.dto.OfertaDto;
import com.empleasv.dao.OfertaEmpleoDAO;
import com.empleasv.dao.PerfilEmpresaDAO;
import com.empleasv.model.OfertaEmpleo;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@WebServlet("/api/ofertas/*")
public class OfertaApiServlet extends ApiServletBase {

    private final OfertaEmpleoDAO ofertaDAO = new OfertaEmpleoDAO();
    private final PerfilEmpresaDAO perfilEmpresaDAO = new PerfilEmpresaDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        try {
            List<String> ruta = segmentos(request);

            if (ruta.isEmpty()) {
                listar(request, response);
            } else if (ruta.size() == 1) {
                obtenerDetalle(request, response);
            } else {
                throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }
        } catch (Exception e) {
            manejar(e, response);
        }
    }

    private void listar(HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

        String titulo = request.getParameter("titulo");
        String ubicacion = request.getParameter("ubicacion");
        String estado = request.getParameter("estado");
        String mias = request.getParameter("mias");

        Integer empresaFiltro = leerEmpresaId(request);
        boolean admin = ApiSesion.esAdmin(request);

        if (mias != null && !mias.equalsIgnoreCase("true")
                && !mias.equalsIgnoreCase("false")) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El parámetro mias debe ser true o false.");
        }

        boolean soloPropias = "true".equalsIgnoreCase(mias);
        if (soloPropias) {
            ApiSesion.requerirRol(request, UsuarioRol.EMPRESA);
            empresaFiltro = ApiSesion.empresaIdDeSesion(request);
        }

        if (estado != null) {
            if (!Set.of("ACTIVA", "PAUSADA", "CERRADA").contains(estado)) {
                throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                        "El estado de la oferta no es válido.");
            }
            if (!admin) {
                throw new ApiExcepcion(403, "PROHIBIDO",
                        "No tienes permisos para filtrar por estado.");
            }
        }

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (OfertaEmpleo oferta : ofertaDAO.listarTodas()) {

            if (!admin && !soloPropias
                    && !"ACTIVA".equals(oferta.getEstado())) {
                continue;
            }

            if (empresaFiltro != null
                    && !empresaFiltro.equals(oferta.getEmpresaId())) {
                continue;
            }

            if (estado != null && !estado.equals(oferta.getEstado())) {
                continue;
            }

            if (titulo != null && !titulo.isBlank()
                    && !oferta.getTitulo().toLowerCase(Locale.ROOT)
                    .contains(titulo.trim().toLowerCase(Locale.ROOT))) {
                continue;
            }

            if (ubicacion != null && !ubicacion.isBlank()
                    && !ubicacion.trim().equals(oferta.getUbicacion())) {
                continue;
            }

            resultado.add(OfertaDto.aMapa(
                    oferta, oferta.getEmpresa().getNombre()));
        }

        responderOk(response, resultado);
    }

    private void obtenerDetalle(HttpServletRequest request,
                                HttpServletResponse response) throws IOException {

        Integer id = idDeRuta(request);
        OfertaEmpleo oferta = ofertaDAO.obtenerPorId(id);

        if (oferta == null) {
            throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                    "La oferta solicitada no existe.");
        }

        boolean visible = "ACTIVA".equals(oferta.getEstado())
                || ApiSesion.esAdmin(request);

        Usuario usuario = ApiSesion.usuario(request);

        if (!visible && usuario != null
                && usuario.getRol() == UsuarioRol.EMPRESA) {
            Integer empresaId = perfilEmpresaDAO
                    .obtenerEmpresaIdPorUsuarioId(usuario.getId());

            visible = Objects.equals(
                    empresaId, oferta.getEmpresaId());
        }

        if (!visible) {
            throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                    "La oferta solicitada no existe.");
        }

        responderOk(response, OfertaDto.aMapa(
                oferta, oferta.getEmpresa().getNombre()));
    }

    private Integer leerEmpresaId(HttpServletRequest request) {
        String valor = request.getParameter("empresaId");

        if (valor == null) {
            return null;
        }

        try {
            int id = Integer.parseInt(valor);
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ignored) {
            // Se devuelve el mismo error para IDs inválidos.
        }

        throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                "El parámetro empresaId debe ser un entero positivo.");
    }
}
