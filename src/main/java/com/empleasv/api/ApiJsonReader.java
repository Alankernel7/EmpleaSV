package com.empleasv.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lector JSON mínimo para solicitudes de la API.
 *
 * Soporta objetos, arreglos, cadenas con escapes, números, booleanos y null.
 * No pretende ser un parser genérico de propósito general: su alcance es el
 * cuerpo de las solicitudes REST de EmpleaSV.
 */
public final class ApiJsonReader {

    private final String texto;
    private int posicion;
    private int profundidad;

    private ApiJsonReader(String texto) {
        this.texto = texto;
    }

    /** Lee un valor JSON y devuelve Map, List, String, Number, Boolean o null. */
    public static Object leer(String texto) {
        if (texto == null) {
            throw new ApiExcepcion(400, "JSON_INVALIDO",
                    "El cuerpo de la solicitud es obligatorio.");
        }
        ApiJsonReader lector = new ApiJsonReader(texto);
        Object valor = lector.valor();
        lector.espacios();
        if (lector.posicion < lector.texto.length()) {
            throw lector.error("Contenido adicional al final del JSON.");
        }
        return valor;
    }

    /** Lee un cuerpo JSON que debe ser un objeto. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> objeto(String texto) {
        Object valor = leer(texto);
        if (!(valor instanceof Map)) {
            throw new ApiExcepcion(400, "JSON_INVALIDO",
                    "Se esperaba un objeto JSON en el cuerpo de la solicitud.");
        }
        return (Map<String, Object>) valor;
    }

    // ==================== Lectura de valores ====================

    private Object valor() {
        espacios();
        if (posicion >= texto.length()) {
            throw error("JSON vacío o incompleto.");
        }
        char c = texto.charAt(posicion);
        switch (c) {
            case '{':
                return objeto();
            case '[':
                return arreglo();
            case '"':
                return cadena();
            case 't':
                return literal("true", Boolean.TRUE);
            case 'f':
                return literal("false", Boolean.FALSE);
            case 'n':
                return literal("null", null);
            default:
                if (c == '-' || (c >= '0' && c <= '9')) {
                    return numero();
                }
                throw error("Carácter inesperado: " + c);
        }
    }

    private Map<String, Object> objeto() {
        entrar();
        Map<String, Object> mapa = new LinkedHashMap<>();
        posicion++; // {
        espacios();
        if (posicion < texto.length() && texto.charAt(posicion) == '}') {
            posicion++;
            salir();
            return mapa;
        }
        while (true) {
            espacios();
            if (posicion >= texto.length() || texto.charAt(posicion) != '"') {
                throw error("Se esperaba el nombre de una propiedad.");
            }
            String clave = cadena();
            espacios();
            if (posicion >= texto.length() || texto.charAt(posicion) != ':') {
                throw error("Se esperaba ':' después de la propiedad.");
            }
            posicion++;
            mapa.put(clave, valor());
            espacios();
            if (posicion >= texto.length()) {
                throw error("Objeto JSON sin cerrar.");
            }
            char c = texto.charAt(posicion++);
            if (c == '}') {
                break;
            }
            if (c != ',') {
                throw error("Se esperaba ',' o '}'.");
            }
        }
        salir();
        return mapa;
    }

    private List<Object> arreglo() {
        entrar();
        List<Object> lista = new ArrayList<>();
        posicion++; // [
        espacios();
        if (posicion < texto.length() && texto.charAt(posicion) == ']') {
            posicion++;
            salir();
            return lista;
        }
        while (true) {
            lista.add(valor());
            espacios();
            if (posicion >= texto.length()) {
                throw error("Arreglo JSON sin cerrar.");
            }
            char c = texto.charAt(posicion++);
            if (c == ']') {
                break;
            }
            if (c != ',') {
                throw error("Se esperaba ',' o ']'.");
            }
        }
        salir();
        return lista;
    }

    private String cadena() {
        posicion++; // "
        StringBuilder sb = new StringBuilder();
        while (posicion < texto.length()) {
            char c = texto.charAt(posicion++);
            if (c == '"') {
                return sb.toString();
            }
            if (c == '\\') {
                if (posicion >= texto.length()) {
                    throw error("Secuencia de escape incompleta.");
                }
                char escape = texto.charAt(posicion++);
                switch (escape) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        if (posicion + 4 > texto.length()) {
                            throw error("Secuencia unicode incompleta.");
                        }
                        String hexadecimal = texto.substring(posicion, posicion + 4);
                        try {
                            sb.append((char) Integer.parseInt(hexadecimal, 16));
                        } catch (NumberFormatException e) {
                            throw error("Secuencia unicode inválida.");
                        }
                        posicion += 4;
                        break;
                    default:
                        throw error("Escape inválido: \\" + escape);
                }
            } else if (c < 0x20) {
                throw error("Carácter de control no permitido dentro de una cadena.");
            } else {
                sb.append(c);
            }
        }
        throw error("Cadena sin cerrar.");
    }

    private Object numero() {
        int inicio = posicion;
        if (texto.charAt(posicion) == '-') {
            posicion++;
        }
        while (posicion < texto.length() && Character.isDigit(texto.charAt(posicion))) {
            posicion++;
        }
        boolean decimal = false;
        if (posicion < texto.length() && texto.charAt(posicion) == '.') {
            decimal = true;
            posicion++;
            while (posicion < texto.length() && Character.isDigit(texto.charAt(posicion))) {
                posicion++;
            }
        }
        if (posicion < texto.length()
                && (texto.charAt(posicion) == 'e' || texto.charAt(posicion) == 'E')) {
            decimal = true;
            posicion++;
            if (posicion < texto.length()
                    && (texto.charAt(posicion) == '+' || texto.charAt(posicion) == '-')) {
                posicion++;
            }
            while (posicion < texto.length() && Character.isDigit(texto.charAt(posicion))) {
                posicion++;
            }
        }
        String numero = texto.substring(inicio, posicion);
        try {
            if (!decimal) {
                return Long.valueOf(numero);
            }
            return new BigDecimal(numero);
        } catch (NumberFormatException e) {
            throw error("Número inválido.");
        }
    }

    private Object literal(String esperado, Object valor) {
        if (!texto.startsWith(esperado, posicion)) {
            throw error("Se esperaba '" + esperado + "'.");
        }
        posicion += esperado.length();
        return valor;
    }

    private void espacios() {
        while (posicion < texto.length() && Character.isWhitespace(texto.charAt(posicion))) {
            posicion++;
        }
    }

    private void entrar() {
        profundidad++;
        if (profundidad > ApiConstantes.MAX_PROFUNDIDAD_JSON) {
            throw new ApiExcepcion(400, "JSON_INVALIDO",
                    "El JSON supera la profundidad máxima permitida.");
        }
    }

    private void salir() {
        profundidad--;
    }

    private ApiExcepcion error(String mensaje) {
        return new ApiExcepcion(400, "JSON_INVALIDO",
                mensaje + " (posición " + posicion + ").");
    }

    // ==================== Acceso tipado al objeto leído ====================

    /** Devuelve un texto recortado o null si el campo no existe. */
    public static String texto(Map<String, Object> datos, String clave) {
        Object valor = datos.get(clave);
        if (valor == null) {
            return null;
        }
        String texto = String.valueOf(valor).trim();
        return texto.isEmpty() ? null : texto;
    }

