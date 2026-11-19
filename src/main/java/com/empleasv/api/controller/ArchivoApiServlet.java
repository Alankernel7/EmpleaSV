
package com.empleasv.api.controller;

import com.empleasv.api.ApiExcepcion;
import com.empleasv.api.ApiServletBase;
import com.empleasv.api.ApiSesion;
import com.empleasv.dao.ArchivoDAO;
import com.empleasv.model.Archivo;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.service.ArchivoService;
import com.empleasv.util.ArchivosUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@WebServlet("/api/archivos/*")
public class ArchivoApiServlet extends ApiServletBase {

    private final ArchivoDAO archivoDAO = new ArchivoDAO();

    private ArchivoService archivoService() {
        return new ArchivoService(getServletContext());
    }

    // GET /api/archivos/{id}
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        try {
            List<String> ruta = segmentos(request);

            if (ruta.size() != 1) {
                throw new ApiExcepcion(
                        404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }

            Usuario usuario =
                    ApiSesion.requerirAutenticado(request);

            Integer archivoId = idDeRuta(request);

            if (archivoId <= 0) {
                throw new ApiExcepcion(
                        400, "ID_INVALIDO",
                        "El identificador debe ser positivo.");
            }

            Archivo archivo =
                    archivoDAO.obtenerPorId(archivoId);

            if (archivo == null) {
                throw new ApiExcepcion(
                        404, "RECURSO_NO_ENCONTRADO",
                        "El archivo solicitado no existe.");
            }

            boolean administrador =
                    usuario.getRol() == UsuarioRol.ADMINISTRADOR;

            boolean propietario =
                    archivoDAO.esPropietario(
                            archivoId, usuario.getId());

            if (!administrador && !propietario) {
                throw new ApiExcepcion(
                        403, "PROHIBIDO",
                        "No tienes permiso para descargar este archivo.");
            }

            Path rutaArchivo;

            try {
                rutaArchivo = archivoService().rutaDe(archivo);

            } catch (IOException e) {
                throw new ApiExcepcion(
                        404, "RECURSO_NO_ENCONTRADO",
                        "El archivo fisico no esta disponible.");
            }

            if (!Files.isRegularFile(rutaArchivo)) {
                throw new ApiExcepcion(
                        404, "RECURSO_NO_ENCONTRADO",
                        "El archivo fisico no esta disponible.");
            }

            boolean imagen =
                    archivo.getCategoria()
                            == Archivo.CategoriaArchivo.FOTO_PERFIL
                            || archivo.getCategoria()
                            == Archivo.CategoriaArchivo.LOGO_EMPRESA;

            boolean pdf =
                    "pdf".equalsIgnoreCase(
                            archivo.getExtension());

            boolean mostrarEnNavegador =
                    "1".equals(request.getParameter("inline"))
                            && (imagen || pdf);

            response.setContentType(archivo.getTipoMime());

            response.setHeader(
                    "X-Content-Type-Options", "nosniff");

            response.setHeader(
                    "Cache-Control",
                    "private, no-store, max-age=0");

            response.setHeader(
                    "Content-Disposition",
                    ArchivosUtil.contentDisposition(
                            archivo.getNombreOriginal(),
                            mostrarEnNavegador)
            );

            response.setContentLengthLong(
                    Files.size(rutaArchivo));

            Files.copy(
                    rutaArchivo,
                    response.getOutputStream());

            response.getOutputStream().flush();

        } catch (Exception e) {
            if (response.isCommitted()) {
                getServletContext().log(
                        "Error durante la descarga REST", e);
            } else {
                manejar(e, response);
            }
        }
    }
}
