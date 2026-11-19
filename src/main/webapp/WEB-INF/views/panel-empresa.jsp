<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Panel empresa"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1><c:out value="${perfilEmpresa.nombreEmpresa}" /></h1>
            <p class="text-muted" style="margin-top:0.35rem;">
                Bienvenido, <c:out value="${perfilEmpresa.nombreResponsable}" />.
                Aqui gestionas las ofertas publicadas por tu empresa.
            </p>
        </div>
        <div class="fila-acciones">
            <a href="${ctx}/perfil-empresa" class="btn btn-secondary">Mi perfil empresarial</a>
            <a href="${ctx}/ofertas?format=jsp&accion=registrar" class="btn btn-primary">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
                Nueva oferta
            </a>
        </div>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <div class="dashboard-stats mb-3">
        <div class="stat-card">
            <div class="stat-card-label">Ofertas publicadas</div>
            <div class="stat-card-value">${totalOfertas}</div>
            <div class="stat-card-change">Total de la empresa</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Activas</div>
            <div class="stat-card-value">${ofertasActivas}</div>
            <div class="stat-card-change">Visibles para los candidatos</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Pausadas</div>
            <div class="stat-card-value">${ofertasPausadas}</div>
            <div class="stat-card-change">Retiradas temporalmente</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Cerradas</div>
            <div class="stat-card-value">${ofertasCerradas}</div>
            <div class="stat-card-change">Finalizadas</div>
        </div>
    </div>

    <div class="section-header">
        <h2>Mis ofertas</h2>
        <a href="${ctx}/ofertas?format=jsp" class="btn btn-ghost btn-sm">Ver listado completo</a>
    </div>

    <c:choose>
        <c:when test="${empty ofertas}">
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M21 13.255A23.931 23.931 0 0112 15c-3.21 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20a2 2 0 01-2-2V7a2 2 0 012-2h14a2 2 0 012 2v11a2 2 0 01-2 2H5z"/></svg>
                </div>
                <h3>Todavia no has publicado ofertas</h3>
                <p>Crea tu primera oferta para que los candidatos puedan conocerte.</p>
                <a href="${ctx}/ofertas?format=jsp&accion=registrar" class="btn btn-primary">Crear primera oferta</a>
            </div>
        </c:when>
        <c:otherwise>
            <p class="text-muted mb-2" style="font-size:0.9rem;">${ofertas.size()} oferta(s) de tu empresa</p>
            <c:forEach var="o" items="${ofertas}">
                <div class="card mb-1">
                    <div class="card-header">
                        <div style="flex:1;">
                            <div class="card-title">
                                <a href="${ctx}/ofertas?format=jsp&accion=detalle&id=${o.id}"><c:out value="${o.titulo}"/></a>
                            </div>
                            <div class="card-meta">
                                <span class="card-meta-item"><c:out value="${o.ubicacion}"/></span>
                                <span class="card-meta-item"><c:out value="${o.tipoContrato}"/></span>
                                <c:if test="${not empty o.salario}">
                                    <span class="card-meta-item">$<fmt:formatNumber value="${o.salario}" type="number" minFractionDigits="2"/></span>
                                </c:if>
                                <c:if test="${not empty o.estado}">
                                    <span class="badge ${o.estado == 'ACTIVA' ? 'badge-success' : (o.estado == 'PAUSADA' ? 'badge-warning' : 'badge-danger')}"><c:out value="${o.estado}"/></span>
                                </c:if>
                            </div>
                        </div>
                        <div class="card-actions">
                            <a href="${ctx}/ofertas?format=jsp&accion=editar&id=${o.id}" class="btn btn-secondary btn-sm">Editar</a>
                            <form action="${ctx}/ofertas" method="post" style="display:inline;"
                                  onsubmit="return confirm('Eliminar esta oferta?');">
                                <input type="hidden" name="accion" value="eliminar"/>
                                <input type="hidden" name="format" value="jsp"/>
                                <input type="hidden" name="id" value="${o.id}"/>
                                <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
                            </form>
                        </div>
                    </div>
                    <c:if test="${not empty o.descripcion}">
                        <div class="card-body">
                            <p class="card-text"><c:out value="${o.descripcion}"/></p>
                        </div>
                    </c:if>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="footer.jsp" %>
