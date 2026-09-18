package com.empleasv.dao;

import com.empleasv.data.ConexionDB;
import com.empleasv.model.Empresa;
import com.empleasv.model.OfertaEmpleo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// DAO para operaciones CRUD de la tabla oferta_empleo
public class OfertaEmpleoDAO {

    // Convierte un ResultSet en un objeto OfertaEmpleo
    private OfertaEmpleo mapearResultSet(ResultSet rs) throws SQLException {
        OfertaEmpleo oferta = new OfertaEmpleo();
        oferta.setId(rs.getInt("id"));
        oferta.setTitulo(rs.getString("titulo"));
        oferta.setDescripcion(rs.getString("descripcion"));
        oferta.setRequisitos(rs.getString("requisitos"));
        oferta.setUbicacion(rs.getString("ubicacion"));

        double salario = rs.getDouble("salario");
        if (rs.wasNull()) {
            oferta.setSalario(null);
        } else {
            oferta.setSalario(salario);
        }

        oferta.setTipoContrato(rs.getString("tipo_contrato"));
        oferta.setHorario(rs.getString("horario"));

        Timestamp ts = rs.getTimestamp("fecha_publicacion");
        if (ts != null) {
            oferta.setFechaPublicacion(ts.toLocalDateTime());
        }

        oferta.setEstado(rs.getString("estado"));
        oferta.setEmpresaId(rs.getInt("empresa_id"));
        return oferta;
    }

    // Lista todas las ofertas
    public List<OfertaEmpleo> listarTodas() {
        List<OfertaEmpleo> ofertas = new ArrayList<>();
        String sql = "SELECT id, titulo, descripcion, requisitos, ubicacion, salario, "
                   + "tipo_contrato, horario, fecha_publicacion, estado, empresa_id "
                   + "FROM oferta_empleo";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ofertas.add(mapearResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar ofertas: " + e.getMessage());
        }

        return ofertas;
    }


    // Obtiene una oferta por su ID
    public OfertaEmpleo obtenerPorId(int id) {
        String sql = "SELECT id, titulo, descripcion, requisitos, ubicacion, salario, "
                   + "tipo_contrato, horario, fecha_publicacion, estado, empresa_id "
                   + "FROM oferta_empleo WHERE id = ?";
        OfertaEmpleo oferta = null;

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    oferta = mapearResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener oferta por ID: " + e.getMessage());
        }

