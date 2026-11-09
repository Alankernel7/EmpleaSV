# Arquitectura de la API REST — EmpleaSV Fase 3

**Objetivo:** explicar cómo se comunicará el cliente con la API y cómo se conecta con la arquitectura MVC existente, sin reemplazar Jakarta Servlet, JSP, JPA/Hibernate, MySQL, Maven ni Tomcat 10.1.
**Estado:** diseño. Los endpoints no están implementados.

---

## 1. Vista general

```
┌──────────────────────────────────────────────────────────────┐
│                         CLIENTE                              │
│  JSP existentes · fetch() · Postman · futuro cliente móvil   │
│  Envía cookie JSESSIONID y recibe JSON                       │
└───────────────────────────┬──────────────────────────────────┘
                            │ HTTP
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                     CAPA DE SEGURIDAD                        │
│  ApiAuthFilter (/api/*)                                      │
│   - revalida sesión y cuenta ACTIVO                          │
│   - responde 401 JSON si falta sesión                        │
│  (La autorización fina se hace en cada servlet)              │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                    CONTROLADORES REST                        │
│  com.empleasv.api.controller.*                               │
│  Servlet REST → ApiServletBase                               │
│   - lee JSON / multipart                                     │
│   - valida entrada                                           │
│   - resuelve usuario y empresa desde la sesión               │
│   - autoriza rol y propiedad                                 │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                       SERVICIOS                              │
│  RegistroService · PerfilService · AdminService ·            │
│  ArchivoService                                              │
│  (reglas de negocio existentes; no se duplican)              │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                 DAO / REPOSITORIOS                           │
│  UsuarioDAO · EmpresaDAO · OfertaEmpleoDAO ·                 │
│  PerfilCandidatoDAO · PerfilEmpresaDAO · ArchivoDAO          │
│  Repository JPA (7)                                          │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                  JPA / HIBERNATE                             │
│  persistence.xml · unidad empleasvPU · HikariCP              │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                         MYSQL                                │
│  base empleasv · 7 tablas · PK id · FK usuario_id/empresa_id │
└──────────────────────────────────────────────────────────────┘
```

La API **no** usa `ConexionDB` (JDBC crudo, `@Deprecated`). Toda la persistencia pasa por las entidades y repositorios JPA existentes.

---

## 2. Estructura de paquetes

### 2.1 Estructura actual (Sprint 2, sin cambios)

```
src/main/java/com/empleasv/
├── config/        JpaConfig
├── controller/    Servlets JSP existentes
├── dao/           Fachadas DAO
├── data/          ConexionDB (deprecado)
├── filter/        AuthFilter
├── model/         Entidades JPA
├── repository/    Repositorios JPA
├── service/       Servicios de negocio
└── util/          PasswordUtil, SesionUtil, ArchivosUtil, etc.
```

### 2.2 Estructura base agregada para la API

```
src/main/java/com/empleasv/api/
├── ApiConstantes.java      constantes (JSON, límites)
├── ApiJson.java            serializador JSON mínimo
├── ApiJsonReader.java      lector JSON mínimo + helpers de campos
├── ApiError.java           estructura de error
├── ApiRespuesta.java       sobre uniforme de respuesta
├── ApiExcepcion.java       excepción con estado HTTP y código
├── ApiServletBase.java     base para servlets REST (incluye PATCH)
├── ApiSesion.java          identidad y autorización por rol/propiedad
├── ApiAuthFilter.java      autenticación de /api/*
└── dto/
    ├── UsuarioDto.java
    ├── EmpresaDto.java
    ├── OfertaDto.java
    ├── ArchivoDto.java
    ├── PerfilCandidatoDto.java
    ├── PerfilEmpresaDto.java
    └── AuditoriaDto.java
```

### 2.3 Paquete reservado para el Integrante 2

```
src/main/java/com/empleasv/api/controller/
├── AuthApiServlet.java
├── UsuarioApiServlet.java
├── EmpresaApiServlet.java
├── OfertaApiServlet.java
├── PerfilCandidatoApiServlet.java
├── PerfilEmpresaApiServlet.java
├── ArchivoApiServlet.java
└── AdminApiServlet.java
```

Cada servlet debe declarar su `@WebServlet` con la ruta correspondiente. Los servlets existentes de JSP **no se modifican**.

**Mapeo recomendado:** usar patrones con comodín (`@WebServlet("/api/ofertas/*")`, `@WebServlet("/api/perfiles/candidato/*")`) para que `getPathInfo()` contenga los segmentos variables. `ApiServletBase.segmentos()` lee `getPathInfo()`; con un mapeo exacto ese valor es `null` y los IDs de ruta no se pueden extraer. `ApiAuthFilter` no depende de `getServletPath()`: calcula la ruta desde `getRequestURI()` menos el contexto.

---

## 3. Flujo de una petición

### 3.1 Petición autenticada típica

