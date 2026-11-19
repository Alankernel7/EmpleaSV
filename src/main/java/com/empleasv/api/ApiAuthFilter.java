package com.empleasv.api;

import com.empleasv.dao.UsuarioDAO;
import com.empleasv.model.Usuario;
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

/**
 * Autenticación para /api/*.
 *
 * No crea un sistema de autenticación nuevo: reutiliza la sesión HTTP que ya
 * crea AuthServlet en /auth y las mismas reglas de AuthFilter. Solo cambia la
 * respuesta: en vez de redirigir a una página de login, devuelve 401 JSON.
 *
 * La autorización por rol y por propiedad se valida en cada servlet, porque
 * depende del recurso y del método HTTP.
 */
@WebFilter(urlPatterns = "/api/*")
public class ApiAuthFilter implements Filter {

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

        // Revalida contra la base de datos que la cuenta siga activa.
        Usuario usuario = SesionUtil.usuario(request);
        if (usuario != null && usuario.getId() != null) {
            Usuario vigente = usuarioDAO.obtenerPorId(usuario.getId());
            if (vigente == null || !"ACTIVO".equals(vigente.getEstado())) {
                SesionUtil.cerrarSesion(request);
                usuario = null;
            } else {
                SesionUtil.actualizarSesion(request, vigente);
                usuario = vigente;
            }
        }

        if (esRutaPublica(request)) {
            chain.doFilter(request, response);
            return;
        }

        if (usuario == null) {
            ApiRespuesta.error(401, "NO_AUTENTICADO",
                    "Debes iniciar sesión para acceder a este recurso.")
                    .escribir(response);
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Rutas que no requieren sesión:
     *  - login y logout,
     *  - registro de candidato/empresa,
     *  - consulta pública de ofertas y empresas.
     */
    private boolean esRutaPublica(HttpServletRequest request) {
        String ruta = rutaCompleta(request);
        String metodo = request.getMethod();

        if ("POST".equalsIgnoreCase(metodo)) {
            return "/api/auth/login".equals(ruta)
                    || "/api/auth/logout".equals(ruta)
                    || "/api/usuarios".equals(ruta);
        }

        if ("GET".equalsIgnoreCase(metodo)) {
            return "/api/ofertas".equals(ruta)
                    || ruta.startsWith("/api/ofertas/")
                    || "/api/empresas".equals(ruta)
                    || ruta.startsWith("/api/empresas/");
        }

        return false;
    }

    /**
     * Ruta completa sin el contexto de la aplicación.
     *
     * No se usa getServletPath() porque su valor depende de cómo esté mapeado
     * cada servlet (mapeo exacto, /recurso/* o el servlet por defecto).
     */
    private String rutaCompleta(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contexto = request.getContextPath();
        if (contexto != null && !contexto.isEmpty() && uri.startsWith(contexto)) {
            return uri.substring(contexto.length());
        }
        return uri;
    }
}
