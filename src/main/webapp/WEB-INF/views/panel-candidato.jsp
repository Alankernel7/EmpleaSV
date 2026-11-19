<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Mi panel"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- Completitud del perfil: datos reales guardados en la base de datos --%>
<c:set var="campos" value="0" />
<c:if test="${not empty perfilCandidato.telefono}"><c:set var="campos" value="${campos + 1}" /></c:if>
<c:if test="${not empty perfilCandidato.direccionResidencia}"><c:set var="campos" value="${campos + 1}" /></c:if>
<c:if test="${not empty perfilCandidato.municipio}"><c:set var="campos" value="${campos + 1}" /></c:if>
<c:if test="${not empty perfilCandidato.departamento}"><c:set var="campos" value="${campos + 1}" /></c:if>
<c:if test="${not empty perfilCandidato.fotoPerfil}"><c:set var="campos" value="${campos + 1}" /></c:if>
<c:if test="${not empty perfilCandidato.curriculum}"><c:set var="campos" value="${campos + 1}" /></c:if>
<c:set var="porcentaje" value="${campos * 100 / 6}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Hola, <c:out value="${nombreUsuario}" /></h1>
            <p class="text-muted" style="margin-top:0.35rem;">Este es el resumen de tu cuenta de candidato.</p>
        </div>
        <a href="${ctx}/ofertas?format=jsp" class="btn btn-primary">Explorar ofertas</a>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <div class="dashboard-stats mb-3">
        <div class="stat-card">
            <div class="stat-card-label">Perfil completado</div>
            <div class="stat-card-value"><fmt:formatNumber value="${porcentaje}" maxFractionDigits="0"/>%</div>
            <div class="stat-card-change">${campos} de 6 datos registrados</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Curriculum</div>
            <div class="stat-card-value" style="font-size:1.25rem;">
                <c:choose>
                    <c:when test="${not empty perfilCandidato.curriculum}">Adjunto</c:when>
                    <c:otherwise>Sin adjuntar</c:otherwise>
                </c:choose>
            </div>
            <div class="stat-card-change">
                <a href="${ctx}/perfil?accion=curriculum">Gestionar curriculum</a>
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Ofertas destacadas</div>
            <div class="stat-card-value">${ofertasDestacadas.size()}</div>
            <div class="stat-card-change">Visibles en el inicio publico</div>
        </div>
    </div>

    <c:if test="${porcentaje < 100}">
        <div class="aviso aviso-aviso">
            <div>
                <strong>Tu perfil esta incompleto</strong>
                Completa tus datos y adjunta tu curriculum para que las empresas puedan conocerte mejor.
            </div>
            <a href="${ctx}/perfil" class="btn btn-primary btn-sm" style="margin-left:auto;">Completar</a>
        </div>
    </c:if>

    <div class="section-header">
        <h2>Accesos rapidos</h2>
    </div>
    <div class="grid-3 mb-3">
        <div class="info-card">
            <div class="info-card-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
            </div>
            <h3>Mi perfil</h3>
            <p>Actualiza tus datos de contacto, direccion y fotografia.</p>
            <a href="${ctx}/perfil">Ir a mi perfil</a>
        </div>
        <div class="info-card">
            <div class="info-card-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
            </div>
            <h3>Mi curriculum</h3>
            <p>Sube, reemplaza o elimina el archivo de tu curriculum.</p>
            <a href="${ctx}/perfil?accion=curriculum">Gestionar curriculum</a>
        </div>
        <div class="info-card">
            <div class="info-card-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
            </div>
            <h3>Buscar ofertas</h3>
            <p>Consulta las ofertas publicadas por las empresas registradas.</p>
            <a href="${ctx}/ofertas?format=jsp">Ver ofertas</a>
        </div>
    </div>

    <div class="aviso aviso-info">
        <div>
            <strong>Postulaciones y ofertas guardadas</strong>
            Estas funciones todavia no estan disponibles: no se han registrado postulaciones ni ofertas
            guardadas en la plataforma.
        </div>
    </div>

    <c:if test="${not empty ofertasDestacadas}">
        <div class="section-header">
            <h2>Ofertas destacadas</h2>
            <a href="${ctx}/ofertas?format=jsp" class="btn btn-ghost btn-sm">Ver todas</a>
        </div>
        <div class="grid-3">
            <c:forEach var="o" items="${ofertasDestacadas}" varStatus="s">
                <c:if test="${s.index < 3}">
                    <div class="featured-card">
                        <div class="featured-card-badge"><c:out value="${o.tipoContrato}"/></div>
                        <h3><c:out value="${o.titulo}"/></h3>
                        <p class="text-muted" style="font-size:0.85rem;"><c:out value="${o.ubicacion}"/></p>
                        <c:if test="${not empty o.descripcion}">
                            <p class="card-text"><c:out value="${fn:substring(o.descripcion, 0, 120)}"/><c:if test="${fn:length(o.descripcion) > 120}">...</c:if></p>
                        </c:if>
                        <a href="${ctx}/ofertas?format=jsp&accion=detalle&id=${o.id}" class="btn btn-outline btn-sm">Ver detalle</a>
                    </div>
                </c:if>
            </c:forEach>
        </div>
    </c:if>
</div>

<%@ include file="footer.jsp" %>
