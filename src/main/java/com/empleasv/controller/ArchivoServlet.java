package com.empleasv.controller;

import com.empleasv.dao.ArchivoDAO;
import com.empleasv.model.Archivo;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.service.ArchivoService;
import com.empleasv.util.SesionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Descarga controlada de archivos (currículum, fotografía y logotipo).
 *
 * Solo el propietario del archivo (o un administrador) puede descargarlo,
 * por lo que el currículum de un candidato nunca queda expuesto a otros.
 * La ruta física nunca se recibe del cliente: se resuelve a partir de la
 * base de datos y se valida contra el directorio base.
 */
@WebServlet("/archivo")
public class ArchivoServlet extends HttpServlet {

    private ArchivoDAO archivoDAO;
    private ArchivoService archivoService;

    @Override
    public void init() throws ServletException {
        archivoDAO = new ArchivoDAO();
        archivoService = new ArchivoService(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer archivoId = parsearId(request.getParameter("id"));
        if (archivoId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Identificador de archivo no valido.");
            return;
        }

        Archivo archivo = archivoDAO.obtenerPorId(archivoId);
        if (archivo == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Archivo no encontrado.");
            return;
        }

        Usuario usuario = SesionUtil.usuario(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath()
                    + "/auth?accion=login&restringido=1");
            return;
        }

        boolean esAdministrador = usuario.getRol() == UsuarioRol.ADMINISTRADOR;
        boolean esDueno = archivoDAO.esPropietario(archivoId, usuario.getId());

        if (!esDueno && !esAdministrador) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "No tienes permiso para acceder a este archivo.");
            return;
        }

        Path ruta;
        try {
            ruta = archivoService.rutaDe(archivo);
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "El archivo fisico no esta disponible.");
            return;
        }

        if (!Files.isRegularFile(ruta)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "El archivo fisico no esta disponible.");
            return;
        }

        boolean esImagen = archivo.getCategoria() == Archivo.CategoriaArchivo.FOTO_PERFIL
                || archivo.getCategoria() == Archivo.CategoriaArchivo.LOGO_EMPRESA;
        boolean esPdf = archivo.getExtension() != null
                && "pdf".equalsIgnoreCase(archivo.getExtension().trim());
        boolean inline = (esImagen || esPdf) && "1".equals(request.getParameter("inline"));

        response.setContentType(archivo.getTipoMime());
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store, max-age=0");
        response.setHeader("Content-Disposition",
                com.empleasv.util.ArchivosUtil.contentDisposition(
                        archivo.getNombreOriginal(), inline));
        response.setContentLengthLong(Files.size(ruta));

        Files.copy(ruta, response.getOutputStream());
        response.getOutputStream().flush();
    }

    private Integer parsearId(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
