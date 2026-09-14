package com.empleasv.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Gestiona la conexión con la base de datos
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
            System.out.println("Driver MySQL cargado correctamente.");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver MySQL: " + e.getMessage());
        }
    }

    // Abre una conexión con la base de datos
    public static Connection obtenerConexion() throws SQLException {
        Connection conexion = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
        System.out.println("Conexion exitosa a la base de datos empleasv.");
        return conexion;
    }
}