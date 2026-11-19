package com.empleasv.model;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "archivo")
public class Archivo {

    public enum CategoriaArchivo {
        FOTO_PERFIL,
        CURRICULUM,
        LOGO_EMPRESA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "nombre_almacenado", nullable = false, length = 255)
    private String nombreAlmacenado;

    @Column(name = "ruta_relativa", nullable = false, length = 500)
    private String rutaRelativa;

    @Column(name = "tipo_mime", nullable = false, length = 100)
    private String tipoMime;

    @Column(name = "extension", nullable = false, length = 20)
    private String extension;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "categoria", nullable = false, length = 30)
    private CategoriaArchivo categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, referencedColumnName = "id")
    private Usuario usuario;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    public Archivo() {
    }

    public Archivo(Integer id, String nombreOriginal, String nombreAlmacenado, String rutaRelativa,
                   String tipoMime, String extension, Long tamanoBytes, CategoriaArchivo categoria,
                   Usuario usuario, LocalDateTime fechaSubida) {
        this.id = id;
        this.nombreOriginal = nombreOriginal;
        this.nombreAlmacenado = nombreAlmacenado;
        this.rutaRelativa = rutaRelativa;
        this.tipoMime = tipoMime;
        this.extension = extension;
        this.tamanoBytes = tamanoBytes;
        this.categoria = categoria;
        this.usuario = usuario;
        this.fechaSubida = fechaSubida;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getNombreAlmacenado() {
        return nombreAlmacenado;
    }

    public void setNombreAlmacenado(String nombreAlmacenado) {
        this.nombreAlmacenado = nombreAlmacenado;
    }

    public String getRutaRelativa() {
        return rutaRelativa;
    }

    public void setRutaRelativa(String rutaRelativa) {
        this.rutaRelativa = rutaRelativa;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public void setTipoMime(String tipoMime) {
        this.tipoMime = tipoMime;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(Long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public CategoriaArchivo getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaArchivo categoria) {
        this.categoria = categoria;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public String getRutaAbsoluta(String basePath) {
        if (basePath == null || basePath.isEmpty()) {
            return rutaRelativa;
        }
        String normalizedBase = basePath.endsWith("/") ? basePath : basePath + "/";
        return normalizedBase + rutaRelativa;
    }

    @Override
    public String toString() {
        return "Archivo{" +
                "id=" + id +
                ", nombreOriginal='" + nombreOriginal + '\'' +
                ", categoria=" + categoria +
                ", tamanoBytes=" + tamanoBytes +
                '}';
    }
}