package com.empleasv.controller;

import com.empleasv.dao.UsuarioDAO;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.service.PerfilService;
import com.empleasv.service.RegistroException;
import com.empleasv.service.RegistroService;
import com.empleasv.util.ArchivoNoValidoException;
import com.empleasv.util.PasswordUtil;
import com.empleasv.util.SesionUtil;
import com.empleasv.util.ValidacionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet de autenticación: login, registro dinámico (candidato / empresa) y logout.
 *
 * El registro acepta dos tipos de cuenta claramente separados:
 *  - USUARIO  (candidato): datos de contacto personal, foto y CV opcionales.
 *  - EMPRESA  (empresa):    datos de la persona responsable y de la organización.
 *
 * Todas las validaciones se realizan también en el servidor.
 */
@WebServlet("/auth")
@MultipartConfig(
        maxFileSize = 6L * 1024 * 1024,
        maxRequestSize = 14L * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024
)
public class AuthServlet extends HttpServlet {

    private UsuarioDAO usuarioDAO;
    private RegistroService registroService;
    private PerfilService perfilService;

    @Override
    public void init() throws ServletException {
        usuarioDAO = new UsuarioDAO();
        registroService = new RegistroService(getServletContext());
        perfilService = new PerfilService(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "login";
        }

        switch (accion) {
            case "login":
                mostrarLogin(request, response);
                break;
            case "registro":
                mostrarRegistro(request, response);
                break;
            case "logout":
                procesarLogout(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/auth?accion=login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if (accion == null) {
            response.sendRedirect(request.getContextPath() + "/auth?accion=login");
            return;
        }

        switch (accion) {
            case "login":
                procesarLogin(request, response);
                break;
            case "registro":
                procesarRegistro(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/auth?accion=login");
        }
    }

    // ==================== GET: Mostrar formularios ====================

    private void mostrarLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SesionUtil.autenticado(request)) {
            SesionUtil.redirigirSegunRol(request, response,
                    SesionUtil.rol(request));
            return;
        }

        SesionUtil.consumirMensajes(request);

        if (request.getAttribute("mensajeError") == null) {
            if (request.getParameter("restringido") != null) {
                request.setAttribute("mensajeError",
                        "Debes iniciar sesion para acceder a esa seccion.");
            } else if ("inactiva".equals(request.getParameter("cuenta"))) {
                request.setAttribute("mensajeError",
                        "Tu cuenta esta desactivada. Contacta al administrador.");
            } else if (request.getParameter("error") != null) {
                request.setAttribute("mensajeError", "Correo o contrasena incorrectos");
            }
        }

        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    private void mostrarRegistro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SesionUtil.autenticado(request)) {
            SesionUtil.redirigirSegunRol(request, response, SesionUtil.rol(request));
            return;
        }

        SesionUtil.consumirMensajes(request);
        request.getRequestDispatcher("/registro.jsp").forward(request, response);
    }

    // ==================== POST: Procesar formularios ====================

    private void procesarLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = obtenerParametro(request, "email");
        String password = obtenerParametro(request, "password");

        List<String> errores = new ArrayList<>();

        if (email == null || email.isEmpty()) {
            errores.add("El correo electronico es obligatorio");
        } else if (!ValidacionUtil.esEmailValido(email)) {
            errores.add("El formato del correo electronico no es valido");
        }

        if (password == null || password.isEmpty()) {
            errores.add("La contrasena es obligatoria");
        }

