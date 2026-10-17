package com.empleasv.controller;

import com.empleasv.model.PerfilEmpresa;
import com.empleasv.model.Usuario;
import com.empleasv.service.PerfilService;
import com.empleasv.service.RegistroException;
import com.empleasv.util.ArchivoNoValidoException;
import com.empleasv.util.SesionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador "Mi perfil empresarial" (rol EMPRESA).
 *
 * Permite crear el perfil si aún no existe (cuentas previas) y actualizar
 * los datos de la organización, incluido el logotipo.
 * Protegido por AuthFilter (solo rol EMPRESA).
 */
@WebServlet("/perfil-empresa")
@MultipartConfig(
        maxFileSize = 6L * 1024 * 1024,
        maxRequestSize = 14L * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024
)
public class PerfilEmpresaServlet extends HttpServlet {

    private PerfilService perfilService;

    @Override
    public void init() throws ServletException {
        perfilService = new PerfilService(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SesionUtil.consumirMensajes(request);

        Usuario usuario = SesionUtil.usuario(request);
        PerfilEmpresa perfil = perfilService.obtenerOCrearEmpresa(usuario.getId());

        boolean incompleto = "completar".equals(request.getParameter("accion"))
                || estaIncompleto(perfil);

        request.setAttribute("usuario", usuario);
        request.setAttribute("perfil", perfil);
        request.setAttribute("perfilIncompleto", incompleto);
        prepararValoresFormulario(request, usuario, perfil);

        request.getRequestDispatcher("/WEB-INF/views/mi-perfil-empresa.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "actualizar";
        }

        Usuario usuario = SesionUtil.usuario(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/auth?accion=login&restringido=1");
            return;
        }

        switch (accion) {
            case "actualizar":
            case "crear":
                actualizarDatos(request, response, usuario);
                break;
            case "subir-logo":
                subirLogo(request, response, usuario);
                break;
            case "eliminar-logo":
                eliminarLogo(request, response, usuario.getId());
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/perfil-empresa");
        }
    }

    // ==================== Acciones ====================

    private void actualizarDatos(HttpServletRequest request, HttpServletResponse response,
                                 Usuario usuario)
            throws ServletException, IOException {

        String nombreResponsable = param(request, "nombre_responsable");
        String email = param(request, "email");
        String nombreEmpresa = param(request, "nombre_empresa");
        String nit = param(request, "nit");
        String telefono = param(request, "telefono_empresa");
        String direccion = param(request, "direccion_empresa");
        String municipio = param(request, "municipio");
        String departamento = param(request, "departamento");
        String sector = param(request, "sector");
        String sitioWeb = param(request, "sitio_web");
        String descripcion = param(request, "descripcion");

        List<String> errores = new ArrayList<>();

        if (nombreResponsable == null || nombreResponsable.isEmpty()) {
            errores.add("El nombre de la persona responsable es obligatorio");
        }
        if (email == null || email.isEmpty()) {
            errores.add("El correo electronico es obligatorio");
        } else if (!com.empleasv.util.ValidacionUtil.esEmailValido(email)) {
            errores.add("El formato del correo electronico no es valido");
        }
        if (nombreEmpresa == null || nombreEmpresa.isEmpty()) {
            errores.add("El nombre legal o comercial de la empresa es obligatorio");
        }

        if (errores.isEmpty()) {
            try {
                PerfilEmpresa perfil = perfilService.actualizarEmpresa(
                        usuario.getId(), nombreResponsable, email, nombreEmpresa, nit,
                        telefono, direccion, municipio, departamento, sector,
                        sitioWeb, descripcion);

                Integer empresaId = perfilService.asegurarEmpresaVinculada(usuario.getId());
                SesionUtil.fijarEmpresaEnSesion(request, empresaId);

                // Refresca los datos visibles de la sesión (nombre y correo)
                Usuario actualizado = new com.empleasv.dao.UsuarioDAO()
                        .obtenerPorId(usuario.getId());
                if (actualizado != null) {
                    SesionUtil.actualizarSesion(request, actualizado);
                }

                SesionUtil.mensajeExito(request,
                        "Perfil empresarial actualizado correctamente.");
                response.sendRedirect(request.getContextPath() + "/perfil-empresa");
                return;
            } catch (RegistroException e) {
                errores.addAll(e.getErrores());
            }
        }

        PerfilEmpresa perfil = perfilService.obtenerOCrearEmpresa(usuario.getId());
        request.setAttribute("usuario", usuario);
        request.setAttribute("perfil", perfil);
        request.setAttribute("perfilIncompleto", estaIncompleto(perfil));
        request.setAttribute("errores", errores);
        request.setAttribute("formNombreResponsable", nombreResponsable);
        request.setAttribute("formEmail", email);
        request.setAttribute("formNombreEmpresa", nombreEmpresa);
        request.setAttribute("formNit", nit);
        request.setAttribute("formTelefono", telefono);
        request.setAttribute("formDireccion", direccion);
        request.setAttribute("formMunicipio", municipio);
        request.setAttribute("formDepartamento", departamento);
        request.setAttribute("formSector", sector);
        request.setAttribute("formSitioWeb", sitioWeb);
        request.setAttribute("formDescripcion", descripcion);

        request.getRequestDispatcher("/WEB-INF/views/mi-perfil-empresa.jsp")
                .forward(request, response);
    }

    private void subirLogo(HttpServletRequest request, HttpServletResponse response,
                           Usuario usuario) throws IOException {
        try {
            Part parte = request.getPart("logo");
            if (parte == null || parte.getSize() <= 0
                    || parte.getSubmittedFileName() == null
                    || parte.getSubmittedFileName().trim().isEmpty()) {
                SesionUtil.mensajeError(request, "Debes seleccionar un logotipo.");
            } else {
                perfilService.subirLogo(usuario, parte);
                SesionUtil.mensajeExito(request, "Logotipo actualizado correctamente.");
            }
        } catch (ArchivoNoValidoException | RegistroException e) {
            SesionUtil.mensajeError(request, e.getMessage());
        } catch (ServletException | IOException e) {
            SesionUtil.mensajeError(request, "No se pudo procesar el archivo.");
        }
        response.sendRedirect(request.getContextPath() + "/perfil-empresa");
    }

    private void eliminarLogo(HttpServletRequest request, HttpServletResponse response,
                              Integer usuarioId) throws IOException {
        try {
            perfilService.eliminarLogo(usuarioId);
            SesionUtil.mensajeExito(request, "Logotipo eliminado.");
        } catch (RegistroException e) {
            SesionUtil.mensajeError(request, e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/perfil-empresa");
    }

    // ==================== Auxiliares ====================

    /** Indica si faltan datos obligatorios antes de poder publicar ofertas. */
    public static boolean estaIncompleto(PerfilEmpresa perfil) {
        if (perfil == null) {
            return true;
        }
        return vacio(perfil.getNombreResponsable())
                || vacio(perfil.getNombreEmpresa())
                || "Empresa sin nombre".equals(perfil.getNombreEmpresa())
                || vacio(perfil.getSector())
                || vacio(perfil.getDireccionEmpresa())
                || vacio(perfil.getTelefonoEmpresa());
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void prepararValoresFormulario(HttpServletRequest request,
                                           Usuario usuario,
                                           PerfilEmpresa perfil) {
        if (request.getAttribute("formNombreResponsable") == null) {
            request.setAttribute("formNombreResponsable",
                    perfil != null && !vacio(perfil.getNombreResponsable())
                            ? perfil.getNombreResponsable() : usuario.getNombre());
        }
        if (request.getAttribute("formEmail") == null) {
            request.setAttribute("formEmail", usuario.getEmail());
        }
        if (request.getAttribute("formNombreEmpresa") == null) {
            request.setAttribute("formNombreEmpresa",
                    perfil != null ? perfil.getNombreEmpresa() : null);
        }
        if (request.getAttribute("formNit") == null) {
            request.setAttribute("formNit", perfil != null ? perfil.getNit() : null);
        }
        if (request.getAttribute("formTelefono") == null) {
            request.setAttribute("formTelefono",
                    perfil != null ? perfil.getTelefonoEmpresa() : null);
        }
        if (request.getAttribute("formDireccion") == null) {
            request.setAttribute("formDireccion",
                    perfil != null ? perfil.getDireccionEmpresa() : null);
        }
        if (request.getAttribute("formMunicipio") == null) {
            request.setAttribute("formMunicipio", perfil != null ? perfil.getMunicipio() : null);
        }
        if (request.getAttribute("formDepartamento") == null) {
            request.setAttribute("formDepartamento",
                    perfil != null ? perfil.getDepartamento() : null);
        }
        if (request.getAttribute("formSector") == null) {
            request.setAttribute("formSector", perfil != null ? perfil.getSector() : null);
        }
        if (request.getAttribute("formSitioWeb") == null) {
            request.setAttribute("formSitioWeb", perfil != null ? perfil.getSitioWeb() : null);
        }
        if (request.getAttribute("formDescripcion") == null) {
            request.setAttribute("formDescripcion",
                    perfil != null ? perfil.getDescripcion() : null);
        }
    }

    private String param(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor != null ? valor.trim() : null;
    }
}
