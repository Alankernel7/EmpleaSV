package com.empleasv.util;

import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Utilidades centralizadas de sesión HTTP.
 * Todas las claves de sesión se definen aquí para evitar duplicación.
 */
public final class SesionUtil {

    /** Usuario autenticado (objeto Usuario sin passwordHash expuesto en vistas). */
    public static final String ATR_USUARIO = "usuarioLogueado";
    /** Nombre del rol en texto (USUARIO, EMPRESA, ADMINISTRADOR). */
    public static final String ATR_ROL = "rolUsuario";
    /** ID del usuario autenticado. */
    public static final String ATR_USUARIO_ID = "usuarioId";
    /** Nombre visible para el saludo del menú. */
    public static final String ATR_NOMBRE_USUARIO = "nombreUsuario";
    /** ID de la tabla empresa a la que pertenece la cuenta EMPRESA. */
    public static final String ATR_EMPRESA_ID = "empresaIdSesion";
    /** Mensajes flash de éxito (se consumen en la siguiente petición). */
    public static final String ATR_MENSAJE_EXITO = "mensajeExito";
    /** Mensajes flash de error. */
    public static final String ATR_MENSAJE_ERROR = "mensajeError";

    private SesionUtil() {
    }

    /** Devuelve el usuario autenticado o null. */
    public static Usuario usuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object valor = session.getAttribute(ATR_USUARIO);
        return (valor instanceof Usuario) ? (Usuario) valor : null;
    }

    /** Devuelve el ID del usuario autenticado o null. */
    public static Integer usuarioId(HttpServletRequest request) {
        Usuario usuario = usuario(request);
        if (usuario != null && usuario.getId() != null) {
            return usuario.getId();
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object valor = session.getAttribute(ATR_USUARIO_ID);
        return (valor instanceof Integer) ? (Integer) valor : null;
    }

    /** Devuelve el rol autenticado o null. */
    public static UsuarioRol rol(HttpServletRequest request) {
        Usuario usuario = usuario(request);
        return usuario != null ? usuario.getRol() : null;
    }

    /** Indica si el usuario autenticado tiene el rol indicado. */
    public static boolean esRol(HttpServletRequest request, UsuarioRol esperado) {
        UsuarioRol actual = rol(request);
        return actual != null && actual == esperado;
    }

    /** Indica si el usuario autenticado tiene sesión activa. */
    public static boolean autenticado(HttpServletRequest request) {
        return usuario(request) != null;
    }

    /**
     * Crea la sesión de autenticación. No almacena la contraseña en sesión.
     *
     * @param request  petición actual
     * @param usuario  usuario autenticado
     * @param empresaId ID de la empresa vinculada (solo rol EMPRESA, puede ser null)
     */
    public static void iniciarSesion(HttpServletRequest request, Usuario usuario, Integer empresaId) {
        HttpSession session = request.getSession(true);
        session.setAttribute(ATR_USUARIO, usuario);
        session.setAttribute(ATR_ROL, usuario.getRol() != null ? usuario.getRol().name() : null);
        session.setAttribute(ATR_USUARIO_ID, usuario.getId());
        session.setAttribute(ATR_NOMBRE_USUARIO, usuario.getNombre());
        if (empresaId != null) {
            session.setAttribute(ATR_EMPRESA_ID, empresaId);
        } else {
            session.removeAttribute(ATR_EMPRESA_ID);
        }
    }

    /**
     * Actualiza los datos visibles de sesión después de editar el perfil.
     */
    public static void actualizarSesion(HttpServletRequest request, Usuario usuario) {
        HttpSession session = request.getSession(false);
        if (session == null || usuario == null) {
            return;
        }
        session.setAttribute(ATR_USUARIO, usuario);
        session.setAttribute(ATR_NOMBRE_USUARIO, usuario.getNombre());
        session.setAttribute(ATR_USUARIO_ID, usuario.getId());
    }

    /**
     * Establece el ID de empresa en sesión (rol EMPRESA).
     */
    public static void fijarEmpresaEnSesion(HttpServletRequest request, Integer empresaId) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        if (empresaId != null) {
            session.setAttribute(ATR_EMPRESA_ID, empresaId);
        } else {
            session.removeAttribute(ATR_EMPRESA_ID);
        }
    }

    /**
     * Invalida la sesión completa (cierre de sesión).
     */
    public static void cerrarSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Redirige al panel correspondiente según el rol autenticado.
     */
    public static void redirigirSegunRol(HttpServletRequest request,
                                         HttpServletResponse response,
                                         UsuarioRol rol) throws IOException {
        String destino;
        if (rol == UsuarioRol.EMPRESA) {
            destino = "/panel-empresa";
        } else if (rol == UsuarioRol.ADMINISTRADOR) {
            destino = "/admin";
        } else {
            destino = "/inicio";
        }
        response.sendRedirect(request.getContextPath() + destino);
    }

    /** Guarda un mensaje de éxito que se mostrará en la siguiente petición. */
    public static void mensajeExito(HttpServletRequest request, String mensaje) {
        request.getSession(true).setAttribute(ATR_MENSAJE_EXITO, mensaje);
    }

    /** Guarda un mensaje de error que se mostrará en la siguiente petición. */
    public static void mensajeError(HttpServletRequest request, String mensaje) {
        request.getSession(true).setAttribute(ATR_MENSAJE_ERROR, mensaje);
    }

    /**
     * Transfiere a la petición los mensajes flash pendientes y los elimina
     * de la sesión para que solo se muestren una vez.
     */
    public static void consumirMensajes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object exito = session.getAttribute(ATR_MENSAJE_EXITO);
        if (exito != null) {
            request.setAttribute("mensajeExito", exito);
            session.removeAttribute(ATR_MENSAJE_EXITO);
        }
        Object error = session.getAttribute(ATR_MENSAJE_ERROR);
        if (error != null) {
            request.setAttribute("mensajeError", error);
            session.removeAttribute(ATR_MENSAJE_ERROR);
        }
    }
}
