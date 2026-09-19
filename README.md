# EmpleaSV

Plataforma web para búsqueda y publicación de ofertas de empleo en El Salvador.

---

## Contexto

| Detalle | Valor |
|---------|-------|
| **Universidad** | Universidad Don Bosco |
| **Asignatura** | DWF901 - Desarrollo de Aplicaciones con Web Frameworks |
| **Sprint** | Sprint I |

### Objetivo del Sprint I

Desarrollar el primer incremento funcional de la plataforma EmpleaSV, estableciendo la configuración base del proyecto Java Web con arquitectura MVC.

---

## Tecnologías

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 | Lenguaje de programación |
| Maven | 3.x | Gestión de dependencias y build |
| Apache Tomcat | 10.1.59 | Servidor de aplicaciones |
| Jakarta Servlet | 6.0 | API de Servlets |
| Jakarta JSP/JSTL | 3.1 / 3.0 | Vistas dinámicas |
| MySQL | - | Base de datos (posteriormente con JDBC) |

---

## Arquitectura MVC

```
┌─────────────────────────────────────────────────────┐
│                    CLIENTE                          │
│                (Navegador Web)                      │
└───────────────────────┬─────────────────────────────┘
                        │ HTTP Request
                        ▼
┌─────────────────────────────────────────────────────┐
│                 CONTROLADOR                         │
│           com.empleasv.controller                   │
│              (Servlets)                             │
└───────────────────────┬─────────────────────────────┘
                        │
          ┌─────────────┴─────────────┐
          ▼                           ▼
┌──────────────────┐      ┌──────────────────┐
│      MODELO      │      │      VISTA       │
│   com.empleasv   │      │    webapp/       │
│   ├── model/     │      │   ├── WEB-INF/   │
│   ├── dao/       │      │   │   └── views/ │
│   └── data/      │      │   ├── css/       │
│                  │      │   ├── js/        │
│  (Dominio +      │      │   └── index.jsp  │
│   Persistencia)  │      │                  │
└──────────────────┘      └──────────────────┘
```

---

## Estructura del proyecto

```
EmpleaSV/
├── pom.xml
├── .gitignore
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── empleasv/
        │           ├── controller/    # Servlets/controladores
        │           ├── model/         # Entidades del dominio
        │           ├── dao/           # Acceso a datos
        │           ├── data/          # Persistencia
        │           └── util/          # Utilidades
        ├── resources/
        └── webapp/
            ├── WEB-INF/
            │   └── views/             # Vistas JSP protegidas
            ├── css/                   # Estilos
            ├── js/                    # JavaScript
            └── index.jsp              # Página de inicio
```

---

## Requisitos

- Java 21 JDK
- Apache Tomcat 10.1.x
- Maven 3.8+
- IntelliJ IDEA (recomendado)

---

## Compilar con Maven

```bash
# Compilar el proyecto
mvn clean compile

# Empaquetar como WAR
mvn clean package
```

El archivo `EmpleaSV.war` se genera en la carpeta `target/`.

---

## Ejecutar con Tomcat

1. Compilar el proyecto:
   ```bash
   mvn clean package
   ```

2. Copiar `target/EmpleaSV.war` a la carpeta `webapps/` de Tomcat

3. Iniciar Tomcat:
   ```bash
   # Linux/Mac
   ./bin/startup.sh

   # Windows
   bin\startup.bat
   ```

4. Abrir en el navegador:

   **http://localhost:8080/EmpleaSV/**

---

## Estado actual

| Componente | Estado |
|------------|--------|
| Configuración Maven | ✅ Completada |
| Arquitectura MVC | ✅ Organizada |
| Git y README | ✅ Configurados |
| POJOs del dominio | ⏳ Pendiente |
| Conexión JDBC/MySQL | ⏳ Pendiente |
| Servlets funcionales | ⏳ Pendiente |
| Vistas JSP | ⏳ Pendiente |

---

## Licencia

Proyecto académico - Universidad Don Bosco

