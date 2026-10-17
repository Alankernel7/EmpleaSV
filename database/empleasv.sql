-- ============================================================
-- EmpleaSV - Script único de instalación
-- ============================================================
-- Este archivo crea la base de datos completa de EmpleaSV
-- desde cero: las siete tablas, relaciones, índices y datos
-- de prueba de empresas y ofertas.
--
-- Convención de claves (aplicable a todo el proyecto):
--   - La clave primaria de cada tabla se llama siempre `id`.
--   - Las claves foráneas llevan nombres descriptivos de su
--     relación: usuario_id, empresa_id, foto_perfil_id,
--     curriculum_id, logo_id.
--
-- INSTALACIÓN NUEVA (único paso):
--   1. Abrir phpMyAdmin (u otro cliente MySQL).
--   2. Seleccionar/importar este archivo completo.
--   3. La base de datos `empleasv` queda lista para la
--      versión actual de la aplicación.
--
-- NO ejecutar este script sobre una base de datos que ya
-- contenga datos: usa CREATE DATABASE IF NOT EXISTS y
-- CREATE TABLE IF NOT EXISTS, pero los INSERT de datos de
-- prueba sí se agregarían de nuevo. En instalaciones nuevas
-- no hay conflicto.
--
-- CUENTA ADMINISTRADORA:
--   Este script NO crea usuarios con contraseñas de ejemplo.
--   Para crear el primer administrador de forma segura:
--     a) Registrar una cuenta de candidato o empresa desde
--        la aplicación (http://localhost:8080/EmpleaSV/auth?accion=registro),
--        y luego, desde MySQL, promoverla:
--          UPDATE usuario SET rol = 'ADMINISTRADOR'
--           WHERE email = 'tu-correo@ejemplo.com';
--     b) O generar un hash con PasswordUtil de la aplicación
--        e insertar la fila con ese hash real.
-- ============================================================

CREATE DATABASE IF NOT EXISTS empleasv
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE empleasv;

