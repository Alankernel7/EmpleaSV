# Contrato de la API REST — EmpleaSV Fase 3

**Base URL local:** `http://localhost:8080/EmpleaSV/api`
**Autenticación:** cookie de sesión `JSESSIONID` creada por `POST /api/auth/login`.
**Formato:** JSON UTF-8, salvo descargas y subidas de archivos.
**Estado:** contrato diseñado. Endpoints **no implementados** todavía.

---

## 1. Sobre uniforme

### 1.1 Éxito

```json
{
  "exito": true,
  "mensaje": "OK",
  "datos": {}
}
```

- `datos` puede ser objeto, arreglo o `null`.
- En `204` no hay cuerpo.

### 1.2 Error

```json
{
  "exito": false,
  "error": {
    "codigo": "DATOS_INVALIDOS",
    "mensaje": "Hay campos inválidos en la solicitud.",
    "detalles": [
      { "campo": "email", "mensaje": "El formato del correo no es válido." }
    ]
  }
}
```

`detalles` es opcional y solo aparece en errores de validación.

### 1.3 Códigos de error

| Código | HTTP | Cuándo |
|---|---|---|
| `NO_AUTENTICADO` | 401 | Falta sesión. |
| `CREDENCIALES_INVALIDAS` | 401 | Email/contraseña incorrectos. |
| `CUENTA_INACTIVA` | 403 | Cuenta desactivada. |
| `PROHIBIDO` | 403 | Rol o propiedad insuficiente. |
| `DATOS_INVALIDOS` | 400 | Campo inválido o faltante. |
| `JSON_INVALIDO` | 400 | Cuerpo mal formado. |
| `ID_REQUERIDO` / `ID_INVALIDO` | 400 | ID de ruta ausente o no numérico. |
| `RECURSO_NO_ENCONTRADO` | 404 | No existe o no es visible. |
| `CONFLICTO` | 409 | Email duplicado. |
| `REGLA_NEGOCIO` | 409 | Último admin, cuenta propia, transición inválida. |
| `PERFIL_EMPRESA_INCOMPLETO` | 409 | Cuenta EMPRESA sin empresa vinculada. |
| `METODO_NO_PERMITIDO` | 405 | Método incorrecto. |
| `CUERPO_DEMASIADO_GRANDE` | 413 | Cuerpo o archivo excedido. |
| `TIPO_CONTENIDO_NO_SOPORTADO` | 415 | Content-Type incorrecto. |
| `ERROR_INTERNO` | 500 | Fallo no controlado. |

---

## 2. Tabla completa de endpoints (33)

Leyenda de acceso: **P** público, **U** USUARIO, **E** EMPRESA, **A** ADMINISTRADOR, **S** autenticado (cualquier rol), **P/E/A** según regla.

