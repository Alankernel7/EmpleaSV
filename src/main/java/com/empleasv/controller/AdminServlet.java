package com.empleasv.controller;

import com.empleasv.model.OfertaEmpleo;
import com.empleasv.model.PerfilEmpresa;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.service.AdminService;
import com.empleasv.util.SesionUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Panel administrativo de EmpleaSV.
 *
 * Secciones: dashboard, usuarios, empresas, ofertas.
 * Acciones POST: activar/desactivar cuentas y cambiar estado de ofertas.
 * Toda operación queda registrada en auditoría (sin datos sensibles).
 * Protegido por AuthFilter (rol ADMINISTRADOR) además de esta clase.
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/admin"})
public class AdminServlet extends HttpServlet {

    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SesionUtil.consumirMensajes(request);

        if (!esAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/inicio");
            return;
        }

        String seccion = parametro(request, "seccion", "dashboard");

        switch (seccion) {
            case "usuarios":
                mostrarUsuarios(request, response);
                break;
            case "empresas":
                mostrarEmpresas(request, response);
                break;
            case "ofertas":
                mostrarOfertas(request, response);
                break;
            case "dashboard":
            default:
                mostrarDashboard(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SesionUtil.consumirMensajes(request);

        if (!esAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/inicio");
            return;
        }

        Integer adminId = SesionUtil.usuarioId(request);
        String accion = parametro(request, "accion", "");
        String seccion = parametro(request, "seccion", "dashboard");
        Integer id = entero(request.getParameter("id"));

        try {
            switch (accion) {
                case "cambiar-estado-usuario": {
                    String nuevoEstado = parametro(request, "estado", "");
                    String mensaje = adminService.cambiarEstadoUsuario(adminId, id, nuevoEstado);
                    SesionUtil.mensajeExito(request, mensaje);
                    break;
                }
                case "cambiar-estado-oferta": {
                    String nuevoEstado = parametro(request, "estado", "");
                    String mensaje = adminService.cambiarEstadoOferta(adminId, id, nuevoEstado);
                    SesionUtil.mensajeExito(request, mensaje);
                    break;
                }
                default:
                    SesionUtil.mensajeError(request, "Accion no reconocida.");
                    break;
            }
        } catch (AdminService.AdminException e) {
            SesionUtil.mensajeError(request, e.getMessage());
        }

        StringBuilder destino = new StringBuilder(request.getContextPath() + "/admin?seccion=");
        destino.append(seccionSanitizada(seccion));
        if ("usuarios".equals(seccionSanitizada(seccion)) && id != null
                && "cambiar-estado-usuario".equals(accion)) {
            // Vuelve al detalle para que se vea el nuevo estado
            destino.append("&detalle=").append(id);
        } else if ("ofertas".equals(seccionSanitizada(seccion)) && id != null
                && "cambiar-estado-oferta".equals(accion)) {
            destino.append("&detalle=").append(id);
        }
        response.sendRedirect(destino.toString());
    }

    // =========================================================
    // Secciones (GET)
    // =========================================================

    private void mostrarDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("seccionActual", "dashboard");
        request.setAttribute("totalUsuarios", adminService.totalUsuarios());
        request.setAttribute("candidatosActivos", adminService.candidatosActivos());
        request.setAttribute("empresasActivas", adminService.empresasActivas());
        request.setAttribute("administradoresActivos", adminService.administradoresActivos());
        request.setAttribute("totalEmpresas", adminService.listarEmpresasConOfertas().size());
        request.setAttribute("ofertasActivas", adminService.contarOfertas("ACTIVA"));
        request.setAttribute("ofertasPausadas", adminService.contarOfertas("PAUSADA"));
        request.setAttribute("ofertasCerradas", adminService.contarOfertas("CERRADA"));
        request.setAttribute("totalOfertas", adminService.contarOfertas(null));
        request.setAttribute("usuariosRecientes", adminService.listarUsuariosRecientes(5));
        request.setAttribute("ofertasRecientes", adminService.listarOfertasRecientes(5));
        request.setAttribute("auditoriaReciente", adminService.listarAuditoriaReciente(8));
        reenviar(request, response, "admin/dashboard.jsp");
    }

    private void mostrarUsuarios(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("seccionActual", "usuarios");
        String q = parametro(request, "q", "");
        String rol = parametro(request, "rol", "");
        String estado = parametro(request, "estado", "");
        Integer detalleId = entero(request.getParameter("detalle"));

        if (detalleId != null) {
            Usuario usuario = adminService.obtenerUsuario(detalleId);
            if (usuario == null) {
                SesionUtil.mensajeError(request, "El usuario indicado no existe.");
                response.sendRedirect(request.getContextPath() + "/admin?seccion=usuarios");
                return;
            }
            request.setAttribute("usuarioDetalle", usuario);
            if (usuario.getRol() == UsuarioRol.EMPRESA) {
                PerfilEmpresa perfil = adminService.obtenerPerfilEmpresa(usuario.getId());
                request.setAttribute("perfilEmpresaDetalle", perfil);
                if (perfil != null && perfil.getEmpresa() != null) {
                    request.setAttribute("ofertasEmpresa",
                            adminService.listarOfertasEmpresa(perfil.getEmpresa().getId()));
                }
            }
            reenviar(request, response, "admin/usuario-detalle.jsp");
            return;
        }

        List<Usuario> usuarios = adminService.buscarUsuarios(q, rol, estado);
        request.setAttribute("usuarios", usuarios);
        request.setAttribute("filtroQ", q);
        request.setAttribute("filtroRol", rol);
        request.setAttribute("filtroEstado", estado);
        reenviar(request, response, "admin/usuarios.jsp");
    }

