

CREATE DATABASE IF NOT EXISTS empleasv
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE empleasv;

-- ------------------------------------------------------------
-- Tabla: empresa
-- ------------------------------------------------------------
CREATE TABLE empresa (
    id          INT             AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150)    NOT NULL,
    descripcion TEXT,
    email       VARCHAR(150)    NOT NULL,
    telefono    VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Tabla: oferta_empleo
-- ------------------------------------------------------------
CREATE TABLE oferta_empleo (
    id                INT             AUTO_INCREMENT PRIMARY KEY,
    titulo            VARCHAR(200)    NOT NULL,
    descripcion       TEXT            NOT NULL,
    requisitos        TEXT,
    ubicacion         VARCHAR(150)    NOT NULL,
    salario           DECIMAL(10,2),
    tipo_contrato     VARCHAR(50)     NOT NULL,
    horario           VARCHAR(100),
    fecha_publicacion DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado            VARCHAR(30)     NOT NULL DEFAULT 'ACTIVA',
    empresa_id        INT             NOT NULL,
    CONSTRAINT fk_oferta_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresa(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
