
package com.empleasv.api.controller;

import com.empleasv.api.ApiExcepcion;
import com.empleasv.api.ApiServletBase;
import com.empleasv.api.ApiSesion;
import com.empleasv.api.dto.OfertaDto;
import com.empleasv.api.dto.UsuarioDto;
import com.empleasv.model.Auditoria;
import com.empleasv.model.OfertaEmpleo;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.service.AdminService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/*")
public class AdminApiServlet extends ApiServletBase {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setHeader("Cache-Control", "no-store");

        try {
            // Todas las rutas de este servlet son administrativas.
            ApiSesion.requerirRol(
                    request, UsuarioRol.ADMINISTRADOR);

            List<String> ruta = segmentos(request);

            if (ruta.size() != 1) {
                throw new ApiExcepcion(
                        404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }

            switch (ruta.get(0)) {
                case "dashboard":
                    dashboard(response);
                    break;

                case "auditoria":
                    auditoria(request, response);
                    break;

                default:
                    throw new ApiExcepcion(
                            404, "RECURSO_NO_ENCONTRADO",
                            "La ruta solicitada no existe.");
            }

        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // GET /api/admin/dashboard
    private void dashboard(HttpServletResponse response)
            throws IOException {

        Map<String, Object> usuarios = new LinkedHashMap<>();

        usuarios.put("total", adminService.totalUsuarios());
        usuarios.put("candidatosActivos",
                adminService.candidatosActivos());
        usuarios.put("empresasActivas",
                adminService.empresasActivas());
        usuarios.put("administradoresActivos",
                adminService.administradoresActivos());

        Map<String, Object> ofertas = new LinkedHashMap<>();

        ofertas.put("ACTIVA",
                adminService.contarOfertas("ACTIVA"));
        ofertas.put("PAUSADA",
                adminService.contarOfertas("PAUSADA"));
        ofertas.put("CERRADA",
                adminService.contarOfertas("CERRADA"));

        List<Map<String, Object>> usuariosRecientes =
                new ArrayList<>();

        for (Usuario usuario :
                adminService.listarUsuariosRecientes(5)) {

            usuariosRecientes.add(UsuarioDto.aMapa(usuario));
        }

        List<Map<String, Object>> ofertasRecientes =
                new ArrayList<>();

        for (OfertaEmpleo oferta :
                adminService.listarOfertasRecientes(5)) {

            ofertasRecientes.add(OfertaDto.aMapa(
                    oferta,
                    oferta.getEmpresa().getNombre()));
        }

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("usuarios", usuarios);
        datos.put("ofertas", ofertas);
        datos.put("usuariosRecientes", usuariosRecientes);
        datos.put("ofertasRecientes", ofertasRecientes);
        datos.put("auditoriaReciente",
                convertirAuditoria(
                        adminService.listarAuditoriaReciente(5)));

        responderOk(response, datos);
    }

    // GET /api/admin/auditoria?limite=20
    private void auditoria(HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException {

        int limite = 20;
        String parametro = request.getParameter("limite");

        if (parametro != null) {
            try {
                limite = Integer.parseInt(parametro);
            } catch (NumberFormatException e) {
                throw new ApiExcepcion(
                        400, "DATOS_INVALIDOS",
                        "limite debe ser un numero entero.");
            }
        }

        if (limite < 1 || limite > 100) {
            throw new ApiExcepcion(
                    400, "DATOS_INVALIDOS",
                    "limite debe estar entre 1 y 100.");
        }

        List<Auditoria> registros =
                adminService.listarAuditoriaReciente(limite);

        responderOk(response, convertirAuditoria(registros));
    }

    // Convertir entidades Auditoria en JSON seguro.
    private List<Map<String, Object>> convertirAuditoria(
            List<Auditoria> registros) {

        List<Map<String, Object>> resultado =
                new ArrayList<>();

        for (Auditoria registro : registros) {

            Map<String, Object> datos = new LinkedHashMap<>();

            datos.put("id", registro.getId());
            datos.put("adminId",
                    registro.getAdmin() != null
                            ? registro.getAdmin().getId()
                            : null);
            datos.put("accion", registro.getAccion());

            datos.put("objetivoTipo",
                    registro.getObjetivoTipo() != null
                            ? registro.getObjetivoTipo().name()
                            : null);

            datos.put("objetivoId",
                    registro.getObjetivoId());

            datos.put("detalle",
                    registro.getDetalle());

            datos.put("fecha",
                    registro.getFecha() != null
                            ? registro.getFecha().toString()
                            : null);

            resultado.add(datos);
        }

        return resultado;
    }
}
