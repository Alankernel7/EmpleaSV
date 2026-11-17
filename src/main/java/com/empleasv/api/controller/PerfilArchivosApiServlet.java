
package com.empleasv.api.controller;

import com.empleasv.api.ApiExcepcion;
import com.empleasv.api.ApiServletBase;
import com.empleasv.api.ApiSesion;
import com.empleasv.api.dto.ArchivoDto;
import com.empleasv.dao.ArchivoDAO;
import com.empleasv.model.Archivo;
import com.empleasv.model.PerfilCandidato;
import com.empleasv.model.PerfilEmpresa;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.service.PerfilService;
import com.empleasv.service.RegistroException;
import com.empleasv.util.ArchivoNoValidoException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(urlPatterns = {
        "/api/perfiles/candidato/foto",
        "/api/perfiles/candidato/curriculums",
        "/api/perfiles/candidato/curriculums/*",
        "/api/perfiles/empresa/logo"
})
@MultipartConfig(
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024,
        fileSizeThreshold = 0
)
public class PerfilArchivosApiServlet extends ApiServletBase {

    private static final String FOTO =
            "/api/perfiles/candidato/foto";

    private static final String CURRICULUMS =
            "/api/perfiles/candidato/curriculums";

    private static final String LOGO =
            "/api/perfiles/empresa/logo";

    private final ArchivoDAO archivoDAO = new ArchivoDAO();

    private PerfilService servicio() {
        return new PerfilService(getServletContext());
    }

