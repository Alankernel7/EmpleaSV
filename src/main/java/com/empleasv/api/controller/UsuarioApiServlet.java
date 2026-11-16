
package com.empleasv.api.controller;

import com.empleasv.api.ApiExcepcion;
import com.empleasv.api.ApiServletBase;
import com.empleasv.api.ApiSesion;
import com.empleasv.api.dto.UsuarioDto;
import com.empleasv.dao.PerfilEmpresaDAO;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.service.AdminService;
import com.empleasv.service.RegistroException;
import com.empleasv.service.RegistroService;
import com.empleasv.util.ValidacionUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebServlet("/api/usuarios/*")
public class UsuarioApiServlet extends ApiServletBase {

    private final AdminService adminService = new AdminService();
    private final PerfilEmpresaDAO perfilEmpresaDAO =
            new PerfilEmpresaDAO();

    // GET /api/usuarios
    // GET /api/usuarios/{id}
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        try {
            List<String> ruta = segmentos(request);

            if (ruta.isEmpty()) {
                listarUsuarios(request, response);
            } else if (ruta.size() == 1) {
                obtenerUsuario(request, response);
            } else {
                throw new ApiExcepcion(
                        404,
                        "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe."
                );
            }

        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // Listado exclusivo para ADMINISTRADOR
    private void listarUsuarios(HttpServletRequest request,
                                HttpServletResponse response)
            throws IOException {

        ApiSesion.requerirRol(
                request, UsuarioRol.ADMINISTRADOR
        );

        String q = request.getParameter("q");
        String rol = request.getParameter("rol");
        String estado = request.getParameter("estado");

        if (rol != null && !rol.isBlank()
                && !Set.of("USUARIO", "EMPRESA", "ADMINISTRADOR")
                .contains(rol)) {

            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "El filtro rol no es válido."
            );
        }

        if (estado != null && !estado.isBlank()
                && !Set.of("ACTIVO", "INACTIVO")
                .contains(estado)) {

            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "El filtro estado no es válido."
            );
        }

