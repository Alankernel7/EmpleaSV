# Diseño de la API REST — EmpleaSV Fase 3

**Proyecto:** `C:\Users\alane\OneDrive\Escritorio\EmpleaSV`
**Alcance:** diseño y contrato de la API REST. **No** implementa endpoints funcionales.
**Base tecnológica:** Java 21, Jakarta Servlet 6.0, JSP/JSTL, JPA/Hibernate 6.4, MySQL, Maven, Tomcat 10.1.
**Fecha:** 8 de noviembre de 2026.

> Estado real: los 33 endpoints de este documento están **diseñados**, no implementados. La implementación corresponde al Integrante 2. La API es aditiva: no reemplaza los servlets JSP del Sprint 2.

---

## 1. Punto de partida: qué existe realmente

### 1.1 Capas existentes

| Capa | Paquete / archivos | Estado |
|---|---|---|
| Configuración JPA | `config/JpaConfig`, `META-INF/persistence.xml` | Existente |
| Modelo | `model/Usuario`, `Empresa`, `OfertaEmpleo`, `PerfilCandidato`, `PerfilEmpresa`, `Archivo`, `Auditoria` | Existente |
| Repositorios | `repository/*Repository` (7) | Existente |
| DAO | `dao/*DAO` (6) | Existente (fachada sobre repositorios) |
| Servicios | `service/RegistroService`, `PerfilService`, `AdminService`, `ArchivoService` | Existente |
| Utilidades | `PasswordUtil`, `ValidacionUtil`, `SesionUtil`, `ArchivosUtil` | Existente |
| Seguridad web | `filter/AuthFilter` | Existente |
| Controladores JSP | `controller/*Servlet` (9) | Existente |
| Vistas | 32 JSP + `styles.css` | Existente |
| API REST | — | **No existía** |

### 1.2 Autenticación existente (no se reemplaza)

- Login: `POST /auth?accion=login` (formulario) → `AuthServlet.procesarLogin`.
- Contraseñas: `PasswordUtil` (PBKDF2WithHmacSHA256, 100.000 iteraciones, salt de 16 bytes).
- Sesión: `HttpSession` con `JSESSIONID`; claves centralizadas en `SesionUtil`.
- Roles: `USUARIO`, `EMPRESA`, `ADMINISTRADOR`.
- Filtro: `AuthFilter` protege rutas JSP y revalida que la cuenta siga `ACTIVO`.

La API **reutiliza esa sesión**. No se crea un token JWT ni un sistema paralelo de autenticación.

### 1.3 Módulos que NO existen y por tanto NO se exponen

| Módulo | Evidencia |
|---|---|
| Postulaciones | No hay entidad, tabla, repositorio, servicio, servlet ni estados. |
| Ofertas guardadas / favoritas | Solo un JSP estático informativo. |
| Recuperación de contraseña | No hay código. |
| Eliminación física de usuarios | El panel admin solo activa/desactiva; no borra. |
| Creación de empresa independiente | La empresa nace al registrar una cuenta `EMPRESA`. |
| Aprobación/moderación de empresas | No existe estado de aprobación. |
| Paginación | Ningún repositorio usa `setFirstResult`. |

**Decisión:** no se diseñan endpoints para postulaciones ni favoritos. El Integrante 2 no debe inventarlos. Si Fase 3 exige postulaciones, primero debe crearse el módulo completo en otra actividad.

---

## 2. Principios de diseño

1. **Aditivo, no destructivo.** `/api/*` convive con `/inicio`, `/ofertas`, `/perfil`, `/admin`, etc.
2. **Sesión existente.** El cliente inicia sesión con `POST /api/auth/login` (adaptador del login actual) y reutiliza la cookie `JSESSIONID`.
3. **La identidad no se negocia.** El servidor obtiene `usuarioId` de la sesión y `empresaId` de `perfil_empresa`. Nunca autoriza con IDs enviados por el cliente.
4. **Respuesta uniforme.** Éxito: `exito`, `mensaje`, `datos`. Error: `exito`, `error {codigo, mensaje, detalles}`.
5. **JSON en camelCase.** Fechas ISO-8601 (`2026-11-08T14:30:00`). Dinero como número decimal (`850.00`).
6. **Archivos por separado.** Los JSON no llevan archivos; las fotos, logos y currículums usan `multipart/form-data`.
7. **Errores honestos.** No se expone `password_hash`, `nombre_almacenado`, `ruta_relativa` ni datos privados del candidato.
8. **Sin dependencias nuevas.** Se agregó un lector JSON mínimo en `com.empleasv.api`; no se incorporó Spring, Jackson ni Gson.
9. **Rutas en plural y subrecursos.** `/api/ofertas/{id}`, `/api/perfiles/candidato/curriculums`.

---

## 3. Arquitectura de la API

```
Cliente (JSP, fetch, Postman, app futura)
        │  HTTP + cookie JSESSIONID
        ▼
ApiAuthFilter  (/api/*)  ── revalida sesión y cuenta ACTIVO
        ▼
Servlet REST (Integrante 2)  ── autorización por rol y propiedad
        ▼
Service existente (RegistroService, PerfilService, AdminService, ArchivoService)
        ▼
DAO / Repository existente
        ▼
JPA/Hibernate (empleasvPU)
        ▼
MySQL (7 tablas)
```

