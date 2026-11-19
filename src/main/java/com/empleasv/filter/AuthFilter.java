package com.empleasv.filter;

import com.empleasv.dao.UsuarioDAO;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.util.SesionUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * Filtro de autorización del lado del servidor.
 *
 * Protege las rutas privadas y verifica el rol del usuario autenticado.
 * No depende de que los botones estén ocultos en la interfaz.
 *
 * Reglas:
 *  - /archivo            -> sesión activa
  - /mis-postulaciones   -> rol USUARIO
  - /ofertas-guardadas   -> rol USUARIO
 *  - /perfil             -> rol USUARIO
 *  - /perfil-empresa     -> rol EMPRESA
 *  - /panel-empresa      -> rol EMPRESA
 *  - /ofertas (escritura)-> rol EMPRESA o ADMINISTRADOR
 *  - /admin              -> rol ADMINISTRADOR
 *
 * Además revalida en base de datos que la cuenta siga activa y
 * refresca los datos de sesión (nombre y estado).
 */
@WebFilter(urlPatterns = {
        "/archivo",
        "/perfil",
        "/perfil/*",
        "/perfil-empresa",
        "/perfil-empresa/*",
        "/panel-empresa",
        "/panel-empresa/*",
        "/ofertas",
        "/mis-postulaciones.jsp",
        "/ofertas-guardadas.jsp",
        "/admin",
        "/admin/*"
})
public class AuthFilter implements Filter {

    private static final Set<String> ACCIONES_ESCRITURA = Set.of(
            "registrar", "actualizar", "eliminar", "editar");

    private UsuarioDAO usuarioDAO;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        usuarioDAO = new UsuarioDAO();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String ruta = request.getServletPath() == null ? "" : request.getServletPath();

        // Revalida la sesión contra la base de datos (estado y nombre vigentes)
        Usuario usuario = SesionUtil.usuario(request);
        if (usuario != null && usuario.getId() != null) {
            Usuario vigente = usuarioDAO.obtenerPorId(usuario.getId());
            if (vigente == null || !"ACTIVO".equals(vigente.getEstado())) {
                SesionUtil.cerrarSesion(request);
                response.sendRedirect(request.getContextPath()
                        + "/auth?accion=login&cuenta=inactiva");
                return;
            }
            SesionUtil.actualizarSesion(request, vigente);
            usuario = vigente;
        }

        boolean requiereSesion = esRutaPrivada(ruta)
                || esAdmin(ruta)
                || (esOfertas(ruta) && esAccionDeEscritura(request));

        if (requiereSesion && usuario == null) {
            response.sendRedirect(request.getContextPath()
                    + "/auth?accion=login&restringido=1");
            return;
        }

        UsuarioRol rol = usuario != null ? usuario.getRol() : null;

        boolean denegado = false;

        if (usuario != null) {
            if (esAdmin(ruta)) {
                denegado = rol != UsuarioRol.ADMINISTRADOR;
            } else if (esPerfilEmpresa(ruta)) {
                denegado = rol != UsuarioRol.EMPRESA;
            } else if (esPanelEmpresa(ruta)) {
                denegado = rol != UsuarioRol.EMPRESA;
            } else if (esPerfilCandidato(ruta) || esSeccionCandidato(ruta)) {
                denegado = rol != UsuarioRol.USUARIO;
            } else if (esOfertas(ruta) && esAccionDeEscritura(request)) {
                denegado = rol != UsuarioRol.EMPRESA && rol != UsuarioRol.ADMINISTRADOR;
            }
        }

        if (denegado) {
            SesionUtil.mensajeError(request,
                    "No tienes permisos para acceder a esa seccion.");
            SesionUtil.redirigirSegunRol(request, response,
                    rol != null ? rol : UsuarioRol.USUARIO);
            return;
        }

        chain.doFilter(request, response);
    }

    // ==================== Clasificación de rutas ====================

    private boolean esRutaPrivada(String ruta) {
        return esPerfilEmpresa(ruta) || esPanelEmpresa(ruta)
                || esPerfilCandidato(ruta) || ruta.startsWith("/archivo")
                || ruta.equals("/mis-postulaciones.jsp")
                || ruta.equals("/ofertas-guardadas.jsp");
    }

    private boolean esPerfilEmpresa(String ruta) {
        return ruta.equals("/perfil-empresa") || ruta.startsWith("/perfil-empresa/");
    }

    private boolean esPanelEmpresa(String ruta) {
        return ruta.equals("/panel-empresa") || ruta.startsWith("/panel-empresa/");
    }

    private boolean esPerfilCandidato(String ruta) {
        return ruta.equals("/perfil") || ruta.startsWith("/perfil/");
    }

    /** Páginas personales del candidato (estado vacío honesto). */
    private boolean esSeccionCandidato(String ruta) {
        return ruta.equals("/mis-postulaciones.jsp")
                || ruta.equals("/ofertas-guardadas.jsp");
    }

    private boolean esOfertas(String ruta) {
        return ruta.equals("/ofertas") || ruta.startsWith("/ofertas/");
    }

    private boolean esAdmin(String ruta) {
        return ruta.equals("/admin") || ruta.startsWith("/admin/");
    }

    private boolean esAccionDeEscritura(HttpServletRequest request) {
        String accion = request.getParameter("accion");
        if (accion == null) {
            return false;
        }
        String metodo = request.getMethod();
        if ("POST".equalsIgnoreCase(metodo)) {
            return ACCIONES_ESCRITURA.contains(accion);
        }
        // GET solo restringe la apertura de formularios de creación/edición
        return "registrar".equals(accion) || "editar".equals(accion);
    }
}
