
package com.empleasv.api.controller;

import com.empleasv.api.ApiExcepcion;
import com.empleasv.api.ApiServletBase;
import com.empleasv.api.ApiSesion;
import com.empleasv.api.dto.EmpresaDto;
import com.empleasv.api.dto.OfertaDto;
import com.empleasv.dao.EmpresaDAO;
import com.empleasv.dao.OfertaEmpleoDAO;
import com.empleasv.model.Empresa;
import com.empleasv.model.OfertaEmpleo;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@WebServlet("/api/empresas/*")
public class EmpresaApiServlet extends ApiServletBase {

    private final EmpresaDAO empresaDAO = new EmpresaDAO();
    private final OfertaEmpleoDAO ofertaDAO = new OfertaEmpleoDAO();

    // GET /api/empresas
    // GET /api/empresas/{id}
    // GET /api/empresas/{id}/ofertas
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        try {
            List<String> ruta = segmentos(request);

            if (ruta.isEmpty()) {
                listarEmpresas(request, response);
            } else if (ruta.size() == 1) {
                obtenerDetalle(request, response);
            } else if (ruta.size() == 2
                    && "ofertas".equals(ruta.get(1))) {
                listarOfertas(request, response);
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

    // Listar todas las empresas o las destacadas
    private void listarEmpresas(HttpServletRequest request,
                                HttpServletResponse response) throws IOException {

        String destacadas = request.getParameter("destacadas");

        List<Empresa> empresas;

        if ("true".equalsIgnoreCase(destacadas)) {
            empresas = empresaDAO.listarDestacadas();
        } else {
            empresas = empresaDAO.listarTodas();
        }

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Empresa empresa : empresas) {
            resultado.add(EmpresaDto.aMapa(empresa));
        }

        responderOk(response, resultado);
    }

    // Consultar una empresa mediante su ID
    private void obtenerDetalle(HttpServletRequest request,
                                HttpServletResponse response) throws IOException {

        Empresa empresa = buscarEmpresa(request);

        responderOk(response, EmpresaDto.aMapa(empresa));
    }

    // Consultar ofertas de una empresa
    private void listarOfertas(HttpServletRequest request,
                               HttpServletResponse response) throws IOException {

        Empresa empresa = buscarEmpresa(request);
        String estado = request.getParameter("estado");

        Usuario usuario = ApiSesion.usuario(request);

        boolean administrador = ApiSesion.esAdmin(request);
        boolean propietaria = false;

        // Verificar si la empresa consultada pertenece
        // al usuario EMPRESA autenticado.
        if (usuario != null
                && usuario.getRol() == UsuarioRol.EMPRESA) {

            Integer empresaSesion =
                    ApiSesion.empresaIdDeSesion(request);

            propietaria = Objects.equals(
                    empresaSesion,
                    empresa.getId()
            );
        }

        boolean puedeVerTodos = administrador || propietaria;

        // Solo el administrador o la empresa propietaria
        // pueden utilizar el filtro de estado completo.
        if (puedeVerTodos && estado != null
                && !Set.of("ACTIVA", "PAUSADA", "CERRADA")
                .contains(estado)) {

            throw new ApiExcepcion(
                    400,
                    "DATOS_INVALIDOS",
                    "El estado indicado no es válido."
            );
        }

        List<OfertaEmpleo> ofertas =
                ofertaDAO.listarPorEmpresa(empresa.getId());

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (OfertaEmpleo oferta : ofertas) {

            // Los visitantes y usuarios no propietarios
            // solamente pueden consultar ofertas activas.
            if (!puedeVerTodos
                    && !"ACTIVA".equals(oferta.getEstado())) {
                continue;
            }

            // Aplicar filtro de estado cuando está permitido.
            if (puedeVerTodos
                    && estado != null
                    && !estado.equals(oferta.getEstado())) {
                continue;
            }

            resultado.add(OfertaDto.aMapa(
                    oferta,
                    empresa.getNombre()
            ));
        }

        responderOk(response, resultado);
    }

    // Buscar empresa y validar su identificador
    private Empresa buscarEmpresa(HttpServletRequest request) {

        Integer id = idDeRuta(request);

        if (id <= 0) {
            throw new ApiExcepcion(
                    400,
                    "ID_INVALIDO",
                    "El identificador debe ser positivo."
            );
        }

        Empresa empresa = empresaDAO.obtenerPorId(id);

        if (empresa == null) {
            throw new ApiExcepcion(
                    404,
                    "RECURSO_NO_ENCONTRADO",
                    "La empresa solicitada no existe."
            );
        }

        return empresa;
    }
}