La API no accede directamente a `ConexionDB` (JDBC crudo, `@Deprecated`).

---

## 4. Recursos y modelo de datos

| Recurso | Tabla(s) | Entidad | Operaciones permitidas |
|---|---|---|---|
| Usuarios | `usuario` | `Usuario` | Registro, consulta, cambio de estado |
| Empresas | `empresa`, `perfil_empresa` | `Empresa` | Consulta pública; actualización vía perfil |
| Ofertas | `oferta_empleo` | `OfertaEmpleo` | CRUD con propiedad |
| Perfil de candidato | `perfil_candidato`, `archivo` | `PerfilCandidato` | Consulta/edición propia; archivos |
| Perfil de empresa | `perfil_empresa`, `archivo` | `PerfilEmpresa` | Consulta/edición propia; logo |
| Archivos | `archivo` | `Archivo` | Descarga controlada |
| Administración | varias | `AdminService` | Dashboard y auditoría |
| Postulaciones | — | — | **No existe: fuera de alcance** |

### Relaciones relevantes

```
usuario 1 ──── 1 perfil_candidato ──── 0..1 archivo (foto_perfil_id)
usuario 1 ──── 1 perfil_empresa ──── 0..1 archivo (logo_id)
perfil_empresa 1 ──── 1 empresa (empresa_id)
empresa 1 ──── * oferta_empleo (empresa_id)
usuario 1 ──── * archivo (usuario_id)
usuario (admin) 1 ──── * auditoria (admin_id)
```

---

## 5. Convenciones del contrato

### 5.1 URLs

- Prefijo: `/api`.
- Recursos en plural: `/api/usuarios`, `/api/empresas`, `/api/ofertas`.
- Subrecursos: `/api/empresas/{id}/ofertas`, `/api/perfiles/candidato/curriculums`.
- Sin versión en la URL en esta fase. Si se requiere, se acordará `/api/v1` antes de implementar.

### 5.2 Métodos

| Método | Uso |
|---|---|
| GET | Consultar; nunca modifica estado. |
| POST | Crear o ejecutar una acción (login, logout, subir archivo). |
| PUT | Reemplazar un recurso con todos sus campos editables. |
| PATCH | Cambiar un atributo puntual (`estado`) o marcar currículum principal. |
| DELETE | Eliminar oferta, foto, logo o currículum. |

### 5.3 Códigos HTTP

| Código | Significado |
|---|---|
| 200 | Consulta o actualización correcta. |
| 201 | Recurso creado. |
| 204 | Acción correcta sin cuerpo. |
| 400 | Datos inválidos, JSON inválido o ID de ruta inválido. |
| 401 | Sin sesión o credenciales inválidas. |
| 403 | Sesión válida pero rol/propiedad insuficiente, o cuenta inactiva. |
| 404 | Recurso inexistente o no visible para el solicitante. |
| 405 | Método HTTP no permitido. |
| 409 | Conflicto de negocio (email duplicado, cuenta propia, último admin). |
| 413 | Archivo o cuerpo demasiado grande. |
| 415 | Tipo de contenido no soportado. |
| 500 | Error interno no controlado. |

### 5.4 Formato uniforme

**Éxito:**

```json
{
  "exito": true,
  "mensaje": "OK",
  "datos": {}
}
```

**Error:**

```json
{
  "exito": false,
  "error": {
    "codigo": "DATOS_INVALIDOS",
    "mensaje": "Hay campos inválidos en la solicitud.",
    "detalles": [
      { "campo": "titulo", "mensaje": "El campo 'titulo' es obligatorio." }
    ]
  }
}
```

### 5.5 Códigos de error de la API

| Código | HTTP | Uso |
|---|---|---|
| `NO_AUTENTICADO` | 401 | No hay sesión. |
| `CREDENCIALES_INVALIDAS` | 401 | Email o contraseña incorrectos. |
| `CUENTA_INACTIVA` | 403 | La cuenta está `INACTIVO`. |
| `PROHIBIDO` | 403 | Rol o propiedad insuficiente. |
| `DATOS_INVALIDOS` | 400 | Validación de campos. |
| `JSON_INVALIDO` | 400 | Cuerpo mal formado. |
| `ID_INVALIDO` / `ID_REQUERIDO` | 400 | Path numérico inválido. |
| `RECURSO_NO_ENCONTRADO` | 404 | No existe o no es visible. |
| `CONFLICTO` | 409 | Email duplicado. |
| `REGLA_NEGOCIO` | 409 | Último admin, cuenta propia, transición inválida. |
| `PERFIL_EMPRESA_INCOMPLETO` | 409 | Cuenta EMPRESA sin empresa vinculada. |
| `METODO_NO_PERMITIDO` | 405 | Método no soportado. |
| `CUERPO_DEMASIADO_GRANDE` | 413 | Cuerpo o archivo excede el límite. |
| `TIPO_CONTENIDO_NO_SOPORTADO` | 415 | No es JSON o multipart según corresponda. |
| `ERROR_INTERNO` | 500 | Fallo no controlado. |

---

## 6. Endpoints

### 6.1 Autenticación (adaptador de la sesión existente)

