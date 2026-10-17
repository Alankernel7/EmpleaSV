package com.empleasv.service;

import com.empleasv.config.JpaConfig;
import com.empleasv.model.Archivo;
import com.empleasv.model.Empresa;
import com.empleasv.model.PerfilCandidato;
import com.empleasv.model.PerfilEmpresa;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.util.ArchivoNoValidoException;
import com.empleasv.util.ArchivosUtil;
import com.empleasv.util.PasswordUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de registro de cuentas.
 *
 * Crea la cuenta de usuario junto con su perfil (candidato o empresarial)
 * en una sola transacción, con contraseña hasheada mediante PasswordUtil.
 * Los archivos opcionales (foto, currículum, logotipo) se validan y almacenan
 * de forma segura fuera del directorio público.
 */
public class RegistroService {

    private final ArchivoService archivoService;

    public RegistroService(ServletContext context) {
        this.archivoService = new ArchivoService(context);
    }

    /**
     * Registra una cuenta de candidato (rol USUARIO) con su perfil.
     *
     * @return usuario creado con ID generado
     */
    public Usuario registrarCandidato(String nombre, String email, String password,
                                      String telefono, String direccion,
                                      String municipio, String departamento,
                                      Part foto, Part curriculum)
            throws RegistroException, ArchivoNoValidoException {

        EntityManager em = JpaConfig.createEntityManager();
        List<Path> escritos = new ArrayList<>();
        try {
            em.getTransaction().begin();

            validarEmailLibre(em, email, null);

            Usuario usuario = construirUsuario(nombre, email, password, UsuarioRol.USUARIO);
            em.persist(usuario);

            Archivo archivoFoto = null;
            Archivo archivoCurriculum = null;
            try {
                if (contieneArchivo(foto)) {
                    archivoFoto = archivoService.guardar(
                            em, usuario, Archivo.CategoriaArchivo.FOTO_PERFIL, foto);
                    escritos.add(archivoService.rutaDe(archivoFoto));
                }
                if (contieneArchivo(curriculum)) {
                    archivoCurriculum = archivoService.guardar(
                            em, usuario, Archivo.CategoriaArchivo.CURRICULUM, curriculum);
                    escritos.add(archivoService.rutaDe(archivoCurriculum));
                }
            } catch (IOException e) {
                throw new ArchivoNoValidoException("No se pudo almacenar el archivo.", e);
            }

            PerfilCandidato perfil = new PerfilCandidato();
            perfil.setUsuario(usuario);
            perfil.setTelefono(vacio(telefono) ? null : telefono.trim());
            perfil.setDireccionResidencia(vacio(direccion) ? null : direccion.trim());
            perfil.setMunicipio(vacio(municipio) ? null : municipio.trim());
            perfil.setDepartamento(vacio(departamento) ? null : departamento.trim());
            perfil.setFotoPerfil(archivoFoto);
            perfil.setCurriculum(archivoCurriculum);
            perfil.setFechaActualizacion(LocalDateTime.now());
            em.persist(perfil);

            em.getTransaction().commit();
            return usuario;

        } catch (Exception e) {
            revertir(em);
            limpiarArchivos(escritos);
            if (e instanceof RegistroException) {
                throw (RegistroException) e;
            }
            if (e instanceof ArchivoNoValidoException) {
                throw (ArchivoNoValidoException) e;
            }
            throw new RegistroException("No se pudo crear la cuenta. Intentalo de nuevo.");
        } finally {
            em.close();
        }
    }

