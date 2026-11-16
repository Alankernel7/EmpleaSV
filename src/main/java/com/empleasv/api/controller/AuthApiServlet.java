
package com.empleasv.api.controller;

import com.empleasv.api.ApiExcepcion;
import com.empleasv.api.ApiJsonReader;
import com.empleasv.api.ApiServletBase;
import com.empleasv.api.ApiSesion;
import com.empleasv.api.dto.UsuarioDto;
import com.empleasv.dao.PerfilEmpresaDAO;
import com.empleasv.dao.UsuarioDAO;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.util.PasswordUtil;
import com.empleasv.util.SesionUtil;
import com.empleasv.util.ValidacionUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthApiServlet extends ApiServletBase {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PerfilEmpresaDAO perfilEmpresaDAO = new PerfilEmpresaDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        try {
            if (segmentos(request).size() != 1) {
                throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }

            switch (segmento(request, 0)) {
                case "login":
                    login(request, response);
                    break;
                case "logout":
                    SesionUtil.cerrarSesion(request);
                    responderNoContenido(response);
                    break;
                case "sesion":
                    metodoNoPermitido(response, "GET");
                    break;
                default:
                    throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                            "La ruta solicitada no existe.");
            }
        } catch (Exception error) {
            manejar(error, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        try {
            if (segmentos(request).size() != 1) {
                throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }

            if ("sesion".equals(segmento(request, 0))) {
                Usuario usuario = ApiSesion.requerirAutenticado(request);
                responderOk(response, datosDeSesion(usuario));
            } else if ("login".equals(segmento(request, 0))
                    || "logout".equals(segmento(request, 0))) {
                metodoNoPermitido(response, "POST");
            } else {
                throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }
        } catch (Exception error) {
            manejar(error, response);
        }
    }

    private void login(HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        Map<String, Object> datos = leerObjeto(request);
        String email = ApiJsonReader.textoRequerido(datos, "email");
        Object valorPassword = datos.get("password");

        if (!ValidacionUtil.esEmailValido(email)
                || email.length() > 150) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El correo electrónico no tiene un formato válido.");
        }
        if (!(valorPassword instanceof String)
                || ((String) valorPassword).isEmpty()) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El campo 'password' es obligatorio.");
        }

        String password = (String) valorPassword;
        Usuario usuario = usuarioDAO.obtenerPorEmail(email);

        if (usuario == null
                || !PasswordUtil.verifyPassword(password, usuario.getPasswordHash())) {
            throw new ApiExcepcion(401, "CREDENCIALES_INVALIDAS",
                    "Correo electrónico o contraseña incorrectos.");
        }

        if (!"ACTIVO".equals(usuario.getEstado())) {
            throw new ApiExcepcion(403, "CUENTA_INACTIVA",
                    "La cuenta está inactiva.");
        }

        Integer empresaId = null;
        if (usuario.getRol() == UsuarioRol.EMPRESA) {
            empresaId = perfilEmpresaDAO.obtenerEmpresaIdPorUsuarioId(usuario.getId());
        }

        request.getSession(true);
        request.changeSessionId();
        SesionUtil.iniciarSesion(request, usuario, empresaId);

        responderOk(response, "Sesión iniciada correctamente.",
                datosDeSesion(usuario, empresaId));
    }

    private Map<String, Object> datosDeSesion(Usuario usuario) {
        Integer empresaId = null;
        if (usuario.getRol() == UsuarioRol.EMPRESA) {
            empresaId = perfilEmpresaDAO.obtenerEmpresaIdPorUsuarioId(usuario.getId());
        }
        return datosDeSesion(usuario, empresaId);
    }

    private Map<String, Object> datosDeSesion(Usuario usuario, Integer empresaId) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("usuario", UsuarioDto.aMapa(usuario));
        datos.put("empresaId", empresaId);
        return datos;
    }
}