| ID | Método | Ruta | Acceso | Entrada principal | Salida | Códigos |
|---|---|---|---|---|---|---|
| A1 | POST | `/api/auth/login` | P | `email`, `password` | usuario + `empresaId` | 200, 400, 401, 403, 500 |
| A2 | POST | `/api/auth/logout` | P | — | sin cuerpo | 204 |
| A3 | GET | `/api/auth/sesion` | S | — | usuario + `empresaId` | 200, 401 |
| U1 | POST | `/api/usuarios` | P | `rol` + datos de candidato/empresa | usuario + `empresaId` | 201, 400, 409, 415 |
| U2 | GET | `/api/usuarios` | A | `q`, `rol`, `estado` | lista de usuarios | 200, 400, 401, 403 |
| U3 | GET | `/api/usuarios/{id}` | U/E propio o A | `id` | usuario | 200, 400, 401, 403, 404 |
| U4 | PATCH | `/api/usuarios/{id}/estado` | A | `estado` | id + estado | 200, 400, 401, 403, 404, 409 |
| E1 | GET | `/api/empresas` | P | `destacadas` | lista de empresas | 200 |
| E2 | GET | `/api/empresas/{id}` | P | `id` | empresa | 200, 400, 404 |
| E3 | GET | `/api/empresas/{id}/ofertas` | P / E propia / A | `id`, `estado` | lista de ofertas | 200, 400, 404 |
| O1 | GET | `/api/ofertas` | P / E / A | `titulo`, `ubicacion`, `empresaId`, `estado`, `mias` | lista de ofertas | 200, 400, 401, 403 |
| O2 | GET | `/api/ofertas/{id}` | P / E propia / A | `id` | oferta | 200, 400, 404 |
| O3 | POST | `/api/ofertas` | E / A | datos de oferta | oferta creada | 201, 400, 401, 403, 409, 500 |
| O4 | PUT | `/api/ofertas/{id}` | E propia / A | datos de oferta | oferta actualizada | 200, 400, 401, 403, 404, 500 |
| O5 | PATCH | `/api/ofertas/{id}/estado` | E propia / A | `estado` | id + estado | 200, 400, 401, 403, 404, 409 |
| O6 | DELETE | `/api/ofertas/{id}` | E propia / A | `id` | sin cuerpo | 204, 401, 403, 404, 500 |
| C1 | GET | `/api/perfiles/candidato` | U | — | perfil candidato | 200, 401, 403 |
| C2 | GET | `/api/perfiles/candidato/{usuarioId}` | U propio / A | `usuarioId` | perfil candidato | 200, 400, 401, 403, 404 |
| C3 | PUT | `/api/perfiles/candidato` | U | nombre, email, teléfono, dirección | perfil actualizado | 200, 400, 401, 403, 409 |
| C4 | POST | `/api/perfiles/candidato/foto` | U | multipart `foto` | archivo | 201, 400, 401, 403, 413, 415 |
| C5 | DELETE | `/api/perfiles/candidato/foto` | U | — | sin cuerpo | 204, 401, 403, 404 |
| C6 | GET | `/api/perfiles/candidato/curriculums` | U | — | lista de archivos | 200, 401, 403 |
| C7 | POST | `/api/perfiles/candidato/curriculums` | U | multipart `curriculum`, `principal` | archivo | 201, 400, 401, 403, 413, 415 |
| C8 | PATCH | `/api/perfiles/candidato/curriculums/{archivoId}/principal` | U | `archivoId` | principal actualizado | 200, 400, 401, 403, 404 |
| C9 | DELETE | `/api/perfiles/candidato/curriculums/{archivoId}` | U | `archivoId` | sin cuerpo | 204, 400, 401, 403, 404 |
| P1 | GET | `/api/perfiles/empresa` | E | — | perfil empresa | 200, 401, 403 |
| P2 | GET | `/api/perfiles/empresa/{usuarioId}` | E propio / A | `usuarioId` | perfil empresa | 200, 400, 401, 403, 404 |
| P3 | PUT | `/api/perfiles/empresa` | E | datos de empresa | perfil actualizado | 200, 400, 401, 403, 409 |
| P4 | POST | `/api/perfiles/empresa/logo` | E | multipart `logo` | archivo | 201, 400, 401, 403, 413, 415 |
| P5 | DELETE | `/api/perfiles/empresa/logo` | E | — | sin cuerpo | 204, 401, 403, 404 |
| F1 | GET | `/api/archivos/{id}` | Propietario / A | `id` | binario | 200, 400, 401, 403, 404 |
| D1 | GET | `/api/admin/dashboard` | A | — | métricas | 200, 401, 403 |
| D2 | GET | `/api/admin/auditoria` | A | `limite` | lista de auditoría | 200, 400, 401, 403 |

**Total: 33 endpoints.**

---

## 3. Diccionarios de campos