La autenticación **no es un sistema nuevo**: `POST /api/auth/login` delega en la misma verificación de `AuthServlet` (`UsuarioDAO.obtenerPorEmail` + `PasswordUtil.verifyPassword`) y en `SesionUtil.iniciarSesion`.

| ID | Método | Ruta | Rol | Descripción |
|---|---|---|---|---|
| A1 | POST | `/api/auth/login` | Público | Inicia sesión y crea `JSESSIONID`. |
| A2 | POST | `/api/auth/logout` | Público (idempotente) | Invalida la sesión. |
| A3 | GET | `/api/auth/sesion` | Autenticado | Devuelve la sesión actual. |

#### A1. `POST /api/auth/login`

**Solicitud:**

```json
{
  "email": "candidato@ejemplo.com",
  "password": "Secreta123"
}
```

**Respuesta 200:**

```json
{
  "exito": true,
  "mensaje": "Sesión iniciada correctamente.",
  "datos": {
    "usuario": {
      "id": 15,
      "nombre": "Ana Pérez",
      "email": "candidato@ejemplo.com",
      "rol": "USUARIO",
      "estado": "ACTIVO",
      "fechaCreacion": "2026-10-01T09:15:00"
    },
    "empresaId": null
  }
}
```

Para `EMPRESA`, `empresaId` es el ID de la tabla `empresa` vinculada (puede ser `null` si el perfil está incompleto).

**Errores:** 400 (`DATOS_INVALIDOS`), 401 (`CREDENCIALES_INVALIDAS`), 403 (`CUENTA_INACTIVA`), 500.

**Reglas de implementación:**

- Validar email con `ValidacionUtil.esEmailValido`.
- Comparar con `PasswordUtil.verifyPassword`.
- Después de autenticar, llamar `request.changeSessionId()` antes de `SesionUtil.iniciarSesion` para mitigar session fixation.
- No devolver `passwordHash`.
- `Cache-Control: no-store`.

#### A2. `POST /api/auth/logout`

Sin cuerpo. Respuesta 204 siempre (si no había sesión, también 204).

#### A3. `GET /api/auth/sesion`

**Respuesta 200:** mismo objeto que A1.

**Errores:** 401 si no hay sesión.

---

### 6.2 Usuarios

| ID | Método | Ruta | Rol | Descripción |
|---|---|---|---|---|
| U1 | POST | `/api/usuarios` | Público | Registro de candidato o empresa. |
| U2 | GET | `/api/usuarios` | ADMINISTRADOR | Lista y filtra cuentas. |
| U3 | GET | `/api/usuarios/{id}` | ADMINISTRADOR o el propio usuario | Consulta una cuenta. |
| U4 | PATCH | `/api/usuarios/{id}/estado` | ADMINISTRADOR | Activa o desactiva una cuenta. |

> No se define `PUT /api/usuarios/{id}`: el nombre y el email se actualizan mediante `PUT /api/perfiles/candidato` o `PUT /api/perfiles/empresa`, que ya reutilizan `PerfilService`. Duplicarlo generaría dos caminos con validaciones distintas.
>
> No se define `DELETE /api/usuarios/{id}`: el proyecto no elimina cuentas, solo las desactiva.

#### U1. `POST /api/usuarios` (registro público)

**Cuerpo para candidato:**

```json
{
  "rol": "USUARIO",
  "nombre": "Ana Pérez",
  "email": "ana@ejemplo.com",
  "password": "Secreta123",
  "password2": "Secreta123",
  "telefono": "7777-1234",
  "direccion": "Col. Escalón, San Salvador",
  "municipio": "San Salvador",
  "departamento": "San Salvador"
}
```

**Cuerpo para empresa:**

```json
{
  "rol": "EMPRESA",
  "nombreResponsable": "Carlos López",
  "email": "rrhh@empresa.com",
  "password": "Secreta123",
  "password2": "Secreta123",
  "nombreEmpresa": "NovaTech Solutions",
  "nit": "0614-123456-101-0",
  "telefonoEmpresa": "2201-4580",
  "direccionEmpresa": "Calle La Reforma #123",
  "municipio": "San Salvador",
  "departamento": "San Salvador",
  "sector": "Tecnología",
  "sitioWeb": "https://novatechsv.com",
  "descripcion": "Desarrollo de software."
}
```

**Validaciones (replican `AuthServlet`):**

| Campo | Regla |
|---|---|
| `rol` | Obligatorio; solo `USUARIO` o `EMPRESA` (nunca `ADMINISTRADOR`). |
| `nombre` / `nombreResponsable` | Obligatorio, máximo 150. |
| `email` | Obligatorio, formato válido, máximo 150, único (409 si existe). |
| `password` | Obligatorio, mínimo 6. |
| `password2` | Opcional en API; si se envía, debe coincidir con `password`. |
| `telefono` (candidato) | Obligatorio, máximo 30. |
| `direccion` (candidato) | Obligatorio, máximo 255. |
| `nombreEmpresa` | Obligatorio, máximo 150. |
| `telefonoEmpresa` / `direccionEmpresa` | Obligatorios; 30 / 255. |
| `municipio` / `departamento` (empresa) | Obligatorios, máximo 100. |
| `sector` (empresa) | Obligatorio, máximo 100. |
| `nit` / `sitioWeb` | Opcionales; 50 / 255. |
| `descripcion` | Opcional; máximo 5000. |

**Respuesta 201:**

