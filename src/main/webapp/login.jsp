<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar sesion - EmpleaSV</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css?v=3">
</head>
<body>
    <jsp:include page="WEB-INF/views/nav.jsp" />

    <div class="auth-page">
        <div class="auth-card">
            <h1>Iniciar sesion</h1>
            <p class="subtitle">Ingresa tus credenciales para acceder a tu cuenta</p>

            <c:if test="${not empty mensajeError}">
                <div class="alert alert-error">
                    <c:out value="${mensajeError}"/>
                </div>
            </c:if>

            <c:if test="${not empty param.registro and param.registro == 'exitoso'}">
                <div class="alert alert-success">
                    Registro exitoso. Ahora puedes iniciar sesion.
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/auth" method="post">
                <input type="hidden" name="accion" value="login">

                <c:if test="${not empty errores}">
                    <div class="alert alert-error">
                        <c:forEach var="error" items="${errores}">
                            <div><c:out value="${error}"/></div>
                        </c:forEach>
                    </div>
                </c:if>

                <div class="form-group">
                    <label class="form-label" for="email">Correo electronico</label>
                    <input type="email" id="email" name="email" class="form-control" placeholder="tu@email.com" value="${fn:escapeXml(email)}" required autocomplete="email">
                </div>

                <div class="form-group">
                    <label class="form-label" for="password">Contrasena</label>
                    <input type="password" id="password" name="password" class="form-control" placeholder="Tu contrasena" required autocomplete="current-password">
                </div>

                <button type="submit" class="btn btn-primary btn-lg" style="width:100%;">Iniciar sesion</button>
            </form>

            <div class="auth-footer">
                No tienes una cuenta? <a href="${pageContext.request.contextPath}/auth?accion=registro">Registrate aqui</a>
            </div>
        </div>
    </div>
</body>
</html>