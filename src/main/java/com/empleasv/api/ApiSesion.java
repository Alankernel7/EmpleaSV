package com.empleasv.api;

import com.empleasv.dao.PerfilEmpresaDAO;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.util.SesionUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Objects;

/**
 * Reglas de identidad y autorización para la API.
 *
 * Reutiliza la sesión HTTP existente (SesionUtil) y la relación
 * cuenta ↔ perfil_empresa ↔ empresa. Nunca toma el ID del usuario ni el
 * ID de la empresa desde el cuerpo de la solicitud para autorizar.
 */
public final class ApiSesion {

    private ApiSesion() {
    }

    public static Usuario usuario(HttpServletRequest request) {
        return SesionUtil.usuario(request);
    }

    public static boolean autenticado(HttpServletRequest request) {
        return SesionUtil.usuario(request) != null;
    }

    public static Usuario requerirAutenticado(HttpServletRequest request) {
        Usuario usuario = SesionUtil.usuario(request);
        if (usuario == null) {
            throw new ApiExcepcion(401, "NO_AUTENTICADO",
                    "Debes iniciar sesión para realizar esta operación.");
        }
        return usuario;
    }

    public static Usuario requerirRol(HttpServletRequest request, UsuarioRol... roles) {
        Usuario usuario = requerirAutenticado(request);
        for (UsuarioRol rol : roles) {
            if (usuario.getRol() == rol) {
                return usuario;
            }
        }
        throw new ApiExcepcion(403, "PROHIBIDO",
                "No tienes permisos para realizar esta operación.");
    }

    public static boolean esAdmin(HttpServletRequest request) {
        return SesionUtil.esRol(request, UsuarioRol.ADMINISTRADOR);
    }

    /** Permite al propietario del recurso o a un administrador. */
    public static void requerirPropietarioOAdmin(HttpServletRequest request,
                                                 Integer usuarioPropietarioId) {
        Usuario usuario = requerirAutenticado(request);
        if (usuario.getRol() == UsuarioRol.ADMINISTRADOR) {
            return;
        }
        if (!Objects.equals(usuario.getId(), usuarioPropietarioId)) {
            throw new ApiExcepcion(403, "PROHIBIDO",
                    "No tienes permisos para acceder a los datos de otro usuario.");
        }
    }

    /**
     * Resuelve la empresa vinculada a la cuenta EMPRESA autenticada.
     * Devuelve null para ADMINISTRADOR, que puede operar con cualquier empresa.
     */
    public static Integer empresaIdDeSesion(HttpServletRequest request) {
        Usuario usuario = requerirAutenticado(request);
        if (usuario.getRol() == UsuarioRol.ADMINISTRADOR) {
            return null;
        }
        if (usuario.getRol() != UsuarioRol.EMPRESA) {
            throw new ApiExcepcion(403, "PROHIBIDO",
                    "Esta operación requiere una cuenta de empresa.");
        }
        Integer empresaId = new PerfilEmpresaDAO().obtenerEmpresaIdPorUsuarioId(usuario.getId());
        if (empresaId == null) {
            throw new ApiExcepcion(409, "PERFIL_EMPRESA_INCOMPLETO",
                    "La cuenta no tiene una empresa vinculada. Completa el perfil empresarial.");
        }
        return empresaId;
    }

    /** Exige que la oferta pertenezca a la empresa de la sesión (admin exento). */
    public static void requerirEmpresaPropia(HttpServletRequest request, Integer empresaIdOferta) {
        Usuario usuario = requerirAutenticado(request);
        if (usuario.getRol() == UsuarioRol.ADMINISTRADOR) {
            return;
        }
        Integer empresaPropia = empresaIdDeSesion(request);
        if (!Objects.equals(empresaPropia, empresaIdOferta)) {
            throw new ApiExcepcion(403, "PROHIBIDO",
                    "Solo puedes gestionar las ofertas de tu propia empresa.");
        }
    }
}
