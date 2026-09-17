
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EmpleaSV - Encuentra tu proximo empleo</title>
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
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/></svg>
            </button>
            <div class="nav-center" id="navCenter">
                <a class="nav-link active" href="${pageContext.request.contextPath}/index.jsp">Inicio</a>
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
        <!-- Hero -->
        <section class="hero">
            <div class="container">
                <div class="hero-content">
                    <h1>Encuentra tu proximo empleo</h1>
                    <p>Explora miles de ofertas de empleo en El Salvador. Tu oportunidad esta aqui.</p>
                    <div class="hero-search">
                        <div class="hero-search-input">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                            <input type="text" placeholder="Puesto, empresa o palabra clave">
                        </div>
                        <div class="hero-search-input">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                            <input type="text" placeholder="Ubicacion">
                        </div>
                        <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-primary">Buscar empleos</a>
                    </div>
                </div>
            </div>
        </section>

        <!-- Stats -->
        <section class="stats-bar">
            <div class="container">
                <div class="stats-grid">
                    <div class="stat-item">
                        <h3>500+</h3>
                        <p>Ofertas activas</p>
                    </div>
                    <div class="stat-item">
                        <h3>200+</h3>
                        <p>Empresas</p>
                    </div>
                    <div class="stat-item">
                        <h3>1,000+</h3>
                        <p>Candidatos</p>
                    </div>
                    <div class="stat-item">
                        <h3>100+</h3>
                        <p>Empleos nuevos</p>
                    </div>
                </div>
            </div>
        </section>

        <!-- Ofertas destacadas -->
        <section class="section">
            <div class="container">
                <div class="section-header">
                    <h2>Ofertas destacadas</h2>
                    <p>Las mejores oportunidades de empleo seleccionadas para ti</p>
                </div>
                <div class="grid-3">

                    <c:forEach var="oferta" items="${ofertasDestacadas}">

                        <%-- Divide el título en palabras para generar las iniciales --%>
                        <c:set var="palabrasTitulo"
                               value="${fn:split(oferta.titulo, ' ')}" />

                        <%-- Primera letra de la primera palabra --%>
                        <c:set var="inicial1"
                               value="${fn:toUpperCase(
                                   fn:substring(palabrasTitulo[0], 0, 1)
                               )}" />

                        <%-- Segunda letra: primera letra de la segunda palabra --%>
                        <c:set var="inicial2" value="" />

                        <c:if test="${fn:length(palabrasTitulo) > 1}">
                            <c:set var="inicial2"
                                   value="${fn:toUpperCase(
                                       fn:substring(palabrasTitulo[1], 0, 1)
                                   )}" />
                        </c:if>


                        <div class="featured-card">

                            <%-- Badge --%>
                            <div class="featured-card-badge">
                                <span class="badge badge-success">
                                    Nueva
                                </span>
                            </div>


                            <%-- Iniciales del título --%>
                            <div class="featured-card-icon">
                                ${inicial1}${inicial2}
                            </div>


                            <%-- Título de la oferta --%>
                            <h4>
                                ${oferta.titulo}
                            </h4>


                            <%-- Nombre de la empresa --%>
                            <p class="company">
                                ${oferta.empresa.nombre}
                            </p>


                            <%-- Información de la oferta --%>
                            <div class="card-meta">

                                <%-- Ubicación --%>
                                <span class="card-meta-item">
                                    ${oferta.ubicacion}
                                </span>


                                <%-- Salario --%>
                                <span class="card-meta-item">
                                    <c:choose>

                                        <c:when test="${not empty oferta.salario}">
                                            $<fmt:formatNumber
                                                value="${oferta.salario}"
                                                minFractionDigits="2"
                                                maxFractionDigits="2"
                                            />
                                        </c:when>

                                        <c:otherwise>
                                            Salario no especificado
                                        </c:otherwise>

                                    </c:choose>
                                </span>


                                <%-- Tipo de contrato --%>
                                <span class="card-meta-item">
                                    ${oferta.tipoContrato}
                                </span>

                            </div>

                        </div>

                    </c:forEach>

                </div>
                <div class="text-center mt-3">
                    <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-primary btn-lg">Ver todas las ofertas</a>
                </div>
            </div>
        </section>

        <!-- Empresas destacadas -->
        <section class="section" style="background: var(--white); border-top: 1px solid var(--gray-200); border-bottom: 1px solid var(--gray-200);">
            <div class="container">
                <div class="section-header">
                    <h2>Empresas destacadas</h2>
                    <p>Empresas que confian en EmpleaSV para encontrar talento</p>
                </div>
                <div class="grid-4">

                    <c:forEach var="empresa" items="${empresasDestacadas}">

                        <div class="company-card">

                            <div class="company-card-logo">
                                ${empresa.iniciales}
                            </div>

                            <h4>${empresa.nombre}</h4>

                        </div>

                    </c:forEach>

                </div>
            </div>
        </section>

        <!-- Para candidatos -->
        <section class="section">
            <div class="container">
                <div class="section-header">
                    <h2>Para candidatos</h2>
                    <p>Encuentra la oportunidad perfecta para tu carrera</p>
                </div>
                <div class="grid-3">
                    <div class="info-card">
                        <div class="info-card-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                        </div>
                        <h3>Busca empleo</h3>
                        <p>Explora cientos de ofertas filtradas por ubicacion, salario y tipo de contrato.</p>
                    </div>
                    <div class="info-card">
                        <div class="info-card-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                        </div>
                        <h3>Crea tu perfil</h3>
                        <p>Muestra tu experiencia, habilidades y educacion a las mejores empresas.</p>
                    </div>
                    <div class="info-card">
                        <div class="info-card-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
                        </div>
                        <h3>Postulate</h3>
                        <p>Envia tu postulacion con un solo clic y da el siguiente paso en tu carrera.</p>
                    </div>
                </div>
                <div class="text-center mt-3">
                    <a href="${pageContext.request.contextPath}/registro.jsp" class="btn btn-primary btn-lg">Crear cuenta gratis</a>
                </div>
            </div>
        </section>

        <!-- Para empresas -->
        <section class="cta-section">
            <div class="container">
                <div class="cta-card">
                    <div class="cta-card-content">
                        <h2>Encuentra al talento que necesitas</h2>
                        <p>Publica tus ofertas y conecta con los mejores profesionales de El Salvador.</p>
                        <div class="flex gap-1" style="flex-wrap:wrap;">
                            <a href="${pageContext.request.contextPath}/registro.jsp" class="btn btn-lg">Publicar oferta</a>
                            <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-outline btn-lg">Iniciar sesion</a>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </main>

    <footer class="footer">
        <div class="container">
            <div class="footer-grid">
                <div>
                    <div class="footer-brand">EmpleaSV</div>
                    <p class="footer-desc">Plataforma de oportunidades laborales para El Salvador. Conectamos talento con las mejores empresas del pais.</p>
                </div>
                <div>
                    <div class="footer-title">Plataforma</div>
                    <ul class="footer-links">
                        <li><a href="${pageContext.request.contextPath}/index.jsp">Inicio</a></li>
                        <li><a href="${pageContext.request.contextPath}/ofertas?format=jsp">Ofertas de empleo</a></li>
                        <li><a href="${pageContext.request.contextPath}/empresas.jsp">Empresas</a></li>
                    </ul>
                </div>
                <div>
                    <div class="footer-title">Candidatos</div>
                    <ul class="footer-links">
                        <li><a href="${pageContext.request.contextPath}/perfil-candidato.jsp">Mi perfil</a></li>
                        <li><a href="${pageContext.request.contextPath}/mis-postulaciones.jsp">Mis postulaciones</a></li>
                        <li><a href="${pageContext.request.contextPath}/ofertas-guardadas.jsp">Ofertas guardadas</a></li>
                    </ul>
                </div>
                <div>
                    <div class="footer-title">Soporte</div>
                    <ul class="footer-links">
                        <li><a href="${pageContext.request.contextPath}/ayuda.jsp">Ayuda</a></li>
                        <li><a href="${pageContext.request.contextPath}/ayuda.jsp#nosotros">Nosotros</a></li>
                        <li><a href="${pageContext.request.contextPath}/ayuda.jsp#contacto">Contacto</a></li>
                    </ul>
                </div>
            </div>
            <div class="footer-bottom">
                <span>&copy; 2026 EmpleaSV. Todos los derechos reservados.</span>
                <div class="footer-bottom-links">
                    <a href="#">Terminos</a>
                    <a href="#">Privacidad</a>
                </div>
            </div>
        </div>
    </footer>

    <script>
        window.addEventListener('scroll', function() {
            var navbar = document.getElementById('navbar');
            if (window.scrollY > 10) { navbar.classList.add('scrolled'); } else { navbar.classList.remove('scrolled'); }
        });
    </script>
</body>
</html>
