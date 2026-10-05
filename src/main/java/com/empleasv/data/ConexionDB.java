package com.empleasv.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestiona la conexión con la base de datos mediante JDBC puro.
 *
 * @deprecated Esta clase se mantiene solo para compatibilidad con código legacy
 * y posibles scripts de migración. Los nuevos DAOs usan JPA/Hibernate a través
 * de {@link com.empleasv.config.JpaConfig}.
 */
@Deprecated
public class ConexionDB {

    // Dirección de la base de datos
    private static final String URL = "jdbc:mysql://localhost:3306/empleasv";

    // Datos de acceso a MySQL
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "";

    // Carga el driver JDBC
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL cargado correctamente (legacy ConexionDB).");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver MySQL: " + e.getMessage());
        }
    }

    // Abre una conexión con la base de datos
    public static Connection obtenerConexion() throws SQLException {
        Connection conexion = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
        System.out.println("Conexion exitosa a la base de datos empleasv (legacy ConexionDB).");
        return conexion;
    }
}