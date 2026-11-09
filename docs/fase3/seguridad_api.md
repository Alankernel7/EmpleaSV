# Seguridad de la API REST — EmpleaSV Fase 3

**Objetivo:** definir cómo se protege `/api/*` reutilizando la autenticación y la autorización que ya existen, sin introducir un sistema paralelo.
**Estado:** diseño. No hay endpoints implementados todavía.

---

## 1. Autenticación existente que se reutiliza

| Elemento | Implementación actual | Uso en la API |
|---|---|---|
| Inicio de sesión | `AuthServlet.procesarLogin` | `POST /api/auth/login` delega en la misma verificación. |
| Hash de contraseña | `PasswordUtil` (PBKDF2WithHmacSHA256, 100.000 iteraciones, salt 16 bytes) | Se reutiliza sin cambios. |
| Sesión | `HttpSession` + cookie `JSESSIONID` | La API usa la misma sesión. |
| Datos de sesión | `SesionUtil` (`usuarioLogueado`, `rolUsuario`, `usuarioId`, `nombreUsuario`, `empresaIdSesion`) | La API lee el usuario y el rol desde aquí. |
| Protección web | `AuthFilter` | `ApiAuthFilter` replica la revalidación para `/api/*`. |
| Rol | Enum `UsuarioRol` | Autorización por rol. |

**No se crea** JWT, OAuth, API keys ni una tabla de tokens. La API es *session-based*, igual que la aplicación JSP.

---

## 2. `ApiAuthFilter`: qué hace y qué no hace

Archivo base creado: `src/main/java/com/empleasv/api/ApiAuthFilter.java` (`@WebFilter("/api/*")`).

**Hace:**

1. Lee el usuario de la sesión con `SesionUtil.usuario(request)`.
2. Revalida contra MySQL que la cuenta siga existiendo y en estado `ACTIVO` (igual que `AuthFilter`).
3. Si la cuenta fue desactivada, invalida la sesión (`SesionUtil.cerrarSesion`).
4. Si la ruta es privada y no hay sesión, responde **401 JSON** (`NO_AUTENTICADO`) en lugar de redirigir a login.
5. Refresca los datos de sesión con el usuario vigente.

**No hace:**

- No decide roles ni propiedad: eso depende del recurso y del método y se valida en cada servlet.
- No emite tokens.
- No gestiona CORS.

**Rutas públicas** (sin sesión):

| Método | Ruta | Motivo |
|---|---|---|
| POST | `/api/auth/login` | Iniciar sesión. |
| POST | `/api/auth/logout` | Cerrar sesión (idempotente). |
| POST | `/api/usuarios` | Registro público. |
| GET | `/api/ofertas`, `/api/ofertas/{id}` | Catálogo público (solo ofertas activas). |
| GET | `/api/empresas`, `/api/empresas/{id}`, `/api/empresas/{id}/ofertas` | Directorio público (solo ofertas activas). |

Todas las demás rutas exigen sesión.

---

## 3. Roles y matriz de permisos

Roles existentes: `USUARIO` (candidato), `EMPRESA`, `ADMINISTRADOR`.

| Operación | Público | USUARIO | EMPRESA | ADMINISTRADOR |
|---|---|---|---|---|
| `POST /api/auth/login` | Sí | Sí | Sí | Sí |
| `POST /api/auth/logout` | Sí | Sí | Sí | Sí |
| `GET /api/auth/sesion` | No | Sí | Sí | Sí |
| `POST /api/usuarios` (registro) | Sí | — | — | — |
| `GET /api/usuarios` | No | No | No | Sí |
| `GET /api/usuarios/{id}` | No | Solo su `id` | Solo su `id` | Cualquiera |
| `PATCH /api/usuarios/{id}/estado` | No | No | No | Sí |
| `GET /api/empresas` | Sí | Sí | Sí | Sí |
| `GET /api/empresas/{id}` | Sí | Sí | Sí | Sí |
| `GET /api/empresas/{id}/ofertas` | Solo `ACTIVA` | Solo `ACTIVA` | Propias: todos los estados | Todos |
| `GET /api/ofertas` | Solo `ACTIVA` | Solo `ACTIVA` | Propias con `mias=true` | Todos |
| `GET /api/ofertas/{id}` | Solo `ACTIVA` | Solo `ACTIVA` | Propias: cualquier estado | Todos |
| `POST /api/ofertas` | No | No | Sí (su empresa) | Sí |
| `PUT /api/ofertas/{id}` | No | No | Solo su empresa | Sí |
| `PATCH /api/ofertas/{id}/estado` | No | No | Solo su empresa | Sí |
| `DELETE /api/ofertas/{id}` | No | No | Solo su empresa | Sí |
| `GET /api/perfiles/candidato` | No | Sí (propio) | No | No |
| `GET /api/perfiles/candidato/{usuarioId}` | No | Solo su `id` | No | Sí |
| `PUT /api/perfiles/candidato` | No | Sí (propio) | No | No |
| `POST/DELETE /api/perfiles/candidato/foto` | No | Sí (propio) | No | No |
| `GET/POST/PATCH/DELETE .../curriculums` | No | Sí (propios) | No | No |
| `GET /api/perfiles/empresa` | No | No | Sí (propio) | No |
| `GET /api/perfiles/empresa/{usuarioId}` | No | No | Solo su `id` | Sí |
| `PUT /api/perfiles/empresa` | No | No | Sí (propio) | No |
| `POST/DELETE /api/perfiles/empresa/logo` | No | No | Sí (propio) | No |
| `GET /api/archivos/{id}` | No | Solo archivos propios | Solo archivos propios | Cualquiera |
| `GET /api/admin/dashboard` | No | No | No | Sí |
| `GET /api/admin/auditoria` | No | No | No | Sí |

