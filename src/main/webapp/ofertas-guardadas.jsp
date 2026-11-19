<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/header.jsp">
    <jsp:param name="tituloPagina" value="Ofertas Guardadas"/>
</jsp:include>

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Mis ofertas guardadas</h1>
            <p class="text-muted" style="margin-top:0.35rem;">Ofertas que marcaste para revisar mas tarde.</p>
        </div>
        <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-secondary">Explorar ofertas</a>
    </div>

    <div class="aviso aviso-info">
        <div>
            <strong>Modulo de ofertas guardadas no disponible</strong>
            La plataforma todavia no guarda ofertas favoritas: esta parte del sistema no ha sido
            implementada, por lo que aqui no hay informacion que mostrar. No se muestran datos de
            ejemplo porque no existen en la base de datos.
        </div>
    </div>

    <div class="empty-state">
        <div class="empty-state-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"/></svg>
        </div>
        <h3>No tienes ofertas guardadas</h3>
        <p>Cuando el modulo este disponible, aqui apareceran las ofertas que marques como favoritas.</p>
        <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-primary">Ver ofertas disponibles</a>
    </div>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>
