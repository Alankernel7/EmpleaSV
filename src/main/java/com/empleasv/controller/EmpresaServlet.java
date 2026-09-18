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

// Controlador para empresas
@WebServlet("/empresas")
public class EmpresaServlet extends HttpServlet {

    private EmpresaDAO empresaDAO;
    private OfertaEmpleoDAO ofertaDAO;

    @Override
    public void init() {
        empresaDAO = new EmpresaDAO();
        ofertaDAO = new OfertaEmpleoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if (accion == null) {
            accion = "listar";
        }

        switch (accion) {

            case "detalle":
                mostrarDetalleEmpresa(request, response);
                break;

            case "listar":
            default:
                listarEmpresas(request, response);
                break;
        }
    }

    // Lista todas las empresas
    private void listarEmpresas(HttpServletRequest request,
                                HttpServletResponse response)
            throws ServletException, IOException {

        List<Empresa> empresas =
                empresaDAO.listarConCantidadOfertas();

        request.setAttribute(
                "empresas",
                empresas
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/empresas.jsp"
        ).forward(request, response);
    }

    // Muestra una empresa y sus ofertas
    private void mostrarDetalleEmpresa(HttpServletRequest request,
                                       HttpServletResponse response)
            throws ServletException, IOException {

        String idParametro =
                request.getParameter("id");

        int empresaId;

        try {
            empresaId =
                    Integer.parseInt(idParametro);

        } catch (NumberFormatException | NullPointerException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID de empresa no valido."
            );

            return;
        }

        Empresa empresa =
                empresaDAO.obtenerPorId(empresaId);

        if (empresa == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Empresa no encontrada."
            );

            return;
        }

        List<OfertaEmpleo> ofertas =
                ofertaDAO.listarPorEmpresa(empresaId);

        request.setAttribute(
                "empresa",
                empresa
        );

        request.setAttribute(
                "ofertas",
                ofertas
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/detalle-empresa.jsp"
        ).forward(request, response);
    }
}