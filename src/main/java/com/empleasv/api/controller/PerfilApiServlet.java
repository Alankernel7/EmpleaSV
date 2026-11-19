
package com.empleasv.api.controller;

import com.empleasv.api.ApiExcepcion;
import com.empleasv.api.ApiServletBase;
import com.empleasv.api.ApiSesion;
import com.empleasv.api.dto.PerfilCandidatoDto;
import com.empleasv.api.dto.PerfilEmpresaDto;
import com.empleasv.dao.UsuarioDAO;
import com.empleasv.model.PerfilCandidato;
import com.empleasv.model.PerfilEmpresa;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.service.PerfilService;
import com.empleasv.service.RegistroException;
import com.empleasv.util.SesionUtil;
import com.empleasv.util.ValidacionUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/api/perfiles/*")
public class PerfilApiServlet extends ApiServletBase {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private PerfilService perfilService() {
        return new PerfilService(getServletContext());
    }

    // GET: consultar perfiles
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {
        try {
            List<String> ruta = segmentos(request);

            if (ruta.size() == 1
                    && "candidato".equals(ruta.get(0))) {

                Usuario usuario = ApiSesion.requerirRol(
                        request, UsuarioRol.USUARIO);

                consultarCandidato(usuario.getId(), response);

            } else if (ruta.size() == 1
                    && "empresa".equals(ruta.get(0))) {

                Usuario usuario = ApiSesion.requerirRol(
                        request, UsuarioRol.EMPRESA);

                consultarEmpresa(usuario.getId(), response);

            } else if (ruta.size() == 2
                    && "candidato".equals(ruta.get(0))) {

                Integer usuarioId = idDeRuta(request, 1);
                validarAcceso(request, usuarioId, UsuarioRol.USUARIO);
                consultarCandidato(usuarioId, response);

            } else if (ruta.size() == 2
                    && "empresa".equals(ruta.get(0))) {

                Integer usuarioId = idDeRuta(request, 1);
                validarAcceso(request, usuarioId, UsuarioRol.EMPRESA);
                consultarEmpresa(usuarioId, response);

            } else {
                throw new ApiExcepcion(
                        404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }

        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // GET: obtener perfil de candidato
    private void consultarCandidato(Integer usuarioId,
                                    HttpServletResponse response)
            throws IOException {

        PerfilService servicio = perfilService();
        PerfilCandidato perfil =
                servicio.obtenerOCrearCandidato(usuarioId);

        if (perfil == null) {
            throw new ApiExcepcion(
                    404, "RECURSO_NO_ENCONTRADO",
                    "El perfil de candidato no existe.");
        }

        responderOk(response, PerfilCandidatoDto.aMapa(
                perfil,
                servicio.listarCurriculums(usuarioId)
        ));
    }

    // GET: obtener perfil de empresa
    private void consultarEmpresa(Integer usuarioId,
                                  HttpServletResponse response)
            throws IOException {

        PerfilEmpresa perfil =
                perfilService().obtenerOCrearEmpresa(usuarioId);

        if (perfil == null) {
            throw new ApiExcepcion(
                    404, "RECURSO_NO_ENCONTRADO",
                    "El perfil empresarial no existe.");
        }

        responderOk(response, PerfilEmpresaDto.aMapa(perfil));
    }

    // Comprobar propiedad o permisos de administrador
    private void validarAcceso(HttpServletRequest request,
                               Integer usuarioId,
                               UsuarioRol rolEsperado) {

        if (usuarioId <= 0) {
            throw new ApiExcepcion(
                    400, "ID_INVALIDO",
                    "El identificador debe ser positivo.");
        }

        ApiSesion.requerirPropietarioOAdmin(request, usuarioId);

        Usuario usuario = usuarioDAO.obtenerPorId(usuarioId);

        if (usuario == null || usuario.getRol() != rolEsperado) {
            throw new ApiExcepcion(
                    404, "RECURSO_NO_ENCONTRADO",
                    "El perfil solicitado no existe.");
        }
    }

    // PUT: actualizar el perfil propio
    @Override
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {
        try {
            List<String> ruta = segmentos(request);

            if (ruta.size() != 1) {
                throw new ApiExcepcion(
                        404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }

            String recurso = ruta.get(0);

            if ("candidato".equals(recurso)) {
                ApiSesion.requerirRol(request, UsuarioRol.USUARIO);

            } else if ("empresa".equals(recurso)) {
                ApiSesion.requerirRol(request, UsuarioRol.EMPRESA);

            } else {
                throw new ApiExcepcion(
                        404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }

            exigirJson(request);
            Map<String, Object> datos = leerObjeto(request);

            if ("candidato".equals(recurso)) {
                actualizarCandidato(request, response, datos);
            } else {
                actualizarEmpresa(request, response, datos);
            }

        } catch (RegistroException e) {
            manejar(convertirErrorRegistro(e), response);

        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // PUT /api/perfiles/candidato
    private void actualizarCandidato(
            HttpServletRequest request,
            HttpServletResponse response,
            Map<String, Object> datos)
            throws IOException, RegistroException {

        Usuario usuario = ApiSesion.requerirRol(
                request, UsuarioRol.USUARIO);

        String nombre = requerido(datos, "nombre", 150);
        String email = correo(datos);

        String telefono = opcional(datos, "telefono", 30);
        String direccion = opcional(datos, "direccion", 255);
        String municipio = opcional(datos, "municipio", 100);
        String departamento = opcional(datos, "departamento", 100);

        PerfilService servicio = perfilService();

        Usuario actualizado = servicio.actualizarCandidato(
                usuario.getId(),
                nombre,
                email,
                telefono,
                direccion,
                municipio,
                departamento
        );

        SesionUtil.actualizarSesion(request, actualizado);

        PerfilCandidato perfil =
                servicio.obtenerOCrearCandidato(usuario.getId());

        responderOk(
                response,
                "Perfil actualizado correctamente.",
                PerfilCandidatoDto.aMapa(
                        perfil,
                        servicio.listarCurriculums(usuario.getId())
                )
        );
    }

    // PUT /api/perfiles/empresa
    private void actualizarEmpresa(
            HttpServletRequest request,
            HttpServletResponse response,
            Map<String, Object> datos)
            throws IOException, RegistroException {

        Usuario usuario = ApiSesion.requerirRol(
                request, UsuarioRol.EMPRESA);

        String responsable = requerido(
                datos, "nombreResponsable", 150);

        String email = correo(datos);

        String nombreEmpresa = requerido(
                datos, "nombreEmpresa", 150);

        String nit = opcional(datos, "nit", 50);
        String telefono = opcional(
                datos, "telefonoEmpresa", 30);
        String direccion = opcional(
                datos, "direccionEmpresa", 255);
        String municipio = opcional(
                datos, "municipio", 100);
        String departamento = opcional(
                datos, "departamento", 100);
        String sector = opcional(datos, "sector", 100);
        String sitioWeb = opcional(datos, "sitioWeb", 255);
        String descripcion = opcional(
                datos, "descripcion", 5000);

        PerfilService servicio = perfilService();

        servicio.actualizarEmpresa(
                usuario.getId(),
                responsable,
                email,
                nombreEmpresa,
                nit,
                telefono,
                direccion,
                municipio,
                departamento,
                sector,
                sitioWeb,
                descripcion
        );

        Integer empresaId = servicio.asegurarEmpresaVinculada(
                usuario.getId());

        if (empresaId == null) {
            throw new ApiExcepcion(
                    409, "PERFIL_EMPRESA_INCOMPLETO",
                    "No se pudo vincular el perfil empresarial.");
        }

        SesionUtil.fijarEmpresaEnSesion(request, empresaId);

        Usuario actualizado =
                usuarioDAO.obtenerPorId(usuario.getId());

        if (actualizado != null) {
            SesionUtil.actualizarSesion(request, actualizado);
        }

        PerfilEmpresa perfil =
                servicio.obtenerOCrearEmpresa(usuario.getId());

        responderOk(
                response,
                "Perfil empresarial actualizado correctamente.",
                PerfilEmpresaDto.aMapa(perfil)
        );
    }

    // Verificar Content-Type
    private void exigirJson(HttpServletRequest request) {
        String tipo = request.getContentType();

        if (tipo == null
                || !tipo.split(";")[0].trim()
                .equalsIgnoreCase("application/json")) {

            throw new ApiExcepcion(
                    415, "TIPO_CONTENIDO_NO_SOPORTADO",
                    "Debes enviar application/json.");
        }
    }

    // Validar correo obligatorio
    private String correo(Map<String, Object> datos) {

        String email = requerido(datos, "email", 150);

        if (!ValidacionUtil.esEmailValido(email)) {
            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "El correo electrónico no es válido.");
        }

        return email;
    }

    // Campo obligatorio
    private String requerido(Map<String, Object> datos,
                             String campo, int maximo) {

        String valor = opcional(datos, campo, maximo);

        if (valor == null) {
            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "El campo '" + campo + "' es obligatorio.");
        }

        return valor;
    }

    // Campo de texto opcional
    private String opcional(Map<String, Object> datos,
                            String campo, int maximo) {

        Object valor = datos.get(campo);

        if (valor == null) {
            return null;
        }

        if (!(valor instanceof String)) {
            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "El campo '" + campo + "' debe ser texto.");
        }

        String texto = ((String) valor).trim();

        if (texto.length() > maximo) {
            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "El campo '" + campo + "' excede "
                            + maximo + " caracteres.");
        }

        return texto.isEmpty() ? null : texto;
    }

    // Convertir errores de PerfilService a errores REST
    private ApiExcepcion convertirErrorRegistro(
            RegistroException error) {

        String mensaje = error.getMessage() == null
                ? "No se pudo actualizar el perfil."
                : error.getMessage();

        String texto = mensaje.toLowerCase(Locale.ROOT);

        if (texto.contains("ya esta registrado")) {
            return new ApiExcepcion(
                    409, "CONFLICTO", mensaje);
        }

        if (texto.contains("no existe")) {
            return new ApiExcepcion(
                    404, "RECURSO_NO_ENCONTRADO", mensaje);
        }

        if (texto.contains("no se pudo")) {
            return new ApiExcepcion(
                    500, "ERROR_INTERNO",
                    "No se pudo completar la operación.");
        }

        return new ApiExcepcion(
                400, "DATOS_INVALIDOS", mensaje);
    }
}
