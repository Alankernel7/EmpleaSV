package com.empleasv.api;

import java.lang.reflect.Array;
import java.util.Map;

/**
 * Serializador JSON mínimo para la capa REST.
 *
 * El proyecto no usa Jackson/Gson y no se agregan dependencias nuevas.
 * Esta clase soporta los tipos necesarios para las respuestas de la API:
 * null, String, Number, Boolean, Map, Iterable y arreglos.
 *
 * Las claves de los mapas deben estar ordenadas por quien construye la
 * respuesta (se recomienda LinkedHashMap) para que la salida sea estable.
 */
public final class ApiJson {

    private ApiJson() {
    }

    /** Escapa una cadena para incluirla dentro de un valor JSON. */
    public static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(valor.length() + 16);
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /** Convierte un valor soportado a su representación JSON. */
    public static String serializar(Object valor) {
        StringBuilder sb = new StringBuilder();
        escribir(valor, sb);
        return sb.toString();
    }

    private static void escribir(Object valor, StringBuilder sb) {
        if (valor == null) {
            sb.append("null");
            return;
        }
        if (valor instanceof String) {
            sb.append('"').append(escapar((String) valor)).append('"');
            return;
        }
        if (valor instanceof Boolean || valor instanceof Number) {
            sb.append(valor.toString());
            return;
        }
        if (valor instanceof Map<?, ?>) {
            sb.append('{');
            boolean primero = true;
            for (Map.Entry<?, ?> entrada : ((Map<?, ?>) valor).entrySet()) {
                if (!primero) {
                    sb.append(',');
                }
                primero = false;
                sb.append('"').append(escapar(String.valueOf(entrada.getKey()))).append("\":");
                escribir(entrada.getValue(), sb);
            }
            sb.append('}');
            return;
        }
        if (valor instanceof Iterable<?>) {
            sb.append('[');
            boolean primero = true;
            for (Object item : (Iterable<?>) valor) {
                if (!primero) {
                    sb.append(',');
                }
                primero = false;
                escribir(item, sb);
            }
            sb.append(']');
            return;
        }
        if (valor.getClass().isArray()) {
            sb.append('[');
            int longitud = Array.getLength(valor);
            for (int i = 0; i < longitud; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                escribir(Array.get(valor, i), sb);
            }
            sb.append(']');
            return;
        }
        sb.append('"').append(escapar(String.valueOf(valor))).append('"');
    }
}