```json
{
  "exito": true,
  "mensaje": "Cuenta registrada correctamente.",
  "datos": {
    "usuario": {
      "id": 20,
      "nombre": "Ana Pérez",
      "email": "ana@ejemplo.com",
      "rol": "USUARIO",
      "estado": "ACTIVO",
      "fechaCreacion": "2026-11-08T14:30:00"
    },
    "empresaId": null
  }
}
```

Para `EMPRESA`, `empresaId` es el ID creado. Los archivos (foto, currículum, logo) **no** se envían en este JSON: se suben después de iniciar sesión mediante los endpoints de perfil.

**Errores:** 400, 409 (`CONFLICTO`), 415.

#### U2. `GET /api/usuarios`

**Parámetros opcionales:** `q` (texto sobre nombre/email), `rol` (`USUARIO|EMPRESA|ADMINISTRADOR`), `estado` (`ACTIVO|INACTIVO`).

**Respuesta 200:** arreglo de objetos `usuario` (mismos campos que U3). Sin paginación (limitación actual).

**Errores:** 400 si `rol` o `estado` no son válidos; 401; 403.

#### U3. `GET /api/usuarios/{id}`

**Respuesta 200:**

```json
{
  "exito": true,
  "mensaje": "OK",
  "datos": {
    "id": 15,
    "nombre": "Ana Pérez",
    "email": "ana@ejemplo.com",
    "rol": "USUARIO",
    "estado": "ACTIVO",
    "fechaCreacion": "2026-10-01T09:15:00"
  }
}
```

Un `USUARIO` solo puede consultar su propio `id`; un `EMPRESA` solo el suyo; un `ADMINISTRADOR` cualquiera. **No** incluye perfil, teléfono ni dirección; esos datos se consultan en el recurso de perfil y solo para el propio usuario o un administrador.

**Errores:** 401, 403, 404.

#### U4. `PATCH /api/usuarios/{id}/estado`

**Solicitud:**

```json
{ "estado": "INACTIVO" }
```

**Respuesta 200:**

```json
{
  "exito": true,
  "mensaje": "Usuario ana@ejemplo.com ahora está INACTIVO.",
  "datos": { "id": 15, "estado": "INACTIVO" }
}
```

**Errores:** 400 (`estado` inválido), 403, 404, 409 (`REGLA_NEGOCIO` si intenta desactivarse a sí mismo o al último administrador activo).

**Backing:** `AdminService.cambiarEstadoUsuario(adminId, objetivoId, estado)`.

---

### 6.3 Empresas

| ID | Método | Ruta | Rol | Descripción |
|---|---|---|---|---|
| E1 | GET | `/api/empresas` | Público | Lista empresas. |
| E2 | GET | `/api/empresas/{id}` | Público | Detalle de empresa. |
| E3 | GET | `/api/empresas/{id}/ofertas` | Público / EMPRESA propietaria / ADMIN | Ofertas de una empresa. |

> No se define `POST /api/empresas`: la empresa se crea al registrar una cuenta `EMPRESA` (U1).
> No se define `PUT /api/empresas/{id}`: los datos públicos se actualizan con `PUT /api/perfiles/empresa`, que ya sincroniza `empresa.nombre`, `categoria`, `telefono` y `descripcion`.
> No se define `DELETE /api/empresas/{id}`: la aplicación no elimina empresas.

#### E1. `GET /api/empresas`

**Parámetro opcional:** `destacadas=true` para usar `EmpresaRepository.listarDestacadas()`; sin el parámetro, `listarTodas()`.

**Respuesta 200:**

```json
{
  "exito": true,
  "mensaje": "OK",
  "datos": [
    {
      "id": 1,
      "nombre": "NovaTech Solutions",
      "descripcion": "Empresa salvadoreña dedicada al desarrollo de software.",
      "categoria": "Tecnología",
      "email": "contacto@novatechsv.com",
      "telefono": "2201-4580"
    }
  ]
}
```

#### E2. `GET /api/empresas/{id}`

**Respuesta 200:** un objeto empresa (mismo formato que E1). **404** si no existe.

#### E3. `GET /api/empresas/{id}/ofertas`

**Parámetro opcional:** `estado` (`ACTIVA|PAUSADA|CERRADA`).

**Regla de visibilidad:**

- Público y `USUARIO`: solo `ACTIVA`; si envían otro `estado`, se ignora.
- `EMPRESA` propietaria de esa empresa: puede ver todos sus estados.
- `ADMINISTRADOR`: puede ver todos.

**Respuesta 200:** arreglo de ofertas (formato de O1). **404** si la empresa no existe.

---

### 6.4 Ofertas de empleo

| ID | Método | Ruta | Rol | Descripción |
|---|---|---|---|---|
| O1 | GET | `/api/ofertas` | Público / autenticado según filtros | Lista ofertas. |
| O2 | GET | `/api/ofertas/{id}` | Público (solo ACTIVA) / propietario / ADMIN | Detalle. |
| O3 | POST | `/api/ofertas` | EMPRESA / ADMINISTRADOR | Crea oferta. |
| O4 | PUT | `/api/ofertas/{id}` | EMPRESA propietaria / ADMINISTRADOR | Actualiza oferta. |
| O5 | PATCH | `/api/ofertas/{id}/estado` | EMPRESA propietaria / ADMINISTRADOR | Cambia estado. |
| O6 | DELETE | `/api/ofertas/{id}` | EMPRESA propietaria / ADMINISTRADOR | Elimina oferta. |

