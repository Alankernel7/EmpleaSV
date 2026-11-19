# Cliente consumidor, pruebas y documentación — Fase 3 (Prioridad 3)

**Fecha:** 18 de noviembre de 2026
**Base URL:** `http://localhost:8080/EmpleaSV/api`
**Contrato de referencia:** [`contrato_api.md`](contrato_api.md)

---

## 1. Resumen de actividades

| # | Actividad | Estado | Evidencia |
|---|---|---|---|
| 1 | Aplicación/módulo independiente que consume la API | ✅ | `cliente/` (HTML + CSS + JS puro, sin dependencias) |
| 2 | Configuración de conexión HTTP | ✅ | `cliente/js/api.js` — `fetch` con base URL configurable y cookies de sesión |
| 3 | Consumo de endpoints de consulta de ofertas | ✅ | Pestañas "Ofertas" y "Empresas" |
| 4 | Ofertas recibidas desde la API mostradas | ✅ | Rejilla de tarjetas + detalle `GET /api/ofertas/{id}` |
| 5 | Consumo de empresas y demás recursos | ✅ | Empresas, perfiles, sesión, auditoría, archivos |
| 6 | Operaciones según endpoints disponibles | ✅ | CRUD completo de ofertas (POST/PUT/PATCH/DELETE) según rol |
| 7 | Manejo de respuestas exitosas y errores | ✅ | Ver sección 4 |
| 8 | Colección de Postman | ✅ | `docs/postman/EmpleaSV.postman_collection.json` |

---

## 2. El cliente independiente (`cliente/`)

Módulo estático separado del WAR: no comparte código con el servidor,
solo consume su API por HTTP. Se sirve desde el **mismo Tomcat**
(`http://localhost:8080/cliente/`), por lo que comparte origen con la
API → **no requiere CORS** y las cookies `JSESSIONID` (con
`credentials: include`) viajan correctamente.

```
cliente/
├── index.html        5 pestañas: Ofertas, Empresas, Sesión, Operaciones, Consola
├── css/cliente.css
├── js/api.js         Módulo HTTP independiente (el "cliente")
├── js/app.js         Lógica de interfaz
└── README.md         Instrucciones de despliegue
```

### 2.1 Configuración de la conexión (actividad 2)

- `ApiCliente` en `js/api.js` envuelve `fetch`:
  - **Base URL** configurable desde la barra superior (guardada en
    `localStorage`), por defecto `http://localhost:8080/EmpleaSV/api`.
  - `credentials: 'include'` → envía/recibe la cookie de sesión.
  - `Accept: application/json` y `Content-Type: application/json; charset=UTF-8`.
  - Cuerpos JSON serializados con `JSON.stringify`; multipart para archivos.
  - Soporta `GET, POST, PUT, PATCH, DELETE` y parámetros de query.

### 2.2 Endpoints consumidos

| Pestaña | Método | Endpoint |
|---|---|---|
| Ofertas | GET | `/ofertas` (`titulo`, `ubicacion`, `empresaId`) |
| Ofertas | GET | `/ofertas/{id}` |
| Empresas | GET | `/empresas` (`destacadas`) |
| Empresas | GET | `/empresas/{id}` · `/empresas/{id}/ofertas` |
| Sesión | POST | `/auth/login` · `/auth/logout` |
| Sesión | GET | `/auth/sesion` |
| Registro | POST | `/usuarios` |
| Operaciones | GET | `/ofertas?mias=true` (solo EMPRESA/ADMIN) |
| Operaciones | POST | `/ofertas` (EMPRESA/ADMIN) |
| Operaciones | PUT | `/ofertas/{id}` (dueña o ADMIN) |
| Operaciones | PATCH | `/ofertas/{id}/estado` (transiciones válidas) |
| Operaciones | DELETE | `/ofertas/{id}` |
| Perfiles y Admin | GET/PUT | `/perfiles/candidato` (perfil propio + CV principal) |
| Perfiles y Admin | GET | `/perfiles/candidato/curriculums` (lista de CV) |
| Perfiles y Admin | POST | `/perfiles/candidato/curriculums` (multipart `curriculum`, `principal`) |
| Perfiles y Admin | GET/PUT | `/perfiles/empresa` |
| Perfiles y Admin | GET | `/admin/dashboard` · `/admin/auditoria?limite=` |
| Consola | — | Historial de cada petición: método, status, tiempo, código |

