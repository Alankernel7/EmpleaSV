package com.empleasv.util;

import com.empleasv.model.Archivo;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Validación y almacenamiento seguro de archivos subidos.
 *
 * Reglas generales:
 *  - Solo extensiones y tipos MIME permitidos por categoría.
 *  - Límite de tamaño por categoría.
 *  - Verificación de la firma binaria (magic bytes) del contenido.
 *  - El nombre original nunca se usa como ruta: se genera un nombre aleatorio.
 *  - Los archivos se guardan fuera del directorio público de la aplicación.
 *  - Se impide el traversal de rutas al leer o escribir.
 */
public final class ArchivosUtil {

    /** Límite para fotografías y logotipos: 2 MB. */
    public static final long LIMITE_IMAGEN_BYTES = 2L * 1024 * 1024;

    /** Límite para currículums: 5 MB. */
    public static final long LIMITE_CURRICULUM_BYTES = 5L * 1024 * 1024;

    /** Parámetro de contexto (web.xml) con la ruta base de almacenamiento. */
    public static final String CONTEXTO_DIRECTORIO = "empleasv.archivos.directorio";

    /** Propiedad de sistema alternativa para configurar la ruta base. */
    public static final String PROPIEDAD_DIRECTORIO = "empleasv.archivos.directorio";

    private static final Map<String, Set<String>> EXTENSIONES = new LinkedHashMap<>();
    private static final Map<String, Set<String>> MIME = new LinkedHashMap<>();
    private static final Map<String, Long> LIMITES = new LinkedHashMap<>();

    static {
        EXTENSIONES.put("FOTO_PERFIL", Set.of("jpg", "jpeg", "png", "webp"));
        EXTENSIONES.put("LOGO_EMPRESA", Set.of("jpg", "jpeg", "png", "webp"));
        EXTENSIONES.put("CURRICULUM", Set.of("pdf", "doc", "docx", "odt", "txt"));

        MIME.put("FOTO_PERFIL", Set.of("image/jpeg", "image/png", "image/webp"));
        MIME.put("LOGO_EMPRESA", Set.of("image/jpeg", "image/png", "image/webp"));
        MIME.put("CURRICULUM", Set.of(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.oasis.opendocument.text",
                "text/plain"));

        LIMITES.put("FOTO_PERFIL", LIMITE_IMAGEN_BYTES);
        LIMITES.put("LOGO_EMPRESA", LIMITE_IMAGEN_BYTES);
        LIMITES.put("CURRICULUM", LIMITE_CURRICULUM_BYTES);
    }

    private ArchivosUtil() {
    }

    // ==================== Directorio base ====================

    /**
     * Resuelve el directorio base donde se guardan los archivos físicos.
     * Orden de búsqueda: parámetro de contexto, propiedad de sistema,
     * variable de entorno y, por último, una carpeta bajo el directorio temporal.
     *
     * @param context ServletContext de la aplicación (puede ser null)
     * @return ruta base normalizada
     */
    public static Path directorioBase(ServletContext context) {
        String ruta = null;

        if (context != null) {
            ruta = context.getInitParameter(CONTEXTO_DIRECTORIO);
        }
        if (ruta == null || ruta.trim().isEmpty()) {
            ruta = System.getProperty(PROPIEDAD_DIRECTORIO);
        }
        if (ruta == null || ruta.trim().isEmpty()) {
            ruta = System.getenv("EMPLEASV_ARCHIVOS_DIR");
        }
        if (ruta == null || ruta.trim().isEmpty()) {
            ruta = Paths.get(System.getProperty("java.io.tmpdir"),
                    "empleasv-archivos").toString();
        }
        return Paths.get(ruta.trim()).toAbsolutePath().normalize();
    }

    /**
     * Resuelve una ruta relativa dentro del directorio base impidiendo
     * cualquier intento de traversal de rutas.
     *
     * @param base        directorio base
     * @param rutaRelativa ruta almacenada en la base de datos
     * @return ruta absoluta validada
     * @throws IOException si la ruta escapa del directorio base
     */
    public static Path resolverRuta(Path base, String rutaRelativa) throws IOException {
        if (base == null || rutaRelativa == null || rutaRelativa.trim().isEmpty()) {
            throw new IOException("Ruta de archivo no valida");
        }
        Path baseNormalizada = base.toAbsolutePath().normalize();
        Path resuelta = baseNormalizada.resolve(rutaRelativa.replace('\\', '/')).normalize();
        if (!resuelta.startsWith(baseNormalizada)) {
            throw new IOException("Ruta de archivo fuera del directorio permitido");
        }
        return resuelta;
    }

