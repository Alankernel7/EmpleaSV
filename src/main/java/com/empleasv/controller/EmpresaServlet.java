package com.empleasv.controller;

import com.empleasv.dao.EmpresaDAO;
import com.empleasv.model.Empresa;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

// Controlador para la página de empresas
@WebServlet("/empresas")
public class EmpresaServlet extends HttpServlet {

    private EmpresaDAO empresaDAO;

    @Override
    public void init() {
        empresaDAO = new EmpresaDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener todas las empresas con su cantidad de ofertas
        List<Empresa> empresas =
                empresaDAO.listarConCantidadOfertas();

        // Enviar los datos a la vista
        request.setAttribute(
                "empresas",
                empresas
        );

        // Mostrar la vista
        request.getRequestDispatcher(
                "/WEB-INF/views/empresas.jsp"
        ).forward(request, response);
    }
}