# Documentacion Sprint I - EmpleaSV

Materia: DWF901  
Fecha: 14 de septiembre de 2026

---

## 1. Archivos creados

| Archivo | Ruta | Descripcion |
|---------|------|-------------|
| `header.jsp` | `src/main/webapp/WEB-INF/views/header.jsp` | Encabezado comun con barra de navegacion |
| `footer.jsp` | `src/main/webapp/WEB-INF/views/footer.jsp` | Pie de pagina comun |
| `listar.jsp` | `src/main/webapp/WEB-INF/views/listar.jsp` | Vista de listado de ofertas con busqueda |
| `detalle.jsp` | `src/main/webapp/WEB-INF/views/detalle.jsp` | Vista de detalle de una oferta |
| `registrar.jsp` | `src/main/webapp/WEB-INF/views/registrar.jsp` | Formulario de registro de oferta |
| `editar.jsp` | `src/main/webapp/WEB-INF/views/editar.jsp` | Formulario de edicion de oferta |
| `filtrar.jsp` | `src/main/webapp/WEB-INF/views/filtrar.jsp` | Vista de filtro por ubicacion |
| `styles.css` | `src/main/webapp/css/styles.css` | Hoja de estilos CSS para todas las vistas |

## 2. Archivos modificados

| Archivo | Ruta | Cambios realizados |
|---------|------|-------------------|
| `OfertaEmpleoServlet.java` | `src/main/java/com/empleasv/controller/` | Adicionado soporte MVC con forward a JSP; validaciones server-side; mantener compatibilidad JSON |

## 3. EM-17 - Vistas JSP/JSTL implementadas

Se crearon 7 vistas JSP utilizan JSTL (`c:forEach`, `c:choose`, `c:if`, `fmt:formatNumber`) y Expression Language (`${...}`). No se utilizan scriptlets Java.

### Funcionalidades por vista:

**listar.jsp**
- Listado completo de ofertas de empleo
- Barra de busqueda por titulo
- Boton para filtrar por ubicacion
- Boton para ver todas las ofertas
- Tarjetas con titulo, ubicacion, tipo contrato, salario y estado
- Botones de editar y eliminar por cada oferta
- Mensajes de exito y error via session flash

**detalle.jsp**
- Muestra todos los campos de una oferta: titulo, ubicacion, tipo contrato, horario, salario, estado, empresa ID, fecha publicacion, descripcion, requisitos
- Botones de editar y eliminar
- Enlace para volver al listado

**registrar.jsp**
- Formulario completo con todos los campos obligatorios marcados con *
- Campos: titulo, descripcion, requisitos, ubicacion, salario, tipo contrato (select), horario, estado (select), empresa ID
- HTML5 validation en cliente + validacion server-side
- Muestra errores de validacion al usuario

**editar.jsp**
- Mismo formulario que registrar pero precargado con datos existentes
- Campo ID como hidden
- Muestra errores de validacion al usuario

**filtrar.jsp**
- Formulario para filtrar por ubicacion
- Muestra cantidad de resultados encontrados
- Tarjetas con resultados

**header.jsp / footer.jsp**
- Encabezado y pie compartidos por todas las vistas
- Barra de navegacion con enlaces a listado y nueva oferta

### Navegacion:
- Todas las vistas acceden al Servlet via `?format=jsp` para indicar que se desea respuesta JSP
- Las acciones se manejan via parametro `accion` (listar, buscar, filtrar, detalle, registrar, editar)
- Los POST se redirigen con flash messages via HttpSession

## 4. EM-18 - Validaciones implementadas

Se implementaron validaciones server-side en el metodo `validarCampos()` del Servlet:

| Campo | Validacion |
|-------|-----------|
| Titulo | Obligatorio, maximo 200 caracteres |
| Descripcion | Obligatoria |
| Requisitos | Opcional (se valida si se proporciona) |
| Ubicacion | Obligatoria, maximo 150 caracteres |
| Tipo de contrato | Obligatorio |
| Horario | Obligatorio, maximo 100 caracteres |
| Salario | Formato numerico (Double) cuando se proporciona, no negativo |
| Empresa ID | Obligatorio, debe ser numerico positivo |
| ID (edicion) | Obligatorio, debe ser numerico |

Los errores se envian al usuario via request attributes y se muestran en las vistas correspondientes.  
Los formularios tambien utilizan atributos HTML5 `required`, `min`, `max`, `maxlength` para validacion en cliente.

## 5. EM-20 - Pruebas realizadas

### Prueba 1: Compilacion Maven
- **Comando:** `mvn clean compile`
- **Resultado:** EXITOSA
- **Detalles:** 6 archivos fuente compilados correctamente con Java 21

### Prueba 2: Empaquetado Maven (WAR)
- **Comando:** `mvn package -DskipTests`
- **Resultado:** EXITOSA
- **Detalles:** Archivo `EmpleaSV.war` generado correctamente

### Prueba 3: Contenido del WAR
- **Verificacion:** Se verifico que el WAR contiene todos los archivos necesarios
- **Resultado:** EXITOSA
- **Detalles:** 7 JSPs, 1 CSS, 6 clases .class, 2 JARs de JSTL

