package com.empleasv.service;

import com.empleasv.model.Archivo;
import com.empleasv.model.OfertaEmpleo;
import com.empleasv.model.PerfilCandidato;
import com.empleasv.model.Postulacion;
import com.empleasv.model.Usuario;
import com.empleasv.model.UsuarioRol;
import com.empleasv.repository.ArchivoRepository;
import com.empleasv.repository.OfertaEmpleoRepository;
import com.empleasv.repository.PostulacionRepository;
import com.empleasv.util.ArchivoNoValidoException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Reglas de negocio de las postulaciones.
 *
 * Un candidato puede postularse una sola vez por oferta y debe adjuntar un
 * curriculum PDF, ya sea uno de su perfil o uno nuevo. La postulacion guarda
 * el archivo exacto enviado para conservar el historial aunque el candidato
 * luego cambie el CV de su perfil.
 */
public class PostulacionService {

    private final PostulacionRepository postulacionRepository = new PostulacionRepository();
    private final OfertaEmpleoRepository ofertaRepository = new OfertaEmpleoRepository();
    private final ArchivoRepository archivoRepository = new ArchivoRepository();
    private final ArchivoService archivoService;
    private final PerfilService perfilService;

    public PostulacionService(ServletContext context) {
        this.archivoService = new ArchivoService(context);
        this.perfilService = new PerfilService(context);
    }

    /**
     * Registra una postulacion.
     *
     * @param candidato    usuario autenticado que se postula
     * @param ofertaId     oferta destino
     * @param mensaje      mensaje opcional de presentacion
     * @param curriculumId CV existente del perfil (puede ser null)
     * @param archivoCv    CV nuevo en PDF (puede ser null)
     * @return postulacion persistida
     */
    public Postulacion crear(Usuario candidato, Integer ofertaId, String mensaje,
                             Integer curriculumId, Part archivoCv)
            throws RegistroException, ArchivoNoValidoException {

        if (candidato == null || candidato.getId() == null) {
            throw new RegistroException("Debes iniciar sesion para postularte.");
        }
        if (ofertaId == null) {
            throw new RegistroException("La oferta es obligatoria.");
        }

        OfertaEmpleo oferta = ofertaRepository.obtenerPorId(ofertaId);
        if (oferta == null) {
            throw new RegistroException("La oferta seleccionada no existe.");
        }
        if (!"ACTIVA".equalsIgnoreCase(oferta.getEstado())) {
            throw new RegistroException("La oferta ya no esta disponible para postulaciones.");
        }
        if (postulacionRepository.existe(ofertaId, candidato.getId())) {
            throw new RegistroException("Ya te postulaste a esta oferta.");
        }

        Archivo curriculum;
        boolean curriculumNuevo = false;

        if (archivoCv != null && archivoCv.getSize() > 0
                && archivoCv.getSubmittedFileName() != null
                && !archivoCv.getSubmittedFileName().trim().isEmpty()) {
            try {
                curriculum = archivoService.guardarPdf(candidato, archivoCv);
            } catch (IOException e) {
                throw new ArchivoNoValidoException("No se pudo almacenar el curriculum.", e);
            }
            curriculumNuevo = true;
        } else if (curriculumId != null) {
            if (!archivoRepository.esCurriculumPropio(curriculumId, candidato.getId())) {
                throw new RegistroException(
                        "El curriculum seleccionado no existe o no te pertenece.");
            }
            curriculum = archivoRepository.obtenerPorId(curriculumId);
            if (curriculum == null) {
                throw new RegistroException("El curriculum seleccionado ya no esta disponible.");
            }
        } else {
            throw new RegistroException("El curriculum es obligatorio para postularte.");
        }

        Postulacion postulacion = new Postulacion();
        postulacion.setOferta(oferta);
        postulacion.setCandidato(candidato);
        postulacion.setCurriculum(curriculum);
        postulacion.setMensajePresentacion(normalizarMensaje(mensaje));
        postulacion.setEstado(Postulacion.EstadoPostulacion.PENDIENTE);
        postulacion.setFechaPostulacion(LocalDateTime.now());

        try {
            postulacionRepository.guardar(postulacion);
        } catch (RuntimeException e) {
            if (curriculumNuevo && curriculum.getId() != null) {
                archivoService.eliminarPorId(curriculum.getId());
            }
            if (postulacionRepository.existe(ofertaId, candidato.getId())) {
                throw new RegistroException("Ya te postulaste a esta oferta.");
            }
            throw new RegistroException("No se pudo registrar la postulacion. Intenta de nuevo.");
        }

        // Si el candidato no tenia curriculum principal, el recien enviado pasa
        // a serlo. Es una mejora de conveniencia: un fallo aqui no invalida la
        // postulacion, que ya quedo registrada.
        if (curriculumNuevo) {
            try {
                PerfilCandidato perfil = perfilService.obtenerOCrearCandidato(candidato.getId());
                if (perfil != null && perfil.getCurriculum() == null) {
                    perfilService.fijarCurriculumPrincipal(candidato.getId(), curriculum.getId());
                }
            } catch (Exception ignored) {
                // La postulacion ya esta guardada con su propio CV.
            }
        }

        return postulacion;
    }

