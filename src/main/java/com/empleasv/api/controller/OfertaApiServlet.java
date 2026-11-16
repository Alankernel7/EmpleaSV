
package com.empleasv.api.controller;

import com.empleasv.api.ApiExcepcion;
import com.empleasv.api.ApiServletBase;
import com.empleasv.api.ApiSesion;
import com.empleasv.api.dto.OfertaDto;
import com.empleasv.dao.EmpresaDAO;
import com.empleasv.dao.OfertaEmpleoDAO;
import com.empleasv.model.Auditoria;
import com.empleasv.model.Empresa;
import com.empleasv.model.OfertaEmpleo;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.service.AdminService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@WebServlet("/api/ofertas/*")
public class OfertaApiServlet extends ApiServletBase {

    private final OfertaEmpleoDAO ofertaDAO = new OfertaEmpleoDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();

    // GET del commit 1: se mantiene la funcionalidad anterior.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            List<String> ruta = segmentos(request);
            if (ruta.isEmpty()) {
                listar(request, response);
            } else if (ruta.size() == 1) {
                detalle(request, response);
            } else {
                throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }
        } catch (Exception e) {
            manejar(e, response);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String titulo = request.getParameter("titulo");
        String ubicacion = request.getParameter("ubicacion");
        String estado = request.getParameter("estado");
        String mias = request.getParameter("mias");

        if (mias != null && !Set.of("true", "false").contains(mias.toLowerCase(Locale.ROOT))) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El parámetro mias debe ser true o false.");
        }

        boolean soloMias = "true".equalsIgnoreCase(mias);
        boolean admin = ApiSesion.esAdmin(request);
        Integer empresaId = leerEmpresaId(request);

        if (soloMias) {
            ApiSesion.requerirRol(request, UsuarioRol.EMPRESA);
            empresaId = ApiSesion.empresaIdDeSesion(request);
        }

        if (estado != null) {
            if (!Set.of("ACTIVA", "PAUSADA", "CERRADA").contains(estado)) {
                throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                        "El estado indicado no es válido.");
            }
            if (!admin) {
                throw new ApiExcepcion(403, "PROHIBIDO",
                        "No tienes permiso para filtrar por estado.");
            }
        }

        List<Map<String, Object>> datos = new ArrayList<>();
        for (OfertaEmpleo oferta : ofertaDAO.listarTodas()) {
            if (!admin && !soloMias && !"ACTIVA".equals(oferta.getEstado())) {
                continue;
            }
            if (empresaId != null && !empresaId.equals(oferta.getEmpresaId())) {
                continue;
            }
            if (estado != null && !estado.equals(oferta.getEstado())) {
                continue;
            }
            if (titulo != null && !titulo.isBlank()
                    && (oferta.getTitulo() == null
                    || !oferta.getTitulo().toLowerCase(Locale.ROOT)
                    .contains(titulo.trim().toLowerCase(Locale.ROOT)))) {
                continue;
            }
            if (ubicacion != null && !ubicacion.isBlank()
                    && !ubicacion.trim().equals(oferta.getUbicacion())) {
                continue;
            }
            datos.add(OfertaDto.aMapa(oferta, oferta.getEmpresa().getNombre()));
        }
        responderOk(response, datos);
    }

    private void detalle(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = idDeRuta(request);
        OfertaEmpleo oferta = ofertaDAO.obtenerPorId(id);
        if (oferta == null) {
            throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                    "La oferta solicitada no existe.");
        }

        boolean puedeVer = "ACTIVA".equals(oferta.getEstado()) || ApiSesion.esAdmin(request);
        Usuario usuario = ApiSesion.usuario(request);
        if (!puedeVer && usuario != null && usuario.getRol() == UsuarioRol.EMPRESA) {
            Integer empresaPropia = ApiSesion.empresaIdDeSesion(request);
            puedeVer = Objects.equals(empresaPropia, oferta.getEmpresaId());
        }
        if (!puedeVer) {
            throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                    "La oferta solicitada no existe.");
        }
        responderOk(response, OfertaDto.aMapa(oferta, oferta.getEmpresa().getNombre()));
    }

    private Integer leerEmpresaId(HttpServletRequest request) {
        String valor = request.getParameter("empresaId");
        if (valor == null) {
            return null;
        }
        try {
            int id = Integer.parseInt(valor);
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException e) {
            // El error se devuelve abajo con un mensaje uniforme.
        }
        throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                "empresaId debe ser un entero positivo.");
    }

    // POST /api/ofertas: solo EMPRESA o ADMINISTRADOR.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            if (!segmentos(request).isEmpty()) {
                throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }
            Usuario usuario = ApiSesion.requerirRol(request,
                    UsuarioRol.EMPRESA, UsuarioRol.ADMINISTRADOR);
            exigirJson(request);
            Map<String, Object> datos = leerObjeto(request);

            Integer empresaId = usuario.getRol() == UsuarioRol.ADMINISTRADOR
                    ? empresaIdValido(datos)
                    : ApiSesion.empresaIdDeSesion(request);
            Empresa empresa = buscarEmpresa(empresaId);

            OfertaEmpleo oferta = new OfertaEmpleo();
            aplicarCampos(datos, oferta, "ACTIVA");
            oferta.setEmpresa(empresa);
            oferta.setFechaPublicacion(LocalDateTime.now());

            if (!ofertaDAO.registrar(oferta)) {
                throw new ApiExcepcion(500, "ERROR_INTERNO",
                        "No se pudo registrar la oferta.");
            }
            response.setHeader("Location", request.getContextPath()
                    + "/api/ofertas/" + oferta.getId());
            responderCreado(response, "Oferta registrada correctamente.",
                    OfertaDto.aMapa(oferta, empresa.getNombre()));
        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // PUT /api/ofertas/{id}: solo propietaria o ADMINISTRADOR.
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            if (segmentos(request).size() != 1) {
                throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }
            Usuario usuario = ApiSesion.requerirRol(request,
                    UsuarioRol.EMPRESA, UsuarioRol.ADMINISTRADOR);
            OfertaEmpleo oferta = ofertaEditable(request);
            exigirJson(request);
            Map<String, Object> datos = leerObjeto(request);

            String estadoAnterior = oferta.getEstado();
            aplicarCampos(datos, oferta, estadoAnterior);
            if (!Objects.equals(estadoAnterior, oferta.getEstado())) {
                validarTransicion(estadoAnterior, oferta.getEstado(), usuario.getRol());
            }

            // La empresa asociada solo puede cambiarla un administrador.
            if (usuario.getRol() == UsuarioRol.ADMINISTRADOR
                    && datos.containsKey("empresaId")) {
                oferta.setEmpresa(buscarEmpresa(empresaIdValido(datos)));
            }

            // Fecha de publicación e ID no se toman del JSON.
            if (!ofertaDAO.actualizar(oferta)) {
                throw new ApiExcepcion(500, "ERROR_INTERNO",
                        "No se pudo actualizar la oferta.");
            }

            if (usuario.getRol() == UsuarioRol.ADMINISTRADOR
                    && !Objects.equals(estadoAnterior, oferta.getEstado())) {
                new AdminService().registrarAuditoria(usuario.getId(),
                        "CAMBIAR_ESTADO_OFERTA", Auditoria.TipoObjetivo.OFERTA,
                        oferta.getId(), "Estado " + estadoAnterior + " -> " + oferta.getEstado());
            }
            responderOk(response, "Oferta actualizada correctamente.",
                    OfertaDto.aMapa(oferta, oferta.getEmpresa().getNombre()));
        } catch (Exception e) {
            manejar(e, response);
        }
    }

    // DELETE /api/ofertas/{id}: solo propietaria o ADMINISTRADOR.
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            if (segmentos(request).size() != 1) {
                throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }
            ApiSesion.requerirRol(request,
                    UsuarioRol.EMPRESA, UsuarioRol.ADMINISTRADOR);
            OfertaEmpleo oferta = ofertaEditable(request);
            if (!ofertaDAO.eliminar(oferta.getId())) {
                throw new ApiExcepcion(500, "ERROR_INTERNO",
                        "No se pudo eliminar la oferta.");
            }
            responderNoContenido(response);
        } catch (Exception e) {
            manejar(e, response);
        }
    }

    private OfertaEmpleo ofertaEditable(HttpServletRequest request) {
        Integer id = idDeRuta(request);
        if (id <= 0) {
            throw new ApiExcepcion(400, "ID_INVALIDO",
                    "El identificador debe ser positivo.");
        }
        OfertaEmpleo oferta = ofertaDAO.obtenerPorId(id);
        if (oferta == null) {
            throw new ApiExcepcion(404, "RECURSO_NO_ENCONTRADO",
                    "La oferta solicitada no existe.");
        }
        ApiSesion.requerirEmpresaPropia(request, oferta.getEmpresaId());
        return oferta;
    }

    private void exigirJson(HttpServletRequest request) {
        String tipo = request.getContentType();
        if (tipo == null || !tipo.toLowerCase(Locale.ROOT)
                .matches("^application/json(?:\\s*;.*)?$")) {
            throw new ApiExcepcion(415, "TIPO_CONTENIDO_NO_SOPORTADO",
                    "La solicitud debe enviarse como application/json.");
        }
    }

    private Empresa buscarEmpresa(Integer id) {
        Empresa empresa = id == null ? null : empresaDAO.obtenerPorId(id);
        if (empresa == null) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "La empresa indicada no existe.");
        }
        return empresa;
    }

    private Integer empresaIdValido(Map<String, Object> datos) {
        Object valor = datos.get("empresaId");
        if (!(valor instanceof Number)) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "empresaId debe ser un entero positivo.");
        }
        try {
            int id = new BigDecimal(valor.toString()).intValueExact();
            if (id > 0) {
                return id;
            }
        } catch (ArithmeticException | NumberFormatException ignored) {
            // Un entero fuera del rango o fraccionario no es válido.
        }
        throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                "empresaId debe ser un entero positivo.");
    }

    private void aplicarCampos(Map<String, Object> datos, OfertaEmpleo oferta,
                               String estadoPorDefecto) {
        oferta.setTitulo(textoObligatorio(datos, "titulo", 200));
        oferta.setDescripcion(textoObligatorio(datos, "descripcion", 0));
        oferta.setRequisitos(textoOpcional(datos, "requisitos", 0));
        oferta.setUbicacion(textoObligatorio(datos, "ubicacion", 150));
        oferta.setSalario(salario(datos));
        oferta.setTipoContrato(textoObligatorio(datos, "tipoContrato", 50));
        oferta.setHorario(textoOpcional(datos, "horario", 100));
        String estado = textoOpcional(datos, "estado", 30);
        if (estado == null) {
            estado = estadoPorDefecto;
        }
        if (!Set.of("ACTIVA", "PAUSADA", "CERRADA").contains(estado)) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "estado debe ser ACTIVA, PAUSADA o CERRADA.");
        }
        oferta.setEstado(estado);
    }

    private String textoObligatorio(Map<String, Object> datos,
                                    String campo, int maximo) {
        String valor = textoOpcional(datos, campo, maximo);
        if (valor == null) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El campo '" + campo + "' es obligatorio.");
        }
        return valor;
    }

    private String textoOpcional(Map<String, Object> datos,
                                 String campo, int maximo) {
        Object valor = datos.get(campo);
        if (valor == null) {
            return null;
        }
        if (!(valor instanceof String)) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El campo '" + campo + "' debe ser texto.");
        }
        String texto = ((String) valor).trim();
        if (maximo > 0 && texto.length() > maximo) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El campo '" + campo + "' excede " + maximo + " caracteres.");
        }
        return texto.isEmpty() ? null : texto;
    }

    private BigDecimal salario(Map<String, Object> datos) {
        Object valor = datos.get("salario");
        if (valor == null) {
            return null;
        }
        if (!(valor instanceof Number)) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "salario debe ser un número decimal.");
        }
        BigDecimal numero = new BigDecimal(valor.toString());
        if (numero.compareTo(BigDecimal.ZERO) < 0
                || numero.compareTo(new BigDecimal("99999999.99")) > 0
                || numero.scale() > 2) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "salario debe estar entre 0 y 99999999.99, con máximo dos decimales.");
        }
        return numero;
    }

    private void validarTransicion(String anterior, String nuevo, UsuarioRol rol) {
        boolean permitida = ("ACTIVA".equals(anterior)
                && ("PAUSADA".equals(nuevo) || "CERRADA".equals(nuevo)))
                || ("PAUSADA".equals(anterior)
                && ("ACTIVA".equals(nuevo) || "CERRADA".equals(nuevo)))
                || ("CERRADA".equals(anterior) && "ACTIVA".equals(nuevo)
                && rol == UsuarioRol.ADMINISTRADOR);
        if (!permitida) {
            throw new ApiExcepcion(409, "REGLA_NEGOCIO",
                    "La transición de estado indicada no está permitida.");
        }
    }


    // PATCH /api/ofertas/{id}/estado
    @Override
    protected void doPatch(HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException {

        try {
            List<String> ruta = segmentos(request);

            if (ruta.size() != 2
                    || !"estado".equals(ruta.get(1))) {
                throw new ApiExcepcion(
                        404, "RECURSO_NO_ENCONTRADO",
                        "La ruta solicitada no existe.");
            }

            Usuario usuario = ApiSesion.requerirRol(
                    request,
                    UsuarioRol.EMPRESA,
                    UsuarioRol.ADMINISTRADOR);

            OfertaEmpleo oferta = ofertaEditable(request);

            exigirJson(request);
            Map<String, Object> datos = leerObjeto(request);

            Object valor = datos.get("estado");

            if (!(valor instanceof String)
                    || !Set.of("ACTIVA", "PAUSADA", "CERRADA")
                    .contains(valor)) {
                throw new ApiExcepcion(
                        400, "DATOS_INVALIDOS",
                        "estado debe ser ACTIVA, PAUSADA o CERRADA.");
            }

            String nuevoEstado = (String) valor;
            String estadoAnterior = oferta.getEstado();

            if (!Objects.equals(estadoAnterior, nuevoEstado)) {

                validarTransicion(
                        estadoAnterior,
                        nuevoEstado,
                        usuario.getRol());

                if (usuario.getRol() == UsuarioRol.ADMINISTRADOR) {

                    new AdminService().cambiarEstadoOferta(
                            usuario.getId(),
                            oferta.getId(),
                            nuevoEstado);

                } else {

                    oferta.setEstado(nuevoEstado);

                    if (!ofertaDAO.actualizar(oferta)) {
                        throw new ApiExcepcion(
                                500, "ERROR_INTERNO",
                                "No se pudo actualizar la oferta.");
                    }
                }
            }

            responderOk(
                    response,
                    "Estado de la oferta actualizado.",
                    Map.of(
                            "id", oferta.getId(),
                            "estado", nuevoEstado)
            );

        } catch (AdminService.AdminException e) {

            manejar(new ApiExcepcion(
                    409,
                    "REGLA_NEGOCIO",
                    e.getMessage()), response);

        } catch (Exception e) {
            manejar(e, response);
        }
    }

}
