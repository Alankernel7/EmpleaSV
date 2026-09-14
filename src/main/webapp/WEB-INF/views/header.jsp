<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${param.tituloPagina} - EmpleaSV</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <nav class="navbar" id="navbar">
        <div class="nav-container">
            <a class="nav-brand" href="${pageContext.request.contextPath}/index.jsp">
                <span class="nav-brand-icon">E</span>
                EmpleaSV
            </a>

            <button class="nav-toggle" onclick="document.getElementById('navCenter').classList.toggle('active')" aria-label="Menu">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/></svg>
            </button>

            <div class="nav-center" id="navCenter">
                <a class="nav-link" href="${pageContext.request.contextPath}/index.jsp">Inicio</a>
                <a class="nav-link" href="${pageContext.request.contextPath}/ofertas?format=jsp">Ofertas</a>
                <a class="nav-link" href="${pageContext.request.contextPath}/empresas.jsp">Empresas</a>
                <a class="nav-link" href="${pageContext.request.contextPath}/ayuda.jsp">Ayuda</a>
            </div>

            <div class="nav-actions">
                <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-ghost btn-sm">Iniciar sesion</a>
                <a href="${pageContext.request.contextPath}/registro.jsp" class="btn btn-primary btn-sm">Registrarse</a>
            </div>
        </div>
    </nav>
    <main>