```
1. Cliente envía GET /api/ofertas/mias con cookie JSESSIONID.
2. ApiAuthFilter:
   a. lee usuario de la sesión;
   b. consulta usuario en BD;
   c. si no existe o está INACTIVO → cierra sesión y responde 401 JSON;
   d. si está activo, refresca la sesión y deja continuar.
3. OfertaApiServlet:
   a. valida el método y el path;
   b. obtiene usuario de ApiSesion;
   c. exige rol EMPRESA;
   d. resuelve empresaId con PerfilEmpresaDAO;
   e. llama a OfertaEmpleoDAO.listarPorEmpresa(empresaId);
   f. convierte entidades a Map con OfertaDto;
   g. responde ApiRespuesta.ok(lista).
4. Cliente recibe 200 con el sobre uniforme.
```

### 3.2 Petición pública

```
1. Cliente envía GET /api/ofertas sin cookie.
2. ApiAuthFilter identifica ruta pública (GET /api/ofertas*).
3. No exige sesión.
4. OfertaApiServlet fuerza estado ACTIVA y responde el catálogo.
```

### 3.3 Registro

```
1. POST /api/usuarios con JSON.
2. ApiAuthFilter deja pasar (ruta pública).
3. UsuarioApiServlet lee y valida el JSON.
4. Llama a RegistroService.registrarCandidato/registrarEmpresa
   (con Part = null, porque los archivos son opcionales).
5. El servicio crea Usuario + perfil (+ Empresa si es EMPRESA) en una transacción.
6. El servlet responde 201 con UsuarioDto y empresaId.
7. El cliente inicia sesión con POST /api/auth/login.
8. Luego sube foto/currículum/logo con multipart.
```

### 3.4 Login

```
1. POST /api/auth/login con JSON.
2. AuthApiServlet valida email/password.
3. UsuarioDAO.obtenerPorEmail + PasswordUtil.verifyPassword.
4. Verifica estado ACTIVO.
5. request.changeSessionId().
6. SesionUtil.iniciarSesion(request, usuario, empresaId).
7. Responde 200 con UsuarioDto (sin hash) y empresaId.
```

### 3.5 Subida de archivo

```
1. Cliente envía multipart/form-data con el campo foto|curriculum|logo.
2. Servlet correspondiente valida sesión y rol.
3. Llama a PerfilService.subirFoto/subirCurriculo/subirLogo.
4. ArchivoService:
   - valida extensión, MIME, magic bytes y tamaño (ArchivosUtil);
   - genera nombre aleatorio;
   - escribe en el directorio configurado (C:/empleasv-archivos);
   - registra metadatos en la tabla archivo.
5. Responde 201 con ArchivoDto (sin ruta interna).
```

### 3.6 Descarga de archivo

```
1. GET /api/archivos/{id} con cookie.
2. ArchivoApiServlet verifica propietario o ADMIN.
3. ArchivoService.rutaDe(archivo) resuelve la ruta física.
4. Responde binario con Content-Type y Content-Disposition.
```

### 3.7 Manejo de errores

```
Servlet REST
   │
   ├── ApiExcepcion (400/401/403/404/409/413/415)
   │      └── ApiRespuesta.error(...).escribir(response)
   │
   └── Throwable no controlado
          ├── getServletContext().log(...)
          └── 500 ERROR_INTERNO (sin stack trace al cliente)
```

Los servlets deben envolver su lógica en `try/catch` y llamar `manejar(error, response)`. `ApiServletBase` ya ofrece ese método.

---

## 4. Seguridad y sesión en la arquitectura

- `ApiAuthFilter` es la **primera barrera**: autenticación y cuenta activa.
- El `AuthFilter` existente **no** intercepta `/api/*` (su mapeo es solo de rutas JSP); `ApiAuthFilter` se registra con `@WebFilter("/api/*")`.
- `ApiSesion` es la **segunda barrera**: rol y propiedad.
- `ApiSesion.empresaIdDeSesion` resuelve la empresa con `PerfilEmpresaDAO`; no se confía en `empresaId` del cliente.
- Los DTO evitan filtrar `passwordHash`, rutas y nombres internos de archivo.
- La API comparte la cookie con los JSP; no hay doble login.

**Recomendaciones pendientes (no implementadas):**

- `changeSessionId()` en el login de la API.
- Token CSRF o cabecera personalizada para POST/PUT/PATCH/DELETE.
- `SameSite`/`secure` en la cookie cuando se use HTTPS.
- Rate limiting en login.

---

## 5. Coexistencia con la aplicación JSP

| Ruta | Uso | Cambia en Fase 3 |
|---|---|---|
| `/inicio` | Inicio según rol | No |
| `/ofertas` | Listado/detalle/CRUD JSP + JSON legacy | No (la API es independiente) |
| `/perfil` | Perfil candidato JSP | No |
| `/perfil-empresa` | Perfil empresa JSP | No |
| `/panel-empresa` | Panel empresa | No |
| `/admin` | Panel admin | No |
| `/archivo` | Descarga JSP existente | No |
| `/api/*` | **Nueva** API REST | Sí (nueva) |

No se modifican tablas, columnas, nombres de entidades ni la convención `id` / `usuario_id` / `empresa_id`.

---

## 6. Orden de implementación recomendado (Integrante 2)

1. **Infraestructura**
   - Revisar `ApiServletBase`, `ApiRespuesta`, `ApiJson`, `ApiJsonReader`.
   - Registrar/verificar `ApiAuthFilter`.