        if (!errores.isEmpty()) {
            request.setAttribute("errores", errores);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        Usuario usuario = usuarioDAO.obtenerPorEmail(email);

        if (usuario == null || !PasswordUtil.verifyPassword(password, usuario.getPasswordHash())) {
            response.sendRedirect(request.getContextPath() + "/auth?accion=login&error=1");
            return;
        }

        if (!"ACTIVO".equals(usuario.getEstado())) {
            request.setAttribute("mensajeError",
                    "La cuenta esta desactivada. Contacte al administrador.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        Integer empresaId = null;
        if (usuario.getRol() == UsuarioRol.EMPRESA) {
            empresaId = perfilService.obtenerEmpresaId(usuario.getId());
        }

        SesionUtil.iniciarSesion(request, usuario, empresaId);
        SesionUtil.redirigirSegunRol(request, response, usuario.getRol());
    }

    private void procesarRegistro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String rolStr = obtenerParametro(request, "rol");

        UsuarioRol rol = null;
        List<String> errores = new ArrayList<>();
        Map<String, String> erroresCampo = new LinkedHashMap<>();

        if ("EMPRESA".equals(rolStr)) {
            rol = UsuarioRol.EMPRESA;
        } else if ("USUARIO".equals(rolStr)) {
            rol = UsuarioRol.USUARIO;
        } else {
            rechazar(erroresCampo, errores, null,
                    "Debes seleccionar el tipo de cuenta: candidato o empresa");
        }

        if (rol == UsuarioRol.USUARIO) {
            validarCandidato(request, errores, erroresCampo);
        } else if (rol == UsuarioRol.EMPRESA) {
            validarEmpresa(request, errores, erroresCampo);
        }

        if (!errores.isEmpty()) {
            reponerValores(request);
            request.setAttribute("errores", errores);
            request.setAttribute("erroresCampo", erroresCampo);
            request.setAttribute("rol", rol != null ? rol.name() : rolStr);
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            return;
        }

        Part archivo = parteDe(request, rol == UsuarioRol.EMPRESA ? "logo" : "foto");

        try {
            if (rol == UsuarioRol.USUARIO) {
                registroService.registrarCandidato(
                        obtenerParametro(request, "nombre"),
                        obtenerParametro(request, "email"),
                        request.getParameter("password"),
                        obtenerParametro(request, "telefono"),
                        obtenerParametro(request, "direccion"),
                        obtenerParametro(request, "municipio"),
                        obtenerParametro(request, "departamento"),
                        archivo,
                        parteDe(request, "curriculum"));
            } else {
                registroService.registrarEmpresa(
                        obtenerParametro(request, "nombre_responsable"),
                        obtenerParametro(request, "email"),
                        request.getParameter("password"),
                        obtenerParametro(request, "nombre_empresa"),
                        obtenerParametro(request, "nit"),
                        obtenerParametro(request, "telefono_empresa"),
                        obtenerParametro(request, "direccion_empresa"),
                        obtenerParametro(request, "municipio"),
                        obtenerParametro(request, "departamento"),
                        obtenerParametro(request, "sector"),
                        obtenerParametro(request, "sitio_web"),
                        obtenerParametro(request, "descripcion"),
                        archivo);
            }
        } catch (RegistroException | ArchivoNoValidoException e) {
            List<String> fallas = new ArrayList<>(errores);
            if (e instanceof RegistroException) {
                fallas.addAll(((RegistroException) e).getErrores());
            } else {
                fallas.add(e.getMessage());
            }
            reponerValores(request);
            request.setAttribute("errores", fallas);
            request.setAttribute("erroresCampo", erroresCampo);
            request.setAttribute("rol", rol.name());
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/auth?accion=login&registro=exitoso");
    }

    /**
     * Devuelve al formulario de registro los valores ya enviados por el
     * usuario para que no tenga que volver a escribirlos tras un error.
     * Nunca se repone la contraseña.
     */
    private void reponerValores(HttpServletRequest request) {
        String[] campos = {
                "nombre", "email", "telefono", "direccion", "municipio",
                "departamento", "nombre_responsable", "nombre_empresa", "nit",
                "telefono_empresa", "direccion_empresa", "sector", "sitio_web",
                "descripcion"
        };
        for (String campo : campos) {
            request.setAttribute(campo, obtenerParametro(request, campo));
        }
    }

    private void procesarLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        SesionUtil.cerrarSesion(request);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.sendRedirect(request.getContextPath() + "/inicio");
    }

    // ==================== Validaciones (lado del servidor) ====================

    private void validarCandidato(HttpServletRequest request, List<String> errores,
                                  Map<String, String> erroresCampo) {
        String nombre = obtenerParametro(request, "nombre");
        String email = obtenerParametro(request, "email");
        String password = request.getParameter("password");
        String password2 = request.getParameter("password2");
        String telefono = obtenerParametro(request, "telefono");
        String direccion = obtenerParametro(request, "direccion");

        validarNombreComun(nombre, "nombre", errores, erroresCampo);
        validarEmailComun(email, errores, erroresCampo);
        validarPasswordComun(password, password2, errores, erroresCampo);

        if (vacio(telefono)) {
            rechazar(erroresCampo, errores, "telefono", "El numero de telefono es obligatorio");
        } else if (telefono.length() > 30) {
            rechazar(erroresCampo, errores, "telefono", "El telefono no debe exceder 30 caracteres");
        }

        if (vacio(direccion)) {
            rechazar(erroresCampo, errores, "direccion", "La direccion de residencia es obligatoria");
        } else if (direccion.length() > 255) {
            rechazar(erroresCampo, errores, "direccion", "La direccion no debe exceder 255 caracteres");
        }

        validarLongitudOpcional(obtenerParametro(request, "municipio"), 100, "municipio",
                "El municipio no debe exceder 100 caracteres", errores, erroresCampo);
        validarLongitudOpcional(obtenerParametro(request, "departamento"), 100, "departamento",
                "El departamento no debe exceder 100 caracteres", errores, erroresCampo);
    }

    private void validarEmpresa(HttpServletRequest request, List<String> errores,
                                Map<String, String> erroresCampo) {
        String nombreResponsable = obtenerParametro(request, "nombre_responsable");
        String email = obtenerParametro(request, "email");
        String password = request.getParameter("password");
        String password2 = request.getParameter("password2");
        String nombreEmpresa = obtenerParametro(request, "nombre_empresa");
        String telefono = obtenerParametro(request, "telefono_empresa");
        String direccion = obtenerParametro(request, "direccion_empresa");
        String municipio = obtenerParametro(request, "municipio");
        String departamento = obtenerParametro(request, "departamento");
        String sector = obtenerParametro(request, "sector");

        validarNombreComun(nombreResponsable, "nombre_responsable", errores, erroresCampo);
        validarEmailComun(email, errores, erroresCampo);
        validarPasswordComun(password, password2, errores, erroresCampo);

        if (vacio(nombreEmpresa)) {
            rechazar(erroresCampo, errores, "nombre_empresa",
                    "El nombre legal o comercial de la empresa es obligatorio");
        } else if (nombreEmpresa.length() > 150) {
            rechazar(erroresCampo, errores, "nombre_empresa",
                    "El nombre de la empresa no debe exceder 150 caracteres");
        }

        validarLongitudOpcional(obtenerParametro(request, "nit"), 50, "nit",
                "El NIT no debe exceder 50 caracteres", errores, erroresCampo);

        if (vacio(telefono)) {
            rechazar(erroresCampo, errores, "telefono_empresa", "El telefono empresarial es obligatorio");
        } else if (telefono.length() > 30) {
            rechazar(erroresCampo, errores, "telefono_empresa",
                    "El telefono empresarial no debe exceder 30 caracteres");
        }

        if (vacio(direccion)) {
            rechazar(erroresCampo, errores, "direccion_empresa", "La direccion empresarial es obligatoria");
        } else if (direccion.length() > 255) {
            rechazar(erroresCampo, errores, "direccion_empresa",
                    "La direccion empresarial no debe exceder 255 caracteres");
        }

        if (vacio(municipio)) {
            rechazar(erroresCampo, errores, "municipio", "El municipio es obligatorio");
        } else if (municipio.length() > 100) {
            rechazar(erroresCampo, errores, "municipio", "El municipio no debe exceder 100 caracteres");
        }
        if (vacio(departamento)) {
            rechazar(erroresCampo, errores, "departamento", "El departamento es obligatorio");
        } else if (departamento.length() > 100) {
            rechazar(erroresCampo, errores, "departamento", "El departamento no debe exceder 100 caracteres");
        }
        if (vacio(sector)) {
            rechazar(erroresCampo, errores, "sector", "El sector o industria es obligatorio");
        } else if (sector.length() > 100) {
            rechazar(erroresCampo, errores, "sector", "El sector no debe exceder 100 caracteres");
        }

        validarLongitudOpcional(obtenerParametro(request, "sitio_web"), 255, "sitio_web",
                "El sitio web no debe exceder 255 caracteres", errores, erroresCampo);
        validarLongitudOpcional(obtenerParametro(request, "descripcion"), 5000, "descripcion",
                "La descripcion es demasiado larga", errores, erroresCampo);
    }

    private void validarNombreComun(String nombre, String campo,
                                    List<String> errores, Map<String, String> erroresCampo) {
        if (vacio(nombre)) {
            rechazar(erroresCampo, errores, campo, "El nombre completo es obligatorio");
        } else if (nombre.length() > 150) {
            rechazar(erroresCampo, errores, campo, "El nombre no debe exceder 150 caracteres");
        }
    }

    private void validarEmailComun(String email, List<String> errores,
                                   Map<String, String> erroresCampo) {
        if (vacio(email)) {
            rechazar(erroresCampo, errores, "email", "El correo electronico es obligatorio");
        } else if (!ValidacionUtil.esEmailValido(email)) {
            rechazar(erroresCampo, errores, "email", "El formato del correo electronico no es valido");
        }
    }

    private void validarPasswordComun(String password, String password2, List<String> errores,
                                      Map<String, String> erroresCampo) {
        if (password == null || password.isEmpty()) {
            rechazar(erroresCampo, errores, "password", "La contrasena es obligatoria");
        } else if (password.length() < 6) {
            rechazar(erroresCampo, errores, "password", "La contrasena debe tener al menos 6 caracteres");
        }

        if (password2 == null || password2.isEmpty()) {
            rechazar(erroresCampo, errores, "password2", "La confirmacion de contrasena es obligatoria");
        } else if (password != null && !password.equals(password2)) {
            rechazar(erroresCampo, errores, "password2", "Las contrasenas no coinciden");
        }
    }

    private void validarLongitudOpcional(String valor, int maximo, String campo, String mensaje,
                                         List<String> errores, Map<String, String> erroresCampo) {
        if (valor != null && valor.length() > maximo) {
            rechazar(erroresCampo, errores, campo, mensaje);
        }
    }

    /**
     * Registra un error tanto en la lista general como, cuando se conoce el
     * campo, en el mapa que permite mostrarlo junto al input correspondiente.
     */
    private void rechazar(Map<String, String> erroresCampo, List<String> errores,
                          String campo, String mensaje) {
        errores.add(mensaje);
        if (campo != null && !erroresCampo.containsKey(campo)) {
            erroresCampo.put(campo, mensaje);
        }
    }

    // ==================== Métodos auxiliares ====================

    private String obtenerParametro(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor != null ? valor.trim() : null;
    }

    private boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    /**
     * Obtiene una parte multipart de forma segura (null si no fue enviada).
     * Ignora partes sin nombre o sin contenido.
     */
    private Part parteDe(HttpServletRequest request, String nombre) {
        try {
            Part parte = request.getPart(nombre);
            if (parte == null || parte.getSize() <= 0) {
                return null;
            }
            String fileName = parte.getSubmittedFileName();
            if (fileName == null || fileName.trim().isEmpty()) {
                return null;
            }
            return parte;
        } catch (IOException | ServletException e) {
            return null;
        }
    }
}