        return oferta;
    }

    // Lista las ofertas activas más recientes para mostrar en el inicio
    public List<OfertaEmpleo> listarDestacadas() {
        List<OfertaEmpleo> ofertas = new ArrayList<>();

        String sql = "SELECT o.id, o.titulo, o.descripcion, o.requisitos, "
                + "o.ubicacion, o.salario, o.tipo_contrato, o.horario, "
                + "o.fecha_publicacion, o.estado, o.empresa_id, "
                + "e.nombre AS empresa_nombre "
                + "FROM oferta_empleo o "
                + "INNER JOIN empresa e ON o.empresa_id = e.id "
                + "WHERE o.estado = 'ACTIVA' "
                + "ORDER BY o.fecha_publicacion DESC "
                + "LIMIT 6";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                OfertaEmpleo oferta = mapearResultSet(rs);

                Empresa empresa = new Empresa();
                empresa.setId(rs.getInt("empresa_id"));
                empresa.setNombre(rs.getString("empresa_nombre"));

                oferta.setEmpresa(empresa);

                ofertas.add(oferta);
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al listar ofertas destacadas: "
                            + e.getMessage()
            );
        }

        return ofertas;
    }

    // Busca ofertas por título (búsqueda parcial con LIKE)
    public List<OfertaEmpleo> buscarPorTitulo(String titulo) {
        List<OfertaEmpleo> ofertas = new ArrayList<>();
        String sql = "SELECT id, titulo, descripcion, requisitos, ubicacion, salario, "
                   + "tipo_contrato, horario, fecha_publicacion, estado, empresa_id "
                   + "FROM oferta_empleo WHERE titulo LIKE ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + titulo + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ofertas.add(mapearResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar ofertas por título: " + e.getMessage());
        }

        return ofertas;
    }

    // Filtra ofertas por ubicación exacta
    public List<OfertaEmpleo> filtrarPorUbicacion(String ubicacion) {
        List<OfertaEmpleo> ofertas = new ArrayList<>();
        String sql = "SELECT id, titulo, descripcion, requisitos, ubicacion, salario, "
                   + "tipo_contrato, horario, fecha_publicacion, estado, empresa_id "
                   + "FROM oferta_empleo WHERE ubicacion = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ubicacion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ofertas.add(mapearResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al filtrar ofertas por ubicación: " + e.getMessage());
        }

        return ofertas;
    }

    // Lista todas las ofertas pertenecientes a una empresa
    public List<OfertaEmpleo> listarPorEmpresa(int empresaId) {

        List<OfertaEmpleo> ofertas = new ArrayList<>();

        String sql =
                "SELECT id, titulo, descripcion, requisitos, ubicacion, salario, "
                        + "tipo_contrato, horario, fecha_publicacion, estado, empresa_id "
                        + "FROM oferta_empleo "
                        + "WHERE empresa_id = ? "
                        + "ORDER BY fecha_publicacion DESC";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empresaId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    ofertas.add(mapearResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al listar ofertas por empresa: "
                            + e.getMessage()
            );
        }

        return ofertas;
    }

    // Registra una nueva oferta en la base de datos
    public boolean registrar(OfertaEmpleo oferta) {
        String sql = "INSERT INTO oferta_empleo (titulo, descripcion, requisitos, ubicacion, "
                   + "salario, tipo_contrato, horario, fecha_publicacion, estado, empresa_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, oferta.getTitulo());
            ps.setString(2, oferta.getDescripcion());
            ps.setString(3, oferta.getRequisitos());
            ps.setString(4, oferta.getUbicacion());

            if (oferta.getSalario() != null) {
                ps.setDouble(5, oferta.getSalario());
            } else {
                ps.setNull(5, java.sql.Types.DOUBLE);
            }

            ps.setString(6, oferta.getTipoContrato());
            ps.setString(7, oferta.getHorario());

            if (oferta.getFechaPublicacion() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(oferta.getFechaPublicacion()));
            } else {
                ps.setNull(8, java.sql.Types.TIMESTAMP);
            }

            ps.setString(9, oferta.getEstado());
            ps.setInt(10, oferta.getEmpresaId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar oferta: " + e.getMessage());
            return false;
        }
    }

    // Actualiza una oferta existente en la base de datos
    public boolean actualizar(OfertaEmpleo oferta) {
        String sql = "UPDATE oferta_empleo SET titulo = ?, descripcion = ?, requisitos = ?, "
                   + "ubicacion = ?, salario = ?, tipo_contrato = ?, horario = ?, "
                   + "fecha_publicacion = ?, estado = ?, empresa_id = ? WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, oferta.getTitulo());
            ps.setString(2, oferta.getDescripcion());
            ps.setString(3, oferta.getRequisitos());
            ps.setString(4, oferta.getUbicacion());

            if (oferta.getSalario() != null) {
                ps.setDouble(5, oferta.getSalario());
            } else {
                ps.setNull(5, java.sql.Types.DOUBLE);
            }

            ps.setString(6, oferta.getTipoContrato());
            ps.setString(7, oferta.getHorario());

            if (oferta.getFechaPublicacion() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(oferta.getFechaPublicacion()));
            } else {
                ps.setNull(8, java.sql.Types.TIMESTAMP);
            }

            ps.setString(9, oferta.getEstado());
            ps.setInt(10, oferta.getEmpresaId());
            ps.setInt(11, oferta.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar oferta: " + e.getMessage());
            return false;
        }
    }

    // Elimina una oferta por su ID
    public boolean eliminar(int id) {
        String sql = "DELETE FROM oferta_empleo WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar oferta: " + e.getMessage());
            return false;
        }
    }
}