2. **Auth**
   - `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/sesion`.

3. **Lectura pública**
   - `GET /api/empresas`, `GET /api/empresas/{id}`, `GET /api/empresas/{id}/ofertas`.
   - `GET /api/ofertas`, `GET /api/ofertas/{id}` (con filtro `ACTIVA`).

4. **Usuarios**
   - `POST /api/usuarios`.
   - `GET /api/usuarios`, `GET /api/usuarios/{id}`, `PATCH /api/usuarios/{id}/estado`.

5. **Perfil de candidato**
   - `GET/PUT /api/perfiles/candidato`.
   - Foto y currículums (multipart).

6. **Perfil de empresa**
   - `GET/PUT /api/perfiles/empresa`.
   - Logo (multipart).

7. **Ofertas de escritura**
   - `POST`, `PUT`, `PATCH estado`, `DELETE`.
   - Verificación de propiedad.

8. **Archivos y administración**
   - `GET /api/archivos/{id}`.
   - `GET /api/admin/dashboard`, `GET /api/admin/auditoria`.

9. **Pruebas manuales** (ver sección 7).

**Construcción de servicios:** `RegistroService`, `PerfilService` y `ArchivoService` reciben `ServletContext`; en los servlets REST se instancian con `new PerfilService(getServletContext())`, etc. `AdminService` y los DAO no reciben contexto.

---

## 7. Verificación recomendada (manual, sin Maven)

No se debe ejecutar Maven, Tomcat ni SQL en esta actividad de diseño. Cuando el Integrante 2 implemente, debe probar:

| # | Prueba | Resultado esperado |
|---|---|---|
| 1 | Login válido | 200 + cookie `JSESSIONID` + usuario sin hash |
| 2 | Login inválido | 401 `CREDENCIALES_INVALIDAS` |
| 3 | Login de cuenta inactiva | 403 `CUENTA_INACTIVA` |
| 4 | Acceso a `/api/perfiles/candidato` sin cookie | 401 `NO_AUTENTICADO` |
| 5 | Candidato intenta `/api/admin/dashboard` | 403 `PROHIBIDO` |
| 6 | Empresa A edita oferta de empresa B | 403 `PROHIBIDO` |
| 7 | Público consulta oferta PAUSADA | 404 |
| 8 | Público lista ofertas | Solo `ACTIVA` |
| 9 | Registro con email repetido | 409 `CONFLICTO` |
| 10 | Registro con `rol=ADMINISTRADOR` | 400 |
| 11 | Subir CV > 5 MB | 413 |
| 12 | Subir ejecutable como CV | 400/415 |
| 13 | Descargar CV de otro usuario | 403 |
| 14 | Admin desactiva su propia cuenta | 409 `REGLA_NEGOCIO` |
| 15 | Empresa crea oferta enviando `empresaId` ajeno | Se ignora; se usa la empresa de la sesión |
| 16 | PATCH de estado con transición inválida | 409 `REGLA_NEGOCIO` |
| 17 | Respuesta de error 500 | Sin stack trace ni SQL |

---

## 8. Riesgos de arquitectura y decisiones

| Riesgo | Mitigación en el diseño |
|---|---|
| Duplicar lógica de negocio | Reutilizar servicios/DAOs existentes; los servlets REST solo adaptan HTTP. |
| Exponer entidades JPA directamente | DTO con `Map` seguro; nunca serializar entidades completas. |
| LazyInitializationException | Los repositorios ya usan `JOIN FETCH` en las relaciones que las vistas leen; el Integrante 2 debe reutilizar esos métodos. |
| Filtrar ofertas no activas | Forzar `ACTIVA` en la capa API; no confiar en `listarTodas()`. |
| Confiar en IDs del cliente | `ApiSesion` resuelve identidad y empresa desde sesión/BD. |
| Añadir dependencias | No se agregó Spring/Jackson/Gson; el lector/serializador JSON es local y mínimo. |
| Romper Sprint 2 | No se modifican servlets, JSP, entidades, tablas ni `web.xml`; solo se agregan clases nuevas. |
| `getPathInfo()` nulo por mapeo exacto | Mapear los servlets REST con comodín (`/api/recurso/*`) y leer segmentos con `ApiServletBase.segmentos()`. |
| Postulaciones inexistentes | No se diseñan endpoints; se documenta la ausencia. |

---

## 9. Resumen para el equipo

- La API es **session-based**, no token-based.
- La capa REST es **delgada**: valida, autoriza, adapta y delega.
- El núcleo de negocio sigue en `service` y `repository`.
- La base de datos **no cambia**: 7 tablas, PK `id`, FKs descriptivas.
- Los 33 endpoints están en `contrato_api.md`; las reglas de seguridad en `seguridad_api.md`; el detalle funcional en `diseno_api_rest.md`.
- La implementación y las pruebas son responsabilidad del Integrante 2.
- El cliente consumidor (Integrante 3) debe usar cookie de sesión, JSON y multipart para archivos.

---

*Documento de arquitectura. No se ejecutó la aplicación; no se afirma que la API funcione.*