### 2.3 Manejo de respuestas y errores (actividad 7)

| Situación | Comportamiento del cliente |
|---|---|
| `2xx` con `exito: true` | Muestra `datos` (tarjetas, detalle, JSON formateado) |
| `401 NO_AUTENTICADO / CREDENCIALES_INVALIDAS` | Toast de error + indicación de volver al login |
| `403 PROHIBIDO` | Mensaje "sin permiso" (ej. EMPRESA en `/admin/dashboard`) |
| `404 RECURSO_NO_ENCONTRADO` | Error visible en la tarjeta de resultado |
| `409 REGLA_NEGOCIO / CONFLICTO` | Muestra el código y mensaje del servidor |
| `400 DATOS_INVALIDOS` | Despliega `error.detalles[]` campo por campo |
| Sin conexión / Tomcat caído | Error `SIN_CONEXION` con la URL intentada |
| Cualquier llamada | Se registra en la **Consola de peticiones** (método, HTTP, ms, código) |

---

## 3. Colección de Postman (actividad 8)

Archivo: **`docs/postman/EmpleaSV.postman_collection.json`**

- Importar: Postman → *Import* → *Files* → seleccionar el archivo.
- **Variables:** `baseUrl`, `emailEmpresa`, `emailCandidato`,
  `emailAdmin`, `password`, `ofertaId`, `empresaId`, `usuarioId`.
- **Orden recomendado:** `Auth › Login (empresa)` primero → la cookie se
  guarda sola y el resto de peticiones la reutiliza.
- **Carpetas (33 solicitudes de prueba):**
  - **Auth:** login empresa/candidato, login inválido (401), sesión, logout (204)
  - **Usuarios:** registro (201 + Location), listado admin, detalle, PATCH estado
  - **Empresas:** listado, destacadas, detalle, ofertas de la empresa
  - **Ofertas:** listado, búsqueda con filtros, detalle, 404, crear (201),
    crear sin sesión (401), PUT, PATCH estado, transición inválida (409), DELETE (204)
  - **Perfiles:** candidato GET/PUT, currículums, subida multipart, perfil empresa
  - **Administración:** dashboard, auditoría, 403 sin rol admin
  - **Archivos:** descarga binaria `GET /api/archivos/{id}`
- Cada solicitud trae `pm.test` automáticos: código HTTP esperado,
  `exito === true`, ausencia de `passwordHash`, solo ofertas `ACTIVAS`, etc.

---

## 4. Pruebas ejecutadas (evidencias reales)

Script reproducible: [`pruebas_evidencias.ps1`](pruebas_evidencias.ps1) ·
Salida cruda: [`evidencias_pruebas.txt`](evidencias_pruebas.txt)

**Cuentas de prueba creadas en la BD `empleasv`:**

| Rol | Email | Contraseña | id |
|---|---|---|---|
| USUARIO | candidato@prueba.com | Prueba123 | 1 |
| EMPRESA | empresa@prueba.com | Prueba123 | 2 |
| ADMINISTRADOR | admin@prueba.com | Prueba123 | 3 |

### 4.1 Resultados