#### O1. `GET /api/ofertas`

**Parámetros opcionales:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `titulo` | texto | Búsqueda LIKE por título (repositorio actual). |
| `ubicacion` | texto | Filtro por ubicación (igualdad exacta en el repositorio actual). |
| `empresaId` | entero | Ofertas de una empresa. |
| `estado` | texto | Solo ADMINISTRADOR (o propietario en E3); público siempre `ACTIVA`. |
| `mias` | booleano | `true` con rol EMPRESA: ofertas de la empresa de la sesión, todos los estados. |

**Regla obligatoria:** el listado público **nunca** debe devolver ofertas `PAUSADA` o `CERRADA`. El código JSP actual tiene esa fuga (`listarTodas` no filtra estado); la API **no debe replicarla**. Si no existe una consulta combinada en el repositorio, el Integrante 2 debe añadirla o filtrar en el servicio; no reutilizar `listarTodas()` sin filtrar.

**Respuesta 200:**

```json
{
  "exito": true,
  "mensaje": "OK",
  "datos": [
    {
      "id": 8,
      "titulo": "Desarrollador Java Junior",
      "descripcion": "Buscamos desarrollador junior...",
      "requisitos": "Estudiante o graduado de Ingeniería...",
      "ubicacion": "San Salvador",
      "salario": 850.00,
      "tipoContrato": "Tiempo completo",
      "horario": "8:00 AM - 5:00 PM",
      "fechaPublicacion": "2026-09-14T08:30:00",
      "estado": "ACTIVA",
      "empresaId": 1,
      "empresaNombre": "NovaTech Solutions"
    }
  ]
}
```

#### O2. `GET /api/ofertas/{id}`

Público solo si `estado = ACTIVA`. Propietario (empresa de la oferta) y ADMINISTRADOR pueden ver cualquier estado. Para el público, una oferta no activa responde **404** (no se revela su existencia).

**Errores:** 400 (ID inválido), 404.

#### O3. `POST /api/ofertas`

**Solicitud:**

```json
{
  "titulo": "Desarrollador Java Junior",
  "descripcion": "Buscamos desarrollador junior para aplicaciones web.",
  "requisitos": "Java, SQL, HTML, CSS y Git.",
  "ubicacion": "San Salvador",
  "salario": 850.00,
  "tipoContrato": "Tiempo completo",
  "horario": "8:00 AM - 5:00 PM",
  "estado": "ACTIVA"
}
```

**Campos:**

| Campo | Obligatorio | Regla |
|---|---|---|
| `titulo` | Sí | Máximo 200. |
| `descripcion` | Sí | Texto. |
| `requisitos` | No | Texto. |
| `ubicacion` | Sí | Máximo 150. |
| `salario` | No | Decimal ≥ 0, `DECIMAL(10,2)`. |
| `tipoContrato` | Sí | Máximo 50. |
| `horario` | No | Máximo 100. |
| `estado` | No | `ACTIVA` por defecto; valores permitidos `ACTIVA`, `PAUSADA`, `CERRADA`. |
| `empresaId` | Solo ADMIN | Para EMPRESA se ignora y se fuerza la empresa de la sesión. |

**Respuesta 201:** objeto oferta creado. `Location: /api/ofertas/{id}`.

**Errores:** 400, 401, 403 (`PROHIBIDO` si no es EMPRESA/ADMIN; `PERFIL_EMPRESA_INCOMPLETO` 409 si la empresa no tiene fila `empresa`), 500.

**Backing:** `OfertaEmpleoDAO.registrar` + resolución de empresa con `PerfilEmpresaDAO.obtenerEmpresaIdPorUsuarioId`. La fecha de publicación y el ID los fija el servidor.

#### O4. `PUT /api/ofertas/{id}`

Reemplazo completo de los campos editables. El servidor verifica propiedad antes de modificar.

**Solicitud:** mismos campos que O3 (sin `empresaId` para EMPRESA; ADMIN puede enviarlo).

**Reglas:**

- `titulo`, `descripcion`, `ubicacion`, `tipoContrato` obligatorios (la columna `tipo_contrato` es `NOT NULL`).
- Si `estado` no se envía, se conserva el actual.
- Si `fechaPublicacion` no se envía, se conserva la actual.
- `empresaId` no puede cambiar para una cuenta EMPRESA.

**Respuesta 200:** oferta actualizada. **Errores:** 400, 403, 404, 500.

**Backing:** `OfertaEmpleoDAO.actualizar`; propiedad con la misma lógica de `OfertaEmpleoServlet.puedeOperar`.

#### O5. `PATCH /api/ofertas/{id}/estado`

**Solicitud:**

```json
{ "estado": "PAUSADA" }
```

**Transiciones permitidas (validación nueva requerida):**

| Desde | Hacia | Quién |
|---|---|---|
| `ACTIVA` | `PAUSADA` | Propietaria / ADMIN |
| `PAUSADA` | `ACTIVA` | Propietaria / ADMIN |
| `ACTIVA` o `PAUSADA` | `CERRADA` | Propietaria / ADMIN |
| `CERRADA` | `ACTIVA` | Solo ADMIN |