    /** Indica si el candidato ya se postulo a la oferta indicada. */
    public boolean yaPostulado(Integer ofertaId, Integer usuarioId) {
        return postulacionRepository.existe(ofertaId, usuarioId);
    }

    /** Postulaciones del candidato autenticado. */
    public List<Postulacion> listarMias(Integer usuarioId) {
        return postulacionRepository.listarPorCandidato(usuarioId);
    }

    /** Postulaciones recibidas por las ofertas de una empresa. */
    public List<Postulacion> listarRecibidas(Integer empresaId) {
        return postulacionRepository.listarPorEmpresa(empresaId);
    }

    /** Todas las postulaciones (administrador). */
    public List<Postulacion> listarTodas() {
        return postulacionRepository.listarTodas();
    }

    /** Obtiene una postulacion con sus relaciones. */
    public Postulacion obtener(Integer postulacionId) {
        return postulacionRepository.obtenerPorId(postulacionId);
    }

    /**
     * Actualiza el estado de una postulacion. Solo la empresa dueña de la
     * oferta o un administrador pueden hacerlo.
     */
    public Postulacion actualizarEstado(Usuario usuario, Integer postulacionId, String estado)
            throws RegistroException {

        if (usuario == null || usuario.getId() == null) {
            throw new RegistroException("Debes iniciar sesion.");
        }
        Postulacion postulacion = postulacionRepository.obtenerPorId(postulacionId);
        if (postulacion == null) {
            throw new RegistroException("La postulacion no existe.");
        }
        if (!puedeVer(usuario, postulacion)) {
            throw new RegistroException("No tienes permiso para modificar esta postulacion.");
        }
        if (usuario.getRol() != UsuarioRol.ADMINISTRADOR
                && !esEmpresaPropietaria(usuario, postulacion)) {
            throw new RegistroException("Solo la empresa puede cambiar el estado de la postulacion.");
        }

        postulacion.setEstado(parsearEstado(estado));
        return postulacionRepository.actualizar(postulacion);
    }

    /**
     * Indica si el usuario puede ver la postulacion: el propio candidato, la
     * empresa dueña de la oferta o un administrador.
     */
    public boolean puedeVer(Usuario usuario, Postulacion postulacion) {
        if (usuario == null || usuario.getId() == null || postulacion == null) {
            return false;
        }
        if (usuario.getRol() == UsuarioRol.ADMINISTRADOR) {
            return true;
        }
        if (postulacion.getCandidato() != null
                && usuario.getId().equals(postulacion.getCandidato().getId())) {
            return true;
        }
        return esEmpresaPropietaria(usuario, postulacion);
    }

    /** Indica si el usuario es la empresa dueña de la oferta de la postulacion. */
    public boolean esEmpresaPropietaria(Usuario usuario, Postulacion postulacion) {
        if (usuario == null || usuario.getId() == null || postulacion == null
                || usuario.getRol() != UsuarioRol.EMPRESA) {
            return false;
        }
        if (postulacion.getOferta() == null || postulacion.getOferta().getEmpresa() == null) {
            return false;
        }
        Integer empresaId = perfilService.obtenerEmpresaId(usuario.getId());
        return empresaId != null
                && empresaId.equals(postulacion.getOferta().getEmpresa().getId());
    }

    /** Convierte el texto recibido al estado valido. */
    public Postulacion.EstadoPostulacion parsearEstado(String estado) throws RegistroException {
        if (estado == null || estado.trim().isEmpty()) {
            throw new RegistroException("Debes indicar el nuevo estado.");
        }
        try {
            return Postulacion.EstadoPostulacion.valueOf(
                    estado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new RegistroException(
                    "Estado no valido. Usa PENDIENTE, REVISADA, ACEPTADA o RECHAZADA.");
        }
    }

    private String normalizarMensaje(String mensaje) {
        if (mensaje == null) {
            return null;
        }
        String limpio = mensaje.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        return limpio.length() > 2000 ? limpio.substring(0, 2000) : limpio;
    }
}
