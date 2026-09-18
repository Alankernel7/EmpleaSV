package com.empleasv.controller;

import com.empleasv.dao.EmpresaDAO;
import com.empleasv.dao.OfertaEmpleoDAO;
import com.empleasv.model.Empresa;
import com.empleasv.model.OfertaEmpleo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

// Controlador de la página de inicio
@WebServlet("/inicio")
public class InicioServlet extends HttpServlet {

    private OfertaEmpleoDAO ofertaDAO;
    private EmpresaDAO empresaDAO;

    @Override
    public void init() throws ServletException {
        ofertaDAO = new OfertaEmpleoDAO();
        empresaDAO = new EmpresaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Obtiene las 6 ofertas activas más recientes
        List<OfertaEmpleo> ofertasDestacadas =
                ofertaDAO.listarDestacadas();

        // Obtiene las 4 empresas con mayor cantidad de ofertas
        List<Empresa> empresasDestacadas =
                empresaDAO.listarDestacadas();

        // Envía los datos a la vista
        request.setAttribute(
                "ofertasDestacadas",
                ofertasDestacadas
        );

        request.setAttribute(
                "empresasDestacadas",
                empresasDestacadas
        );

        // Envía la petición al JSP
        request.getRequestDispatcher(
                "/WEB-INF/views/index.jsp"
        ).forward(request, response);
    }
}
