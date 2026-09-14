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
        String sql = "SELECT id, nombre, descripcion, email, telefono FROM empresa";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Empresa empresa = new Empresa();
                empresa.setId(rs.getInt("id"));
                empresa.setNombre(rs.getString("nombre"));
                empresa.setDescripcion(rs.getString("descripcion"));
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
        String sql = "SELECT id, nombre, descripcion, email, telefono FROM empresa WHERE id = ?";
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
                    empresa.setEmail(rs.getString("email"));
                    empresa.setTelefono(rs.getString("telefono"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener empresa por ID: " + e.getMessage());
        }

        return empresa;
    }
}
