-- ============================================================
-- EmpleaSV - Migracion: tabla postulacion
-- ============================================================
-- Para bases de datos EXISTENTES (ya creadas con una version
-- anterior de empleasv.sql). En instalaciones nuevas, la tabla
-- ya viene incluida en database/empleasv.sql.
--
-- Ejecutar UNA SOLA VEZ en la base `empleasv`.
-- No modifica tablas existentes ni toca datos.
--
-- La tabla guarda el curriculum exacto que el candidato envio
-- en cada postulacion. Aunque luego cambie el CV de su perfil,
-- la empresa sigue viendo el documento original.
-- ============================================================

USE empleasv;

CREATE TABLE IF NOT EXISTS postulacion (
    id                      INT             AUTO_INCREMENT PRIMARY KEY,
    oferta_id               INT             NOT NULL,
    usuario_id              INT             NOT NULL,
    curriculum_id           INT             NOT NULL,
    mensaje_presentacion    TEXT            NULL,
    estado                  VARCHAR(30)     NOT NULL DEFAULT 'PENDIENTE',
    fecha_postulacion       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_postulacion_oferta_usuario
        UNIQUE (oferta_id, usuario_id),
    CONSTRAINT fk_postulacion_oferta
        FOREIGN KEY (oferta_id)
        REFERENCES oferta_empleo(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_postulacion_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_postulacion_curriculum
        FOREIGN KEY (curriculum_id)
        REFERENCES archivo(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_postulacion_usuario ON postulacion(usuario_id);
CREATE INDEX idx_postulacion_oferta ON postulacion(oferta_id);
CREATE INDEX idx_postulacion_curriculum ON postulacion(curriculum_id);
CREATE INDEX idx_postulacion_estado ON postulacion(estado);

-- Verificacion:
--   SHOW CREATE TABLE postulacion;
-- =========================================================
