package com.empleasv.util;

/**
 * Excepción lanzada cuando un archivo subido no cumple las reglas de
 * seguridad (extensión, MIME, tamaño o contenido).
 * El mensaje es apto para mostrarse al usuario final.
 */
public class ArchivoNoValidoException extends Exception {

    public ArchivoNoValidoException(String mensaje) {
        super(mensaje);
    }

    public ArchivoNoValidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
