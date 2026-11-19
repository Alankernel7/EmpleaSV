package com.empleasv.service;

import com.empleasv.model.Auditoria;
import com.empleasv.model.Empresa;
import com.empleasv.model.OfertaEmpleo;
import com.empleasv.model.PerfilEmpresa;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.repository.AuditoriaRepository;
import com.empleasv.repository.EmpresaRepository;
import com.empleasv.repository.OfertaEmpleoRepository;
import com.empleasv.repository.PerfilEmpresaRepository;
import com.empleasv.repository.UsuarioRepository;

import java.util.List;
import java.util.Objects;

/**
 * Lógica de negocio del panel administrativo.
 * Centraliza las reglas de seguridad de activación/desactivación
 * y el registro de auditoría. No hay borrado físico en este panel.
 */
public class AdminService {

    /** Estados válidos de una cuenta de usuario. */
    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_INACTIVO = "INACTIVO";

    /** Estados válidos de una oferta de empleo. */
    private static final List<String> ESTADOS_OFERTA = List.of("ACTIVA", "PAUSADA", "CERRADA");

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final OfertaEmpleoRepository ofertaRepository;
    private final PerfilEmpresaRepository perfilEmpresaRepository;
    private final AuditoriaRepository auditoriaRepository;

    public AdminService() {
        this.usuarioRepository = new UsuarioRepository();
        this.empresaRepository = new EmpresaRepository();
        this.ofertaRepository = new OfertaEmpleoRepository();
        this.perfilEmpresaRepository = new PerfilEmpresaRepository();
        this.auditoriaRepository = new AuditoriaRepository();
    }

    // =========================================================
    // Indicadores
    // =========================================================

    /** Usuarios registrados (cualquier rol/estado). */
    public long totalUsuarios() {
        return usuarioRepository.contarTodos();
    }

    /** Cuentas candidato (rol USUARIO) activas. */
    public long candidatosActivos() {
        return usuarioRepository.contarPorRol(UsuarioRol.USUARIO, ESTADO_ACTIVO);
    }

    /** Cuentas empresa (rol EMPRESA) activas. */
    public long empresasActivas() {
        return usuarioRepository.contarPorRol(UsuarioRol.EMPRESA, ESTADO_ACTIVO);
    }

    /** Administradores activos. */
    public long administradoresActivos() {
        return usuarioRepository.contarPorRol(UsuarioRol.ADMINISTRADOR, ESTADO_ACTIVO);
    }

    /** Ofertas por estado (null = total). */
    public long contarOfertas(String estado) {
        return ofertaRepository.contarPorEstado(estado);
    }

    /** Empresas registradas con su cantidad de ofertas. */
    public List<Empresa> listarEmpresasConOfertas() {
        return empresaRepository.listarConCantidadOfertas();
    }

    /** Últimos usuarios registrados. */
    public List<Usuario> listarUsuariosRecientes(int limite) {
        return usuarioRepository.listarRecientes(limite);
    }

    /** Últimas ofertas publicadas. */
    public List<OfertaEmpleo> listarOfertasRecientes(int limite) {
        return ofertaRepository.listarRecientes(limite);
    }

    /** Últimos registros de auditoría. */
    public List<Auditoria> listarAuditoriaReciente(int limite) {
        return auditoriaRepository.listarUltimos(limite);
    }

    // =========================================================
    // Usuarios
    // =========================================================

    /** Busca y filtra usuarios para la tabla administrativa. */
    public List<Usuario> buscarUsuarios(String texto, String rol, String estado) {
        return usuarioRepository.buscarFiltrado(texto, parsearRol(rol), normalizarEstado(estado));
    }

    /** Obtiene un usuario por ID (sin exponer el hash fuera de la capa de servicio). */
    public Usuario obtenerUsuario(Integer id) {
        return usuarioRepository.obtenerPorId(id);
    }

    /**
     * Activa o desactiva una cuenta de usuario.
     *
     * @param adminId     ID del administrador que ejecuta la acción
     * @param objetivoId  ID de la cuenta objetivo
     * @param nuevoEstado ACTIVO o INACTIVO
     * @return mensaje de éxito
     * @throws AdminException si la operación viola una regla de seguridad
     */
    public String cambiarEstadoUsuario(Integer adminId, Integer objetivoId, String nuevoEstado)
            throws AdminException {
        if (adminId == null) {
            throw new AdminException("Sesión de administrador inválida.");
        }
        if (objetivoId == null) {
            throw new AdminException("Usuario no especificado.");
        }
        String estado = normalizarEstado(nuevoEstado);
        if (!ESTADO_ACTIVO.equals(estado) && !ESTADO_INACTIVO.equals(estado)) {
            throw new AdminException("Estado inválido.");
        }
        if (Objects.equals(adminId, objetivoId)) {
            throw new AdminException("No puedes desactivar tu propia cuenta de administrador.");
        }
        Usuario objetivo = usuarioRepository.obtenerPorId(objetivoId);
        if (objetivo == null) {
            throw new AdminException("El usuario indicado no existe.");
        }
        if (estado.equals(ESTADO_INACTIVO)
                && objetivo.getRol() == UsuarioRol.ADMINISTRADOR
                && ESTADO_ACTIVO.equals(objetivo.getEstado())
                && administradoresActivos() <= 1) {
            throw new AdminException("No se puede desactivar el último administrador activo.");
        }
        if (estado.equals(objetivo.getEstado())) {
            return "El usuario ya se encuentra en estado " + estado + ".";
        }

        objetivo.setEstado(estado);
        usuarioRepository.actualizar(objetivo);
        registrarAuditoria(adminId,
                ESTADO_ACTIVO.equals(estado) ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO",
                Auditoria.TipoObjetivo.USUARIO,
                objetivo.getId(),
                "Cuenta de " + objetivo.getEmail() + " (" + objetivo.getRol() + ") -> " + estado);
        return "Usuario " + objetivo.getEmail() + " ahora está " + estado + ".";
    }