### 3.1 Usuario (respuesta)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | entero | PK `usuario.id`. |
| `nombre` | texto | ≤150. |
| `email` | texto | ≤150. |
| `rol` | texto | `USUARIO`, `EMPRESA`, `ADMINISTRADOR`. |
| `estado` | texto | `ACTIVO`, `INACTIVO`. |
| `fechaCreacion` | fecha ISO | `LocalDateTime`. |

Nunca incluye `passwordHash`.

### 3.2 Empresa (respuesta)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | entero | PK `empresa.id`. |
| `nombre` | texto | ≤150. |
| `descripcion` | texto | Puede ser null. |
| `categoria` | texto | ≤100. |
| `email` | texto | Contacto público. |
| `telefono` | texto | Puede ser null. |
| `cantidadOfertas` | entero | Solo si el servicio lo calculó (`@Transient`). |

### 3.3 Oferta

| Campo | Tipo | Notas |
|---|---|---|
| `id` | entero | PK `oferta_empleo.id`. |
| `titulo` | texto | ≤200. |
| `descripcion` | texto | Obligatoria. |
| `requisitos` | texto | Puede ser null. |
| `ubicacion` | texto | ≤150. |
| `salario` | decimal o null | `DECIMAL(10,2)`, ≥0. |
| `tipoContrato` | texto | ≤50. |
| `horario` | texto | ≤100. |
| `fechaPublicacion` | fecha ISO | La fija el servidor. |
| `estado` | texto | `ACTIVA`, `PAUSADA`, `CERRADA`. |
| `empresaId` | entero | FK `empresa.id`. |
| `empresaNombre` | texto | Nombre de la empresa. |

### 3.4 Perfil de candidato

| Campo | Tipo | Notas |
|---|---|---|
| `id` | entero | PK `perfil_candidato.id`. |
| `usuario` | objeto | Usuario seguro. |
| `telefono` | texto | Privado. |
| `direccionResidencia` | texto | Privado. |
| `municipio` | texto | Privado. |
| `departamento` | texto | Privado. |
| `foto` | objeto o null | `ArchivoDto`. |
| `curriculumPrincipal` | objeto o null | `ArchivoDto`. |
| `curriculums` | arreglo | Todos los CV del usuario. |
| `fechaActualizacion` | fecha ISO | — |

### 3.5 Perfil de empresa

| Campo | Tipo | Notas |
|---|---|---|
| `id` | entero | PK `perfil_empresa.id`. |
| `usuario` | objeto | Usuario seguro. |
| `nombreResponsable` | texto | ≤150. |
| `nombreEmpresa` | texto | ≤150. |
| `nit` | texto | ≤50. |
| `telefonoEmpresa` | texto | ≤30. |
| `direccionEmpresa` | texto | ≤255. |
| `municipio` | texto | ≤100. |
| `departamento` | texto | ≤100. |
| `sector` | texto | ≤100. |
| `sitioWeb` | texto | ≤255. |
| `descripcion` | texto | ≤5000. |
| `logo` | objeto o null | `ArchivoDto`. |
| `empresaId` | entero o null | FK `empresa.id`. |
| `fechaActualizacion` | fecha ISO | — |

### 3.6 Archivo (metadatos)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | entero | PK `archivo.id`. |
| `nombreOriginal` | texto | Nombre subido. |
| `tipoMime` | texto | — |
| `extension` | texto | — |
| `tamanoBytes` | entero largo | — |
| `categoria` | texto | `FOTO_PERFIL`, `CURRICULUM`, `LOGO_EMPRESA`. |
| `fechaSubida` | fecha ISO | — |
| `urlDescarga` | texto | `/api/archivos/{id}`. |

No incluye `nombreAlmacenado`, `rutaRelativa` ni `usuarioId`.

---

## 4. Ejemplos de solicitud y respuesta

### 4.1 Login

**POST** `/api/auth/login`

```json
{
  "email": "ana@ejemplo.com",
  "password": "Secreta123"
}
```

**200**