### Prueba 4: Estructura del proyecto
- **Verificacion:** Se verifico la estructura de directorios completa
- **Resultado:** EXITOSA
- **Detalles:** Todas las capas MVC presentes (model, dao, data, controller, views)

### Prueba 5: Conexión JDBC
- **Verificacion:** Se reviso ConexionDB.java
- **Resultado:** CONFIGURADO
- **Detalles:** Conexion a `jdbc:mysql://localhost:3306/empleasv` con usuario root. No se ejecuto prueba de conexion porque requiere MySQL corriendo.

### Prueba 6: DAO de Empresa
- **Verificacion:** Se reviso EmpresaDAO.java
- **Resultado:** IMPLEMENTADO
- **Detalles:** Metodos `listarTodas()` y `obtenerPorId()` disponibles. No se ejecuto prueba porque requiere datos en la BD.

### Prueba 7: DAO de OfertaEmpleo
- **Verificacion:** Se reviso OfertaEmpleoDAO.java
- **Resultado:** IMPLEMENTADO
- **Detalles:** CRUD completo: listarTodas, obtenerPorId, buscarPorTitulo, filtrarPorUbicacion, registrar, actualizar, eliminar. No se ejecuto prueba porque requiere datos en la BD.

### Prueba 8: Validaciones server-side
- **Verificacion:** Se reviso el metodo `validarCampos()` en el Servlet
- **Resultado:** IMPLEMENTADO
- **Detalles:** Validacion de todos los campos obligatorios y formatos. Los errores se envian a las vistas via request attributes.

### Prueba 9: Navegacion Servlet-JSP
- **Verificacion:** Se reviso el flujo de forward/redirect en el Servlet
- **Resultado:** IMPLEMENTADO
- **Detalles:** GET con format=jsp hace forward a JSP. POST redirige con flash messages. Compatibilidad JSON mantenada.

## 6. Incidencias encontradas

### Incidencia 1: MySQL no disponible para pruebas de integracion
- **Descripcion:** No se pudieron ejecutar pruebas de conexion JDBC ni pruebas de DAO porque MySQL no esta disponible en el entorno de ejecucion actual.
- **Impacto:** Bajo. La compilacion y estructura del codigo son correctas. Las pruebas de integracion deben ejecutarse cuando MySQL este disponible.
- **Solucion:** Verificar manualmente desplegando en Tomcat con MySQL corriendo.

### Incidencia 2: Maven y Java no estan en PATH del sistema
- **Descripcion:** Maven y Java no se encontraban en el PATH del sistema. Se localizaron en directorios alternativos.
- **Impacto:** Ninguno sobre el proyecto. Solo afecta la ejecucion de comandos desde la terminal.
- **Solucion:** Se configuro JAVA_HOME y PATH manualmente para ejecutar Maven.

## 7. Limitaciones actuales

1. **Sin autenticacion:** No hay sistema de login, usuarios ni roles. Cualquier usuario puede acceder a todas las funcionalidades.

2. **Sin datos de empresa en formularios:** Los formularios de registro y edicion de ofertas solicitan el `empresa_id` como campo de texto numerico. No se muestra un select con las empresas disponibles porque `EmpresaDAO` no tenia este requisito previo.

3. **JSON manual:** La serializacion a JSON se realiza manualmente sin libreria externa (Gson, Jackson). Esto funciona pero no es ideal para produccion.

4. **Sin paginacion:** El listado muestra todas las ofertas sin paginacion.

5. **Sin CSS deBootstrap o framework:** Se utilizo CSS custom sin frameworks externos.

## 8. Funcionalidades pendientes

Las siguientes funcionalidades NO fueron implementadas porque pertenecen a historias de usuario futuras (EM-2 a EM-9):

- Sistema de autenticacion y login
- Gestion de usuarios y roles
- Gestion completa de empresas (CRUD via interfaz web)
- Paginacion de resultados
- Exportacion de datos
- Estadisticas y reportes
- Integracion con API externas

## 9. Resultado de Maven

```
BUILD SUCCESS
Tiempo total: 4.602 s
Compilados: 6 archivos fuente
Target: 21
```

## 10. Confirmacion de compilacion

**Si, el proyecto compila correctamente.** Se ejecutaron `mvn clean compile` y `mvn package` exitosamente. El archivo WAR `EmpleaSV.war` se genero correctamente con todos los archivos necesarios.

## 11. Estructura final del proyecto

```
src/main/java/com/empleasv/
├── controller/
│   └── OfertaEmpleoServlet.java
├── dao/
│   ├── EmpresaDAO.java
│   └── OfertaEmpleoDAO.java
├── data/
│   └── ConexionDB.java
└── model/
    ├── Empresa.java
    └── OfertaEmpleo.java

src/main/webapp/
├── css/
│   └── styles.css
├── index.jsp
├── js/
│   (vacio)
└── WEB-INF/
    └── views/
        ├── header.jsp
        ├── footer.jsp
        ├── listar.jsp
        ├── detalle.jsp
        ├── registrar.jsp
        ├── editar.jsp
        └── filtrar.jsp
```