        if (q != null && q.length() > 150) {
            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "El filtro de búsqueda es demasiado largo."
            );
        }

        List<Usuario> usuarios =
                adminService.buscarUsuarios(q, rol, estado);

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            resultado.add(UsuarioDto.aMapa(usuario));
        }

        responderOk(response, resultado);
    }

    // Detalle del usuario propio o de cualquier usuario para ADMIN
    private void obtenerUsuario(HttpServletRequest request,
                                HttpServletResponse response)
            throws IOException {

        Integer id = idDeRuta(request);

        if (id <= 0) {
            throw new ApiExcepcion(
                    400, "ID_INVALIDO",
                    "El identificador debe ser positivo."
            );
        }

        ApiSesion.requerirPropietarioOAdmin(request, id);

        Usuario usuario = adminService.obtenerUsuario(id);

        if (usuario == null) {
            throw new ApiExcepcion(
                    404, "RECURSO_NO_ENCONTRADO",
                    "El usuario solicitado no existe."
            );
        }

        responderOk(response, UsuarioDto.aMapa(usuario));
    }

    // POST /api/usuarios
    // Registro público de candidatos y empresas
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        try {
            if (!segmentos(request).isEmpty()) {
                throw new ApiExcepcion(
                        404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe."
                );
            }

            exigirJson(request);

            Map<String, Object> datos = leerObjeto(request);

            String rol = requerido(datos, "rol", 20);

            if (!Set.of("USUARIO", "EMPRESA").contains(rol)) {
                throw new ApiExcepcion(
                        400, "DATOS_INVALIDOS",
                        "Solo se pueden registrar cuentas USUARIO o EMPRESA."
                );
            }

            String email = requerido(datos, "email", 150);

            if (!ValidacionUtil.esEmailValido(email)) {
                throw new ApiExcepcion(
                        400, "DATOS_INVALIDOS",
                        "El correo electrónico no es válido."
                );
            }

            String password = validarPassword(datos);

            RegistroService registro =
                    new RegistroService(getServletContext());

            Usuario nuevo;

            if ("USUARIO".equals(rol)) {
                nuevo = registrarCandidato(
                        registro, datos, email, password
                );
            } else {
                nuevo = registrarEmpresa(
                        registro, datos, email, password
                );
            }

            Integer empresaId = null;

            if (nuevo.getRol() == UsuarioRol.EMPRESA) {
                empresaId = perfilEmpresaDAO
                        .obtenerEmpresaIdPorUsuarioId(nuevo.getId());
            }

            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("usuario", UsuarioDto.aMapa(nuevo));
            resultado.put("empresaId", empresaId);

            response.setHeader(
                    "Location",
                    request.getContextPath()
                            + "/api/usuarios/" + nuevo.getId()
            );

            responderCreado(
                    response,
                    "Cuenta registrada correctamente.",
                    resultado
            );

        } catch (RegistroException e) {
            String mensaje = e.getMessage() == null
                    ? ""
                    : e.getMessage().toLowerCase(Locale.ROOT);

            if (mensaje.contains("ya esta registrado")) {
                manejar(new ApiExcepcion(
                        409, "CONFLICTO",
                        "El correo electrónico ya está registrado."
                ), response);
            } else {
                manejar(e, response);
            }

        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // Registro de candidato mediante RegistroService
    private Usuario registrarCandidato(
            RegistroService registro,
            Map<String, Object> datos,
            String email,
            String password
    ) throws Exception {

        String nombre = requerido(datos, "nombre", 150);
        String telefono = requerido(datos, "telefono", 30);
        String direccion = requerido(datos, "direccion", 255);

        String municipio = opcional(datos, "municipio", 100);
        String departamento = opcional(datos, "departamento", 100);

        return registro.registrarCandidato(
                nombre,
                email,
                password,
                telefono,
                direccion,
                municipio,
                departamento,
                null,
                null
        );
    }

    // Registro de empresa mediante RegistroService
    private Usuario registrarEmpresa(
            RegistroService registro,
            Map<String, Object> datos,
            String email,
            String password
    ) throws Exception {

        String responsable =
                requerido(datos, "nombreResponsable", 150);

        String nombreEmpresa =
                requerido(datos, "nombreEmpresa", 150);

        String nit = opcional(datos, "nit", 50);

        String telefono =
                requerido(datos, "telefonoEmpresa", 30);

        String direccion =
                requerido(datos, "direccionEmpresa", 255);

        String municipio =
                requerido(datos, "municipio", 100);

        String departamento =
                requerido(datos, "departamento", 100);

        String sector =
                requerido(datos, "sector", 100);

        String sitioWeb =
                opcional(datos, "sitioWeb", 255);

        String descripcion =
                opcional(datos, "descripcion", 5000);

        return registro.registrarEmpresa(
                responsable,
                email,
                password,
                nombreEmpresa,
                nit,
                telefono,
                direccion,
                municipio,
                departamento,
                sector,
                sitioWeb,
                descripcion,
                null
        );
    }

    // Verificar que la solicitud contiene JSON
    private void exigirJson(HttpServletRequest request) {

        String tipo = request.getContentType();

        if (tipo == null ||
                !tipo.split(";")[0].trim()
                        .equalsIgnoreCase("application/json")) {

            throw new ApiExcepcion(
                    415,
                    "TIPO_CONTENIDO_NO_SOPORTADO",
                    "La solicitud debe contener application/json."
            );
        }
    }

    // Validar contraseña y confirmación opcional
    private String validarPassword(Map<String, Object> datos) {

        Object valor = datos.get("password");

        if (!(valor instanceof String)) {
            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "La contraseña es obligatoria."
            );
        }

        String password = (String) valor;

        if (password.length() < 6 || password.trim().isEmpty()) {
            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "La contraseña debe tener al menos 6 caracteres."
            );
        }

        if (datos.containsKey("password2")) {
            Object confirmacion = datos.get("password2");

            if (!(confirmacion instanceof String)
                    || !password.equals(confirmacion)) {

                throw new ApiExcepcion(
                        400, "DATOS_INVALIDOS",
                        "Las contraseñas no coinciden."
                );
            }
        }

        return password;
    }

    // Campos de texto obligatorios
    private String requerido(Map<String, Object> datos,
                             String campo, int maximo) {

        String valor = opcional(datos, campo, maximo);

        if (valor == null) {
            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "El campo '" + campo + "' es obligatorio."
            );
        }

        return valor;
    }

    // Campos opcionales con límite de longitud
    private String opcional(Map<String, Object> datos,
                            String campo, int maximo) {

        Object valor = datos.get(campo);

        if (valor == null) {
            return null;
        }

        if (!(valor instanceof String)) {
            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "El campo '" + campo + "' debe ser texto."
            );
        }

        String texto = ((String) valor).trim();

        if (texto.length() > maximo) {
            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "El campo '" + campo + "' excede "
                            + maximo + " caracteres."
            );
        }

        return texto.isEmpty() ? null : texto;
    }


    // PATCH /api/usuarios/{id}/estado
    @Override
    protected void doPatch(HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException {

        response.setHeader("Cache-Control", "no-store");

        try {
            List<String> ruta = segmentos(request);

            if (ruta.size() != 2
                    || !"estado".equals(ruta.get(1))) {
                throw new ApiExcepcion(
                        404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }

            Usuario admin = ApiSesion.requerirRol(
                    request, UsuarioRol.ADMINISTRADOR);

            Integer usuarioId = idDeRuta(request, 0);

            if (usuarioId <= 0) {
                throw new ApiExcepcion(
                        400, "ID_INVALIDO",
                        "El identificador debe ser positivo.");
            }

            exigirJson(request);
            Map<String, Object> datos = leerObjeto(request);

            Object valor = datos.get("estado");

            if (!(valor instanceof String)
                    || !Set.of("ACTIVO", "INACTIVO")
                    .contains(valor)) {
                throw new ApiExcepcion(
                        400, "DATOS_INVALIDOS",
                        "estado debe ser ACTIVO o INACTIVO.");
            }

            String estado = (String) valor;

            String mensaje = adminService.cambiarEstadoUsuario(
                    admin.getId(), usuarioId, estado);

            responderOk(
                    response,
                    mensaje,
                    Map.of(
                            "id", usuarioId,
                            "estado", estado)
            );

        } catch (AdminService.AdminException e) {

            int codigo = e.getMessage() != null
                    && e.getMessage().contains("no existe")
                    ? 404 : 409;

            manejar(new ApiExcepcion(
                    codigo,
                    codigo == 404
                            ? "RECURSO_NO_ENCONTRADO"
                            : "REGLA_NEGOCIO",
                    e.getMessage()), response);

        } catch (Exception e) {
            manejar(e, response);
        }
    }

}
