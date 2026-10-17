package com.empleasv.controller;

import com.empleasv.model.Archivo;
import com.empleasv.model.PerfilCandidato;
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
 * Controlador "Mi perfil" del candidato (rol USUARIO).
 *
 * Permite consultar y editar los datos personales, así como subir,
 * reemplazar o eliminar la fotografía de perfil y el currículum.
 * El acceso está protegido por AuthFilter (solo rol USUARIO).
 */
@WebServlet("/perfil")
@MultipartConfig(
        maxFileSize = 6L * 1024 * 1024,
        maxRequestSize = 14L * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024
)
public class PerfilServlet extends HttpServlet {

    private PerfilService perfilService;

    @Override
    public void init() throws ServletException {
        perfilService = new PerfilService(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "ver";
        }

        SesionUtil.consumirMensajes(request);

        Usuario usuario = SesionUtil.usuario(request);
        PerfilCandidato perfil = perfilService.obtenerOCrearCandidato(usuario.getId());

        request.setAttribute("usuario", usuario);
        request.setAttribute("perfil", perfil);
        prepararValoresFormulario(request, usuario, perfil);

        if ("curriculum".equals(accion)) {
            request.setAttribute("curriculums",
                    perfilService.listarCurriculums(usuario.getId()));
            request.getRequestDispatcher("/WEB-INF/views/mi-curriculum.jsp")
                    .forward(request, response);
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/mi-perfil.jsp")
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
                actualizarDatos(request, response, usuario);
                break;
            case "subir-foto":
                subirArchivo(request, response, usuario, Archivo.CategoriaArchivo.FOTO_PERFIL);
                break;
            case "eliminar-foto":
                eliminarArchivo(request, response, usuario.getId(),
                        Archivo.CategoriaArchivo.FOTO_PERFIL, "/perfil");
                break;
            case "subir-curriculum":
                subirArchivo(request, response, usuario, Archivo.CategoriaArchivo.CURRICULUM);
                break;
            case "agregar-curriculum":
                subirArchivo(request, response, usuario, Archivo.CategoriaArchivo.CURRICULUM);
                break;
            case "curriculum-principal":
                fijarCurriculumPrincipal(request, response, usuario);
                break;
            case "eliminar-curriculum-archivo":
                eliminarCurriculumEspecifico(request, response, usuario);
                break;
            case "eliminar-curriculum":
                eliminarArchivo(request, response, usuario.getId(),
                        Archivo.CategoriaArchivo.CURRICULUM, "/perfil?accion=curriculum");
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/perfil");
        }
    }

    // ==================== Acciones ====================

    private void actualizarDatos(HttpServletRequest request, HttpServletResponse response,
                                 Usuario usuario)
            throws ServletException, IOException {

        String nombre = param(request, "nombre");
        String email = param(request, "email");
        String telefono = param(request, "telefono");
        String direccion = param(request, "direccion");
        String municipio = param(request, "municipio");
        String departamento = param(request, "departamento");

        List<String> errores = new ArrayList<>();

        if (nombre == null || nombre.isEmpty()) {
            errores.add("El nombre es obligatorio");
        } else if (nombre.length() > 150) {
            errores.add("El nombre no debe exceder 150 caracteres");
        }
        if (email == null || email.isEmpty()) {
            errores.add("El correo electronico es obligatorio");
        } else if (!com.empleasv.util.ValidacionUtil.esEmailValido(email)) {
            errores.add("El formato del correo electronico no es valido");
        }

        if (errores.isEmpty()) {
            try {
                Usuario actualizado = perfilService.actualizarCandidato(
                        usuario.getId(), nombre, email, telefono,
                        direccion, municipio, departamento);
                SesionUtil.actualizarSesion(request, actualizado);
                SesionUtil.mensajeExito(request,
                        "Perfil actualizado correctamente. Los cambios se mantienen al cerrar sesion.");
                response.sendRedirect(request.getContextPath() + "/perfil");
                return;
            } catch (RegistroException e) {
                errores.addAll(e.getErrores());
            }
        }

        // Reenvío con los valores capturados para no perder la información
        Usuario temporal = new Usuario();
        temporal.setId(usuario.getId());
        temporal.setNombre(nombre);
        temporal.setEmail(email);
        temporal.setRol(usuario.getRol());
        temporal.setEstado(usuario.getEstado());

        PerfilCandidato perfil = perfilService.obtenerOCrearCandidato(usuario.getId());
        request.setAttribute("usuario", temporal);
        request.setAttribute("perfil", perfil);
        request.setAttribute("errores", errores);
        request.setAttribute("formNombre", nombre);
        request.setAttribute("formEmail", email);
        request.setAttribute("formTelefono", telefono);
        request.setAttribute("formDireccion", direccion);
        request.setAttribute("formMunicipio", municipio);
        request.setAttribute("formDepartamento", departamento);

        request.getRequestDispatcher("/WEB-INF/views/mi-perfil.jsp")
                .forward(request, response);
    }