    /** Devuelve un texto obligatorio o lanza 400 si falta o está vacío. */
    public static String textoRequerido(Map<String, Object> datos, String clave) {
        String valor = texto(datos, clave);
        if (valor == null) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El campo '" + clave + "' es obligatorio.");
        }
        return valor;
    }

    /** Devuelve un entero o null si el campo no existe. */
    public static Integer entero(Map<String, Object> datos, String clave) {
        Object valor = datos.get(clave);
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number) {
            return ((Number) valor).intValue();
        }
        String texto = String.valueOf(valor).trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException e) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El campo '" + clave + "' debe ser un número entero.");
        }
    }

    /** Devuelve un entero obligatorio o lanza 400. */
    public static Integer enteroRequerido(Map<String, Object> datos, String clave) {
        Integer valor = entero(datos, clave);
        if (valor == null) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El campo '" + clave + "' es obligatorio.");
        }
        return valor;
    }

    /** Devuelve un decimal o null si el campo no existe. */
    public static BigDecimal decimal(Map<String, Object> datos, String clave) {
        Object valor = datos.get(clave);
        if (valor == null) {
            return null;
        }
        if (valor instanceof BigDecimal) {
            return (BigDecimal) valor;
        }
        if (valor instanceof Number) {
            return new BigDecimal(valor.toString());
        }
        String texto = String.valueOf(valor).trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                    "El campo '" + clave + "' debe ser un número decimal.");
        }
    }

    /** Devuelve un booleano o null si el campo no existe. */
    public static Boolean booleano(Map<String, Object> datos, String clave) {
        Object valor = datos.get(clave);
        if (valor == null) {
            return null;
        }
        if (valor instanceof Boolean) {
            return (Boolean) valor;
        }
        String texto = String.valueOf(valor).trim();
        if ("true".equalsIgnoreCase(texto) || "1".equals(texto)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(texto) || "0".equals(texto)) {
            return Boolean.FALSE;
        }
        throw new ApiExcepcion(400, "DATOS_INVALIDOS",
                "El campo '" + clave + "' debe ser true o false.");
    }
}
