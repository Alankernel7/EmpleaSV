package com.empleasv.controller;

import com.empleasv.dao.PerfilCandidatoDAO;
import com.empleasv.model.Archivo;
import com.empleasv.model.PerfilCandidato;
import com.empleasv.model.Postulacion;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.service.ArchivoService;
import com.empleasv.service.PerfilService;
import com.empleasv.service.PostulacionService;
import com.empleasv.service.RegistroException;
import com.empleasv.util.ArchivoNoValidoException;
import com.empleasv.util.ArchivosUtil;
import com.empleasv.util.SesionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Gestión web de postulaciones.
 *
 * Acciones GET:
 *  - mis       (USUARIO)  lista las postulaciones del candidato
 *  - recibidas (EMPRESA)  lista las postulaciones de sus ofertas
 *  - detalle   (dueños)   detalle de una postulación
 *  - cv        (dueños)   descarga o abre el CV enviado
 *
 * Acciones POST:
 *  - crear  (USUARIO)  registra la postulación con CV obligatorio
 *  - estado (EMPRESA)  cambia el estado de una postulación recibida
 */
@WebServlet("/postulaciones")
@MultipartConfig(
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024
)
public class PostulacionServlet extends HttpServlet {

    private PostulacionService postulacionService;
    private ArchivoService archivoService;
    private PerfilService perfilService;
    private PerfilCandidatoDAO perfilCandidatoDAO;