**Reglas adicionales:**

- Un `USUARIO` no puede acceder a `/api/admin/*` ni a perfiles de empresa.
- Una `EMPRESA` no puede acceder a perfiles de candidato ni a `/api/admin/*`.
- Un `ADMINISTRADOR` puede leer perfiles y archivos, pero **no** puede editar el perfil de otra persona ni cambiar roles (no existe esa operación).
- El registro público **no** permite `rol = ADMINISTRADOR`.

---

## 4. Reglas de propiedad (anti-IDOR)

### 4.1 Identidad del solicitante

- El `usuarioId` se obtiene **siempre** de `SesionUtil.usuario(request).getId()`.
- El `empresaId` se obtiene **siempre** de `PerfilEmpresaDAO.obtenerEmpresaIdPorUsuarioId(usuarioId)`.
- Los IDs del cuerpo (`usuarioId`, `empresaId`) **no autorizan** nada.
- En `POST/PUT /api/ofertas`, si un ADMIN envía `empresaId`, se valida que exista. Si una EMPRESA lo envía, se **ignora** y se fuerza su propia empresa.

### 4.2 Ofertas

- `PUT`, `PATCH` y `DELETE` verifican que `oferta.empresa_id` coincida con la empresa resuelta de la sesión.
- Un ADMIN está exento de la comprobación de propiedad, pero la operación queda auditada cuando cambia estado.
- Si la oferta no existe o no es visible, se responde **404** (no 403) para no confirmar su existencia a un tercero.

### 4.3 Perfiles y archivos

- `PUT /api/perfiles/candidato` opera sobre el usuario de la sesión; no acepta `usuarioId` en el cuerpo.
- `GET /api/perfiles/candidato/{usuarioId}` permite el propio `id` o un ADMIN.
- `GET /api/archivos/{id}` exige `ArchivoRepository.esPropietario(id, usuarioId)` o rol ADMIN. Un currículum o foto de otro usuario responde **403**.
- `DELETE` de un currículum verifica que el archivo pertenezca al usuario y sea de categoría `CURRICULUM`.

### 4.4 Administración

- `PATCH /api/usuarios/{id}/estado` reutiliza `AdminService.cambiarEstadoUsuario`.
- El servicio impide desactivar la propia cuenta y el último administrador activo.
- No existe endpoint para cambiar el rol de una cuenta.

---

## 5. Validaciones obligatorias en el servidor

Ninguna validación del cliente es suficiente. El servidor debe validar:

| Grupo | Reglas |
|---|---|
| Identificadores | Numéricos, existentes y visibles para el solicitante. |
| Cuenta | `nombre` 1–150; `email` válido y ≤150; `password` ≥6; email único (409). |
| Candidato | `telefono` ≤30 y obligatorio al registrar; `direccion` ≤255 y obligatoria al registrar; `municipio`/`departamento` ≤100. |
| Empresa | `nombreEmpresa` ≤150; `nit` ≤50; `telefonoEmpresa` ≤30; `direccionEmpresa` ≤255; `municipio`/`departamento`/`sector` ≤100; `sitioWeb` ≤255; `descripcion` ≤5000. |
| Oferta | `titulo` ≤200; `descripcion` obligatoria; `ubicacion` ≤150; `tipoContrato` ≤50; `horario` ≤100; `salario` decimal ≥0; `estado` dentro de `ACTIVA|PAUSADA|CERRADA`. |
| Estado de oferta | Transiciones válidas (ver `diseno_api_rest.md`, O5). |
| Archivos | Extensión, MIME, magic bytes y tamaño (`ArchivosUtil`): imagen ≤2 MB; currículum ≤5 MB. |
| JSON | Cuerpo válido, objeto, ≤1 MB, sin campos desconocidos relevantes. |
| Método | Coherente con el recurso; si no, 405. |
| Contenido | JSON para cuerpos; `multipart/form-data` para archivos; si no, 415. |

---

## 6. Protección de información

### 6.1 Nunca se devuelve

- `usuario.password_hash`.
- `archivo.nombre_almacenado`.
- `archivo.ruta_relativa` ni rutas absolutas del servidor.
- `archivo.usuario_id` (el propietario no se expone en `ArchivoDto`).
- Datos de sesión internos (`JSESSIONID`, atributos de sesión).
- Trazas de excepción o SQL en la respuesta.

