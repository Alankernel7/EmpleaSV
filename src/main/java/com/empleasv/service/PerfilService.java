package com.empleasv.service;

import com.empleasv.config.JpaConfig;
import com.empleasv.model.Archivo;
import com.empleasv.model.Empresa;
import com.empleasv.model.PerfilCandidato;
import com.empleasv.model.PerfilEmpresa;
import com.empleasv.model.Usuario;
import com.empleasv.repository.ArchivoRepository;
import com.empleasv.repository.PerfilCandidatoRepository;
import com.empleasv.repository.PerfilEmpresaRepository;
import com.empleasv.util.ArchivoNoValidoException;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de perfiles: actualización de datos personales y empresariales,
 * vinculación con la tabla empresa y gestión segura de archivos
 * (fotografía, currículum y logotipo).
 */
public class PerfilService {

    private final PerfilCandidatoRepository perfilCandidatoRepository = new PerfilCandidatoRepository();
    private final PerfilEmpresaRepository perfilEmpresaRepository = new PerfilEmpresaRepository();
    private final ArchivoRepository archivoRepository = new ArchivoRepository();
    private final ArchivoService archivoService;

    public PerfilService(ServletContext context) {
        this.archivoService = new ArchivoService(context);
    }

    // ==================== Perfil del candidato ====================

