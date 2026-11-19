package com.empleasv.controller;

import com.empleasv.dao.EmpresaDAO;
import com.empleasv.dao.OfertaEmpleoDAO;
import com.empleasv.dao.UsuarioDAO;
import com.empleasv.model.Empresa;
import com.empleasv.model.OfertaEmpleo;
import com.empleasv.model.PerfilCandidato;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
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
 * Controlador de la página de inicio.
 *
 * Despacha según el rol autenticado:
 *  - sin sesión      → inicio público (Sprint 1)
 *  - USUARIO         → panel del candidato con datos reales
 *  - EMPRESA         → panel empresarial
 *  - ADMINISTRADOR   → gestión de ofertas
 */
@WebServlet("/inicio")
public class InicioServlet extends HttpServlet {

    private OfertaEmpleoDAO ofertaDAO;
    private EmpresaDAO empresaDAO;
    private UsuarioDAO usuarioDAO;
    private PerfilService perfilService;

    @Override
    public void init() throws ServletException {
        ofertaDAO = new OfertaEmpleoDAO();
        empresaDAO = new EmpresaDAO();
        usuarioDAO = new UsuarioDAO();
        perfilService = new PerfilService(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        SesionUtil.consumirMensajes(request);

        Usuario usuario = SesionUtil.usuario(request);

        if (usuario != null && usuario.getRol() == UsuarioRol.EMPRESA) {
            response.sendRedirect(request.getContextPath() + "/panel-empresa");
            return;
        }

        if (usuario != null && usuario.getRol() == UsuarioRol.ADMINISTRADOR) {
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        // Ofertas y empresas destacadas: datos reales, compartidos por ambos inicios
        List<OfertaEmpleo> ofertasDestacadas = ofertaDAO.listarDestacadas();
        List<Empresa> empresasDestacadas = empresaDAO.listarDestacadas();

        request.setAttribute("ofertasDestacadas", ofertasDestacadas);
        request.setAttribute("empresasDestacadas", empresasDestacadas);

        // Contadores reales del inicio publico (nada estimado)
        List<OfertaEmpleo> todasLasOfertas = ofertaDAO.listarTodas();
        int activas = 0;
        for (OfertaEmpleo oferta : todasLasOfertas) {
            if ("ACTIVA".equalsIgnoreCase(oferta.getEstado())) {
                activas++;
            }
        }
        request.setAttribute("totalOfertas", todasLasOfertas.size());
        request.setAttribute("ofertasActivas", activas);
        request.setAttribute("totalEmpresas", empresaDAO.listarTodas().size());
        request.setAttribute("totalCandidatos",
                usuarioDAO.listarPorRol(UsuarioRol.USUARIO).size());

        if (usuario != null && usuario.getRol() == UsuarioRol.USUARIO) {
            PerfilCandidato perfil = perfilService.obtenerOCrearCandidato(usuario.getId());
            request.setAttribute("perfilCandidato", perfil);
            request.getRequestDispatcher("/WEB-INF/views/panel-candidato.jsp")
                    .forward(request, response);
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/index.jsp")
                .forward(request, response);
    }
}