```json
{
  "exito": true,
  "mensaje": "Sesión iniciada correctamente.",
  "datos": {
    "usuario": {
      "id": 15,
      "nombre": "Ana Pérez",
      "email": "ana@ejemplo.com",
      "rol": "USUARIO",
      "estado": "ACTIVO",
      "fechaCreacion": "2026-10-01T09:15:00"
    },
    "empresaId": null
  }
}
```

### 4.2 Registro

**POST** `/api/usuarios`

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

**201**

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

### 4.3 Consulta de ofertas

**GET** `/api/ofertas?titulo=java&ubicacion=San%20Salvador`

**200**

```json
{
  "exito": true,
  "mensaje": "OK",
  "datos": [
    {
      "id": 8,
      "titulo": "Desarrollador Java Junior",
      "descripcion": "Buscamos desarrollador junior...",
      "requisitos": "Java, SQL, HTML, CSS y Git.",
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

### 4.4 Crear oferta

**POST** `/api/ofertas` (cookie de una cuenta EMPRESA)

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

**201** + `Location: /api/ofertas/34`

```json
{
  "exito": true,
  "mensaje": "Oferta registrada correctamente.",
  "datos": {
    "id": 34,
    "titulo": "Desarrollador Java Junior",
    "descripcion": "Buscamos desarrollador junior para aplicaciones web.",
    "requisitos": "Java, SQL, HTML, CSS y Git.",
    "ubicacion": "San Salvador",
    "salario": 850.00,
    "tipoContrato": "Tiempo completo",
    "horario": "8:00 AM - 5:00 PM",
    "fechaPublicacion": "2026-11-08T14:35:00",
    "estado": "ACTIVA",
    "empresaId": 4,
    "empresaNombre": "Mi Empresa"
  }
}
```

### 4.5 Actualizar perfil de candidato

**PUT** `/api/perfiles/candidato`

```json
{
  "nombre": "Ana Pérez",
  "email": "ana@ejemplo.com",
  "telefono": "7777-5678",
  "direccion": "Col. Escalón, San Salvador",
  "municipio": "San Salvador",
  "departamento": "San Salvador"
}
```

**200**

```json
{
  "exito": true,
  "mensaje": "Perfil actualizado correctamente.",
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
    "telefono": "7777-5678",
    "direccionResidencia": "Col. Escalón, San Salvador",
    "municipio": "San Salvador",
    "departamento": "San Salvador",
    "foto": null,
    "curriculumPrincipal": null,
    "fechaActualizacion": "2026-11-08T14:40:00",
    "curriculums": []
  }
}
```

### 4.6 Subir currículum

**POST** `/api/perfiles/candidato/curriculums`
`Content-Type: multipart/form-data`

| Parte | Valor |
|---|---|
| `curriculum` | archivo `cv-ana.pdf` |
| `principal` | `true` |

**201**

```json
{
  "exito": true,
  "mensaje": "Currículum agregado correctamente.",
  "datos": {
    "id": 7,
    "nombreOriginal": "cv-ana.pdf",
    "tipoMime": "application/pdf",
    "extension": "pdf",
    "tamanoBytes": 245760,
    "categoria": "CURRICULUM",
    "fechaSubida": "2026-11-08T14:45:00",
    "urlDescarga": "/api/archivos/7"
  }
}
```

### 4.7 Recurso no encontrado

**404**

```json
{
  "exito": false,
  "error": {
    "codigo": "RECURSO_NO_ENCONTRADO",
    "mensaje": "No se encontró la oferta con id 999."
  }
}
```

### 4.8 Datos inválidos

**400**

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

### 4.9 Sin autorización

**401**

```json
{
  "exito": false,
  "error": {
    "codigo": "NO_AUTENTICADO",
    "mensaje": "Debes iniciar sesión para acceder a este recurso."
  }
}
```

**403**

```json
{
  "exito": false,
  "error": {
    "codigo": "PROHIBIDO",
    "mensaje": "Solo puedes gestionar las ofertas de tu propia empresa."
  }
}
```

### 4.10 Error interno

**500**

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

## 5. Reglas de consumo para el Integrante 3

1. **Autenticarse primero.**
   `POST /api/auth/login` con `email` y `password`. Guardar la cookie `JSESSIONID` y enviarla en todas las solicitudes privadas.

2. **Content-Type.**
   - JSON: `Content-Type: application/json` y `Accept: application/json`.
   - Archivos: `multipart/form-data` (no meter archivos en JSON).

3. **No enviar IDs de autorización.**
   No enviar `usuarioId` ni `empresaId` para “decidir” quién es el usuario. El servidor usa la sesión. En ofertas, una EMPRESA no debe enviar `empresaId`; se ignora.

4. **Fechas y dinero.**
   - Fechas: ISO-8601 (`2026-11-08T14:30:00`).
   - Salario: número (`850.00`), no cadena con formato.

5. **Listados.**
   - El catálogo público solo devuelve ofertas `ACTIVA`.
   - Una oferta `PAUSADA`/`CERRADA` responde 404 al público.
   - `mias=true` exige sesión de EMPRESA; sin sesión responde 401, no se ignora.
   - No hay paginación; no enviar `page`/`size` esperando efecto.

6. **Errores.**
   - Revisar `exito` antes de leer `datos`.
   - Si `exito=false`, leer `error.codigo`, `error.mensaje` y `error.detalles`.
   - Tratar 401 → volver al login; 403 → mostrar “sin permiso”; 404 → “no encontrado”; 409 → conflicto de negocio.

7. **Archivos.**
   - Subir con `multipart/form-data`.
   - Descargar con `GET /api/archivos/{id}` (cookie de propietario o admin).
   - No construir rutas del servidor.

8. **Ofertas.**
   - EMPRESA solo puede crear/editar/eliminar ofertas de su propia empresa.
   - ADMIN puede operar cualquiera.
   - `PATCH /api/ofertas/{id}/estado` respeta las transiciones válidas.

9. **Privacidad.**
   - No mostrar teléfono, dirección ni currículum de un candidato a terceros.
   - No mostrar `passwordHash` (la API nunca lo devuelve).
   - No mostrar NIT ni responsable de una empresa en el directorio público.

10. **CORS.**
    Si el cliente se sirve desde otro origen, el Integrante 2/3 debe acordar CORS explícito (origen permitido y credenciales). No asumir `*`.

11. **Sesión.**
    - Logout con `POST /api/auth/logout`.
    - Tras logout, cualquier endpoint privado devuelve 401.
    - Una cuenta desactivada pierde acceso en la siguiente petición.

12. **Fuera de alcance.**
    No consumir endpoints de postulaciones ni de ofertas guardadas: **no existen** en este contrato.

---

## 6. Correspondencia con el proyecto actual

| Recurso API | Servicio / repositorio que la implementación debe reutilizar |
|---|---|
| Auth | `UsuarioDAO.obtenerPorEmail`, `PasswordUtil`, `SesionUtil` |
| Registro | `RegistroService.registrarCandidato/registrarEmpresa` |
| Usuarios admin | `AdminService.buscarUsuarios/cambiarEstadoUsuario` |
| Empresas | `EmpresaDAO.listarTodas/listarDestacadas/obtenerPorId` |
| Ofertas | `OfertaEmpleoDAO` + `PerfilEmpresaDAO.obtenerEmpresaIdPorUsuarioId` |
| Perfil candidato | `PerfilService.actualizarCandidato/obtenerOCrearCandidato` + `ArchivoService` |
| Perfil empresa | `PerfilService.actualizarEmpresa/obtenerOCrearEmpresa` + `ArchivoService` |
| Archivos | `ArchivoRepository`, `ArchivoService.rutaDe` |
| Admin | `AdminService` |

---

*Contrato de diseño. No afirma que los endpoints funcionen: no han sido implementados ni probados.*