No se permite `CERRADA → PAUSADA` (el panel admin actual lo permitiría por error; la API no debe reproducirlo).

**Respuesta 200:** `{ "id": 8, "estado": "PAUSADA" }`.

**Errores:** 400, 403, 404, 409 (`REGLA_NEGOCIO` por transición inválida).

**Backing:** ADMIN → `AdminService.cambiarEstadoOferta` (auditado); EMPRESA → `OfertaEmpleoDAO.actualizar` (sin auditoría, igual que el flujo JSP actual).

#### O6. `DELETE /api/ofertas/{id}`

**Respuesta 204.** **Errores:** 403, 404, 500. Solo propietaria o ADMIN.

**Backing:** `OfertaEmpleoDAO.eliminar`.

---

### 6.5 Perfil de candidato

| ID | Método | Ruta | Rol | Descripción |
|---|---|---|---|---|
| C1 | GET | `/api/perfiles/candidato` | USUARIO | Perfil propio. |
| C2 | GET | `/api/perfiles/candidato/{usuarioId}` | USUARIO propietario / ADMIN | Perfil por usuario. |
| C3 | PUT | `/api/perfiles/candidato` | USUARIO | Actualiza perfil propio. |
| C4 | POST | `/api/perfiles/candidato/foto` | USUARIO | Sube/reemplaza foto (multipart). |
| C5 | DELETE | `/api/perfiles/candidato/foto` | USUARIO | Elimina foto. |
| C6 | GET | `/api/perfiles/candidato/curriculums` | USUARIO | Lista currículums. |
| C7 | POST | `/api/perfiles/candidato/curriculums` | USUARIO | Agrega currículum (multipart). |
| C8 | PATCH | `/api/perfiles/candidato/curriculums/{archivoId}/principal` | USUARIO | Marca currículum principal. |
| C9 | DELETE | `/api/perfiles/candidato/curriculums/{archivoId}` | USUARIO | Elimina currículum propio. |

#### C1 / C2. Consulta

**Respuesta 200:**

```json
{
  "exito": true,
  "mensaje": "OK",
  "datos": {
    "id": 2,
    "usuario": {
      "id": 15,
      "nombre": "Ana Pérez",
      "email": "ana@ejemplo.com",
      "rol": "USUARIO",
      "estado": "ACTIVO",
      "fechaCreacion": "2026-10-01T09:15:00"
    },
    "telefono": "7777-1234",
    "direccionResidencia": "Col. Escalón, San Salvador",
    "municipio": "San Salvador",
    "departamento": "San Salvador",
    "foto": null,
    "curriculumPrincipal": {
      "id": 7,
      "nombreOriginal": "cv-ana.pdf",
      "tipoMime": "application/pdf",
      "extension": "pdf",
      "tamanoBytes": 245760,
      "categoria": "CURRICULUM",
      "fechaSubida": "2026-10-02T10:00:00",
      "urlDescarga": "/api/archivos/7"
    },
    "fechaActualizacion": "2026-10-05T16:20:00",
    "curriculums": [ ... ]
  }
}
```

**Privacidad:** teléfono y dirección solo se devuelven al propio candidato o a un ADMINISTRADOR. No se exponen en listados públicos ni en ofertas.

**Errores:** 401, 403, 404.

#### C3. `PUT /api/perfiles/candidato`

**Solicitud:**

```json
{
  "nombre": "Ana Pérez",
  "email": "ana@ejemplo.com",
  "telefono": "7777-1234",
  "direccion": "Col. Escalón, San Salvador",
  "municipio": "San Salvador",
  "departamento": "San Salvador"
}
```

**Respuesta 200:** perfil actualizado (mismo formato de C1). **Errores:** 400, 401, 403, 409 (email duplicado).

**Backing:** `PerfilService.actualizarCandidato`.

#### C4. `POST /api/perfiles/candidato/foto`

- `Content-Type: multipart/form-data`
- Campo: `foto`
- Formatos: `jpg`, `jpeg`, `png`, `webp`; máximo 2 MB (reglas de `ArchivosUtil`).
- **Respuesta 201:** `ArchivoDto`.
- **Errores:** 400 (tipo/tamaño), 413, 415.

**Backing:** `PerfilService.subirFoto` / `ArchivoService`.

#### C5. `DELETE /api/perfiles/candidato/foto`

**Respuesta 204.** **Errores:** 404 si no tiene foto.

#### C6. `GET /api/perfiles/candidato/curriculums`

**Respuesta 200:** arreglo de `ArchivoDto` (todos los currículums del usuario).

#### C7. `POST /api/perfiles/candidato/curriculums`

- `multipart/form-data`; campo `curriculum`; opcional `principal=true`.
- Formatos: `pdf`, `doc`, `docx`, `odt`, `txt`; máximo 5 MB.
- **Respuesta 201:** `ArchivoDto`.
- **Errores:** 400, 413, 415.

**Backing:** `PerfilService.agregarCurriculum` y, si `principal=true`, `fijarCurriculumPrincipal`.

#### C8. `PATCH /api/perfiles/candidato/curriculums/{archivoId}/principal`

Sin cuerpo. **Respuesta 200:** perfil actualizado o `{ "curriculumPrincipalId": 7 }`. **Errores:** 400, 403, 404.

