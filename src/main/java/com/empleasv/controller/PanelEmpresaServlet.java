package com.empleasv.controller;

import com.empleasv.dao.OfertaEmpleoDAO;
import com.empleasv.model.OfertaEmpleo;
import com.empleasv.model.PerfilEmpresa;
import com.empleasv.model.Usuario;
import com.empleasv.service.PerfilService;
import com.empleasv.util.SesionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Panel de la cuenta EMPRESA.
 *
 * Muestra la bienvenida, los datos de la organización y las ofertas
 * publicadas por esa empresa, con indicadores calculados con datos reales.
 *
 * Si la cuenta no tiene perfil empresarial completo, redirige a la pantalla
 * de completar perfil antes de permitir publicar ofertas.
 */
@WebServlet("/panel-empresa")
public class PanelEmpresaServlet extends HttpServlet {

    private OfertaEmpleoDAO ofertaDAO;
    private PerfilService perfilService;

    @Override
    public void init() throws ServletException {
        ofertaDAO = new OfertaEmpleoDAO();
        perfilService = new PerfilService(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SesionUtil.consumirMensajes(request);

        Usuario usuario = SesionUtil.usuario(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/auth?accion=login&restringido=1");
            return;
        }

        PerfilEmpresa perfil = perfilService.obtenerOCrearEmpresa(usuario.getId());

        if (PerfilEmpresaServlet.estaIncompleto(perfil)) {
            SesionUtil.mensajeError(request,
                    "Completa la informacion obligatoria de tu perfil empresarial "
                            + "antes de publicar ofertas.");
            response.sendRedirect(request.getContextPath() + "/perfil-empresa?accion=completar");
            return;
        }

        Integer empresaId = perfilService.asegurarEmpresaVinculada(usuario.getId());
        if (empresaId == null) {
            SesionUtil.mensajeError(request,
                    "No se pudo vincular tu cuenta con una empresa. "
                            + "Completa el perfil empresarial.");
            response.sendRedirect(request.getContextPath() + "/perfil-empresa?accion=completar");
            return;
        }

        SesionUtil.fijarEmpresaEnSesion(request, empresaId);

        List<OfertaEmpleo> ofertas = ofertaDAO.listarPorEmpresa(empresaId);

        int activas = 0;
        int pausadas = 0;
        int cerradas = 0;

        for (OfertaEmpleo oferta : ofertas) {
            String estado = oferta.getEstado();
            if ("ACTIVA".equalsIgnoreCase(estado)) {
                activas++;
            } else if ("PAUSADA".equalsIgnoreCase(estado)) {
                pausadas++;
            } else if ("CERRADA".equalsIgnoreCase(estado)) {
                cerradas++;
            }
        }

        request.setAttribute("perfilEmpresa", perfil);
        request.setAttribute("empresaId", empresaId);
        request.setAttribute("ofertas", ofertas);
        request.setAttribute("totalOfertas", ofertas.size());
        request.setAttribute("ofertasActivas", activas);
        request.setAttribute("ofertasPausadas", pausadas);
        request.setAttribute("ofertasCerradas", cerradas);

        request.getRequestDispatcher("/WEB-INF/views/panel-empresa.jsp")
                .forward(request, response);
    }
}
