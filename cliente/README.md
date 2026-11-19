# Cliente independiente — EmpleaSV (Fase 3, Prioridad 3)

Aplicación web independiente que consume la API REST de EmpleaSV
(`http://localhost:8080/EmpleaSV/api`). No forma parte del WAR: es un
módulo estático (HTML + CSS + JavaScript puro, sin dependencias) que se
sirve desde el mismo servidor Tomcat, por lo que comparte origen con la
API y **no requiere configuración CORS**.

## Estructura

```
cliente/
├── index.html        Interfaz con 5 pestañas
├── css/cliente.css   Estilos
└── js/
    ├── api.js        Módulo HTTP independiente (fetch, sesión, errores)
    └── app.js        Lógica de interfaz
```

## Despliegue

1. Levantar Tomcat 10.1 con el WAR de EmpleaSV desplegado.
2. Copiar esta carpeta al directorio `webapps` de Tomcat:

   ```bat
   xcopy /E /I cliente C:\ruta\apache-tomcat\webapps\cliente
   ```

3. Abrir <http://localhost:8080/cliente/>.

## Funciones

| Pestaña | Endpoints consumidos |
|---|---|
| Ofertas | `GET /api/ofertas` (filtros `titulo`, `ubicacion`, `empresaId`), `GET /api/ofertas/{id}` |
| Empresas | `GET /api/empresas`, `GET /api/empresas/{id}`, `GET /api/empresas/{id}/ofertas` |
| Sesión / Registro | `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/sesion`, `POST /api/usuarios` |
| Operaciones (CRUD) | `GET /api/ofertas?mias=true`, `POST`, `PUT`, `PATCH /estado`, `DELETE /api/ofertas/{id}` |
| Perfiles y Admin | `GET/PUT /api/perfiles/candidato`, `GET/POST /api/perfiles/candidato/curriculums` (multipart), `GET/PUT /api/perfiles/empresa`, `GET /api/admin/dashboard`, `GET /api/admin/auditoria` |
| Consola | Historial de cada petición: método, HTTP status, tiempo, código de la API |

## Manejo de errores (según contrato)

- Se verifica siempre `exito` antes de leer `datos`.
- `401 NO_AUTENTICADO / CREDENCIALES_INVALIDAS` → aviso para volver al login.
- `403 PROHIBIDO` → mensaje de sin permiso.
- `404 RECURSO_NO_ENCONTRADO`, `409 REGLA_NEGOCIO / CONFLICTO`, `400 DATOS_INVALIDOS`
  (con `detalles[]` campo a campo) → se muestran en la tarjeta de estado y en el toast.
- Sin conexión con Tomcat → error `SIN_CONEXION` con instrucciones.

## Cuentas de prueba

| Rol | Email | Contraseña |
|---|---|---|
| Candidato (USUARIO) | `candidato@prueba.com` | `Prueba123` |
| Empresa (EMPRESA) | `empresa@prueba.com` | `Prueba123` |
| Administrador | `admin@prueba.com` | `Prueba123` |

La URL base es configurable desde la barra superior (se guarda en
`localStorage`).
