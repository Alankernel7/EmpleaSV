package com.empleasv.controller;

import com.empleasv.dao.OfertaEmpleoDAO;
import com.empleasv.model.OfertaEmpleo;
import com.empleasv.dao.EmpresaDAO;
import com.empleasv.dao.PerfilEmpresaDAO;
import com.empleasv.model.Empresa;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.util.SesionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
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
    private PerfilEmpresaDAO perfilEmpresaDAO;

    @Override
    public void init() throws ServletException {
        ofertaDAO = new OfertaEmpleoDAO();
        empresaDAO = new EmpresaDAO();
        perfilEmpresaDAO = new PerfilEmpresaDAO();
    }

    // ==================== Autorización y propiedad ====================

    /**
     * Devuelve el ID de la empresa propietaria de la cuenta autenticada.
     * Devuelve null para ADMINISTRADOR (puede operar con cualquier empresa)
     * y para cuentas sin empresa vinculada.
     */
    private Integer empresaPropia(HttpServletRequest request) {
        Usuario usuario = SesionUtil.usuario(request);
        if (usuario == null) {
            return null;
        }
        if (usuario.getRol() == UsuarioRol.ADMINISTRADOR) {
            return null;
        }
        return perfilEmpresaDAO.obtenerEmpresaIdPorUsuarioId(usuario.getId());
    }

    /**
     * Comprueba que la oferta pertenezca a la empresa de la sesión.
     * Un administrador puede operar sobre cualquier oferta.
     *
     * @return true si está permitido
     */
    private boolean puedeOperar(HttpServletRequest request, OfertaEmpleo oferta) {
        if (oferta == null) {
            return false;
        }
        Usuario usuario = SesionUtil.usuario(request);
        if (usuario == null) {
            return false;
        }
        if (usuario.getRol() == UsuarioRol.ADMINISTRADOR) {
            return true;
        }
        Integer propia = empresaPropia(request);
        return propia != null && propia.equals(oferta.getEmpresaId());
    }

    /**
     * Verifica la propiedad de la oferta y, si no corresponde,
     * redirige al panel con un mensaje de error.
     *
     * @return true si el usuario puede continuar
     */
    private boolean verificarPropiedad(HttpServletRequest request,
                                        HttpServletResponse response,
                                        OfertaEmpleo oferta)
            throws IOException {
        if (puedeOperar(request, oferta)) {
            return true;
        }
        HttpSession session = request.getSession();
        session.setAttribute("mensajeError",
                "No tienes permiso para modificar esta oferta.");
        if (SesionUtil.esRol(request, UsuarioRol.EMPRESA)) {
            response.sendRedirect(request.getContextPath() + "/panel-empresa");
        } else {
            response.sendRedirect(request.getContextPath() + "/ofertas?format=jsp");
        }
        return false;
    }

    /**
     * Para cuentas EMPRESA garantiza que exista la empresa vinculada.
     * Si no existe, redirige a completar el perfil empresarial.
     */
    private Integer resolverEmpresaPropia(HttpServletRequest request,
                                           HttpServletResponse response)
            throws IOException {
        Integer empresaId = empresaPropia(request);
        if (empresaId == null) {
            HttpSession session = request.getSession();
            session.setAttribute("mensajeError",
                    "Debes completar tu perfil empresarial antes de publicar ofertas.");
            response.sendRedirect(request.getContextPath()
                    + "/perfil-empresa?accion=completar");
            return null;
        }
        return empresaId;
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

        List<OfertaEmpleo> ofertas;
        boolean soloMias = "1".equals(request.getParameter("mias"));

        if (soloMias) {
            ofertas = ofertasPropias(request, response);
            if (ofertas == null) {
                return;
            }
            request.setAttribute("soloMias", Boolean.TRUE);
        } else {
            ofertas = ofertaDAO.listarTodas();
        }

        request.setAttribute("ofertas", ofertas);
        SesionUtil.consumirMensajes(request);

        request.getRequestDispatcher("/WEB-INF/views/listar.jsp").forward(request, response);
    }

    /**
     * Devuelve las ofertas de la empresa de la sesión para "Mis ofertas".
     * Devuelve null cuando ya se envió una redirección (sin sesión, rol sin
     * empresa vinculada o cuenta sin permisos). Un administrador sin empresa
     * propia ve el listado general.
     */
    private List<OfertaEmpleo> ofertasPropias(HttpServletRequest request,
                                              HttpServletResponse response)
            throws IOException {

        Usuario usuario = SesionUtil.usuario(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath()
                    + "/auth?accion=login&restringido=1");
            return null;
        }

        if (usuario.getRol() == UsuarioRol.ADMINISTRADOR) {
            return ofertaDAO.listarTodas();
        }

        if (usuario.getRol() != UsuarioRol.EMPRESA) {
            response.sendRedirect(request.getContextPath() + "/ofertas?format=jsp");
            return null;
        }

        Integer empresaId = empresaPropia(request);
        if (empresaId == null) {
            request.getSession().setAttribute("mensajeError",
                    "Debes completar tu perfil empresarial antes de ver tus ofertas.");
            response.sendRedirect(request.getContextPath()
                    + "/perfil-empresa?accion=completar");
            return null;
        }

        return ofertaDAO.listarPorEmpresa(empresaId);
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
        SesionUtil.consumirMensajes(request);
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
        SesionUtil.consumirMensajes(request);
        request.getRequestDispatcher("/WEB-INF/views/filtrar.jsp").forward(request, response);
    }

    // ==================== MVC: Detalle ====================

    private void obtenerDetalleJsp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SesionUtil.consumirMensajes(request);
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

        SesionUtil.consumirMensajes(request);
        Usuario usuario = SesionUtil.usuario(request);
        List<Empresa> empresas;

        if (usuario != null && usuario.getRol() == UsuarioRol.EMPRESA) {
            // La cuenta solo puede publicar en su propia empresa
            Integer empresaId = resolverEmpresaPropia(request, response);
            if (empresaId == null) {
                return;
            }
            Empresa propia = empresaDAO.obtenerPorId(empresaId);
            empresas = propia != null ? List.of(propia) : new ArrayList<>();
            request.setAttribute("empresaPropiaId", empresaId);
        } else {
            empresas = empresaDAO.listarTodas();
        }

        request.setAttribute("empresas", empresas);

        request.getRequestDispatcher(
                "/WEB-INF/views/registrar.jsp"
        ).forward(request, response);    }

    // ==================== MVC: Formulario Edicion ====================

    private void mostrarFormularioEdicion(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        SesionUtil.consumirMensajes(request);
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

        // Solo el dueño de la oferta (o un administrador) puede editarla
        if (!verificarPropiedad(request, response, oferta)) {
            return;
        }

        Usuario usuario = SesionUtil.usuario(request);
        List<Empresa> empresas;

        if (usuario != null && usuario.getRol() == UsuarioRol.EMPRESA) {
            Integer empresaId = empresaPropia(request);
            Empresa propia = empresaId != null ? empresaDAO.obtenerPorId(empresaId) : null;
            empresas = propia != null ? List.of(propia) : new ArrayList<>();
            request.setAttribute("empresaPropiaId", empresaId);
        } else {
            empresas = empresaDAO.listarTodas();
        }

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

        Integer empresaForzada = empresaForzadaParaEscritura(request, response);
        if (empresaForzada == null && esCuentaEmpresa(request)) {
            return;
        }

        List<String> errores = validarCampos(request, false, empresaForzada);
        if (!errores.isEmpty()) {
            request.setAttribute("errores", errores);
            prepararEmpresasParaFormulario(request, empresaForzada);

            request.getRequestDispatcher("/WEB-INF/views/registrar.jsp").forward(request, response);
            return;
        }

        OfertaEmpleo oferta = construirOfertaDesdeRequest(request, false, empresaForzada);

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

        Integer id = parsearEntero(request, "id");

        OfertaEmpleo ofertaExistente = (id != null) ? ofertaDAO.obtenerPorId(id) : null;
        if (ofertaExistente == null) {
            HttpSession session = request.getSession();
            session.setAttribute("mensajeError", "No se encontro la oferta indicada.");
            response.sendRedirect(request.getContextPath() + "/ofertas?format=jsp");
            return;
        }

        if (!verificarPropiedad(request, response, ofertaExistente)) {
            return;
        }

        Integer empresaForzada = esCuentaEmpresa(request) ? empresaPropia(request) : null;

        List<String> errores = validarCampos(request, true, empresaForzada);
        if (!errores.isEmpty()) {
            request.setAttribute("oferta", ofertaExistente);
            prepararEmpresasParaFormulario(request, empresaForzada);
            request.setAttribute("errores", errores);

            request.getRequestDispatcher(
                    "/WEB-INF/views/editar.jsp"
            ).forward(request, response);
            return;
        }

        OfertaEmpleo oferta =
                construirOfertaDesdeRequest(request, true, empresaForzada);

        // Mantener la fecha de publicación original
        oferta.setFechaPublicacion(ofertaExistente.getFechaPublicacion());

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

        OfertaEmpleo oferta = ofertaDAO.obtenerPorId(id);
        if (oferta == null) {
            session.setAttribute("mensajeError", "No se encontro la oferta indicada.");
            response.sendRedirect(request.getContextPath() + "/ofertas?format=jsp");
            return;
        }

        if (!verificarPropiedad(request, response, oferta)) {
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

    private List<String> validarCampos(HttpServletRequest request, boolean esEdicion,
                                       Integer empresaForzada) {
        List<String> errores = new ArrayList<>();

        String titulo = obtenerParametro(request, "titulo");
        String descripcion = obtenerParametro(request, "descripcion");
        String ubicacion = obtenerParametro(request, "ubicacion");
        String tipoContrato = obtenerParametro(request, "tipo_contrato");
        String horario = obtenerParametro(request, "horario");
        String salarioStr = obtenerParametro(request, "salario");
        Integer empresaId = empresaForzada != null
                ? empresaForzada : parsearEntero(request, "empresa_id");

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
                BigDecimal salario = new BigDecimal(salarioStr);
                if (salario.compareTo(BigDecimal.ZERO) < 0) {
                    errores.add("El salario no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                errores.add("El salario debe ser un valor numerico valido.");
            }
        }

        String estado = obtenerParametro(request, "estado");
        if (estado == null || estado.isEmpty()) {
            errores.add("Debe seleccionar un estado para la oferta.");
        } else if (!ESTADOS_VALIDOS.contains(estado)) {
            errores.add("El estado seleccionado no es valido.");
        }

        if (empresaId == null || empresaId <= 0) {
            errores.add("Debe existir una empresa asociada a la oferta.");
        }

        return errores;
    }

    /** Estados admitidos por la oferta de empleo. */
    private static final List<String> ESTADOS_VALIDOS =
            List.of("ACTIVA", "PAUSADA", "CERRADA");

    /**
     * Devuelve el estado enviado por el cliente y, si viene vacio o ausente,
     * el indicado como respaldo (evita violar la restriccion NOT NULL).
     */
    private String resolverEstado(String enviado, String respaldo) {
        if (enviado == null || enviado.isEmpty()) {
            return respaldo;
        }
        return ESTADOS_VALIDOS.contains(enviado) ? enviado : respaldo;
    }

    private OfertaEmpleo construirOfertaDesdeRequest(HttpServletRequest request,
                                                     boolean esEdicion,
                                                     Integer empresaForzada) {
        OfertaEmpleo oferta = new OfertaEmpleo();

        if (esEdicion) {
            oferta.setId(parsearEntero(request, "id"));
        }

        oferta.setTitulo(obtenerParametro(request, "titulo"));
        oferta.setDescripcion(obtenerParametro(request, "descripcion"));
        oferta.setRequisitos(obtenerParametro(request, "requisitos"));
        oferta.setUbicacion(obtenerParametro(request, "ubicacion"));
        oferta.setSalario(parsearBigDecimal(request, "salario"));
        oferta.setTipoContrato(obtenerParametro(request, "tipo_contrato"));
        oferta.setHorario(obtenerParametro(request, "horario"));
        oferta.setFechaPublicacion(parsearFecha(request, "fecha_publicacion"));
        oferta.setEstado(obtenerParametro(request, "estado"));

        // La empresa se resuelve en el servidor: nunca se confía en un ID
        // enviado libremente por el navegador.
        Integer empresaId = empresaForzada != null
                ? empresaForzada : parsearEntero(request, "empresa_id");
        Empresa empresa = (empresaId != null) ? empresaDAO.obtenerPorId(empresaId) : null;
        oferta.setEmpresa(empresa);

        return oferta;
    }

    // ==================== Auxiliares de autorización ====================

    private boolean esCuentaEmpresa(HttpServletRequest request) {
        Usuario usuario = SesionUtil.usuario(request);
        return usuario != null && usuario.getRol() == UsuarioRol.EMPRESA;
    }

    /**
     * Resuelve la empresa que debe quedar asociada a la oferta al escribir.
     * Para EMPRESA siempre será la suya; para ADMINISTRADOR se toma del formulario.
     *
     * @return ID de empresa o null si no aplica / no está disponible
     */
    private Integer empresaForzadaParaEscritura(HttpServletRequest request,
                                                HttpServletResponse response)
            throws IOException {
        if (esCuentaEmpresa(request)) {
            return resolverEmpresaPropia(request, response);
        }
        return null;
    }

    /**
     * Carga la lista de empresas para el formulario según el rol.
     */
    private void prepararEmpresasParaFormulario(HttpServletRequest request,
                                                Integer empresaForzada) {
        if (esCuentaEmpresa(request)) {
            Empresa propia = empresaForzada != null
                    ? empresaDAO.obtenerPorId(empresaForzada) : null;
            request.setAttribute("empresas",
                    propia != null ? List.of(propia) : new ArrayList<Empresa>());
            request.setAttribute("empresaPropiaId", empresaForzada);
        } else {
            request.setAttribute("empresas", empresaDAO.listarTodas());
        }
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

        Integer empresaId;
        Usuario usuarioJson = SesionUtil.usuario(request);
        if (usuarioJson != null && usuarioJson.getRol() == UsuarioRol.ADMINISTRADOR) {
            empresaId = parsearEntero(request, "empresa_id");
            if (empresaId == null) {
                enviarError(response, 400, "El campo 'empresa_id' es obligatorio y debe ser numerico");
                return;
            }
        } else {
            empresaId = usuarioJson != null
                    ? perfilEmpresaDAO.obtenerEmpresaIdPorUsuarioId(usuarioJson.getId()) : null;
            if (empresaId == null) {
                enviarError(response, 403,
                        "Completa tu perfil empresarial antes de publicar ofertas");
                return;
            }
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

        OfertaEmpleo oferta = new OfertaEmpleo();
        oferta.setTitulo(titulo);
        oferta.setDescripcion(descripcion);
        oferta.setRequisitos(obtenerParametro(request, "requisitos"));
        oferta.setUbicacion(ubicacion);
        oferta.setSalario(parsearBigDecimal(request, "salario"));
        oferta.setTipoContrato(obtenerParametro(request, "tipo_contrato"));
        oferta.setHorario(obtenerParametro(request, "horario"));
        // fecha_publicacion y estado son NOT NULL: se completan en el servidor
        LocalDateTime fecha = parsearFecha(request, "fecha_publicacion");
        oferta.setFechaPublicacion(fecha != null ? fecha : LocalDateTime.now());
        oferta.setEstado(resolverEstado(obtenerParametro(request, "estado"), "ACTIVA"));
        oferta.setEmpresa(empresaDAO.obtenerPorId(empresaId));

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

        if (id == null) {
            enviarError(response, 400, "El campo 'id' es obligatorio y debe ser numerico");
            return;
        }

        OfertaEmpleo existente = ofertaDAO.obtenerPorId(id);
        if (existente == null) {
            enviarError(response, 404, "No se encontro la oferta con id: " + id);
            return;
        }
        if (!puedeOperar(request, existente)) {
            enviarError(response, 403, "No autorizado para modificar esta oferta");
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

        Integer empresaId = esCuentaEmpresa(request)
                ? empresaPropia(request) : parsearEntero(request, "empresa_id");
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
        oferta.setSalario(parsearBigDecimal(request, "salario"));
        oferta.setTipoContrato(obtenerParametro(request, "tipo_contrato"));
        oferta.setHorario(obtenerParametro(request, "horario"));
        // Se conservan los valores originales si el cliente no los envia
        LocalDateTime fecha = parsearFecha(request, "fecha_publicacion");
        oferta.setFechaPublicacion(fecha != null ? fecha : existente.getFechaPublicacion());
        oferta.setEstado(resolverEstado(obtenerParametro(request, "estado"),
                existente.getEstado()));
        oferta.setEmpresa(empresaDAO.obtenerPorId(empresaId));

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

        OfertaEmpleo existente = ofertaDAO.obtenerPorId(id);
        if (existente == null) {
            enviarError(response, 404, "No se encontro la oferta con id: " + id);
            return;
        }
        if (!puedeOperar(request, existente)) {
            enviarError(response, 403, "No autorizado para eliminar esta oferta");
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

    private BigDecimal parsearBigDecimal(HttpServletRequest request, String nombre) {
        String valor = obtenerParametro(request, nombre);
        if (valor == null || valor.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(valor);
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