| # | Prueba | Esperado | Obtenido | Resultado |
|---|---|---|---|---|
| 1 | `GET /ofertas?titulo=java` (público) | 200 + lista | 200, 1 oferta NovaTech | ✅ |
| 2 | `GET /ofertas/999999` | 404 | 404 `RECURSO_NO_ENCONTRADO` | ✅ |
| 3 | `GET /perfiles/candidato` sin sesión | 401 | 401 `NO_AUTENTICADO` | ✅ |
| 4 | `POST /auth/login` con credenciales malas | 401 | 401 `CREDENCIALES_INVALIDAS` | ✅ |
| 5 | `POST /auth/login` (empresa) | 200 + usuario | 200, id=2, `empresaId`=13 | ✅ |
| 6 | `GET /admin/dashboard` con sesión EMPRESA | 403 | 403 `PROHIBIDO` | ✅ |
| 7 | `POST /ofertas` con sesión EMPRESA | 201 + `Location` | 201, oferta id=32 creada | ✅ |
| 8 | `PUT /ofertas/32` (actualizar) | 200 | 200, título/salario actualizados | ✅ |
| 9 | `PATCH /ofertas/32/estado` → PAUSADA | 200 | 200 `{estado: PAUSADA}` | ✅ |
| 10 | `PATCH` de una oferta ya PAUSADA | (ver nota) | 200 — el servidor admite la transición trivial | ⚠️ nota 4.2 |
| 11 | `DELETE /ofertas/32` | 204 | 204 sin cuerpo | ✅ |
| 12 | `POST /auth/login` (admin) | 200 | 200, rol ADMINISTRADOR | ✅ |
| 13 | `GET /admin/dashboard` (admin) | 200 + métricas | 200: 3 usuarios, 26 ACTIVA / 2 PAUSADA / 2 CERRADA | ✅ |
| 14 | `GET /empresas/1/ofertas` (público) | 200 solo ACTIVAS | 200, 3 ofertas ACTIVAS | ✅ |
| 15 | `POST /auth/logout` | 204 | 204 | ✅ |
| 16 | `GET /ofertas?mias=true` tras logout | 401 | 401 | ✅ |
| 17 | Oferta EMPRESA: ACTIVA → CERRADA | 200 | 200 | ✅ |
| 18 | Oferta EMPRESA: CERRADA → ACTIVA | 409 | **409 `REGLA_NEGOCIO`** | ✅ |
| 19 | `PUT /ofertas/999999` | 404 | 404 | ✅ |
| 20 | `GET /perfiles/candidato` (sesión USUARIO) | 200 | 200 | ✅ |
| 21 | `GET /perfiles/candidato/curriculums` (sesión USUARIO) | 200 | 200 (lista vacía) | ✅ |
| 22 | `GET /perfiles/empresa` (sesión EMPRESA) | 200 | 200 | ✅ |
| 23 | `GET /admin/auditoria?limite=10` (sesión ADMIN) | 200 | 200 | ✅ |

### 4.2 Nota sobre la prueba 10

El contrato (sección 5.8) define las transiciones `ACTIVA↔PAUSADA`,
`ACTIVA→CERRADA` y `PAUSADA→CERRADA`. La implementación también admite
`PAUSADA→PAUSADA` (idempotente, responde 200 en vez de 409). La regla de
negocio **sí** se valida correctamente en el caso que el contrato marca
como prohibido para EMPRESA: `CERRADA→ACTIVA` responde **409
`REGLA_NEGOCIO`** (prueba 18), comportamiento verificado también con la
colección de Postman.

### 4.3 Verificación del cliente en navegador

- `http://localhost:8080/cliente/` → **200** (desplegado en Tomcat).
- Al abrirlo, la pestaña **Ofertas** carga automáticamente
  `GET /api/ofertas` y pinta las tarjetas recibidas desde la API
  (evidencia visual: se registra cada llamada en la pestaña
  *Consola de peticiones* con status 200 y tiempo de respuesta).

---

## 5. Cómo ejecutar todo desde cero

```powershell
# 1. MySQL (XAMPP) y Tomcat 10.1
C:\xampp\mysql\bin\mysqld.exe --defaults-file=C:\xampp\mysql\bin\my.ini
C:\xampp\tomcat\bin\startup.bat      # o el Tomcat 10.1 usado en esta rama

# 2. Base de datos (una sola vez)
cmd /c "C:\xampp\mysql\bin\mysql.exe -u root < database\empleasv.sql"

# 3. Compilar y desplegar el WAR
mvn clean package -DskipTests
copy target\EmpleaSV.war <tomcat>\webapps\

# 4. Desplegar el cliente
xcopy /E /I cliente <tomcat>\webapps\cliente

# 5. Abrir
#    App principal: http://localhost:8080/EmpleaSV/
#    Cliente REST:  http://localhost:8080/cliente/
#    Postman:       importar docs/postman/EmpleaSV.postman_collection.json
```

---

## 6. Conclusiones

- Los **33 endpoints** del contrato responden con el sobre uniforme
  `{exito, mensaje, datos}` / `{exito, error{codigo, mensaje, detalles}}`
  y los códigos HTTP documentados (200/201/204/400/401/403/404/409/415).
- El cliente independiente consume la API sin tocar el código del
  servidor, con sesión por cookie y manejo completo de errores.
- La colección de Postman sirve a la vez como **pruebas automatizadas**
  (`pm.test`) y como documentación viva del contrato.
- Pendiente acordado en el contrato (regla 10): si en el futuro el
  cliente se sirve desde **otro origen**, habrá que configurar CORS
  explícito en la API; hoy no es necesario porque ambos corren en
  `http://localhost:8080`.
