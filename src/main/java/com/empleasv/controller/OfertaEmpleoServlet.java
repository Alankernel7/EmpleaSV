package com.empleasv.controller;

import com.empleasv.dao.OfertaEmpleoDAO;
import com.empleasv.model.OfertaEmpleo;
import com.empleasv.dao.EmpresaDAO;
import com.empleasv.model.Empresa;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

// Servlet para gestionar ofertas de empleo
@WebServlet("/ofertas")
public class OfertaEmpleoServlet extends HttpServlet {

    private OfertaEmpleoDAO ofertaDAO;
    private EmpresaDAO empresaDAO;

    @Override
    public void init() throws ServletException {
        ofertaDAO = new OfertaEmpleoDAO();
        empresaDAO = new EmpresaDAO();
    }

    // ==================== GET: JSON y MVC ====================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String format = request.getParameter("format");
        boolean usarJsp = "jsp".equalsIgnoreCase(format);

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }

        try {
            switch (accion) {
                case "listar":
                    if (usarJsp) {
                        listarOfertasJsp(request, response);
                    } else {
                        listarOfertasJson(response);
                    }
                    break;
                case "buscar":
                    if (usarJsp) {
                        buscarPorTituloJsp(request, response);
                    } else {
                        buscarPorTituloJson(request, response);
                    }
                    break;
                case "filtrar":
                    if (usarJsp) {
                        filtrarPorUbicacionJsp(request, response);
                    } else {
                        filtrarPorUbicacionJson(request, response);
                    }
                    break;
                case "detalle":
                    if (usarJsp) {
                        obtenerDetalleJsp(request, response);
                    } else {
                        obtenerDetalleJson(request, response);
                    }
                    break;
                case "registrar":
                    if (usarJsp) {
                        mostrarFormularioRegistro(request, response);
                    } else {
                        enviarError(response, 400, "Use POST para registrar");
                    }
                    break;
                case "editar":
                    if (usarJsp) {
                        mostrarFormularioEdicion(request, response);
                    } else {
                        enviarError(response, 400, "Use POST para editar");
                    }
                    break;
                default:
                    if (usarJsp) {
                        listarOfertasJsp(request, response);
                    } else {
                        enviarError(response, 400, "Accion no valida: " + accion);
                    }
            }
        } catch (Exception e) {
            if (usarJsp) {
                request.setAttribute("mensajeError", "Error interno: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/listar.jsp").forward(request, response);
            } else {
                enviarError(response, 500, "Error interno: " + e.getMessage());
            }
        }
    }

    // ==================== POST: JSON y MVC ====================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String format = request.getParameter("format");
        boolean usarJsp = "jsp".equalsIgnoreCase(format);

        String accion = request.getParameter("accion");
        if (accion == null) {
            if (usarJsp) {
                response.sendRedirect(request.getContextPath() + "/ofertas?format=jsp");
            } else {
                enviarError(response, 400, "No se especifico ninguna accion");
            }
            return;
        }

        try {
            switch (accion) {
                case "registrar":
                    if (usarJsp) {
                        registrarOfertaJsp(request, response);
                    } else {
                        registrarOfertaJson(request, response);
                    }
                    break;
                case "actualizar":
                    if (usarJsp) {
                        actualizarOfertaJsp(request, response);
                    } else {
                        actualizarOfertaJson(request, response);
                    }
                    break;
                case "eliminar":
                    if (usarJsp) {
                        eliminarOfertaJsp(request, response);
                    } else {
                        eliminarOfertaJson(request, response);
                    }
                    break;
                default:
                    if (usarJsp) {
                        response.sendRedirect(request.getContextPath() + "/ofertas?format=jsp");
                    } else {
                        enviarError(response, 400, "Accion no valida: " + accion);
                    }
            }
        } catch (Exception e) {
            if (usarJsp) {
                request.setAttribute("mensajeError", "Error interno: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/listar.jsp").forward(request, response);
            } else {
                enviarError(response, 500, "Error interno: " + e.getMessage());
            }
        }
    }

    // ==================== MVC: Listar ====================

    private void listarOfertasJsp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<OfertaEmpleo> ofertas = ofertaDAO.listarTodas();
        request.setAttribute("ofertas", ofertas);

        HttpSession session = request.getSession();
        String msgExito = (String) session.getAttribute("mensajeExito");
        String msgError = (String) session.getAttribute("mensajeError");
        if (msgExito != null) {
            request.setAttribute("mensajeExito", msgExito);
            session.removeAttribute("mensajeExito");
        }
        if (msgError != null) {
            request.setAttribute("mensajeError", msgError);
            session.removeAttribute("mensajeError");
        }

        request.getRequestDispatcher("/WEB-INF/views/listar.jsp").forward(request, response);
    }

    // ==================== MVC: Buscar ====================

    private void buscarPorTituloJsp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String titulo = obtenerParametro(request, "titulo");
        List<OfertaEmpleo> ofertas;
        if (titulo != null && !titulo.isEmpty()) {
            ofertas = ofertaDAO.buscarPorTitulo(titulo);
        } else {
            ofertas = ofertaDAO.listarTodas();

        }
        request.setAttribute("ofertas", ofertas);
        request.getRequestDispatcher("/WEB-INF/views/listar.jsp").forward(request, response);
    }

    // ==================== MVC: Filtrar ====================

    private void filtrarPorUbicacionJsp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ubicacion = obtenerParametro(request, "ubicacion");
        List<OfertaEmpleo> ofertas;
        if (ubicacion != null && !ubicacion.isEmpty()) {
            ofertas = ofertaDAO.filtrarPorUbicacion(ubicacion);
        } else {
            ofertas = ofertaDAO.listarTodas();
        }
        request.setAttribute("ofertas", ofertas);
        request.getRequestDispatcher("/WEB-INF/views/filtrar.jsp").forward(request, response);
    }

    // ==================== MVC: Detalle ====================

    private void obtenerDetalleJsp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer id = parsearEntero(request, "id");
        if (id == null) {
            request.setAttribute("mensajeError", "ID de oferta no valido.");
            listarOfertasJsp(request, response);
            return;
        }

        OfertaEmpleo oferta = ofertaDAO.obtenerPorId(id);
        if (oferta == null) {
            request.setAttribute("mensajeError", "No se encontro la oferta con id: " + id);
            listarOfertasJsp(request, response);
            return;
        }

        request.setAttribute("oferta", oferta);
        request.getRequestDispatcher("/WEB-INF/views/detalle.jsp").forward(request, response);
    }

    // ==================== MVC: Formulario Registro ====================

    private void mostrarFormularioRegistro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Empresa> empresas = empresaDAO.listarTodas();

        request.setAttribute("empresas", empresas);

        request.getRequestDispatcher(
                "/WEB-INF/views/registrar.jsp"
        ).forward(request, response);    }

    // ==================== MVC: Formulario Edicion ====================

    private void mostrarFormularioEdicion(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Integer id = parsearEntero(request, "id");

        if (id == null) {

            request.setAttribute(
                    "mensajeError",
                    "ID de oferta no valido."
            );

            listarOfertasJsp(request, response);
            return;
        }

        OfertaEmpleo oferta =
                ofertaDAO.obtenerPorId(id);

        if (oferta == null) {

            request.setAttribute(
                    "mensajeError",
                    "No se encontro la oferta con id: " + id
            );

            listarOfertasJsp(request, response);
            return;
        }

        // Obtener todas las empresas
        List<Empresa> empresas =
                empresaDAO.listarTodas();

        // Enviar oferta y empresas a la vista
        request.setAttribute(
                "oferta",
                oferta
        );

        request.setAttribute(
                "empresas",
                empresas
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/editar.jsp"
        ).forward(request, response);
    }

    // ==================== MVC: Registrar (POST) ====================

    private void registrarOfertaJsp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<String> errores = validarCampos(request, false);
        if (!errores.isEmpty()) {
            request.setAttribute("errores", errores);

            List<Empresa> empresas = empresaDAO.listarTodas();
            request.setAttribute("empresas", empresas);

            request.getRequestDispatcher("/WEB-INF/views/registrar.jsp").forward(request, response);
            return;
        }

        OfertaEmpleo oferta = construirOfertaDesdeRequest(request, false);

        oferta.setFechaPublicacion(
                LocalDateTime.now()
        );

        boolean exito = ofertaDAO.registrar(oferta);

        HttpSession session = request.getSession();
        if (exito) {
            session.setAttribute("mensajeExito", "Oferta registrada correctamente.");
        } else {
            session.setAttribute("mensajeError", "No se pudo registrar la oferta.");
        }
        response.sendRedirect(request.getContextPath() + "/ofertas?format=jsp");
    }

    // ==================== MVC: Actualizar (POST) ====================

    private void actualizarOfertaJsp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<String> errores = validarCampos(request, true);
        if (!errores.isEmpty()) {
            Integer id = parsearEntero(request, "id");

            OfertaEmpleo oferta = (id != null) ? ofertaDAO.obtenerPorId(id)  : null;

            List<Empresa> empresas = empresaDAO.listarTodas();

            request.setAttribute("oferta",oferta);
            request.setAttribute("empresas",empresas);
            request.setAttribute("errores",errores);

            request.getRequestDispatcher(
                    "/WEB-INF/views/editar.jsp"
            ).forward(request, response);
            return;
        }

        OfertaEmpleo oferta =
                construirOfertaDesdeRequest(request, true);

        // Recuperar la oferta original
        OfertaEmpleo ofertaOriginal =
                ofertaDAO.obtenerPorId(oferta.getId());

        // Mantener la fecha de publicación original
        if (ofertaOriginal != null) {

            oferta.setFechaPublicacion(
                    ofertaOriginal.getFechaPublicacion()
            );
        }

        boolean exito = ofertaDAO.actualizar(oferta);

        HttpSession session = request.getSession();
        if (exito) {
            session.setAttribute("mensajeExito", "Oferta actualizada correctamente.");
        } else {
            session.setAttribute("mensajeError", "No se pudo actualizar la oferta.");
        }
        response.sendRedirect(request.getContextPath() + "/ofertas?format=jsp&accion=detalle&id=" + oferta.getId());
    }

    // ==================== MVC: Eliminar (POST) ====================

    private void eliminarOfertaJsp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer id = parsearEntero(request, "id");
        HttpSession session = request.getSession();

        if (id == null) {
            session.setAttribute("mensajeError", "ID de oferta no valido.");
            response.sendRedirect(request.getContextPath() + "/ofertas?format=jsp");
            return;
        }

        boolean exito = ofertaDAO.eliminar(id);
        if (exito) {
            session.setAttribute("mensajeExito", "Oferta eliminada correctamente.");
        } else {
            session.setAttribute("mensajeError", "No se pudo eliminar la oferta.");
        }
        response.sendRedirect(request.getContextPath() + "/ofertas?format=jsp");
    }

    // ==================== Validaciones (EM-18) ====================

    private List<String> validarCampos(HttpServletRequest request, boolean esEdicion) {
        List<String> errores = new ArrayList<>();

        String titulo = obtenerParametro(request, "titulo");
        String descripcion = obtenerParametro(request, "descripcion");
        String ubicacion = obtenerParametro(request, "ubicacion");
        String tipoContrato = obtenerParametro(request, "tipo_contrato");
        String horario = obtenerParametro(request, "horario");
        String salarioStr = obtenerParametro(request, "salario");
        Integer empresaId = parsearEntero(request, "empresa_id");

        if (esEdicion) {
            Integer id = parsearEntero(request, "id");
            if (id == null) {
                errores.add("El ID de la oferta es obligatorio y debe ser numerico.");
            }
        }

        if (titulo == null || titulo.isEmpty()) {
            errores.add("El titulo es obligatorio.");
        } else if (titulo.length() > 200) {
            errores.add("El titulo no debe exceder 200 caracteres.");
        }

        if (descripcion == null || descripcion.isEmpty()) {
            errores.add("La descripcion es obligatoria.");
        }

        if (ubicacion == null || ubicacion.isEmpty()) {
            errores.add("La ubicacion es obligatoria.");
        } else if (ubicacion.length() > 150) {
            errores.add("La ubicacion no debe exceder 150 caracteres.");
        }

        if (tipoContrato == null || tipoContrato.isEmpty()) {
            errores.add("El tipo de contrato es obligatorio.");
        }

        if (horario == null || horario.isEmpty()) {
            errores.add("El horario es obligatorio.");
        } else if (horario.length() > 100) {
            errores.add("El horario no debe exceder 100 caracteres.");
        }

        if (salarioStr != null && !salarioStr.isEmpty()) {
            try {
                Double salario = Double.parseDouble(salarioStr);
                if (salario < 0) {
                    errores.add("El salario no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                errores.add("El salario debe ser un valor numerico valido.");
            }
        }

        if (empresaId == null || empresaId <= 0) {
            errores.add("El ID de la empresa es obligatorio y debe ser un numero positivo.");
        }

        return errores;
    }

    private OfertaEmpleo construirOfertaDesdeRequest(HttpServletRequest request, boolean esEdicion) {
        OfertaEmpleo oferta = new OfertaEmpleo();

        if (esEdicion) {
            oferta.setId(parsearEntero(request, "id"));
        }

        oferta.setTitulo(obtenerParametro(request, "titulo"));
        oferta.setDescripcion(obtenerParametro(request, "descripcion"));
        oferta.setRequisitos(obtenerParametro(request, "requisitos"));
        oferta.setUbicacion(obtenerParametro(request, "ubicacion"));
        oferta.setSalario(parsearDouble(request, "salario"));
        oferta.setTipoContrato(obtenerParametro(request, "tipo_contrato"));
        oferta.setHorario(obtenerParametro(request, "horario"));
        oferta.setFechaPublicacion(parsearFecha(request, "fecha_publicacion"));
        oferta.setEstado(obtenerParametro(request, "estado"));
        oferta.setEmpresaId(parsearEntero(request, "empresa_id"));

        return oferta;
    }

    // ==================== JSON: Listar ====================

    private void listarOfertasJson(HttpServletResponse response) throws IOException {
        List<OfertaEmpleo> ofertas = ofertaDAO.listarTodas();
        String json = convertirListaAJson(ofertas);
        enviarJson(response, 200, json);
    }

    // ==================== JSON: Buscar ====================

    private void buscarPorTituloJson(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String titulo = obtenerParametro(request, "titulo");
        if (titulo == null || titulo.isEmpty()) {
            enviarError(response, 400, "El parametro 'titulo' es requerido");
            return;
        }

        List<OfertaEmpleo> ofertas = ofertaDAO.buscarPorTitulo(titulo);
        String json = convertirListaAJson(ofertas);
        enviarJson(response, 200, json);
    }

    // ==================== JSON: Filtrar ====================

    private void filtrarPorUbicacionJson(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String ubicacion = obtenerParametro(request, "ubicacion");
        if (ubicacion == null || ubicacion.isEmpty()) {
            enviarError(response, 400, "El parametro 'ubicacion' es requerido");
            return;
        }

        List<OfertaEmpleo> ofertas = ofertaDAO.filtrarPorUbicacion(ubicacion);
        String json = convertirListaAJson(ofertas);
        enviarJson(response, 200, json);
    }

    // ==================== JSON: Detalle ====================

    private void obtenerDetalleJson(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Integer id = parsearEntero(request, "id");
        if (id == null) {
            enviarError(response, 400, "El parametro 'id' es requerido y debe ser numerico");
            return;
        }

        OfertaEmpleo oferta = ofertaDAO.obtenerPorId(id);
        if (oferta == null) {
            enviarError(response, 404, "No se encontro la oferta con id: " + id);
            return;
        }

        String empresaNombre = "";
        if (oferta.getEmpresaId() != null) {
            Empresa empresa = empresaDAO.obtenerPorId(oferta.getEmpresaId());
            if (empresa != null) {
                empresaNombre = empresa.getNombre();
            }
        }

        String json = convertirOfertaAJson(oferta, empresaNombre);
        enviarJson(response, 200, json);
    }

    // ==================== JSON: Registrar ====================

    private void registrarOfertaJson(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String titulo = obtenerParametro(request, "titulo");
        String descripcion = obtenerParametro(request, "descripcion");
        String ubicacion = obtenerParametro(request, "ubicacion");
        Integer empresaId = parsearEntero(request, "empresa_id");

        if (titulo == null || titulo.isEmpty()) {
            enviarError(response, 400, "El campo 'titulo' es obligatorio");
            return;
        }
        if (descripcion == null || descripcion.isEmpty()) {
            enviarError(response, 400, "El campo 'descripcion' es obligatorio");
            return;
        }
        if (ubicacion == null || ubicacion.isEmpty()) {
            enviarError(response, 400, "El campo 'ubicacion' es obligatorio");
            return;
        }
        if (empresaId == null) {
            enviarError(response, 400, "El campo 'empresa_id' es obligatorio y debe ser numerico");
            return;
        }

        OfertaEmpleo oferta = new OfertaEmpleo();
        oferta.setTitulo(titulo);
        oferta.setDescripcion(descripcion);
        oferta.setRequisitos(obtenerParametro(request, "requisitos"));
        oferta.setUbicacion(ubicacion);
        oferta.setSalario(parsearDouble(request, "salario"));
        oferta.setTipoContrato(obtenerParametro(request, "tipo_contrato"));
        oferta.setHorario(obtenerParametro(request, "horario"));
        oferta.setFechaPublicacion(parsearFecha(request, "fecha_publicacion"));
        oferta.setEstado(obtenerParametro(request, "estado"));
        oferta.setEmpresaId(empresaId);

        boolean exito = ofertaDAO.registrar(oferta);
        if (exito) {
            enviarJson(response, 201, "{\"mensaje\": \"Oferta registrada correctamente\"}");
        } else {
            enviarError(response, 500, "No se pudo registrar la oferta");
        }
    }

    // ==================== JSON: Actualizar ====================

    private void actualizarOfertaJson(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Integer id = parsearEntero(request, "id");
        String titulo = obtenerParametro(request, "titulo");
        String descripcion = obtenerParametro(request, "descripcion");
        String ubicacion = obtenerParametro(request, "ubicacion");
        Integer empresaId = parsearEntero(request, "empresa_id");

        if (id == null) {
            enviarError(response, 400, "El campo 'id' es obligatorio y debe ser numerico");
            return;
        }
        if (titulo == null || titulo.isEmpty()) {
            enviarError(response, 400, "El campo 'titulo' es obligatorio");
            return;
        }
        if (descripcion == null || descripcion.isEmpty()) {
            enviarError(response, 400, "El campo 'descripcion' es obligatorio");
            return;
        }
        if (ubicacion == null || ubicacion.isEmpty()) {
            enviarError(response, 400, "El campo 'ubicacion' es obligatorio");
            return;
        }
        if (empresaId == null) {
            enviarError(response, 400, "El campo 'empresa_id' es obligatorio y debe ser numerico");
            return;
        }

        OfertaEmpleo oferta = new OfertaEmpleo();
        oferta.setId(id);
        oferta.setTitulo(titulo);
        oferta.setDescripcion(descripcion);
        oferta.setRequisitos(obtenerParametro(request, "requisitos"));
        oferta.setUbicacion(ubicacion);
        oferta.setSalario(parsearDouble(request, "salario"));
        oferta.setTipoContrato(obtenerParametro(request, "tipo_contrato"));
        oferta.setHorario(obtenerParametro(request, "horario"));
        oferta.setFechaPublicacion(parsearFecha(request, "fecha_publicacion"));
        oferta.setEstado(obtenerParametro(request, "estado"));
        oferta.setEmpresaId(empresaId);

        boolean exito = ofertaDAO.actualizar(oferta);
        if (exito) {
            enviarJson(response, 200, "{\"mensaje\": \"Oferta actualizada correctamente\"}");
        } else {
            enviarError(response, 500, "No se pudo actualizar la oferta");
        }
    }

    // ==================== JSON: Eliminar ====================

    private void eliminarOfertaJson(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Integer id = parsearEntero(request, "id");
        if (id == null) {
            enviarError(response, 400, "El parametro 'id' es requerido y debe ser numerico");
            return;
        }

        boolean exito = ofertaDAO.eliminar(id);
        if (exito) {
            enviarJson(response, 200, "{\"mensaje\": \"Oferta eliminada correctamente\"}");
        } else {
            enviarError(response, 500, "No se pudo eliminar la oferta");
        }
    }

    // ==================== Metodos auxiliares JSON ====================

    private String convertirListaAJson(List<OfertaEmpleo> ofertas) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ofertas.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(convertirOfertaAJson(ofertas.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String convertirOfertaAJson(OfertaEmpleo o) {
        return convertirOfertaAJson(o, "");
    }

    private String convertirOfertaAJson(OfertaEmpleo o, String empresaNombre) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\": ").append(o.getId() != null ? o.getId() : "null").append(",");
        sb.append("\"titulo\": \"").append(escapeJson(o.getTitulo())).append("\",");
        sb.append("\"descripcion\": \"").append(escapeJson(o.getDescripcion())).append("\",");
        sb.append("\"requisitos\": \"").append(escapeJson(o.getRequisitos())).append("\",");
        sb.append("\"ubicacion\": \"").append(escapeJson(o.getUbicacion())).append("\",");
        sb.append("\"salario\": ").append(o.getSalario() != null ? o.getSalario() : "null").append(",");
        sb.append("\"tipoContrato\": \"").append(escapeJson(o.getTipoContrato())).append("\",");
        sb.append("\"horario\": \"").append(escapeJson(o.getHorario())).append("\",");
        sb.append("\"fechaPublicacion\": \"").append(o.getFechaPublicacion() != null ? o.getFechaPublicacion() : "").append("\",");
        sb.append("\"estado\": \"").append(escapeJson(o.getEstado())).append("\",");
        sb.append("\"empresaId\": ").append(o.getEmpresaId() != null ? o.getEmpresaId() : "null").append(",");
        sb.append("\"empresaNombre\": \"").append(escapeJson(empresaNombre)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private void enviarJson(HttpServletResponse response, int status, String json)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    private void enviarError(HttpServletResponse response, int status, String mensaje)
            throws IOException {
        String json = "{\"error\": \"" + escapeJson(mensaje) + "\"}";
        enviarJson(response, status, json);
    }

    // ==================== Metodos auxiliares generales ====================

    private String obtenerParametro(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor != null ? valor.trim() : null;
    }

    private Integer parsearEntero(HttpServletRequest request, String nombre) {
        String valor = obtenerParametro(request, nombre);
        if (valor == null || valor.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parsearDouble(HttpServletRequest request, String nombre) {
        String valor = obtenerParametro(request, nombre);
        if (valor == null || valor.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parsearFecha(HttpServletRequest request, String nombre) {
        String valor = obtenerParametro(request, nombre);
        if (valor == null || valor.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