    // GET /api/perfiles/candidato/curriculums
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        try {
            if (!CURRICULUMS.equals(request.getServletPath())
                    || !segmentos(request).isEmpty()) {
                noEncontrado();
            }

            Usuario usuario = ApiSesion.requerirRol(
                    request, UsuarioRol.USUARIO);

            List<Map<String, Object>> resultado =
                    new ArrayList<>();

            for (Archivo archivo :
                    servicio().listarCurriculums(usuario.getId())) {

                resultado.add(ArchivoDto.aMapa(archivo));
            }

            responderOk(response, resultado);

        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // POST: subir foto, curriculum o logo
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        try {
            String ruta = request.getServletPath();

            if (!segmentos(request).isEmpty()) {
                noEncontrado();
            }

            Usuario usuario;
            String campo;

            if (FOTO.equals(ruta)) {
                usuario = ApiSesion.requerirRol(
                        request, UsuarioRol.USUARIO);
                campo = "foto";

            } else if (CURRICULUMS.equals(ruta)) {
                usuario = ApiSesion.requerirRol(
                        request, UsuarioRol.USUARIO);
                campo = "curriculum";

            } else if (LOGO.equals(ruta)) {
                usuario = ApiSesion.requerirRol(
                        request, UsuarioRol.EMPRESA);
                campo = "logo";

            } else {
                noEncontrado();
                return;
            }

            exigirMultipart(request);

            boolean principal = false;

            if (CURRICULUMS.equals(ruta)) {
                String valor = request.getParameter("principal");

                if (valor != null
                        && !valor.equalsIgnoreCase("true")
                        && !valor.equalsIgnoreCase("false")) {

                    throw new ApiExcepcion(
                            400, "DATOS_INVALIDOS",
                            "principal debe ser true o false.");
                }

                principal = "true".equalsIgnoreCase(valor);
            }

            Part parte = request.getPart(campo);

            if (parte == null || parte.getSize() <= 0
                    || parte.getSubmittedFileName() == null
                    || parte.getSubmittedFileName().isBlank()) {

                throw new ApiExcepcion(
                        400, "DATOS_INVALIDOS",
                        "Debes seleccionar un archivo en el campo "
                                + campo + ".");
            }

            PerfilService perfilService = servicio();
            Archivo archivo;
            String mensaje;

            if (FOTO.equals(ruta)) {

                archivo = perfilService.subirFoto(usuario, parte);
                mensaje = "Fotografia actualizada correctamente.";

            } else if (CURRICULUMS.equals(ruta)) {

                archivo = perfilService.agregarCurriculum(
                        usuario, parte);

                if (principal) {
                    perfilService.fijarCurriculumPrincipal(
                            usuario.getId(), archivo.getId());
                }

                mensaje = "Curriculum agregado correctamente.";

            } else {

                archivo = perfilService.subirLogo(usuario, parte);
                mensaje = "Logotipo actualizado correctamente.";
            }

            response.setHeader(
                    "Location",
                    request.getContextPath()
                            + "/api/archivos/" + archivo.getId()
            );

            responderCreado(
                    response,
                    mensaje,
                    ArchivoDto.aMapa(archivo)
            );

        } catch (ArchivoNoValidoException e) {
            manejar(errorArchivo(e), response);

        } catch (RegistroException e) {
            manejar(errorRegistro(e), response);

        } catch (IllegalStateException e) {
            manejar(new ApiExcepcion(
                    413, "CUERPO_DEMASIADO_GRANDE",
                    "El archivo supera el limite permitido."
            ), response);

        } catch (ServletException e) {
            manejar(new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "No se pudo interpretar el formulario multipart."
            ), response);

        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // DELETE: eliminar foto, logo o curriculum propio
    @Override
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response)
            throws IOException {

        try {
            String ruta = request.getServletPath();
            List<String> partes = segmentos(request);
            PerfilService perfilService = servicio();

            if (FOTO.equals(ruta) && partes.isEmpty()) {

                Usuario usuario = ApiSesion.requerirRol(
                        request, UsuarioRol.USUARIO);

                PerfilCandidato perfil =
                        perfilService.obtenerOCrearCandidato(
                                usuario.getId());

                if (perfil == null
                        || perfil.getFotoPerfil() == null) {
                    noEncontrado();
                }

                perfilService.eliminarFoto(usuario.getId());

            } else if (LOGO.equals(ruta) && partes.isEmpty()) {

                Usuario usuario = ApiSesion.requerirRol(
                        request, UsuarioRol.EMPRESA);

                PerfilEmpresa perfil =
                        perfilService.obtenerOCrearEmpresa(
                                usuario.getId());

                if (perfil == null || perfil.getLogo() == null) {
                    noEncontrado();
                }

                perfilService.eliminarLogo(usuario.getId());

            } else if (CURRICULUMS.equals(ruta)
                    && partes.size() == 1) {

                Usuario usuario = ApiSesion.requerirRol(
                        request, UsuarioRol.USUARIO);

                Integer archivoId = idDeRuta(request);

                validarCurriculumPropio(
                        archivoId, usuario.getId());

                perfilService.eliminarCurriculumArchivo(
                        usuario.getId(), archivoId);

            } else {
                noEncontrado();
            }

            responderNoContenido(response);

        } catch (RegistroException e) {
            manejar(errorRegistro(e), response);

        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // PATCH /api/perfiles/candidato/curriculums/{id}/principal
    @Override
    protected void doPatch(HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException {

        try {
            String ruta = request.getServletPath();
            List<String> partes = segmentos(request);

            if (!CURRICULUMS.equals(ruta)
                    || partes.size() != 2
                    || !"principal".equals(partes.get(1))) {

                noEncontrado();
            }

            Usuario usuario = ApiSesion.requerirRol(
                    request, UsuarioRol.USUARIO);

            Integer archivoId = idDeRuta(request);

            validarCurriculumPropio(
                    archivoId, usuario.getId());

            servicio().fijarCurriculumPrincipal(
                    usuario.getId(), archivoId);

            responderOk(
                    response,
                    "Curriculum principal actualizado.",
                    Map.of(
                            "archivoId", archivoId,
                            "principal", true
                    )
            );

        } catch (RegistroException e) {
            manejar(errorRegistro(e), response);

        } catch (Exception e) {
            manejar(e, response);
        }
    }

    private void validarCurriculumPropio(
            Integer archivoId, Integer usuarioId) {

        if (archivoId <= 0) {
            throw new ApiExcepcion(
                    400, "ID_INVALIDO",
                    "El identificador debe ser positivo.");
        }

        if (!archivoDAO.esCurriculumPropio(
                archivoId, usuarioId)) {

            throw new ApiExcepcion(
                    404, "RECURSO_NO_ENCONTRADO",
                    "El curriculum no existe o no pertenece al usuario.");
        }
    }

    private void exigirMultipart(HttpServletRequest request) {

        String tipo = request.getContentType();

        if (tipo == null || !tipo.toLowerCase(Locale.ROOT)
                .startsWith("multipart/form-data;")) {

            throw new ApiExcepcion(
                    415, "TIPO_CONTENIDO_NO_SOPORTADO",
                    "La solicitud debe usar multipart/form-data.");
        }
    }

    private ApiExcepcion errorArchivo(
            ArchivoNoValidoException error) {

        String mensaje = error.getMessage();
        String texto = mensaje == null
                ? "" : mensaje.toLowerCase(Locale.ROOT);

        if (texto.contains("tamano maximo")) {
            return new ApiExcepcion(
                    413, "CUERPO_DEMASIADO_GRANDE", mensaje);
        }

        if (texto.contains("extension no permitida")
                || texto.contains("tipo de contenido")) {

            return new ApiExcepcion(
                    415, "TIPO_CONTENIDO_NO_SOPORTADO", mensaje);
        }

        return new ApiExcepcion(
                400, "DATOS_INVALIDOS", mensaje);
    }

    private ApiExcepcion errorRegistro(
            RegistroException error) {

        return new ApiExcepcion(
                409,
                "REGLA_NEGOCIO",
                error.getMessage() == null
                        ? "No se pudo completar la operacion."
                        : error.getMessage()
        );
    }

    private void noEncontrado() {
        throw new ApiExcepcion(
                404, "RECURSO_NO_ENCONTRADO",
                "El recurso solicitado no existe.");
    }
}
