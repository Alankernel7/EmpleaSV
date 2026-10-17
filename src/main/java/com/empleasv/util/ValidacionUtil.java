package com.empleasv.util;

import java.util.regex.Pattern;

/**
 * Expresiones regulares y validaciones comunes de entrada.
 */
public final class ValidacionUtil {

    /** Formato válido de correo electrónico. */
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    private ValidacionUtil() {
    }

    /** Indica si el correo tiene un formato válido. */
    public static boolean esEmailValido(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
