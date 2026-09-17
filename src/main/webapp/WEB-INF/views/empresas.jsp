<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/header.jsp">
    <jsp:param name="tituloPagina" value="Empresas"/>
</jsp:include>

<!-- Hero empresas -->
<section class="hero" style="padding: 3rem 0 4rem;">
    <div class="container">
        <div class="hero-content">
            <h1 style="font-size:2.25rem;">Encuentra al talento que necesitas</h1>
            <p>Publica tus ofertas y conecta con los mejores profesionales de El Salvador.</p>
            <div class="flex gap-1" style="justify-content:center; flex-wrap:wrap;">
                <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=registrar" class="btn btn-lg" style="background:var(--white); color:var(--primary); font-weight:700;">Publicar oferta</a>
                <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-outline btn-lg" style="color:var(--white); border-color:rgba(255,255,255,0.4);">Iniciar sesion</a>
            </div>
        </div>
    </div>
</section>

<div class="container section">
    <div class="section-header">
        <h2>Empresas que confian en nosotros</h2>
        <p>Empresas lideres en El Salvador utilizan EmpleaSV para encontrar talento</p>
    </div>

    <div class="grid-4">

        <c:forEach var="empresa" items="${empresas}">

            <div class="company-card">

                <div class="company-card-logo">
                    ${empresa.iniciales}
                </div>

                <h4>
                    ${empresa.nombre}
                </h4>

                <p>
                    ${empresa.categoria} | ${empresa.cantidadOfertas}
                    <c:choose>
                        <c:when test="${empresa.cantidadOfertas == 1}">
                            oferta
                        </c:when>
                        <c:otherwise>
                            ofertas
                        </c:otherwise>
                    </c:choose>
                </p>

            </div>

        </c:forEach>

    </div>

    <!-- Beneficios para empresas -->
    <div class="section" style="padding-top:3rem;">
        <div class="section-header">
            <h2>Por que publicar en EmpleaSV?</h2>
            <p>Beneficios de utilizar nuestra plataforma</p>
        </div>
        <div class="grid-3">
            <div class="info-card">
                <div class="info-card-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
                </div>
                <h3>Publicacion rapida</h3>
                <p>Publica tu oferta en minutos y llega a cientos de candidatos calificados.</p>
            </div>
            <div class="info-card">
                <div class="info-card-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/></svg>
                </div>
                <h3>Talento calificado</h3>
                <p>Accede a un pool de candidatos con habilidades verificadas y perfiles completos.</p>
            </div>
            <div class="info-card">
                <div class="info-card-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
                </div>
                <h3>Resultados medibles</h3>
                <p>Obten estadisticas y reportes sobre el rendimiento de tus ofertas.</p>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>