    private void mostrarEmpresas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("seccionActual", "empresas");
        Integer detalleId = entero(request.getParameter("detalle"));

        if (detalleId != null) {
            Usuario usuario = adminService.obtenerUsuario(detalleId);
            if (usuario == null) {
                SesionUtil.mensajeError(request, "La cuenta indicada no existe.");
                response.sendRedirect(request.getContextPath() + "/admin?seccion=empresas");
                return;
            }
            PerfilEmpresa perfil = adminService.obtenerPerfilEmpresa(usuario.getId());
            request.setAttribute("cuentaDetalle", usuario);
            request.setAttribute("perfilEmpresaDetalle", perfil);
            if (perfil != null && perfil.getEmpresa() != null) {
                request.setAttribute("empresaDetalle", perfil.getEmpresa());
                request.setAttribute("ofertasEmpresa",
                        adminService.listarOfertasEmpresa(perfil.getEmpresa().getId()));
            }
            reenviar(request, response, "admin/empresa-detalle.jsp");
            return;
        }

        List<Usuario> cuentasEmpresa = adminService.buscarUsuarios("", "EMPRESA", "");
        // Pre-carga de perfiles y contadores fuera de la sesion JPA (evita LazyInitializationException)
        List<java.util.Map<String, Object>> filas = new java.util.ArrayList<>();
        for (Usuario cuenta : cuentasEmpresa) {
            PerfilEmpresa perfil = adminService.obtenerPerfilEmpresa(cuenta.getId());
            java.util.Map<String, Object> fila = new java.util.HashMap<>();
            fila.put("cuenta", cuenta);
            fila.put("perfil", perfil);
            fila.put("nombreEmpresa", perfil == null ? null : perfil.getNombreEmpresa());
            int ofertas = 0;
            if (perfil != null && perfil.getEmpresa() != null) {
                ofertas = adminService.listarOfertasEmpresa(perfil.getEmpresa().getId()).size();
            }
            fila.put("ofertas", ofertas);
            filas.add(fila);
        }
        request.setAttribute("cuentasEmpresa", filas);
        reenviar(request, response, "admin/empresas.jsp");
    }

    private void mostrarOfertas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("seccionActual", "ofertas");
        String q = parametro(request, "q", "");
        String estado = parametro(request, "estado", "");
        Integer detalleId = entero(request.getParameter("detalle"));

        if (detalleId != null) {
            OfertaEmpleo oferta = adminService.obtenerOferta(detalleId);
            if (oferta == null) {
                SesionUtil.mensajeError(request, "La oferta indicada no existe.");
                response.sendRedirect(request.getContextPath() + "/admin?seccion=ofertas");
                return;
            }
            request.setAttribute("ofertaDetalle", oferta);
            request.setAttribute("empresaCuentaInactiva",
                    adminService.idsEmpresasCuentaInactiva().contains(oferta.getEmpresa().getId()));
            reenviar(request, response, "admin/oferta-detalle.jsp");
            return;
        }

        List<OfertaEmpleo> ofertas = adminService.buscarOfertas(q, estado);
        request.setAttribute("ofertas", ofertas);
        request.setAttribute("filtroQ", q);
        request.setAttribute("filtroEstado", estado);
        request.setAttribute("empresasCuentaInactiva", adminService.idsEmpresasCuentaInactiva());
        reenviar(request, response, "admin/ofertas.jsp");
    }

    // =========================================================
    // Utilidades
    // =========================================================

    private boolean esAdmin(HttpServletRequest request) {
        return SesionUtil.esRol(request, UsuarioRol.ADMINISTRADOR);
    }

    private void reenviar(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/WEB-INF/views/" + vista);
        dispatcher.forward(request, response);
    }

    private String parametro(HttpServletRequest request, String nombre, String respaldo) {
        String valor = request.getParameter(nombre);
        return (valor == null || valor.trim().isEmpty()) ? respaldo : valor.trim();
    }

    private String seccionSanitizada(String seccion) {
        if ("usuarios".equals(seccion) || "empresas".equals(seccion)
                || "ofertas".equals(seccion) || "dashboard".equals(seccion)) {
            return seccion;
        }
        return "dashboard";
    }

    private Integer entero(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
