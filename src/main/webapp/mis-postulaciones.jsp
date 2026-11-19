<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/header.jsp">
    <jsp:param name="tituloPagina" value="Mis Postulaciones"/>
</jsp:include>

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Mis postulaciones</h1>
            <p class="text-muted" style="margin-top:0.35rem;">Historial de ofertas a las que te has postulado.</p>
        </div>
        <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-secondary">Explorar ofertas</a>
    </div>

    <div class="aviso aviso-info">
        <div>
            <strong>Modulo de postulaciones no disponible</strong>
            La plataforma todavia no registra postulaciones: esta parte del sistema no ha sido
            implementada, por lo que aqui no hay informacion que mostrar. No se muestran datos de
            ejemplo porque no existen en la base de datos.
        </div>
    </div>

    <div class="empty-state">
        <div class="empty-state-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
        </div>
        <h3>Sin postulaciones registradas</h3>
        <p>Cuando el modulo este disponible, aqui apareceran las ofertas a las que te hayas postulado.</p>
        <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-primary">Ver ofertas disponibles</a>
    </div>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>