    @Override
    public void init() throws ServletException {
        postulacionService = new PostulacionService(getServletContext());
        archivoService = new ArchivoService(getServletContext());
        perfilService = new PerfilService(getServletContext());
        perfilCandidatoDAO = new PerfilCandidatoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuario = SesionUtil.usuario(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath()
                    + "/auth?accion=login&restringido=1");
            return;
        }

        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = usuario.getRol() == UsuarioRol.USUARIO ? "mis" : "recibidas";
        }

        switch (accion) {
            case "mis":
                mostrarMias(request, response, usuario);
                break;
            case "recibidas":
                mostrarRecibidas(request, response, usuario);
                break;
            case "detalle":
                mostrarDetalle(request, response, usuario);
                break;
            case "cv":
                descargarCv(request, response, usuario);
                break;
            default:
                SesionUtil.redirigirSegunRol(request, response, usuario.getRol());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Usuario usuario = SesionUtil.usuario(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath()
                    + "/auth?accion=login&restringido=1");
            return;
        }

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "";
        }

        switch (accion) {
            case "crear":
                crearPostulacion(request, response, usuario);
                break;
            case "estado":
                cambiarEstado(request, response, usuario);
                break;
            default:
                SesionUtil.redirigirSegunRol(request, response, usuario.getRol());
        }
    }

    // ==================== Listados ====================

    private void mostrarMias(HttpServletRequest request, HttpServletResponse response,
                             Usuario usuario)
            throws ServletException, IOException {

        if (usuario.getRol() != UsuarioRol.USUARIO) {
            SesionUtil.mensajeError(request, "Solo los candidatos tienen postulaciones propias.");
            SesionUtil.redirigirSegunRol(request, response, usuario.getRol());
            return;
        }

        SesionUtil.consumirMensajes(request);
        List<Postulacion> postulaciones = postulacionService.listarMias(usuario.getId());
        request.setAttribute("postulaciones", postulaciones);
        request.setAttribute("totalPostulaciones", postulaciones.size());
        request.getRequestDispatcher("/WEB-INF/views/mis-postulaciones.jsp")
                .forward(request, response);
    }

    private void mostrarRecibidas(HttpServletRequest request, HttpServletResponse response,
                                  Usuario usuario)
            throws ServletException, IOException {

        if (usuario.getRol() != UsuarioRol.EMPRESA && usuario.getRol() != UsuarioRol.ADMINISTRADOR) {
            SesionUtil.mensajeError(request, "Solo las empresas pueden ver postulaciones recibidas.");
            SesionUtil.redirigirSegunRol(request, response, usuario.getRol());
            return;
        }

        SesionUtil.consumirMensajes(request);
        List<Postulacion> postulaciones;
        try {
            if (usuario.getRol() == UsuarioRol.ADMINISTRADOR) {
                postulaciones = postulacionService.listarTodas();
            } else {
                // Se deriva la empresa desde el usuario autenticado; nunca de
                // un empresa_id enviado en la solicitud.
                Integer empresaId = perfilService.obtenerEmpresaId(usuario.getId());
                if (empresaId == null) {
                    request.setAttribute("mensajeError",
                            "Completa el perfil de empresa para recibir postulaciones.");
                    request.setAttribute("errorCarga", true);
                    postulaciones = List.of();
                } else {
                    postulaciones = postulacionService.listarRecibidas(empresaId);
                }
            }
        } catch (RuntimeException e) {
            getServletContext().log("No se pudieron cargar las postulaciones recibidas.", e);
            request.setAttribute("mensajeError",
                    "No se pudieron cargar las postulaciones. Intenta de nuevo más tarde.");
            request.setAttribute("errorCarga", true);
            postulaciones = List.of();
        }
        int totalRecibidas = postulaciones.size();
        long totalPendientes = postulaciones.stream()
                .filter(p -> p.getEstado() == Postulacion.EstadoPostulacion.PENDIENTE)
                .count();

        String busqueda = request.getParameter("buscar");
        busqueda = busqueda == null ? "" : busqueda.trim();
        String estadoSeleccionado = request.getParameter("estado");
        if (estadoSeleccionado == null) {
            estadoSeleccionado = "";
        } else {
            estadoSeleccionado = estadoSeleccionado.trim().toUpperCase(Locale.ROOT);
        }
        boolean estadoValido = estadoSeleccionado.isEmpty();
        for (Postulacion.EstadoPostulacion estado : Postulacion.EstadoPostulacion.values()) {
            if (estado.name().equals(estadoSeleccionado)) {
                estadoValido = true;
                break;
            }
        }
        if (!estadoValido) {
            estadoSeleccionado = "";
        }

        String busquedaNormalizada = busqueda.toLowerCase(Locale.ROOT);
        final String estadoFiltro = estadoSeleccionado;
        postulaciones = postulaciones.stream()
                .filter(p -> estadoFiltro.isEmpty()
                        || (p.getEstado() != null && estadoFiltro.equals(p.getEstado().name())))
                .filter(p -> busquedaNormalizada.isEmpty()
                        || contiene(p.getCandidato() != null ? p.getCandidato().getNombre() : null,
                                busquedaNormalizada)
                        || contiene(p.getOferta() != null ? p.getOferta().getTitulo() : null,
                                busquedaNormalizada))
                .toList();

        Map<Integer, PerfilCandidato> perfilesCandidato = new HashMap<>();
        for (Postulacion postulacion : postulaciones) {
            if (postulacion.getCandidato() == null || postulacion.getCandidato().getId() == null) {
                continue;
            }
            Integer candidatoId = postulacion.getCandidato().getId();
            if (!perfilesCandidato.containsKey(candidatoId)) {
                perfilesCandidato.put(candidatoId, obtenerPerfilCandidato(candidatoId));
            }
        }

        request.setAttribute("postulaciones", postulaciones);
        request.setAttribute("totalPostulaciones", totalRecibidas);
        request.setAttribute("postulacionesPendientes", totalPendientes);
        request.setAttribute("esAdministrador", usuario.getRol() == UsuarioRol.ADMINISTRADOR);
        request.setAttribute("perfilesCandidato", perfilesCandidato);
        request.setAttribute("busqueda", busqueda);
        request.setAttribute("estadoSeleccionado", estadoSeleccionado);
        List<String> estadosDisponibles = new ArrayList<>();
        for (Postulacion.EstadoPostulacion estado : Postulacion.EstadoPostulacion.values()) {
            estadosDisponibles.add(estado.name());
        }
        request.setAttribute("estados", estadosDisponibles);
        request.getRequestDispatcher("/WEB-INF/views/postulaciones-recibidas.jsp")
                .forward(request, response);
    }

    private void mostrarDetalle(HttpServletRequest request, HttpServletResponse response,
                                Usuario usuario)
            throws ServletException, IOException {

        Integer id = parsearEntero(request.getParameter("id"));
        Postulacion postulacion = postulacionService.obtener(id);
        if (postulacion == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Postulacion no encontrada.");
            return;
        }
        if (!postulacionService.puedeVer(usuario, postulacion)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "No tienes permiso para ver esta postulacion.");
            return;
        }

        SesionUtil.consumirMensajes(request);
        boolean puedeCambiarEstado = usuario.getRol() == UsuarioRol.ADMINISTRADOR
                || postulacionService.esEmpresaPropietaria(usuario, postulacion);

        request.setAttribute("postulacion", postulacion);
        PerfilCandidato perfilCandidato = postulacion.getCandidato() != null
                ? obtenerPerfilCandidato(postulacion.getCandidato().getId())
                : null;
        request.setAttribute("perfilCandidato", perfilCandidato);
        request.setAttribute("puedeCambiarEstado", puedeCambiarEstado);
        request.setAttribute("estados", Postulacion.EstadoPostulacion.values());
        request.getRequestDispatcher("/WEB-INF/views/detalle-postulacion.jsp")
                .forward(request, response);
    }

    // ==================== CV ====================

    private void descargarCv(HttpServletRequest request, HttpServletResponse response,
                             Usuario usuario)
            throws IOException {

        Integer id = parsearEntero(request.getParameter("id"));
        Postulacion postulacion = postulacionService.obtener(id);
        if (postulacion == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Postulacion no encontrada.");
            return;
        }
        if (!postulacionService.puedeVer(usuario, postulacion)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "No tienes permiso para acceder a este curriculum.");
            return;
        }

        Archivo curriculum = postulacion.getCurriculum();
        if (curriculum == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "La postulacion no tiene curriculum asociado.");
            return;
        }

        Path ruta;
        try {
            ruta = archivoService.rutaDe(curriculum);
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

        boolean inline = "1".equals(request.getParameter("inline"));
        response.setContentType(curriculum.getTipoMime());
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store, max-age=0");
        response.setHeader("Content-Disposition", ArchivosUtil.contentDisposition(
                curriculum.getNombreOriginal(), inline));
        response.setContentLengthLong(Files.size(ruta));

        Files.copy(ruta, response.getOutputStream());
        response.getOutputStream().flush();
    }

    // ==================== Acciones POST ====================

    private void crearPostulacion(HttpServletRequest request, HttpServletResponse response,
                                  Usuario usuario)
            throws ServletException, IOException {

        if (usuario.getRol() != UsuarioRol.USUARIO) {
            SesionUtil.mensajeError(request, "Solo los candidatos pueden postularse.");
            SesionUtil.redirigirSegunRol(request, response, usuario.getRol());
            return;
        }

        Integer ofertaId = parsearEntero(primerParam(request, "oferta_id", "ofertaId"));
        Integer curriculumId = parsearEntero(primerParam(request, "curriculum_id", "curriculumId"));
        String mensaje = request.getParameter("mensaje");

        Part curriculum = null;
        try {
            curriculum = request.getPart("curriculum");
        } catch (IllegalStateException e) {
            SesionUtil.mensajeError(request, "El CV supera el tamano maximo permitido de 5 MB.");
            redirigirAOferta(request, response, ofertaId);
            return;
        } catch (ServletException e) {
            SesionUtil.mensajeError(request, "No se pudo leer el archivo enviado.");
            redirigirAOferta(request, response, ofertaId);
            return;
        }

        try {
            postulacionService.crear(usuario, ofertaId, mensaje, curriculumId, curriculum);
            SesionUtil.mensajeExito(request,
                    "Tu postulacion fue enviada correctamente. La empresa revisara tu CV.");
            response.sendRedirect(request.getContextPath() + "/postulaciones?accion=mis");
        } catch (RegistroException | ArchivoNoValidoException e) {
            SesionUtil.mensajeError(request, e.getMessage());
            redirigirAOferta(request, response, ofertaId);
        }
    }

    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response,
                               Usuario usuario)
            throws IOException {

        if (usuario.getRol() != UsuarioRol.EMPRESA && usuario.getRol() != UsuarioRol.ADMINISTRADOR) {
            SesionUtil.mensajeError(request, "No tienes permisos para cambiar el estado.");
            SesionUtil.redirigirSegunRol(request, response, usuario.getRol());
            return;
        }

        Integer id = parsearEntero(request.getParameter("id"));
        String estado = request.getParameter("estado");

        try {
            postulacionService.actualizarEstado(usuario, id, estado);
            SesionUtil.mensajeExito(request, "Estado de la postulacion actualizado.");
            response.sendRedirect(request.getContextPath()
                    + "/postulaciones?accion=detalle&id=" + id);
        } catch (RegistroException e) {
            SesionUtil.mensajeError(request, e.getMessage());
            if (id != null) {
                response.sendRedirect(request.getContextPath()
                        + "/postulaciones?accion=detalle&id=" + id);
            } else {
                response.sendRedirect(request.getContextPath()
                        + "/postulaciones?accion=recibidas");
            }
        }
    }

    // ==================== Utilidades ====================

    private void redirigirAOferta(HttpServletRequest request, HttpServletResponse response,
                                  Integer ofertaId) throws IOException {
        if (ofertaId == null) {
            response.sendRedirect(request.getContextPath() + "/ofertas?format=jsp");
            return;
        }
        response.sendRedirect(request.getContextPath()
                + "/ofertas?format=jsp&accion=detalle&id=" + ofertaId + "&postular=1");
    }

    private String primerParam(HttpServletRequest request, String... nombres) {
        for (String nombre : nombres) {
            String valor = request.getParameter(nombre);
            if (valor != null && !valor.trim().isEmpty()) {
                return valor;
            }
        }
        return null;
    }

    private boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(texto);
    }

    private PerfilCandidato obtenerPerfilCandidato(Integer usuarioId) {
        try {
            return perfilCandidatoDAO.obtenerPorUsuarioId(usuarioId);
        } catch (RuntimeException e) {
            getServletContext().log("No se pudo cargar el perfil del candidato " + usuarioId, e);
            return null;
        }
    }

    private Integer parsearEntero(String valor) {
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