    /**
     * Registra una cuenta de empresa (rol EMPRESA) con su perfil empresarial.
     *
     * Crea además el registro de la tabla 'empresa' al que quedarán asociadas
     * las ofertas publicadas por esta cuenta.
     *
     * @return usuario creado con ID generado
     */
    public Usuario registrarEmpresa(String nombreResponsable, String email, String password,
                                    String nombreEmpresa, String nit, String telefonoEmpresa,
                                    String direccionEmpresa, String municipio, String departamento,
                                    String sector, String sitioWeb, String descripcion,
                                    Part logo)
            throws RegistroException, ArchivoNoValidoException {

        EntityManager em = JpaConfig.createEntityManager();
        List<Path> escritos = new ArrayList<>();
        try {
            em.getTransaction().begin();

            validarEmailLibre(em, email, null);

            Usuario usuario = construirUsuario(
                    nombreResponsable, email, password, UsuarioRol.EMPRESA);
            em.persist(usuario);

            Archivo archivoLogo = null;
            try {
                if (contieneArchivo(logo)) {
                    archivoLogo = archivoService.guardar(
                            em, usuario, Archivo.CategoriaArchivo.LOGO_EMPRESA, logo);
                    escritos.add(archivoService.rutaDe(archivoLogo));
                }
            } catch (IOException e) {
                throw new ArchivoNoValidoException("No se pudo almacenar el logotipo.", e);
            }

            // Registro de la tabla empresa: es el que usan las ofertas de empleo.
            Empresa empresa = new Empresa();
            empresa.setNombre(vacio(nombreEmpresa) ? "Empresa sin nombre" : nombreEmpresa.trim());
            empresa.setCategoria(vacio(sector) ? "General" : sector.trim());
            empresa.setEmail(normalizar(email));
            empresa.setTelefono(vacio(telefonoEmpresa) ? null : telefonoEmpresa.trim());
            empresa.setDescripcion(vacio(descripcion) ? null : descripcion.trim());
            em.persist(empresa);

            PerfilEmpresa perfil = new PerfilEmpresa();
            perfil.setUsuario(usuario);
            perfil.setNombreResponsable(normalizar(nombreResponsable));
            perfil.setNombreEmpresa(empresa.getNombre());
            perfil.setNit(vacio(nit) ? null : nit.trim());
            perfil.setTelefonoEmpresa(vacio(telefonoEmpresa) ? null : telefonoEmpresa.trim());
            perfil.setDireccionEmpresa(vacio(direccionEmpresa) ? null : direccionEmpresa.trim());
            perfil.setMunicipio(vacio(municipio) ? null : municipio.trim());
            perfil.setDepartamento(vacio(departamento) ? null : departamento.trim());
            perfil.setSector(vacio(sector) ? null : sector.trim());
            perfil.setSitioWeb(vacio(sitioWeb) ? null : sitioWeb.trim());
            perfil.setDescripcion(vacio(descripcion) ? null : descripcion.trim());
            perfil.setLogo(archivoLogo);
            perfil.setEmpresa(empresa);
            perfil.setFechaActualizacion(LocalDateTime.now());
            em.persist(perfil);

            em.getTransaction().commit();
            return usuario;

        } catch (Exception e) {
            revertir(em);
            limpiarArchivos(escritos);
            if (e instanceof RegistroException) {
                throw (RegistroException) e;
            }
            if (e instanceof ArchivoNoValidoException) {
                throw (ArchivoNoValidoException) e;
            }
            throw new RegistroException("No se pudo crear la cuenta. Intentalo de nuevo.");
        } finally {
            em.close();
        }
    }

    // ==================== Auxiliares ====================

    private Usuario construirUsuario(String nombre, String email, String password, UsuarioRol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(normalizar(nombre));
        usuario.setEmail(normalizar(email));
        usuario.setPasswordHash(PasswordUtil.hashPassword(password));
        usuario.setRol(rol);
        usuario.setEstado("ACTIVO");
        usuario.setFechaCreacion(LocalDateTime.now());
        return usuario;
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    /**
     * Verifica que el correo no esté registrado.
     * Compara en minúsculas para mantener la unicidad práctica del sistema.
     */
    private void validarEmailLibre(EntityManager em, String email, Integer usuarioExcluido)
            throws RegistroException {
        String normalizado = normalizar(email).toLowerCase();
        if (normalizado.isEmpty()) {
            throw new RegistroException("El correo electronico es obligatorio");
        }
        Long cantidad = em.createQuery(
                "SELECT COUNT(u) FROM Usuario u WHERE LOWER(u.email) = :email", Long.class)
                .setParameter("email", normalizado)
                .getSingleResult();
        if (cantidad > 0) {
            throw new RegistroException("El correo electronico ya esta registrado");
        }
        if (usuarioExcluido != null) {
            Long propios = em.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE LOWER(u.email) = :email AND u.id <> :id",
                    Long.class)
                    .setParameter("email", normalizado)
                    .setParameter("id", usuarioExcluido)
                    .getSingleResult();
            if (propios > 0) {
                throw new RegistroException("El correo electronico ya esta registrado");
            }
        }
    }

    private boolean contieneArchivo(Part parte) {
        return parte != null && parte.getSize() > 0
                && parte.getSubmittedFileName() != null
                && !parte.getSubmittedFileName().trim().isEmpty();
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
            // La transacción ya pudo haberse revertido.
        }
    }

    private void limpiarArchivos(List<Path> escritos) {
        for (Path ruta : escritos) {
            ArchivosUtil.eliminarSilencioso(ruta);
        }
    }
}
