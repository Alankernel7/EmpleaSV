<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Mis Postulaciones"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Mis postulaciones</h1>
            <p class="text-muted" style="margin-top:0.35rem;">Historial de ofertas a las que te has postulado.</p>
        </div>
        <a href="${ctx}/ofertas?format=jsp" class="btn btn-secondary">Explorar ofertas</a>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <c:choose>
        <c:when test="${empty postulaciones}">
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                </div>
                <h3>Sin postulaciones registradas</h3>
                <p>Cuando te postules a una oferta, aparecera aqui con el estado de tu candidatura.</p>
                <a href="${ctx}/ofertas?format=jsp" class="btn btn-primary">Ver ofertas disponibles</a>
            </div>
        </c:when>
        <c:otherwise>
            <p class="text-muted mb-2" style="font-size:0.9rem;">${totalPostulaciones} postulacion(es) registrada(s)</p>

            <c:forEach var="p" items="${postulaciones}">
                <c:set var="claseEstado"
                       value="${p.estado == 'ACEPTADA' ? 'badge-success' : (p.estado == 'RECHAZADA' ? 'badge-danger' : (p.estado == 'REVISADA' ? 'badge-info' : 'badge-warning'))}" />
                <div class="postulacion-card">
                    <div class="postulacion-info">
                        <h4>
                            <c:choose>
                                <c:when test="${not empty p.oferta}">
                                    <a href="${ctx}/ofertas?format=jsp&accion=detalle&id=${p.oferta.id}">
                                        <c:out value="${p.oferta.titulo}"/>
                                    </a>
                                </c:when>
                                <c:otherwise>Oferta no disponible</c:otherwise>
                            </c:choose>
                        </h4>
                        <p>
                            <c:if test="${not empty p.oferta.empresa}">
                                <c:out value="${p.oferta.empresa.nombre}"/> &middot;
                            </c:if>
                            Enviada el ${fn:substring(p.fechaPostulacion, 0, 10)}
                        </p>
                        <p>
                            CV:
                            <c:choose>
                                <c:when test="${not empty p.curriculum}">
                                    <c:out value="${p.curriculum.nombreOriginal}"/>
                                </c:when>
                                <c:otherwise>No adjunto</c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                    <div class="fila-acciones">
                        <span class="badge ${claseEstado}"><c:out value="${p.estado}"/></span>
                        <c:if test="${not empty p.curriculum}">
                            <a href="${ctx}/postulaciones?accion=cv&id=${p.id}&inline=1" target="_blank"
                               rel="noopener" class="btn btn-outline btn-sm">Ver CV</a>
                            <a href="${ctx}/postulaciones?accion=cv&id=${p.id}" class="btn btn-ghost btn-sm">Descargar</a>
                        </c:if>
                        <a href="${ctx}/postulaciones?accion=detalle&id=${p.id}" class="btn btn-secondary btn-sm">Detalle</a>
                    </div>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="footer.jsp" %>
