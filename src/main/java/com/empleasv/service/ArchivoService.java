package com.empleasv.service;

import com.empleasv.config.JpaConfig;
import com.empleasv.model.Archivo;
import com.empleasv.model.Usuario;
import com.empleasv.repository.ArchivoRepository;
import com.empleasv.util.ArchivoNoValidoException;
import com.empleasv.util.ArchivosUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Servicio de archivos: valida, almacena fuera del directorio público,
 * persiste sus metadatos y elimina de forma segura.
 */
public class ArchivoService {

    private final ArchivoRepository archivoRepository = new ArchivoRepository();
    private final ServletContext context;

    public ArchivoService(ServletContext context) {
        this.context = context;
    }

    /**
     * Valida una parte subida, escribe el archivo en disco de forma segura
     * y persiste sus metadatos.
     *
     * @param propietario usuario dueño del archivo
     * @param categoria   categoría del archivo
     * @param parte       contenido subido
     * @return archivo persistido
     */
    public Archivo guardar(Usuario propietario, Archivo.CategoriaArchivo categoria, Part parte)
            throws ArchivoNoValidoException, IOException {

        if (propietario == null || propietario.getId() == null) {
            throw new ArchivoNoValidoException("Debes iniciar sesion para subir archivos.");
        }

        ArchivosUtil.Metadata metadata = ArchivosUtil.validar(parte, categoria);

        Path base = ArchivosUtil.directorioBase(context);
        String nombreAlmacenado = ArchivosUtil.nombreAlmacenado(metadata.extension);
        String rutaRelativa = ArchivosUtil.rutaRelativa(
                categoria, propietario.getId(), nombreAlmacenado);
        Path destino = ArchivosUtil.resolverRuta(base, rutaRelativa);

        ArchivosUtil.almacenar(parte, destino);

        Archivo archivo = new Archivo();
        archivo.setNombreOriginal(metadata.nombreOriginal);
        archivo.setNombreAlmacenado(nombreAlmacenado);
        archivo.setRutaRelativa(rutaRelativa);
        archivo.setTipoMime(metadata.tipoMime);
        archivo.setExtension(metadata.extension);
        archivo.setTamanoBytes(metadata.tamanoBytes);
        archivo.setCategoria(categoria);
        archivo.setUsuario(propietario);
        archivo.setFechaSubida(LocalDateTime.now());

        try {
            return archivoRepository.guardar(archivo);
        } catch (RuntimeException e) {
            ArchivosUtil.eliminarSilencioso(destino);
            throw e;
        }
    }

    /**
     * Persiste un archivo ya escrito en disco dentro de la transacción activa.
     * Utilizado por el registro donde usuario y archivo se crean juntos.
     *
     * @param em        EntityManager con transacción activa
     * @param propietario usuario dueño
     * @param categoria categoría
     * @param parte     contenido subido
     * @return archivo persistido
     */
    public Archivo guardar(jakarta.persistence.EntityManager em, Usuario propietario,
                           Archivo.CategoriaArchivo categoria, Part parte)
            throws ArchivoNoValidoException, IOException {

        ArchivosUtil.Metadata metadata = ArchivosUtil.validar(parte, categoria);

        Path base = ArchivosUtil.directorioBase(context);
        String nombreAlmacenado = ArchivosUtil.nombreAlmacenado(metadata.extension);
        String rutaRelativa = ArchivosUtil.rutaRelativa(
                categoria, propietario.getId(), nombreAlmacenado);
        Path destino = ArchivosUtil.resolverRuta(base, rutaRelativa);

        ArchivosUtil.almacenar(parte, destino);

        Archivo archivo = new Archivo();
        archivo.setNombreOriginal(metadata.nombreOriginal);
        archivo.setNombreAlmacenado(nombreAlmacenado);
        archivo.setRutaRelativa(rutaRelativa);
        archivo.setTipoMime(metadata.tipoMime);
        archivo.setExtension(metadata.extension);
        archivo.setTamanoBytes(metadata.tamanoBytes);
        archivo.setCategoria(categoria);
        archivo.setUsuario(propietario);
        archivo.setFechaSubida(LocalDateTime.now());

        archivoRepository.guardar(em, archivo);
        return archivo;
    }

    /** Devuelve la ruta absoluta de un archivo almacenado. */
    public Path rutaDe(Archivo archivo) throws IOException {
        if (archivo == null) {
            throw new IOException("Archivo no disponible");
        }
        Path base = ArchivosUtil.directorioBase(context);
        return ArchivosUtil.resolverRuta(base, archivo.getRutaRelativa());
    }

    /** Elimina el archivo físico. No lanza excepción si no existe. */
    public void eliminarFisico(Archivo archivo) {
        if (archivo == null) {
            return;
        }
        try {
            ArchivosUtil.eliminarSilencioso(rutaDe(archivo));
        } catch (IOException ignored) {
            // No hay nada que limpiar.
        }
    }

    /** Elimina un archivo por su ID: metadatos en BD y contenido en disco. */
    public void eliminarPorId(Integer archivoId) {
        if (archivoId == null) {
            return;
        }
        Archivo archivo = archivoRepository.obtenerPorId(archivoId);
        if (archivo == null) {
            return;
        }
        Path ruta;
        try {
            ruta = rutaDe(archivo);
        } catch (IOException e) {
            ruta = null;
        }
        archivoRepository.eliminar(archivoId);
        ArchivosUtil.eliminarSilencioso(ruta);
    }
}