**Backing:** `PerfilService.fijarCurriculumPrincipal(usuarioId, archivoId)`.

#### C9. `DELETE /api/perfiles/candidato/curriculums/{archivoId}`

**Respuesta 204.** **Errores:** 403 si el archivo no pertenece al usuario, 404.

**Backing:** `PerfilService.eliminarCurriculumArchivo(usuarioId, archivoId)`.

---

### 6.6 Perfil de empresa

| ID | Método | Ruta | Rol | Descripción |
|---|---|---|---|---|
| P1 | GET | `/api/perfiles/empresa` | EMPRESA | Perfil propio. |
| P2 | GET | `/api/perfiles/empresa/{usuarioId}` | EMPRESA propietaria / ADMIN | Perfil por usuario. |
| P3 | PUT | `/api/perfiles/empresa` | EMPRESA | Actualiza perfil y datos públicos. |
| P4 | POST | `/api/perfiles/empresa/logo` | EMPRESA | Sube/reemplaza logo (multipart). |
| P5 | DELETE | `/api/perfiles/empresa/logo` | EMPRESA | Elimina logo. |

#### P3. `PUT /api/perfiles/empresa`

**Solicitud:**

```json
{
  "nombreResponsable": "Carlos López",
  "email": "rrhh@empresa.com",
  "nombreEmpresa": "NovaTech Solutions",
  "nit": "0614-123456-101-0",
  "telefonoEmpresa": "2201-4580",
  "direccionEmpresa": "Calle La Reforma #123",
  "municipio": "San Salvador",
  "departamento": "San Salvador",
  "sector": "Tecnología",
  "sitioWeb": "https://novatechsv.com",
  "descripcion": "Desarrollo de software."
}
```

**Respuesta 200:** `PerfilEmpresaDto` (incluye `empresaId` y `logo`).

**Backing:** `PerfilService.actualizarEmpresa`, que también actualiza `usuario.nombre`, `usuario.email` y los campos públicos de `empresa`.

#### P4 / P5. Logo

- `POST` multipart, campo `logo`; `jpg`, `jpeg`, `png`, `webp`; máximo 2 MB; **201** `ArchivoDto`.
- `DELETE` **204**; 404 si no tiene logo.

---

### 6.7 Archivos

| ID | Método | Ruta | Rol | Descripción |
|---|---|---|---|---|
| F1 | GET | `/api/archivos/{id}` | Propietario del archivo / ADMIN | Descarga controlada. |

**Respuesta 200:** binario con `Content-Type` tomado de `archivo.tipo_mime`, `Content-Disposition: attachment; filename="..."`, `Content-Length`.

**Errores:** 401, 403 (no propietario), 404.

**Backing:** misma lógica de `ArchivoServlet` y `ArchivoRepository.esPropietario`. No se expone `ruta_relativa`.

---

### 6.8 Administración

| ID | Método | Ruta | Rol | Descripción |
|---|---|---|---|---|
| D1 | GET | `/api/admin/dashboard` | ADMINISTRADOR | Métricas y actividad reciente. |
| D2 | GET | `/api/admin/auditoria` | ADMINISTRADOR | Últimos registros de auditoría. |

#### D1. `GET /api/admin/dashboard`

**Respuesta 200:**

```json
{
  "exito": true,
  "mensaje": "OK",
  "datos": {
    "usuarios": {
      "total": 20,
      "candidatosActivos": 12,
      "empresasActivas": 4,
      "administradoresActivos": 1
    },
    "ofertas": {
      "ACTIVA": 14,
      "PAUSADA": 2,
      "CERRADA": 3
    },
    "usuariosRecientes": [ ... ],
    "ofertasRecientes": [ ... ],
    "auditoriaReciente": [ ... ]
  }
}
```

**Backing:** `AdminService.totalUsuarios`, `candidatosActivos`, `empresasActivas`, `administradoresActivos`, `contarOfertas`, `listarUsuariosRecientes`, `listarOfertasRecientes`, `listarAuditoriaReciente`.

#### D2. `GET /api/admin/auditoria`

**Parámetro opcional:** `limite` (entero, por defecto 20, máximo 100).

**Respuesta 200:**

```json
{
  "exito": true,
  "mensaje": "OK",
  "datos": [
    {
      "id": 31,
      "adminId": 1,
      "accion": "CAMBIAR_ESTADO_OFERTA",
      "objetivoTipo": "OFERTA",
      "objetivoId": 8,
      "detalle": "\"Desarrollador Java Junior\": ACTIVA -> PAUSADA",
      "fecha": "2026-11-08T12:10:00"
    }
  ]
}
```

---

## 7. Ejemplos de respuestas exigidos

### 7.1 Consulta exitosa

```json
{
  "exito": true,
  "mensaje": "OK",
  "datos": [
    { "id": 1, "nombre": "NovaTech Solutions", "categoria": "Tecnología" }
  ]
}
```

### 7.2 Registro exitoso (201)

```json
{
  "exito": true,
  "mensaje": "Cuenta registrada correctamente.",
  "datos": {
    "usuario": {
      "id": 20,
      "nombre": "Ana Pérez",
      "email": "ana@ejemplo.com",
      "rol": "USUARIO",
      "estado": "ACTIVO",
      "fechaCreacion": "2026-11-08T14:30:00"
    },
    "empresaId": null
  }
}
```

