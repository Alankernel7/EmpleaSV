package com.empleasv.dao;

import com.empleasv.data.ConexionDB;
import com.empleasv.model.Empresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DAO para operaciones CRUD de la tabla empresa
public class EmpresaDAO {

    // Lista todas las empresas
    public List<Empresa> listarTodas() {
        List<Empresa> empresas = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, categoria, email, telefono FROM empresa";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Empresa empresa = new Empresa();
                empresa.setId(rs.getInt("id"));
                empresa.setNombre(rs.getString("nombre"));
                empresa.setDescripcion(rs.getString("descripcion"));
                empresa.setCategoria(rs.getString("categoria"));
                empresa.setEmail(rs.getString("email"));
                empresa.setTelefono(rs.getString("telefono"));
                empresas.add(empresa);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar empresas: " + e.getMessage());
        }

        return empresas;
    }

    // Obtiene una empresa por su ID
    public Empresa obtenerPorId(int id) {
        String sql = "SELECT id, nombre, descripcion, categoria, email, telefono FROM empresa WHERE id = ?";
        Empresa empresa = null;

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    empresa = new Empresa();
                    empresa.setId(rs.getInt("id"));
                    empresa.setNombre(rs.getString("nombre"));
                    empresa.setDescripcion(rs.getString("descripcion"));
                    empresa.setCategoria(rs.getString("categoria"));
                    empresa.setEmail(rs.getString("email"));
                    empresa.setTelefono(rs.getString("telefono"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener empresa por ID: " + e.getMessage());
        }

        return empresa;
    }

    // Lista las 4 empresas con mayor cantidad de ofertas publicadas
    public List<Empresa> listarDestacadas() {
        List<Empresa> empresas = new ArrayList<>();

        String sql = "SELECT e.id, e.nombre, e.descripcion, e.categoria, e.email, e.telefono, "
                + "COUNT(o.id) AS cantidad_ofertas "
                + "FROM empresa e "
                + "LEFT JOIN oferta_empleo o ON e.id = o.empresa_id "
                + "GROUP BY e.id, e.nombre, e.descripcion, e.categoria, e.email, e.telefono "
                + "ORDER BY cantidad_ofertas DESC "
                + "LIMIT 4";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Empresa empresa = new Empresa();

                empresa.setId(rs.getInt("id"));
                empresa.setNombre(rs.getString("nombre"));
                empresa.setDescripcion(rs.getString("descripcion"));
                empresa.setCategoria(rs.getString("categoria"));
                empresa.setEmail(rs.getString("email"));
                empresa.setTelefono(rs.getString("telefono"));

                empresa.setCantidadOfertas(
                        rs.getInt("cantidad_ofertas")
                );

                empresas.add(empresa);
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al listar empresas destacadas: "
                            + e.getMessage()
            );
        }

        return empresas;
    }

    // Lista todas las empresas junto con su cantidad de ofertas
    public List<Empresa> listarConCantidadOfertas() {

        List<Empresa> empresas = new ArrayList<>();

        String sql =
                "SELECT e.id, e.nombre, e.descripcion, e.categoria, "
                        + "e.email, e.telefono, COUNT(o.id) AS cantidad_ofertas "
                        + "FROM empresa e "
                        + "LEFT JOIN oferta_empleo o ON e.id = o.empresa_id "
                        + "GROUP BY e.id, e.nombre, e.descripcion, e.categoria, "
                        + "e.email, e.telefono "
                        + "ORDER BY e.nombre ASC";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Empresa empresa = new Empresa();

                empresa.setId(rs.getInt("id"));
                empresa.setNombre(rs.getString("nombre"));
                empresa.setDescripcion(rs.getString("descripcion"));
                empresa.setCategoria(rs.getString("categoria"));
                empresa.setEmail(rs.getString("email"));
                empresa.setTelefono(rs.getString("telefono"));

                empresa.setCantidadOfertas(
                        rs.getInt("cantidad_ofertas")
                );

                empresas.add(empresa);
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al listar empresas con cantidad de ofertas: "
                            + e.getMessage()
            );
        }

        return empresas;
    }

}

