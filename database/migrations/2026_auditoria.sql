-- ============================================================
-- EmpleaSV - Migración: tabla auditoria
-- ============================================================
-- Para bases de datos EXISTENTES (ya creadas con una versión
-- anterior de empleasv.sql). En instalaciones nuevas, la tabla
-- ya viene incluida en database/empleasv.sql.
--
-- Ejecutar UNA SOLA VEZ en la base `empleasv`.
-- No modifica tablas existentes ni toca datos.
-- ============================================================

USE empleasv;

CREATE TABLE IF NOT EXISTS auditoria (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    admin_id        INT             NOT NULL,
    accion          VARCHAR(50)     NOT NULL,
    objetivo_tipo   VARCHAR(30)     NOT NULL,
    objetivo_id     INT             NULL,
    detalle         VARCHAR(255)    NULL,
    fecha           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_admin
        FOREIGN KEY (admin_id)
        REFERENCES usuario(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Los siguientes índices se crean solo si la tabla es nueva;
-- si ya existen, MySQL reportará un error harmless de índice
-- duplicado que puede ignorarse.
CREATE INDEX idx_auditoria_fecha ON auditoria(fecha);
CREATE INDEX idx_auditoria_admin ON auditoria(admin_id);
CREATE INDEX idx_auditoria_objetivo ON auditoria(objetivo_tipo, objetivo_id);

-- Verificación:
--   SHOW CREATE TABLE auditoria;
-- =========================================================