    /**
     * Actualiza los datos del candidato. Verifica la unicidad del correo
     * excluyendo la propia cuenta.
     *
     * @return usuario actualizado (para refrescar la sesión)
     */
    public Usuario actualizarCandidato(Integer usuarioId, String nombre, String email,
                                       String telefono, String direccion,
                                       String municipio, String departamento)
            throws RegistroException {

        validarNombre(nombre);
        validarEmail(email, usuarioId);

        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();

            Usuario usuario = em.find(Usuario.class, usuarioId);
            if (usuario == null) {
                throw new RegistroException("La cuenta no existe.");
            }
            usuario.setNombre(nombre.trim());
            usuario.setEmail(email.trim());

            PerfilCandidato perfil = perfilCandidatoRepository.buscarPorUsuario(em, usuarioId);
            if (perfil == null) {
                perfil = new PerfilCandidato();
                perfil.setUsuario(usuario);
                perfil.setFechaActualizacion(LocalDateTime.now());
                em.persist(perfil);
            }
            perfil.setTelefono(vacio(telefono) ? null : telefono.trim());
            perfil.setDireccionResidencia(vacio(direccion) ? null : direccion.trim());
            perfil.setMunicipio(vacio(municipio) ? null : municipio.trim());
            perfil.setDepartamento(vacio(departamento) ? null : departamento.trim());
            perfil.setFechaActualizacion(LocalDateTime.now());

            em.getTransaction().commit();
            return usuario;
        } catch (RegistroException e) {
            revertir(em);
            throw e;
        } catch (Exception e) {
            revertir(em);
            throw new RegistroException("No se pudo actualizar el perfil.");
        } finally {
            em.close();
        }
    }

    /**
     * Devuelve el perfil del candidato, creándolo si no existe todavía.
     */
    public PerfilCandidato obtenerOCrearCandidato(Integer usuarioId) {
        PerfilCandidato perfil = perfilCandidatoRepository.obtenerPorUsuarioId(usuarioId);
        if (perfil == null) {
            perfil = perfilCandidatoRepository.crearSiNoExiste(usuarioId);
            perfil = perfilCandidatoRepository.obtenerPorUsuarioId(usuarioId);
        }
        return perfil;
    }

    // ==================== Perfil empresarial ====================

    /**
     * Crea o actualiza el perfil empresarial y sincroniza el registro
     * de la tabla empresa usado por las ofertas.
     */
    public PerfilEmpresa actualizarEmpresa(Integer usuarioId, String nombreResponsable,
                                           String email, String nombreEmpresa, String nit,
                                           String telefonoEmpresa, String direccionEmpresa,
                                           String municipio, String departamento,
                                           String sector, String sitioWeb, String descripcion)
            throws RegistroException {

        validarNombre(nombreResponsable);
        validarEmail(email, usuarioId);

        if (vacio(nombreEmpresa)) {
            throw new RegistroException("El nombre legal o comercial de la empresa es obligatorio");
        }

        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();

            Usuario usuario = em.find(Usuario.class, usuarioId);
            if (usuario == null) {
                throw new RegistroException("La cuenta no existe.");
            }
            usuario.setNombre(nombreResponsable.trim());
            usuario.setEmail(email.trim());

            PerfilEmpresa perfil = perfilEmpresaRepository.buscarPorUsuario(em, usuarioId);
            if (perfil == null) {
                perfil = new PerfilEmpresa();
                perfil.setUsuario(usuario);
                perfil.setFechaActualizacion(LocalDateTime.now());
                em.persist(perfil);
            }

            perfil.setNombreResponsable(nombreResponsable.trim());
            perfil.setNombreEmpresa(nombreEmpresa.trim());
            perfil.setNit(vacio(nit) ? null : nit.trim());
            perfil.setTelefonoEmpresa(vacio(telefonoEmpresa) ? null : telefonoEmpresa.trim());
            perfil.setDireccionEmpresa(vacio(direccionEmpresa) ? null : direccionEmpresa.trim());
            perfil.setMunicipio(vacio(municipio) ? null : municipio.trim());
            perfil.setDepartamento(vacio(departamento) ? null : departamento.trim());
            perfil.setSector(vacio(sector) ? null : sector.trim());
            perfil.setSitioWeb(vacio(sitioWeb) ? null : sitioWeb.trim());
            perfil.setDescripcion(vacio(descripcion) ? null : descripcion.trim());
            perfil.setFechaActualizacion(LocalDateTime.now());

            Empresa empresa = perfil.getEmpresa();
            if (empresa == null) {
                empresa = crearRegistroEmpresa(em, usuario, perfil);
                perfil.setEmpresa(empresa);
            } else {
                empresa.setNombre(perfil.getNombreEmpresa());
                empresa.setCategoria(vacio(perfil.getSector()) ? "General" : perfil.getSector());
                empresa.setTelefono(perfil.getTelefonoEmpresa());
                empresa.setDescripcion(perfil.getDescripcion());
            }

            em.getTransaction().commit();
            return perfil;
        } catch (RegistroException e) {
            revertir(em);
            throw e;
        } catch (Exception e) {
            revertir(em);
            throw new RegistroException("No se pudo actualizar el perfil empresarial.");
        } finally {
            em.close();
        }
    }

    /**
     * Devuelve el perfil empresarial, creándolo si la cuenta aún no tiene uno.
     */
    public PerfilEmpresa obtenerOCrearEmpresa(Integer usuarioId) {
        PerfilEmpresa perfil = perfilEmpresaRepository.obtenerPorUsuarioId(usuarioId);
        if (perfil == null) {
            perfilEmpresaRepository.crearSiNoExiste(usuarioId);
            perfil = perfilEmpresaRepository.obtenerPorUsuarioId(usuarioId);
        }
        return perfil;
    }

    /**
     * Garantiza que la cuenta empresarial tenga un registro en la tabla empresa
     * (necesario para publicar ofertas). Devuelve el ID de la empresa o null
     * si la cuenta no tiene perfil empresarial.
     */
    public Integer asegurarEmpresaVinculada(Integer usuarioId) {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();

            PerfilEmpresa perfil = perfilEmpresaRepository.buscarPorUsuario(em, usuarioId);
            if (perfil == null) {
                em.getTransaction().rollback();
                return null;
            }

            Empresa empresa = perfil.getEmpresa();
            if (empresa == null) {
                empresa = crearRegistroEmpresa(em, perfil.getUsuario(), perfil);
                perfil.setEmpresa(empresa);
                perfil.setFechaActualizacion(LocalDateTime.now());
            }

            Integer empresaId = empresa.getId();
            em.getTransaction().commit();
            return empresaId;
        } catch (Exception e) {
            revertir(em);
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Devuelve el ID de la empresa vinculada a la cuenta sin crear nada.
     */
    public Integer obtenerEmpresaId(Integer usuarioId) {
        return perfilEmpresaRepository.obtenerEmpresaIdPorUsuarioId(usuarioId);
    }

    // ==================== Archivos del perfil ====================

    /** Sube o reemplaza la fotografía de perfil del candidato. */
    public Archivo subirFoto(Usuario usuario, Part parte)
            throws ArchivoNoValidoException, RegistroException {
        return reemplazarArchivoCandidato(usuario, Archivo.CategoriaArchivo.FOTO_PERFIL, parte);
    }

    /** Elimina la fotografía de perfil. */
    public void eliminarFoto(Integer usuarioId) throws RegistroException {
        reemplazarArchivoCandidato(usuarioId, Archivo.CategoriaArchivo.FOTO_PERFIL, null);
    }

    /**
     * Sube un currículum nuevo. Si la cuenta todavía no tenía un currículum
     * principal, el archivo recién subido pasa a ser el principal. Los
     * archivos anteriores no se borran: siguen disponibles en la lista de
     * currículums del candidato.
     */
    public Archivo subirCurriculum(Usuario usuario, Part parte)
            throws ArchivoNoValidoException, RegistroException {
        return agregarCurriculum(usuario, parte);
    }

    /**
     * Elimina el currículum principal (acción heredada "eliminar-curriculum").
     * El resto de los archivos permanece intacto.
     */
    public void eliminarCurriculum(Integer usuarioId) throws RegistroException {
        Integer principalId = obtenerCurriculumPrincipalId(usuarioId);
        if (principalId != null) {
            eliminarCurriculumArchivo(usuarioId, principalId);
        }
    }

    /**
     * Lista todos los currículums del candidato, del más reciente al más
     * antiguo. No requiere columnas nuevas: se filtra por usuario y por la
     * categoría CURRICULUM que ya existe en la tabla archivo.
     */
    public List<Archivo> listarCurriculums(Integer usuarioId) {
        return archivoRepository.listarCurriculums(usuarioId);
    }

    /**
     * Agrega un currículum sin alterar los archivos ya subidos.
     *
     * @param usuario propietario de la cuenta
     * @param parte   contenido subido
     * @return archivo persistido
     */
    public Archivo agregarCurriculum(Usuario usuario, Part parte)
            throws ArchivoNoValidoException, RegistroException {
        Archivo nuevo = guardarArchivo(usuario, Archivo.CategoriaArchivo.CURRICULUM, parte);
        try {
            fijarPrincipalSoloSiFalta(usuario.getId(), nuevo);
        } catch (RegistroException e) {
            archivoService.eliminarPorId(nuevo.getId());
            throw e;
        } catch (RuntimeException e) {
            archivoService.eliminarPorId(nuevo.getId());
            throw new RegistroException("No se pudo guardar el curriculum.");
        }
        return nuevo;
    }

    /**
     * Marca un currículum propio como el principal del perfil.
     *
     * @param usuarioId dueño de la cuenta
     * @param archivoId currículum que debe quedar como principal
     */
    public void fijarCurriculumPrincipal(Integer usuarioId, Integer archivoId)
            throws RegistroException {
        if (archivoId == null || !archivoRepository.esCurriculumPropio(archivoId, usuarioId)) {
            throw new RegistroException("El curriculum seleccionado no existe o no te pertenece.");
        }

        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();

            PerfilCandidato perfil = perfilCandidatoRepository.buscarPorUsuario(em, usuarioId);
            if (perfil == null) {
                perfil = crearPerfilCandidato(em, usuarioId);
            }
            perfil.setCurriculum(em.find(Archivo.class, archivoId));
            perfil.setFechaActualizacion(LocalDateTime.now());

            em.getTransaction().commit();
        } catch (RegistroException e) {
            revertir(em);
            throw e;
        } catch (Exception e) {
            revertir(em);
            throw new RegistroException("No se pudo marcar el curriculum como principal.");
        } finally {
            em.close();
        }
    }

    /**
     * Elimina un currículum propio. Si era el principal se limpia primero la
     * referencia de {@code perfil_candidato.curriculum_id}, para que la clave
     * forzada nunca quede apuntando a un archivo que ya no existe.
     *
     * @param usuarioId dueño de la cuenta
     * @param archivoId currículum a eliminar
     */
    public void eliminarCurriculumArchivo(Integer usuarioId, Integer archivoId)
            throws RegistroException {
        if (archivoId == null || !archivoRepository.esCurriculumPropio(archivoId, usuarioId)) {
            throw new RegistroException("El curriculum seleccionado no existe o no te pertenece.");
        }

        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();

            // El principal se resuelve con JPQL: no se lee la asociacion
            // perezosa de la entidad, que ya puede estar desvinculada.
            List<Integer> ids = em.createQuery(
                    "SELECT c.id FROM PerfilCandidato p JOIN p.curriculum c"
                            + " WHERE p.usuario.id = :usuarioId",
                    Integer.class)
                    .setParameter("usuarioId", usuarioId)
                    .getResultList();
            Integer principalId = ids.isEmpty() ? null : ids.get(0);

            PerfilCandidato perfil = perfilCandidatoRepository.buscarPorUsuario(em, usuarioId);
            if (perfil != null && archivoId.equals(principalId)) {
                perfil.setCurriculum(null);
                perfil.setFechaActualizacion(LocalDateTime.now());
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            revertir(em);
            throw new RegistroException("No se pudo eliminar el curriculum.");
        } finally {
            em.close();
        }

        archivoService.eliminarPorId(archivoId);
    }

    /** Devuelve el ID del currículum principal o null si no hay ninguno. */
    private Integer obtenerCurriculumPrincipalId(Integer usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        EntityManager em = JpaConfig.createEntityManager();
        try {
            List<Integer> ids = em.createQuery(
                    "SELECT c.id FROM PerfilCandidato p JOIN p.curriculum c"
                            + " WHERE p.usuario.id = :usuarioId",
                    Integer.class)
                    .setParameter("usuarioId", usuarioId)
                    .getResultList();
            return ids.isEmpty() ? null : ids.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Asigna el archivo como currículum principal únicamente cuando la
     * cuenta todavía no tiene uno.
     */
    private void fijarPrincipalSoloSiFalta(Integer usuarioId, Archivo nuevo)
            throws RegistroException {
        EntityManager em = JpaConfig.createEntityManager();
        try {
            em.getTransaction().begin();

            PerfilCandidato perfil = perfilCandidatoRepository.buscarPorUsuario(em, usuarioId);
            if (perfil == null) {
                perfil = crearPerfilCandidato(em, usuarioId);
            }
            if (perfil.getCurriculum() == null) {
                perfil.setCurriculum(em.find(Archivo.class, nuevo.getId()));
                perfil.setFechaActualizacion(LocalDateTime.now());
            }

            em.getTransaction().commit();
        } catch (RegistroException e) {
            revertir(em);
            throw e;
        } catch (Exception e) {
            revertir(em);
            throw new RegistroException("No se pudo guardar el curriculum.");
        } finally {
            em.close();
        }
    }

    /** Crea el perfil de candidato cuando la cuenta todavía no tiene uno. */
    private PerfilCandidato crearPerfilCandidato(EntityManager em, Integer usuarioId)
            throws RegistroException {
        Usuario usuario = em.find(Usuario.class, usuarioId);
        if (usuario == null) {
            throw new RegistroException("La cuenta no existe.");
        }
        PerfilCandidato perfil = new PerfilCandidato();
        perfil.setUsuario(usuario);
        perfil.setFechaActualizacion(LocalDateTime.now());
        em.persist(perfil);
        return perfil;
    }

    /** Sube o reemplaza el logotipo de la empresa. */
    public Archivo subirLogo(Usuario usuario, Part parte)
            throws ArchivoNoValidoException, RegistroException {
        return reemplazarArchivoEmpresa(usuario, Archivo.CategoriaArchivo.LOGO_EMPRESA, parte);
    }

    /** Elimina el logotipo de la empresa. */
    public void eliminarLogo(Integer usuarioId) throws RegistroException {
        reemplazarArchivoEmpresa(usuarioId, Archivo.CategoriaArchivo.LOGO_EMPRESA, null);
    }

    /**
     * Sube o reemplaza el archivo indicado según la categoría.
     *
     * @param usuario  propietario de la cuenta
     * @param categoria FOTO_PERFIL o CURRICULUM
     * @param parte     contenido subido
     * @return archivo persistido
     */
    public Archivo subirSegunCategoria(Usuario usuario, Archivo.CategoriaArchivo categoria,
                                       Part parte)
            throws ArchivoNoValidoException, RegistroException {
        if (categoria == Archivo.CategoriaArchivo.CURRICULUM) {
            return subirCurriculum(usuario, parte);
        }
        return subirFoto(usuario, parte);
    }

    // ==================== Implementación interna ====================

    private Archivo reemplazarArchivoCandidato(Usuario usuario, Archivo.CategoriaArchivo categoria,
                                               Part parte)
            throws ArchivoNoValidoException, RegistroException {
        Archivo nuevo = guardarArchivo(usuario, categoria, parte);
        Integer anterior = aplicarCambioArchivo(usuario.getId(), categoria, nuevo, true);
        archivoService.eliminarPorId(anterior);
        return nuevo;
    }

    private void reemplazarArchivoCandidato(Integer usuarioId, Archivo.CategoriaArchivo categoria,
                                            Part parte) throws RegistroException {
        Archivo nuevo = null;
        try {
            if (parte != null && parte.getSize() > 0) {
                Usuario usuario = new Usuario();
                usuario.setId(usuarioId);
                nuevo = guardarArchivo(usuario, categoria, parte);
            }
        } catch (ArchivoNoValidoException e) {
            throw new RegistroException(e.getMessage());
        }
        Integer anterior = aplicarCambioArchivo(usuarioId, categoria, nuevo, true);
        archivoService.eliminarPorId(anterior);
    }

    private Archivo reemplazarArchivoEmpresa(Usuario usuario, Archivo.CategoriaArchivo categoria,
                                             Part parte)
            throws ArchivoNoValidoException, RegistroException {
        Archivo nuevo = guardarArchivo(usuario, categoria, parte);
        Integer anterior = aplicarCambioArchivo(usuario.getId(), categoria, nuevo, false);
        archivoService.eliminarPorId(anterior);
        return nuevo;
    }

    private void reemplazarArchivoEmpresa(Integer usuarioId, Archivo.CategoriaArchivo categoria,
                                          Part parte) throws RegistroException {
        Archivo nuevo = null;
        try {
            if (parte != null && parte.getSize() > 0) {
                Usuario usuario = new Usuario();
                usuario.setId(usuarioId);
                nuevo = guardarArchivo(usuario, categoria, parte);
            }
        } catch (ArchivoNoValidoException e) {
            throw new RegistroException(e.getMessage());
        }
        Integer anterior = aplicarCambioArchivo(usuarioId, categoria, nuevo, false);
        archivoService.eliminarPorId(anterior);
    }

    private Archivo guardarArchivo(Usuario usuario, Archivo.CategoriaArchivo categoria, Part parte)
            throws ArchivoNoValidoException {
        try {
            return archivoService.guardar(usuario, categoria, parte);
        } catch (IOException e) {
            throw new ArchivoNoValidoException("No se pudo almacenar el archivo.", e);
        }
    }

    /**
     * Asigna o retira el archivo en el perfil correspondiente.
     *
     * @param nuevo        archivo nuevo (null para eliminar)
     * @param esCandidato  true si el perfil es de candidato
     * @return ID del archivo anterior reemplazado
     */
    private Integer aplicarCambioArchivo(Integer usuarioId,
                                         Archivo.CategoriaArchivo categoria,
                                         Archivo nuevo,
                                         boolean esCandidato) throws RegistroException {
        EntityManager em = JpaConfig.createEntityManager();
        Integer anterior = null;
        try {
            em.getTransaction().begin();

            Integer nuevoId = nuevo != null ? nuevo.getId() : null;
            Archivo nuevoReferenciado = nuevoId != null
                    ? em.find(Archivo.class, nuevoId) : null;

            if (esCandidato) {
                PerfilCandidato perfil = perfilCandidatoRepository.buscarPorUsuario(em, usuarioId);
                if (perfil == null) {
                    Usuario usuario = em.find(Usuario.class, usuarioId);
                    if (usuario == null) {
                        throw new RegistroException("La cuenta no existe.");
                    }
                    perfil = new PerfilCandidato();
                    perfil.setUsuario(usuario);
                    perfil.setFechaActualizacion(LocalDateTime.now());
                    em.persist(perfil);
                }
                Archivo actual = categoria == Archivo.CategoriaArchivo.FOTO_PERFIL
                        ? perfil.getFotoPerfil() : perfil.getCurriculum();
                if (actual != null) {
                    anterior = actual.getId();
                }
                if (categoria == Archivo.CategoriaArchivo.FOTO_PERFIL) {
                    perfil.setFotoPerfil(nuevoReferenciado);
                } else {
                    perfil.setCurriculum(nuevoReferenciado);
                }
                perfil.setFechaActualizacion(LocalDateTime.now());
            } else {
                PerfilEmpresa perfil = perfilEmpresaRepository.buscarPorUsuario(em, usuarioId);
                if (perfil == null) {
                    throw new RegistroException("La cuenta no tiene perfil empresarial.");
                }
                Archivo actual = perfil.getLogo();
                if (actual != null) {
                    anterior = actual.getId();
                }
                perfil.setLogo(nuevoReferenciado);
                perfil.setFechaActualizacion(LocalDateTime.now());
            }

            em.getTransaction().commit();
            return anterior;
        } catch (RegistroException e) {
            revertir(em);
            if (nuevo != null) {
                archivoService.eliminarPorId(nuevo.getId());
            }
            throw e;
        } catch (Exception e) {
            revertir(em);
            if (nuevo != null) {
                archivoService.eliminarPorId(nuevo.getId());
            }
            throw new RegistroException("No se pudo actualizar el archivo del perfil.");
        } finally {
            em.close();
        }
    }

    private Empresa crearRegistroEmpresa(EntityManager em, Usuario usuario, PerfilEmpresa perfil) {
        Empresa empresa = new Empresa();
        empresa.setNombre(vacio(perfil.getNombreEmpresa())
                ? "Empresa sin nombre" : perfil.getNombreEmpresa());
        empresa.setCategoria(vacio(perfil.getSector()) ? "General" : perfil.getSector());
        empresa.setEmail(usuario != null && usuario.getEmail() != null
                ? usuario.getEmail() : "sin-correo@empleasv.local");
        empresa.setTelefono(perfil.getTelefonoEmpresa());
        empresa.setDescripcion(perfil.getDescripcion());
        em.persist(empresa);
        return empresa;
    }

    private void validarNombre(String nombre) throws RegistroException {
        if (vacio(nombre)) {
            throw new RegistroException("El nombre es obligatorio");
        }
        if (nombre.trim().length() > 150) {
            throw new RegistroException("El nombre no debe exceder 150 caracteres");
        }
    }

    private void validarEmail(String email, Integer usuarioId) throws RegistroException {
        if (vacio(email)) {
            throw new RegistroException("El correo electronico es obligatorio");
        }
        String normalizado = email.trim().toLowerCase();
        EntityManager em = JpaConfig.createEntityManager();
        try {
            List<String> errores = new ArrayList<>();
            if (!com.empleasv.util.ValidacionUtil.esEmailValido(email)) {
                errores.add("El formato del correo electronico no es valido");
            }
            Long cantidad = em.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE LOWER(u.email) = :email AND u.id <> :id",
                    Long.class)
                    .setParameter("email", normalizado)
                    .setParameter("id", usuarioId)
                    .getSingleResult();
            if (cantidad > 0) {
                errores.add("El correo electronico ya esta registrado en otra cuenta");
            }
            if (!errores.isEmpty()) {
                throw new RegistroException(errores);
            }
        } finally {
            em.close();
        }
    }

    private boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void revertir(EntityManager em) {
        try {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } catch (Exception ignored) {
            // La transacción ya pudo revertirse.
        }
    }
}
