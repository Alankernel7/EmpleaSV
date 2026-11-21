package com.empleasv.api.controller;

import com.empleasv.api.ApiExcepcion;
import com.empleasv.api.ApiServletBase;
import com.empleasv.api.ApiSesion;
import com.empleasv.api.dto.PostulacionDto;
import com.empleasv.model.Archivo;
import com.empleasv.model.Postulacion;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.service.ArchivoService;
import com.empleasv.service.PostulacionService;
import com.empleasv.service.RegistroException;
import com.empleasv.util.ArchivoNoValidoException;
import com.empleasv.util.ArchivosUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * API REST de postulaciones.
 *
 * GET    /api/postulaciones          lista según el rol autenticado
 * GET    /api/postulaciones/{id}     detalle
 * GET    /api/postulaciones/{id}/cv  descarga del CV enviado
 * POST   /api/postulaciones          crea (multipart con CV o JSON con curriculumId)
 * PATCH  /api/postulaciones/{id}/estado  cambia el estado (empresa dueña o admin)
 */
@WebServlet(urlPatterns = {"/api/postulaciones", "/api/postulaciones/*"})
@MultipartConfig(
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024
)
public class PostulacionApiServlet extends ApiServletBase {

    private PostulacionService postulacionService;
    private ArchivoService archivoService;

    @Override
    public void init() throws ServletException {
        postulacionService = new PostulacionService(getServletContext());
        archivoService = new ArchivoService(getServletContext());
    }

    // ==================== GET ====================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Usuario usuario = ApiSesion.requerirAutenticado(request);
            List<String> ruta = segmentos(request);

            if (ruta.isEmpty()) {
                listar(request, response, usuario);
                return;
            }

            Integer id = idDeRuta(request);
            Postulacion postulacion = postulacionService.obtener(id);
            if (postulacion == null) {
                throw new ApiExcepcion(404, "NO_ENCONTRADO",
                        "La postulacion solicitada no existe.");
            }
            if (!postulacionService.puedeVer(usuario, postulacion)) {
                throw new ApiExcepcion(403, "PROHIBIDO",
                        "No tienes permisos para acceder a esta postulacion.");
            }