-- ------------------------------------------------------------
-- Tabla: empresa
-- Datos públicos de las empresas que publican ofertas.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS empresa (
    id           INT             AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(150)    NOT NULL,
    descripcion  TEXT,
    categoria    VARCHAR(100)    NOT NULL,
    email        VARCHAR(150)    NOT NULL,
    telefono     VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Tabla: usuario
-- Cuentas de la plataforma (candidato, empresa o administrador).
-- `rol` es VARCHAR(20), compatible con el mapeo EnumType.STRING
-- de Hibernate (USUARIO | EMPRESA | ADMINISTRADOR).
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id               INT             AUTO_INCREMENT PRIMARY KEY,
    nombre             VARCHAR(150)    NOT NULL,
    email              VARCHAR(150)    NOT NULL UNIQUE,
    password_hash      VARCHAR(255)    NOT NULL,
    rol                VARCHAR(20)     NOT NULL DEFAULT 'USUARIO',
    estado             VARCHAR(20)     NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Tabla: oferta_empleo
-- Ofertas publicadas por una empresa de la tabla `empresa`.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS oferta_empleo (
    id                  INT             AUTO_INCREMENT PRIMARY KEY,
    titulo              VARCHAR(200)    NOT NULL,
    descripcion         TEXT            NOT NULL,
    requisitos          TEXT,
    ubicacion           VARCHAR(150)    NOT NULL,
    salario             DECIMAL(10,2),
    tipo_contrato       VARCHAR(50)     NOT NULL,
    horario             VARCHAR(100),
    fecha_publicacion   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado              VARCHAR(30)     NOT NULL DEFAULT 'ACTIVA',
    empresa_id          INT             NOT NULL,
    CONSTRAINT fk_oferta_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresa(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_oferta_empresa ON oferta_empleo(empresa_id);
CREATE INDEX idx_oferta_estado ON oferta_empleo(estado);
CREATE INDEX idx_oferta_ubicacion ON oferta_empleo(ubicacion);

-- ------------------------------------------------------------
-- Tabla: archivo
-- Metadatos de archivos subidos (fotos de perfil, currículums,
-- logos). Los archivos físicos se guardan fuera del directorio
-- público, en la ruta configurada en web.xml.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS archivo (
    id                  INT             AUTO_INCREMENT PRIMARY KEY,
    nombre_original     VARCHAR(255)    NOT NULL,
    nombre_almacenado   VARCHAR(255)    NOT NULL,
    ruta_relativa       VARCHAR(500)    NOT NULL,
    tipo_mime           VARCHAR(100)    NOT NULL,
    extension           VARCHAR(20)     NOT NULL,
    tamano_bytes        BIGINT          NOT NULL,
    categoria           VARCHAR(30)     NOT NULL,  -- 'FOTO_PERFIL', 'CURRICULUM', 'LOGO_EMPRESA'
    usuario_id          INT             NOT NULL,
    fecha_subida        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_archivo_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_archivo_usuario_categoria ON archivo(usuario_id, categoria);
CREATE INDEX idx_archivo_categoria ON archivo(categoria);

-- ------------------------------------------------------------
-- Tabla: perfil_candidato
-- Perfil extendido de los usuarios con rol USUARIO.
-- Se crea automáticamente al registrarse como candidato o al
-- editar el perfil.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS perfil_candidato (
    id                      INT             AUTO_INCREMENT PRIMARY KEY,
    usuario_id              INT             NOT NULL UNIQUE,
    telefono                VARCHAR(30),
    direccion_residencia    VARCHAR(255),
    municipio               VARCHAR(100),
    departamento            VARCHAR(100),
    foto_perfil_id          INT             NULL,
    curriculum_id           INT             NULL,
    fecha_actualizacion     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_perfil_candidato_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_perfil_candidato_foto
        FOREIGN KEY (foto_perfil_id)
        REFERENCES archivo(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT fk_perfil_candidato_curriculum
        FOREIGN KEY (curriculum_id)
        REFERENCES archivo(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_perfil_candidato_usuario ON perfil_candidato(usuario_id);

-- ------------------------------------------------------------
-- Tabla: perfil_empresa
-- Perfil de los usuarios con rol EMPRESA.
-- `empresa_id` vincula la cuenta con la fila de `empresa` que
-- publica las ofertas de empleo (misma fila que usa
-- oferta_empleo.empresa_id). Cada cuenta solo puede tener una
-- empresa y esa empresa no puede ser usada por otra cuenta.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS perfil_empresa (
    id                      INT             AUTO_INCREMENT PRIMARY KEY,
    usuario_id              INT             NOT NULL UNIQUE,
    nombre_responsable      VARCHAR(150)    NOT NULL,
    nombre_empresa          VARCHAR(150)    NOT NULL,
    nit                     VARCHAR(50),
    telefono_empresa        VARCHAR(30),
    direccion_empresa       VARCHAR(255),
    municipio               VARCHAR(100),
    departamento            VARCHAR(100),
    sector                  VARCHAR(100),
    sitio_web               VARCHAR(255),
    descripcion             TEXT,
    logo_id                 INT             NULL,
    empresa_id              INT             NULL,
    fecha_actualizacion     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_perfil_empresa_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_perfil_empresa_logo
        FOREIGN KEY (logo_id)
        REFERENCES archivo(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT fk_perfil_empresa_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresa(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_perfil_empresa_usuario ON perfil_empresa(usuario_id);
CREATE INDEX idx_perfil_empresa_nombre ON perfil_empresa(nombre_empresa);
CREATE UNIQUE INDEX uq_perfil_empresa_empresa ON perfil_empresa(empresa_id);

-- ------------------------------------------------------------
-- Tabla: auditoria
-- Registro de acciones administrativas (activar/desactivar
-- cuentas, cambiar estado de ofertas). Solo guarda el ID del
-- administrador, la acción, el objetivo y la fecha: nunca
-- contraseñas ni hashes. Sin borrado físico.
-- ------------------------------------------------------------
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

CREATE INDEX idx_auditoria_fecha ON auditoria(fecha);
CREATE INDEX idx_auditoria_admin ON auditoria(admin_id);
CREATE INDEX idx_auditoria_objetivo ON auditoria(objetivo_tipo, objetivo_id);

-- ============================================================
-- Datos de prueba: empresas
-- (No incluye usuarios ni contraseñas; ver encabezado del
-- archivo para crear el administrador de forma segura.)
-- ============================================================

INSERT INTO empresa
(nombre, descripcion, categoria, email, telefono)
VALUES

    (
        'NovaTech Solutions',
        'Empresa salvadoreña dedicada al desarrollo de software, aplicaciones web y soluciones digitales para empresas.',
        'Tecnología',
        'contacto@novatechsv.com',
        '2201-4580'
    ),

    (
        'Grupo Creativa',
        'Agencia especializada en diseño gráfico, marketing digital, publicidad y creación de contenido.',
        'Marketing',
        'talento@grupocreativa.com',
        '2264-7812'
    ),

    (
        'Servicios Corporativos Centroamericanos',
        'Empresa dedicada a servicios administrativos, financieros y de atención empresarial.',
        'Servicios Corporativos',
        'rrhh@serviciosca.com',
        '2521-3390'
    ),

    (
        'Digital Commerce SV',
        'Empresa enfocada en comercio electrónico, marketing, ventas digitales y administración de tiendas en línea.',
        'Comercio Electronico',
        'empleos@digitalcommercesv.com',
        '2243-9075'
    ),

    (
        'Innova Systems',
        'Compañía especializada en infraestructura tecnológica, soporte técnico, redes y servicios informáticos.',
        'Tecnologia',
        'reclutamiento@innovasystems.com',
        '2133-6250'
    ),

    (
        'Conecta Business',
        'Empresa dedicada a ventas, servicio al cliente y soluciones comerciales para pequeñas y medianas empresas.',
        'Ventas',
        'talento@conectabusiness.com',
        '2298-4110'
    ),

    (
        'TechCorp El Salvador',
        'Empresa líder en soluciones tecnológicas y desarrollo de software a medida.',
        'Tecnologia',
        'rrhh@techcorp.com',
        '2264-5415'
    ),

    (
         'Creative Studio',
         'Agencia de diseño y marketing digital especializada en branding y campañas publicitarias.',
         'Marketing',
         'contacto@creativestudiosv.com',
         '2245-7890'
    ),

    (
        'Grupo Meridian',
        'Empresa dedicada a servicios de marketing digital.',
        'Marketing',
        'talento@meridian.com',
        '2155-1831'
    ),

    (
        'DataVision SA',
        'Empresa especializada en análisis de datos y soluciones de inteligencia empresarial.',
        'Analitica de Datos',
        'contacto@datavisionsv.com',
        '2263-6567'
    ),

    (
        'Consultores Asociados',
        'Firma de consultoría en gestión empresarial y desarrollo organizacional.',
        'Consultoria',
        'rrhh@consultoresasociadossv.com',
        '2284-9362'
    ),

    (
        'NetSolutions SV',
        'Empresa de servicios de tecnología y soluciones de redes para empresas.',
        'Tecnologia',
        'empleos@netsolutionssv.com',
        '2291-4720'
     );


-- ============================================================
-- Datos de prueba: ofertas de empleo
-- ============================================================

INSERT INTO oferta_empleo
(
    titulo,
    descripcion,
    requisitos,
    ubicacion,
    salario,
    tipo_contrato,
    horario,
    fecha_publicacion,
    estado,
    empresa_id
)
VALUES

-- ------------------------------------------------------------
-- Empresa 1: NovaTech Solutions
-- ------------------------------------------------------------

(
    'Desarrollador Java Junior',
    'Buscamos desarrollador junior para participar en el desarrollo y mantenimiento de aplicaciones web empresariales utilizando Java.',
    'Estudiante o graduado de Ingeniería en Sistemas, Ciencias de la Computación o carrera relacionada. Conocimientos de Java, SQL, HTML, CSS y Git.',
    'San Salvador',
    850.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-14 08:30:00',
    'ACTIVA',
    1
),

(
    'Desarrollador Frontend',
    'Responsable de implementar interfaces web responsivas y colaborar con el equipo de diseño y desarrollo backend.',
    'Conocimientos de HTML5, CSS3, JavaScript, diseño responsivo y consumo de APIs REST.',
    'Antiguo Cuscatlán',
    950.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-10 10:15:00',
    'ACTIVA',
    1
),

(
    'Practicante de Desarrollo Web',
    'Apoyo en desarrollo, pruebas y mantenimiento de aplicaciones internas bajo supervisión del equipo de tecnología.',
    'Estudiante universitario con conocimientos básicos de Java, HTML, CSS, JavaScript y bases de datos.',
    'San Salvador',
    400.00,
    'Practica',
    '8:00 AM - 1:00 PM',
    '2026-09-03 09:00:00',
    'ACTIVA',
    1
),


-- ------------------------------------------------------------
-- Empresa 2: Grupo Creativa
-- ------------------------------------------------------------

(
    'Diseñador UI/UX Junior',
    'Creación de interfaces digitales, prototipos y piezas gráficas para proyectos web y campañas digitales.',
    'Manejo de Figma, principios de diseño UI/UX, diseño responsivo y portafolio de trabajos.',
    'Santa Tecla',
    750.00,
    'Tiempo completo',
    '9:00 AM - 6:00 PM',
    '2026-09-12 13:20:00',
    'ACTIVA',
    2
),

(
    'Diseñador Gráfico Freelance',
    'Creación de piezas gráficas para redes sociales, campañas publicitarias y materiales promocionales.',
    'Experiencia utilizando Illustrator, Photoshop o herramientas equivalentes. Portafolio indispensable.',
    'Remoto',
    600.00,
    'Freelance',
    'Horario flexible',
    '2026-08-28 11:45:00',
    'ACTIVA',
    2
),


-- ------------------------------------------------------------
-- Empresa 3: Servicios Corporativos Centroamericanos
-- ------------------------------------------------------------

(
    'Asistente Administrativo',
    'Apoyo en elaboración de documentos, organización de archivos, atención de llamadas y seguimiento de procesos administrativos.',
    'Bachiller o estudiante universitario. Manejo de Microsoft Office, organización y buena comunicación.',
    'San Salvador',
    550.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-08 08:00:00',
    'ACTIVA',
    3
),

(
    'Auxiliar Contable',
    'Apoyo al departamento de contabilidad en registro de operaciones, elaboración de reportes y control de documentación.',
    'Estudiante de Contaduría Pública o carreras relacionadas. Conocimientos de Excel y principios contables.',
    'San Salvador',
    625.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-08-25 14:10:00',
    'PAUSADA',
    3
),


-- ------------------------------------------------------------
-- Empresa 4: Digital Commerce SV
-- ------------------------------------------------------------

(
    'Especialista en Marketing Digital',
    'Planificación y ejecución de campañas digitales, administración de redes sociales y análisis de métricas de rendimiento.',
    'Experiencia en redes sociales, Meta Ads, creación de contenido y análisis de métricas digitales.',
    'Santa Tecla',
    800.00,
    'Tiempo completo',
    '9:00 AM - 6:00 PM',
    '2026-09-11 15:30:00',
    'ACTIVA',
    4
),

(
    'Gestor de Tienda en Línea',
    'Administración de catálogo de productos, actualización de inventario, procesamiento de pedidos y atención de consultas de clientes.',
    'Conocimientos de comercio electrónico, Excel, redes sociales y atención al cliente.',
    'San Salvador',
    650.00,
    'Medio tiempo',
    '1:00 PM - 6:00 PM',
    '2026-09-05 12:40:00',
    'ACTIVA',
    4
),


-- ------------------------------------------------------------
-- Empresa 5: Innova Systems
-- ------------------------------------------------------------

(
    'Técnico de Soporte Informático',
    'Brindar soporte técnico a usuarios, instalación de programas, mantenimiento preventivo y solución de incidencias de hardware y software.',
    'Conocimientos de Windows, redes, hardware, mantenimiento de computadoras y atención a usuarios.',
    'Soyapango',
    650.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-09 07:50:00',
    'ACTIVA',
    5
),

(
    'Administrador de Redes Junior',
    'Apoyo en configuración y monitoreo de redes, equipos de comunicación y servicios de infraestructura tecnológica.',
    'Conocimientos básicos de TCP/IP, configuración de routers y switches, Windows Server y Linux.',
    'San Salvador',
    900.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-08-20 10:25:00',
    'CERRADA',
    5
),


-- ------------------------------------------------------------
-- Empresa 6: Conecta Business
-- ------------------------------------------------------------

(
    'Ejecutivo de Ventas',
    'Prospección de clientes, seguimiento de oportunidades comerciales, elaboración de cotizaciones y cumplimiento de metas mensuales.',
    'Experiencia en ventas, facilidad de comunicación, orientación a resultados y manejo básico de herramientas informáticas.',
    'San Salvador',
    600.00,
    'Tiempo completo',
    '8:30 AM - 5:30 PM',
    '2026-09-13 09:35:00',
    'ACTIVA',
    6
),

(
    'Agente de Servicio al Cliente',
    'Atención de consultas de clientes por teléfono, correo electrónico y canales digitales.',
    'Excelente comunicación, orientación al cliente y disponibilidad para trabajar en equipo.',
    'San Salvador',
    525.00,
    'Temporal',
    '9:00 AM - 6:00 PM',
    '2026-08-18 16:00:00',
    'CERRADA',
    6
),

-- ------------------------------------------------------------
-- Empresa 7: TechCorp El Salvador
-- ------------------------------------------------------------

(
    'Desarrollador Full Stack',
    'Desarrollo de aplicaciones web y móviles, integración de APIs y colaboración con equipos multidisciplinarios.',
    'Experiencia en desarrollo con JavaScript, Node.js, React o Angular, conocimientos de bases de datos y control de versiones.',
    'San Salvador',
    1500.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-06 10:45:00',
    'ACTIVA',
    7
),

(
    'Ingeniero de Software',
    'Desarrollo de aplicaciones web y móviles, implementación de nuevas funcionalidades y optimización de código.',
    'Experiencia en desarrollo con Java, Python o C#, conocimientos de bases de datos y control de versiones.',
    'San Salvador',
    1200.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-07 11:15:00',
    'ACTIVA',
    7
),

(
    'Analista de Datos Junior',
    'Análisis e interpretación de datos, generación de reportes y apoyo en la toma de decisiones estratégicas.',
    'Conocimientos en Excel avanzado, SQL, Power BI o Tableau, y habilidades analíticas.',
    'Santa Tecla',
    850.00,
    'Tiempo completo',
    '9:00 AM - 6:00 PM',
    '2026-08-30 14:50:00',
    'PAUSADA',
    7
),

-- ------------------------------------------------------------
-- Empresa 8: Creative Studio
-- ------------------------------------------------------------
(
    'Diseñador UI/UX',
    'Creación de piezas gráficas para campañas publicitarias, branding y diseño de materiales digitales e impresos.',
    'Experiencia en diseño gráfico, manejo avanzado de Adobe Creative Suite y portafolio de trabajos.',
    'Santa Ana',
    1200.00,
    'Tiempo completo',
    '9:00 AM - 6:00 PM',
    '2026-09-02 13:10:00',
    'ACTIVA',
    8
),

(
    'Especialista en Marketing Digital',
    'Planificación y ejecución de estrategias de marketing digital, gestión de redes sociales y análisis de métricas.',
    'Experiencia en marketing digital, manejo de herramientas como Google Ads, Meta Ads y análisis de datos.',
    'San Salvador',
    900.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-04 10:30:00',
    'ACTIVA',
    8
),

(
    'Community Manager',
    'Gestión de redes sociales, creación de contenido, interacción con la comunidad y análisis de métricas de engagement.',
    'Experiencia en gestión de redes sociales, habilidades de comunicación y creatividad.',
    'San Salvador',
    750.00,
    'Tiempo completo',
    '9:00 AM - 6:00 PM',
    '2026-09-01 11:20:00',
    'ACTIVA',
    8
),

-- ------------------------------------------------------------
-- Empresa 9: Grupo Meridian
-- ------------------------------------------------------------

(
    'Gerente de Marketing ',
    'Responsable de liderar el equipo de marketing digital, desarrollar estrategias y supervisar campañas.',
    'Experiencia en marketing digital, liderazgo de equipos, análisis de métricas y conocimientos en SEO y SEM.',
    'San Salvador',
    2000.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-01 10:00:00',
    'ACTIVA',
    9
),

    (
    'Especialista en SEO',
    'Optimización de sitios web para motores de búsqueda, análisis de palabras clave y seguimiento de métricas de rendimiento.',
    'Experiencia en SEO, herramientas como Google Analytics y SEMrush, y conocimientos de HTML y CSS.',
    'San Salvador',
    800.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-03 09:45:00',
    'ACTIVA',
    9
),

(
    'Analista de Marketing Digital',
    'Análisis de campañas digitales, generación de reportes y apoyo en la toma de decisiones estratégicas.',
    'Conocimientos en marketing digital, herramientas de análisis y habilidades analíticas.',
    'Santa Tecla',
    850.00,
    'Tiempo completo',
    '9:00 AM - 6:00 PM',
    '2026-09-05 10:15:00',
    'ACTIVA',
    9
),

-- ------------------------------------------------------------
-- Empresa 10: DataVision SA
-- ------------------------------------------------

(
    'Analista de Datos',
    'Análisis e interpretación de datos, generación de reportes y apoyo en la toma de decisiones estratégicas.',
    'Conocimientos en Excel avanzado, SQL, Power BI o Tableau, y habilidades analíticas.',
    'La Libertad',
    1300.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-06 11:30:00',
    'ACTIVA',
    10
),

(
    'Científico de Datos Junior',
    'Desarrollo de modelos predictivos, análisis estadístico y visualización de datos para apoyar la toma de decisiones.',
    'Experiencia en Python o R, conocimientos en machine learning y análisis estadístico.',
    'Santa Tecla',
    1200.00,
    'Tiempo completo',
    '9:00 AM - 6:00 PM',
    '2026-09-07 14:20:00',
    'ACTIVA',
    10
),

(
    'Especialista en Inteligencia de Negocios',
    'Diseño y desarrollo de dashboards, análisis de datos y generación de reportes para la toma de decisiones estratégicas.',
    'Experiencia en Power BI, Tableau o herramientas similares, conocimientos en SQL y habilidades analíticas.',
    'San Salvador',
    1400.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-08 10:50:00',
    'ACTIVA',
    10
),


-- ------------------------------------------------------------
-- Empresa 11: Consultores Asociados
-- ------------------------------------------------------------

(
    'Consultor de Negocios',
    'Asesoramiento a empresas en estrategias de crecimiento, optimización de procesos y desarrollo organizacional.',
    'Experiencia en consultoría empresarial, habilidades analíticas y conocimientos en gestión de proyectos.',
    'San Salvador',
    1800.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-09 09:10:00',
    'ACTIVA',
    11
),

(
    'Analista de Procesos',
    'Análisis y mejora de procesos internos, identificación de oportunidades de optimización y documentación de procesos.',
    'Experiencia en análisis de procesos, habilidades analíticas y conocimientos en metodologías de mejora continua.',
    'Santa Tecla',
    1300.00,
    'Tiempo completo',
    '9:00 AM - 6:00 PM',
    '2026-09-10 11:40:00',
    'ACTIVA',
    11
),

(
    'Contador Fiscal',
    'Responsable de la contabilidad fiscal de la empresa, elaboración de declaraciones tributarias y cumplimiento de obligaciones fiscales.',
    'Experiencia en contaduría, conocimientos en legislación tributaria y manejo de software contable.',
    'San Salvador',
    1100.00,
    'Medio tiempo',
    '8:00 AM - 5:00 PM',
    '2026-09-11 10:20:00',
    'ACTIVA',
    11

),

-- ------------------------------------------------------------
-- Empresa 12: NetSolutions SV
-- ------------------------------------------------------------

(
    'Administrador de Sistemas',
    'Gestión y mantenimiento de servidores, redes y sistemas informáticos de la empresa.',
    'Experiencia en administración de sistemas, conocimientos en Windows Server, Linux y redes.',
    'San Salvador',
    1400.00,
    'Tiempo completo',
    '8:00 AM - 5:00 PM',
    '2026-09-12 09:30:00',
    'ACTIVA',
    12
),

(
    'Responsable de Soporte',
    'Brindar soporte técnico a usuarios, resolución de incidencias y mantenimiento de equipos de cómputo.',
    'Experiencia en soporte técnico, conocimientos en hardware, software y redes.',
    'San Miguel',
    900.00,
    'Tiempo completo',
    '9:00 AM - 6:00 PM',
    '2026-09-13 10:45:00',
    'ACTIVA',
    12
);