    // =========================================================
    // Ofertas
    // =========================================================

    /** Busca y filtra ofertas para la tabla administrativa. */
    public List<OfertaEmpleo> buscarOfertas(String texto, String estado) {
        String est = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;
        if (est != null && !ESTADOS_OFERTA.contains(est)) {
            est = null;
        }
        return ofertaRepository.buscarAdministrativa(texto, est);
    }

    /** Obtiene una oferta por ID. */
    public OfertaEmpleo obtenerOferta(Integer id) {
        return ofertaRepository.obtenerPorId(id);
    }

    /**
     * Cambia el estado de una oferta (ACTIVA/PAUSADA/CERRADA).
     * No borra la oferta; solo modifica su estado.
     *
     * @param adminId    ID del administrador
     * @param ofertaId   ID de la oferta
     * @param nuevoEstado nuevo estado
     * @return mensaje de éxito
     * @throws AdminException si algo falla
     */
    public String cambiarEstadoOferta(Integer adminId, Integer ofertaId, String nuevoEstado)
            throws AdminException {
        if (adminId == null) {
            throw new AdminException("Sesión de administrador inválida.");
        }
        if (ofertaId == null) {
            throw new AdminException("Oferta no especificada.");
        }
        String estado = (nuevoEstado == null) ? "" : nuevoEstado.trim().toUpperCase();
        if (!ESTADOS_OFERTA.contains(estado)) {
            throw new AdminException("Estado de oferta inválido.");
        }
        OfertaEmpleo oferta = ofertaRepository.obtenerPorId(ofertaId);
        if (oferta == null) {
            throw new AdminException("La oferta indicada no existe.");
        }
        if (estado.equals(oferta.getEstado())) {
            return "La oferta ya se encuentra en estado " + estado + ".";
        }
        String anterior = oferta.getEstado();
        oferta.setEstado(estado);
        if (!ofertaRepository.actualizar(oferta)) {
            throw new AdminException("No se pudo actualizar la oferta.");
        }
        registrarAuditoria(adminId, "CAMBIAR_ESTADO_OFERTA",
                Auditoria.TipoObjetivo.OFERTA,
                oferta.getId(),
                "\"" + oferta.getTitulo() + "\": " + anterior + " -> " + estado);
        return "Oferta actualizada a " + estado + ".";
    }

    // =========================================================
    // Empresas
    // =========================================================

    /** Perfil empresarial de un usuario EMPRESA (puede ser null). */
    public PerfilEmpresa obtenerPerfilEmpresa(Integer usuarioId) {
        return perfilEmpresaRepository.obtenerPorUsuarioId(usuarioId);
    }

    /** Ofertas de una empresa. */
    public List<OfertaEmpleo> listarOfertasEmpresa(Integer empresaId) {
        return ofertaRepository.listarPorEmpresa(empresaId);
    }

    /** Cuenta de una empresa vinculada al perfil (para activar/desactivar su cuenta). */
    public Usuario obtenerUsuarioDeEmpresa(PerfilEmpresa perfil) {
        if (perfil == null || perfil.getUsuario() == null) {
            return null;
        }
        return usuarioRepository.obtenerPorId(perfil.getUsuario().getId());
    }

    /**
     * IDs de empresas cuya cuenta responsable está desactivada.
     * Permite marcar en el listado las ofertas publicadas por
     * empresas inactivas.
     *
     * @return conjunto de IDs de empresa con cuenta INACTIVA
     */
    public java.util.Set<Integer> idsEmpresasCuentaInactiva() {
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        List<Usuario> inactivas = usuarioRepository.buscarFiltrado(
                null, UsuarioRol.EMPRESA, ESTADO_INACTIVO);
        for (Usuario cuenta : inactivas) {
            PerfilEmpresa perfil = perfilEmpresaRepository.obtenerPorUsuarioId(cuenta.getId());
            if (perfil != null && perfil.getEmpresa() != null) {
                ids.add(perfil.getEmpresa().getId());
            }
        }
        return ids;
    }

    // =========================================================
    // Auditoría
    // =========================================================

    /**
     * Registra la acción administrativa. Nunca guarda contraseñas ni hashes.
     */
    public void registrarAuditoria(Integer adminId, String accion,
                                   Auditoria.TipoObjetivo tipo, Integer objetivoId,
                                   String detalle) {
        try {
            Usuario admin = usuarioRepository.obtenerPorId(adminId);
            if (admin == null) {
                return;
            }
            auditoriaRepository.guardar(new Auditoria(admin, accion, tipo, objetivoId, detalle));
        } catch (RuntimeException e) {
            // La auditoría no debe bloquear la operación principal;
            // el error queda en logs del servidor.
            System.err.println("[AUDITORIA] No se pudo registrar: " + accion + " - " + e.getMessage());
        }
    }

    // =========================================================
    // Utilidades
    // =========================================================

    private UsuarioRol parsearRol(String rol) {
        if (rol == null || rol.trim().isEmpty()) {
            return null;
        }
        try {
            return UsuarioRol.valueOf(rol.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return null;
        }
        String est = estado.trim().toUpperCase();
        return ESTADO_ACTIVO.equals(est) || ESTADO_INACTIVO.equals(est) ? est : null;
    }

    /** Excepción de negocio para reglas administrativas. */
    public static class AdminException extends Exception {
        public AdminException(String mensaje) {
            super(mensaje);
        }
    }
}