            if (ruta.size() == 1) {
                responderOk(response, PostulacionDto.aMapa(postulacion));
                return;
            }
            if (ruta.size() == 2 && "cv".equalsIgnoreCase(ruta.get(1))) {
                descargarCv(request, response, postulacion);
                return;
            }
            throw new ApiExcepcion(404, "NO_ENCONTRADO",
                    "El recurso solicitado no existe.");
        } catch (Exception e) {
            manejar(e, response);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response, Usuario usuario)
            throws IOException {
        List<Postulacion> postulaciones;
        if (usuario.getRol() == UsuarioRol.USUARIO) {
            postulaciones = postulacionService.listarMias(usuario.getId());
        } else if (usuario.getRol() == UsuarioRol.EMPRESA) {
            postulaciones = postulacionService.listarRecibidas(
                    ApiSesion.empresaIdDeSesion(request));
        } else if (usuario.getRol() == UsuarioRol.ADMINISTRADOR) {
            postulaciones = postulacionService.listarTodas();
        } else {
            throw new ApiExcepcion(403, "PROHIBIDO",
                    "No tienes permisos para consultar postulaciones.");
        }

        List<Map<String, Object>> datos = new ArrayList<>();
        for (Postulacion postulacion : postulaciones) {
            datos.add(PostulacionDto.aMapa(postulacion));
        }
        responderOk(response, datos);
    }

    private void descargarCv(HttpServletRequest request, HttpServletResponse response,
                             Postulacion postulacion) throws IOException {
        Archivo curriculum = postulacion.getCurriculum();
        if (curriculum == null) {
            throw new ApiExcepcion(404, "CV_NO_DISPONIBLE",
                    "La postulacion no tiene un curriculum asociado.");
        }

        Path ruta;
        try {
            ruta = archivoService.rutaDe(curriculum);
        } catch (IOException e) {
            throw new ApiExcepcion(404, "CV_NO_DISPONIBLE",
                    "El archivo fisico no esta disponible.");
        }
        if (!Files.isRegularFile(ruta)) {
            throw new ApiExcepcion(404, "CV_NO_DISPONIBLE",
                    "El archivo fisico no esta disponible.");
        }

        boolean inline = "1".equals(request.getParameter("inline"))
                || "true".equalsIgnoreCase(request.getParameter("inline"));
        response.setContentType(curriculum.getTipoMime());
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store, max-age=0");
        response.setHeader("Content-Disposition", ArchivosUtil.contentDisposition(
                curriculum.getNombreOriginal(), inline));
        response.setContentLengthLong(Files.size(ruta));

        Files.copy(ruta, response.getOutputStream());
        response.getOutputStream().flush();
    }

    // ==================== POST ====================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (!segmentos(request).isEmpty()) {
                throw new ApiExcepcion(404, "NO_ENCONTRADO",
                        "El recurso solicitado no existe.");
            }

            Usuario usuario = ApiSesion.requerirRol(request, UsuarioRol.USUARIO);
            String contentType = request.getContentType();
            boolean multipart = contentType != null
                    && contentType.toLowerCase(Locale.ROOT)
                            .startsWith("multipart/form-data");

            Integer ofertaId;
            Integer curriculumId = null;
            String mensaje;
            Part archivoCv = null;

            if (multipart) {
                request.setCharacterEncoding("UTF-8");
                ofertaId = enteroObligatorio(
                        primerValor(request.getParameter("ofertaId"),
                                request.getParameter("oferta_id")), "ofertaId");
                curriculumId = enteroOpcional(
                        primerValor(request.getParameter("curriculumId"),
                                request.getParameter("curriculum_id")));
                mensaje = request.getParameter("mensaje");
                try {
                    archivoCv = request.getPart("curriculum");
                } catch (IllegalStateException e) {
                    throw new ApiExcepcion(413, "ARCHIVO_DEMASIADO_GRANDE",
                            "El CV supera el tamano maximo permitido de 5 MB.");
                } catch (ServletException e) {
                    throw new ApiExcepcion(400, "SOLICITUD_INVALIDA",
                            "No se pudo leer el archivo enviado.");
                }
            } else {
                exigirJson(request);
                Map<String, Object> datos = leerObjeto(request);
                ofertaId = enteroObligatorio(
                        primerValor(datos.get("ofertaId"), datos.get("oferta_id")),
                        "ofertaId");
                curriculumId = enteroOpcional(
                        primerValor(datos.get("curriculumId"), datos.get("curriculum_id")));
                mensaje = textoOpcional(datos.get("mensaje"));
            }

            Postulacion creada = postulacionService.crear(
                    usuario, ofertaId, mensaje, curriculumId, archivoCv);
            Postulacion completa = postulacionService.obtener(creada.getId());
            if (completa == null) {
                completa = creada;
            }

            response.setHeader("Location", request.getContextPath()
                    + "/api/postulaciones/" + completa.getId());
            responderCreado(response, "Postulacion registrada correctamente.",
                    PostulacionDto.aMapa(completa));
        } catch (RegistroException e) {
            int estado = esErrorDePermiso(e.getMessage()) ? 403 : 409;
            String codigo = estado == 403 ? "PROHIBIDO" : "CONFLICTO";
            manejar(new ApiExcepcion(estado, codigo, e.getMessage()), response);
        } catch (ArchivoNoValidoException e) {
            int estado = e.getMessage() != null
                    && e.getMessage().toLowerCase(Locale.ROOT).contains("supera")
                    ? 413 : 415;
            String codigo = estado == 413
                    ? "ARCHIVO_DEMASIADO_GRANDE" : "ARCHIVO_NO_VALIDO";
            manejar(new ApiExcepcion(estado, codigo, e.getMessage()), response);
        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // ==================== PATCH ====================

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<String> ruta = segmentos(request);
            if (ruta.size() != 2 || !"estado".equalsIgnoreCase(ruta.get(1))) {
                throw new ApiExcepcion(404, "NO_ENCONTRADO",
                        "El recurso solicitado no existe.");
            }

            Usuario usuario = ApiSesion.requerirRol(request,
                    UsuarioRol.EMPRESA, UsuarioRol.ADMINISTRADOR);
            Integer id = idDeRuta(request);

            exigirJson(request);
            Map<String, Object> datos = leerObjeto(request);
            Object valor = datos.get("estado");
            if (!(valor instanceof String)) {
                throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                        "El campo 'estado' es obligatorio y debe ser texto.");
            }

            Postulacion actualizada;
            try {
                actualizada = postulacionService.actualizarEstado(
                        usuario, id, (String) valor);
            } catch (RegistroException e) {
                int estado = esErrorDePermiso(e.getMessage()) ? 403 : 409;
                String codigo = estado == 403 ? "PROHIBIDO" : "CONFLICTO";
                throw new ApiExcepcion(estado, codigo, e.getMessage());
            }

            responderOk(response, "Estado de la postulacion actualizado.",
                    PostulacionDto.aMapa(actualizada));
        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // ==================== Utilidades ====================

    private void exigirJson(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null
                || !contentType.toLowerCase(Locale.ROOT).contains("application/json")) {
            throw new ApiExcepcion(415, "TIPO_CONTENIDO_NO_SOPORTADO",
                    "Esta operacion requiere un cuerpo JSON o multipart/form-data.");
        }
    }

    private Object primerValor(Object... valores) {
        for (Object valor : valores) {
            if (valor != null) {
                return valor;
            }
        }
        return null;
    }

    private Integer enteroObligatorio(Object valor, String campo) {
        Integer numero = enteroOpcional(valor);
        if (numero == null) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El campo '" + campo + "' es obligatorio y debe ser un entero positivo.");
        }
        return numero;
    }

    private Integer enteroOpcional(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number) {
            long numero = ((Number) valor).longValue();
            if (numero <= 0 || numero > Integer.MAX_VALUE) {
                throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                        "El identificador debe ser un entero positivo.");
            }
            return (int) numero;
        }
        if (valor instanceof String) {
            String texto = ((String) valor).trim();
            if (texto.isEmpty()) {
                return null;
            }
            try {
                int numero = Integer.parseInt(texto);
                if (numero <= 0) {
                    throw new NumberFormatException();
                }
                return numero;
            } catch (NumberFormatException e) {
                throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                        "El identificador debe ser un entero positivo.");
            }
        }
        throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                "El identificador debe ser un entero positivo.");
    }

    private String textoOpcional(Object valor) {
        if (valor == null) {
            return null;
        }
        if (!(valor instanceof String)) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El campo 'mensaje' debe ser texto.");
        }
        String texto = ((String) valor).trim();
        if (texto.isEmpty()) {
            return null;
        }
        if (texto.length() > 2000) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El mensaje no debe exceder 2000 caracteres.");
        }
        return texto;
    }

    private boolean esErrorDePermiso(String mensaje) {
        if (mensaje == null) {
            return false;
        }
        String texto = mensaje.toLowerCase(Locale.ROOT);
        return texto.contains("permiso") || texto.contains("solo la empresa");
    }
}