    // ==================== Validación ====================

    /**
     * Valida una parte del formulario contra las reglas de la categoría.
     *
     * @param parte      parte multipart recibida
     * @param categoria  categoría del archivo
     * @return metadatos normalizados (nombre original, extensión, MIME, tamaño, cabecera)
     * @throws ArchivoNoValidoException si el archivo no cumple las reglas
     */
    public static Metadata validar(Part parte, Archivo.CategoriaArchivo categoria)
            throws ArchivoNoValidoException {

        String categoriaNombre = categoria.name();
        Set<String> extensionesPermitidas = EXTENSIONES.get(categoriaNombre);
        Set<String> mimesPermitidos = MIME.get(categoriaNombre);
        long limite = LIMITES.get(categoriaNombre);

        String nombreOriginal = nombreOriginalSeguro(parte.getSubmittedFileName());

        if (nombreOriginal.isEmpty()) {
            throw new ArchivoNoValidoException("Debe seleccionar un archivo.");
        }

        String extension = extensionDe(nombreOriginal);
        if (extension.isEmpty()) {
            throw new ArchivoNoValidoException("El archivo debe tener una extension valida.");
        }
        if (!extensionesPermitidas.contains(extension)) {
            throw new ArchivoNoValidoException(
                    "Extension no permitida (" + extension + "). Formatos aceptados: "
                            + String.join(", ", extensionesPermitidas) + ".");
        }

        String mime = parte.getContentType() != null
                ? parte.getContentType().toLowerCase(Locale.ROOT).trim() : "";
        if (mime.isEmpty() || !mimesPermitidos.contains(mime)) {
            throw new ArchivoNoValidoException(
                    "El tipo de contenido del archivo no es valido para esta operacion.");
        }

        long tamano = parte.getSize();
        if (tamano <= 0) {
            throw new ArchivoNoValidoException("El archivo esta vacio.");
        }
        if (tamano > limite) {
            throw new ArchivoNoValidoException(
                    "El archivo supera el tamano maximo permitido de "
                            + (limite / (1024 * 1024)) + " MB.");
        }

        byte[] cabecera;
        try (InputStream in = parte.getInputStream()) {
            cabecera = in.readNBytes(16);
        } catch (IOException e) {
            throw new ArchivoNoValidoException("No se pudo leer el archivo.", e);
        }

        if (!firmaValida(cabecera, extension)) {
            throw new ArchivoNoValidoException(
                    "El contenido del archivo no coincide con su extension.");
        }

        Metadata metadata = new Metadata();
        metadata.nombreOriginal = nombreOriginal;
        metadata.extension = extension;
        metadata.tipoMime = mime;
        metadata.tamanoBytes = tamano;
        metadata.cabecera = cabecera;
        return metadata;
    }

    /**
     * Comprueba la firma binaria del archivo frente a la extensión declarada
     * y descarta archivos ejecutables.
     */
    private static boolean firmaValida(byte[] cabecera, String extension) {
        if (cabecera == null || cabecera.length < 4) {
            return false;
        }

        // Archivos ejecutables o scripts siempre rechazados
        if (cabecera[0] == 'M' && cabecera[1] == 'Z') {
            return false;
        }
        if ((cabecera[0] & 0xFF) == 0x7F && cabecera[1] == 'E'
                && cabecera[2] == 'L' && cabecera[3] == 'F') {
            return false;
        }
        if (cabecera[0] == '#' && cabecera[1] == '!') {
            return false;
        }

        switch (extension) {
            case "jpg":
            case "jpeg":
                return (cabecera[0] & 0xFF) == 0xFF && (cabecera[1] & 0xFF) == 0xD8
                        && (cabecera[2] & 0xFF) == 0xFF;
            case "png":
                return (cabecera[0] & 0xFF) == 0x89 && cabecera[1] == 'P'
                        && cabecera[2] == 'N' && cabecera[3] == 'G';
            case "webp":
                return cabecera.length >= 12
                        && cabecera[0] == 'R' && cabecera[1] == 'I'
                        && cabecera[2] == 'F' && cabecera[3] == 'F'
                        && cabecera[8] == 'W' && cabecera[9] == 'E'
                        && cabecera[10] == 'B' && cabecera[11] == 'P';
            case "pdf":
                return cabecera[0] == '%' && cabecera[1] == 'P'
                        && cabecera[2] == 'D' && cabecera[3] == 'F';
            case "doc":
                return (cabecera[0] & 0xFF) == 0xD0 && (cabecera[1] & 0xFF) == 0xCF
                        && (cabecera[2] & 0xFF) == 0x11 && (cabecera[3] & 0xFF) == 0xE0;
            case "docx":
            case "odt":
                return cabecera[0] == 'P' && cabecera[1] == 'K';
            case "txt":
                return !contieneNulo(cabecera);
            default:
                return false;
        }
    }

