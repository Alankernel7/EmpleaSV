<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Detalle de Oferta"/>
</jsp:include>

<div class="container section-sm">
    <c:choose>
        <c:when test="${empty oferta}">
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                </div>
                <h3>Oferta no encontrada</h3>
                <p>La oferta que buscas no existe o fue eliminada.</p>
                <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-primary">Volver al listado</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="page-header">
                <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-ghost btn-sm">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/></svg>
                    Volver
                </a>
            </div>

            <div class="detail-card">
                <div class="detail-card-header">
                    <div class="flex-between" style="flex-wrap:wrap; gap:0.75rem;">
                        <div>
                            <h1>${oferta.titulo}</h1>
                            <div class="card-meta" style="margin-top:0.5rem;">
                                <span class="card-meta-item">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/></svg>
                                    ${oferta.ubicacion}
                                </span>
                                <span class="card-meta-item">${oferta.tipoContrato}</span>
                                <c:if test="${not empty oferta.horario}">
                                    <span class="card-meta-item">${oferta.horario}</span>
                                </c:if>
                            </div>
                        </div>
                        <c:if test="${not empty oferta.estado}">
                            <span class="badge
                                <c:choose>
                                    <c:when test='${oferta.estado == "ACTIVA"}'>badge-success</c:when>
                                    <c:when test='${oferta.estado == "PAUSADA"}'>badge-warning</c:when>
                                    <c:otherwise>badge-danger</c:otherwise>
                                </c:choose>
                            " style="font-size:0.8rem; padding:0.3rem 0.75rem;">${oferta.estado}</span>
                        </c:if>
                    </div>
                </div>

                <div class="detail-card-body">
                    <div class="detail-row">
                        <span class="detail-label">Salario</span>
                        <span class="detail-value">
                            <c:choose>
                                <c:when test="${not empty oferta.salario}">
                                    $<fmt:formatNumber value="${oferta.salario}" type="number" minFractionDigits="2"/>
                                </c:when>
                                <c:otherwise>No especificado</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Tipo de contrato</span>
                        <span class="detail-value">${oferta.tipoContrato}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Horario</span>
                        <span class="detail-value">${not empty oferta.horario ? oferta.horario : 'No especificado'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Empresa ID</span>
                        <span class="detail-value">${oferta.empresaId}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Fecha de publicacion</span>
                        <span class="detail-value">
                            <c:choose>
                                <c:when test="${not empty oferta.fechaPublicacion}">${oferta.fechaPublicacion}</c:when>
                                <c:otherwise>No especificada</c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <c:if test="${not empty oferta.descripcion}">
                        <div class="detail-section">
                            <h3>Descripcion del puesto</h3>
                            <p style="line-height:1.8; color:var(--gray-700);">${oferta.descripcion}</p>
                        </div>
                    </c:if>

                    <c:if test="${not empty oferta.requisitos}">
                        <div class="detail-section">
                            <h3>Requisitos</h3>
                            <p style="line-height:1.8; color:var(--gray-700);">${oferta.requisitos}</p>
                        </div>
                    </c:if>
                </div>

                <div class="detail-actions">
                    <a href="#" class="btn btn-primary btn-lg" onclick="alert('La funcion de postulacion estara disponible proximamente.'); return false;">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                        Postularme
                    </a>
                    <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=editar&id=${oferta.id}" class="btn btn-secondary">Editar</a>
                    <form action="${pageContext.request.contextPath}/ofertas" method="post" style="display:inline;" onsubmit="return confirm('Eliminar esta oferta?')">
                        <input type="hidden" name="accion" value="eliminar"/>
                        <input type="hidden" name="id" value="${oferta.id}"/>
                        <button type="submit" class="btn btn-danger">Eliminar</button>
                    </form>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="footer.jsp" %>