### 6.2 Visibilidad de datos

| Dato | Público | USUARIO propietario | EMPRESA propietaria | ADMIN |
|---|---|---|---|---|
| Nombre y email de cuenta | No | Sí | Sí | Sí |
| Teléfono/dirección del candidato | **No** | Sí | No | Sí (lectura) |
| Currículum/foto | No | Sí | No | Sí (descarga) |
| Datos públicos de empresa (`empresa`) | Sí | Sí | Sí | Sí |
| `perfil_empresa` (NIT, responsable, dirección) | No | No | Sí | Sí (lectura) |
| Logo | No | No | Sí | Sí |
| Auditoría | No | No | No | Sí |
| Ofertas `PAUSADA`/`CERRADA` | No | No | Sí (propias) | Sí |

**Nota:** el código JSP actual permite que `/archivo` sea accesible a cualquier sesión si es propietario o admin; no hay logos públicos. La API mantiene esa restricción. Si el producto requiere logos públicos, es un cambio de alcance que debe decidirse explícitamente.

### 6.3 Mensajes de error

- No incluir datos personales en el mensaje.
- No distinguir “no existe” de “no tienes permiso” cuando la distinción filtre información (ofertas no activas → 404).
- No devolver el hash ni la contraseña en ningún caso.

---

## 7. Riesgos actuales y recomendaciones

| Riesgo | Estado actual | Recomendación para Fase 3 |
|---|---|---|
| CSRF | Sin tokens; acciones por cookie de sesión | Añadir token CSRF o exigir cabecera personalizada en POST/PUT/PATCH/DELETE. |
| Session fixation | `AuthServlet` no cambia el ID de sesión | Llamar `request.changeSessionId()` en el login de la API. |
| Cookie insegura | `web.xml`: `secure=false` | Activar `secure` bajo HTTPS y `SameSite=Lax`. |
| Fuerza bruta | Sin límite de intentos | Añadir rate-limit por email/IP en el login. |
| Email case-sensitive | Login distingue mayúsculas | Normalizar a `LOWER(email)` en la consulta o guardar normalizado. |
| Logout por GET | En JSP actual | La API ya usa `POST /api/auth/logout`; no exponer logout por GET. |
| CORS | No configurado | Si el cliente es otro origen, definir orígenes permitidos; no usar `*` con credenciales. |
| Datos privados en logs | Riesgo si se registra el cuerpo | No registrar `password`, `passwordHash` ni archivos. |
| IDOR | Controlado por diseño | Validar propiedad en cada endpoint; no confiar en IDs del cliente. |
| Inyección SQL | JPA con parámetros | Mantener JPQL parametrizado; no concatenar SQL. |
| XSS | JSON plano | El cliente debe escapar; no devolver HTML. |

---

## 8. Decisiones de respuesta: 401, 403 y 404

| Situación | Código | Código de error |
|---|---|---|
| Sin sesión en ruta privada | 401 | `NO_AUTENTICADO` |
| Credenciales incorrectas | 401 | `CREDENCIALES_INVALIDAS` |
| Cuenta `INACTIVO` | 403 | `CUENTA_INACTIVA` |
| Rol insuficiente | 403 | `PROHIBIDO` |
| Recurso de otro usuario/empresa | 403 | `PROHIBIDO` |
| Oferta no activa consultada por público | 404 | `RECURSO_NO_ENCONTRADO` |
| Recurso inexistente | 404 | `RECURSO_NO_ENCONTRADO` |
| Desactivar la propia cuenta o el último admin | 409 | `REGLA_NEGOCIO` |
| Email ya registrado | 409 | `CONFLICTO` |
| Cuenta EMPRESA sin empresa vinculada | 409 | `PERFIL_EMPRESA_INCOMPLETO` |

---

## 9. Checklist de seguridad para el Integrante 2

- [ ] Todo servlet REST extiende `ApiServletBase` y captura `ApiExcepcion`.
- [ ] `ApiAuthFilter` está activo en `/api/*` y devuelve JSON, no redirecciones.
- [ ] El login usa `PasswordUtil` y `request.changeSessionId()`.
- [ ] El `usuarioId` sale de la sesión, nunca del cuerpo.
- [ ] El `empresaId` sale de `PerfilEmpresaDAO`, nunca del cuerpo (salvo ADMIN explícito).
- [ ] Los listados públicos filtran `estado = ACTIVA`.
- [ ] `PUT/PATCH/DELETE` de ofertas verifican propiedad.
- [ ] `GET /api/archivos/{id}` verifica propietario o ADMIN.
- [ ] Ninguna respuesta contiene `passwordHash`, `rutaRelativa` ni `nombreAlmacenado`.
- [ ] Los errores 500 no incluyen stack trace.
- [ ] Los archivos se validan con `ArchivosUtil` (extensión, MIME, magic bytes, tamaño).
- [ ] Los estados de oferta respetan las transiciones definidas.
- [ ] No se agregaron endpoints de postulaciones ni favoritos.

---

*Documento de diseño de seguridad. No se ejecutaron pruebas de penetración ni de integración.*