    private static boolean contieneNulo(byte[] datos) {
        for (byte b : datos) {
            if (b == 0) {
                return true;
            }
        }
        return false;
    }

    // ==================== Nombres seguros ====================

    /**
     * Elimina cualquier componente de ruta del nombre indicado por el usuario.
     */
    public static String nombreOriginalSeguro(String nombre) {
        if (nombre == null) {
            return "";
        }
        String limpio = nombre.replace('\\', '/');
        int corte = limpio.lastIndexOf('/');
        if (corte >= 0) {
            limpio = limpio.substring(corte + 1);
        }
        limpio = limpio.replaceAll("[\\p{Cntrl}&&[^\t]]", "").trim();
        if (limpio.length() > 150) {
            limpio = limpio.substring(0, 150);
        }
        return limpio;
    }

    /** Devuelve la extensión en minúsculas sin el punto. */
    public static String extensionDe(String nombre) {
        if (nombre == null) {
            return "";
        }
        int punto = nombre.lastIndexOf('.');
        if (punto < 0 || punto == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(punto + 1).toLowerCase(Locale.ROOT).trim();
    }

    /** Genera un nombre de archivo aleatorio e inyectable en el sistema de archivos. */
    public static String nombreAlmacenado(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    /** Construye la ruta relativa (con '/') dentro del directorio base. */
    public static String rutaRelativa(Archivo.CategoriaArchivo categoria,
                                      Integer usuarioId,
                                      String nombreAlmacenado) {
        return categoria.name() + "/" + usuarioId + "/" + nombreAlmacenado;
    }

    /**
     * Cabecera del archivo como texto hexadecimal/ASCII para validaciones.
     */
    public static String cabeceraComoTexto(byte[] cabecera) {
        if (cabecera == null || cabecera.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : Arrays.copyOf(cabecera, Math.min(cabecera.length, 8))) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * Copia el contenido de la parte al destino indicado.
     *
     * @param parte       parte multipart
     * @param destino     ruta absoluta destino
     * @throws IOException si no se puede escribir
     */
    public static void almacenar(Part parte, Path destino) throws IOException {
        Path padre = destino.getParent();
        if (padre != null) {
            Files.createDirectories(padre);
        }
        try (InputStream in = parte.getInputStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Elimina un archivo físico si existe. Nunca lanza excepción. */
    public static void eliminarSilencioso(Path destino) {
        if (destino == null) {
            return;
        }
        try {
            Files.deleteIfExists(destino);
        } catch (IOException ignored) {
            // El archivo fisico pudo no existir; los metadatos ya se eliminaron.
        }
    }

    /** Cabecera segura para Content-Disposition. */
    public static String contentDisposition(String nombreOriginal) {
        return contentDisposition(nombreOriginal, false);
    }

    /**
     * Cabecera segura para Content-Disposition.
     *
     * @param nombreOriginal nombre mostrado al usuario
     * @param inline         true para imágenes (renderizadas en el navegador)
     */
    public static String contentDisposition(String nombreOriginal, boolean inline) {
        String nombre = nombreOriginalSeguro(nombreOriginal);
        if (nombre.isEmpty()) {
            nombre = "archivo";
        }
        String ascii = nombre.replaceAll("[^\\x20-\\x7E]", "_");
        String utf8 = nombre.replace("\"", "'");
        String modo = inline ? "inline" : "attachment";
        return modo + "; filename=\"" + ascii + "\"; filename*=UTF-8''"
                + utf8.replace(" ", "%20");
    }

    /** Convierte bytes a texto ISO-8859-1 para inspecciones. */
    public static String bytesATexto(byte[] datos) {
        if (datos == null) {
            return "";
        }
        return new String(datos, StandardCharsets.ISO_8859_1);
    }

    /** Metadatos normalizados de un archivo validado. */
    public static final class Metadata {
        public String nombreOriginal;
        public String extension;
        public String tipoMime;
        public long tamanoBytes;
        public byte[] cabecera;
    }
}