### 7.3 Actualización exitosa

```json
{
  "exito": true,
  "mensaje": "Oferta actualizada correctamente.",
  "datos": { "id": 8, "titulo": "Desarrollador Java Junior" }
}
```

### 7.4 Recurso no encontrado (404)

```json
{
  "exito": false,
  "error": {
    "codigo": "RECURSO_NO_ENCONTRADO",
    "mensaje": "No se encontró la oferta con id 999."
  }
}
```

### 7.5 Datos inválidos (400)

```json
{
  "exito": false,
  "error": {
    "codigo": "DATOS_INVALIDOS",
    "mensaje": "Hay campos inválidos en la solicitud.",
    "detalles": [
      { "campo": "titulo", "mensaje": "El campo 'titulo' es obligatorio." },
      { "campo": "salario", "mensaje": "El salario debe ser un número mayor o igual a 0." }
    ]
  }
}
```

### 7.6 Acceso no autorizado (401 / 403)

Sin sesión (401):

```json
{
  "exito": false,
  "error": {
    "codigo": "NO_AUTENTICADO",
    "mensaje": "Debes iniciar sesión para acceder a este recurso."
  }
}
```

Con sesión pero sin permiso (403):

```json
{
  "exito": false,
  "error": {
    "codigo": "PROHIBIDO",
    "mensaje": "Solo puedes gestionar las ofertas de tu propia empresa."
  }
}
```

### 7.7 Error interno (500)

```json
{
  "exito": false,
  "error": {
    "codigo": "ERROR_INTERNO",
    "mensaje": "Ocurrió un error interno. Intenta nuevamente más tarde."
  }
}
```

---

## 8. Trazabilidad endpoint → código existente

| Endpoint | Controlador base | Servicio / DAO |
|---|---|---|
| A1 | `AuthServlet.procesarLogin` | `UsuarioDAO.obtenerPorEmail`, `PasswordUtil`, `SesionUtil` |
| U1 | `AuthServlet.procesarRegistro` | `RegistroService.registrarCandidato/registrarEmpresa` |
| U2–U4 | `AdminServlet` | `AdminService.buscarUsuarios/cambiarEstadoUsuario` |
| E1–E3 | `EmpresaServlet` | `EmpresaDAO`, `OfertaEmpleoDAO.listarPorEmpresa` |
| O1–O6 | `OfertaEmpleoServlet` | `OfertaEmpleoDAO`, `PerfilEmpresaDAO` |
| C1–C9 | `PerfilServlet` | `PerfilService`, `ArchivoService` |
| P1–P5 | `PerfilEmpresaServlet` | `PerfilService`, `ArchivoService` |
| F1 | `ArchivoServlet` | `ArchivoRepository`, `ArchivoService.rutaDe` |
| D1–D2 | `AdminServlet` | `AdminService` |

---

## 9. Decisiones y límites explícitos

1. **No hay endpoints de postulaciones** porque el módulo no existe. No se simula.
2. **No hay endpoints de ofertas guardadas** por la misma razón.
3. **No se expone `DELETE /api/usuarios`** porque el negocio usa desactivación, no borrado.
4. **No se expone creación/edición directa de `empresa`**: se hace a través del registro y del perfil empresarial.
5. **No hay paginación**: los repositorios actuales no la soportan. El contrato no promete `page`/`size`. Añadirla es trabajo del Integrante 2 si el tiempo lo permite, sin romper el contrato.
6. **No hay CORS**: la API está pensada para el mismo origen (JSP/fetch). Si el Integrante 3 usa otro origen, se debe acordar CORS explícito.
7. **El login de la API es un adaptador**, no un sistema nuevo. La cookie `JSESSIONID` sigue siendo el mecanismo de sesión.
8. **La API no reemplaza las vistas JSP**; convive con ellas.

---

## 10. Qué debe hacer el Integrante 2

1. Crear los servlets REST en `com.empleasv.api.controller` (o subpaquetes) extendiendo `ApiServletBase`.
2. Implementar el adaptador de login/logout/sesión reutilizando `UsuarioDAO`, `PasswordUtil` y `SesionUtil`.
3. Implementar cada endpoint con las validaciones y códigos de este documento.
4. Resolver la empresa desde la sesión, nunca desde el body.
5. Filtrar ofertas `ACTIVA` en los listados públicos.
6. Implementar la descarga de archivos reutilizando `ArchivoRepository` y `ArchivoService`.
7. Añadir las pruebas manuales indicadas en `arquitectura_api.md`.
8. No inventar postulaciones ni favoritos hasta que exista el módulo.

---

## 11. Qué necesita el Integrante 3

- La cookie `JSESSIONID` después de `POST /api/auth/login`.
- El sobre uniforme `exito/mensaje/datos` y `exito/error`.
- Los 33 endpoints de `contrato_api.md`.
- Las reglas de visibilidad (ofertas no activas no se muestran al público).
- Que los archivos se suben con `multipart/form-data`, no dentro del JSON.
- Que las fechas son ISO-8601 y el dinero es numérico.
- Que no debe enviar `usuarioId` ni `empresaId` para autorizar; el servidor los resuelve.

---

*Documento de diseño. Los endpoints no están implementados ni probados en ejecución.*
