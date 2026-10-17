package com.empleasv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Excepción de negocio para operaciones de registro y actualización de cuentas.
 * Transporta una lista de mensajes aptos para mostrar al usuario.
 */
public class RegistroException extends Exception {

    private final List<String> errores;

    public RegistroException(String mensaje) {
        super(mensaje);
        this.errores = Collections.singletonList(mensaje);
    }

    public RegistroException(List<String> errores) {
        super(errores == null || errores.isEmpty()
                ? "Error de validacion" : String.join("; ", errores));
        this.errores = errores != null ? new ArrayList<>(errores) : new ArrayList<>();
    }

    public List<String> getErrores() {
        return Collections.unmodifiableList(errores);
    }
}
