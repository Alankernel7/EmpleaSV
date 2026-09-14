<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar sesion - EmpleaSV</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <nav class="navbar">
        <div class="nav-container">
            <a class="nav-brand" href="${pageContext.request.contextPath}/index.jsp">
                <span class="nav-brand-icon">E</span>
                EmpleaSV
            </a>
            <div class="nav-center">
                <a class="nav-link" href="${pageContext.request.contextPath}/index.jsp">Inicio</a>
                <a class="nav-link" href="${pageContext.request.contextPath}/ofertas?format=jsp">Ofertas</a>
                <a class="nav-link" href="${pageContext.request.contextPath}/empresas.jsp">Empresas</a>
            </div>
            <div class="nav-actions">
                <a href="${pageContext.request.contextPath}/registro.jsp" class="btn btn-primary btn-sm">Registrarse</a>
            </div>
        </div>
    </nav>

    <div class="auth-page">
        <div class="auth-card">
            <h1>Iniciar sesion</h1>
            <p class="subtitle">Ingresa tus credenciales para acceder a tu cuenta</p>

            <div class="alert alert-info" style="font-size:0.85rem;">
                Esta funcion estara disponible proximamente.
            </div>

            <form action="#" method="post" onsubmit="return false;">
                <div class="form-group">
                    <label class="form-label" for="email">Correo electronico</label>
                    <input type="email" id="email" name="email" class="form-control" placeholder="tu@email.com" disabled>
                </div>

                <div class="form-group">
                    <label class="form-label" for="password">Contrasena</label>
                    <input type="password" id="password" name="password" class="form-control" placeholder="Tu contrasena" disabled>
                </div>

                <div class="flex-between" style="margin-bottom:1.5rem;">
                    <div class="form-check">
                        <input type="checkbox" id="remember" disabled>
                        <label for="remember">Recordarme</label>
                    </div>
                    <a href="#" style="font-size:0.85rem; font-weight:500;">Olvidaste tu contrasena?</a>
                </div>

                <button type="submit" class="btn btn-primary btn-lg" style="width:100%;" disabled>Iniciar sesion</button>
            </form>

            <div class="auth-footer">
                No tienes una cuenta? <a href="${pageContext.request.contextPath}/registro.jsp">Registrate aqui</a>
            </div>
        </div>
    </div>
</body>
</html>
