<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Filtrar por Ubicacion"/>
</jsp:include>

<div class="container section-sm">
    <div class="page-header">
        <h1>Filtrar por ubicacion</h1>
        <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-ghost btn-sm">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/></svg>
            Volver
        </a>
    </div>

    <div class="search-bar mb-3">
        <form action="${pageContext.request.contextPath}/ofertas" method="get" class="search-bar-inline">
            <input type="hidden" name="format" value="jsp"/>
            <input type="hidden" name="accion" value="filtrar"/>
            <div class="form-group">
                <input type="text" id="ubicacion" name="ubicacion" class="form-control" value="${param.ubicacion}" placeholder="Ej: San Salvador, Santa Ana..."/>
            </div>
            <button type="submit" class="btn btn-primary">Filtrar</button>
            <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-secondary">Ver todas</a>
        </form>
    </div>

    <c:if test="${not empty ofertas}">
        <p class="text-muted mb-2" style="font-size:0.9rem;">${ofertas.size()} resultado(s) encontrado(s)</p>
    </c:if>

    <c:choose>
        <c:when test="${empty ofertas}">
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/></svg>
                </div>
                <h3>No se encontraron ofertas</h3>
                <p>No hay ofertas para esta ubicacion. Intenta con otra ciudad.</p>
            </div>
        </c:when>
        <c:otherwise>
            <c:forEach var="o" items="${ofertas}">
                <div class="card mb-1">
                    <div class="card-header">
                        <div style="flex:1;">
                            <div class="card-title">
                                <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=detalle&id=${o.id}">${o.titulo}</a>
                            </div>
                            <div class="card-meta">
                                <span class="card-meta-item">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/></svg>
                                    ${o.ubicacion}
                                </span>
                                <span class="card-meta-item">${o.tipoContrato}</span>
                                <c:if test="${not empty o.salario}">
                                    <span class="card-meta-item">$<fmt:formatNumber value="${o.salario}" type="number" minFractionDigits="2"/></span>
                                </c:if>
                            </div>
                        </div>
                        <div class="card-actions">
                            <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=editar&id=${o.id}" class="btn btn-secondary btn-sm">Editar</a>
                        </div>
                    </div>
                    <c:if test="${not empty o.descripcion}">
                        <div class="card-body">
                            <p class="card-text">${o.descripcion}</p>
                        </div>
                    </c:if>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="footer.jsp" %>