    private void subirArchivo(HttpServletRequest request, HttpServletResponse response,
                              Usuario usuario, Archivo.CategoriaArchivo categoria)
            throws ServletException, IOException {

        String destino = categoria == Archivo.CategoriaArchivo.CURRICULUM
                ? "/perfil?accion=curriculum" : "/perfil";

        try {
            Part parte = request.getPart(categoria == Archivo.CategoriaArchivo.CURRICULUM
                    ? "curriculum" : "foto");
            if (parte == null || parte.getSize() <= 0
                    || parte.getSubmittedFileName() == null
                    || parte.getSubmittedFileName().trim().isEmpty()) {
                SesionUtil.mensajeError(request, "Debes seleccionar un archivo.");
            } else {
                perfilService.subirSegunCategoria(usuario, categoria, parte);
                SesionUtil.mensajeExito(request,
                        categoria == Archivo.CategoriaArchivo.CURRICULUM
                                ? "Curriculum subido correctamente."
                                : "Fotografia de perfil actualizada.");
            }
        } catch (ArchivoNoValidoException e) {
            SesionUtil.mensajeError(request, e.getMessage());
        } catch (RegistroException e) {
            SesionUtil.mensajeError(request, e.getMessage());
        } catch (ServletException | IOException e) {
            SesionUtil.mensajeError(request, "No se pudo procesar el archivo.");
        }

        response.sendRedirect(request.getContextPath() + destino);
    }

    private void eliminarArchivo(HttpServletRequest request, HttpServletResponse response,
                                 Integer usuarioId, Archivo.CategoriaArchivo categoria,
                                 String destino) throws IOException {
        try {
            if (categoria == Archivo.CategoriaArchivo.CURRICULUM) {
                perfilService.eliminarCurriculum(usuarioId);
            } else {
                perfilService.eliminarFoto(usuarioId);
            }
            SesionUtil.mensajeExito(request, "Archivo eliminado correctamente.");
        } catch (RegistroException e) {
            SesionUtil.mensajeError(request, e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + destino);
    }

    private void fijarCurriculumPrincipal(HttpServletRequest request, HttpServletResponse response,
                                          Usuario usuario) throws IOException {
        String destino = request.getContextPath() + "/perfil?accion=curriculum";
        Integer archivoId = parsearId(request.getParameter("archivoId"));
        try {
            if (archivoId == null) {
                throw new RegistroException("Curriculum no valido.");
            }
            perfilService.fijarCurriculumPrincipal(usuario.getId(), archivoId);
            SesionUtil.mensajeExito(request, "El curriculum seleccionado ahora es el principal.");
        } catch (RegistroException e) {
            SesionUtil.mensajeError(request, e.getMessage());
        }
        response.sendRedirect(destino);
    }

    private void eliminarCurriculumEspecifico(HttpServletRequest request, HttpServletResponse response,
                                              Usuario usuario) throws IOException {
        String destino = request.getContextPath() + "/perfil?accion=curriculum";
        Integer archivoId = parsearId(request.getParameter("archivoId"));
        try {
            if (archivoId == null) {
                throw new RegistroException("Curriculum no valido.");
            }
            perfilService.eliminarCurriculumArchivo(usuario.getId(), archivoId);
            SesionUtil.mensajeExito(request, "Curriculum eliminado correctamente.");
        } catch (RegistroException e) {
            SesionUtil.mensajeError(request, e.getMessage());
        }
        response.sendRedirect(destino);
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

    // ==================== Auxiliares ====================

    private void prepararValoresFormulario(HttpServletRequest request,
                                           Usuario usuario,
                                           PerfilCandidato perfil) {
        if (request.getAttribute("formNombre") == null) {
            request.setAttribute("formNombre", usuario.getNombre());
        }
        if (request.getAttribute("formEmail") == null) {
            request.setAttribute("formEmail", usuario.getEmail());
        }
        if (request.getAttribute("formTelefono") == null) {
            request.setAttribute("formTelefono",
                    perfil != null ? perfil.getTelefono() : null);
        }
        if (request.getAttribute("formDireccion") == null) {
            request.setAttribute("formDireccion",
                    perfil != null ? perfil.getDireccionResidencia() : null);
        }
        if (request.getAttribute("formMunicipio") == null) {
            request.setAttribute("formMunicipio", perfil != null ? perfil.getMunicipio() : null);
        }
        if (request.getAttribute("formDepartamento") == null) {
            request.setAttribute("formDepartamento",
                    perfil != null ? perfil.getDepartamento() : null);
        }
    }

    private String param(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor != null ? valor.trim() : null;
    }
}
